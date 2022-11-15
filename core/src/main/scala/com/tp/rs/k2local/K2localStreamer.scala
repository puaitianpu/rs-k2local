package com.tp.rs.k2local

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.{CommitterSettings, ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.pattern.after
import akka.stream.scaladsl.RunnableGraph
import akka.stream.{ActorAttributes, Supervision}
import com.tp.rs.k2local.config.K2localStreamerConfig
import com.typesafe.scalalogging.Logger
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
  * K2local 数据处理流程接口定义
  * @tparam K Kafka key data type
  * @tparam V Kafka value data type
  * @tparam E Event data type
  * @tparam A Target type
  */
trait K2localStreamer[K, V, E, A] {

  import PassThroughFunctor._

  val system: ActorSystem[_]

  protected val logger: Logger = Logger(getClass)

  val config: K2localStreamerConfig[K, V]

  /**
    * Kafka 消费者配置
    */
  lazy val consumerSettings: ConsumerSettings[K, V] =
    ConsumerSettings(system.toClassic, config.consumerKeyDeserializer, config.consumerValueDeserializer)
      .withClientId(java.util.UUID.randomUUID().toString.toUpperCase)
      .withGroupId(config.groupId)

  private[this] val decider: Supervision.Decider = {
    case NonFatal(e) =>
      system.toClassic.log.warning("consuming flow error! will resume. reason: {}", e)
      Supervision.Resume
  }

  /**
    * 通用执行流程:
    * 1. 从Kafka获取消息数据源， Merge 源数据到流并根据匹配大小或者时间窗口grouped合并消息处理
    * 2. 包装消息的数据和偏移量信息
    * 3. 解析Kafka消息
    * 4. 转换消息格式
    * 5. 数据聚合
    * 6. 加载数据
    * 7. 提交偏移量
    * @param ec ExecutionContext
    * @return
    */
  def flow()(implicit ec: ExecutionContext): RunnableGraph[DrainingControl[Done]] =
    Consumer
      .committablePartitionedSource(consumerSettings, Subscriptions.topics(config.topicIn)) // 通过工程方法创建一个消费者源
      .flatMapMerge(config.maxPartitions, _._2) // 将源中的消息先Merge
      .groupedWithin(config.batchSize, config.batchWindow) // group 分组处理， 先满足 batchSize 条消息或者满足 batchWindow 窗口时间
      .map(ms => PassThrough(ms, foldOffsets(ms.map(_.committableOffset)))) // 包装消息并处理消息的偏移量
      .map(_.map(extractor)) // 通用处理流程，将Kafka消息: CommittableMessage[K, V] 解析成 E
      .map(_.map(_.flatMap(transfer))) // 通用处理流程，将消息类型 E 转换为 Option[A]
      .map(_.map(aggregate)) // 数据聚合
      .mapAsync(1)(x => loaderWithRetry(x.data, x.offsets.offsets.values.sum).map(_ => x.offsets)) // 带重试的加载数据
      //.mapAsync(5)((x: CommittableOffsetBatch) => commitOffsetsWithRetry(x.commitScaladsl(), commitBudge, x.offsets.values.sum)(ec, system.toClassic.scheduler))
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .toMat(Committer.sink(CommitterSettings(system)))(DrainingControl.apply)

  /**
    * 将 Kafka 消息中的 Value 解析出为 E
    * @param messages Kafka 消息列表
    * @return
    */
  def extractor(messages: Seq[CommittableMessage[K, V]]): Seq[E]

  /**
    * 将数据类型 E 转换为 Option[A]
    * @param e E 数据
    * @return
    */
  def transfer(e: E): Option[A]

  /**
    * 数据聚合
    * @param rows 数据列表
    * @return
    */
  def aggregate(rows: Seq[A]): Seq[A]

  /**
    * 数据加载
    * @param rs 数据集
    * @param offsets 偏移量
    * @param ec ExecutionContext
    * @return
    */
  def loader(rs: Seq[A], offsets: Long)(implicit ec: ExecutionContext): Future[_]

  protected val reloadBudget: List[Int] = {
    val n                        = 32
    lazy val fibs: LazyList[Int] = 1 #:: 1 #:: fibs.zip(fibs.tail).map(x => x._1 + x._2)
    fibs.take(n).toList
  }

  protected val commitBudge: Seq[FiniteDuration] = reloadBudget.take(20).map(_.milliseconds)

  private[this] def foldOffsets(os: Seq[ConsumerMessage.CommittableOffset]) =
    os.foldLeft(CommittableOffsetBatch.empty)((batch, p) => batch.updated(p))

  /**
    * 带重试的加载数据
    * @param rows 数据
    * @param offsets 偏移量
    * @param ec ExecutionContext
    */
  private[this] def loaderWithRetry(rows: Seq[A], offsets: Long)(implicit ec: ExecutionContext): Future[Unit] = {
    val number   = 10
    val interval = 100.milliseconds

    val p = Promise[Unit]

    def execute(xs: List[Int]): Unit = xs match {
      case Nil =>
        logger.info("loading canceled!")
        p.failure(new Exception("max retry executed!"))
      case y :: ys =>
        val result = loader(rows, offsets)
        result.onComplete {
          case Success(_) =>
            logger.info("execute loader success!")
            p.success(())
          case Failure(e) =>
            e.printStackTrace()
            logger.info(
              "error: {}, message: {}, rows size: {}, will retry after {} for times, now {} times.",
              e.getClass.getName,
              e.getMessage,
              rows.length,
              interval,
              number,
              number - ys.length
            )
            system.toClassic.scheduler.scheduleOnce(interval * y)(execute(ys))
        }
    }

    execute(reloadBudget.take(number))

    val f = after(10.minutes, system.toClassic.scheduler)(Future.failed(new Exception()))
    Future.firstCompletedOf(p.future :: f :: Nil)
  }

  /*@deprecated("reconstruct")
  private[this] def commitOffsetsWithRetry[T](f: => Future[T], delays: Seq[FiniteDuration], offsets: Long)(
    implicit ec: ExecutionContext,
    scheduler: akka.actor.Scheduler): Future[T] = {
    logger.info("commit offsets sum: {}", offsets)
    f.recoverWith {
      case _ if delays.nonEmpty =>
        logger.info("retried! budget: {}", delays.length)
        after(delays.head, scheduler)(commitOffsetsWithRetry(f, delays.tail, offsets)(ec, scheduler))
    }
  }*/

}

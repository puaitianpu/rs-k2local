package com.tp.rs.k2local

import java.io.File
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

import akka.actor.typed.ActorSystem
import akka.kafka.ConsumerMessage
import com.github.tototoshi.csv.CSVWriter
import com.tp.rs.k2local.config.K2localStreamerConfig
import com.tp.rs.k2local.dao.EventCsvDataDAO
import com.tp.rs.k2local.database.DatabaseComponent
import com.tp.rs.k2local.idl.{AdxEvent, EventCsvData}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

object AdxEventK2localStreamer {

  def apply()(implicit system: ActorSystem[_], dc: DatabaseComponent): AdxEventK2localStreamer = new AdxEventK2localStreamer()

}

class AdxEventK2localStreamer()(implicit val system: ActorSystem[_], val dc: DatabaseComponent)
    extends K2localStreamer[String, Array[Byte], AdxEvent, AdxEvent] {

  private[this] val dao: EventCsvDataDAO = EventCsvDataDAO()

  override val config: K2localStreamerConfig[String, Array[Byte]] =
    K2localStreamerConfig(new StringDeserializer, new ByteArrayDeserializer, "k2local.adx-event")

  /**
    * 将 Kafka 消息中的 Value 解析出为 E
    *
    * @param messages Kafka 消息列表
    * @return
    */
  override def extractor(messages: Seq[ConsumerMessage.CommittableMessage[String, Array[Byte]]]): Seq[AdxEvent] =
    messages.map(m => Try(AdxEvent.parseFrom(m.record.value))).collect {
      case Success(value) => value
    }

  /**
    * 将数据类型 E 转换为 Option[A]
    *
    * @param e E 数据
    * @return
    */
  override def transfer(e: AdxEvent): Option[AdxEvent] = Some(e)

  /**
    * 数据聚合
    *
    * @param rows 数据列表
    * @return
    */
  override def aggregate(rows: Seq[AdxEvent]): Seq[AdxEvent] = rows

  /**
    * 数据加载
    *
    * @param rs      数据集
    * @param offsets 偏移量
    * @param ec      ExecutionContext
    * @return
    */
  override def loader(rs: Seq[AdxEvent], offsets: Long)(implicit ec: ExecutionContext): Future[_] =
    rs match {
      case xs if xs.nonEmpty =>
        logger.info("Seq[AdxEvent] size is {}, offsets: {}", xs.length, offsets)
        val timestamp = System.currentTimeMillis()
        val filename  = timestamp.toString.concat(".csv")
        val filepath  = filePathOf(config.csvFilePath, timestamp).concat("/").concat(filename)
        val file      = fileOf(filepath)
        val write     = CSVWriter.open(file)
        write.writeAll(formatted(xs))
        write.close()
        dao.create(eventCsvDataOf(filename, filepath, xs.length, offsets, timestamp))
      case _ =>
        Future.successful(0)
    }

  private def eventCsvDataOf(filename: String, filepath: String, fileLines: Int, offsets: Long, timestamp: Long): EventCsvData =
    EventCsvData(
      filename = filename,
      filepath = filepath,
      fileLines = fileLines,
      offsets = offsets,
      createdAt = timestamp
    )

  private def fileOf(filepath: String): File = {
    val file = new File(filepath)
    if (!file.exists()) file.createNewFile()
    file
  }

  /**
    * 根据路径和时间错生成文件存放路径
    * @param path 存放目录
    * @param timestamp 时间戳
    * @return
    */
  private def filePathOf(path: String, timestamp: Long): String = {
    val date      = Instant.ofEpochMilli(timestamp).atZone(ZoneId.of(config.timeZone)).toOffsetDateTime
    val formatter = DateTimeFormatter.ofPattern("YYYY/MM/dd/HH")
    val result    = if (path.endsWith("/")) path else path.concat("/")
    val f         = new File(path)
    if (!f.exists()) f.mkdirs()
    result.concat(formatter.format(date))
  }

  // 将数据格式化成数据仓库存储格式
  private def formatted(rs: Seq[AdxEvent]) =
    rs.map { x =>
      Seq(
        x.requestId,
        timestampToString(x.timestamp),
        timestampToString(x.timestamp0),
        x.longitude,
        x.latitude,
        x.ecpm,
        x.dealPrice,
        x.amount,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        x.impExpireAt,
        x.clickExpireAt,
        0L,
        0L,
        0L,
        0L,
        0L,
        x.impressionType,
        x.pubAppAdId,
        x.pubAppId,
        x.pubId,
        x.dspAppId,
        x.dspId,
        x.dspCode,
        x.adGroupId,
        x.adWidth,
        x.adHeight,
        x.imgRealWidth,
        x.imgRealHeight,
        x.placementWidth,
        x.placementHeight,
        0,
        0,
        0,
        0,
        0,
        x.eventType.value,
        x.valid.value,
        x.carrier.value,
        x.osType.value,
        x.connectionType.value,
        x.adType.value,
        x.isAgApp.value,
        x.dspBiddingType.value,
        x.pubApiType.value,
        x.dspApiType.value,
        x.dealType.value,
        x.adCreativeType.value,
        x.adInteractionType.value,
        x.useType.value,
        x.dropType.value,
        x.isVideoAd.value,
        x.hasMatchedVideo.value,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        x.apiVersion.utf8Clear,
        x.mcc.utf8Clear,
        x.ip,
        x.deviceModel.utf8Clear,
        x.idfa.utf8Clear,
        x.imei.utf8Clear,
        x.imsi.utf8Clear,
        x.mac.utf8Clear,
        x.androidId.utf8Clear,
        x.googleAdId.utf8Clear,
        x.deviceId.utf8Clear,
        x.ua.utf8Clear,
        x.pubAppAdKey,
        x.pubAppKey,
        x.dspName,
        x.adTitle.utf8Clear,
        x.adPackageName.utf8Clear,
        x.adImgUrl.utf8Clear,
        x.adIconUrl.utf8Clear,
        x.adDeeplinkUrl.utf8Clear,
        x.adTargetUrl.utf8Clear,
        x.adDownloadUrl.utf8Clear,
        x.adRedirectDownloadUrl.utf8Clear,
        x.adHtml.utf8Clear,
        x.channelId,
        x.subChannelId,
        x.mid,
        x.sdkVersion,
        x.clientIp
      )
    }

  implicit class UC(str: String) {
    def utf8Clear: String = {
      val result = if (str.contains("\u0000")) str.replace("\u0000", "") else str
      if (result.contains("\\u0000")) str.replace("\\u0000", "") else result
    }
  }

  private def timestampToString(timestamp: Long) = {
    val datetime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.of(config.timeZone)).toOffsetDateTime
    DateTimeFormatter.ISO_DATE_TIME.format(datetime)
  }

}

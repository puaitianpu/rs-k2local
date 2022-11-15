package com.tp.rs.k2local

import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import cats.Functor

// Kafka 数据经过流处理包装
// T: Message Data Value Type
// offsets: 偏移量数据
final case class PassThrough[T](data: T, offsets: CommittableOffsetBatch)

// PassThrough Functor
object PassThroughFunctor {

  implicit val passThroughFunctor: Functor[PassThrough] = new Functor[PassThrough] {
    override def map[A, B](fa: PassThrough[A])(f: A => B): PassThrough[B] = PassThrough(f(fa.data), fa.offsets)
  }

}

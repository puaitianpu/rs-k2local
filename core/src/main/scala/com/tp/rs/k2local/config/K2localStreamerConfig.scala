package com.tp.rs.k2local.config

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.Deserializer

import scala.concurrent.duration._

object K2localStreamerConfig {

  def apply[K, V](kd: Deserializer[K], vd: Deserializer[V], config: String): K2localStreamerConfig[K, V] =
    K2localStreamerConfig(kd, vd, ConfigFactory.load().getConfig(config))

  def apply[K, V](kd: Deserializer[K], vd: Deserializer[V], config: Config): K2localStreamerConfig[K, V] =
    K2localStreamerConfig(
      consumerKeyDeserializer = kd,
      consumerValueDeserializer = vd,
      topicIn = config.getString("topic-in"),
      groupId = config.getString("group-id"),
      batchSize = config.getInt("batch-size"),
      batchWindow = config.getDuration("batch-time-window").toMillis.milliseconds,
      maxPartitions = config.getInt("max-partitions"),
      parallelism = config.getInt("parallelism"),
      csvFilePath = config.getString("csv-data-path"),
      timeZone = config.getString("time-zone")
    )

}

final case class K2localStreamerConfig[K, V](consumerKeyDeserializer: Deserializer[K],
                                             consumerValueDeserializer: Deserializer[V],
                                             topicIn: String,
                                             groupId: String,
                                             batchSize: Int,
                                             batchWindow: FiniteDuration,
                                             maxPartitions: Int,
                                             parallelism: Int,
                                             csvFilePath: String,
                                             timeZone: String)

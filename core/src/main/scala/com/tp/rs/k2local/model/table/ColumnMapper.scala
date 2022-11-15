package com.tp.rs.k2local.model.table

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}

import com.tp.rs.k2local.idl._
import slick.ast.BaseTypedType
import slick.lifted.MappedProjection

/**
  * 数据库表字段类型匹配
  */
trait ColumnMapper extends BaseTable {

  import dc.profile.api._

  // Timestamp 转换成 Long
  implicit class T2L(t: Timestamp) {
    def asLong: Long = t.getTime
  }

  // Long 转换成 Timestamp
  implicit class L2T(l: Long) {
    def asTimestamp: Timestamp = Timestamp.from(Instant.ofEpochMilli(l))
  }

  implicit class L2S(l: Long) {

    def asTimeString: String = this.asTimeStringWithOffset()

    def asTimeStringWithOffset(offset: Int = 8): String =
      Instant
        .ofEpochMilli(l)
        .atOffset(ZoneOffset.ofHours(offset))
        .format(DateTimeFormatter.ISO_DATE_TIME)

  }

  implicit class I2S(i: Instant) {

    def asInstantString: String = asInstantStringWithOffset()

    def asInstantStringWithOffset(offset: Int = 8): String = {
      i.atOffset(ZoneOffset.ofHours(offset))
        .format(DateTimeFormatter.ISO_DATE_TIME)
    }
  }

  implicit class TimestampLongMapping(ts: Rep[Timestamp]) {
    def asLong: MappedProjection[Long, Timestamp] = ts <> (tF._1, tF._2)
  }

  implicit class TimestampInstantMapping(ts: Rep[Timestamp]) {
    def asInstant: MappedProjection[Instant, Timestamp] = ts <> (tI._1, tI._2)
  }

  private[this] val tF: (Timestamp => Long, Long => Option[Timestamp]) = (
    { t: Timestamp =>
      t.getTime
    }, { l: Long =>
      Option(new Timestamp(l))
    }
  )

  private[this] val tI: (Timestamp => Instant, Instant => Option[Timestamp]) = (
    { t: Timestamp =>
      t.toInstant
    }, { l: Instant =>
      Option(Timestamp.from(l))
    }
  )

  // 数据相关字段转换为对应 PB 枚举类型
  implicit val boolColumnType: BaseTypedType[Bool]                       = MappedColumnType.base(_.value, Bool.fromValue)
  implicit val dspApiTypeColumnType: BaseTypedType[DspApiType]           = MappedColumnType.base(_.value, DspApiType.fromValue)
  implicit val pubApiTypeColumnType: BaseTypedType[PubApiType]           = MappedColumnType.base(_.value, PubApiType.fromValue)
  implicit val adTypeColumnType: BaseTypedType[AdType]                   = MappedColumnType.base(_.value, AdType.fromValue)
  implicit val biddingTypeColumnType: BaseTypedType[BiddingType]         = MappedColumnType.base(_.value, BiddingType.fromValue)
  implicit val osTypeColumnType: BaseTypedType[OSType]                   = MappedColumnType.base(_.value, OSType.fromValue)
  implicit val validTypeColumnType: BaseTypedType[ValidType]             = MappedColumnType.base(_.value, ValidType.fromValue)
  implicit val eventTypeColumnType: BaseTypedType[EventType]             = MappedColumnType.base(_.value, EventType.fromValue)
  implicit val carrierColumnType: BaseTypedType[Carrier]                 = MappedColumnType.base(_.value, Carrier.fromValue)
  implicit val connectionTypeColumnType: BaseTypedType[ConnectionType]   = MappedColumnType.base(_.value, ConnectionType.fromValue)
  implicit val creativeTypeColumnType: BaseTypedType[CreativeType]       = MappedColumnType.base(_.value, CreativeType.fromValue)
  implicit val interactionTypeColumnType: BaseTypedType[InteractionType] = MappedColumnType.base(_.value, InteractionType.fromValue)
  implicit val dealTypeTypeColumnType: BaseTypedType[DealType]           = MappedColumnType.base(_.value, DealType.fromValue)
  implicit val csvLoadStatus: BaseTypedType[CsvLoadStatus]               = MappedColumnType.base(_.value, CsvLoadStatus.fromValue)
  implicit val useType: BaseTypedType[UseType]                           = MappedColumnType.base(_.value, UseType.fromValue)
  implicit val dropType: BaseTypedType[DropType]                         = MappedColumnType.base(_.value, DropType.fromValue)

}

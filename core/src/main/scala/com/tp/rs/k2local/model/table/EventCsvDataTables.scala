package com.tp.rs.k2local.model.table

import java.sql.Timestamp

import com.tp.rs.k2local.idl.{CsvLoadStatus, EventCsvData}

@SuppressWarnings(Array("org.wartremover.warts.Nothing"))
trait EventCsvDataTables extends ColumnMapper {

  import dc.profile.api._

  val eventCsvData = TableQuery[EventCsvDataTable]((tag: Tag) => new EventCsvDataTable(tag, "csv_data_record"))

  class EventCsvDataTable(tag: Tag, table: String) extends Table[EventCsvData](tag, table) {

    def id        = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def filename  = column[String]("filename", O.SqlType("TEXT"), O.Default(""))
    def filepath  = column[String]("filepath", O.SqlType("TEXT"), O.Default(""))
    def fileLines = column[Int]("file_lines", O.SqlType("INTEGER"), O.Default(0))
    def offsets   = column[Long]("offsets", O.SqlType("Long"), O.Default(0L))
    def status    = column[CsvLoadStatus]("status", O.SqlType("SMALLINT"), O.Default(CsvLoadStatus.NEW))
    def createdAt = column[Timestamp]("created_at", O.SqlType("timestamptz default now()"))
    def loadedAt  = column[Timestamp]("loaded_at", O.SqlType("timestamptz default now()"))

    def * = (id, filename, filepath, fileLines, offsets, status, createdAt.asLong, loadedAt.asLong).mapTo[EventCsvData]
  }

}

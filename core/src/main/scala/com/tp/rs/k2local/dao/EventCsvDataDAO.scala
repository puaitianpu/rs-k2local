package com.tp.rs.k2local.dao

import com.tp.rs.k2local.database.DatabaseComponent
import com.tp.rs.k2local.idl.EventCsvData

import scala.concurrent.{ExecutionContext, Future}

object EventCsvDataDAO {
  def apply()(implicit dc: DatabaseComponent): EventCsvDataDAO = new EventCsvDataDAO {
    lazy override val dc: DatabaseComponent = dc
  }
}

trait EventCsvDataDAO extends DAO {

  import dc.profile.api._

  def create(x: EventCsvData)(implicit ec: ExecutionContext): Future[Int] = {
    val q = tables.eventCsvData += x
    dc.db.run(q.transactionally)
  }

}

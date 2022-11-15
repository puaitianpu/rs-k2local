package com.tp.rs.k2local.model

import com.tp.rs.k2local.database.DatabaseComponent
import com.tp.rs.k2local.model.table.EventCsvDataTables

object Tables {
  def apply()(implicit dc: DatabaseComponent): Tables = new Tables()
}

@SuppressWarnings(Array("org.wartremover.warts.Nothing"))
class Tables()(implicit val dc: DatabaseComponent) extends EventCsvDataTables

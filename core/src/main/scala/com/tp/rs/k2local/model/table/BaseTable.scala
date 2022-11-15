package com.tp.rs.k2local.model.table

import com.tp.rs.k2local.database.DatabaseComponent

trait BaseTable {

  val dc: DatabaseComponent

  protected def textType(n: Int): String = dc.profile match {
    case slick.jdbc.PostgresProfile => "text"
    case _                          => s"VARCHAR($n)"
  }

}

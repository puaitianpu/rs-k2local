package com.tp.rs.k2local.database

import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcBackend.Database

/**
  * 数据库组件接口定义
  */
trait DatabaseComponent {

  val profile: JdbcProfile
  val db: Database

}

object DatabaseComponent extends DatabaseComponent {

  val profile: JdbcProfile = slick.jdbc.PostgresProfile
  val db                   = Database.forConfig("database.main")

}

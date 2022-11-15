package com.tp.rs.k2local.dao

import com.tp.rs.k2local.model.Tables
import com.tp.rs.k2local.model.table.ColumnMapper
import com.typesafe.scalalogging.Logger
import slick.lifted.CanBeQueryCondition

trait DAO extends ColumnMapper {

  import dc.profile.api._

  protected val logger: Logger = Logger(getClass)

  protected val tables: Tables = Tables()(dc)

  implicit final class MaybeFilter[E, U](query: Query[E, U, Seq]) {

    def maybeFilter[V, T: CanBeQueryCondition](value: Option[V])(f: (E, V) => T): Query[E, U, Seq] =
      value match {
        case Some(v) => query.withFilter(f(_, v))
        case _       => query
      }

    def maybeStringFilter[T: CanBeQueryCondition](value: Option[String])(f: (E, String) => T): Query[E, U, Seq] =
      value match {
        case Some(v) if v.trim.nonEmpty => query.withFilter(f(_, v))
        case _                          => query
      }
  }

}

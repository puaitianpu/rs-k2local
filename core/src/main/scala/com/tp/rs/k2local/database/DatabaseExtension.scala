package com.tp.rs.k2local.database

import akka.actor.typed.{ActorSystem, Extension, ExtensionId}

object DatabaseExtension extends ExtensionId[DatabaseExtension] {

  override def createExtension(system: ActorSystem[_]): DatabaseExtension = new DatabaseExtension(system)
}

class DatabaseExtension(system: ActorSystem[_]) extends Extension {

  val databaseComponent: DatabaseComponent = DatabaseComponent

}

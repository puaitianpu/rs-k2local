package com.tp.rs.k2local

import java.util.UUID

import akka.actor.typed.SpawnProtocol.Command
import akka.actor.typed._
import com.tp.rs.k2local.database.{DatabaseComponent, DatabaseExtension}
import com.typesafe.scalalogging.Logger
import pureconfig.ConfigSource

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  protected val logger: Logger = Logger(getClass)

  val systemId: String                      = ConfigSource.default.at("akka.system.name").loadOrThrow[String]
  val selfUuid: String                      = UUID.randomUUID().toString
  implicit val system: ActorSystem[Command] = ActorSystem(SpawnProtocol(), systemId)
  implicit val dc: DatabaseComponent        = DatabaseExtension(system).databaseComponent

  AdxEventK2localStreamer().flow().run()

  Await.result(system.whenTerminated, Duration.Inf)
}

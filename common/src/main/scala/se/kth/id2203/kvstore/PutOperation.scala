package se.kth.id2203.kvstore

import java.io.Serializable
import java.util.UUID

import se.sics.kompics.KompicsEvent

case class PutOperation(key: String, value: String) extends KompicsEvent with Operation with Serializable {
  val id: UUID = UUID.randomUUID()
}

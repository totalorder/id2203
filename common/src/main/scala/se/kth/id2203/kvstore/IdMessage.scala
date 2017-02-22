package se.kth.id2203.kvstore

import java.util.UUID

import se.sics.kompics.KompicsEvent

trait IdMessage extends KompicsEvent with Serializable {
  val id: UUID
}

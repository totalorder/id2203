package se.kth.id2203.kvstore

import java.io.Serializable
import java.util.UUID

import com.google.common.base.MoreObjects
import se.sics.kompics.KompicsEvent

class PutOperation(val key: String, val value: String) extends KompicsEvent with IdMessage with Serializable {
  val id: UUID = UUID.randomUUID()

  override def toString: String = MoreObjects.toStringHelper(this).add("id", id).add("key", key).add("value", value).toString
}

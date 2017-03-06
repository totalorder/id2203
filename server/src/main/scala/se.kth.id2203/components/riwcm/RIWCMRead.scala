package se.kth.id2203.components.riwcm

import java.util.UUID
import se.sics.kompics.KompicsEvent

case class RIWCMRead(key: String) extends KompicsEvent with Serializable {
  val uuid: UUID = UUID.randomUUID()
}

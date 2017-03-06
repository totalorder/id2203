package se.kth.id2203.components.riwcm

import java.util.UUID

import se.sics.kompics.KompicsEvent

case class RIWCMInternalRead(uuid: UUID, pid: Int, key: String, rid: Int) extends KompicsEvent with Serializable {
}

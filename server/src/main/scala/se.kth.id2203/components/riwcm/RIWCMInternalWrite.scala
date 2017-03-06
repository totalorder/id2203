package se.kth.id2203.components.riwcm

import java.util.UUID

import se.sics.kompics.KompicsEvent

case class RIWCMInternalWrite(uuid: UUID, pid: Int, key: String, rid: Int, ts: Int, wr: Int, value: Option[String]) extends KompicsEvent with Serializable {
}

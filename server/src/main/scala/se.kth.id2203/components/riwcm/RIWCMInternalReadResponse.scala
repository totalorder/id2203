package se.kth.id2203.components.riwcm

import java.util.UUID

import se.sics.kompics.KompicsEvent

//@SerialVersionUID(12349238942342933L)
case class RIWCMInternalReadResponse(uuid: UUID, pid: Int, key: String, rid: Int, ts: Int, wr: Int, value: Option[String]) extends KompicsEvent with Serializable {
}

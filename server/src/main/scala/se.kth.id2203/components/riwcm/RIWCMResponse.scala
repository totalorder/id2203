package se.kth.id2203.components.riwcm

import java.util.UUID

import se.sics.kompics.KompicsEvent

case class RIWCMReadResponse(id: UUID, key: String, value: Option[String]) extends KompicsEvent with Serializable {
}

case class RIWCMWriteResponse(id: UUID, key: String) extends KompicsEvent with Serializable {
}

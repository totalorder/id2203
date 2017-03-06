package se.kth.id2203.components.riwcm

import java.util.UUID

import se.sics.kompics.KompicsEvent

case class RIWCMResponse(id: UUID, key: String, value: Option[String]) extends KompicsEvent with Serializable {
}

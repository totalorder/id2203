package se.kth.id2203.components.riwcm

import java.util.UUID

import se.sics.kompics.KompicsEvent

case class RIWCMResponse(id: UUID, key: String, value: String) extends KompicsEvent with Serializable {
}

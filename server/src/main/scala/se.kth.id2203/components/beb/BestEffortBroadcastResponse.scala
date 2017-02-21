package se.kth.id2203.components.beb

import java.util.UUID

import se.sics.kompics.KompicsEvent

case class BestEffortBroadcastResponse(id: UUID) extends KompicsEvent with Serializable

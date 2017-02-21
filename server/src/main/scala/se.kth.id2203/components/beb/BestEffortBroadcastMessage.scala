package se.kth.id2203.components.beb

import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent

case class BestEffortBroadcastMessage(src: NetAddress, event: KompicsEvent) extends KompicsEvent with Serializable

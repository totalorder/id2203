package se.kth.id2203.components.kv

import se.sics.kompics.KompicsEvent

case class WriteRequest(key: String, value: String) extends KompicsEvent with Serializable

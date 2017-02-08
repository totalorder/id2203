package se.kth.id2203.networking

import se.sics.kompics.KompicsEvent

final case class Message(src: NetAddress, dst: NetAddress, payload: KompicsEvent) extends JavaMessage(src, dst, payload) {
}

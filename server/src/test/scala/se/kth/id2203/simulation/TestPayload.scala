package se.kth.id2203.simulation

import se.sics.kompics.KompicsEvent


case class TestPayload(payload: String) extends KompicsEvent with Serializable{

}

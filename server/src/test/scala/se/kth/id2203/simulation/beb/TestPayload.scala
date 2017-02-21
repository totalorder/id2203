package se.kth.id2203.simulation.beb

import se.sics.kompics.KompicsEvent


case class TestPayload(payload: String) extends KompicsEvent with Serializable{

}

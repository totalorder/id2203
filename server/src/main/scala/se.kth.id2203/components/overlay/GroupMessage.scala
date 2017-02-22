package se.kth.id2203.components.overlay

import se.kth.id2203.overlay.LookupTable
import se.sics.kompics.KompicsEvent

case class GroupMessage(lookupTable: LookupTable) extends KompicsEvent with Serializable
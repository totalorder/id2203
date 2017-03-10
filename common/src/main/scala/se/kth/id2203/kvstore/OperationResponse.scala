package se.kth.id2203.kvstore

import java.io.Serializable

import se.sics.kompics.KompicsEvent


trait OperationResponse extends KompicsEvent with IdMessage with Serializable {
}

package se.kth.id2203.kvstore

import java.io.Serializable

trait Operation extends IdMessage with Serializable {
  val key: String
}

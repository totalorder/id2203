package se.kth.id2203.kvstore

import com.google.common.base.MoreObjects

class PutOperation(key: String, val value: String) extends Operation(key) {
  override def toString: String = MoreObjects.toStringHelper(this).add("id", id).add("key", key).add("value", value).toString
}

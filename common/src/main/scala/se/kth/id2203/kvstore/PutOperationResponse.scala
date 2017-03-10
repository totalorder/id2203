package se.kth.id2203.kvstore

import java.io.Serializable
import java.util.UUID


@SerialVersionUID(3456903489238942L)
case class PutOperationResponse(id: UUID, key: String) extends OperationResponse with Serializable {
  override def toString: String = s"$key was successfully put"
}

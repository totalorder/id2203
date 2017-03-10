package se.kth.id2203.kvstore

import java.io.Serializable
import java.util.UUID

@SerialVersionUID(94568902389348912L)
case class GetOperationResponse(id: UUID, key: String, value: String) extends OperationResponse with IdMessage with Serializable {
  override def toString = s"$key=$value"
}

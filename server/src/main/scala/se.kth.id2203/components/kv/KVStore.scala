package se.kth.id2203.components.kv

import com.larskroll.common.J6
import org.slf4j.LoggerFactory
import se.kth.id2203.bootstrapping.{Booted, Bootstrapping, GetInitialAssignments, InitialAssignments}
import se.kth.id2203.components.beb.{BestEffortBroadcastDeliver, BestEffortBroadcastPort, BestEffortBroadcastRequest}
import se.kth.id2203.components.overlay.{GroupMessage, GroupPort}
import se.kth.id2203.kvstore.OpResponse.Code
import se.kth.id2203.kvstore.{GetOperation, OpResponse, PutOperation}
import se.kth.id2203.networking.{Message, NetAddress}
import se.kth.id2203.overlay.LookupTable
import se.sics.kompics.network.Network
import se.sics.kompics.sl._

import scala.collection.JavaConverters._
import scala.collection.mutable


class KVStore extends ComponentDefinition {
  private val LOG = LoggerFactory.getLogger(classOf[KVStore])
  //******* Ports ******
  private val net = requires[Network]
  private val bestEffortBroadcast = requires[BestEffortBroadcastPort]
  private val group = requires[GroupPort]

  //******* Fields ******
  private val self = config.getValue("id2203.project.address", classOf[NetAddress])
  private val store: mutable.Map[String, String] = new mutable.HashMap()

  private var lut: LookupTable = null

  group uponEvent {
    case message: GroupMessage => handle {
      lut = message.lookupTable
      LOG.info("Got group membership: {}", message)
    }
  }

  net uponEvent {
    case Message(src, dst, operation: GetOperation) => handle {
      LOG.info("Got get operation: {}", operation)
      store.get(operation.key) match {
        case None => trigger(Message(self, src, new OpResponse(operation.id, Code.NOT_FOUND, null)), net)
        case Some(value) => trigger(Message(self, src, new OpResponse(operation.id, Code.OK, value)), net)
      }
    }
  }

  net uponEvent {
    case Message(src, dst, operation: PutOperation) => handle {
      LOG.info("Got put operation: {}", operation)
      trigger(BestEffortBroadcastRequest(WriteRequest(operation.key, operation.value), lut.lookup(operation.key).asScala.toList), bestEffortBroadcast)
      trigger(Message(self, src, new OpResponse(operation.id, Code.OK, operation.value)), net)
    }
  }

  bestEffortBroadcast uponEvent {
    case BestEffortBroadcastDeliver(_, write: WriteRequest) => handle {
      LOG.info("Writing to store: {}", write)
      store.put(write.key, write.value)
    }
  }


}

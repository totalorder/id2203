package se.kth.id2203.overlay

import java.util.UUID

import com.larskroll.common.J6
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import se.kth.id2203.bootstrapping.{Booted, Bootstrapping, GetInitialAssignments, InitialAssignments}
import se.kth.id2203.components.overlay.{GroupMessage, GroupPort}
import se.kth.id2203.components.riwcm._
import se.kth.id2203.kvstore._
import se.kth.id2203.networking.{Message, NetAddress}
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer

import scala.collection.mutable



class VSOverlayManager extends ComponentDefinition {
  private val LOG = LoggerFactory.getLogger(classOf[VSOverlayManager])
  //******* Ports ******
  private val route = provides[Routing]
  private val group = provides[GroupPort]
  private val riwcm = requires[RIWCMPort]
  private val boot = requires[Bootstrapping]
  private val net = requires[Network]
  private val timer = requires[Timer]
  private val requests: mutable.Map[UUID, Tuple2[NetAddress, UUID]] = new mutable.HashMap()
  private val forwards: mutable.Map[UUID, NetAddress] = new mutable.HashMap()

  //******* Fields ******
  final private[overlay] val self = config.getValue("id2203.project.address", classOf[NetAddress])
  private var lut = null: LookupTable

  //******* Handlers ******
  boot uponEvent {
    case event: GetInitialAssignments => handle {
      LOG.info("Generating LookupTable...")
      lut = LookupTable.generate(event.nodes) // TODO: Might be bad to assign here
      LOG.debug("Generated assignments:\n{}", lut)
      trigger(new InitialAssignments(lut), boot)
    }
  }

  boot uponEvent {
    case event: Booted => handle {
      event.assignment match {
        case assignment: LookupTable =>
          LOG.info("Got NodeAssignment, overlay ready.")
          lut = assignment
          val pid = LookupTable.hash(event.id.toString)
          trigger(GroupMessage(pid, assignment.lookup(event.id.toString).asScala.toList, assignment), group)
        case _ =>
          LOG.error("Got invalid NodeAssignment type. Expected: LookupTable; Got: {}", event.assignment.getClass)
      }
    }
  }

  net uponEvent {
    case Message(src, _, payload: RouteMsg) => handle {
      val partition = lut.lookup(payload.key)
      val target: NetAddress = J6.randomElement(partition)
      forwards.put(payload.id, src)
      LOG.info("Forwarding message for key {} to {} (msg: {})", payload.key, target, payload.msg)
      trigger(Message(self, target, payload.msg), net)
    }
  }

  net uponEvent {
    case Message(src, _, response: OperationResponse) => handle {
      val dst = forwards.remove(response.id).get
      LOG.info("Returning message to {} (msg: {})", dst, response: Any)
      trigger(Message(self, dst, response), net)
    }
  }

  route uponEvent {
    case payload: RouteMsg => handle {
        val partition = lut.lookup(payload.key)
        val target = J6.randomElement(partition)
        LOG.info("Routing message for key {} to {}", payload.key, target: Any)
        trigger(Message(self, target, payload.msg), net)
    }
  }

  net uponEvent {
    case Message(src, _, payload: Connect) => handle {
      if (lut != null) {
        LOG.debug("Accepting connection request from {}", src)
        val size = lut.getNodes.size
        trigger(Message(self, src, payload.ack(size)), net)
      }
      else LOG.info("Rejecting connection request from {}, as system is not ready, yet.", src)
    }
  }

  net uponEvent {
    case Message(src, dst, operation: GetOperation) => handle {
      LOG.info("Got get operation: {}", operation)
      val readMesage = RIWCMRead(operation.key)
      requests.put(readMesage.uuid, (src, operation.id))
      trigger(readMesage, riwcm)
    }
  }

  net uponEvent {
    case Message(src, dst, operation: PutOperation) => handle {
      LOG.info("Got put operation: {}", operation)
      val writeMessage = RIWCMWrite(operation.key, operation.value)
      requests.put(writeMessage.uuid, (src, operation.id))
      trigger(writeMessage, riwcm)
    }
  }

  riwcm uponEvent {
    case response: RIWCMReadResponse => handle {
      val (dst, uuid) = requests.remove(response.id).get
      LOG.info(s"Got read response: {}. Responding to: $dst", response)
      val code = if (response.value.isEmpty) OpResponse.Code.NOT_FOUND else OpResponse.Code.OK
      trigger(Message(self, dst, GetOperationResponse(uuid, response.key, response.value.orNull)), net)
    }
  }

  riwcm uponEvent {
    case response: RIWCMWriteResponse => handle {
      LOG.info("Got write response: {}", response)
      val (dst, uuid) = requests.remove(response.id).get
      trigger(Message(self, dst, PutOperationResponse(uuid, response.key)), net)
    }
  }
}

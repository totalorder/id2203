package se.kth.id2203.overlay

import com.larskroll.common.J6
import org.slf4j.LoggerFactory
import se.kth.id2203.bootstrapping.{Booted, Bootstrapping, GetInitialAssignments, InitialAssignments}
import se.kth.id2203.networking.{Message, NetAddress}
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer



class VSOverlayManager extends ComponentDefinition {
  private val LOG = LoggerFactory.getLogger(classOf[VSOverlayManager])
  //******* Ports ******
  private val route = provides[Routing]
  private val boot = requires[Bootstrapping]
  private val net = requires[Network]
  private val timer = requires[Timer]

  //******* Fields ******
  final private[overlay] val self = config.getValue("id2203.project.address", classOf[NetAddress])
  private var lut = null: LookupTable

  //******* Handlers ******
  boot uponEvent {
    case event: GetInitialAssignments => handle {
      LOG.info("Generating LookupTable...")
      val lut = LookupTable.generate(event.nodes)
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
        case _ =>
          LOG.error("Got invalid NodeAssignment type. Expected: LookupTable; Got: {}", event.assignment.getClass)
      }
    }
  }

  net uponEvent {
    case Message(src, _, payload: RouteMsg) => handle {
      val partition = lut.lookup(payload.key)
      val target: NetAddress = J6.randomElement(partition)
      LOG.info("Forwarding message for key {} to {}", payload.key, target: Any)
      trigger(Message(src, target, payload.msg), net)
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
}

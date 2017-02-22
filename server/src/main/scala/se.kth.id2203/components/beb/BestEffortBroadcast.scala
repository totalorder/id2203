package se.kth.id2203.components.beb

import org.slf4j.LoggerFactory
import se.kth.id2203.networking.{Message, NetAddress}
import se.sics.kompics.network.Network
import se.sics.kompics.sl.{ComponentDefinition, NegativePort, PositivePort, handle}

class BestEffortBroadcast extends ComponentDefinition {
  private val LOG = LoggerFactory.getLogger(classOf[BestEffortBroadcast])

  private val bestEffortBroadcast: NegativePort[BestEffortBroadcastPort] = provides[BestEffortBroadcastPort]
  private val net: PositivePort[Network] = requires[Network]
  private val self = config.getValue("id2203.project.address", classOf[NetAddress])

  bestEffortBroadcast uponEvent {
    case request: BestEffortBroadcastRequest => handle {
      request.addresses.foreach(address => trigger(Message(self, address, BestEffortBroadcastMessage(self, request.event)), net))
    }
  }

  net uponEvent {
    case Message(src, _, payload: BestEffortBroadcastMessage) => handle {
      trigger(BestEffortBroadcastDeliver(payload.src, payload.event), bestEffortBroadcast)
    }
  }
}

package se.kth.id2203.simulation.beb

import java.net.InetAddress
import java.util.UUID

import org.junit.Assert
import org.slf4j.LoggerFactory
import se.kth.id2203.components.beb.{BestEffortBroadcastPort, _}
import se.kth.id2203.networking.{Message, NetAddress}
import se.kth.id2203.simulation.{SimulationClient, SimulationResultMap, SimulationResultSingleton, TestPayload}
import se.sics.kompics._
import se.sics.kompics.network.{Address, Network}
import se.sics.kompics.simulator.adaptor.Operation1
import se.sics.kompics.simulator.core.SimulatorPort
import se.sics.kompics.simulator.events.system.StartNodeEvent
import se.sics.kompics.simulator.util.GlobalView
import se.sics.kompics.sl.{ComponentDefinition, NegativePort, PositivePort, handle}
import se.sics.kompics.timer.Timer

class BestEffortBroadcastClient(init: BestEffortBroadcastClient.Init) extends ComponentDefinition {
  private val LOG = LoggerFactory.getLogger(classOf[BestEffortBroadcastClient])
  //******* Ports ******
//  private val bestEffortBroadcast: PositivePort[BestEffortBroadcastPort] = requires[BestEffortBroadcastPort]
  private val net: PositivePort[Network] = requires[Network]
  private val timer = requires[Timer]
  private val simulator = requires[SimulatorPort]

  //******* Fields ******
  private val self = config.getValue("id2203.project.address", classOf[NetAddress])
  private val server = config.getValue("id2203.project.bootstrap-address", classOf[NetAddress])
  private val res = SimulationResultSingleton.getInstance
  private val pending = new java.util.TreeMap[UUID, String]
  private val uuid = init.uuid
  private val key = init.broadcast
  private val value = init.receive
  private val gv = config().getValue("simulation.globalview", classOf[GlobalView])
  private val bestEffortBroadcastComponent: Component = create(classOf[BestEffortBroadcast], Init.NONE)
  private val bestEffortBroadcast: PositivePort[BestEffortBroadcastPort] = bestEffortBroadcastComponent.getPositive(classOf[BestEffortBroadcastPort]).asInstanceOf[PositivePort[BestEffortBroadcastPort]]

  connect[Network](net -> bestEffortBroadcastComponent)

  if (gv.getValue("besteffortbroadcast.addresses", classOf[List[NetAddress]]) == null) {
    gv.setValue("besteffortbroadcast.addresses", List[NetAddress]())
  }
  // TODO: Race condition?
  gv.setValue("besteffortbroadcast.addresses", gv.getValue("besteffortbroadcast.addresses", classOf[List[NetAddress]]) :+ self)

  control.asInstanceOf[NegativePort[ControlPort]] uponEvent {
    case event: Start => handle {

      val addresses = gv.getValue("besteffortbroadcast.addresses", classOf[List[NetAddress]])

      if (init.broadcast != null) {
        trigger(BestEffortBroadcastRequest(TestPayload(init.broadcast), addresses), bestEffortBroadcast)
      }
    }
  }

  bestEffortBroadcast uponEvent {
    case response: BestEffortBroadcastDeliver => handle {
      response match {
        case BestEffortBroadcastDeliver(_, payload: TestPayload) => {
          LOG.info("Broadcast delivered: " + payload.payload)
          res.put(uuid.toString, payload.payload)
        }
        case _ => LOG.error("Invalid message received")
      }
    }
  }
}

case class BestEffortBroadcastClientConf(broadcast: String, receive: String)

object BestEffortBroadcastClient extends SimulationClient[BestEffortBroadcastClientConf] {
  case class Init(uuid: UUID, broadcast: String, receive: String) extends se.sics.kompics.Init[BestEffortBroadcastClient]

  override def start(uuid: UUID) = (conf: BestEffortBroadcastClientConf) => new Operation1[StartNodeEvent, Integer]() {
    def generate(self: Integer): StartNodeEvent = new StartEvent(self) {
      def getComponentDefinition: Class[_ <: ComponentDefinition] = classOf[BestEffortBroadcastClient]
      def getComponentInit: Init = Init(uuid, conf.broadcast, conf.receive)
    }
  }

  override def assert(uuid: UUID, assertResult: Object, idx: Int, res: SimulationResultMap, conf: BestEffortBroadcastClientConf) = Assert.assertEquals(idx + ": <[" + conf.broadcast + "]>" + uuid, assertResult, res.get(uuid.toString, classOf[String])): Unit
}


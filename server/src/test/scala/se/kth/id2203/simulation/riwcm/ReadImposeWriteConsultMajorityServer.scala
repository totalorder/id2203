package se.kth.id2203.simulation.riwcm

import java.net.InetAddress
import java.util.UUID

import org.junit.Assert
import org.slf4j.LoggerFactory
import se.kth.id2203.components.beb.{BestEffortBroadcastPort, _}
import se.kth.id2203.components.overlay.{GroupMessage, GroupPort}
import se.kth.id2203.components.riwcm.{RIWCMPort, RIWCMRead, RIWCMResponse, ReadImposeWriteConsultMajority}
import se.kth.id2203.networking.{Message, NetAddress}
import se.kth.id2203.overlay.LookupTable
import se.kth.id2203.simulation.{SimulationClient, SimulationResultMap, SimulationResultSingleton, TestPayload}
import se.sics.kompics._
import se.sics.kompics.network.{Address, Network}
import se.sics.kompics.simulator.adaptor.Operation1
import se.sics.kompics.simulator.core.SimulatorPort
import se.sics.kompics.simulator.events.system.StartNodeEvent
import se.sics.kompics.simulator.util.GlobalView
import se.sics.kompics.sl.{ComponentDefinition, NegativePort, PositivePort, handle}
import se.sics.kompics.timer.Timer

import scala.collection.mutable

case class Addresses(addresses: List[NetAddress]) extends KompicsEvent with Serializable

case class RIWCMServerRead(uuid: UUID, key: String) extends KompicsEvent with Serializable

class ReadImposeWriteConsultMajorityServer(init: ReadImposeWriteConsultMajorityServer.Init) extends ComponentDefinition {
  private val LOG = LoggerFactory.getLogger(classOf[ReadImposeWriteConsultMajorityServer])
  //******* Ports ******
  private val net: PositivePort[Network] = requires[Network]
  private val timer = requires[Timer]
  private val simulator = requires[SimulatorPort]

  //******* Fields ******
  private val self = config.getValue("id2203.project.address", classOf[NetAddress])
  private val server = config.getValue("id2203.project.bootstrap-address", classOf[NetAddress])
  private val res = SimulationResultSingleton.getInstance
  private val pending = new java.util.TreeMap[UUID, String]
  private val uuid = init.uuid
  private val gv = config().getValue("simulation.globalview", classOf[GlobalView])
  private val riwcmComponent: Component = create(classOf[ReadImposeWriteConsultMajority], Init.NONE)
  private val riwcm: PositivePort[RIWCMPort] = riwcmComponent.getPositive(classOf[RIWCMPort]).asInstanceOf[PositivePort[RIWCMPort]]
  private val bestEffortBroadcastComponent: Component = create(classOf[BestEffortBroadcast], Init.NONE)
  private val bestEffortBroadcast: PositivePort[BestEffortBroadcastPort] = bestEffortBroadcastComponent.getPositive(classOf[BestEffortBroadcastPort]).asInstanceOf[PositivePort[BestEffortBroadcastPort]]
  private val groupPort: NegativePort[GroupPort] = riwcmComponent.getNegative(classOf[GroupPort]).asInstanceOf[NegativePort[GroupPort]]
  private val requests: mutable.Map[UUID, NetAddress] = new mutable.HashMap()

  connect[BestEffortBroadcastPort](bestEffortBroadcastComponent -> riwcmComponent)
  connect[Network](net -> bestEffortBroadcastComponent)
  connect[Network](net -> riwcmComponent)

  if (gv.getValue("readimposeconsultmajority.addresses", classOf[List[NetAddress]]) == null) {
    gv.setValue("readimposeconsultmajority.addresses", List[NetAddress]())
  }
  // TODO: Race condition?
  gv.setValue("readimposeconsultmajority.addresses", gv.getValue("readimposeconsultmajority.addresses", classOf[List[NetAddress]]) :+ self)


  control.asInstanceOf[NegativePort[ControlPort]] uponEvent {
    case event: Start => handle {
      val addresses = gv.getValue("readimposeconsultmajority.addresses", classOf[List[NetAddress]])
      if (addresses.length == init.nodes) {
        trigger(BestEffortBroadcastRequest(Addresses(addresses), addresses), bestEffortBroadcast)
      }

      LOG.info(s"$self: Server started")
      res.put(uuid.toString, "OK")
    }
  }

  bestEffortBroadcast uponEvent {
    case BestEffortBroadcastDeliver(_, addresses: Addresses) => handle {
      trigger(GroupMessage(LookupTable.hash(uuid.toString), addresses.addresses, null), groupPort)
    }
  }

  net uponEvent {
    case Message(src, _, read: RIWCMServerRead) => handle {
      val readMesage = RIWCMRead(read.key)
      requests.put(readMesage.uuid, src)
      trigger(readMesage, riwcm)
    }
  }

  riwcm uponEvent {
    case response: RIWCMResponse => handle {
      trigger(Message(self, requests(response.id), response), net)
    }
  }
}

case class ReadImposeWriteConsultMajorityServerConf(nodes: Int)

object ReadImposeWriteConsultMajorityServer extends SimulationClient[ReadImposeWriteConsultMajorityServerConf] {
  case class Init(uuid: UUID, nodes: Int) extends se.sics.kompics.Init[ReadImposeWriteConsultMajorityServer]

  override def start(uuid: UUID) = (conf: ReadImposeWriteConsultMajorityServerConf) => new Operation1[StartNodeEvent, Integer]() {
    def generate(self: Integer): StartNodeEvent = new StartEvent(self) {
      def getComponentDefinition: Class[_ <: ComponentDefinition] = classOf[ReadImposeWriteConsultMajorityServer]
      def getComponentInit: Init = Init(uuid, conf.nodes)
    }
  }

  override def assert(uuid: UUID, assertResult: Object, idx: Int, res: SimulationResultMap, conf: ReadImposeWriteConsultMajorityServerConf) = Assert.assertEquals(idx + ": " + uuid, assertResult, res.get(uuid.toString, classOf[String])): Unit
}


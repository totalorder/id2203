package se.kth.id2203.simulation.riwcm

import java.net.InetAddress
import java.util.UUID

import scala.collection.JavaConverters._
import com.larskroll.common.J6
import org.junit.Assert
import org.slf4j.LoggerFactory
import se.kth.id2203.components.beb.{BestEffortBroadcastPort, _}
import se.kth.id2203.components.riwcm.RIWCMResponse
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

class ReadImposeWriteConsultMajorityClient(init: ReadImposeWriteConsultMajorityClient.Init) extends ComponentDefinition {
  private val LOG = LoggerFactory.getLogger(classOf[ReadImposeWriteConsultMajorityClient])
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
//  private val key = init.broadcast
//  private val value = init.receive
  private val gv = config().getValue("simulation.globalview", classOf[GlobalView])
//  private val bestEffortBroadcastComponent: Component = create(classOf[BestEffortBroadcast], Init.NONE)
//  private val bestEffortBroadcast: PositivePort[BestEffortBroadcastPort] = bestEffortBroadcastComponent.getPositive(classOf[BestEffortBroadcastPort]).asInstanceOf[PositivePort[BestEffortBroadcastPort]]

//  connect[Network](net -> bestEffortBroadcastComponent)

  control.asInstanceOf[NegativePort[ControlPort]] uponEvent {
    case event: Start => handle {
      val addresses = gv.getValue("readimposeconsultmajority.addresses", classOf[List[NetAddress]])
      val address = J6.randomElement(addresses.asJava)

      trigger(Message(self, address, RIWCMServerRead(UUID.randomUUID(), init.key)), net)
    }
  }


  net uponEvent {
    case Message(src, dst, response: RIWCMResponse) => handle {
      LOG.info(s"Response: $response")
      res.put(uuid.toString, response.value.getOrElse("NOT_FOUND"))
    }
  }
}

case class ReadImposeWriteConsultMajorityClientConf(key: String, value: Option[String])

object ReadImposeWriteConsultMajorityClient extends SimulationClient[ReadImposeWriteConsultMajorityClientConf] {
  case class Init(uuid: UUID, key: String, value: Option[String]) extends se.sics.kompics.Init[ReadImposeWriteConsultMajorityClient]

  override def start(uuid: UUID) = (conf: ReadImposeWriteConsultMajorityClientConf) => new Operation1[StartNodeEvent, Integer]() {
    def generate(self: Integer): StartNodeEvent = new StartEvent(self) {
      def getComponentDefinition: Class[_ <: ComponentDefinition] = classOf[ReadImposeWriteConsultMajorityClient]
      def getComponentInit: Init = Init(uuid, conf.key, conf.value)
    }
  }

  override def assert(uuid: UUID, assertResult: Object, idx: Int, res: SimulationResultMap, conf: ReadImposeWriteConsultMajorityClientConf) = Assert.assertEquals(idx + ": <[" + conf.key + "]>" + uuid, assertResult, res.get(uuid.toString, classOf[String])): Unit
}


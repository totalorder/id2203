package se.kth.id2203.simulation.riwcm

import java.util.UUID

import scala.collection.JavaConverters._
import com.larskroll.common.J6
import org.junit.Assert
import org.slf4j.LoggerFactory
import se.kth.id2203.components.riwcm.{RIWCMReadResponse, RIWCMWriteResponse}
import se.kth.id2203.networking.{Message, NetAddress}
import se.kth.id2203.simulation.{SimulationClient, SimulationResultMap, SimulationResultSingleton}
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

  control.asInstanceOf[NegativePort[ControlPort]] uponEvent {
    case event: Start => handle {
      val addresses = gv.getValue("readimposeconsultmajority.addresses", classOf[List[NetAddress]])
      val address = J6.randomElement(addresses.asJava)

      if (init.value.isDefined) {
        trigger(Message(self, address, RIWCMServerWrite(UUID.randomUUID(), init.key, init.value.get)), net)
      } else {
        trigger(Message(self, address, RIWCMServerRead(UUID.randomUUID(), init.key)), net)
      }
    }
  }


  net uponEvent {
    case Message(src, dst, response: RIWCMReadResponse) => handle {
      LOG.info(s"Response: $response")
      res.put(uuid.toString, response.value.getOrElse("NOT_FOUND"))
    }
  }

  net uponEvent {
    case Message(src, dst, response: RIWCMWriteResponse) => handle {
      LOG.info(s"Response: $response")
      res.put(uuid.toString, "WRITTEN")
    }
  }
}

case class ReadImposeWriteConsultMajorityClientConf(key: String, value: String)
case class Sticky(id: String, assertObject: Object)

object ReadImposeWriteConsultMajorityClient extends SimulationClient[ReadImposeWriteConsultMajorityClientConf] {
  case class Init(uuid: UUID, key: String, value: Option[String]) extends se.sics.kompics.Init[ReadImposeWriteConsultMajorityClient]

  override def start(uuid: UUID) = (conf: ReadImposeWriteConsultMajorityClientConf) => new Operation1[StartNodeEvent, Integer]() {
    def generate(self: Integer): StartNodeEvent = new StartEvent(self) {
      def getComponentDefinition: Class[_ <: ComponentDefinition] = classOf[ReadImposeWriteConsultMajorityClient]
      def getComponentInit: Init = Init(uuid, conf.key, Option(conf.value))
    }
  }

  override def assert(uuid: UUID, assertResult: Object, idx: Int, res: SimulationResultMap, conf: ReadImposeWriteConsultMajorityClientConf): Unit = {
    val message = idx + ": <[" + conf.key + "]>" + uuid
    val actual = res.get(uuid.toString, classOf[String])

    assertResult match {
      case expected: String => Assert.assertEquals(message, expected, actual)
      case expected: Set[Object] => Assert.assertTrue(expected.contains(actual))
      case expected: Sticky => {
        val current = res.get(expected.id, classOf[String])
        println(s"$current == $actual")
        if (current != null) {
          Assert.assertEquals(current, actual)
        } else {
          res.put(expected.id, actual)
        }

        assert(uuid, expected.assertObject, idx, res, conf)
      }
    }
  }
}


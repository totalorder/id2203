package se.kth.id2203.simulation.server
import java.util.UUID

import org.junit.Assert
import org.slf4j.LoggerFactory
import se.kth.id2203.kvstore._
import se.kth.id2203.networking.{Message, NetAddress}
import se.kth.id2203.overlay.RouteMsg
import se.kth.id2203.simulation.{SimulationClient, SimulationResultMap, SimulationResultSingleton}
import se.sics.kompics._
import se.sics.kompics.network.Network
import se.sics.kompics.simulator.adaptor.Operation1
import se.sics.kompics.simulator.core.SimulatorPort
import se.sics.kompics.simulator.events.system.StartNodeEvent
import se.sics.kompics.sl.{ComponentDefinition, PositivePort, handle}
import se.sics.kompics.timer.Timer

class ServerClient(init: ServerClient.Init) extends ComponentDefinition {
  private val LOG = LoggerFactory.getLogger(classOf[ServerClient])
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
  private val key = init.key
  private val value_ = Option(init.value)

  ctrl uponEvent {
    case _: Start => handle {
      val op: IdMessage = value_ match {
        case Some(value) => new PutOperation(key, value)
        case None => new GetOperation(key)
      }
      trigger(Message(self, server, new RouteMsg(op.id, key, op)), net)
      pending.put(op.id, key)
      LOG.info("Sending {}", op)
      res.put(uuid.toString, "SENT")
    }
  }

  net uponEvent {
    case Message(src, dst, opResponse: OperationResponse) => handle {
      LOG.debug("Got OpResponse: {}", opResponse)
      val key = pending.remove(opResponse.id)
      key match {
        case null => LOG.warn("ID {} was not pending! Ignoring response.", opResponse.id)
        case _ => {
          opResponse match {
            case putResponse: PutOperationResponse => res.put(uuid.toString, "OK")
            case getResponse: GetOperationResponse => res.put(uuid.toString, Option(getResponse.value).getOrElse("NOT_FOUND"))
          }
        }
      }
    }
  }
}

case class ServerClientConf(key: String, value: String)
case class ServerClientInit(uuid: UUID, broadcast: String, receive: String) extends se.sics.kompics.Init[ServerClient]

object ServerClient extends SimulationClient[ServerClientConf] {
  case class Init(uuid: UUID, key: String, value: String) extends se.sics.kompics.Init[ServerClient]

  override def start(uuid: UUID) = (conf: ServerClientConf) => new Operation1[StartNodeEvent, Integer]() {
    def generate(self: Integer): StartNodeEvent = new StartEvent(self) {
      def getComponentDefinition: Class[_ <: ComponentDefinition] = classOf[ServerClient]
      def getComponentInit: Init = Init(uuid, conf.key, conf.value)
    }
  }

  override def assert(uuid: UUID, assertResult: Object, idx: Int, res: SimulationResultMap, conf: ServerClientConf) = Assert.assertEquals(idx + ": <[" + conf.key + "]>" + uuid, assertResult, res.get(uuid.toString, classOf[String])): Unit
}


package se.kth.id2203.simulation.server

import java.net.InetAddress
import java.util.UUID

import org.junit.Assert
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.{ParentComponent, ParentComponentInit}
import se.kth.id2203.simulation.{SimulationClient, SimulationResultMap}
import se.sics.kompics.simulator.adaptor.Operation1
import se.sics.kompics.simulator.events.system.StartNodeEvent
import se.sics.kompics.sl.ComponentDefinition

case class ServerConf()

object Server
  extends SimulationClient[ServerConf] {
  override def start(uuid: UUID) = (conf: ServerConf) => new Operation1[StartNodeEvent, Integer]() {
    def generate(self: Integer): StartNodeEvent = new StartEvent(self) {
      def getComponentDefinition: Class[_ <: ComponentDefinition] = classOf[ParentComponent]
      def getComponentInit: ParentComponentInit = ParentComponentInit(uuid)
    }
  }

  override def assert(uuid: UUID, assertResult: Object, idx: Int, res: SimulationResultMap, conf: ServerConf) = Assert.assertEquals(idx + ": " + uuid, assertResult, res.get(uuid.toString, classOf[String])): Unit
}

object ServerBootstrap extends SimulationClient[ServerConf] {
  override def start(uuid: UUID) = (conf: ServerConf) => new Operation1[StartNodeEvent, Integer]() {
    def generate(self: Integer): StartNodeEvent = new StartEvent(self) {
      override val selfAdr: NetAddress = bsAdr

      def getComponentDefinition: Class[_ <: ComponentDefinition] = classOf[ParentComponent]
      def getComponentInit: ParentComponentInit = ParentComponentInit(uuid)

      override def initConfigUpdate: java.util.Map[String, AnyRef] = {
        val config = super.initConfigUpdate
        config.remove("id2203.project.bootstrap-address")
        config
      }
    }
  }

  override def assert(uuid: UUID, assertResult: Object, idx: Int, res: SimulationResultMap, conf: ServerConf) = Assert.assertEquals(idx + ": ]>" + uuid, assertResult, res.get(uuid.toString, classOf[String])): Unit
}

package se.kth.id2203.simulation

import java.net.InetAddress
import java.util.UUID

import se.kth.id2203.networking.NetAddress
import se.sics.kompics.network.Address
import se.sics.kompics.simulator.adaptor.Operation1
import se.sics.kompics.simulator.events.system.StartNodeEvent

abstract class SimulationClient[T] {
  def start(uuid: UUID): (T) => Operation1[StartNodeEvent, Integer]
  def assert(uuid: UUID, assertResult: Object, idx: Int, res: SimulationResultMap, conf: T): Unit

  abstract class StartEvent(self: Integer) extends StartNodeEvent {
    val selfAdr = new NetAddress(InetAddress.getByName("192.168.1." + self), 45678)
    val bsAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45678)

    def getNodeAddress: Address = selfAdr
    override def toString: String = this.getClass.getSimpleName + "<" + selfAdr.toString + ">"

    override def initConfigUpdate: java.util.Map[String, AnyRef] = {
      val config = new java.util.HashMap[String, AnyRef]
      config.put("id2203.project.address", selfAdr)
      config.put("id2203.project.bootstrap-address", bsAdr)
      config
    }
  }
}

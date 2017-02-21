package se.kth.id2203.simulation.beb
import java.util.UUID

import org.junit.Assert
import se.kth.id2203.simulation.SimulationResultMap
import se.sics.kompics.simulator.adaptor.Operation1
import se.sics.kompics.simulator.events.system.StartNodeEvent

abstract class SimulationClient[T] {
//  type T
  def start(uuid: UUID): (T) => Operation1[StartNodeEvent, Integer]

  def assert(uuid: UUID, assertResult: Object, idx: Int, res: SimulationResultMap, conf: T): Unit
}

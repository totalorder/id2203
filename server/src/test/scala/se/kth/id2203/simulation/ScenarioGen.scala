package se.kth.id2203.simulation

import java.util.UUID

import se.sics.kompics.KompicsEvent
import se.sics.kompics.simulator.SimulationScenario
import se.sics.kompics.simulator.adaptor.Operation1
import se.sics.kompics.simulator.adaptor.distributions.ConstantDistribution
import se.sics.kompics.simulator.events.system.StartNodeEvent
import se.sics.kompics.simulator.run.LauncherComp



object ScenarioGen {
  class Event[T](val operation: Operation1[_ <: KompicsEvent, _ <: Number], val uuid: UUID, val conf: T, val assertResult: Object, val asserter: (UUID, Object, Int, SimulationResultMap, T) => Unit) extends Serializable {
    def assert(idx: Int, res: SimulationResultMap): Unit = asserter.apply(uuid, assertResult, idx, res, conf)
  }

  class Scenario(val simulationScenario: SimulationScenario, val events: List[Event[_]]) extends Serializable {
    def assertResults(res: SimulationResultMap): Unit = {
      var idx = 0
      for (event <- events) {
        event.assert(idx, res)
        idx += 1
      }
    }

    def simulate(launcherCompClass: Class[LauncherComp]) {
      simulationScenario.simulate(launcherCompClass)
    }
  }

  class ScenarioBuilder private(val events: List[Event[_]], val servers: Int) extends Serializable {
    def withOp[T](simulationClient: SimulationClient[T],
                  conf: T,
                  assertResult: Object
                 ): ScenarioBuilder = {
      val uuid = UUID.randomUUID
      val operation: Operation1[StartNodeEvent, Integer] = simulationClient.start(uuid).apply(conf)
      new ScenarioBuilder(events :+ new Event[T](operation, uuid, conf, assertResult, simulationClient.assert), servers)
    }

    def this(servers: Int) {
      this(List[Event[_]](), servers)
    }

    def build = new Scenario(
      new SimulationScenario() {
        var lastProcess: SimulationScenario#StochasticProcess = null

        var idx: Int = 1
        for (event <- events) {
          val process: SimulationScenario#StochasticProcess = new StochasticProcess {
            {
              eventInterArrivalTime(constant(1000))
              raise(1, event.operation.asInstanceOf[Operation1[_ <: KompicsEvent, Integer]], new ConstantDistribution(classOf[Integer], idx.asInstanceOf[Integer]))
            }
          }
          if (lastProcess == null) process.start()
          else process.startAfterTerminationOf(10000, lastProcess)
          lastProcess = process
          idx += 1
        }
        terminateAfterTerminationOf(100000, lastProcess)
      }, events)
  }
}
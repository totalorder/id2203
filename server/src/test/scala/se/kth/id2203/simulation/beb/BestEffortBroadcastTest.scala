package se.kth.id2203.simulation.beb

import org.junit.{Before, Test}
import se.kth.id2203.simulation.{ScenarioGen, SimulationResultMap, SimulationResultSingleton}
import se.sics.kompics.Kompics
import se.sics.kompics.simulator.run.LauncherComp


class BestEffortBroadcastTest {
  var bootThreshold: Int = -1
  var scenarioBuilder = new ScenarioGen.ScenarioBuilder(bootThreshold)
  var res: SimulationResultMap = _

  @Before
  def setUp(): Unit = {
    bootThreshold = Kompics.getConfig.getValue("id2203.project.bootThreshold", classOf[Integer])
    res = SimulationResultSingleton.getInstance
  }

  @Test
  def broadcast2(): Unit = {
    val scenario = scenarioBuilder
      .withOp(BestEffortBroadcastClient, BestEffortBroadcastClientConf(null, "hola!"), "hola!")
      .withOp(BestEffortBroadcastClient, BestEffortBroadcastClientConf("hola!", "hola!"), "hola!")
      .build

    scenario.simulate(classOf[LauncherComp])
    scenario.assertResults(res)
  }

  @Test
  def broadcast3(): Unit = {
    val scenario = scenarioBuilder
      .withOp(BestEffortBroadcastClient, BestEffortBroadcastClientConf(null, "hola!"), "hola!")
      .withOp(BestEffortBroadcastClient, BestEffortBroadcastClientConf(null, "hola!"), "hola!")
      .withOp(BestEffortBroadcastClient, BestEffortBroadcastClientConf("hola!", "hola!"), "hola!")
      .build

    scenario.simulate(classOf[LauncherComp])
    scenario.assertResults(res)
  }
}

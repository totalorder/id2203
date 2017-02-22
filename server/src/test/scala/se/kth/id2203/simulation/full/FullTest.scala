package se.kth.id2203.simulation.full

import org.junit.{Before, Test}
import se.kth.id2203.simulation.{ScenarioGen, SimulationResultMap, SimulationResultSingleton}
import se.sics.kompics.Kompics
import se.sics.kompics.simulator.run.LauncherComp


class FullTest {
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
      .withOp(FullClient, FullClientConf(null, "hola!"), "hola!")
      .withOp(FullClient, FullClientConf("hola!", "hola!"), "hola!")
      .build

    scenario.simulate(classOf[LauncherComp])
    scenario.assertResults(res)
  }

  @Test
  def broadcast3(): Unit = {
    val scenario = scenarioBuilder
      .withOp(FullClient, FullClientConf(null, "hola!"), "hola!")
      .withOp(FullClient, FullClientConf(null, "hola!"), "hola!")
      .withOp(FullClient, FullClientConf("hola!", "hola!"), "hola!")
      .build

    scenario.simulate(classOf[LauncherComp])
    scenario.assertResults(res)
  }
}

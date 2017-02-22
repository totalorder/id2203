package se.kth.id2203.simulation.server

import org.junit.{Before, Test}
import se.kth.id2203.simulation.{ScenarioGen, SimulationResultMap, SimulationResultSingleton}
import se.sics.kompics.Kompics
import se.sics.kompics.simulator.run.LauncherComp


class ServerTest {
  var bootThreshold: Int = -1
  var scenarioBuilder = new ScenarioGen.ScenarioBuilder(bootThreshold)
  var res: SimulationResultMap = _

  @Before
  def setUp(): Unit = {
    bootThreshold = Kompics.getConfig.getValue("id2203.project.bootThreshold", classOf[Integer])
    res = SimulationResultSingleton.getInstance
  }

  @Test
  def getEmptyKey(): Unit = {
    val scenario = scenarioBuilder
      .withOp(ServerBootstrap, ServerConf(), null)
      .withOp(Server, ServerConf(), null)
      .withOp(Server, ServerConf(), null)
      .withOp(ServerClient, ServerClientConf("asd", null), "NOT_FOUND", 50000)
      .build

    scenario.simulate(classOf[LauncherComp])
    scenario.assertResults(res)
  }

  @Test
  def getPutKey(): Unit = {
    val scenario = scenarioBuilder
      .withOp(ServerBootstrap, ServerConf(), null)
      .withOp(Server, ServerConf(), null)
      .withOp(Server, ServerConf(), null)
      .withOp(ServerClient, ServerClientConf("asd", "hello!"), "hello!", 50000)
      .withOp(ServerClient, ServerClientConf("asd", null), "hello!")
      .withOp(ServerClient, ServerClientConf("asd", null), "hello!")
      .withOp(ServerClient, ServerClientConf("asd", null), "hello!")
      .withOp(ServerClient, ServerClientConf("asd", null), "hello!")
      .build

    scenario.simulate(classOf[LauncherComp])
    scenario.assertResults(res)
  }
}

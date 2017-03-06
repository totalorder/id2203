package se.kth.id2203.simulation.riwcm

import org.junit.{Before, Test}
import se.kth.id2203.simulation.{ScenarioGen, SimulationResultMap, SimulationResultSingleton}
import se.sics.kompics.Kompics
import se.sics.kompics.simulator.run.LauncherComp


class RIWCMTest {
  var bootThreshold: Int = -1
  var scenarioBuilder = new ScenarioGen.ScenarioBuilder(bootThreshold)
  var res: SimulationResultMap = _

  @Before
  def setUp(): Unit = {
    bootThreshold = Kompics.getConfig.getValue("id2203.project.bootThreshold", classOf[Integer])
    res = SimulationResultSingleton.getInstance
  }

  @Test
  def getKey(): Unit = {
    val scenario = scenarioBuilder
      .withOp(ReadImposeWriteConsultMajorityServer, ReadImposeWriteConsultMajorityServerConf(3), "OK")
      .withOp(ReadImposeWriteConsultMajorityServer, ReadImposeWriteConsultMajorityServerConf(3), "OK")
      .withOp(ReadImposeWriteConsultMajorityServer, ReadImposeWriteConsultMajorityServerConf(3), "OK")
      .withOp(ReadImposeWriteConsultMajorityClient, ReadImposeWriteConsultMajorityClientConf("asd", null), "NOT_FOUND")
      .build

    scenario.simulate(classOf[LauncherComp])
    scenario.assertResults(res)
  }
}

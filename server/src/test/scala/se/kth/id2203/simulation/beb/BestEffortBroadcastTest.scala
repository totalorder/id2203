package se.kth.id2203.simulation.beb

import org.junit.Test
import se.kth.id2203.simulation.SimulationResultSingleton
import se.sics.kompics.Kompics
import se.sics.kompics.simulator.run.LauncherComp
import se.sics.kompics.simulator.util.GlobalView


class BestEffortBroadcastTest {
  @Test
  def testStartClient: Unit = {
    val bootThreshold = Kompics.getConfig.getValue("id2203.project.bootThreshold", classOf[Integer])

    val scenario = new ScenarioGen.ScenarioBuilder(bootThreshold)
      .withOp(BestEffortBroadcastClient, BestEffortBroadcastClientConf(null, "hola!"), "hola!")
      .withOp(BestEffortBroadcastClient, BestEffortBroadcastClientConf("hola!", "hola!"), "hola!")
      .build

    val res = SimulationResultSingleton.getInstance
    scenario.simulate(classOf[LauncherComp])
    scenario.assertResults(res)
  }
}

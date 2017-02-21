/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.simulation;

import org.junit.Test;
import se.sics.kompics.Kompics;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

import java.io.Serializable;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class OpsTest implements Serializable {

    @Test
    public void getKeyTest() {
        long seed = 123;
        Integer bootThreshold = Kompics.getConfig().getValue("id2203.project.bootThreshold", Integer.class);
        SimulationScenario.setSeed(seed);
        ScenarioGen.ScenarioBuilder scenarioBuilder = new ScenarioGen.ScenarioBuilder(bootThreshold);
        ScenarioGen.Scenario scenario = scenarioBuilder
                .withGetKey("asd", "NOT_FOUND")
                .withPutKey("asd", "hello!", "hello!")
                .withGetKey("asd", "hello!")
                .build();
        SimulationResultMap res = SimulationResultSingleton.getInstance();
        scenario.simulate(LauncherComp.class);
        scenario.assertResults(res);
    }
}

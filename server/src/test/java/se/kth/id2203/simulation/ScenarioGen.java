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

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import se.kth.id2203.ParentComponent;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.run.LauncherComp;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public abstract class ScenarioGen {
    static class Scenario implements Serializable {
        private final SimulationScenario simulationScenario;
        private final ImmutableList<ScenarioBuilder.Event> events;

        public Scenario(final SimulationScenario simulationScenario, final ImmutableList<ScenarioBuilder.Event> events) {
            this.simulationScenario = simulationScenario;
            this.events = events;
        }

        public void assertResults(final SimulationResultMap res) {
            int idx = 0;
            for (final ScenarioBuilder.Event event : events) {
                Assert.assertEquals(idx + ": <[" + event.key + "]>" + event.uuid, event.assertResult, res.get(event.uuid.toString(), String.class));
                idx++;
            }
        }

        public void simulate(final Class<LauncherComp> launcherCompClass) {
            simulationScenario.simulate(launcherCompClass);
        }
    }

    static class ScenarioBuilder implements Serializable {
        class Event implements Serializable {
            public final Operation1 operation;
            public final String key;
            public final UUID uuid;
            public final Object assertResult;

            public Event(final Operation1 operation, final String key, final UUID uuid, final Object assertResult) {
                this.operation = operation;
                this.key = key;
                this.uuid = uuid;
                this.assertResult = assertResult;
            }
        }

        final ImmutableList<Event> events;
        private final int servers;

        public ScenarioBuilder(final int servers) {
            this(new ImmutableList.Builder<Event>().build(), servers);
        }

        private ScenarioBuilder(final ImmutableList<Event> events, final int servers) {
            this.events = events;
            this.servers = servers;
        }

        private ScenarioBuilder withClientOperation(final String key, final String value, final Object assertResult) {
            final UUID uuid = UUID.randomUUID();
            return new ScenarioBuilder(
                    new ImmutableList.Builder<Event>()
                            .addAll(events)
                            .add(new Event(startClientOp(uuid, key, value), key, uuid, assertResult))
                            .build(),
                    servers);
        }

        public ScenarioBuilder withGetKey(final String key, final Object assertResult) {
            return withClientOperation(key, null, assertResult);
        }

        public ScenarioBuilder withPutKey(final String key, final String value, final Object assertResult) {
            return withClientOperation(key, value, assertResult);
        }

        public Scenario build() {
            return new Scenario(
                new SimulationScenario() {
                    {
                        SimulationScenario.StochasticProcess startCluster = new SimulationScenario.StochasticProcess() {
                            {
                                eventInterArrivalTime(constant(1000));
                                raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                            }
                        };

                        startCluster.start();

                        SimulationScenario.StochasticProcess lastProcess = null;
                        for (final Event event: events) {
                            SimulationScenario.StochasticProcess process = new SimulationScenario.StochasticProcess() {
                                {
                                    eventInterArrivalTime(constant(1000));
                                    raise(1, event.operation, new BasicIntSequentialDistribution(1));
                                }
                            };

                            if (lastProcess == null) {
                                process.startAfterTerminationOf(10000, startCluster);
                            } else {
                                process.startAfterTerminationOf(10000, lastProcess);
                            }

                            lastProcess = process;
                        }

                        terminateAfterTerminationOf(100000, lastProcess);
                    }
                }, events);
        }
    }


    public static final Operation1 startServerOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {
                final NetAddress selfAdr;
                final NetAddress bsAdr;

                {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName("192.168.0." + self), 45678);
                        bsAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45678);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return ParentComponent.class;
                }

                @Override
                public String toString() {
                    return "StartNode<" + selfAdr.toString() + ">";
                }

                @Override
                public Init getComponentInit() {
                    return Init.NONE;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("id2203.project.address", selfAdr);
                    if (self != 1) { // don't put this at the bootstrap server, or it will act as a bootstrap client
                        config.put("id2203.project.bootstrap-address", bsAdr);
                    }
                    return config;
                }
            };
        }
    };

    public static final Operation1 startClientOp(final UUID uuid, final String key, final String value) { return new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {
                final NetAddress selfAdr;
                final NetAddress bsAdr;

                {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName("192.168.1." + self), 45678);
                        bsAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45678);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return ScenarioClient.class;
                }

                @Override
                public String toString() {
                    return "StartClient<" + selfAdr.toString() + ">";
                }

                @Override
                public Init getComponentInit() {
                    return new ScenarioClient.Init(uuid, key, value);
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("id2203.project.address", selfAdr);
                    config.put("id2203.project.bootstrap-address", bsAdr);
                    return config;
                }
            };
        }
        };
    }
}

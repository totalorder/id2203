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

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.kvstore.OpResponse;
import se.kth.id2203.kvstore.GetOperation;
import se.kth.id2203.kvstore.PutOperation;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.RouteMsg;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.simulator.core.SimulatorPort;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class ScenarioClient extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(ScenarioClient.class);
    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<SimulatorPort> simulator = positive(SimulatorPort.class);

    //******* Fields ******
    private final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private final NetAddress server = config().getValue("id2203.project.bootstrap-address", NetAddress.class);
    private final SimulationResultMap res = SimulationResultSingleton.getInstance();
    private final Map<UUID, String> pending = new TreeMap<>();
    private UUID uuid;
    private String key;
    private String value;

    public ScenarioClient(Init init) {
        uuid = init.uuid;
        key = init.key;
        value = init.value;
    }

//    //******* Handlers ******
//    protected final Handler<Start> startHandler = new Handler<Start>() {
//
//        @Override
//        public void handle(Start event) {
//            GetOperation op = new GetOperation(key);
//            if (value != null) {
//                op = new PutOperation(key, value);
//            }
//            RouteMsg rm = new RouteMsg(op.key(), op); // don't know which partition is responsible, so ask the bootstrap server to forward it
//            trigger(new Message(self, server, rm), net);
//            pending.put(op.id(), op.key());
//            LOG.info("Sending {}", op);
//            res.put(uuid.toString(), "SENT");
//        }
//    };
//    protected final ClassMatchedHandler<OpResponse, Message> responseHandler = new ClassMatchedHandler<OpResponse, Message>() {
//
//        @Override
//        public void handle(OpResponse content, Message context) {
//            LOG.debug("Got OpResponse: {}", content);
//            LOG.debug("OpResponse UUID: {}", uuid);
//
//            String key = pending.remove(content.id);
//            if (key == null) {
//                LOG.warn("ID {} was not pending! Ignoring response.", content.id);
//                return;
//            }
//            if (content.status.equals(OpResponse.Code.OK) && content.value != null) {
//                res.put(uuid.toString(), content.value);
//            }  else {
//                res.put(uuid.toString(), content.status.toString());
//            }
//        }
//    };

    public static class Init extends se.sics.kompics.Init<ScenarioClient> {
        public final UUID uuid;
        public final String key;
        public final String value;

        public Init(final UUID uuid, final String key, final String value) {
            this.uuid = uuid;
            this.key = key;
            this.value = value;
        }
    }

    {
//        subscribe(startHandler, control);
//        subscribe(responseHandler, net);
    }
}

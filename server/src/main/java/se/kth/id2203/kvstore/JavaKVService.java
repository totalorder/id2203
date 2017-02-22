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
package se.kth.id2203.kvstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.kvstore.OpResponse.Code;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.Routing;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class JavaKVService extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(JavaKVService.class);
    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Routing> route = requires(Routing.class);
    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    final Map<String, String> store = new HashMap<>();
    //******* Handlers ******
    protected final ClassMatchedHandler<GetOperation, Message> opHandler = new ClassMatchedHandler<GetOperation, Message>() {
        @Override
        public void handle(GetOperation content, Message context) {
            LOG.info("Got operation: {}", content);
            LOG.info(Arrays.toString(store.entrySet().toArray()));
            final String value = store.get(content.key());
            if (value == null) {
                trigger(new Message(self, context.getSource(), new OpResponse(content.id(), Code.NOT_FOUND, null)), net);
                return;
            }
            trigger(new Message(self, context.getSource(), new OpResponse(content.id(), Code.OK, value)), net);
        }
    };

    protected final ClassMatchedHandler<PutOperation, Message> putOpHandler = new ClassMatchedHandler<PutOperation, Message>() {
        @Override
        public void handle(PutOperation operation, Message context) {
            LOG.info("Got operation: {}", operation);
            store.put(operation.key(), operation.value());
            LOG.info(Arrays.toString(store.entrySet().toArray()));
            trigger(new Message(self, context.getSource(), new OpResponse(operation.id(), Code.OK, operation.value())), net);
        }
    };

    {
        subscribe(opHandler, net);
        subscribe(putOpHandler, net);
    }

}

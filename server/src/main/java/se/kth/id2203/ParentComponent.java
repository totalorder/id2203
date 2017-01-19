package se.kth.id2203;

import com.google.common.base.Optional;
import se.kth.id2203.bootstrapping.BootstrapClient;
import se.kth.id2203.bootstrapping.BootstrapServer;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.kvstore.KVService;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.Routing;
import se.kth.id2203.overlay.VSOverlayManager;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

public class ParentComponent
        extends ComponentDefinition {

    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    protected final Component timer = create(JavaTimer.class, Init.NONE);
    protected final Component net = create(NettyNetwork.class, new NettyInit(self));
    protected final Component overlay = create(VSOverlayManager.class, Init.NONE);
    protected final Component kv = create(KVService.class, Init.NONE);

    {

        Optional<NetAddress> serverO = config().readValue("id2203.project.bootstrap-address", NetAddress.class);
        Component c;
        if (serverO.isPresent()) { // start in client mode
            c = create(BootstrapClient.class, Init.NONE);
        } else { // start in server mode
            c = create(BootstrapServer.class, Init.NONE);
        }
        connect(timer.getPositive(Timer.class), c.getNegative(Timer.class), Channel.TWO_WAY);
        connect(net.getPositive(Network.class), c.getNegative(Network.class), Channel.TWO_WAY);
        // Overlay
        connect(c.getPositive(Bootstrapping.class), overlay.getNegative(Bootstrapping.class), Channel.TWO_WAY);
        connect(net.getPositive(Network.class), overlay.getNegative(Network.class), Channel.TWO_WAY);
        // KV
        connect(overlay.getPositive(Routing.class), kv.getNegative(Routing.class), Channel.TWO_WAY);
        connect(net.getPositive(Network.class), kv.getNegative(Network.class), Channel.TWO_WAY);
    }
}

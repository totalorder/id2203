package se.kth.id2203.client;

import se.kth.id2203.kvstore.ClientService;
import se.kth.id2203.networking.NetAddress;
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
    protected final Component client = create(ClientService.class, Init.NONE);

    {
        connect(timer.getPositive(Timer.class), client.getNegative(Timer.class), Channel.TWO_WAY);
        connect(net.getPositive(Network.class), client.getNegative(Network.class), Channel.TWO_WAY);
    }
}

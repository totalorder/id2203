package se.kth.id2203;

import com.google.common.base.Optional;
import se.kth.id2203.bootstrapping.BootstrapClient;
import se.kth.id2203.bootstrapping.BootstrapServer;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.components.beb.BestEffortBroadcast;
import se.kth.id2203.components.beb.BestEffortBroadcastPort;
import se.kth.id2203.components.overlay.GroupPort;
import se.kth.id2203.components.riwcm.RIWCMPort;
import se.kth.id2203.components.riwcm.ReadImposeWriteConsultMajority;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.VSOverlayManager;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.sl.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class ParentComponent
        extends ComponentDefinition {

    public ParentComponent(ParentComponentInit init) {
    }

    public ParentComponent() {
    }

    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    //******* Children ******
    protected final Component overlay = create(VSOverlayManager.class, Init.NONE);
    protected final Component bestEfforBroadcast = create(BestEffortBroadcast.class, Init.NONE);
    protected final Component riwcmComponent = create(ReadImposeWriteConsultMajority.class, Init.NONE);
    protected final Component boot;

    {
        Optional<NetAddress> serverO = config().readValue("id2203.project.bootstrap-address", NetAddress.class);
        if (serverO.isPresent()) { // start in client mode
            boot = create(BootstrapClient.class, Init.NONE);
        } else { // start in server mode
            boot = create(BootstrapServer.class, Init.NONE);
        }
        connect(timer, boot.getNegative(Timer.class), Channel.TWO_WAY);
        connect(net, boot.getNegative(Network.class), Channel.TWO_WAY);
        // Overlay
        connect(boot.getPositive(Bootstrapping.class), overlay.getNegative(Bootstrapping.class), Channel.TWO_WAY);
        connect(net, overlay.getNegative(Network.class), Channel.TWO_WAY);
        connect(net, bestEfforBroadcast.getNegative(Network.class), Channel.TWO_WAY);

        connect(bestEfforBroadcast.getPositive(BestEffortBroadcastPort.class), riwcmComponent.getNegative(BestEffortBroadcastPort.class), Channel.TWO_WAY);
        connect(overlay.getPositive(GroupPort.class), riwcmComponent.getNegative(GroupPort.class), Channel.TWO_WAY);
        connect(riwcmComponent.getPositive(RIWCMPort.class), overlay.getNegative(RIWCMPort.class), Channel.TWO_WAY);
        connect(net, riwcmComponent.getNegative(Network.class), Channel.TWO_WAY);
    }
}

package se.kth.id2203.networking;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.network.Transport;

public class JavaMessage extends NetMessage implements PatternExtractor<Class, KompicsEvent> {

    private static final long serialVersionUID = -5669973156467202337L;

    private final KompicsEvent _payload;

    public JavaMessage(NetAddress src, NetAddress dst, KompicsEvent payload) {
        super(src, dst, Transport.TCP);
        this._payload = payload;
    }

    @Override
    public Class extractPattern() {
        return _payload.getClass();
    }

    @Override
    public KompicsEvent extractValue() {
        return _payload;
    }
}

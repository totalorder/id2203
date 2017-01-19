/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2203.kvstore;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.UUID;
import se.sics.kompics.KompicsEvent;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class OpResponse implements KompicsEvent, Serializable {

    private static final long serialVersionUID = -1668600257615491286L;

    public final UUID id;
    public final Code status;

    public OpResponse(UUID id, Code status) {
        this.id = id;
        this.status = status;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("status", status)
                .toString();
    }

    public static enum Code {

        OK, NOT_FOUND, NOT_IMPLEMENTED;
    }
}

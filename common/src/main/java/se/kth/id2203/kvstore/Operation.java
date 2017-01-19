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
public class Operation implements KompicsEvent, Serializable {

    private static final long serialVersionUID = 2525600659083087179L;

    public final String key;
    public final UUID id;

    public Operation(String key) {
        this.key = key;
        this.id = UUID.randomUUID();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("key", key)
                .toString();
    }
}

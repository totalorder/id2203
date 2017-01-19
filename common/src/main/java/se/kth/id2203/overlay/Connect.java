/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2203.overlay;

import java.io.Serializable;
import java.util.UUID;
import se.sics.kompics.KompicsEvent;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class Connect implements KompicsEvent, Serializable {

    private static final long serialVersionUID = -8702183971999213125L;

    public final UUID id;

    public Connect(UUID id) {
        this.id = id;
    }

    public Ack ack(int clusterSize) {
        return new Ack(id, clusterSize);
    }
    
    public static class Ack implements KompicsEvent, Serializable {

        private static final long serialVersionUID = -8702183971999213125L;

        public final UUID id;
        public final int clusterSize;

        public Ack(UUID id, int clusterSize) {
            this.id = id;
            this.clusterSize = clusterSize;
        }
    }
}

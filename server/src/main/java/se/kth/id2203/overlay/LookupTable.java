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
package se.kth.id2203.overlay;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeMultimap;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import javafx.util.Pair;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.bootstrapping.Node;
import se.kth.id2203.bootstrapping.NodeAssignment;
import se.kth.id2203.networking.NetAddress;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class LookupTable implements NodeAssignment {
    private static Logger LOG = LoggerFactory.getLogger(LookupTable.class);
    private final static int REPLICATION_FACTOR = 2;

    private static final long serialVersionUID = -8766981433378303267L;

    private final TreeMultimap<Integer, NetAddress> partitions = TreeMultimap.create();

    public Collection<NetAddress> lookup(String key) {
        int keyHash = hash(key);
        Integer partition = partitions.keySet().floor(keyHash);
        if (partition == null) {
            partition = partitions.keySet().last();
        }
        return partitions.get(partition);
    }

    public Collection<NetAddress> getNodes() {
        return partitions.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LookupTable(\n");
        for (Integer key : partitions.keySet()) {
            sb.append(key);
            sb.append(" -> ");
            sb.append(Iterables.toString(partitions.get(key)));
            sb.append("\n");
        }
        sb.append(")");
        return sb.toString();
    }

    static LookupTable generate(ImmutableSet<Node> nodes) {
        // Create a list of (hash(address), address) tuples ordered by hash(address)
        final List<Pair<Integer, NetAddress>> keys = nodes.stream()
                .map(node -> new Pair<>(hash(node.id().toString()), node.address()))
                .sorted(Comparator.comparing(Pair::getKey))
                .collect(Collectors.toList());

        // Create a lookup table where each node is in the partition of its hash,
        // as well as in the following N partitions.
        final LookupTable lut = new LookupTable();
        for (int i = 0; i < keys.size(); i++) {
            final NetAddress address = keys.get(i).getValue();
            for (int r = 0; r < REPLICATION_FACTOR; r++) {
                lut.partitions.put(keys.get((i + r) % (keys.size())).getKey(), address);
            }
        }
        return lut;
    }

    private static int hash(final String key) {
        return new BigInteger(DigestUtils.sha1(key.getBytes())).mod(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
    }
}

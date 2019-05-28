/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.cluster;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.atomix.cluster.discovery.BootstrapDiscoveryConfig;
import io.atomix.cluster.discovery.DiscoveryConfig;
import io.atomix.cluster.discovery.Node;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Atomix cluster test.
 */
public class AtomixClusterTest {

  @Test
  public void testBootstrap() throws Exception {
    Collection<Node> bootstrapLocations = Arrays.asList(
        Node.newBuilder().setId("foo").setHost("localhost").setPort(5000).build(),
        Node.newBuilder().setId("bar").setHost("localhost").setPort(5001).build(),
        Node.newBuilder().setId("baz").setHost("localhost").setPort(5002).build());

    AtomixCluster cluster1 = new AtomixCluster(ClusterConfig.newBuilder()
        .setNode(Member.newBuilder()
            .setId("foo")
            .setHost("localhost")
            .setPort(5000)
            .build())
        .setDiscovery(DiscoveryConfig.newBuilder()
            .setBootstrap(BootstrapDiscoveryConfig.newBuilder()
                .addAllNodes(bootstrapLocations)
                .build())
            .build())
        .build());
    cluster1.start().join();

    assertEquals("foo", cluster1.getMembershipService().getLocalMember().getId());

    AtomixCluster cluster2 = new AtomixCluster(ClusterConfig.newBuilder()
        .setNode(Member.newBuilder()
            .setId("bar")
            .setHost("localhost")
            .setPort(5001)
            .build())
        .setDiscovery(DiscoveryConfig.newBuilder()
            .setBootstrap(BootstrapDiscoveryConfig.newBuilder()
                .addAllNodes(bootstrapLocations)
                .build())
            .build())
        .build());
    cluster2.start().join();

    assertEquals("bar", cluster2.getMembershipService().getLocalMember().getId());

    AtomixCluster cluster3 = new AtomixCluster(ClusterConfig.newBuilder()
        .setNode(Member.newBuilder()
            .setId("baz")
            .setHost("localhost")
            .setPort(5002)
            .build())
        .setDiscovery(DiscoveryConfig.newBuilder()
            .setBootstrap(BootstrapDiscoveryConfig.newBuilder()
                .addAllNodes(bootstrapLocations)
                .build())
            .build())
        .build());
    cluster3.start().join();

    assertEquals("baz", cluster3.getMembershipService().getLocalMember().getId());

    List<CompletableFuture<Void>> futures = Stream.of(cluster1, cluster2, cluster3).map(AtomixCluster::stop)
        .collect(Collectors.toList());
    try {
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
    } catch (Exception e) {
      // Do nothing
    }
  }
}

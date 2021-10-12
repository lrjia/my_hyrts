package com.google.common.graph;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nullable;

class ConfigurableValueGraph<N, V> extends AbstractValueGraph<N, V> {
   private final boolean isDirected;
   private final boolean allowsSelfLoops;
   private final ElementOrder<N> nodeOrder;
   protected final MapIteratorCache<N, GraphConnections<N, V>> nodeConnections;
   protected long edgeCount;

   ConfigurableValueGraph(AbstractGraphBuilder<? super N> builder) {
      this(builder, builder.nodeOrder.createMap((Integer)builder.expectedNodeCount.or((int)10)), 0L);
   }

   ConfigurableValueGraph(AbstractGraphBuilder<? super N> builder, Map<N, GraphConnections<N, V>> nodeConnections, long edgeCount) {
      this.isDirected = builder.directed;
      this.allowsSelfLoops = builder.allowsSelfLoops;
      this.nodeOrder = builder.nodeOrder.cast();
      this.nodeConnections = (MapIteratorCache)(nodeConnections instanceof TreeMap ? new MapRetrievalCache(nodeConnections) : new MapIteratorCache(nodeConnections));
      this.edgeCount = Graphs.checkNonNegative(edgeCount);
   }

   public Set<N> nodes() {
      return this.nodeConnections.unmodifiableKeySet();
   }

   public boolean isDirected() {
      return this.isDirected;
   }

   public boolean allowsSelfLoops() {
      return this.allowsSelfLoops;
   }

   public ElementOrder<N> nodeOrder() {
      return this.nodeOrder;
   }

   public Set<N> adjacentNodes(Object node) {
      return this.checkedConnections(node).adjacentNodes();
   }

   public Set<N> predecessors(Object node) {
      return this.checkedConnections(node).predecessors();
   }

   public Set<N> successors(Object node) {
      return this.checkedConnections(node).successors();
   }

   public V edgeValueOrDefault(Object nodeU, Object nodeV, @Nullable V defaultValue) {
      GraphConnections<N, V> connectionsU = (GraphConnections)this.nodeConnections.get(nodeU);
      if (connectionsU == null) {
         return defaultValue;
      } else {
         V value = connectionsU.value(nodeV);
         return value == null ? defaultValue : value;
      }
   }

   protected long edgeCount() {
      return this.edgeCount;
   }

   protected final GraphConnections<N, V> checkedConnections(Object node) {
      GraphConnections<N, V> connections = (GraphConnections)this.nodeConnections.get(node);
      if (connections == null) {
         Preconditions.checkNotNull(node);
         throw new IllegalArgumentException(String.format("Node %s is not an element of this graph.", node));
      } else {
         return connections;
      }
   }

   protected final boolean containsNode(@Nullable Object node) {
      return this.nodeConnections.containsKey(node);
   }
}

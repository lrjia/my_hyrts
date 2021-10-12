package com.google.common.graph;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nullable;

class ConfigurableNetwork<N, E> extends AbstractNetwork<N, E> {
   private final boolean isDirected;
   private final boolean allowsParallelEdges;
   private final boolean allowsSelfLoops;
   private final ElementOrder<N> nodeOrder;
   private final ElementOrder<E> edgeOrder;
   protected final MapIteratorCache<N, NetworkConnections<N, E>> nodeConnections;
   protected final MapIteratorCache<E, N> edgeToReferenceNode;

   ConfigurableNetwork(NetworkBuilder<? super N, ? super E> builder) {
      this(builder, builder.nodeOrder.createMap((Integer)builder.expectedNodeCount.or((int)10)), builder.edgeOrder.createMap((Integer)builder.expectedEdgeCount.or((int)20)));
   }

   ConfigurableNetwork(NetworkBuilder<? super N, ? super E> builder, Map<N, NetworkConnections<N, E>> nodeConnections, Map<E, N> edgeToReferenceNode) {
      this.isDirected = builder.directed;
      this.allowsParallelEdges = builder.allowsParallelEdges;
      this.allowsSelfLoops = builder.allowsSelfLoops;
      this.nodeOrder = builder.nodeOrder.cast();
      this.edgeOrder = builder.edgeOrder.cast();
      this.nodeConnections = (MapIteratorCache)(nodeConnections instanceof TreeMap ? new MapRetrievalCache(nodeConnections) : new MapIteratorCache(nodeConnections));
      this.edgeToReferenceNode = new MapIteratorCache(edgeToReferenceNode);
   }

   public Set<N> nodes() {
      return this.nodeConnections.unmodifiableKeySet();
   }

   public Set<E> edges() {
      return this.edgeToReferenceNode.unmodifiableKeySet();
   }

   public boolean isDirected() {
      return this.isDirected;
   }

   public boolean allowsParallelEdges() {
      return this.allowsParallelEdges;
   }

   public boolean allowsSelfLoops() {
      return this.allowsSelfLoops;
   }

   public ElementOrder<N> nodeOrder() {
      return this.nodeOrder;
   }

   public ElementOrder<E> edgeOrder() {
      return this.edgeOrder;
   }

   public Set<E> incidentEdges(Object node) {
      return this.checkedConnections(node).incidentEdges();
   }

   public EndpointPair<N> incidentNodes(Object edge) {
      N nodeU = this.checkedReferenceNode(edge);
      N nodeV = ((NetworkConnections)this.nodeConnections.get(nodeU)).oppositeNode(edge);
      return EndpointPair.of((Network)this, nodeU, nodeV);
   }

   public Set<N> adjacentNodes(Object node) {
      return this.checkedConnections(node).adjacentNodes();
   }

   public Set<E> edgesConnecting(Object nodeU, Object nodeV) {
      NetworkConnections<N, E> connectionsU = this.checkedConnections(nodeU);
      if (!this.allowsSelfLoops && nodeU == nodeV) {
         return ImmutableSet.of();
      } else {
         Preconditions.checkArgument(this.containsNode(nodeV), "Node %s is not an element of this graph.", nodeV);
         return connectionsU.edgesConnecting(nodeV);
      }
   }

   public Set<E> inEdges(Object node) {
      return this.checkedConnections(node).inEdges();
   }

   public Set<E> outEdges(Object node) {
      return this.checkedConnections(node).outEdges();
   }

   public Set<N> predecessors(Object node) {
      return this.checkedConnections(node).predecessors();
   }

   public Set<N> successors(Object node) {
      return this.checkedConnections(node).successors();
   }

   protected final NetworkConnections<N, E> checkedConnections(Object node) {
      NetworkConnections<N, E> connections = (NetworkConnections)this.nodeConnections.get(node);
      if (connections == null) {
         Preconditions.checkNotNull(node);
         throw new IllegalArgumentException(String.format("Node %s is not an element of this graph.", node));
      } else {
         return connections;
      }
   }

   protected final N checkedReferenceNode(Object edge) {
      N referenceNode = this.edgeToReferenceNode.get(edge);
      if (referenceNode == null) {
         Preconditions.checkNotNull(edge);
         throw new IllegalArgumentException(String.format("Edge %s is not an element of this graph.", edge));
      } else {
         return referenceNode;
      }
   }

   protected final boolean containsNode(@Nullable Object node) {
      return this.nodeConnections.containsKey(node);
   }

   protected final boolean containsEdge(@Nullable Object edge) {
      return this.edgeToReferenceNode.containsKey(edge);
   }
}

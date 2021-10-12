package com.google.common.graph;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class UndirectedGraphConnections<N, V> implements GraphConnections<N, V> {
   private final Map<N, V> adjacentNodeValues;

   private UndirectedGraphConnections(Map<N, V> adjacentNodeValues) {
      this.adjacentNodeValues = (Map)Preconditions.checkNotNull(adjacentNodeValues);
   }

   static <N, V> UndirectedGraphConnections<N, V> of() {
      return new UndirectedGraphConnections(new HashMap(2, 1.0F));
   }

   static <N, V> UndirectedGraphConnections<N, V> ofImmutable(Map<N, V> adjacentNodeValues) {
      return new UndirectedGraphConnections(ImmutableMap.copyOf(adjacentNodeValues));
   }

   public Set<N> adjacentNodes() {
      return Collections.unmodifiableSet(this.adjacentNodeValues.keySet());
   }

   public Set<N> predecessors() {
      return this.adjacentNodes();
   }

   public Set<N> successors() {
      return this.adjacentNodes();
   }

   public V value(Object node) {
      return this.adjacentNodeValues.get(node);
   }

   public void removePredecessor(Object node) {
      this.removeSuccessor(node);
   }

   public V removeSuccessor(Object node) {
      return this.adjacentNodeValues.remove(node);
   }

   public void addPredecessor(N node, V value) {
      this.addSuccessor(node, value);
   }

   public V addSuccessor(N node, V value) {
      return this.adjacentNodeValues.put(node, value);
   }
}

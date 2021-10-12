package com.google.common.graph;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

abstract class AbstractUndirectedNetworkConnections<N, E> implements NetworkConnections<N, E> {
   protected final Map<E, N> incidentEdgeMap;

   protected AbstractUndirectedNetworkConnections(Map<E, N> incidentEdgeMap) {
      this.incidentEdgeMap = (Map)Preconditions.checkNotNull(incidentEdgeMap);
   }

   public Set<N> predecessors() {
      return this.adjacentNodes();
   }

   public Set<N> successors() {
      return this.adjacentNodes();
   }

   public Set<E> incidentEdges() {
      return Collections.unmodifiableSet(this.incidentEdgeMap.keySet());
   }

   public Set<E> inEdges() {
      return this.incidentEdges();
   }

   public Set<E> outEdges() {
      return this.incidentEdges();
   }

   public N oppositeNode(Object edge) {
      return Preconditions.checkNotNull(this.incidentEdgeMap.get(edge));
   }

   public N removeInEdge(Object edge, boolean isSelfLoop) {
      return !isSelfLoop ? this.removeOutEdge(edge) : null;
   }

   public N removeOutEdge(Object edge) {
      N previousNode = this.incidentEdgeMap.remove(edge);
      return Preconditions.checkNotNull(previousNode);
   }

   public void addInEdge(E edge, N node, boolean isSelfLoop) {
      if (!isSelfLoop) {
         this.addOutEdge(edge, node);
      }

   }

   public void addOutEdge(E edge, N node) {
      N previousNode = this.incidentEdgeMap.put(edge, node);
      Preconditions.checkState(previousNode == null);
   }
}

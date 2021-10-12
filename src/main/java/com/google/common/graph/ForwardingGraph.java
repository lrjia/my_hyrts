package com.google.common.graph;

import java.util.Set;

abstract class ForwardingGraph<N> extends AbstractGraph<N> {
   protected abstract Graph<N> delegate();

   public Set<N> nodes() {
      return this.delegate().nodes();
   }

   public Set<EndpointPair<N>> edges() {
      return this.delegate().edges();
   }

   public boolean isDirected() {
      return this.delegate().isDirected();
   }

   public boolean allowsSelfLoops() {
      return this.delegate().allowsSelfLoops();
   }

   public ElementOrder<N> nodeOrder() {
      return this.delegate().nodeOrder();
   }

   public Set<N> adjacentNodes(Object node) {
      return this.delegate().adjacentNodes(node);
   }

   public Set<N> predecessors(Object node) {
      return this.delegate().predecessors(node);
   }

   public Set<N> successors(Object node) {
      return this.delegate().successors(node);
   }

   public int degree(Object node) {
      return this.delegate().degree(node);
   }

   public int inDegree(Object node) {
      return this.delegate().inDegree(node);
   }

   public int outDegree(Object node) {
      return this.delegate().outDegree(node);
   }
}

package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.math.IntMath;
import com.google.common.primitives.Ints;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;

@Beta
public abstract class AbstractGraph<N> implements Graph<N> {
   protected long edgeCount() {
      long degreeSum = 0L;

      Object node;
      for(Iterator i$ = this.nodes().iterator(); i$.hasNext(); degreeSum += (long)this.degree(node)) {
         node = i$.next();
      }

      Preconditions.checkState((degreeSum & 1L) == 0L);
      return degreeSum >>> 1;
   }

   public Set<EndpointPair<N>> edges() {
      return new AbstractSet<EndpointPair<N>>() {
         public UnmodifiableIterator<EndpointPair<N>> iterator() {
            return EndpointPairIterator.of(AbstractGraph.this);
         }

         public int size() {
            return Ints.saturatedCast(AbstractGraph.this.edgeCount());
         }

         public boolean contains(@Nullable Object obj) {
            if (!(obj instanceof EndpointPair)) {
               return false;
            } else {
               EndpointPair<?> endpointPair = (EndpointPair)obj;
               return AbstractGraph.this.isDirected() == endpointPair.isOrdered() && AbstractGraph.this.nodes().contains(endpointPair.nodeU()) && AbstractGraph.this.successors(endpointPair.nodeU()).contains(endpointPair.nodeV());
            }
         }
      };
   }

   public int degree(Object node) {
      if (this.isDirected()) {
         return IntMath.saturatedAdd(this.predecessors(node).size(), this.successors(node).size());
      } else {
         Set<N> neighbors = this.adjacentNodes(node);
         int selfLoopCount = this.allowsSelfLoops() && neighbors.contains(node) ? 1 : 0;
         return IntMath.saturatedAdd(neighbors.size(), selfLoopCount);
      }
   }

   public int inDegree(Object node) {
      return this.isDirected() ? this.predecessors(node).size() : this.degree(node);
   }

   public int outDegree(Object node) {
      return this.isDirected() ? this.successors(node).size() : this.degree(node);
   }

   public String toString() {
      String propertiesString = String.format("isDirected: %s, allowsSelfLoops: %s", this.isDirected(), this.allowsSelfLoops());
      return String.format("%s, nodes: %s, edges: %s", propertiesString, this.nodes(), this.edges());
   }
}

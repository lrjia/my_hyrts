package com.google.common.graph;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;

abstract class EndpointPairIterator<N> extends AbstractIterator<EndpointPair<N>> {
   private final Graph<N> graph;
   private final Iterator<N> nodeIterator;
   protected N node;
   protected Iterator<N> successorIterator;

   static <N> EndpointPairIterator<N> of(Graph<N> graph) {
      return (EndpointPairIterator)(graph.isDirected() ? new EndpointPairIterator.Directed(graph) : new EndpointPairIterator.Undirected(graph));
   }

   private EndpointPairIterator(Graph<N> graph) {
      this.node = null;
      this.successorIterator = ImmutableSet.of().iterator();
      this.graph = graph;
      this.nodeIterator = graph.nodes().iterator();
   }

   protected final boolean advance() {
      Preconditions.checkState(!this.successorIterator.hasNext());
      if (!this.nodeIterator.hasNext()) {
         return false;
      } else {
         this.node = this.nodeIterator.next();
         this.successorIterator = this.graph.successors(this.node).iterator();
         return true;
      }
   }

   // $FF: synthetic method
   EndpointPairIterator(Graph x0, Object x1) {
      this(x0);
   }

   private static final class Undirected<N> extends EndpointPairIterator<N> {
      private Set<N> visitedNodes;

      private Undirected(Graph<N> graph) {
         super(graph, null);
         this.visitedNodes = Sets.newHashSetWithExpectedSize(graph.nodes().size());
      }

      protected EndpointPair<N> computeNext() {
         while(true) {
            if (this.successorIterator.hasNext()) {
               N otherNode = this.successorIterator.next();
               if (!this.visitedNodes.contains(otherNode)) {
                  return EndpointPair.unordered(this.node, otherNode);
               }
            } else {
               this.visitedNodes.add(this.node);
               if (!this.advance()) {
                  this.visitedNodes = null;
                  return (EndpointPair)this.endOfData();
               }
            }
         }
      }

      // $FF: synthetic method
      Undirected(Graph x0, Object x1) {
         this(x0);
      }
   }

   private static final class Directed<N> extends EndpointPairIterator<N> {
      private Directed(Graph<N> graph) {
         super(graph, null);
      }

      protected EndpointPair<N> computeNext() {
         do {
            if (this.successorIterator.hasNext()) {
               return EndpointPair.ordered(this.node, this.successorIterator.next());
            }
         } while(this.advance());

         return (EndpointPair)this.endOfData();
      }

      // $FF: synthetic method
      Directed(Graph x0, Object x1) {
         this(x0);
      }
   }
}

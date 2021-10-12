package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.math.IntMath;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

@Beta
public abstract class AbstractNetwork<N, E> implements Network<N, E> {
   public Graph<N> asGraph() {
      return new AbstractGraph<N>() {
         public Set<N> nodes() {
            return AbstractNetwork.this.nodes();
         }

         public Set<EndpointPair<N>> edges() {
            return (Set)(AbstractNetwork.this.allowsParallelEdges() ? super.edges() : new AbstractSet<EndpointPair<N>>() {
               public Iterator<EndpointPair<N>> iterator() {
                  return Iterators.transform(AbstractNetwork.this.edges().iterator(), new Function<E, EndpointPair<N>>() {
                     public EndpointPair<N> apply(E edge) {
                        return AbstractNetwork.this.incidentNodes(edge);
                     }
                  });
               }

               public int size() {
                  return AbstractNetwork.this.edges().size();
               }

               public boolean contains(@Nullable Object obj) {
                  if (!(obj instanceof EndpointPair)) {
                     return false;
                  } else {
                     EndpointPair<?> endpointPair = (EndpointPair)obj;
                     return isDirected() == endpointPair.isOrdered() && nodes().contains(endpointPair.nodeU()) && successors(endpointPair.nodeU()).contains(endpointPair.nodeV());
                  }
               }
            });
         }

         public ElementOrder<N> nodeOrder() {
            return AbstractNetwork.this.nodeOrder();
         }

         public boolean isDirected() {
            return AbstractNetwork.this.isDirected();
         }

         public boolean allowsSelfLoops() {
            return AbstractNetwork.this.allowsSelfLoops();
         }

         public Set<N> adjacentNodes(Object node) {
            return AbstractNetwork.this.adjacentNodes(node);
         }

         public Set<N> predecessors(Object node) {
            return AbstractNetwork.this.predecessors(node);
         }

         public Set<N> successors(Object node) {
            return AbstractNetwork.this.successors(node);
         }
      };
   }

   public int degree(Object node) {
      return this.isDirected() ? IntMath.saturatedAdd(this.inEdges(node).size(), this.outEdges(node).size()) : IntMath.saturatedAdd(this.incidentEdges(node).size(), this.edgesConnecting(node, node).size());
   }

   public int inDegree(Object node) {
      return this.isDirected() ? this.inEdges(node).size() : this.degree(node);
   }

   public int outDegree(Object node) {
      return this.isDirected() ? this.outEdges(node).size() : this.degree(node);
   }

   public Set<E> adjacentEdges(Object edge) {
      EndpointPair<?> endpointPair = this.incidentNodes(edge);
      Set<E> endpointPairIncidentEdges = Sets.union(this.incidentEdges(endpointPair.nodeU()), this.incidentEdges(endpointPair.nodeV()));
      return Sets.difference(endpointPairIncidentEdges, ImmutableSet.of(edge));
   }

   public String toString() {
      String propertiesString = String.format("isDirected: %s, allowsParallelEdges: %s, allowsSelfLoops: %s", this.isDirected(), this.allowsParallelEdges(), this.allowsSelfLoops());
      return String.format("%s, nodes: %s, edges: %s", propertiesString, this.nodes(), this.edgeIncidentNodesMap());
   }

   private Map<E, EndpointPair<N>> edgeIncidentNodesMap() {
      Function<E, EndpointPair<N>> edgeToIncidentNodesFn = new Function<E, EndpointPair<N>>() {
         public EndpointPair<N> apply(E edge) {
            return AbstractNetwork.this.incidentNodes(edge);
         }
      };
      return Maps.asMap(this.edges(), edgeToIncidentNodesFn);
   }
}

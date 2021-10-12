package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nullable;

@Beta
public final class Graphs {
   private Graphs() {
   }

   public static boolean hasCycle(Graph<?> graph) {
      int numEdges = graph.edges().size();
      if (numEdges == 0) {
         return false;
      } else if (!graph.isDirected() && numEdges >= graph.nodes().size()) {
         return true;
      } else {
         Map<Object, Graphs.NodeVisitState> visitedNodes = Maps.newHashMapWithExpectedSize(graph.nodes().size());
         Iterator i$ = graph.nodes().iterator();

         Object node;
         do {
            if (!i$.hasNext()) {
               return false;
            }

            node = i$.next();
         } while(!subgraphHasCycle(graph, visitedNodes, node, (Object)null));

         return true;
      }
   }

   public static boolean hasCycle(Network<?, ?> network) {
      return !network.isDirected() && network.allowsParallelEdges() && network.edges().size() > network.asGraph().edges().size() ? true : hasCycle(network.asGraph());
   }

   private static boolean subgraphHasCycle(Graph<?> graph, Map<Object, Graphs.NodeVisitState> visitedNodes, Object node, @Nullable Object previousNode) {
      Graphs.NodeVisitState state = (Graphs.NodeVisitState)visitedNodes.get(node);
      if (state == Graphs.NodeVisitState.COMPLETE) {
         return false;
      } else if (state == Graphs.NodeVisitState.PENDING) {
         return true;
      } else {
         visitedNodes.put(node, Graphs.NodeVisitState.PENDING);
         Iterator i$ = graph.successors(node).iterator();

         Object nextNode;
         do {
            if (!i$.hasNext()) {
               visitedNodes.put(node, Graphs.NodeVisitState.COMPLETE);
               return false;
            }

            nextNode = i$.next();
         } while(!canTraverseWithoutReusingEdge(graph, nextNode, previousNode) || !subgraphHasCycle(graph, visitedNodes, nextNode, node));

         return true;
      }
   }

   private static boolean canTraverseWithoutReusingEdge(Graph<?> graph, Object nextNode, @Nullable Object previousNode) {
      return graph.isDirected() || !Objects.equal(previousNode, nextNode);
   }

   public static <N> Graph<N> transitiveClosure(Graph<N> graph) {
      MutableGraph<N> transitiveClosure = GraphBuilder.from(graph).allowsSelfLoops(true).build();
      if (graph.isDirected()) {
         Iterator i$ = graph.nodes().iterator();

         while(i$.hasNext()) {
            N node = i$.next();
            Iterator i$ = reachableNodes(graph, node).iterator();

            while(i$.hasNext()) {
               N reachableNode = i$.next();
               transitiveClosure.putEdge(node, reachableNode);
            }
         }

         return transitiveClosure;
      } else {
         Set<N> visitedNodes = new HashSet();
         Iterator i$ = graph.nodes().iterator();

         while(true) {
            Object node;
            do {
               if (!i$.hasNext()) {
                  return transitiveClosure;
               }

               node = i$.next();
            } while(visitedNodes.contains(node));

            Set<N> reachableNodes = reachableNodes(graph, node);
            visitedNodes.addAll(reachableNodes);
            int pairwiseMatch = 1;
            Iterator i$ = reachableNodes.iterator();

            while(i$.hasNext()) {
               N nodeU = i$.next();
               Iterator i$ = Iterables.limit(reachableNodes, pairwiseMatch++).iterator();

               while(i$.hasNext()) {
                  N nodeV = i$.next();
                  transitiveClosure.putEdge(nodeU, nodeV);
               }
            }
         }
      }
   }

   public static <N> Set<N> reachableNodes(Graph<N> graph, Object node) {
      Preconditions.checkArgument(graph.nodes().contains(node), "Node %s is not an element of this graph.", node);
      Set<N> visitedNodes = new LinkedHashSet();
      Queue<N> queuedNodes = new ArrayDeque();
      visitedNodes.add(node);
      queuedNodes.add(node);

      while(!queuedNodes.isEmpty()) {
         N currentNode = queuedNodes.remove();
         Iterator i$ = graph.successors(currentNode).iterator();

         while(i$.hasNext()) {
            N successor = i$.next();
            if (visitedNodes.add(successor)) {
               queuedNodes.add(successor);
            }
         }
      }

      return Collections.unmodifiableSet(visitedNodes);
   }

   public static boolean equivalent(@Nullable Graph<?> graphA, @Nullable Graph<?> graphB) {
      if (graphA == graphB) {
         return true;
      } else if (graphA != null && graphB != null) {
         return graphA.isDirected() == graphB.isDirected() && graphA.nodes().equals(graphB.nodes()) && graphA.edges().equals(graphB.edges());
      } else {
         return false;
      }
   }

   public static boolean equivalent(@Nullable ValueGraph<?, ?> graphA, @Nullable ValueGraph<?, ?> graphB) {
      if (graphA == graphB) {
         return true;
      } else if (graphA != null && graphB != null) {
         if (graphA.isDirected() == graphB.isDirected() && graphA.nodes().equals(graphB.nodes()) && graphA.edges().equals(graphB.edges())) {
            Iterator i$ = graphA.edges().iterator();

            EndpointPair edge;
            do {
               if (!i$.hasNext()) {
                  return true;
               }

               edge = (EndpointPair)i$.next();
            } while(graphA.edgeValue(edge.nodeU(), edge.nodeV()).equals(graphB.edgeValue(edge.nodeU(), edge.nodeV())));

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean equivalent(@Nullable Network<?, ?> networkA, @Nullable Network<?, ?> networkB) {
      if (networkA == networkB) {
         return true;
      } else if (networkA != null && networkB != null) {
         if (networkA.isDirected() == networkB.isDirected() && networkA.nodes().equals(networkB.nodes()) && networkA.edges().equals(networkB.edges())) {
            Iterator i$ = networkA.edges().iterator();

            Object edge;
            do {
               if (!i$.hasNext()) {
                  return true;
               }

               edge = i$.next();
            } while(networkA.incidentNodes(edge).equals(networkB.incidentNodes(edge)));

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static <N> Graph<N> transpose(Graph<N> graph) {
      if (!graph.isDirected()) {
         return graph;
      } else {
         return (Graph)(graph instanceof Graphs.TransposedGraph ? ((Graphs.TransposedGraph)graph).graph : new Graphs.TransposedGraph(graph));
      }
   }

   public static <N, V> ValueGraph<N, V> transpose(ValueGraph<N, V> graph) {
      if (!graph.isDirected()) {
         return graph;
      } else {
         return (ValueGraph)(graph instanceof Graphs.TransposedValueGraph ? ((Graphs.TransposedValueGraph)graph).graph : new Graphs.TransposedValueGraph(graph));
      }
   }

   public static <N, E> Network<N, E> transpose(Network<N, E> network) {
      if (!network.isDirected()) {
         return network;
      } else {
         return (Network)(network instanceof Graphs.TransposedNetwork ? ((Graphs.TransposedNetwork)network).network : new Graphs.TransposedNetwork(network));
      }
   }

   public static <N> MutableGraph<N> inducedSubgraph(Graph<N> graph, Iterable<? extends N> nodes) {
      MutableGraph<N> subgraph = GraphBuilder.from(graph).build();
      Iterator i$ = nodes.iterator();

      Object node;
      while(i$.hasNext()) {
         node = i$.next();
         subgraph.addNode(node);
      }

      i$ = subgraph.nodes().iterator();

      while(i$.hasNext()) {
         node = i$.next();
         Iterator i$ = graph.successors(node).iterator();

         while(i$.hasNext()) {
            N successorNode = i$.next();
            if (subgraph.nodes().contains(successorNode)) {
               subgraph.putEdge(node, successorNode);
            }
         }
      }

      return subgraph;
   }

   public static <N, V> MutableValueGraph<N, V> inducedSubgraph(ValueGraph<N, V> graph, Iterable<? extends N> nodes) {
      MutableValueGraph<N, V> subgraph = ValueGraphBuilder.from(graph).build();
      Iterator i$ = nodes.iterator();

      Object node;
      while(i$.hasNext()) {
         node = i$.next();
         subgraph.addNode(node);
      }

      i$ = subgraph.nodes().iterator();

      while(i$.hasNext()) {
         node = i$.next();
         Iterator i$ = graph.successors(node).iterator();

         while(i$.hasNext()) {
            N successorNode = i$.next();
            if (subgraph.nodes().contains(successorNode)) {
               subgraph.putEdgeValue(node, successorNode, graph.edgeValue(node, successorNode));
            }
         }
      }

      return subgraph;
   }

   public static <N, E> MutableNetwork<N, E> inducedSubgraph(Network<N, E> network, Iterable<? extends N> nodes) {
      MutableNetwork<N, E> subgraph = NetworkBuilder.from(network).build();
      Iterator i$ = nodes.iterator();

      Object node;
      while(i$.hasNext()) {
         node = i$.next();
         subgraph.addNode(node);
      }

      i$ = subgraph.nodes().iterator();

      while(i$.hasNext()) {
         node = i$.next();
         Iterator i$ = network.outEdges(node).iterator();

         while(i$.hasNext()) {
            E edge = i$.next();
            N successorNode = network.incidentNodes(edge).adjacentNode(node);
            if (subgraph.nodes().contains(successorNode)) {
               subgraph.addEdge(node, successorNode, edge);
            }
         }
      }

      return subgraph;
   }

   public static <N> MutableGraph<N> copyOf(Graph<N> graph) {
      MutableGraph<N> copy = GraphBuilder.from(graph).expectedNodeCount(graph.nodes().size()).build();
      Iterator i$ = graph.nodes().iterator();

      while(i$.hasNext()) {
         N node = i$.next();
         copy.addNode(node);
      }

      i$ = graph.edges().iterator();

      while(i$.hasNext()) {
         EndpointPair<N> edge = (EndpointPair)i$.next();
         copy.putEdge(edge.nodeU(), edge.nodeV());
      }

      return copy;
   }

   public static <N, V> MutableValueGraph<N, V> copyOf(ValueGraph<N, V> graph) {
      MutableValueGraph<N, V> copy = ValueGraphBuilder.from(graph).expectedNodeCount(graph.nodes().size()).build();
      Iterator i$ = graph.nodes().iterator();

      while(i$.hasNext()) {
         N node = i$.next();
         copy.addNode(node);
      }

      i$ = graph.edges().iterator();

      while(i$.hasNext()) {
         EndpointPair<N> edge = (EndpointPair)i$.next();
         copy.putEdgeValue(edge.nodeU(), edge.nodeV(), graph.edgeValue(edge.nodeU(), edge.nodeV()));
      }

      return copy;
   }

   public static <N, E> MutableNetwork<N, E> copyOf(Network<N, E> network) {
      MutableNetwork<N, E> copy = NetworkBuilder.from(network).expectedNodeCount(network.nodes().size()).expectedEdgeCount(network.edges().size()).build();
      Iterator i$ = network.nodes().iterator();

      Object edge;
      while(i$.hasNext()) {
         edge = i$.next();
         copy.addNode(edge);
      }

      i$ = network.edges().iterator();

      while(i$.hasNext()) {
         edge = i$.next();
         EndpointPair<N> endpointPair = network.incidentNodes(edge);
         copy.addEdge(endpointPair.nodeU(), endpointPair.nodeV(), edge);
      }

      return copy;
   }

   @CanIgnoreReturnValue
   static int checkNonNegative(int value) {
      Preconditions.checkArgument(value >= 0, "Not true that %s is non-negative.", value);
      return value;
   }

   @CanIgnoreReturnValue
   static int checkPositive(int value) {
      Preconditions.checkArgument(value > 0, "Not true that %s is positive.", value);
      return value;
   }

   @CanIgnoreReturnValue
   static long checkNonNegative(long value) {
      Preconditions.checkArgument(value >= 0L, "Not true that %s is non-negative.", value);
      return value;
   }

   @CanIgnoreReturnValue
   static long checkPositive(long value) {
      Preconditions.checkArgument(value > 0L, "Not true that %s is positive.", value);
      return value;
   }

   private static enum NodeVisitState {
      PENDING,
      COMPLETE;
   }

   private static class TransposedNetwork<N, E> extends AbstractNetwork<N, E> {
      private final Network<N, E> network;

      TransposedNetwork(Network<N, E> network) {
         this.network = network;
      }

      public Set<N> nodes() {
         return this.network.nodes();
      }

      public Set<E> edges() {
         return this.network.edges();
      }

      public boolean isDirected() {
         return this.network.isDirected();
      }

      public boolean allowsParallelEdges() {
         return this.network.allowsParallelEdges();
      }

      public boolean allowsSelfLoops() {
         return this.network.allowsSelfLoops();
      }

      public ElementOrder<N> nodeOrder() {
         return this.network.nodeOrder();
      }

      public ElementOrder<E> edgeOrder() {
         return this.network.edgeOrder();
      }

      public Set<N> adjacentNodes(Object node) {
         return this.network.adjacentNodes(node);
      }

      public Set<N> predecessors(Object node) {
         return this.network.successors(node);
      }

      public Set<N> successors(Object node) {
         return this.network.predecessors(node);
      }

      public Set<E> incidentEdges(Object node) {
         return this.network.incidentEdges(node);
      }

      public Set<E> inEdges(Object node) {
         return this.network.outEdges(node);
      }

      public Set<E> outEdges(Object node) {
         return this.network.inEdges(node);
      }

      public EndpointPair<N> incidentNodes(Object edge) {
         EndpointPair<N> endpointPair = this.network.incidentNodes(edge);
         return EndpointPair.of(this.network, endpointPair.nodeV(), endpointPair.nodeU());
      }

      public Set<E> adjacentEdges(Object edge) {
         return this.network.adjacentEdges(edge);
      }

      public Set<E> edgesConnecting(Object nodeU, Object nodeV) {
         return this.network.edgesConnecting(nodeV, nodeU);
      }
   }

   private static class TransposedValueGraph<N, V> extends AbstractValueGraph<N, V> {
      private final ValueGraph<N, V> graph;

      TransposedValueGraph(ValueGraph<N, V> graph) {
         this.graph = graph;
      }

      public Set<N> nodes() {
         return this.graph.nodes();
      }

      protected long edgeCount() {
         return (long)this.graph.edges().size();
      }

      public boolean isDirected() {
         return this.graph.isDirected();
      }

      public boolean allowsSelfLoops() {
         return this.graph.allowsSelfLoops();
      }

      public ElementOrder<N> nodeOrder() {
         return this.graph.nodeOrder();
      }

      public Set<N> adjacentNodes(Object node) {
         return this.graph.adjacentNodes(node);
      }

      public Set<N> predecessors(Object node) {
         return this.graph.successors(node);
      }

      public Set<N> successors(Object node) {
         return this.graph.predecessors(node);
      }

      public V edgeValue(Object nodeU, Object nodeV) {
         return this.graph.edgeValue(nodeV, nodeU);
      }

      public V edgeValueOrDefault(Object nodeU, Object nodeV, @Nullable V defaultValue) {
         return this.graph.edgeValueOrDefault(nodeV, nodeU, defaultValue);
      }
   }

   private static class TransposedGraph<N> extends AbstractGraph<N> {
      private final Graph<N> graph;

      TransposedGraph(Graph<N> graph) {
         this.graph = graph;
      }

      public Set<N> nodes() {
         return this.graph.nodes();
      }

      protected long edgeCount() {
         return (long)this.graph.edges().size();
      }

      public boolean isDirected() {
         return this.graph.isDirected();
      }

      public boolean allowsSelfLoops() {
         return this.graph.allowsSelfLoops();
      }

      public ElementOrder<N> nodeOrder() {
         return this.graph.nodeOrder();
      }

      public Set<N> adjacentNodes(Object node) {
         return this.graph.adjacentNodes(node);
      }

      public Set<N> predecessors(Object node) {
         return this.graph.successors(node);
      }

      public Set<N> successors(Object node) {
         return this.graph.predecessors(node);
      }
   }
}

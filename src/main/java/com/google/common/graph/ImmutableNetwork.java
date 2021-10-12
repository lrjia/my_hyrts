package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;

@Beta
public final class ImmutableNetwork<N, E> extends ConfigurableNetwork<N, E> {
   private ImmutableNetwork(Network<N, E> network) {
      super(NetworkBuilder.from(network), getNodeConnections(network), getEdgeToReferenceNode(network));
   }

   public static <N, E> ImmutableNetwork<N, E> copyOf(Network<N, E> network) {
      return network instanceof ImmutableNetwork ? (ImmutableNetwork)network : new ImmutableNetwork(network);
   }

   /** @deprecated */
   @Deprecated
   public static <N, E> ImmutableNetwork<N, E> copyOf(ImmutableNetwork<N, E> network) {
      return (ImmutableNetwork)Preconditions.checkNotNull(network);
   }

   public ImmutableGraph<N> asGraph() {
      final Graph<N> asGraph = super.asGraph();
      return new ImmutableGraph<N>() {
         protected Graph<N> delegate() {
            return asGraph;
         }
      };
   }

   private static <N, E> Map<N, NetworkConnections<N, E>> getNodeConnections(Network<N, E> network) {
      ImmutableMap.Builder<N, NetworkConnections<N, E>> nodeConnections = ImmutableMap.builder();
      Iterator i$ = network.nodes().iterator();

      while(i$.hasNext()) {
         N node = i$.next();
         nodeConnections.put(node, connectionsOf(network, node));
      }

      return nodeConnections.build();
   }

   private static <N, E> Map<E, N> getEdgeToReferenceNode(Network<N, E> network) {
      ImmutableMap.Builder<E, N> edgeToReferenceNode = ImmutableMap.builder();
      Iterator i$ = network.edges().iterator();

      while(i$.hasNext()) {
         E edge = i$.next();
         edgeToReferenceNode.put(edge, network.incidentNodes(edge).nodeU());
      }

      return edgeToReferenceNode.build();
   }

   private static <N, E> NetworkConnections<N, E> connectionsOf(Network<N, E> network, N node) {
      Map incidentEdgeMap;
      if (network.isDirected()) {
         incidentEdgeMap = Maps.asMap(network.inEdges(node), sourceNodeFn(network));
         Map<E, N> outEdgeMap = Maps.asMap(network.outEdges(node), targetNodeFn(network));
         int selfLoopCount = network.edgesConnecting(node, node).size();
         return (NetworkConnections)(network.allowsParallelEdges() ? DirectedMultiNetworkConnections.ofImmutable(incidentEdgeMap, outEdgeMap, selfLoopCount) : DirectedNetworkConnections.ofImmutable(incidentEdgeMap, outEdgeMap, selfLoopCount));
      } else {
         incidentEdgeMap = Maps.asMap(network.incidentEdges(node), adjacentNodeFn(network, node));
         return (NetworkConnections)(network.allowsParallelEdges() ? UndirectedMultiNetworkConnections.ofImmutable(incidentEdgeMap) : UndirectedNetworkConnections.ofImmutable(incidentEdgeMap));
      }
   }

   private static <N, E> Function<E, N> sourceNodeFn(final Network<N, E> network) {
      return new Function<E, N>() {
         public N apply(E edge) {
            return network.incidentNodes(edge).source();
         }
      };
   }

   private static <N, E> Function<E, N> targetNodeFn(final Network<N, E> network) {
      return new Function<E, N>() {
         public N apply(E edge) {
            return network.incidentNodes(edge).target();
         }
      };
   }

   private static <N, E> Function<E, N> adjacentNodeFn(final Network<N, E> network, final N node) {
      return new Function<E, N>() {
         public N apply(E edge) {
            return network.incidentNodes(edge).adjacentNode(node);
         }
      };
   }
}

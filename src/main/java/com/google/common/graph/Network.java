package com.google.common.graph;

import com.google.common.annotations.Beta;
import java.util.Set;
import javax.annotation.Nullable;

@Beta
public interface Network<N, E> {
   Set<N> nodes();

   Set<E> edges();

   Graph<N> asGraph();

   boolean isDirected();

   boolean allowsParallelEdges();

   boolean allowsSelfLoops();

   ElementOrder<N> nodeOrder();

   ElementOrder<E> edgeOrder();

   Set<N> adjacentNodes(Object var1);

   Set<N> predecessors(Object var1);

   Set<N> successors(Object var1);

   Set<E> incidentEdges(Object var1);

   Set<E> inEdges(Object var1);

   Set<E> outEdges(Object var1);

   int degree(Object var1);

   int inDegree(Object var1);

   int outDegree(Object var1);

   EndpointPair<N> incidentNodes(Object var1);

   Set<E> adjacentEdges(Object var1);

   Set<E> edgesConnecting(Object var1, Object var2);

   boolean equals(@Nullable Object var1);

   int hashCode();
}

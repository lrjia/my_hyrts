package com.google.common.graph;

import com.google.common.annotations.Beta;
import java.util.Set;
import javax.annotation.Nullable;

@Beta
public interface Graph<N> {
   Set<N> nodes();

   Set<EndpointPair<N>> edges();

   boolean isDirected();

   boolean allowsSelfLoops();

   ElementOrder<N> nodeOrder();

   Set<N> adjacentNodes(Object var1);

   Set<N> predecessors(Object var1);

   Set<N> successors(Object var1);

   int degree(Object var1);

   int inDegree(Object var1);

   int outDegree(Object var1);

   boolean equals(@Nullable Object var1);

   int hashCode();
}

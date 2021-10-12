package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
public interface MutableNetwork<N, E> extends Network<N, E> {
   @CanIgnoreReturnValue
   boolean addNode(N var1);

   @CanIgnoreReturnValue
   boolean addEdge(N var1, N var2, E var3);

   @CanIgnoreReturnValue
   boolean removeNode(Object var1);

   @CanIgnoreReturnValue
   boolean removeEdge(Object var1);
}

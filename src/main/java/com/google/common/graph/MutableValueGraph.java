package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
public interface MutableValueGraph<N, V> extends ValueGraph<N, V> {
   @CanIgnoreReturnValue
   boolean addNode(N var1);

   @CanIgnoreReturnValue
   V putEdgeValue(N var1, N var2, V var3);

   @CanIgnoreReturnValue
   boolean removeNode(Object var1);

   @CanIgnoreReturnValue
   V removeEdge(Object var1, Object var2);
}

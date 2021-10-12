package com.google.common.graph;

import com.google.common.annotations.Beta;
import javax.annotation.Nullable;

@Beta
public interface ValueGraph<N, V> extends Graph<N> {
   V edgeValue(Object var1, Object var2);

   V edgeValueOrDefault(Object var1, Object var2, @Nullable V var3);

   boolean equals(@Nullable Object var1);

   int hashCode();
}

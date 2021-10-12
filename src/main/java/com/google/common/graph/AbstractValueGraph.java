package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;

@Beta
public abstract class AbstractValueGraph<N, V> extends AbstractGraph<N> implements ValueGraph<N, V> {
   public V edgeValue(Object nodeU, Object nodeV) {
      V value = this.edgeValueOrDefault(nodeU, nodeV, (Object)null);
      if (value == null) {
         Preconditions.checkArgument(this.nodes().contains(nodeU), "Node %s is not an element of this graph.", nodeU);
         Preconditions.checkArgument(this.nodes().contains(nodeV), "Node %s is not an element of this graph.", nodeV);
         throw new IllegalArgumentException(String.format("Edge connecting %s to %s is not present in this graph.", nodeU, nodeV));
      } else {
         return value;
      }
   }

   public String toString() {
      String propertiesString = String.format("isDirected: %s, allowsSelfLoops: %s", this.isDirected(), this.allowsSelfLoops());
      return String.format("%s, nodes: %s, edges: %s", propertiesString, this.nodes(), this.edgeValueMap());
   }

   private Map<EndpointPair<N>, V> edgeValueMap() {
      Function<EndpointPair<N>, V> edgeToValueFn = new Function<EndpointPair<N>, V>() {
         public V apply(EndpointPair<N> edge) {
            return AbstractValueGraph.this.edgeValue(edge.nodeU(), edge.nodeV());
         }
      };
      return Maps.asMap(this.edges(), edgeToValueFn);
   }
}

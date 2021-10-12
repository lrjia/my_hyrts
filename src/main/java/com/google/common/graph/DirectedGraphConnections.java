package com.google.common.graph;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;

final class DirectedGraphConnections<N, V> implements GraphConnections<N, V> {
   private static final Object PRED = new Object();
   private final Map<N, Object> adjacentNodeValues;
   private int predecessorCount;
   private int successorCount;

   private DirectedGraphConnections(Map<N, Object> adjacentNodeValues, int predecessorCount, int successorCount) {
      this.adjacentNodeValues = (Map)Preconditions.checkNotNull(adjacentNodeValues);
      this.predecessorCount = Graphs.checkNonNegative(predecessorCount);
      this.successorCount = Graphs.checkNonNegative(successorCount);
      Preconditions.checkState(predecessorCount <= adjacentNodeValues.size() && successorCount <= adjacentNodeValues.size());
   }

   static <N, V> DirectedGraphConnections<N, V> of() {
      int initialCapacity = 4;
      return new DirectedGraphConnections(new HashMap(initialCapacity, 1.0F), 0, 0);
   }

   static <N, V> DirectedGraphConnections<N, V> ofImmutable(Set<N> predecessors, Map<N, V> successorValues) {
      Map<N, Object> adjacentNodeValues = new HashMap();
      adjacentNodeValues.putAll(successorValues);
      Iterator i$ = predecessors.iterator();

      while(i$.hasNext()) {
         N predecessor = i$.next();
         Object value = adjacentNodeValues.put(predecessor, PRED);
         if (value != null) {
            adjacentNodeValues.put(predecessor, new DirectedGraphConnections.PredAndSucc(value));
         }
      }

      return new DirectedGraphConnections(ImmutableMap.copyOf((Map)adjacentNodeValues), predecessors.size(), successorValues.size());
   }

   public Set<N> adjacentNodes() {
      return Collections.unmodifiableSet(this.adjacentNodeValues.keySet());
   }

   public Set<N> predecessors() {
      return new AbstractSet<N>() {
         public UnmodifiableIterator<N> iterator() {
            final Iterator<Entry<N, Object>> entries = DirectedGraphConnections.this.adjacentNodeValues.entrySet().iterator();
            return new AbstractIterator<N>() {
               protected N computeNext() {
                  while(true) {
                     if (entries.hasNext()) {
                        Entry<N, Object> entry = (Entry)entries.next();
                        if (!DirectedGraphConnections.isPredecessor(entry.getValue())) {
                           continue;
                        }

                        return entry.getKey();
                     }

                     return this.endOfData();
                  }
               }
            };
         }

         public int size() {
            return DirectedGraphConnections.this.predecessorCount;
         }

         public boolean contains(@Nullable Object obj) {
            return DirectedGraphConnections.isPredecessor(DirectedGraphConnections.this.adjacentNodeValues.get(obj));
         }
      };
   }

   public Set<N> successors() {
      return new AbstractSet<N>() {
         public UnmodifiableIterator<N> iterator() {
            final Iterator<Entry<N, Object>> entries = DirectedGraphConnections.this.adjacentNodeValues.entrySet().iterator();
            return new AbstractIterator<N>() {
               protected N computeNext() {
                  while(true) {
                     if (entries.hasNext()) {
                        Entry<N, Object> entry = (Entry)entries.next();
                        if (!DirectedGraphConnections.isSuccessor(entry.getValue())) {
                           continue;
                        }

                        return entry.getKey();
                     }

                     return this.endOfData();
                  }
               }
            };
         }

         public int size() {
            return DirectedGraphConnections.this.successorCount;
         }

         public boolean contains(@Nullable Object obj) {
            return DirectedGraphConnections.isSuccessor(DirectedGraphConnections.this.adjacentNodeValues.get(obj));
         }
      };
   }

   public V value(Object node) {
      Object value = this.adjacentNodeValues.get(node);
      if (value == PRED) {
         return null;
      } else {
         return value instanceof DirectedGraphConnections.PredAndSucc ? ((DirectedGraphConnections.PredAndSucc)value).successorValue : value;
      }
   }

   public void removePredecessor(Object node) {
      Object previousValue = this.adjacentNodeValues.get(node);
      if (previousValue == PRED) {
         this.adjacentNodeValues.remove(node);
         Graphs.checkNonNegative(--this.predecessorCount);
      } else if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
         this.adjacentNodeValues.put(node, ((DirectedGraphConnections.PredAndSucc)previousValue).successorValue);
         Graphs.checkNonNegative(--this.predecessorCount);
      }

   }

   public V removeSuccessor(Object node) {
      Object previousValue = this.adjacentNodeValues.get(node);
      if (previousValue != null && previousValue != PRED) {
         if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
            this.adjacentNodeValues.put(node, PRED);
            Graphs.checkNonNegative(--this.successorCount);
            return ((DirectedGraphConnections.PredAndSucc)previousValue).successorValue;
         } else {
            this.adjacentNodeValues.remove(node);
            Graphs.checkNonNegative(--this.successorCount);
            return previousValue;
         }
      } else {
         return null;
      }
   }

   public void addPredecessor(N node, V unused) {
      Object previousValue = this.adjacentNodeValues.put(node, PRED);
      if (previousValue == null) {
         Graphs.checkPositive(++this.predecessorCount);
      } else if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
         this.adjacentNodeValues.put(node, previousValue);
      } else if (previousValue != PRED) {
         this.adjacentNodeValues.put(node, new DirectedGraphConnections.PredAndSucc(previousValue));
         Graphs.checkPositive(++this.predecessorCount);
      }

   }

   public V addSuccessor(N node, V value) {
      Object previousValue = this.adjacentNodeValues.put(node, value);
      if (previousValue == null) {
         Graphs.checkPositive(++this.successorCount);
         return null;
      } else if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
         this.adjacentNodeValues.put(node, new DirectedGraphConnections.PredAndSucc(value));
         return ((DirectedGraphConnections.PredAndSucc)previousValue).successorValue;
      } else if (previousValue == PRED) {
         this.adjacentNodeValues.put(node, new DirectedGraphConnections.PredAndSucc(value));
         Graphs.checkPositive(++this.successorCount);
         return null;
      } else {
         return previousValue;
      }
   }

   private static boolean isPredecessor(@Nullable Object value) {
      return value == PRED || value instanceof DirectedGraphConnections.PredAndSucc;
   }

   private static boolean isSuccessor(@Nullable Object value) {
      return value != PRED && value != null;
   }

   private static final class PredAndSucc {
      private final Object successorValue;

      PredAndSucc(Object successorValue) {
         this.successorValue = successorValue;
      }
   }
}

package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import javax.annotation.Nullable;

@Beta
public abstract class EndpointPair<N> implements Iterable<N> {
   private final N nodeU;
   private final N nodeV;

   private EndpointPair(N nodeU, N nodeV) {
      this.nodeU = Preconditions.checkNotNull(nodeU);
      this.nodeV = Preconditions.checkNotNull(nodeV);
   }

   public static <N> EndpointPair<N> ordered(N source, N target) {
      return new EndpointPair.Ordered(source, target);
   }

   public static <N> EndpointPair<N> unordered(N nodeU, N nodeV) {
      return new EndpointPair.Unordered(nodeV, nodeU);
   }

   static <N> EndpointPair<N> of(Graph<?> graph, N nodeU, N nodeV) {
      return graph.isDirected() ? ordered(nodeU, nodeV) : unordered(nodeU, nodeV);
   }

   static <N> EndpointPair<N> of(Network<?, ?> network, N nodeU, N nodeV) {
      return network.isDirected() ? ordered(nodeU, nodeV) : unordered(nodeU, nodeV);
   }

   public abstract N source();

   public abstract N target();

   public final N nodeU() {
      return this.nodeU;
   }

   public final N nodeV() {
      return this.nodeV;
   }

   public final N adjacentNode(Object node) {
      if (node.equals(this.nodeU)) {
         return this.nodeV;
      } else if (node.equals(this.nodeV)) {
         return this.nodeU;
      } else {
         throw new IllegalArgumentException(String.format("EndpointPair %s does not contain node %s", this, node));
      }
   }

   public abstract boolean isOrdered();

   public final UnmodifiableIterator<N> iterator() {
      return Iterators.forArray(this.nodeU, this.nodeV);
   }

   public abstract boolean equals(@Nullable Object var1);

   public abstract int hashCode();

   // $FF: synthetic method
   EndpointPair(Object x0, Object x1, Object x2) {
      this(x0, x1);
   }

   private static final class Unordered<N> extends EndpointPair<N> {
      private Unordered(N nodeU, N nodeV) {
         super(nodeU, nodeV, null);
      }

      public N source() {
         throw new UnsupportedOperationException("Cannot call source()/target() on a EndpointPair from an undirected graph. Consider calling adjacentNode(node) if you already have a node, or nodeU()/nodeV() if you don't.");
      }

      public N target() {
         throw new UnsupportedOperationException("Cannot call source()/target() on a EndpointPair from an undirected graph. Consider calling adjacentNode(node) if you already have a node, or nodeU()/nodeV() if you don't.");
      }

      public boolean isOrdered() {
         return false;
      }

      public boolean equals(@Nullable Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof EndpointPair)) {
            return false;
         } else {
            EndpointPair<?> other = (EndpointPair)obj;
            if (this.isOrdered() != other.isOrdered()) {
               return false;
            } else if (this.nodeU().equals(other.nodeU())) {
               return this.nodeV().equals(other.nodeV());
            } else {
               return this.nodeU().equals(other.nodeV()) && this.nodeV().equals(other.nodeU());
            }
         }
      }

      public int hashCode() {
         return this.nodeU().hashCode() + this.nodeV().hashCode();
      }

      public String toString() {
         return String.format("[%s, %s]", this.nodeU(), this.nodeV());
      }

      // $FF: synthetic method
      Unordered(Object x0, Object x1, Object x2) {
         this(x0, x1);
      }
   }

   private static final class Ordered<N> extends EndpointPair<N> {
      private Ordered(N source, N target) {
         super(source, target, null);
      }

      public N source() {
         return this.nodeU();
      }

      public N target() {
         return this.nodeV();
      }

      public boolean isOrdered() {
         return true;
      }

      public boolean equals(@Nullable Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof EndpointPair)) {
            return false;
         } else {
            EndpointPair<?> other = (EndpointPair)obj;
            if (this.isOrdered() != other.isOrdered()) {
               return false;
            } else {
               return this.source().equals(other.source()) && this.target().equals(other.target());
            }
         }
      }

      public int hashCode() {
         return Objects.hashCode(this.source(), this.target());
      }

      public String toString() {
         return String.format("<%s -> %s>", this.source(), this.target());
      }

      // $FF: synthetic method
      Ordered(Object x0, Object x1, Object x2) {
         this(x0, x1);
      }
   }
}

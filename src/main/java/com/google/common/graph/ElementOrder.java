package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.util.Comparator;
import java.util.Map;
import javax.annotation.Nullable;

@Beta
public final class ElementOrder<T> {
   private final ElementOrder.Type type;
   @Nullable
   private final Comparator<T> comparator;

   private ElementOrder(ElementOrder.Type type, @Nullable Comparator<T> comparator) {
      this.type = (ElementOrder.Type)Preconditions.checkNotNull(type);
      this.comparator = comparator;
      Preconditions.checkState(type == ElementOrder.Type.SORTED == (comparator != null));
   }

   public static <S> ElementOrder<S> unordered() {
      return new ElementOrder(ElementOrder.Type.UNORDERED, (Comparator)null);
   }

   public static <S> ElementOrder<S> insertion() {
      return new ElementOrder(ElementOrder.Type.INSERTION, (Comparator)null);
   }

   public static <S extends Comparable<? super S>> ElementOrder<S> natural() {
      return new ElementOrder(ElementOrder.Type.SORTED, Ordering.natural());
   }

   public static <S> ElementOrder<S> sorted(Comparator<S> comparator) {
      return new ElementOrder(ElementOrder.Type.SORTED, comparator);
   }

   public ElementOrder.Type type() {
      return this.type;
   }

   public Comparator<T> comparator() {
      if (this.comparator != null) {
         return this.comparator;
      } else {
         throw new UnsupportedOperationException("This ordering does not define a comparator.");
      }
   }

   public boolean equals(@Nullable Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof ElementOrder)) {
         return false;
      } else {
         ElementOrder<?> other = (ElementOrder)obj;
         return this.type == other.type && Objects.equal(this.comparator, other.comparator);
      }
   }

   public int hashCode() {
      return Objects.hashCode(this.type, this.comparator);
   }

   public String toString() {
      MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper((Object)this).add("type", this.type);
      if (this.comparator != null) {
         helper.add("comparator", this.comparator);
      }

      return helper.toString();
   }

   <K extends T, V> Map<K, V> createMap(int expectedSize) {
      switch(this.type) {
      case UNORDERED:
         return Maps.newHashMapWithExpectedSize(expectedSize);
      case INSERTION:
         return Maps.newLinkedHashMapWithExpectedSize(expectedSize);
      case SORTED:
         return Maps.newTreeMap(this.comparator());
      default:
         throw new AssertionError();
      }
   }

   <T1 extends T> ElementOrder<T1> cast() {
      return this;
   }

   public static enum Type {
      UNORDERED,
      INSERTION,
      SORTED;
   }
}

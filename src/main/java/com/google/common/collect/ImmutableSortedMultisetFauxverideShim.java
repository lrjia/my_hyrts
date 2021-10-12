package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;

@GwtIncompatible
abstract class ImmutableSortedMultisetFauxverideShim<E> extends ImmutableMultiset<E> {
   /** @deprecated */
   @Deprecated
   public static <E> ImmutableSortedMultiset.Builder<E> builder() {
      throw new UnsupportedOperationException();
   }

   /** @deprecated */
   @Deprecated
   public static <E> ImmutableSortedMultiset<E> of(E element) {
      throw new UnsupportedOperationException();
   }

   /** @deprecated */
   @Deprecated
   public static <E> ImmutableSortedMultiset<E> of(E e1, E e2) {
      throw new UnsupportedOperationException();
   }

   /** @deprecated */
   @Deprecated
   public static <E> ImmutableSortedMultiset<E> of(E e1, E e2, E e3) {
      throw new UnsupportedOperationException();
   }

   /** @deprecated */
   @Deprecated
   public static <E> ImmutableSortedMultiset<E> of(E e1, E e2, E e3, E e4) {
      throw new UnsupportedOperationException();
   }

   /** @deprecated */
   @Deprecated
   public static <E> ImmutableSortedMultiset<E> of(E e1, E e2, E e3, E e4, E e5) {
      throw new UnsupportedOperationException();
   }

   /** @deprecated */
   @Deprecated
   public static <E> ImmutableSortedMultiset<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E... remaining) {
      throw new UnsupportedOperationException();
   }

   /** @deprecated */
   @Deprecated
   public static <E> ImmutableSortedMultiset<E> copyOf(E[] elements) {
      throw new UnsupportedOperationException();
   }
}

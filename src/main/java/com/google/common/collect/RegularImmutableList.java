package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible(
   serializable = true,
   emulated = true
)
class RegularImmutableList<E> extends ImmutableList<E> {
   static final ImmutableList<Object> EMPTY;
   private final transient Object[] array;

   RegularImmutableList(Object[] array) {
      this.array = array;
   }

   public int size() {
      return this.array.length;
   }

   boolean isPartialView() {
      return false;
   }

   int copyIntoArray(Object[] dst, int dstOff) {
      System.arraycopy(this.array, 0, dst, dstOff, this.array.length);
      return dstOff + this.array.length;
   }

   public E get(int index) {
      return this.array[index];
   }

   public UnmodifiableListIterator<E> listIterator(int index) {
      return Iterators.forArray(this.array, 0, this.array.length, index);
   }

   static {
      EMPTY = new RegularImmutableList(ObjectArrays.EMPTY_ARRAY);
   }
}

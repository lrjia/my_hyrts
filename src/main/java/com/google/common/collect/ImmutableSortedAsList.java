package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.util.Comparator;
import javax.annotation.Nullable;

@GwtCompatible(
   emulated = true
)
final class ImmutableSortedAsList<E> extends RegularImmutableAsList<E> implements SortedIterable<E> {
   ImmutableSortedAsList(ImmutableSortedSet<E> backingSet, ImmutableList<E> backingList) {
      super(backingSet, (ImmutableList)backingList);
   }

   ImmutableSortedSet<E> delegateCollection() {
      return (ImmutableSortedSet)super.delegateCollection();
   }

   public Comparator<? super E> comparator() {
      return this.delegateCollection().comparator();
   }

   @GwtIncompatible
   public int indexOf(@Nullable Object target) {
      int index = this.delegateCollection().indexOf(target);
      return index >= 0 && this.get(index).equals(target) ? index : -1;
   }

   @GwtIncompatible
   public int lastIndexOf(@Nullable Object target) {
      return this.indexOf(target);
   }

   public boolean contains(Object target) {
      return this.indexOf(target) >= 0;
   }

   @GwtIncompatible
   ImmutableList<E> subListUnchecked(int fromIndex, int toIndex) {
      ImmutableList<E> parentSubList = super.subListUnchecked(fromIndex, toIndex);
      return (new RegularImmutableSortedSet(parentSubList, this.comparator())).asList();
   }
}

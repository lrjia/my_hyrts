package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import javax.annotation.Nullable;

@GwtCompatible(
   serializable = true,
   emulated = true
)
public abstract class ImmutableSortedSet<E> extends ImmutableSortedSetFauxverideShim<E> implements NavigableSet<E>, SortedIterable<E> {
   final transient Comparator<? super E> comparator;
   @LazyInit
   @GwtIncompatible
   transient ImmutableSortedSet<E> descendingSet;

   static <E> RegularImmutableSortedSet<E> emptySet(Comparator<? super E> comparator) {
      return Ordering.natural().equals(comparator) ? RegularImmutableSortedSet.NATURAL_EMPTY_SET : new RegularImmutableSortedSet(ImmutableList.of(), comparator);
   }

   public static <E> ImmutableSortedSet<E> of() {
      return RegularImmutableSortedSet.NATURAL_EMPTY_SET;
   }

   public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(E element) {
      return new RegularImmutableSortedSet(ImmutableList.of(element), Ordering.natural());
   }

   public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(E e1, E e2) {
      return construct(Ordering.natural(), 2, e1, e2);
   }

   public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(E e1, E e2, E e3) {
      return construct(Ordering.natural(), 3, e1, e2, e3);
   }

   public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(E e1, E e2, E e3, E e4) {
      return construct(Ordering.natural(), 4, e1, e2, e3, e4);
   }

   public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(E e1, E e2, E e3, E e4, E e5) {
      return construct(Ordering.natural(), 5, e1, e2, e3, e4, e5);
   }

   public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E... remaining) {
      Comparable[] contents = new Comparable[6 + remaining.length];
      contents[0] = e1;
      contents[1] = e2;
      contents[2] = e3;
      contents[3] = e4;
      contents[4] = e5;
      contents[5] = e6;
      System.arraycopy(remaining, 0, contents, 6, remaining.length);
      return construct(Ordering.natural(), contents.length, (Comparable[])contents);
   }

   public static <E extends Comparable<? super E>> ImmutableSortedSet<E> copyOf(E[] elements) {
      return construct(Ordering.natural(), elements.length, (Object[])elements.clone());
   }

   public static <E> ImmutableSortedSet<E> copyOf(Iterable<? extends E> elements) {
      Ordering<E> naturalOrder = Ordering.natural();
      return copyOf(naturalOrder, (Iterable)elements);
   }

   public static <E> ImmutableSortedSet<E> copyOf(Collection<? extends E> elements) {
      Ordering<E> naturalOrder = Ordering.natural();
      return copyOf(naturalOrder, (Collection)elements);
   }

   public static <E> ImmutableSortedSet<E> copyOf(Iterator<? extends E> elements) {
      Ordering<E> naturalOrder = Ordering.natural();
      return copyOf(naturalOrder, (Iterator)elements);
   }

   public static <E> ImmutableSortedSet<E> copyOf(Comparator<? super E> comparator, Iterator<? extends E> elements) {
      return (new ImmutableSortedSet.Builder(comparator)).addAll(elements).build();
   }

   public static <E> ImmutableSortedSet<E> copyOf(Comparator<? super E> comparator, Iterable<? extends E> elements) {
      Preconditions.checkNotNull(comparator);
      boolean hasSameComparator = SortedIterables.hasSameComparator(comparator, elements);
      if (hasSameComparator && elements instanceof ImmutableSortedSet) {
         ImmutableSortedSet<E> original = (ImmutableSortedSet)elements;
         if (!original.isPartialView()) {
            return original;
         }
      }

      E[] array = (Object[])Iterables.toArray(elements);
      return construct(comparator, array.length, array);
   }

   public static <E> ImmutableSortedSet<E> copyOf(Comparator<? super E> comparator, Collection<? extends E> elements) {
      return copyOf(comparator, (Iterable)elements);
   }

   public static <E> ImmutableSortedSet<E> copyOfSorted(SortedSet<E> sortedSet) {
      Comparator<? super E> comparator = SortedIterables.comparator(sortedSet);
      ImmutableList<E> list = ImmutableList.copyOf((Collection)sortedSet);
      return list.isEmpty() ? emptySet(comparator) : new RegularImmutableSortedSet(list, comparator);
   }

   static <E> ImmutableSortedSet<E> construct(Comparator<? super E> comparator, int n, E... contents) {
      if (n == 0) {
         return emptySet(comparator);
      } else {
         ObjectArrays.checkElementsNotNull(contents, n);
         Arrays.sort(contents, 0, n, comparator);
         int uniques = 1;

         for(int i = 1; i < n; ++i) {
            E cur = contents[i];
            E prev = contents[uniques - 1];
            if (comparator.compare(cur, prev) != 0) {
               contents[uniques++] = cur;
            }
         }

         Arrays.fill(contents, uniques, n, (Object)null);
         return new RegularImmutableSortedSet(ImmutableList.asImmutableList(contents, uniques), comparator);
      }
   }

   public static <E> ImmutableSortedSet.Builder<E> orderedBy(Comparator<E> comparator) {
      return new ImmutableSortedSet.Builder(comparator);
   }

   public static <E extends Comparable<?>> ImmutableSortedSet.Builder<E> reverseOrder() {
      return new ImmutableSortedSet.Builder(Ordering.natural().reverse());
   }

   public static <E extends Comparable<?>> ImmutableSortedSet.Builder<E> naturalOrder() {
      return new ImmutableSortedSet.Builder(Ordering.natural());
   }

   int unsafeCompare(Object a, Object b) {
      return unsafeCompare(this.comparator, a, b);
   }

   static int unsafeCompare(Comparator<?> comparator, Object a, Object b) {
      return comparator.compare(a, b);
   }

   ImmutableSortedSet(Comparator<? super E> comparator) {
      this.comparator = comparator;
   }

   public Comparator<? super E> comparator() {
      return this.comparator;
   }

   public abstract UnmodifiableIterator<E> iterator();

   public ImmutableSortedSet<E> headSet(E toElement) {
      return this.headSet(toElement, false);
   }

   @GwtIncompatible
   public ImmutableSortedSet<E> headSet(E toElement, boolean inclusive) {
      return this.headSetImpl(Preconditions.checkNotNull(toElement), inclusive);
   }

   public ImmutableSortedSet<E> subSet(E fromElement, E toElement) {
      return this.subSet(fromElement, true, toElement, false);
   }

   @GwtIncompatible
   public ImmutableSortedSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
      Preconditions.checkNotNull(fromElement);
      Preconditions.checkNotNull(toElement);
      Preconditions.checkArgument(this.comparator.compare(fromElement, toElement) <= 0);
      return this.subSetImpl(fromElement, fromInclusive, toElement, toInclusive);
   }

   public ImmutableSortedSet<E> tailSet(E fromElement) {
      return this.tailSet(fromElement, true);
   }

   @GwtIncompatible
   public ImmutableSortedSet<E> tailSet(E fromElement, boolean inclusive) {
      return this.tailSetImpl(Preconditions.checkNotNull(fromElement), inclusive);
   }

   abstract ImmutableSortedSet<E> headSetImpl(E var1, boolean var2);

   abstract ImmutableSortedSet<E> subSetImpl(E var1, boolean var2, E var3, boolean var4);

   abstract ImmutableSortedSet<E> tailSetImpl(E var1, boolean var2);

   @GwtIncompatible
   public E lower(E e) {
      return Iterators.getNext(this.headSet(e, false).descendingIterator(), (Object)null);
   }

   @GwtIncompatible
   public E floor(E e) {
      return Iterators.getNext(this.headSet(e, true).descendingIterator(), (Object)null);
   }

   @GwtIncompatible
   public E ceiling(E e) {
      return Iterables.getFirst(this.tailSet(e, true), (Object)null);
   }

   @GwtIncompatible
   public E higher(E e) {
      return Iterables.getFirst(this.tailSet(e, false), (Object)null);
   }

   public E first() {
      return this.iterator().next();
   }

   public E last() {
      return this.descendingIterator().next();
   }

   /** @deprecated */
   @Deprecated
   @CanIgnoreReturnValue
   @GwtIncompatible
   public final E pollFirst() {
      throw new UnsupportedOperationException();
   }

   /** @deprecated */
   @Deprecated
   @CanIgnoreReturnValue
   @GwtIncompatible
   public final E pollLast() {
      throw new UnsupportedOperationException();
   }

   @GwtIncompatible
   public ImmutableSortedSet<E> descendingSet() {
      ImmutableSortedSet<E> result = this.descendingSet;
      if (result == null) {
         result = this.descendingSet = this.createDescendingSet();
         result.descendingSet = this;
      }

      return result;
   }

   @GwtIncompatible
   ImmutableSortedSet<E> createDescendingSet() {
      return new DescendingImmutableSortedSet(this);
   }

   @GwtIncompatible
   public abstract UnmodifiableIterator<E> descendingIterator();

   abstract int indexOf(@Nullable Object var1);

   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use SerializedForm");
   }

   Object writeReplace() {
      return new ImmutableSortedSet.SerializedForm(this.comparator, this.toArray());
   }

   private static class SerializedForm<E> implements Serializable {
      final Comparator<? super E> comparator;
      final Object[] elements;
      private static final long serialVersionUID = 0L;

      public SerializedForm(Comparator<? super E> comparator, Object[] elements) {
         this.comparator = comparator;
         this.elements = elements;
      }

      Object readResolve() {
         return (new ImmutableSortedSet.Builder(this.comparator)).add((Object[])this.elements).build();
      }
   }

   public static final class Builder<E> extends ImmutableSet.Builder<E> {
      private final Comparator<? super E> comparator;

      public Builder(Comparator<? super E> comparator) {
         this.comparator = (Comparator)Preconditions.checkNotNull(comparator);
      }

      @CanIgnoreReturnValue
      public ImmutableSortedSet.Builder<E> add(E element) {
         super.add(element);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSortedSet.Builder<E> add(E... elements) {
         super.add(elements);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSortedSet.Builder<E> addAll(Iterable<? extends E> elements) {
         super.addAll(elements);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSortedSet.Builder<E> addAll(Iterator<? extends E> elements) {
         super.addAll(elements);
         return this;
      }

      public ImmutableSortedSet<E> build() {
         E[] contentsArray = (Object[])this.contents;
         ImmutableSortedSet<E> result = ImmutableSortedSet.construct(this.comparator, this.size, contentsArray);
         this.size = result.size();
         return result;
      }
   }
}

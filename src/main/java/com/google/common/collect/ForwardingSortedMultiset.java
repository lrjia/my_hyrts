package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;

@Beta
@GwtCompatible(
   emulated = true
)
public abstract class ForwardingSortedMultiset<E> extends ForwardingMultiset<E> implements SortedMultiset<E> {
   protected ForwardingSortedMultiset() {
   }

   protected abstract SortedMultiset<E> delegate();

   public NavigableSet<E> elementSet() {
      return (NavigableSet)super.elementSet();
   }

   public Comparator<? super E> comparator() {
      return this.delegate().comparator();
   }

   public SortedMultiset<E> descendingMultiset() {
      return this.delegate().descendingMultiset();
   }

   public Multiset.Entry<E> firstEntry() {
      return this.delegate().firstEntry();
   }

   protected Multiset.Entry<E> standardFirstEntry() {
      Iterator<Multiset.Entry<E>> entryIterator = this.entrySet().iterator();
      if (!entryIterator.hasNext()) {
         return null;
      } else {
         Multiset.Entry<E> entry = (Multiset.Entry)entryIterator.next();
         return Multisets.immutableEntry(entry.getElement(), entry.getCount());
      }
   }

   public Multiset.Entry<E> lastEntry() {
      return this.delegate().lastEntry();
   }

   protected Multiset.Entry<E> standardLastEntry() {
      Iterator<Multiset.Entry<E>> entryIterator = this.descendingMultiset().entrySet().iterator();
      if (!entryIterator.hasNext()) {
         return null;
      } else {
         Multiset.Entry<E> entry = (Multiset.Entry)entryIterator.next();
         return Multisets.immutableEntry(entry.getElement(), entry.getCount());
      }
   }

   public Multiset.Entry<E> pollFirstEntry() {
      return this.delegate().pollFirstEntry();
   }

   protected Multiset.Entry<E> standardPollFirstEntry() {
      Iterator<Multiset.Entry<E>> entryIterator = this.entrySet().iterator();
      if (!entryIterator.hasNext()) {
         return null;
      } else {
         Multiset.Entry<E> entry = (Multiset.Entry)entryIterator.next();
         entry = Multisets.immutableEntry(entry.getElement(), entry.getCount());
         entryIterator.remove();
         return entry;
      }
   }

   public Multiset.Entry<E> pollLastEntry() {
      return this.delegate().pollLastEntry();
   }

   protected Multiset.Entry<E> standardPollLastEntry() {
      Iterator<Multiset.Entry<E>> entryIterator = this.descendingMultiset().entrySet().iterator();
      if (!entryIterator.hasNext()) {
         return null;
      } else {
         Multiset.Entry<E> entry = (Multiset.Entry)entryIterator.next();
         entry = Multisets.immutableEntry(entry.getElement(), entry.getCount());
         entryIterator.remove();
         return entry;
      }
   }

   public SortedMultiset<E> headMultiset(E upperBound, BoundType boundType) {
      return this.delegate().headMultiset(upperBound, boundType);
   }

   public SortedMultiset<E> subMultiset(E lowerBound, BoundType lowerBoundType, E upperBound, BoundType upperBoundType) {
      return this.delegate().subMultiset(lowerBound, lowerBoundType, upperBound, upperBoundType);
   }

   protected SortedMultiset<E> standardSubMultiset(E lowerBound, BoundType lowerBoundType, E upperBound, BoundType upperBoundType) {
      return this.tailMultiset(lowerBound, lowerBoundType).headMultiset(upperBound, upperBoundType);
   }

   public SortedMultiset<E> tailMultiset(E lowerBound, BoundType boundType) {
      return this.delegate().tailMultiset(lowerBound, boundType);
   }

   protected abstract class StandardDescendingMultiset extends DescendingMultiset<E> {
      public StandardDescendingMultiset() {
      }

      SortedMultiset<E> forwardMultiset() {
         return ForwardingSortedMultiset.this;
      }
   }

   protected class StandardElementSet extends SortedMultisets.NavigableElementSet<E> {
      public StandardElementSet() {
         super(ForwardingSortedMultiset.this);
      }
   }
}

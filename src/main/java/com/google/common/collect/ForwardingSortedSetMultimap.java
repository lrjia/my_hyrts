package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Comparator;
import java.util.SortedSet;
import javax.annotation.Nullable;

@GwtCompatible
public abstract class ForwardingSortedSetMultimap<K, V> extends ForwardingSetMultimap<K, V> implements SortedSetMultimap<K, V> {
   protected ForwardingSortedSetMultimap() {
   }

   protected abstract SortedSetMultimap<K, V> delegate();

   public SortedSet<V> get(@Nullable K key) {
      return this.delegate().get(key);
   }

   public SortedSet<V> removeAll(@Nullable Object key) {
      return this.delegate().removeAll(key);
   }

   public SortedSet<V> replaceValues(K key, Iterable<? extends V> values) {
      return this.delegate().replaceValues(key, values);
   }

   public Comparator<? super V> valueComparator() {
      return this.delegate().valueComparator();
   }
}

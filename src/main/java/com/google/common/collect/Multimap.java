package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;

@GwtCompatible
public interface Multimap<K, V> {
   int size();

   boolean isEmpty();

   boolean containsKey(@Nullable Object var1);

   boolean containsValue(@Nullable Object var1);

   boolean containsEntry(@Nullable Object var1, @Nullable Object var2);

   @CanIgnoreReturnValue
   boolean put(@Nullable K var1, @Nullable V var2);

   @CanIgnoreReturnValue
   boolean remove(@Nullable Object var1, @Nullable Object var2);

   @CanIgnoreReturnValue
   boolean putAll(@Nullable K var1, Iterable<? extends V> var2);

   @CanIgnoreReturnValue
   boolean putAll(Multimap<? extends K, ? extends V> var1);

   @CanIgnoreReturnValue
   Collection<V> replaceValues(@Nullable K var1, Iterable<? extends V> var2);

   @CanIgnoreReturnValue
   Collection<V> removeAll(@Nullable Object var1);

   void clear();

   Collection<V> get(@Nullable K var1);

   Set<K> keySet();

   Multiset<K> keys();

   Collection<V> values();

   Collection<Entry<K, V>> entries();

   Map<K, Collection<V>> asMap();

   boolean equals(@Nullable Object var1);

   int hashCode();
}

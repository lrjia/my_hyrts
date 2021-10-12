package com.google.common.reflect;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingMapEntry;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;

@Beta
public final class MutableTypeToInstanceMap<B> extends ForwardingMap<TypeToken<? extends B>, B> implements TypeToInstanceMap<B> {
   private final Map<TypeToken<? extends B>, B> backingMap = Maps.newHashMap();

   @Nullable
   public <T extends B> T getInstance(Class<T> type) {
      return this.trustedGet(TypeToken.of(type));
   }

   @Nullable
   @CanIgnoreReturnValue
   public <T extends B> T putInstance(Class<T> type, @Nullable T value) {
      return this.trustedPut(TypeToken.of(type), value);
   }

   @Nullable
   public <T extends B> T getInstance(TypeToken<T> type) {
      return this.trustedGet(type.rejectTypeVariables());
   }

   @Nullable
   @CanIgnoreReturnValue
   public <T extends B> T putInstance(TypeToken<T> type, @Nullable T value) {
      return this.trustedPut(type.rejectTypeVariables(), value);
   }

   /** @deprecated */
   @Deprecated
   @CanIgnoreReturnValue
   public B put(TypeToken<? extends B> key, B value) {
      throw new UnsupportedOperationException("Please use putInstance() instead.");
   }

   /** @deprecated */
   @Deprecated
   public void putAll(Map<? extends TypeToken<? extends B>, ? extends B> map) {
      throw new UnsupportedOperationException("Please use putInstance() instead.");
   }

   public Set<Entry<TypeToken<? extends B>, B>> entrySet() {
      return MutableTypeToInstanceMap.UnmodifiableEntry.transformEntries(super.entrySet());
   }

   protected Map<TypeToken<? extends B>, B> delegate() {
      return this.backingMap;
   }

   @Nullable
   private <T extends B> T trustedPut(TypeToken<T> type, @Nullable T value) {
      return this.backingMap.put(type, value);
   }

   @Nullable
   private <T extends B> T trustedGet(TypeToken<T> type) {
      return this.backingMap.get(type);
   }

   private static final class UnmodifiableEntry<K, V> extends ForwardingMapEntry<K, V> {
      private final Entry<K, V> delegate;

      static <K, V> Set<Entry<K, V>> transformEntries(final Set<Entry<K, V>> entries) {
         return new ForwardingSet<Entry<K, V>>() {
            protected Set<Entry<K, V>> delegate() {
               return entries;
            }

            public Iterator<Entry<K, V>> iterator() {
               return MutableTypeToInstanceMap.UnmodifiableEntry.transformEntries(super.iterator());
            }

            public Object[] toArray() {
               return this.standardToArray();
            }

            public <T> T[] toArray(T[] array) {
               return this.standardToArray(array);
            }
         };
      }

      private static <K, V> Iterator<Entry<K, V>> transformEntries(Iterator<Entry<K, V>> entries) {
         return Iterators.transform(entries, new Function<Entry<K, V>, Entry<K, V>>() {
            public Entry<K, V> apply(Entry<K, V> entry) {
               return new MutableTypeToInstanceMap.UnmodifiableEntry(entry);
            }
         });
      }

      private UnmodifiableEntry(Entry<K, V> delegate) {
         this.delegate = (Entry)Preconditions.checkNotNull(delegate);
      }

      protected Entry<K, V> delegate() {
         return this.delegate;
      }

      public V setValue(V value) {
         throw new UnsupportedOperationException();
      }

      // $FF: synthetic method
      UnmodifiableEntry(Entry x0, Object x1) {
         this(x0);
      }
   }
}

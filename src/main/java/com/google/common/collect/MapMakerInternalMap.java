package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.j2objc.annotations.Weak;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

@GwtIncompatible
class MapMakerInternalMap<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>, S extends MapMakerInternalMap.Segment<K, V, E, S>> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Serializable {
   static final int MAXIMUM_CAPACITY = 1073741824;
   static final int MAX_SEGMENTS = 65536;
   static final int CONTAINS_VALUE_RETRIES = 3;
   static final int DRAIN_THRESHOLD = 63;
   static final int DRAIN_MAX = 16;
   static final long CLEANUP_EXECUTOR_DELAY_SECS = 60L;
   final transient int segmentMask;
   final transient int segmentShift;
   final transient MapMakerInternalMap.Segment<K, V, E, S>[] segments;
   final int concurrencyLevel;
   final Equivalence<Object> keyEquivalence;
   final transient MapMakerInternalMap.InternalEntryHelper<K, V, E, S> entryHelper;
   static final MapMakerInternalMap.WeakValueReference<Object, Object, MapMakerInternalMap.DummyInternalEntry> UNSET_WEAK_VALUE_REFERENCE = new MapMakerInternalMap.WeakValueReference<Object, Object, MapMakerInternalMap.DummyInternalEntry>() {
      public MapMakerInternalMap.DummyInternalEntry getEntry() {
         return null;
      }

      public void clear() {
      }

      public Object get() {
         return null;
      }

      public MapMakerInternalMap.WeakValueReference<Object, Object, MapMakerInternalMap.DummyInternalEntry> copyFor(ReferenceQueue<Object> queue, MapMakerInternalMap.DummyInternalEntry entry) {
         return this;
      }
   };
   transient Set<K> keySet;
   transient Collection<V> values;
   transient Set<Entry<K, V>> entrySet;
   private static final long serialVersionUID = 5L;

   private MapMakerInternalMap(MapMaker builder, MapMakerInternalMap.InternalEntryHelper<K, V, E, S> entryHelper) {
      this.concurrencyLevel = Math.min(builder.getConcurrencyLevel(), 65536);
      this.keyEquivalence = builder.getKeyEquivalence();
      this.entryHelper = entryHelper;
      int initialCapacity = Math.min(builder.getInitialCapacity(), 1073741824);
      int segmentShift = 0;

      int segmentCount;
      for(segmentCount = 1; segmentCount < this.concurrencyLevel; segmentCount <<= 1) {
         ++segmentShift;
      }

      this.segmentShift = 32 - segmentShift;
      this.segmentMask = segmentCount - 1;
      this.segments = this.newSegmentArray(segmentCount);
      int segmentCapacity = initialCapacity / segmentCount;
      if (segmentCapacity * segmentCount < initialCapacity) {
         ++segmentCapacity;
      }

      int segmentSize;
      for(segmentSize = 1; segmentSize < segmentCapacity; segmentSize <<= 1) {
      }

      for(int i = 0; i < this.segments.length; ++i) {
         this.segments[i] = this.createSegment(segmentSize, -1);
      }

   }

   static <K, V> MapMakerInternalMap<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>, ?> create(MapMaker builder) {
      if (builder.getKeyStrength() == MapMakerInternalMap.Strength.STRONG && builder.getValueStrength() == MapMakerInternalMap.Strength.STRONG) {
         return new MapMakerInternalMap(builder, MapMakerInternalMap.StrongKeyStrongValueEntry.Helper.instance());
      } else if (builder.getKeyStrength() == MapMakerInternalMap.Strength.STRONG && builder.getValueStrength() == MapMakerInternalMap.Strength.WEAK) {
         return new MapMakerInternalMap(builder, MapMakerInternalMap.StrongKeyWeakValueEntry.Helper.instance());
      } else if (builder.getKeyStrength() == MapMakerInternalMap.Strength.WEAK && builder.getValueStrength() == MapMakerInternalMap.Strength.STRONG) {
         return new MapMakerInternalMap(builder, MapMakerInternalMap.WeakKeyStrongValueEntry.Helper.instance());
      } else if (builder.getKeyStrength() == MapMakerInternalMap.Strength.WEAK && builder.getValueStrength() == MapMakerInternalMap.Strength.WEAK) {
         return new MapMakerInternalMap(builder, MapMakerInternalMap.WeakKeyWeakValueEntry.Helper.instance());
      } else {
         throw new AssertionError();
      }
   }

   static <K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> MapMakerInternalMap.WeakValueReference<K, V, E> unsetWeakValueReference() {
      return UNSET_WEAK_VALUE_REFERENCE;
   }

   static int rehash(int h) {
      h += h << 15 ^ -12931;
      h ^= h >>> 10;
      h += h << 3;
      h ^= h >>> 6;
      h += (h << 2) + (h << 14);
      return h ^ h >>> 16;
   }

   @VisibleForTesting
   E copyEntry(E original, E newNext) {
      int hash = original.getHash();
      return this.segmentFor(hash).copyEntry(original, newNext);
   }

   int hash(Object key) {
      int h = this.keyEquivalence.hash(key);
      return rehash(h);
   }

   void reclaimValue(MapMakerInternalMap.WeakValueReference<K, V, E> valueReference) {
      E entry = valueReference.getEntry();
      int hash = entry.getHash();
      this.segmentFor(hash).reclaimValue(entry.getKey(), hash, valueReference);
   }

   void reclaimKey(E entry) {
      int hash = entry.getHash();
      this.segmentFor(hash).reclaimKey(entry, hash);
   }

   @VisibleForTesting
   boolean isLiveForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
      return this.segmentFor(entry.getHash()).getLiveValueForTesting(entry) != null;
   }

   MapMakerInternalMap.Segment<K, V, E, S> segmentFor(int hash) {
      return this.segments[hash >>> this.segmentShift & this.segmentMask];
   }

   MapMakerInternalMap.Segment<K, V, E, S> createSegment(int initialCapacity, int maxSegmentSize) {
      return this.entryHelper.newSegment(this, initialCapacity, maxSegmentSize);
   }

   V getLiveValue(E entry) {
      if (entry.getKey() == null) {
         return null;
      } else {
         V value = entry.getValue();
         return value == null ? null : value;
      }
   }

   final MapMakerInternalMap.Segment<K, V, E, S>[] newSegmentArray(int ssize) {
      return new MapMakerInternalMap.Segment[ssize];
   }

   @VisibleForTesting
   MapMakerInternalMap.Strength keyStrength() {
      return this.entryHelper.keyStrength();
   }

   @VisibleForTesting
   MapMakerInternalMap.Strength valueStrength() {
      return this.entryHelper.valueStrength();
   }

   @VisibleForTesting
   Equivalence<Object> valueEquivalence() {
      return this.entryHelper.valueStrength().defaultEquivalence();
   }

   public boolean isEmpty() {
      long sum = 0L;
      MapMakerInternalMap.Segment<K, V, E, S>[] segments = this.segments;

      int i;
      for(i = 0; i < segments.length; ++i) {
         if (segments[i].count != 0) {
            return false;
         }

         sum += (long)segments[i].modCount;
      }

      if (sum != 0L) {
         for(i = 0; i < segments.length; ++i) {
            if (segments[i].count != 0) {
               return false;
            }

            sum -= (long)segments[i].modCount;
         }

         if (sum != 0L) {
            return false;
         }
      }

      return true;
   }

   public int size() {
      MapMakerInternalMap.Segment<K, V, E, S>[] segments = this.segments;
      long sum = 0L;

      for(int i = 0; i < segments.length; ++i) {
         sum += (long)segments[i].count;
      }

      return Ints.saturatedCast(sum);
   }

   public V get(@Nullable Object key) {
      if (key == null) {
         return null;
      } else {
         int hash = this.hash(key);
         return this.segmentFor(hash).get(key, hash);
      }
   }

   E getEntry(@Nullable Object key) {
      if (key == null) {
         return null;
      } else {
         int hash = this.hash(key);
         return this.segmentFor(hash).getEntry(key, hash);
      }
   }

   public boolean containsKey(@Nullable Object key) {
      if (key == null) {
         return false;
      } else {
         int hash = this.hash(key);
         return this.segmentFor(hash).containsKey(key, hash);
      }
   }

   public boolean containsValue(@Nullable Object value) {
      if (value == null) {
         return false;
      } else {
         MapMakerInternalMap.Segment<K, V, E, S>[] segments = this.segments;
         long last = -1L;

         for(int i = 0; i < 3; ++i) {
            long sum = 0L;
            MapMakerInternalMap.Segment[] arr$ = segments;
            int len$ = segments.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               MapMakerInternalMap.Segment<K, V, E, S> segment = arr$[i$];
               int unused = segment.count;
               AtomicReferenceArray<E> table = segment.table;

               for(int j = 0; j < table.length(); ++j) {
                  for(MapMakerInternalMap.InternalEntry e = (MapMakerInternalMap.InternalEntry)table.get(j); e != null; e = e.getNext()) {
                     V v = segment.getLiveValue(e);
                     if (v != null && this.valueEquivalence().equivalent(value, v)) {
                        return true;
                     }
                  }
               }

               sum += (long)segment.modCount;
            }

            if (sum == last) {
               break;
            }

            last = sum;
         }

         return false;
      }
   }

   @CanIgnoreReturnValue
   public V put(K key, V value) {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
      int hash = this.hash(key);
      return this.segmentFor(hash).put(key, hash, value, false);
   }

   @CanIgnoreReturnValue
   public V putIfAbsent(K key, V value) {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
      int hash = this.hash(key);
      return this.segmentFor(hash).put(key, hash, value, true);
   }

   public void putAll(Map<? extends K, ? extends V> m) {
      Iterator i$ = m.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<? extends K, ? extends V> e = (Entry)i$.next();
         this.put(e.getKey(), e.getValue());
      }

   }

   @CanIgnoreReturnValue
   public V remove(@Nullable Object key) {
      if (key == null) {
         return null;
      } else {
         int hash = this.hash(key);
         return this.segmentFor(hash).remove(key, hash);
      }
   }

   @CanIgnoreReturnValue
   public boolean remove(@Nullable Object key, @Nullable Object value) {
      if (key != null && value != null) {
         int hash = this.hash(key);
         return this.segmentFor(hash).remove(key, hash, value);
      } else {
         return false;
      }
   }

   @CanIgnoreReturnValue
   public boolean replace(K key, @Nullable V oldValue, V newValue) {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(newValue);
      if (oldValue == null) {
         return false;
      } else {
         int hash = this.hash(key);
         return this.segmentFor(hash).replace(key, hash, oldValue, newValue);
      }
   }

   @CanIgnoreReturnValue
   public V replace(K key, V value) {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
      int hash = this.hash(key);
      return this.segmentFor(hash).replace(key, hash, value);
   }

   public void clear() {
      MapMakerInternalMap.Segment[] arr$ = this.segments;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MapMakerInternalMap.Segment<K, V, E, S> segment = arr$[i$];
         segment.clear();
      }

   }

   public Set<K> keySet() {
      Set<K> ks = this.keySet;
      return ks != null ? ks : (this.keySet = new MapMakerInternalMap.KeySet());
   }

   public Collection<V> values() {
      Collection<V> vs = this.values;
      return vs != null ? vs : (this.values = new MapMakerInternalMap.Values());
   }

   public Set<Entry<K, V>> entrySet() {
      Set<Entry<K, V>> es = this.entrySet;
      return es != null ? es : (this.entrySet = new MapMakerInternalMap.EntrySet());
   }

   private static <E> ArrayList<E> toArrayList(Collection<E> c) {
      ArrayList<E> result = new ArrayList(c.size());
      Iterators.addAll(result, c.iterator());
      return result;
   }

   Object writeReplace() {
      return new MapMakerInternalMap.SerializationProxy(this.entryHelper.keyStrength(), this.entryHelper.valueStrength(), this.keyEquivalence, this.entryHelper.valueStrength().defaultEquivalence(), this.concurrencyLevel, this);
   }

   private static final class SerializationProxy<K, V> extends MapMakerInternalMap.AbstractSerializationProxy<K, V> {
      private static final long serialVersionUID = 3L;

      SerializationProxy(MapMakerInternalMap.Strength keyStrength, MapMakerInternalMap.Strength valueStrength, Equivalence<Object> keyEquivalence, Equivalence<Object> valueEquivalence, int concurrencyLevel, ConcurrentMap<K, V> delegate) {
         super(keyStrength, valueStrength, keyEquivalence, valueEquivalence, concurrencyLevel, delegate);
      }

      private void writeObject(ObjectOutputStream out) throws IOException {
         out.defaultWriteObject();
         this.writeMapTo(out);
      }

      private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         MapMaker mapMaker = this.readMapMaker(in);
         this.delegate = mapMaker.makeMap();
         this.readEntries(in);
      }

      private Object readResolve() {
         return this.delegate;
      }
   }

   abstract static class AbstractSerializationProxy<K, V> extends ForwardingConcurrentMap<K, V> implements Serializable {
      private static final long serialVersionUID = 3L;
      final MapMakerInternalMap.Strength keyStrength;
      final MapMakerInternalMap.Strength valueStrength;
      final Equivalence<Object> keyEquivalence;
      final Equivalence<Object> valueEquivalence;
      final int concurrencyLevel;
      transient ConcurrentMap<K, V> delegate;

      AbstractSerializationProxy(MapMakerInternalMap.Strength keyStrength, MapMakerInternalMap.Strength valueStrength, Equivalence<Object> keyEquivalence, Equivalence<Object> valueEquivalence, int concurrencyLevel, ConcurrentMap<K, V> delegate) {
         this.keyStrength = keyStrength;
         this.valueStrength = valueStrength;
         this.keyEquivalence = keyEquivalence;
         this.valueEquivalence = valueEquivalence;
         this.concurrencyLevel = concurrencyLevel;
         this.delegate = delegate;
      }

      protected ConcurrentMap<K, V> delegate() {
         return this.delegate;
      }

      void writeMapTo(ObjectOutputStream out) throws IOException {
         out.writeInt(this.delegate.size());
         Iterator i$ = this.delegate.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<K, V> entry = (Entry)i$.next();
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
         }

         out.writeObject((Object)null);
      }

      MapMaker readMapMaker(ObjectInputStream in) throws IOException {
         int size = in.readInt();
         return (new MapMaker()).initialCapacity(size).setKeyStrength(this.keyStrength).setValueStrength(this.valueStrength).keyEquivalence(this.keyEquivalence).concurrencyLevel(this.concurrencyLevel);
      }

      void readEntries(ObjectInputStream in) throws IOException, ClassNotFoundException {
         while(true) {
            K key = in.readObject();
            if (key == null) {
               return;
            }

            V value = in.readObject();
            this.delegate.put(key, value);
         }
      }
   }

   private abstract static class SafeToArraySet<E> extends AbstractSet<E> {
      private SafeToArraySet() {
      }

      public Object[] toArray() {
         return MapMakerInternalMap.toArrayList(this).toArray();
      }

      public <E> E[] toArray(E[] a) {
         return MapMakerInternalMap.toArrayList(this).toArray(a);
      }

      // $FF: synthetic method
      SafeToArraySet(Object x0) {
         this();
      }
   }

   final class EntrySet extends MapMakerInternalMap.SafeToArraySet<Entry<K, V>> {
      EntrySet() {
         super(null);
      }

      public Iterator<Entry<K, V>> iterator() {
         return MapMakerInternalMap.this.new EntryIterator();
      }

      public boolean contains(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> e = (Entry)o;
            Object key = e.getKey();
            if (key == null) {
               return false;
            } else {
               V v = MapMakerInternalMap.this.get(key);
               return v != null && MapMakerInternalMap.this.valueEquivalence().equivalent(e.getValue(), v);
            }
         }
      }

      public boolean remove(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> e = (Entry)o;
            Object key = e.getKey();
            return key != null && MapMakerInternalMap.this.remove(key, e.getValue());
         }
      }

      public int size() {
         return MapMakerInternalMap.this.size();
      }

      public boolean isEmpty() {
         return MapMakerInternalMap.this.isEmpty();
      }

      public void clear() {
         MapMakerInternalMap.this.clear();
      }
   }

   final class Values extends AbstractCollection<V> {
      public Iterator<V> iterator() {
         return MapMakerInternalMap.this.new ValueIterator();
      }

      public int size() {
         return MapMakerInternalMap.this.size();
      }

      public boolean isEmpty() {
         return MapMakerInternalMap.this.isEmpty();
      }

      public boolean contains(Object o) {
         return MapMakerInternalMap.this.containsValue(o);
      }

      public void clear() {
         MapMakerInternalMap.this.clear();
      }

      public Object[] toArray() {
         return MapMakerInternalMap.toArrayList(this).toArray();
      }

      public <E> E[] toArray(E[] a) {
         return MapMakerInternalMap.toArrayList(this).toArray(a);
      }
   }

   final class KeySet extends MapMakerInternalMap.SafeToArraySet<K> {
      KeySet() {
         super(null);
      }

      public Iterator<K> iterator() {
         return MapMakerInternalMap.this.new KeyIterator();
      }

      public int size() {
         return MapMakerInternalMap.this.size();
      }

      public boolean isEmpty() {
         return MapMakerInternalMap.this.isEmpty();
      }

      public boolean contains(Object o) {
         return MapMakerInternalMap.this.containsKey(o);
      }

      public boolean remove(Object o) {
         return MapMakerInternalMap.this.remove(o) != null;
      }

      public void clear() {
         MapMakerInternalMap.this.clear();
      }
   }

   final class EntryIterator extends MapMakerInternalMap<K, V, E, S>.HashIterator<Entry<K, V>> {
      EntryIterator() {
         super();
      }

      public Entry<K, V> next() {
         return this.nextEntry();
      }
   }

   final class WriteThroughEntry extends AbstractMapEntry<K, V> {
      final K key;
      V value;

      WriteThroughEntry(K key, V value) {
         this.key = key;
         this.value = value;
      }

      public K getKey() {
         return this.key;
      }

      public V getValue() {
         return this.value;
      }

      public boolean equals(@Nullable Object object) {
         if (!(object instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> that = (Entry)object;
            return this.key.equals(that.getKey()) && this.value.equals(that.getValue());
         }
      }

      public int hashCode() {
         return this.key.hashCode() ^ this.value.hashCode();
      }

      public V setValue(V newValue) {
         V oldValue = MapMakerInternalMap.this.put(this.key, newValue);
         this.value = newValue;
         return oldValue;
      }
   }

   final class ValueIterator extends MapMakerInternalMap<K, V, E, S>.HashIterator<V> {
      ValueIterator() {
         super();
      }

      public V next() {
         return this.nextEntry().getValue();
      }
   }

   final class KeyIterator extends MapMakerInternalMap<K, V, E, S>.HashIterator<K> {
      KeyIterator() {
         super();
      }

      public K next() {
         return this.nextEntry().getKey();
      }
   }

   abstract class HashIterator<T> implements Iterator<T> {
      int nextSegmentIndex;
      int nextTableIndex;
      MapMakerInternalMap.Segment<K, V, E, S> currentSegment;
      AtomicReferenceArray<E> currentTable;
      E nextEntry;
      MapMakerInternalMap<K, V, E, S>.WriteThroughEntry nextExternal;
      MapMakerInternalMap<K, V, E, S>.WriteThroughEntry lastReturned;

      HashIterator() {
         this.nextSegmentIndex = MapMakerInternalMap.this.segments.length - 1;
         this.nextTableIndex = -1;
         this.advance();
      }

      public abstract T next();

      final void advance() {
         this.nextExternal = null;
         if (!this.nextInChain()) {
            if (!this.nextInTable()) {
               while(this.nextSegmentIndex >= 0) {
                  this.currentSegment = MapMakerInternalMap.this.segments[this.nextSegmentIndex--];
                  if (this.currentSegment.count != 0) {
                     this.currentTable = this.currentSegment.table;
                     this.nextTableIndex = this.currentTable.length() - 1;
                     if (this.nextInTable()) {
                        return;
                     }
                  }
               }

            }
         }
      }

      boolean nextInChain() {
         if (this.nextEntry != null) {
            for(this.nextEntry = this.nextEntry.getNext(); this.nextEntry != null; this.nextEntry = this.nextEntry.getNext()) {
               if (this.advanceTo(this.nextEntry)) {
                  return true;
               }
            }
         }

         return false;
      }

      boolean nextInTable() {
         while(true) {
            if (this.nextTableIndex >= 0) {
               if ((this.nextEntry = (MapMakerInternalMap.InternalEntry)this.currentTable.get(this.nextTableIndex--)) == null || !this.advanceTo(this.nextEntry) && !this.nextInChain()) {
                  continue;
               }

               return true;
            }

            return false;
         }
      }

      boolean advanceTo(E entry) {
         boolean var4;
         try {
            K key = entry.getKey();
            V value = MapMakerInternalMap.this.getLiveValue(entry);
            if (value == null) {
               var4 = false;
               return var4;
            }

            this.nextExternal = MapMakerInternalMap.this.new WriteThroughEntry(key, value);
            var4 = true;
         } finally {
            this.currentSegment.postReadCleanup();
         }

         return var4;
      }

      public boolean hasNext() {
         return this.nextExternal != null;
      }

      MapMakerInternalMap<K, V, E, S>.WriteThroughEntry nextEntry() {
         if (this.nextExternal == null) {
            throw new NoSuchElementException();
         } else {
            this.lastReturned = this.nextExternal;
            this.advance();
            return this.lastReturned;
         }
      }

      public void remove() {
         CollectPreconditions.checkRemove(this.lastReturned != null);
         MapMakerInternalMap.this.remove(this.lastReturned.getKey());
         this.lastReturned = null;
      }
   }

   static final class CleanupMapTask implements Runnable {
      final WeakReference<MapMakerInternalMap<?, ?, ?, ?>> mapReference;

      public CleanupMapTask(MapMakerInternalMap<?, ?, ?, ?> map) {
         this.mapReference = new WeakReference(map);
      }

      public void run() {
         MapMakerInternalMap<?, ?, ?, ?> map = (MapMakerInternalMap)this.mapReference.get();
         if (map == null) {
            throw new CancellationException();
         } else {
            MapMakerInternalMap.Segment[] arr$ = map.segments;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               MapMakerInternalMap.Segment<?, ?, ?, ?> segment = arr$[i$];
               segment.runCleanup();
            }

         }
      }
   }

   static final class WeakKeyWeakValueSegment<K, V> extends MapMakerInternalMap.Segment<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>, MapMakerInternalMap.WeakKeyWeakValueSegment<K, V>> {
      private final ReferenceQueue<K> queueForKeys = new ReferenceQueue();
      private final ReferenceQueue<V> queueForValues = new ReferenceQueue();

      WeakKeyWeakValueSegment(MapMakerInternalMap<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>, MapMakerInternalMap.WeakKeyWeakValueSegment<K, V>> map, int initialCapacity, int maxSegmentSize) {
         super(map, initialCapacity, maxSegmentSize);
      }

      MapMakerInternalMap.WeakKeyWeakValueSegment<K, V> self() {
         return this;
      }

      ReferenceQueue<K> getKeyReferenceQueueForTesting() {
         return this.queueForKeys;
      }

      ReferenceQueue<V> getValueReferenceQueueForTesting() {
         return this.queueForValues;
      }

      public MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> castForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return (MapMakerInternalMap.WeakKeyWeakValueEntry)entry;
      }

      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> getWeakValueReferenceForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> e) {
         return this.castForTesting(e).getValueReference();
      }

      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> newWeakValueReferenceForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> e, V value) {
         return new MapMakerInternalMap.WeakValueReferenceImpl(this.queueForValues, value, this.castForTesting(e));
      }

      public void setWeakValueReferenceForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> e, MapMakerInternalMap.WeakValueReference<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>> valueReference) {
         MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> entry = this.castForTesting(e);
         MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> previous = entry.valueReference;
         entry.valueReference = valueReference;
         previous.clear();
      }

      void maybeDrainReferenceQueues() {
         this.drainKeyReferenceQueue(this.queueForKeys);
         this.drainValueReferenceQueue(this.queueForValues);
      }

      void maybeClearReferenceQueues() {
         this.clearReferenceQueue(this.queueForKeys);
      }
   }

   static final class WeakKeyStrongValueSegment<K, V> extends MapMakerInternalMap.Segment<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>, MapMakerInternalMap.WeakKeyStrongValueSegment<K, V>> {
      private final ReferenceQueue<K> queueForKeys = new ReferenceQueue();

      WeakKeyStrongValueSegment(MapMakerInternalMap<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>, MapMakerInternalMap.WeakKeyStrongValueSegment<K, V>> map, int initialCapacity, int maxSegmentSize) {
         super(map, initialCapacity, maxSegmentSize);
      }

      MapMakerInternalMap.WeakKeyStrongValueSegment<K, V> self() {
         return this;
      }

      ReferenceQueue<K> getKeyReferenceQueueForTesting() {
         return this.queueForKeys;
      }

      public MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> castForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return (MapMakerInternalMap.WeakKeyStrongValueEntry)entry;
      }

      void maybeDrainReferenceQueues() {
         this.drainKeyReferenceQueue(this.queueForKeys);
      }

      void maybeClearReferenceQueues() {
         this.clearReferenceQueue(this.queueForKeys);
      }
   }

   static final class StrongKeyWeakValueSegment<K, V> extends MapMakerInternalMap.Segment<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>, MapMakerInternalMap.StrongKeyWeakValueSegment<K, V>> {
      private final ReferenceQueue<V> queueForValues = new ReferenceQueue();

      StrongKeyWeakValueSegment(MapMakerInternalMap<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>, MapMakerInternalMap.StrongKeyWeakValueSegment<K, V>> map, int initialCapacity, int maxSegmentSize) {
         super(map, initialCapacity, maxSegmentSize);
      }

      MapMakerInternalMap.StrongKeyWeakValueSegment<K, V> self() {
         return this;
      }

      ReferenceQueue<V> getValueReferenceQueueForTesting() {
         return this.queueForValues;
      }

      public MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> castForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return (MapMakerInternalMap.StrongKeyWeakValueEntry)entry;
      }

      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> getWeakValueReferenceForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> e) {
         return this.castForTesting(e).getValueReference();
      }

      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> newWeakValueReferenceForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> e, V value) {
         return new MapMakerInternalMap.WeakValueReferenceImpl(this.queueForValues, value, this.castForTesting(e));
      }

      public void setWeakValueReferenceForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> e, MapMakerInternalMap.WeakValueReference<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>> valueReference) {
         MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> entry = this.castForTesting(e);
         MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> previous = entry.valueReference;
         entry.valueReference = valueReference;
         previous.clear();
      }

      void maybeDrainReferenceQueues() {
         this.drainValueReferenceQueue(this.queueForValues);
      }

      void maybeClearReferenceQueues() {
         this.clearReferenceQueue(this.queueForValues);
      }
   }

   static final class StrongKeyStrongValueSegment<K, V> extends MapMakerInternalMap.Segment<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>, MapMakerInternalMap.StrongKeyStrongValueSegment<K, V>> {
      StrongKeyStrongValueSegment(MapMakerInternalMap<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>, MapMakerInternalMap.StrongKeyStrongValueSegment<K, V>> map, int initialCapacity, int maxSegmentSize) {
         super(map, initialCapacity, maxSegmentSize);
      }

      MapMakerInternalMap.StrongKeyStrongValueSegment<K, V> self() {
         return this;
      }

      public MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> castForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return (MapMakerInternalMap.StrongKeyStrongValueEntry)entry;
      }
   }

   abstract static class Segment<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>, S extends MapMakerInternalMap.Segment<K, V, E, S>> extends ReentrantLock {
      @Weak
      final MapMakerInternalMap<K, V, E, S> map;
      volatile int count;
      int modCount;
      int threshold;
      volatile AtomicReferenceArray<E> table;
      final int maxSegmentSize;
      final AtomicInteger readCount = new AtomicInteger();

      Segment(MapMakerInternalMap<K, V, E, S> map, int initialCapacity, int maxSegmentSize) {
         this.map = map;
         this.maxSegmentSize = maxSegmentSize;
         this.initTable(this.newEntryArray(initialCapacity));
      }

      abstract S self();

      @GuardedBy("this")
      void maybeDrainReferenceQueues() {
      }

      void maybeClearReferenceQueues() {
      }

      void setValue(E entry, V value) {
         this.map.entryHelper.setValue(this.self(), entry, value);
      }

      E copyEntry(E original, E newNext) {
         return this.map.entryHelper.copy(this.self(), original, newNext);
      }

      AtomicReferenceArray<E> newEntryArray(int size) {
         return new AtomicReferenceArray(size);
      }

      void initTable(AtomicReferenceArray<E> newTable) {
         this.threshold = newTable.length() * 3 / 4;
         if (this.threshold == this.maxSegmentSize) {
            ++this.threshold;
         }

         this.table = newTable;
      }

      abstract E castForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> var1);

      ReferenceQueue<K> getKeyReferenceQueueForTesting() {
         throw new AssertionError();
      }

      ReferenceQueue<V> getValueReferenceQueueForTesting() {
         throw new AssertionError();
      }

      MapMakerInternalMap.WeakValueReference<K, V, E> getWeakValueReferenceForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         throw new AssertionError();
      }

      MapMakerInternalMap.WeakValueReference<K, V, E> newWeakValueReferenceForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry, V value) {
         throw new AssertionError();
      }

      void setWeakValueReferenceForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry, MapMakerInternalMap.WeakValueReference<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>> valueReference) {
         throw new AssertionError();
      }

      void setTableEntryForTesting(int i, MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         this.table.set(i, this.castForTesting(entry));
      }

      E copyForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry, @Nullable MapMakerInternalMap.InternalEntry<K, V, ?> newNext) {
         return this.map.entryHelper.copy(this.self(), this.castForTesting(entry), this.castForTesting(newNext));
      }

      void setValueForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry, V value) {
         this.map.entryHelper.setValue(this.self(), this.castForTesting(entry), value);
      }

      E newEntryForTesting(K key, int hash, @Nullable MapMakerInternalMap.InternalEntry<K, V, ?> next) {
         return this.map.entryHelper.newEntry(this.self(), key, hash, this.castForTesting(next));
      }

      @CanIgnoreReturnValue
      boolean removeTableEntryForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return this.removeEntryForTesting(this.castForTesting(entry));
      }

      E removeFromChainForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> first, MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return this.removeFromChain(this.castForTesting(first), this.castForTesting(entry));
      }

      @Nullable
      V getLiveValueForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return this.getLiveValue(this.castForTesting(entry));
      }

      void tryDrainReferenceQueues() {
         if (this.tryLock()) {
            try {
               this.maybeDrainReferenceQueues();
            } finally {
               this.unlock();
            }
         }

      }

      @GuardedBy("this")
      void drainKeyReferenceQueue(ReferenceQueue<K> keyReferenceQueue) {
         int i = 0;

         Reference ref;
         while((ref = keyReferenceQueue.poll()) != null) {
            E entry = (MapMakerInternalMap.InternalEntry)ref;
            this.map.reclaimKey(entry);
            ++i;
            if (i == 16) {
               break;
            }
         }

      }

      @GuardedBy("this")
      void drainValueReferenceQueue(ReferenceQueue<V> valueReferenceQueue) {
         int i = 0;

         Reference ref;
         while((ref = valueReferenceQueue.poll()) != null) {
            MapMakerInternalMap.WeakValueReference<K, V, E> valueReference = (MapMakerInternalMap.WeakValueReference)ref;
            this.map.reclaimValue(valueReference);
            ++i;
            if (i == 16) {
               break;
            }
         }

      }

      <T> void clearReferenceQueue(ReferenceQueue<T> referenceQueue) {
         while(referenceQueue.poll() != null) {
         }

      }

      E getFirst(int hash) {
         AtomicReferenceArray<E> table = this.table;
         return (MapMakerInternalMap.InternalEntry)table.get(hash & table.length() - 1);
      }

      E getEntry(Object key, int hash) {
         if (this.count != 0) {
            for(MapMakerInternalMap.InternalEntry e = this.getFirst(hash); e != null; e = e.getNext()) {
               if (e.getHash() == hash) {
                  K entryKey = e.getKey();
                  if (entryKey == null) {
                     this.tryDrainReferenceQueues();
                  } else if (this.map.keyEquivalence.equivalent(key, entryKey)) {
                     return e;
                  }
               }
            }
         }

         return null;
      }

      E getLiveEntry(Object key, int hash) {
         return this.getEntry(key, hash);
      }

      V get(Object key, int hash) {
         Object value;
         try {
            E e = this.getLiveEntry(key, hash);
            if (e != null) {
               value = e.getValue();
               if (value == null) {
                  this.tryDrainReferenceQueues();
               }

               Object var5 = value;
               return var5;
            }

            value = null;
         } finally {
            this.postReadCleanup();
         }

         return value;
      }

      boolean containsKey(Object key, int hash) {
         boolean var4;
         try {
            if (this.count == 0) {
               boolean var8 = false;
               return var8;
            }

            E e = this.getLiveEntry(key, hash);
            var4 = e != null && e.getValue() != null;
         } finally {
            this.postReadCleanup();
         }

         return var4;
      }

      @VisibleForTesting
      boolean containsValue(Object value) {
         boolean var11;
         try {
            if (this.count != 0) {
               AtomicReferenceArray<E> table = this.table;
               int length = table.length();

               for(int i = 0; i < length; ++i) {
                  for(MapMakerInternalMap.InternalEntry e = (MapMakerInternalMap.InternalEntry)table.get(i); e != null; e = e.getNext()) {
                     V entryValue = this.getLiveValue(e);
                     if (entryValue != null && this.map.valueEquivalence().equivalent(value, entryValue)) {
                        boolean var7 = true;
                        return var7;
                     }
                  }
               }
            }

            var11 = false;
         } finally {
            this.postReadCleanup();
         }

         return var11;
      }

      V put(K key, int hash, V value, boolean onlyIfAbsent) {
         this.lock();

         try {
            this.preWriteCleanup();
            int newCount = this.count + 1;
            if (newCount > this.threshold) {
               this.expand();
               newCount = this.count + 1;
            }

            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = (MapMakerInternalMap.InternalEntry)table.get(index);

            MapMakerInternalMap.InternalEntry e;
            Object entryKey;
            for(e = first; e != null; e = e.getNext()) {
               entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  V entryValue = e.getValue();
                  Object var12;
                  if (entryValue == null) {
                     ++this.modCount;
                     this.setValue(e, value);
                     newCount = this.count;
                     this.count = newCount;
                     var12 = null;
                     return var12;
                  }

                  if (!onlyIfAbsent) {
                     ++this.modCount;
                     this.setValue(e, value);
                     var12 = entryValue;
                     return var12;
                  }

                  var12 = entryValue;
                  return var12;
               }
            }

            ++this.modCount;
            e = this.map.entryHelper.newEntry(this.self(), key, hash, first);
            this.setValue(e, value);
            table.set(index, e);
            this.count = newCount;
            entryKey = null;
            return entryKey;
         } finally {
            this.unlock();
         }
      }

      @GuardedBy("this")
      void expand() {
         AtomicReferenceArray<E> oldTable = this.table;
         int oldCapacity = oldTable.length();
         if (oldCapacity < 1073741824) {
            int newCount = this.count;
            AtomicReferenceArray<E> newTable = this.newEntryArray(oldCapacity << 1);
            this.threshold = newTable.length() * 3 / 4;
            int newMask = newTable.length() - 1;

            for(int oldIndex = 0; oldIndex < oldCapacity; ++oldIndex) {
               E head = (MapMakerInternalMap.InternalEntry)oldTable.get(oldIndex);
               if (head != null) {
                  E next = head.getNext();
                  int headIndex = head.getHash() & newMask;
                  if (next == null) {
                     newTable.set(headIndex, head);
                  } else {
                     E tail = head;
                     int tailIndex = headIndex;

                     MapMakerInternalMap.InternalEntry e;
                     int newIndex;
                     for(e = next; e != null; e = e.getNext()) {
                        newIndex = e.getHash() & newMask;
                        if (newIndex != tailIndex) {
                           tailIndex = newIndex;
                           tail = e;
                        }
                     }

                     newTable.set(tailIndex, tail);

                     for(e = head; e != tail; e = e.getNext()) {
                        newIndex = e.getHash() & newMask;
                        E newNext = (MapMakerInternalMap.InternalEntry)newTable.get(newIndex);
                        E newFirst = this.copyEntry(e, newNext);
                        if (newFirst != null) {
                           newTable.set(newIndex, newFirst);
                        } else {
                           --newCount;
                        }
                     }
                  }
               }
            }

            this.table = newTable;
            this.count = newCount;
         }
      }

      boolean replace(K key, int hash, V oldValue, V newValue) {
         this.lock();

         try {
            this.preWriteCleanup();
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = (MapMakerInternalMap.InternalEntry)table.get(index);

            for(MapMakerInternalMap.InternalEntry e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  V entryValue = e.getValue();
                  boolean var11;
                  if (entryValue == null) {
                     if (isCollected(e)) {
                        int newCount = this.count - 1;
                        ++this.modCount;
                        E newFirst = this.removeFromChain(first, e);
                        newCount = this.count - 1;
                        table.set(index, newFirst);
                        this.count = newCount;
                     }

                     var11 = false;
                     return var11;
                  }

                  if (!this.map.valueEquivalence().equivalent(oldValue, entryValue)) {
                     var11 = false;
                     return var11;
                  }

                  ++this.modCount;
                  this.setValue(e, newValue);
                  var11 = true;
                  return var11;
               }
            }

            boolean var16 = false;
            return var16;
         } finally {
            this.unlock();
         }
      }

      V replace(K key, int hash, V newValue) {
         this.lock();

         try {
            this.preWriteCleanup();
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = (MapMakerInternalMap.InternalEntry)table.get(index);

            MapMakerInternalMap.InternalEntry e;
            for(e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  V entryValue = e.getValue();
                  Object var10;
                  if (entryValue == null) {
                     if (isCollected(e)) {
                        int newCount = this.count - 1;
                        ++this.modCount;
                        E newFirst = this.removeFromChain(first, e);
                        newCount = this.count - 1;
                        table.set(index, newFirst);
                        this.count = newCount;
                     }

                     var10 = null;
                     return var10;
                  }

                  ++this.modCount;
                  this.setValue(e, newValue);
                  var10 = entryValue;
                  return var10;
               }
            }

            e = null;
            return e;
         } finally {
            this.unlock();
         }
      }

      @CanIgnoreReturnValue
      V remove(Object key, int hash) {
         this.lock();

         MapMakerInternalMap.InternalEntry e;
         try {
            this.preWriteCleanup();
            int newCount = this.count - 1;
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = (MapMakerInternalMap.InternalEntry)table.get(index);

            for(e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  V entryValue = e.getValue();
                  MapMakerInternalMap.InternalEntry newFirst;
                  if (entryValue == null && !isCollected(e)) {
                     newFirst = null;
                     return newFirst;
                  }

                  ++this.modCount;
                  newFirst = this.removeFromChain(first, e);
                  newCount = this.count - 1;
                  table.set(index, newFirst);
                  this.count = newCount;
                  Object var11 = entryValue;
                  return var11;
               }
            }

            e = null;
         } finally {
            this.unlock();
         }

         return e;
      }

      boolean remove(Object key, int hash, Object value) {
         this.lock();

         boolean var17;
         try {
            this.preWriteCleanup();
            int newCount = this.count - 1;
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = (MapMakerInternalMap.InternalEntry)table.get(index);

            for(MapMakerInternalMap.InternalEntry e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  V entryValue = e.getValue();
                  boolean explicitRemoval = false;
                  if (this.map.valueEquivalence().equivalent(value, entryValue)) {
                     explicitRemoval = true;
                  } else if (!isCollected(e)) {
                     boolean var18 = false;
                     return var18;
                  }

                  ++this.modCount;
                  E newFirst = this.removeFromChain(first, e);
                  newCount = this.count - 1;
                  table.set(index, newFirst);
                  this.count = newCount;
                  boolean var13 = explicitRemoval;
                  return var13;
               }
            }

            var17 = false;
         } finally {
            this.unlock();
         }

         return var17;
      }

      void clear() {
         if (this.count != 0) {
            this.lock();

            try {
               AtomicReferenceArray<E> table = this.table;

               for(int i = 0; i < table.length(); ++i) {
                  table.set(i, (Object)null);
               }

               this.maybeClearReferenceQueues();
               this.readCount.set(0);
               ++this.modCount;
               this.count = 0;
            } finally {
               this.unlock();
            }
         }

      }

      @GuardedBy("this")
      E removeFromChain(E first, E entry) {
         int newCount = this.count;
         E newFirst = entry.getNext();

         for(MapMakerInternalMap.InternalEntry e = first; e != entry; e = e.getNext()) {
            E next = this.copyEntry(e, newFirst);
            if (next != null) {
               newFirst = next;
            } else {
               --newCount;
            }
         }

         this.count = newCount;
         return newFirst;
      }

      @CanIgnoreReturnValue
      boolean reclaimKey(E entry, int hash) {
         this.lock();

         try {
            int newCount = this.count - 1;
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = (MapMakerInternalMap.InternalEntry)table.get(index);

            for(MapMakerInternalMap.InternalEntry e = first; e != null; e = e.getNext()) {
               if (e == entry) {
                  ++this.modCount;
                  E newFirst = this.removeFromChain(first, e);
                  newCount = this.count - 1;
                  table.set(index, newFirst);
                  this.count = newCount;
                  boolean var9 = true;
                  return var9;
               }
            }

            boolean var13 = false;
            return var13;
         } finally {
            this.unlock();
         }
      }

      @CanIgnoreReturnValue
      boolean reclaimValue(K key, int hash, MapMakerInternalMap.WeakValueReference<K, V, E> valueReference) {
         this.lock();

         boolean var17;
         try {
            int newCount = this.count - 1;
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = (MapMakerInternalMap.InternalEntry)table.get(index);

            for(MapMakerInternalMap.InternalEntry e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  MapMakerInternalMap.WeakValueReference<K, V, E> v = ((MapMakerInternalMap.WeakValueEntry)e).getValueReference();
                  if (v == valueReference) {
                     ++this.modCount;
                     E newFirst = this.removeFromChain(first, e);
                     newCount = this.count - 1;
                     table.set(index, newFirst);
                     this.count = newCount;
                     boolean var12 = true;
                     return var12;
                  }

                  boolean var11 = false;
                  return var11;
               }
            }

            var17 = false;
         } finally {
            this.unlock();
         }

         return var17;
      }

      @CanIgnoreReturnValue
      boolean clearValueForTesting(K key, int hash, MapMakerInternalMap.WeakValueReference<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>> valueReference) {
         this.lock();

         try {
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = (MapMakerInternalMap.InternalEntry)table.get(index);

            for(MapMakerInternalMap.InternalEntry e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  MapMakerInternalMap.WeakValueReference<K, V, E> v = ((MapMakerInternalMap.WeakValueEntry)e).getValueReference();
                  if (v == valueReference) {
                     E newFirst = this.removeFromChain(first, e);
                     table.set(index, newFirst);
                     boolean var11 = true;
                     return var11;
                  }

                  boolean var10 = false;
                  return var10;
               }
            }

            boolean var16 = false;
            return var16;
         } finally {
            this.unlock();
         }
      }

      @GuardedBy("this")
      boolean removeEntryForTesting(E entry) {
         int hash = entry.getHash();
         int newCount = this.count - 1;
         AtomicReferenceArray<E> table = this.table;
         int index = hash & table.length() - 1;
         E first = (MapMakerInternalMap.InternalEntry)table.get(index);

         for(MapMakerInternalMap.InternalEntry e = first; e != null; e = e.getNext()) {
            if (e == entry) {
               ++this.modCount;
               E newFirst = this.removeFromChain(first, e);
               newCount = this.count - 1;
               table.set(index, newFirst);
               this.count = newCount;
               return true;
            }
         }

         return false;
      }

      static <K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> boolean isCollected(E entry) {
         return entry.getValue() == null;
      }

      @Nullable
      V getLiveValue(E entry) {
         if (entry.getKey() == null) {
            this.tryDrainReferenceQueues();
            return null;
         } else {
            V value = entry.getValue();
            if (value == null) {
               this.tryDrainReferenceQueues();
               return null;
            } else {
               return value;
            }
         }
      }

      void postReadCleanup() {
         if ((this.readCount.incrementAndGet() & 63) == 0) {
            this.runCleanup();
         }

      }

      @GuardedBy("this")
      void preWriteCleanup() {
         this.runLockedCleanup();
      }

      void runCleanup() {
         this.runLockedCleanup();
      }

      void runLockedCleanup() {
         if (this.tryLock()) {
            try {
               this.maybeDrainReferenceQueues();
               this.readCount.set(0);
            } finally {
               this.unlock();
            }
         }

      }
   }

   static final class WeakValueReferenceImpl<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> extends WeakReference<V> implements MapMakerInternalMap.WeakValueReference<K, V, E> {
      final E entry;

      WeakValueReferenceImpl(ReferenceQueue<V> queue, V referent, E entry) {
         super(referent, queue);
         this.entry = entry;
      }

      public E getEntry() {
         return this.entry;
      }

      public MapMakerInternalMap.WeakValueReference<K, V, E> copyFor(ReferenceQueue<V> queue, E entry) {
         return new MapMakerInternalMap.WeakValueReferenceImpl(queue, this.get(), entry);
      }
   }

   static final class DummyInternalEntry implements MapMakerInternalMap.InternalEntry<Object, Object, MapMakerInternalMap.DummyInternalEntry> {
      private DummyInternalEntry() {
         throw new AssertionError();
      }

      public MapMakerInternalMap.DummyInternalEntry getNext() {
         throw new AssertionError();
      }

      public int getHash() {
         throw new AssertionError();
      }

      public Object getKey() {
         throw new AssertionError();
      }

      public Object getValue() {
         throw new AssertionError();
      }
   }

   interface WeakValueReference<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> {
      @Nullable
      V get();

      E getEntry();

      void clear();

      MapMakerInternalMap.WeakValueReference<K, V, E> copyFor(ReferenceQueue<V> var1, E var2);
   }

   static final class WeakKeyWeakValueEntry<K, V> extends MapMakerInternalMap.AbstractWeakKeyEntry<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> implements MapMakerInternalMap.WeakValueEntry<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> {
      private volatile MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> valueReference = MapMakerInternalMap.unsetWeakValueReference();

      WeakKeyWeakValueEntry(ReferenceQueue<K> queue, K key, int hash, @Nullable MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> next) {
         super(queue, key, hash, next);
      }

      public V getValue() {
         return this.valueReference.get();
      }

      MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> copy(ReferenceQueue<K> queueForKeys, ReferenceQueue<V> queueForValues, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> newNext) {
         MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> newEntry = new MapMakerInternalMap.WeakKeyWeakValueEntry(queueForKeys, this.getKey(), this.hash, newNext);
         newEntry.valueReference = this.valueReference.copyFor(queueForValues, newEntry);
         return newEntry;
      }

      public void clearValue() {
         this.valueReference.clear();
      }

      void setValue(V value, ReferenceQueue<V> queueForValues) {
         MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> previous = this.valueReference;
         this.valueReference = new MapMakerInternalMap.WeakValueReferenceImpl(queueForValues, value, this);
         previous.clear();
      }

      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> getValueReference() {
         return this.valueReference;
      }

      static final class Helper<K, V> implements MapMakerInternalMap.InternalEntryHelper<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>, MapMakerInternalMap.WeakKeyWeakValueSegment<K, V>> {
         private static final MapMakerInternalMap.WeakKeyWeakValueEntry.Helper<?, ?> INSTANCE = new MapMakerInternalMap.WeakKeyWeakValueEntry.Helper();

         static <K, V> MapMakerInternalMap.WeakKeyWeakValueEntry.Helper<K, V> instance() {
            return INSTANCE;
         }

         public MapMakerInternalMap.Strength keyStrength() {
            return MapMakerInternalMap.Strength.WEAK;
         }

         public MapMakerInternalMap.Strength valueStrength() {
            return MapMakerInternalMap.Strength.WEAK;
         }

         public MapMakerInternalMap.WeakKeyWeakValueSegment<K, V> newSegment(MapMakerInternalMap<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>, MapMakerInternalMap.WeakKeyWeakValueSegment<K, V>> map, int initialCapacity, int maxSegmentSize) {
            return new MapMakerInternalMap.WeakKeyWeakValueSegment(map, initialCapacity, maxSegmentSize);
         }

         public MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> copy(MapMakerInternalMap.WeakKeyWeakValueSegment<K, V> segment, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> entry, @Nullable MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> newNext) {
            if (entry.getKey() == null) {
               return null;
            } else {
               return MapMakerInternalMap.Segment.isCollected(entry) ? null : entry.copy(segment.queueForKeys, segment.queueForValues, newNext);
            }
         }

         public void setValue(MapMakerInternalMap.WeakKeyWeakValueSegment<K, V> segment, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> entry, V value) {
            entry.setValue(value, segment.queueForValues);
         }

         public MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> newEntry(MapMakerInternalMap.WeakKeyWeakValueSegment<K, V> segment, K key, int hash, @Nullable MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> next) {
            return new MapMakerInternalMap.WeakKeyWeakValueEntry(segment.queueForKeys, key, hash, next);
         }
      }
   }

   static final class WeakKeyStrongValueEntry<K, V> extends MapMakerInternalMap.AbstractWeakKeyEntry<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>> implements MapMakerInternalMap.StrongValueEntry<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>> {
      @Nullable
      private volatile V value = null;

      WeakKeyStrongValueEntry(ReferenceQueue<K> queue, K key, int hash, @Nullable MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> next) {
         super(queue, key, hash, next);
      }

      @Nullable
      public V getValue() {
         return this.value;
      }

      void setValue(V value) {
         this.value = value;
      }

      MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> copy(ReferenceQueue<K> queueForKeys, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> newNext) {
         MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> newEntry = new MapMakerInternalMap.WeakKeyStrongValueEntry(queueForKeys, this.getKey(), this.hash, newNext);
         newEntry.setValue(this.value);
         return newEntry;
      }

      static final class Helper<K, V> implements MapMakerInternalMap.InternalEntryHelper<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>, MapMakerInternalMap.WeakKeyStrongValueSegment<K, V>> {
         private static final MapMakerInternalMap.WeakKeyStrongValueEntry.Helper<?, ?> INSTANCE = new MapMakerInternalMap.WeakKeyStrongValueEntry.Helper();

         static <K, V> MapMakerInternalMap.WeakKeyStrongValueEntry.Helper<K, V> instance() {
            return INSTANCE;
         }

         public MapMakerInternalMap.Strength keyStrength() {
            return MapMakerInternalMap.Strength.WEAK;
         }

         public MapMakerInternalMap.Strength valueStrength() {
            return MapMakerInternalMap.Strength.STRONG;
         }

         public MapMakerInternalMap.WeakKeyStrongValueSegment<K, V> newSegment(MapMakerInternalMap<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>, MapMakerInternalMap.WeakKeyStrongValueSegment<K, V>> map, int initialCapacity, int maxSegmentSize) {
            return new MapMakerInternalMap.WeakKeyStrongValueSegment(map, initialCapacity, maxSegmentSize);
         }

         public MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> copy(MapMakerInternalMap.WeakKeyStrongValueSegment<K, V> segment, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> entry, @Nullable MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> newNext) {
            return entry.getKey() == null ? null : entry.copy(segment.queueForKeys, newNext);
         }

         public void setValue(MapMakerInternalMap.WeakKeyStrongValueSegment<K, V> segment, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> entry, V value) {
            entry.setValue(value);
         }

         public MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> newEntry(MapMakerInternalMap.WeakKeyStrongValueSegment<K, V> segment, K key, int hash, @Nullable MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> next) {
            return new MapMakerInternalMap.WeakKeyStrongValueEntry(segment.queueForKeys, key, hash, next);
         }
      }
   }

   abstract static class AbstractWeakKeyEntry<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> extends WeakReference<K> implements MapMakerInternalMap.InternalEntry<K, V, E> {
      final int hash;
      final E next;

      AbstractWeakKeyEntry(ReferenceQueue<K> queue, K key, int hash, @Nullable E next) {
         super(key, queue);
         this.hash = hash;
         this.next = next;
      }

      public K getKey() {
         return this.get();
      }

      public int getHash() {
         return this.hash;
      }

      public E getNext() {
         return this.next;
      }
   }

   static final class StrongKeyWeakValueEntry<K, V> extends MapMakerInternalMap.AbstractStrongKeyEntry<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> implements MapMakerInternalMap.WeakValueEntry<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> {
      private volatile MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> valueReference = MapMakerInternalMap.unsetWeakValueReference();

      StrongKeyWeakValueEntry(K key, int hash, @Nullable MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> next) {
         super(key, hash, next);
      }

      public V getValue() {
         return this.valueReference.get();
      }

      public void clearValue() {
         this.valueReference.clear();
      }

      void setValue(V value, ReferenceQueue<V> queueForValues) {
         MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> previous = this.valueReference;
         this.valueReference = new MapMakerInternalMap.WeakValueReferenceImpl(queueForValues, value, this);
         previous.clear();
      }

      MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> copy(ReferenceQueue<V> queueForValues, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> newNext) {
         MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> newEntry = new MapMakerInternalMap.StrongKeyWeakValueEntry(this.key, this.hash, newNext);
         newEntry.valueReference = this.valueReference.copyFor(queueForValues, newEntry);
         return newEntry;
      }

      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> getValueReference() {
         return this.valueReference;
      }

      static final class Helper<K, V> implements MapMakerInternalMap.InternalEntryHelper<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>, MapMakerInternalMap.StrongKeyWeakValueSegment<K, V>> {
         private static final MapMakerInternalMap.StrongKeyWeakValueEntry.Helper<?, ?> INSTANCE = new MapMakerInternalMap.StrongKeyWeakValueEntry.Helper();

         static <K, V> MapMakerInternalMap.StrongKeyWeakValueEntry.Helper<K, V> instance() {
            return INSTANCE;
         }

         public MapMakerInternalMap.Strength keyStrength() {
            return MapMakerInternalMap.Strength.STRONG;
         }

         public MapMakerInternalMap.Strength valueStrength() {
            return MapMakerInternalMap.Strength.WEAK;
         }

         public MapMakerInternalMap.StrongKeyWeakValueSegment<K, V> newSegment(MapMakerInternalMap<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>, MapMakerInternalMap.StrongKeyWeakValueSegment<K, V>> map, int initialCapacity, int maxSegmentSize) {
            return new MapMakerInternalMap.StrongKeyWeakValueSegment(map, initialCapacity, maxSegmentSize);
         }

         public MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> copy(MapMakerInternalMap.StrongKeyWeakValueSegment<K, V> segment, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> entry, @Nullable MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> newNext) {
            return MapMakerInternalMap.Segment.isCollected(entry) ? null : entry.copy(segment.queueForValues, newNext);
         }

         public void setValue(MapMakerInternalMap.StrongKeyWeakValueSegment<K, V> segment, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> entry, V value) {
            entry.setValue(value, segment.queueForValues);
         }

         public MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> newEntry(MapMakerInternalMap.StrongKeyWeakValueSegment<K, V> segment, K key, int hash, @Nullable MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> next) {
            return new MapMakerInternalMap.StrongKeyWeakValueEntry(key, hash, next);
         }
      }
   }

   static final class StrongKeyStrongValueEntry<K, V> extends MapMakerInternalMap.AbstractStrongKeyEntry<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>> implements MapMakerInternalMap.StrongValueEntry<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>> {
      @Nullable
      private volatile V value = null;

      StrongKeyStrongValueEntry(K key, int hash, @Nullable MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> next) {
         super(key, hash, next);
      }

      @Nullable
      public V getValue() {
         return this.value;
      }

      void setValue(V value) {
         this.value = value;
      }

      MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> copy(MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> newNext) {
         MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> newEntry = new MapMakerInternalMap.StrongKeyStrongValueEntry(this.key, this.hash, newNext);
         newEntry.value = this.value;
         return newEntry;
      }

      static final class Helper<K, V> implements MapMakerInternalMap.InternalEntryHelper<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>, MapMakerInternalMap.StrongKeyStrongValueSegment<K, V>> {
         private static final MapMakerInternalMap.StrongKeyStrongValueEntry.Helper<?, ?> INSTANCE = new MapMakerInternalMap.StrongKeyStrongValueEntry.Helper();

         static <K, V> MapMakerInternalMap.StrongKeyStrongValueEntry.Helper<K, V> instance() {
            return INSTANCE;
         }

         public MapMakerInternalMap.Strength keyStrength() {
            return MapMakerInternalMap.Strength.STRONG;
         }

         public MapMakerInternalMap.Strength valueStrength() {
            return MapMakerInternalMap.Strength.STRONG;
         }

         public MapMakerInternalMap.StrongKeyStrongValueSegment<K, V> newSegment(MapMakerInternalMap<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>, MapMakerInternalMap.StrongKeyStrongValueSegment<K, V>> map, int initialCapacity, int maxSegmentSize) {
            return new MapMakerInternalMap.StrongKeyStrongValueSegment(map, initialCapacity, maxSegmentSize);
         }

         public MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> copy(MapMakerInternalMap.StrongKeyStrongValueSegment<K, V> segment, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> entry, @Nullable MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> newNext) {
            return entry.copy(newNext);
         }

         public void setValue(MapMakerInternalMap.StrongKeyStrongValueSegment<K, V> segment, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> entry, V value) {
            entry.setValue(value);
         }

         public MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> newEntry(MapMakerInternalMap.StrongKeyStrongValueSegment<K, V> segment, K key, int hash, @Nullable MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> next) {
            return new MapMakerInternalMap.StrongKeyStrongValueEntry(key, hash, next);
         }
      }
   }

   interface WeakValueEntry<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> extends MapMakerInternalMap.InternalEntry<K, V, E> {
      MapMakerInternalMap.WeakValueReference<K, V, E> getValueReference();

      void clearValue();
   }

   interface StrongValueEntry<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> extends MapMakerInternalMap.InternalEntry<K, V, E> {
   }

   abstract static class AbstractStrongKeyEntry<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> implements MapMakerInternalMap.InternalEntry<K, V, E> {
      final K key;
      final int hash;
      final E next;

      AbstractStrongKeyEntry(K key, int hash, @Nullable E next) {
         this.key = key;
         this.hash = hash;
         this.next = next;
      }

      public K getKey() {
         return this.key;
      }

      public int getHash() {
         return this.hash;
      }

      public E getNext() {
         return this.next;
      }
   }

   interface InternalEntry<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> {
      E getNext();

      int getHash();

      K getKey();

      V getValue();
   }

   interface InternalEntryHelper<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>, S extends MapMakerInternalMap.Segment<K, V, E, S>> {
      MapMakerInternalMap.Strength keyStrength();

      MapMakerInternalMap.Strength valueStrength();

      S newSegment(MapMakerInternalMap<K, V, E, S> var1, int var2, int var3);

      E newEntry(S var1, K var2, int var3, @Nullable E var4);

      E copy(S var1, E var2, @Nullable E var3);

      void setValue(S var1, E var2, V var3);
   }

   static enum Strength {
      STRONG {
         Equivalence<Object> defaultEquivalence() {
            return Equivalence.equals();
         }
      },
      WEAK {
         Equivalence<Object> defaultEquivalence() {
            return Equivalence.identity();
         }
      };

      private Strength() {
      }

      abstract Equivalence<Object> defaultEquivalence();

      // $FF: synthetic method
      Strength(Object x2) {
         this();
      }
   }
}

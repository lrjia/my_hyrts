package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;

@GwtCompatible(
   serializable = true,
   emulated = true
)
public final class LinkedHashMultimap<K, V> extends AbstractSetMultimap<K, V> {
   private static final int DEFAULT_KEY_CAPACITY = 16;
   private static final int DEFAULT_VALUE_SET_CAPACITY = 2;
   @VisibleForTesting
   static final double VALUE_SET_LOAD_FACTOR = 1.0D;
   @VisibleForTesting
   transient int valueSetCapacity = 2;
   private transient LinkedHashMultimap.ValueEntry<K, V> multimapHeaderEntry;
   @GwtIncompatible
   private static final long serialVersionUID = 1L;

   public static <K, V> LinkedHashMultimap<K, V> create() {
      return new LinkedHashMultimap(16, 2);
   }

   public static <K, V> LinkedHashMultimap<K, V> create(int expectedKeys, int expectedValuesPerKey) {
      return new LinkedHashMultimap(Maps.capacity(expectedKeys), Maps.capacity(expectedValuesPerKey));
   }

   public static <K, V> LinkedHashMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
      LinkedHashMultimap<K, V> result = create(multimap.keySet().size(), 2);
      result.putAll(multimap);
      return result;
   }

   private static <K, V> void succeedsInValueSet(LinkedHashMultimap.ValueSetLink<K, V> pred, LinkedHashMultimap.ValueSetLink<K, V> succ) {
      pred.setSuccessorInValueSet(succ);
      succ.setPredecessorInValueSet(pred);
   }

   private static <K, V> void succeedsInMultimap(LinkedHashMultimap.ValueEntry<K, V> pred, LinkedHashMultimap.ValueEntry<K, V> succ) {
      pred.setSuccessorInMultimap(succ);
      succ.setPredecessorInMultimap(pred);
   }

   private static <K, V> void deleteFromValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry) {
      succeedsInValueSet(entry.getPredecessorInValueSet(), entry.getSuccessorInValueSet());
   }

   private static <K, V> void deleteFromMultimap(LinkedHashMultimap.ValueEntry<K, V> entry) {
      succeedsInMultimap(entry.getPredecessorInMultimap(), entry.getSuccessorInMultimap());
   }

   private LinkedHashMultimap(int keyCapacity, int valueSetCapacity) {
      super(new LinkedHashMap(keyCapacity));
      CollectPreconditions.checkNonnegative(valueSetCapacity, "expectedValuesPerKey");
      this.valueSetCapacity = valueSetCapacity;
      this.multimapHeaderEntry = new LinkedHashMultimap.ValueEntry((Object)null, (Object)null, 0, (LinkedHashMultimap.ValueEntry)null);
      succeedsInMultimap(this.multimapHeaderEntry, this.multimapHeaderEntry);
   }

   Set<V> createCollection() {
      return new LinkedHashSet(this.valueSetCapacity);
   }

   Collection<V> createCollection(K key) {
      return new LinkedHashMultimap.ValueSet(key, this.valueSetCapacity);
   }

   @CanIgnoreReturnValue
   public Set<V> replaceValues(@Nullable K key, Iterable<? extends V> values) {
      return super.replaceValues(key, values);
   }

   public Set<Entry<K, V>> entries() {
      return super.entries();
   }

   public Set<K> keySet() {
      return super.keySet();
   }

   public Collection<V> values() {
      return super.values();
   }

   Iterator<Entry<K, V>> entryIterator() {
      return new Iterator<Entry<K, V>>() {
         LinkedHashMultimap.ValueEntry<K, V> nextEntry;
         LinkedHashMultimap.ValueEntry<K, V> toRemove;

         {
            this.nextEntry = LinkedHashMultimap.this.multimapHeaderEntry.successorInMultimap;
         }

         public boolean hasNext() {
            return this.nextEntry != LinkedHashMultimap.this.multimapHeaderEntry;
         }

         public Entry<K, V> next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               LinkedHashMultimap.ValueEntry<K, V> result = this.nextEntry;
               this.toRemove = result;
               this.nextEntry = this.nextEntry.successorInMultimap;
               return result;
            }
         }

         public void remove() {
            CollectPreconditions.checkRemove(this.toRemove != null);
            LinkedHashMultimap.this.remove(this.toRemove.getKey(), this.toRemove.getValue());
            this.toRemove = null;
         }
      };
   }

   Iterator<V> valueIterator() {
      return Maps.valueIterator(this.entryIterator());
   }

   public void clear() {
      super.clear();
      succeedsInMultimap(this.multimapHeaderEntry, this.multimapHeaderEntry);
   }

   @GwtIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeInt(this.keySet().size());
      Iterator i$ = this.keySet().iterator();

      while(i$.hasNext()) {
         K key = i$.next();
         stream.writeObject(key);
      }

      stream.writeInt(this.size());
      i$ = this.entries().iterator();

      while(i$.hasNext()) {
         Entry<K, V> entry = (Entry)i$.next();
         stream.writeObject(entry.getKey());
         stream.writeObject(entry.getValue());
      }

   }

   @GwtIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.multimapHeaderEntry = new LinkedHashMultimap.ValueEntry((Object)null, (Object)null, 0, (LinkedHashMultimap.ValueEntry)null);
      succeedsInMultimap(this.multimapHeaderEntry, this.multimapHeaderEntry);
      this.valueSetCapacity = 2;
      int distinctKeys = stream.readInt();
      Map<K, Collection<V>> map = new LinkedHashMap();

      int entries;
      for(entries = 0; entries < distinctKeys; ++entries) {
         K key = stream.readObject();
         map.put(key, this.createCollection(key));
      }

      entries = stream.readInt();

      for(int i = 0; i < entries; ++i) {
         K key = stream.readObject();
         V value = stream.readObject();
         ((Collection)map.get(key)).add(value);
      }

      this.setMap(map);
   }

   @VisibleForTesting
   final class ValueSet extends Sets.ImprovedAbstractSet<V> implements LinkedHashMultimap.ValueSetLink<K, V> {
      private final K key;
      @VisibleForTesting
      LinkedHashMultimap.ValueEntry<K, V>[] hashTable;
      private int size = 0;
      private int modCount = 0;
      private LinkedHashMultimap.ValueSetLink<K, V> firstEntry;
      private LinkedHashMultimap.ValueSetLink<K, V> lastEntry;

      ValueSet(K key, int expectedValues) {
         this.key = key;
         this.firstEntry = this;
         this.lastEntry = this;
         int tableSize = Hashing.closedTableSize(expectedValues, 1.0D);
         LinkedHashMultimap.ValueEntry<K, V>[] hashTable = new LinkedHashMultimap.ValueEntry[tableSize];
         this.hashTable = hashTable;
      }

      private int mask() {
         return this.hashTable.length - 1;
      }

      public LinkedHashMultimap.ValueSetLink<K, V> getPredecessorInValueSet() {
         return this.lastEntry;
      }

      public LinkedHashMultimap.ValueSetLink<K, V> getSuccessorInValueSet() {
         return this.firstEntry;
      }

      public void setPredecessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry) {
         this.lastEntry = entry;
      }

      public void setSuccessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry) {
         this.firstEntry = entry;
      }

      public Iterator<V> iterator() {
         return new Iterator<V>() {
            LinkedHashMultimap.ValueSetLink<K, V> nextEntry;
            LinkedHashMultimap.ValueEntry<K, V> toRemove;
            int expectedModCount;

            {
               this.nextEntry = ValueSet.this.firstEntry;
               this.expectedModCount = ValueSet.this.modCount;
            }

            private void checkForComodification() {
               if (ValueSet.this.modCount != this.expectedModCount) {
                  throw new ConcurrentModificationException();
               }
            }

            public boolean hasNext() {
               this.checkForComodification();
               return this.nextEntry != ValueSet.this;
            }

            public V next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  LinkedHashMultimap.ValueEntry<K, V> entry = (LinkedHashMultimap.ValueEntry)this.nextEntry;
                  V result = entry.getValue();
                  this.toRemove = entry;
                  this.nextEntry = entry.getSuccessorInValueSet();
                  return result;
               }
            }

            public void remove() {
               this.checkForComodification();
               CollectPreconditions.checkRemove(this.toRemove != null);
               ValueSet.this.remove(this.toRemove.getValue());
               this.expectedModCount = ValueSet.this.modCount;
               this.toRemove = null;
            }
         };
      }

      public int size() {
         return this.size;
      }

      public boolean contains(@Nullable Object o) {
         int smearedHash = Hashing.smearedHash(o);

         for(LinkedHashMultimap.ValueEntry entry = this.hashTable[smearedHash & this.mask()]; entry != null; entry = entry.nextInValueBucket) {
            if (entry.matchesValue(o, smearedHash)) {
               return true;
            }
         }

         return false;
      }

      public boolean add(@Nullable V value) {
         int smearedHash = Hashing.smearedHash(value);
         int bucket = smearedHash & this.mask();
         LinkedHashMultimap.ValueEntry<K, V> rowHead = this.hashTable[bucket];

         LinkedHashMultimap.ValueEntry entry;
         for(entry = rowHead; entry != null; entry = entry.nextInValueBucket) {
            if (entry.matchesValue(value, smearedHash)) {
               return false;
            }
         }

         entry = new LinkedHashMultimap.ValueEntry(this.key, value, smearedHash, rowHead);
         LinkedHashMultimap.succeedsInValueSet(this.lastEntry, entry);
         LinkedHashMultimap.succeedsInValueSet(entry, this);
         LinkedHashMultimap.succeedsInMultimap(LinkedHashMultimap.this.multimapHeaderEntry.getPredecessorInMultimap(), entry);
         LinkedHashMultimap.succeedsInMultimap(entry, LinkedHashMultimap.this.multimapHeaderEntry);
         this.hashTable[bucket] = entry;
         ++this.size;
         ++this.modCount;
         this.rehashIfNecessary();
         return true;
      }

      private void rehashIfNecessary() {
         if (Hashing.needsResizing(this.size, this.hashTable.length, 1.0D)) {
            LinkedHashMultimap.ValueEntry<K, V>[] hashTable = new LinkedHashMultimap.ValueEntry[this.hashTable.length * 2];
            this.hashTable = hashTable;
            int mask = hashTable.length - 1;

            for(LinkedHashMultimap.ValueSetLink entry = this.firstEntry; entry != this; entry = entry.getSuccessorInValueSet()) {
               LinkedHashMultimap.ValueEntry<K, V> valueEntry = (LinkedHashMultimap.ValueEntry)entry;
               int bucket = valueEntry.smearedValueHash & mask;
               valueEntry.nextInValueBucket = hashTable[bucket];
               hashTable[bucket] = valueEntry;
            }
         }

      }

      @CanIgnoreReturnValue
      public boolean remove(@Nullable Object o) {
         int smearedHash = Hashing.smearedHash(o);
         int bucket = smearedHash & this.mask();
         LinkedHashMultimap.ValueEntry<K, V> prev = null;

         for(LinkedHashMultimap.ValueEntry entry = this.hashTable[bucket]; entry != null; entry = entry.nextInValueBucket) {
            if (entry.matchesValue(o, smearedHash)) {
               if (prev == null) {
                  this.hashTable[bucket] = entry.nextInValueBucket;
               } else {
                  prev.nextInValueBucket = entry.nextInValueBucket;
               }

               LinkedHashMultimap.deleteFromValueSet(entry);
               LinkedHashMultimap.deleteFromMultimap(entry);
               --this.size;
               ++this.modCount;
               return true;
            }

            prev = entry;
         }

         return false;
      }

      public void clear() {
         Arrays.fill(this.hashTable, (Object)null);
         this.size = 0;

         for(LinkedHashMultimap.ValueSetLink entry = this.firstEntry; entry != this; entry = entry.getSuccessorInValueSet()) {
            LinkedHashMultimap.ValueEntry<K, V> valueEntry = (LinkedHashMultimap.ValueEntry)entry;
            LinkedHashMultimap.deleteFromMultimap(valueEntry);
         }

         LinkedHashMultimap.succeedsInValueSet(this, this);
         ++this.modCount;
      }
   }

   @VisibleForTesting
   static final class ValueEntry<K, V> extends ImmutableEntry<K, V> implements LinkedHashMultimap.ValueSetLink<K, V> {
      final int smearedValueHash;
      @Nullable
      LinkedHashMultimap.ValueEntry<K, V> nextInValueBucket;
      LinkedHashMultimap.ValueSetLink<K, V> predecessorInValueSet;
      LinkedHashMultimap.ValueSetLink<K, V> successorInValueSet;
      LinkedHashMultimap.ValueEntry<K, V> predecessorInMultimap;
      LinkedHashMultimap.ValueEntry<K, V> successorInMultimap;

      ValueEntry(@Nullable K key, @Nullable V value, int smearedValueHash, @Nullable LinkedHashMultimap.ValueEntry<K, V> nextInValueBucket) {
         super(key, value);
         this.smearedValueHash = smearedValueHash;
         this.nextInValueBucket = nextInValueBucket;
      }

      boolean matchesValue(@Nullable Object v, int smearedVHash) {
         return this.smearedValueHash == smearedVHash && Objects.equal(this.getValue(), v);
      }

      public LinkedHashMultimap.ValueSetLink<K, V> getPredecessorInValueSet() {
         return this.predecessorInValueSet;
      }

      public LinkedHashMultimap.ValueSetLink<K, V> getSuccessorInValueSet() {
         return this.successorInValueSet;
      }

      public void setPredecessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry) {
         this.predecessorInValueSet = entry;
      }

      public void setSuccessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry) {
         this.successorInValueSet = entry;
      }

      public LinkedHashMultimap.ValueEntry<K, V> getPredecessorInMultimap() {
         return this.predecessorInMultimap;
      }

      public LinkedHashMultimap.ValueEntry<K, V> getSuccessorInMultimap() {
         return this.successorInMultimap;
      }

      public void setSuccessorInMultimap(LinkedHashMultimap.ValueEntry<K, V> multimapSuccessor) {
         this.successorInMultimap = multimapSuccessor;
      }

      public void setPredecessorInMultimap(LinkedHashMultimap.ValueEntry<K, V> multimapPredecessor) {
         this.predecessorInMultimap = multimapPredecessor;
      }
   }

   private interface ValueSetLink<K, V> {
      LinkedHashMultimap.ValueSetLink<K, V> getPredecessorInValueSet();

      LinkedHashMultimap.ValueSetLink<K, V> getSuccessorInValueSet();

      void setPredecessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> var1);

      void setSuccessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> var1);
   }
}

package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@GwtCompatible(
   serializable = true,
   emulated = true
)
public final class ArrayListMultimap<K, V> extends AbstractListMultimap<K, V> {
   private static final int DEFAULT_VALUES_PER_KEY = 3;
   @VisibleForTesting
   transient int expectedValuesPerKey;
   @GwtIncompatible
   private static final long serialVersionUID = 0L;

   public static <K, V> ArrayListMultimap<K, V> create() {
      return new ArrayListMultimap();
   }

   public static <K, V> ArrayListMultimap<K, V> create(int expectedKeys, int expectedValuesPerKey) {
      return new ArrayListMultimap(expectedKeys, expectedValuesPerKey);
   }

   public static <K, V> ArrayListMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
      return new ArrayListMultimap(multimap);
   }

   private ArrayListMultimap() {
      super(new HashMap());
      this.expectedValuesPerKey = 3;
   }

   private ArrayListMultimap(int expectedKeys, int expectedValuesPerKey) {
      super(Maps.newHashMapWithExpectedSize(expectedKeys));
      CollectPreconditions.checkNonnegative(expectedValuesPerKey, "expectedValuesPerKey");
      this.expectedValuesPerKey = expectedValuesPerKey;
   }

   private ArrayListMultimap(Multimap<? extends K, ? extends V> multimap) {
      this(multimap.keySet().size(), multimap instanceof ArrayListMultimap ? ((ArrayListMultimap)multimap).expectedValuesPerKey : 3);
      this.putAll(multimap);
   }

   List<V> createCollection() {
      return new ArrayList(this.expectedValuesPerKey);
   }

   public void trimToSize() {
      Iterator i$ = this.backingMap().values().iterator();

      while(i$.hasNext()) {
         Collection<V> collection = (Collection)i$.next();
         ArrayList<V> arrayList = (ArrayList)collection;
         arrayList.trimToSize();
      }

   }

   @GwtIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      Serialization.writeMultimap(this, stream);
   }

   @GwtIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.expectedValuesPerKey = 3;
      int distinctKeys = Serialization.readCount(stream);
      Map<K, Collection<V>> map = Maps.newHashMap();
      this.setMap(map);
      Serialization.populateMultimap(this, stream, distinctKeys);
   }
}
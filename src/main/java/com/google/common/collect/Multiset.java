package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;

@GwtCompatible
public interface Multiset<E> extends Collection<E> {
   int count(@Nullable Object var1);

   @CanIgnoreReturnValue
   int add(@Nullable E var1, int var2);

   @CanIgnoreReturnValue
   int remove(@Nullable Object var1, int var2);

   @CanIgnoreReturnValue
   int setCount(E var1, int var2);

   @CanIgnoreReturnValue
   boolean setCount(E var1, int var2, int var3);

   Set<E> elementSet();

   Set<Multiset.Entry<E>> entrySet();

   boolean equals(@Nullable Object var1);

   int hashCode();

   String toString();

   Iterator<E> iterator();

   boolean contains(@Nullable Object var1);

   boolean containsAll(Collection<?> var1);

   @CanIgnoreReturnValue
   boolean add(E var1);

   @CanIgnoreReturnValue
   boolean remove(@Nullable Object var1);

   @CanIgnoreReturnValue
   boolean removeAll(Collection<?> var1);

   @CanIgnoreReturnValue
   boolean retainAll(Collection<?> var1);

   public interface Entry<E> {
      E getElement();

      int getCount();

      boolean equals(Object var1);

      int hashCode();

      String toString();
   }
}

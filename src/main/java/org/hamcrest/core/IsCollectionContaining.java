package org.hamcrest.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IsCollectionContaining<T> extends TypeSafeDiagnosingMatcher<Iterable<? super T>> {
   private final Matcher<? super T> elementMatcher;

   public IsCollectionContaining(Matcher<? super T> elementMatcher) {
      this.elementMatcher = elementMatcher;
   }

   protected boolean matchesSafely(Iterable<? super T> collection, Description mismatchDescription) {
      boolean isPastFirst = false;

      for(Iterator i$ = collection.iterator(); i$.hasNext(); isPastFirst = true) {
         Object item = i$.next();
         if (this.elementMatcher.matches(item)) {
            return true;
         }

         if (isPastFirst) {
            mismatchDescription.appendText(", ");
         }

         this.elementMatcher.describeMismatch(item, mismatchDescription);
      }

      return false;
   }

   public void describeTo(Description description) {
      description.appendText("a collection containing ").appendDescriptionOf(this.elementMatcher);
   }

   @Factory
   public static <T> Matcher<Iterable<? super T>> hasItem(Matcher<? super T> itemMatcher) {
      return new IsCollectionContaining(itemMatcher);
   }

   @Factory
   public static <T> Matcher<Iterable<? super T>> hasItem(T item) {
      return new IsCollectionContaining(IsEqual.equalTo(item));
   }

   @Factory
   public static <T> Matcher<Iterable<T>> hasItems(Matcher<? super T>... itemMatchers) {
      List<Matcher<? super Iterable<T>>> all = new ArrayList(itemMatchers.length);
      Matcher[] arr$ = itemMatchers;
      int len$ = itemMatchers.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Matcher<? super T> elementMatcher = arr$[i$];
         all.add(new IsCollectionContaining(elementMatcher));
      }

      return AllOf.allOf((Iterable)all);
   }

   @Factory
   public static <T> Matcher<Iterable<T>> hasItems(T... items) {
      List<Matcher<? super Iterable<T>>> all = new ArrayList(items.length);
      Object[] arr$ = items;
      int len$ = items.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         T element = arr$[i$];
         all.add(hasItem(element));
      }

      return AllOf.allOf((Iterable)all);
   }
}

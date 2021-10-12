package org.hamcrest.core;

import java.util.Iterator;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class Every<T> extends TypeSafeDiagnosingMatcher<Iterable<T>> {
   private final Matcher<? super T> matcher;

   public Every(Matcher<? super T> matcher) {
      this.matcher = matcher;
   }

   public boolean matchesSafely(Iterable<T> collection, Description mismatchDescription) {
      Iterator i$ = collection.iterator();

      Object t;
      do {
         if (!i$.hasNext()) {
            return true;
         }

         t = i$.next();
      } while(this.matcher.matches(t));

      mismatchDescription.appendText("an item ");
      this.matcher.describeMismatch(t, mismatchDescription);
      return false;
   }

   public void describeTo(Description description) {
      description.appendText("every item is ").appendDescriptionOf(this.matcher);
   }

   @Factory
   public static <U> Matcher<Iterable<U>> everyItem(Matcher<U> itemMatcher) {
      return new Every(itemMatcher);
   }
}

package org.hamcrest.core;

import java.util.Iterator;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

abstract class ShortcutCombination<T> extends BaseMatcher<T> {
   private final Iterable<Matcher<? super T>> matchers;

   public ShortcutCombination(Iterable<Matcher<? super T>> matchers) {
      this.matchers = matchers;
   }

   public abstract boolean matches(Object var1);

   public abstract void describeTo(Description var1);

   protected boolean matches(Object o, boolean shortcut) {
      Iterator i$ = this.matchers.iterator();

      Matcher matcher;
      do {
         if (!i$.hasNext()) {
            return !shortcut;
         }

         matcher = (Matcher)i$.next();
      } while(matcher.matches(o) != shortcut);

      return shortcut;
   }

   public void describeTo(Description description, String operator) {
      description.appendList("(", " " + operator + " ", ")", this.matchers);
   }
}

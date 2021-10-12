package org.hamcrest;

public abstract class DiagnosingMatcher<T> extends BaseMatcher<T> {
   public final boolean matches(Object item) {
      return this.matches(item, Description.NONE);
   }

   public final void describeMismatch(Object item, Description mismatchDescription) {
      this.matches(item, mismatchDescription);
   }

   protected abstract boolean matches(Object var1, Description var2);
}

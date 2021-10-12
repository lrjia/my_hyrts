package org.hamcrest;

import org.hamcrest.internal.ReflectiveTypeFinder;

public abstract class TypeSafeDiagnosingMatcher<T> extends BaseMatcher<T> {
   private static final ReflectiveTypeFinder TYPE_FINDER = new ReflectiveTypeFinder("matchesSafely", 2, 0);
   private final Class<?> expectedType;

   protected abstract boolean matchesSafely(T var1, Description var2);

   protected TypeSafeDiagnosingMatcher(Class<?> expectedType) {
      this.expectedType = expectedType;
   }

   protected TypeSafeDiagnosingMatcher(ReflectiveTypeFinder typeFinder) {
      this.expectedType = typeFinder.findExpectedType(this.getClass());
   }

   protected TypeSafeDiagnosingMatcher() {
      this(TYPE_FINDER);
   }

   public final boolean matches(Object item) {
      return item != null && this.expectedType.isInstance(item) && this.matchesSafely(item, new Description.NullDescription());
   }

   public final void describeMismatch(Object item, Description mismatchDescription) {
      if (item != null && this.expectedType.isInstance(item)) {
         this.matchesSafely(item, mismatchDescription);
      } else {
         super.describeMismatch(item, mismatchDescription);
      }

   }
}

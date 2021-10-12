package org.hamcrest;

import org.hamcrest.internal.ReflectiveTypeFinder;

public abstract class TypeSafeMatcher<T> extends BaseMatcher<T> {
   private static final ReflectiveTypeFinder TYPE_FINDER = new ReflectiveTypeFinder("matchesSafely", 1, 0);
   private final Class<?> expectedType;

   protected TypeSafeMatcher() {
      this(TYPE_FINDER);
   }

   protected TypeSafeMatcher(Class<?> expectedType) {
      this.expectedType = expectedType;
   }

   protected TypeSafeMatcher(ReflectiveTypeFinder typeFinder) {
      this.expectedType = typeFinder.findExpectedType(this.getClass());
   }

   protected abstract boolean matchesSafely(T var1);

   protected void describeMismatchSafely(T item, Description mismatchDescription) {
      super.describeMismatch(item, mismatchDescription);
   }

   public final boolean matches(Object item) {
      return item != null && this.expectedType.isInstance(item) && this.matchesSafely(item);
   }

   public final void describeMismatch(Object item, Description description) {
      if (item == null) {
         super.describeMismatch(item, description);
      } else if (!this.expectedType.isInstance(item)) {
         description.appendText("was a ").appendText(item.getClass().getName()).appendText(" (").appendValue(item).appendText(")");
      } else {
         this.describeMismatchSafely(item, description);
      }

   }
}

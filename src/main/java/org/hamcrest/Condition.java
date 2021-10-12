package org.hamcrest;

public abstract class Condition<T> {
   public static final Condition.NotMatched<Object> NOT_MATCHED = new Condition.NotMatched();

   private Condition() {
   }

   public abstract boolean matching(Matcher<T> var1, String var2);

   public abstract <U> Condition<U> and(Condition.Step<? super T, U> var1);

   public final boolean matching(Matcher<T> match) {
      return this.matching(match, "");
   }

   public final <U> Condition<U> then(Condition.Step<? super T, U> mapping) {
      return this.and(mapping);
   }

   public static <T> Condition<T> notMatched() {
      return NOT_MATCHED;
   }

   public static <T> Condition<T> matched(T theValue, Description mismatch) {
      return new Condition.Matched(theValue, mismatch);
   }

   // $FF: synthetic method
   Condition(Object x0) {
      this();
   }

   private static final class NotMatched<T> extends Condition<T> {
      private NotMatched() {
         super(null);
      }

      public boolean matching(Matcher<T> match, String message) {
         return false;
      }

      public <U> Condition<U> and(Condition.Step<? super T, U> mapping) {
         return notMatched();
      }

      // $FF: synthetic method
      NotMatched(Object x0) {
         this();
      }
   }

   private static final class Matched<T> extends Condition<T> {
      private final T theValue;
      private final Description mismatch;

      private Matched(T theValue, Description mismatch) {
         super(null);
         this.theValue = theValue;
         this.mismatch = mismatch;
      }

      public boolean matching(Matcher<T> matcher, String message) {
         if (matcher.matches(this.theValue)) {
            return true;
         } else {
            this.mismatch.appendText(message);
            matcher.describeMismatch(this.theValue, this.mismatch);
            return false;
         }
      }

      public <U> Condition<U> and(Condition.Step<? super T, U> next) {
         return next.apply(this.theValue, this.mismatch);
      }

      // $FF: synthetic method
      Matched(Object x0, Description x1, Object x2) {
         this(x0, x1);
      }
   }

   public interface Step<I, O> {
      Condition<O> apply(I var1, Description var2);
   }
}

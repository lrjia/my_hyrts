package org.junit.rules;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Assert;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.matchers.ThrowableCauseMatcher;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ExpectedException implements TestRule {
   private final ExpectedExceptionMatcherBuilder fMatcherBuilder = new ExpectedExceptionMatcherBuilder();
   private boolean handleAssumptionViolatedExceptions = false;
   private boolean handleAssertionErrors = false;

   public static ExpectedException none() {
      return new ExpectedException();
   }

   private ExpectedException() {
   }

   public ExpectedException handleAssertionErrors() {
      this.handleAssertionErrors = true;
      return this;
   }

   public ExpectedException handleAssumptionViolatedExceptions() {
      this.handleAssumptionViolatedExceptions = true;
      return this;
   }

   public Statement apply(Statement base, Description description) {
      return new ExpectedException.ExpectedExceptionStatement(base);
   }

   public void expect(Matcher<?> matcher) {
      this.fMatcherBuilder.add(matcher);
   }

   public void expect(Class<? extends Throwable> type) {
      this.expect(CoreMatchers.instanceOf(type));
   }

   public void expectMessage(String substring) {
      this.expectMessage(CoreMatchers.containsString(substring));
   }

   public void expectMessage(Matcher<String> matcher) {
      this.expect(ThrowableMessageMatcher.hasMessage(matcher));
   }

   public void expectCause(Matcher<? extends Throwable> expectedCause) {
      this.expect(ThrowableCauseMatcher.hasCause(expectedCause));
   }

   private void failDueToMissingException() throws AssertionError {
      String expectation = StringDescription.toString(this.fMatcherBuilder.build());
      Assert.fail("Expected test to throw " + expectation);
   }

   private void optionallyHandleException(Throwable e, boolean handleException) throws Throwable {
      if (handleException) {
         this.handleException(e);
      } else {
         throw e;
      }
   }

   private void handleException(Throwable e) throws Throwable {
      if (this.fMatcherBuilder.expectsThrowable()) {
         Assert.assertThat(e, this.fMatcherBuilder.build());
      } else {
         throw e;
      }
   }

   private class ExpectedExceptionStatement extends Statement {
      private final Statement fNext;

      public ExpectedExceptionStatement(Statement base) {
         this.fNext = base;
      }

      public void evaluate() throws Throwable {
         try {
            this.fNext.evaluate();
            if (ExpectedException.this.fMatcherBuilder.expectsThrowable()) {
               ExpectedException.this.failDueToMissingException();
            }
         } catch (AssumptionViolatedException var2) {
            ExpectedException.this.optionallyHandleException(var2, ExpectedException.this.handleAssumptionViolatedExceptions);
         } catch (AssertionError var3) {
            ExpectedException.this.optionallyHandleException(var3, ExpectedException.this.handleAssertionErrors);
         } catch (Throwable var4) {
            ExpectedException.this.handleException(var4);
         }

      }
   }
}

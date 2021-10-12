package org.junit.internal.runners.statements;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.model.Statement;

public class ExpectException extends Statement {
   private Statement fNext;
   private final Class<? extends Throwable> fExpected;

   public ExpectException(Statement next, Class<? extends Throwable> expected) {
      this.fNext = next;
      this.fExpected = expected;
   }

   public void evaluate() throws Exception {
      boolean complete = false;

      try {
         this.fNext.evaluate();
         complete = true;
      } catch (AssumptionViolatedException var4) {
         throw var4;
      } catch (Throwable var5) {
         if (!this.fExpected.isAssignableFrom(var5.getClass())) {
            String message = "Unexpected exception, expected<" + this.fExpected.getName() + "> but was<" + var5.getClass().getName() + ">";
            throw new Exception(message, var5);
         }
      }

      if (complete) {
         throw new AssertionError("Expected exception: " + this.fExpected.getName());
      }
   }
}

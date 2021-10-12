package org.junit.experimental.results;

import java.util.Iterator;
import java.util.List;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

class FailureList {
   private final List<Failure> failures;

   public FailureList(List<Failure> failures) {
      this.failures = failures;
   }

   public Result result() {
      Result result = new Result();
      RunListener listener = result.createListener();
      Iterator i$ = this.failures.iterator();

      while(i$.hasNext()) {
         Failure failure = (Failure)i$.next();

         try {
            listener.testFailure(failure);
         } catch (Exception var6) {
            throw new RuntimeException("I can't believe this happened");
         }
      }

      return result;
   }
}

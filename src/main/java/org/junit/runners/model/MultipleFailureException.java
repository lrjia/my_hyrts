package org.junit.runners.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MultipleFailureException extends Exception {
   private static final long serialVersionUID = 1L;
   private final List<Throwable> fErrors;

   public MultipleFailureException(List<Throwable> errors) {
      this.fErrors = new ArrayList(errors);
   }

   public List<Throwable> getFailures() {
      return Collections.unmodifiableList(this.fErrors);
   }

   public String getMessage() {
      StringBuilder sb = new StringBuilder(String.format("There were %d errors:", this.fErrors.size()));
      Iterator i$ = this.fErrors.iterator();

      while(i$.hasNext()) {
         Throwable e = (Throwable)i$.next();
         sb.append(String.format("\n  %s(%s)", e.getClass().getName(), e.getMessage()));
      }

      return sb.toString();
   }

   public static void assertEmpty(List<Throwable> errors) throws Throwable {
      if (!errors.isEmpty()) {
         if (errors.size() == 1) {
            throw (Throwable)errors.get(0);
         } else {
            throw new org.junit.internal.runners.model.MultipleFailureException(errors);
         }
      }
   }
}

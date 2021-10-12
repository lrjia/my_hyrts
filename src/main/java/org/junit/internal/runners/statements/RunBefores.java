package org.junit.internal.runners.statements;

import java.util.Iterator;
import java.util.List;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RunBefores extends Statement {
   private final Statement fNext;
   private final Object fTarget;
   private final List<FrameworkMethod> fBefores;

   public RunBefores(Statement next, List<FrameworkMethod> befores, Object target) {
      this.fNext = next;
      this.fBefores = befores;
      this.fTarget = target;
   }

   public void evaluate() throws Throwable {
      Iterator i$ = this.fBefores.iterator();

      while(i$.hasNext()) {
         FrameworkMethod before = (FrameworkMethod)i$.next();
         before.invokeExplosively(this.fTarget);
      }

      this.fNext.evaluate();
   }
}

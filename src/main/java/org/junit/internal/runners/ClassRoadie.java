package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/** @deprecated */
@Deprecated
public class ClassRoadie {
   private RunNotifier fNotifier;
   private TestClass fTestClass;
   private Description fDescription;
   private final Runnable fRunnable;

   public ClassRoadie(RunNotifier notifier, TestClass testClass, Description description, Runnable runnable) {
      this.fNotifier = notifier;
      this.fTestClass = testClass;
      this.fDescription = description;
      this.fRunnable = runnable;
   }

   protected void runUnprotected() {
      this.fRunnable.run();
   }

   protected void addFailure(Throwable targetException) {
      this.fNotifier.fireTestFailure(new Failure(this.fDescription, targetException));
   }

   public void runProtected() {
      try {
         this.runBefores();
         this.runUnprotected();
      } catch (FailedBefore var5) {
      } finally {
         this.runAfters();
      }

   }

   private void runBefores() throws FailedBefore {
      try {
         try {
            List<Method> befores = this.fTestClass.getBefores();
            Iterator i$ = befores.iterator();

            while(i$.hasNext()) {
               Method before = (Method)i$.next();
               before.invoke((Object)null);
            }

         } catch (InvocationTargetException var4) {
            throw var4.getTargetException();
         }
      } catch (AssumptionViolatedException var5) {
         throw new FailedBefore();
      } catch (Throwable var6) {
         this.addFailure(var6);
         throw new FailedBefore();
      }
   }

   private void runAfters() {
      List<Method> afters = this.fTestClass.getAfters();
      Iterator i$ = afters.iterator();

      while(i$.hasNext()) {
         Method after = (Method)i$.next();

         try {
            after.invoke((Object)null);
         } catch (InvocationTargetException var5) {
            this.addFailure(var5.getTargetException());
         } catch (Throwable var6) {
            this.addFailure(var6);
         }
      }

   }
}

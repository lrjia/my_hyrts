package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/** @deprecated */
@Deprecated
public class MethodRoadie {
   private final Object fTest;
   private final RunNotifier fNotifier;
   private final Description fDescription;
   private TestMethod fTestMethod;

   public MethodRoadie(Object test, TestMethod method, RunNotifier notifier, Description description) {
      this.fTest = test;
      this.fNotifier = notifier;
      this.fDescription = description;
      this.fTestMethod = method;
   }

   public void run() {
      if (this.fTestMethod.isIgnored()) {
         this.fNotifier.fireTestIgnored(this.fDescription);
      } else {
         this.fNotifier.fireTestStarted(this.fDescription);

         try {
            long timeout = this.fTestMethod.getTimeout();
            if (timeout > 0L) {
               this.runWithTimeout(timeout);
            } else {
               this.runTest();
            }
         } finally {
            this.fNotifier.fireTestFinished(this.fDescription);
         }

      }
   }

   private void runWithTimeout(final long timeout) {
      this.runBeforesThenTestThenAfters(new Runnable() {
         public void run() {
            ExecutorService service = Executors.newSingleThreadExecutor();
            Callable<Object> callable = new Callable<Object>() {
               public Object call() throws Exception {
                  MethodRoadie.this.runTestMethod();
                  return null;
               }
            };
            Future<Object> result = service.submit(callable);
            service.shutdown();

            try {
               boolean terminated = service.awaitTermination(timeout, TimeUnit.MILLISECONDS);
               if (!terminated) {
                  service.shutdownNow();
               }

               result.get(0L, TimeUnit.MILLISECONDS);
            } catch (TimeoutException var5) {
               MethodRoadie.this.addFailure(new Exception(String.format("test timed out after %d milliseconds", timeout)));
            } catch (Exception var6) {
               MethodRoadie.this.addFailure(var6);
            }

         }
      });
   }

   public void runTest() {
      this.runBeforesThenTestThenAfters(new Runnable() {
         public void run() {
            MethodRoadie.this.runTestMethod();
         }
      });
   }

   public void runBeforesThenTestThenAfters(Runnable test) {
      try {
         this.runBefores();
         test.run();
      } catch (FailedBefore var7) {
      } catch (Exception var8) {
         throw new RuntimeException("test should never throw an exception to this level");
      } finally {
         this.runAfters();
      }

   }

   protected void runTestMethod() {
      try {
         this.fTestMethod.invoke(this.fTest);
         if (this.fTestMethod.expectsException()) {
            this.addFailure(new AssertionError("Expected exception: " + this.fTestMethod.getExpectedException().getName()));
         }
      } catch (InvocationTargetException var4) {
         Throwable actual = var4.getTargetException();
         if (actual instanceof AssumptionViolatedException) {
            return;
         }

         if (!this.fTestMethod.expectsException()) {
            this.addFailure(actual);
         } else if (this.fTestMethod.isUnexpected(actual)) {
            String message = "Unexpected exception, expected<" + this.fTestMethod.getExpectedException().getName() + "> but was<" + actual.getClass().getName() + ">";
            this.addFailure(new Exception(message, actual));
         }
      } catch (Throwable var5) {
         this.addFailure(var5);
      }

   }

   private void runBefores() throws FailedBefore {
      try {
         try {
            List<Method> befores = this.fTestMethod.getBefores();
            Iterator i$ = befores.iterator();

            while(i$.hasNext()) {
               Method before = (Method)i$.next();
               before.invoke(this.fTest);
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
      List<Method> afters = this.fTestMethod.getAfters();
      Iterator i$ = afters.iterator();

      while(i$.hasNext()) {
         Method after = (Method)i$.next();

         try {
            after.invoke(this.fTest);
         } catch (InvocationTargetException var5) {
            this.addFailure(var5.getTargetException());
         } catch (Throwable var6) {
            this.addFailure(var6);
         }
      }

   }

   protected void addFailure(Throwable e) {
      this.fNotifier.fireTestFailure(new Failure(this.fDescription, e));
   }
}

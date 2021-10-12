package org.junit.runner.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Result;

public class RunNotifier {
   private final List<RunListener> fListeners = Collections.synchronizedList(new ArrayList());
   private volatile boolean fPleaseStop = false;

   public void addListener(RunListener listener) {
      this.fListeners.add(listener);
   }

   public void removeListener(RunListener listener) {
      this.fListeners.remove(listener);
   }

   public void fireTestRunStarted(final Description description) {
      (new RunNotifier.SafeNotifier() {
         protected void notifyListener(RunListener each) throws Exception {
            each.testRunStarted(description);
         }
      }).run();
   }

   public void fireTestRunFinished(final Result result) {
      (new RunNotifier.SafeNotifier() {
         protected void notifyListener(RunListener each) throws Exception {
            each.testRunFinished(result);
         }
      }).run();
   }

   public void fireTestStarted(final Description description) throws StoppedByUserException {
      if (this.fPleaseStop) {
         throw new StoppedByUserException();
      } else {
         (new RunNotifier.SafeNotifier() {
            protected void notifyListener(RunListener each) throws Exception {
               each.testStarted(description);
            }
         }).run();
      }
   }

   public void fireTestFailure(Failure failure) {
      this.fireTestFailures(this.fListeners, Arrays.asList(failure));
   }

   private void fireTestFailures(List<RunListener> listeners, final List<Failure> failures) {
      if (!failures.isEmpty()) {
         (new RunNotifier.SafeNotifier(listeners) {
            protected void notifyListener(RunListener listener) throws Exception {
               Iterator i$ = failures.iterator();

               while(i$.hasNext()) {
                  Failure each = (Failure)i$.next();
                  listener.testFailure(each);
               }

            }
         }).run();
      }

   }

   public void fireTestAssumptionFailed(final Failure failure) {
      (new RunNotifier.SafeNotifier() {
         protected void notifyListener(RunListener each) throws Exception {
            each.testAssumptionFailure(failure);
         }
      }).run();
   }

   public void fireTestIgnored(final Description description) {
      (new RunNotifier.SafeNotifier() {
         protected void notifyListener(RunListener each) throws Exception {
            each.testIgnored(description);
         }
      }).run();
   }

   public void fireTestFinished(final Description description) {
      (new RunNotifier.SafeNotifier() {
         protected void notifyListener(RunListener each) throws Exception {
            each.testFinished(description);
         }
      }).run();
   }

   public void pleaseStop() {
      this.fPleaseStop = true;
   }

   public void addFirstListener(RunListener listener) {
      this.fListeners.add(0, listener);
   }

   private abstract class SafeNotifier {
      private final List<RunListener> fCurrentListeners;

      SafeNotifier() {
         this(RunNotifier.this.fListeners);
      }

      SafeNotifier(List<RunListener> currentListeners) {
         this.fCurrentListeners = currentListeners;
      }

      void run() {
         synchronized(RunNotifier.this.fListeners) {
            List<RunListener> safeListeners = new ArrayList();
            List<Failure> failures = new ArrayList();
            Iterator all = this.fCurrentListeners.iterator();

            while(all.hasNext()) {
               try {
                  RunListener listener = (RunListener)all.next();
                  this.notifyListener(listener);
                  safeListeners.add(listener);
               } catch (Exception var7) {
                  failures.add(new Failure(Description.TEST_MECHANISM, var7));
               }
            }

            RunNotifier.this.fireTestFailures(safeListeners, failures);
         }
      }

      protected abstract void notifyListener(RunListener var1) throws Exception;
   }
}

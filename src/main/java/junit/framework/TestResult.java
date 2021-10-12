package junit.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class TestResult {
   protected List<TestFailure> fFailures = new ArrayList();
   protected List<TestFailure> fErrors = new ArrayList();
   protected List<TestListener> fListeners = new ArrayList();
   protected int fRunTests = 0;
   private boolean fStop = false;

   public synchronized void addError(Test test, Throwable t) {
      this.fErrors.add(new TestFailure(test, t));
      Iterator i$ = this.cloneListeners().iterator();

      while(i$.hasNext()) {
         TestListener each = (TestListener)i$.next();
         each.addError(test, t);
      }

   }

   public synchronized void addFailure(Test test, AssertionFailedError t) {
      this.fFailures.add(new TestFailure(test, t));
      Iterator i$ = this.cloneListeners().iterator();

      while(i$.hasNext()) {
         TestListener each = (TestListener)i$.next();
         each.addFailure(test, t);
      }

   }

   public synchronized void addListener(TestListener listener) {
      this.fListeners.add(listener);
   }

   public synchronized void removeListener(TestListener listener) {
      this.fListeners.remove(listener);
   }

   private synchronized List<TestListener> cloneListeners() {
      List<TestListener> result = new ArrayList();
      result.addAll(this.fListeners);
      return result;
   }

   public void endTest(Test test) {
      Iterator i$ = this.cloneListeners().iterator();

      while(i$.hasNext()) {
         TestListener each = (TestListener)i$.next();
         each.endTest(test);
      }

   }

   public synchronized int errorCount() {
      return this.fErrors.size();
   }

   public synchronized Enumeration<TestFailure> errors() {
      return Collections.enumeration(this.fErrors);
   }

   public synchronized int failureCount() {
      return this.fFailures.size();
   }

   public synchronized Enumeration<TestFailure> failures() {
      return Collections.enumeration(this.fFailures);
   }

   protected void run(final TestCase test) {
      this.startTest(test);
      Protectable p = new Protectable() {
         public void protect() throws Throwable {
            test.runBare();
         }
      };
      this.runProtected(test, p);
      this.endTest(test);
   }

   public synchronized int runCount() {
      return this.fRunTests;
   }

   public void runProtected(Test test, Protectable p) {
      try {
         p.protect();
      } catch (AssertionFailedError var4) {
         this.addFailure(test, var4);
      } catch (ThreadDeath var5) {
         throw var5;
      } catch (Throwable var6) {
         this.addError(test, var6);
      }

   }

   public synchronized boolean shouldStop() {
      return this.fStop;
   }

   public void startTest(Test test) {
      int count = test.countTestCases();
      synchronized(this) {
         this.fRunTests += count;
      }

      Iterator i$ = this.cloneListeners().iterator();

      while(i$.hasNext()) {
         TestListener each = (TestListener)i$.next();
         each.startTest(test);
      }

   }

   public synchronized void stop() {
      this.fStop = true;
   }

   public synchronized boolean wasSuccessful() {
      return this.failureCount() == 0 && this.errorCount() == 0;
   }
}

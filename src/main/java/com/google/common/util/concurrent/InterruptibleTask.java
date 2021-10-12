package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Level;
import java.util.logging.Logger;

@GwtCompatible(
   emulated = true
)
abstract class InterruptibleTask implements Runnable {
   private volatile Thread runner;
   private volatile boolean doneInterrupting;
   private static final InterruptibleTask.AtomicHelper ATOMIC_HELPER;
   private static final Logger log = Logger.getLogger(InterruptibleTask.class.getName());

   public final void run() {
      if (ATOMIC_HELPER.compareAndSetRunner(this, (Thread)null, Thread.currentThread())) {
         try {
            this.runInterruptibly();
         } finally {
            if (this.wasInterrupted()) {
               while(!this.doneInterrupting) {
                  Thread.yield();
               }
            }

         }

      }
   }

   abstract void runInterruptibly();

   abstract boolean wasInterrupted();

   final void interruptTask() {
      Thread currentRunner = this.runner;
      if (currentRunner != null) {
         currentRunner.interrupt();
      }

      this.doneInterrupting = true;
   }

   static {
      Object helper;
      try {
         helper = new InterruptibleTask.SafeAtomicHelper(AtomicReferenceFieldUpdater.newUpdater(InterruptibleTask.class, Thread.class, "runner"));
      } catch (Throwable var2) {
         log.log(Level.SEVERE, "SafeAtomicHelper is broken!", var2);
         helper = new InterruptibleTask.SynchronizedAtomicHelper();
      }

      ATOMIC_HELPER = (InterruptibleTask.AtomicHelper)helper;
   }

   private static final class SynchronizedAtomicHelper extends InterruptibleTask.AtomicHelper {
      private SynchronizedAtomicHelper() {
         super(null);
      }

      boolean compareAndSetRunner(InterruptibleTask task, Thread expect, Thread update) {
         synchronized(task) {
            if (task.runner == expect) {
               task.runner = update;
            }

            return true;
         }
      }

      // $FF: synthetic method
      SynchronizedAtomicHelper(Object x0) {
         this();
      }
   }

   private static final class SafeAtomicHelper extends InterruptibleTask.AtomicHelper {
      final AtomicReferenceFieldUpdater<InterruptibleTask, Thread> runnerUpdater;

      SafeAtomicHelper(AtomicReferenceFieldUpdater runnerUpdater) {
         super(null);
         this.runnerUpdater = runnerUpdater;
      }

      boolean compareAndSetRunner(InterruptibleTask task, Thread expect, Thread update) {
         return this.runnerUpdater.compareAndSet(task, expect, update);
      }
   }

   private abstract static class AtomicHelper {
      private AtomicHelper() {
      }

      abstract boolean compareAndSetRunner(InterruptibleTask var1, Thread var2, Thread var3);

      // $FF: synthetic method
      AtomicHelper(Object x0) {
         this();
      }
   }
}

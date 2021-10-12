package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Level;
import java.util.logging.Logger;

@GwtCompatible(
   emulated = true
)
abstract class AggregateFutureState {
   private volatile Set<Throwable> seenExceptions = null;
   private volatile int remaining;
   private static final AggregateFutureState.AtomicHelper ATOMIC_HELPER;
   private static final Logger log = Logger.getLogger(AggregateFutureState.class.getName());

   AggregateFutureState(int remainingFutures) {
      this.remaining = remainingFutures;
   }

   final Set<Throwable> getOrInitSeenExceptions() {
      Set<Throwable> seenExceptionsLocal = this.seenExceptions;
      if (seenExceptionsLocal == null) {
         seenExceptionsLocal = Sets.newConcurrentHashSet();
         this.addInitialException(seenExceptionsLocal);
         ATOMIC_HELPER.compareAndSetSeenExceptions(this, (Set)null, seenExceptionsLocal);
         seenExceptionsLocal = this.seenExceptions;
      }

      return seenExceptionsLocal;
   }

   abstract void addInitialException(Set<Throwable> var1);

   final int decrementRemainingAndGet() {
      return ATOMIC_HELPER.decrementAndGetRemainingCount(this);
   }

   static {
      Object helper;
      try {
         helper = new AggregateFutureState.SafeAtomicHelper(AtomicReferenceFieldUpdater.newUpdater(AggregateFutureState.class, Set.class, "seenExceptions"), AtomicIntegerFieldUpdater.newUpdater(AggregateFutureState.class, "remaining"));
      } catch (Throwable var2) {
         log.log(Level.SEVERE, "SafeAtomicHelper is broken!", var2);
         helper = new AggregateFutureState.SynchronizedAtomicHelper();
      }

      ATOMIC_HELPER = (AggregateFutureState.AtomicHelper)helper;
   }

   private static final class SynchronizedAtomicHelper extends AggregateFutureState.AtomicHelper {
      private SynchronizedAtomicHelper() {
         super(null);
      }

      void compareAndSetSeenExceptions(AggregateFutureState state, Set<Throwable> expect, Set<Throwable> update) {
         synchronized(state) {
            if (state.seenExceptions == expect) {
               state.seenExceptions = update;
            }

         }
      }

      int decrementAndGetRemainingCount(AggregateFutureState state) {
         synchronized(state) {
            state.remaining--;
            return state.remaining;
         }
      }

      // $FF: synthetic method
      SynchronizedAtomicHelper(Object x0) {
         this();
      }
   }

   private static final class SafeAtomicHelper extends AggregateFutureState.AtomicHelper {
      final AtomicReferenceFieldUpdater<AggregateFutureState, Set<Throwable>> seenExceptionsUpdater;
      final AtomicIntegerFieldUpdater<AggregateFutureState> remainingCountUpdater;

      SafeAtomicHelper(AtomicReferenceFieldUpdater seenExceptionsUpdater, AtomicIntegerFieldUpdater remainingCountUpdater) {
         super(null);
         this.seenExceptionsUpdater = seenExceptionsUpdater;
         this.remainingCountUpdater = remainingCountUpdater;
      }

      void compareAndSetSeenExceptions(AggregateFutureState state, Set<Throwable> expect, Set<Throwable> update) {
         this.seenExceptionsUpdater.compareAndSet(state, expect, update);
      }

      int decrementAndGetRemainingCount(AggregateFutureState state) {
         return this.remainingCountUpdater.decrementAndGet(state);
      }
   }

   private abstract static class AtomicHelper {
      private AtomicHelper() {
      }

      abstract void compareAndSetSeenExceptions(AggregateFutureState var1, Set<Throwable> var2, Set<Throwable> var3);

      abstract int decrementAndGetRemainingCount(AggregateFutureState var1);

      // $FF: synthetic method
      AtomicHelper(Object x0) {
         this();
      }
   }
}

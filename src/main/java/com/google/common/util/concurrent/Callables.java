package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;

@GwtCompatible(
   emulated = true
)
public final class Callables {
   private Callables() {
   }

   public static <T> Callable<T> returning(@Nullable final T value) {
      return new Callable<T>() {
         public T call() {
            return value;
         }
      };
   }

   @Beta
   @GwtIncompatible
   public static <T> AsyncCallable<T> asAsyncCallable(final Callable<T> callable, final ListeningExecutorService listeningExecutorService) {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(listeningExecutorService);
      return new AsyncCallable<T>() {
         public ListenableFuture<T> call() throws Exception {
            return listeningExecutorService.submit(callable);
         }
      };
   }

   @GwtIncompatible
   static <T> Callable<T> threadRenaming(final Callable<T> callable, final Supplier<String> nameSupplier) {
      Preconditions.checkNotNull(nameSupplier);
      Preconditions.checkNotNull(callable);
      return new Callable<T>() {
         public T call() throws Exception {
            Thread currentThread = Thread.currentThread();
            String oldName = currentThread.getName();
            boolean restoreName = Callables.trySetName((String)nameSupplier.get(), currentThread);
            boolean var9 = false;

            Object var4;
            try {
               var9 = true;
               var4 = callable.call();
               var9 = false;
            } finally {
               if (var9) {
                  if (restoreName) {
                     Callables.trySetName(oldName, currentThread);
                  }

               }
            }

            if (restoreName) {
               Callables.trySetName(oldName, currentThread);
            }

            return var4;
         }
      };
   }

   @GwtIncompatible
   static Runnable threadRenaming(final Runnable task, final Supplier<String> nameSupplier) {
      Preconditions.checkNotNull(nameSupplier);
      Preconditions.checkNotNull(task);
      return new Runnable() {
         public void run() {
            Thread currentThread = Thread.currentThread();
            String oldName = currentThread.getName();
            boolean restoreName = Callables.trySetName((String)nameSupplier.get(), currentThread);
            boolean var8 = false;

            try {
               var8 = true;
               task.run();
               var8 = false;
            } finally {
               if (var8) {
                  if (restoreName) {
                     Callables.trySetName(oldName, currentThread);
                  }

               }
            }

            if (restoreName) {
               Callables.trySetName(oldName, currentThread);
            }

         }
      };
   }

   @GwtIncompatible
   private static boolean trySetName(String threadName, Thread currentThread) {
      try {
         currentThread.setName(threadName);
         return true;
      } catch (SecurityException var3) {
         return false;
      }
   }
}

package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.ForOverride;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

@GwtCompatible
abstract class AbstractCatchingFuture<V, X extends Throwable, F, T> extends AbstractFuture.TrustedFuture<V> implements Runnable {
   @Nullable
   ListenableFuture<? extends V> inputFuture;
   @Nullable
   Class<X> exceptionType;
   @Nullable
   F fallback;

   static <X extends Throwable, V> ListenableFuture<V> create(ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback) {
      AbstractCatchingFuture.CatchingFuture<V, X> future = new AbstractCatchingFuture.CatchingFuture(input, exceptionType, fallback);
      input.addListener(future, MoreExecutors.directExecutor());
      return future;
   }

   static <V, X extends Throwable> ListenableFuture<V> create(ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback, Executor executor) {
      AbstractCatchingFuture.CatchingFuture<V, X> future = new AbstractCatchingFuture.CatchingFuture(input, exceptionType, fallback);
      input.addListener(future, MoreExecutors.rejectionPropagatingExecutor(executor, future));
      return future;
   }

   static <X extends Throwable, V> ListenableFuture<V> create(ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback) {
      AbstractCatchingFuture.AsyncCatchingFuture<V, X> future = new AbstractCatchingFuture.AsyncCatchingFuture(input, exceptionType, fallback);
      input.addListener(future, MoreExecutors.directExecutor());
      return future;
   }

   static <X extends Throwable, V> ListenableFuture<V> create(ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback, Executor executor) {
      AbstractCatchingFuture.AsyncCatchingFuture<V, X> future = new AbstractCatchingFuture.AsyncCatchingFuture(input, exceptionType, fallback);
      input.addListener(future, MoreExecutors.rejectionPropagatingExecutor(executor, future));
      return future;
   }

   AbstractCatchingFuture(ListenableFuture<? extends V> inputFuture, Class<X> exceptionType, F fallback) {
      this.inputFuture = (ListenableFuture)Preconditions.checkNotNull(inputFuture);
      this.exceptionType = (Class)Preconditions.checkNotNull(exceptionType);
      this.fallback = Preconditions.checkNotNull(fallback);
   }

   public final void run() {
      ListenableFuture<? extends V> localInputFuture = this.inputFuture;
      Class<X> localExceptionType = this.exceptionType;
      F localFallback = this.fallback;
      if (!(localInputFuture == null | localExceptionType == null | localFallback == null | this.isCancelled())) {
         this.inputFuture = null;
         this.exceptionType = null;
         this.fallback = null;
         V sourceResult = null;
         Throwable throwable = null;

         try {
            sourceResult = Futures.getDone(localInputFuture);
         } catch (ExecutionException var10) {
            throwable = (Throwable)Preconditions.checkNotNull(var10.getCause());
         } catch (Throwable var11) {
            throwable = var11;
         }

         if (throwable == null) {
            this.set(sourceResult);
         } else if (!Platform.isInstanceOfThrowableClass(throwable, localExceptionType)) {
            this.setException(throwable);
         } else {
            Throwable castThrowable = throwable;

            Object fallbackResult;
            try {
               fallbackResult = this.doFallback(localFallback, castThrowable);
            } catch (Throwable var9) {
               this.setException(var9);
               return;
            }

            this.setResult(fallbackResult);
         }
      }
   }

   @Nullable
   @ForOverride
   abstract T doFallback(F var1, X var2) throws Exception;

   @ForOverride
   abstract void setResult(@Nullable T var1);

   protected final void afterDone() {
      this.maybePropagateCancellation(this.inputFuture);
      this.inputFuture = null;
      this.exceptionType = null;
      this.fallback = null;
   }

   private static final class CatchingFuture<V, X extends Throwable> extends AbstractCatchingFuture<V, X, Function<? super X, ? extends V>, V> {
      CatchingFuture(ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback) {
         super(input, exceptionType, fallback);
      }

      @Nullable
      V doFallback(Function<? super X, ? extends V> fallback, X cause) throws Exception {
         return fallback.apply(cause);
      }

      void setResult(@Nullable V result) {
         this.set(result);
      }
   }

   private static final class AsyncCatchingFuture<V, X extends Throwable> extends AbstractCatchingFuture<V, X, AsyncFunction<? super X, ? extends V>, ListenableFuture<? extends V>> {
      AsyncCatchingFuture(ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback) {
         super(input, exceptionType, fallback);
      }

      ListenableFuture<? extends V> doFallback(AsyncFunction<? super X, ? extends V> fallback, X cause) throws Exception {
         ListenableFuture<? extends V> replacement = fallback.apply(cause);
         Preconditions.checkNotNull(replacement, "AsyncFunction.apply returned null instead of a Future. Did you mean to return immediateFuture(null)?");
         return replacement;
      }

      void setResult(ListenableFuture<? extends V> result) {
         this.setFuture(result);
      }
   }
}

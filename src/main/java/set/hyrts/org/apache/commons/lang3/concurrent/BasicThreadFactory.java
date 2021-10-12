package set.hyrts.org.apache.commons.lang3.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import set.hyrts.org.apache.commons.lang3.Validate;

public class BasicThreadFactory implements ThreadFactory {
   private final AtomicLong threadCounter;
   private final ThreadFactory wrappedFactory;
   private final UncaughtExceptionHandler uncaughtExceptionHandler;
   private final String namingPattern;
   private final Integer priority;
   private final Boolean daemonFlag;

   private BasicThreadFactory(BasicThreadFactory.Builder builder) {
      if (builder.wrappedFactory == null) {
         this.wrappedFactory = Executors.defaultThreadFactory();
      } else {
         this.wrappedFactory = builder.wrappedFactory;
      }

      this.namingPattern = builder.namingPattern;
      this.priority = builder.priority;
      this.daemonFlag = builder.daemonFlag;
      this.uncaughtExceptionHandler = builder.exceptionHandler;
      this.threadCounter = new AtomicLong();
   }

   public final ThreadFactory getWrappedFactory() {
      return this.wrappedFactory;
   }

   public final String getNamingPattern() {
      return this.namingPattern;
   }

   public final Boolean getDaemonFlag() {
      return this.daemonFlag;
   }

   public final Integer getPriority() {
      return this.priority;
   }

   public final UncaughtExceptionHandler getUncaughtExceptionHandler() {
      return this.uncaughtExceptionHandler;
   }

   public long getThreadCount() {
      return this.threadCounter.get();
   }

   public Thread newThread(Runnable r) {
      Thread t = this.getWrappedFactory().newThread(r);
      this.initializeThread(t);
      return t;
   }

   private void initializeThread(Thread t) {
      if (this.getNamingPattern() != null) {
         Long count = this.threadCounter.incrementAndGet();
         t.setName(String.format(this.getNamingPattern(), count));
      }

      if (this.getUncaughtExceptionHandler() != null) {
         t.setUncaughtExceptionHandler(this.getUncaughtExceptionHandler());
      }

      if (this.getPriority() != null) {
         t.setPriority(this.getPriority());
      }

      if (this.getDaemonFlag() != null) {
         t.setDaemon(this.getDaemonFlag());
      }

   }

   // $FF: synthetic method
   BasicThreadFactory(BasicThreadFactory.Builder x0, Object x1) {
      this(x0);
   }

   public static class Builder implements set.hyrts.org.apache.commons.lang3.builder.Builder<BasicThreadFactory> {
      private ThreadFactory wrappedFactory;
      private UncaughtExceptionHandler exceptionHandler;
      private String namingPattern;
      private Integer priority;
      private Boolean daemonFlag;

      public BasicThreadFactory.Builder wrappedFactory(ThreadFactory factory) {
         Validate.notNull(factory, "Wrapped ThreadFactory must not be null!");
         this.wrappedFactory = factory;
         return this;
      }

      public BasicThreadFactory.Builder namingPattern(String pattern) {
         Validate.notNull(pattern, "Naming pattern must not be null!");
         this.namingPattern = pattern;
         return this;
      }

      public BasicThreadFactory.Builder daemon(boolean f) {
         this.daemonFlag = f;
         return this;
      }

      public BasicThreadFactory.Builder priority(int prio) {
         this.priority = prio;
         return this;
      }

      public BasicThreadFactory.Builder uncaughtExceptionHandler(UncaughtExceptionHandler handler) {
         Validate.notNull(handler, "Uncaught exception handler must not be null!");
         this.exceptionHandler = handler;
         return this;
      }

      public void reset() {
         this.wrappedFactory = null;
         this.exceptionHandler = null;
         this.namingPattern = null;
         this.priority = null;
         this.daemonFlag = null;
      }

      public BasicThreadFactory build() {
         BasicThreadFactory factory = new BasicThreadFactory(this);
         this.reset();
         return factory;
      }
   }
}

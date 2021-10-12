package set.hyrts.org.apache.log4j.varia;

import set.hyrts.org.apache.log4j.AppenderSkeleton;
import set.hyrts.org.apache.log4j.spi.LoggingEvent;

public class NullAppender extends AppenderSkeleton {
   private static NullAppender instance = new NullAppender();

   public void activateOptions() {
   }

   /** @deprecated */
   public NullAppender getInstance() {
      return instance;
   }

   public static NullAppender getNullAppender() {
      return instance;
   }

   public void close() {
   }

   public void doAppend(LoggingEvent event) {
   }

   protected void append(LoggingEvent event) {
   }

   public boolean requiresLayout() {
      return false;
   }
}

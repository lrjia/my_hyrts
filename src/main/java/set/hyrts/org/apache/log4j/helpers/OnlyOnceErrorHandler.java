package set.hyrts.org.apache.log4j.helpers;

import java.io.InterruptedIOException;
import set.hyrts.org.apache.log4j.Appender;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.org.apache.log4j.spi.ErrorHandler;
import set.hyrts.org.apache.log4j.spi.LoggingEvent;

public class OnlyOnceErrorHandler implements ErrorHandler {
   final String WARN_PREFIX = "log4j warning: ";
   final String ERROR_PREFIX = "log4j error: ";
   boolean firstTime = true;

   public void setLogger(Logger logger) {
   }

   public void activateOptions() {
   }

   public void error(String message, Exception e, int errorCode) {
      this.error(message, e, errorCode, (LoggingEvent)null);
   }

   public void error(String message, Exception e, int errorCode, LoggingEvent event) {
      if (e instanceof InterruptedIOException || e instanceof InterruptedException) {
         Thread.currentThread().interrupt();
      }

      if (this.firstTime) {
         LogLog.error(message, e);
         this.firstTime = false;
      }

   }

   public void error(String message) {
      if (this.firstTime) {
         LogLog.error(message);
         this.firstTime = false;
      }

   }

   public void setAppender(Appender appender) {
   }

   public void setBackupAppender(Appender appender) {
   }
}

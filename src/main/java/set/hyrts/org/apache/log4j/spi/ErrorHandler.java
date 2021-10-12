package set.hyrts.org.apache.log4j.spi;

import set.hyrts.org.apache.log4j.Appender;
import set.hyrts.org.apache.log4j.Logger;

public interface ErrorHandler extends OptionHandler {
   void setLogger(Logger var1);

   void error(String var1, Exception var2, int var3);

   void error(String var1);

   void error(String var1, Exception var2, int var3, LoggingEvent var4);

   void setAppender(Appender var1);

   void setBackupAppender(Appender var1);
}

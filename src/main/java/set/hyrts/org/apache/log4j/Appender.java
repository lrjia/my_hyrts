package set.hyrts.org.apache.log4j;

import set.hyrts.org.apache.log4j.spi.ErrorHandler;
import set.hyrts.org.apache.log4j.spi.Filter;
import set.hyrts.org.apache.log4j.spi.LoggingEvent;

public interface Appender {
   void addFilter(Filter var1);

   Filter getFilter();

   void clearFilters();

   void close();

   void doAppend(LoggingEvent var1);

   String getName();

   void setErrorHandler(ErrorHandler var1);

   ErrorHandler getErrorHandler();

   void setLayout(Layout var1);

   Layout getLayout();

   void setName(String var1);

   boolean requiresLayout();
}

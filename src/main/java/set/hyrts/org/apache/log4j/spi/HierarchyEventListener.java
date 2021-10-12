package set.hyrts.org.apache.log4j.spi;

import set.hyrts.org.apache.log4j.Appender;
import set.hyrts.org.apache.log4j.Category;

public interface HierarchyEventListener {
   void addAppenderEvent(Category var1, Appender var2);

   void removeAppenderEvent(Category var1, Appender var2);
}

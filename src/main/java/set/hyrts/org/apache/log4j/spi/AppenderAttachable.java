package set.hyrts.org.apache.log4j.spi;

import java.util.Enumeration;
import set.hyrts.org.apache.log4j.Appender;

public interface AppenderAttachable {
   void addAppender(Appender var1);

   Enumeration getAllAppenders();

   Appender getAppender(String var1);

   boolean isAttached(Appender var1);

   void removeAllAppenders();

   void removeAppender(Appender var1);

   void removeAppender(String var1);
}

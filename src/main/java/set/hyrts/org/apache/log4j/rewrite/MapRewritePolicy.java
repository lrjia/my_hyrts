package set.hyrts.org.apache.log4j.rewrite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import set.hyrts.org.apache.log4j.Category;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.org.apache.log4j.spi.LoggingEvent;

public class MapRewritePolicy implements RewritePolicy {
   public LoggingEvent rewrite(LoggingEvent source) {
      Object msg = source.getMessage();
      if (msg instanceof Map) {
         Map props = new HashMap(source.getProperties());
         Map eventProps = (Map)msg;
         Object newMsg = eventProps.get("message");
         if (newMsg == null) {
            newMsg = msg;
         }

         Iterator iter = eventProps.entrySet().iterator();

         while(iter.hasNext()) {
            Entry entry = (Entry)iter.next();
            if (!"message".equals(entry.getKey())) {
               props.put(entry.getKey(), entry.getValue());
            }
         }

         return new LoggingEvent(source.getFQNOfLoggerClass(), (Category)(source.getLogger() != null ? source.getLogger() : Logger.getLogger(source.getLoggerName())), source.getTimeStamp(), source.getLevel(), newMsg, source.getThreadName(), source.getThrowableInformation(), source.getNDC(), source.getLocationInformation(), props);
      } else {
         return source;
      }
   }
}

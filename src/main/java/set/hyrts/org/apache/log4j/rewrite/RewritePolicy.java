package set.hyrts.org.apache.log4j.rewrite;

import set.hyrts.org.apache.log4j.spi.LoggingEvent;

public interface RewritePolicy {
   LoggingEvent rewrite(LoggingEvent var1);
}

package set.hyrts.org.apache.log4j.net;

import set.hyrts.org.apache.log4j.Level;
import set.hyrts.org.apache.log4j.spi.LoggingEvent;
import set.hyrts.org.apache.log4j.spi.TriggeringEventEvaluator;

class DefaultEvaluator implements TriggeringEventEvaluator {
   public boolean isTriggeringEvent(LoggingEvent event) {
      return event.getLevel().isGreaterOrEqual(Level.ERROR);
   }
}

package set.hyrts.org.apache.log4j.varia;

import set.hyrts.org.apache.log4j.spi.Filter;
import set.hyrts.org.apache.log4j.spi.LoggingEvent;

public class DenyAllFilter extends Filter {
   /** @deprecated */
   public String[] getOptionStrings() {
      return null;
   }

   /** @deprecated */
   public void setOption(String key, String value) {
   }

   public int decide(LoggingEvent event) {
      return -1;
   }
}

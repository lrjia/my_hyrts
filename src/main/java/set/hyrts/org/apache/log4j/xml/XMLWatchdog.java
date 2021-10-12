package set.hyrts.org.apache.log4j.xml;

import set.hyrts.org.apache.log4j.LogManager;
import set.hyrts.org.apache.log4j.helpers.FileWatchdog;

class XMLWatchdog extends FileWatchdog {
   XMLWatchdog(String filename) {
      super(filename);
   }

   public void doOnChange() {
      (new DOMConfigurator()).doConfigure(this.filename, LogManager.getLoggerRepository());
   }
}

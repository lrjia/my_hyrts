package set.hyrts.org.apache.log4j;

import set.hyrts.org.apache.log4j.helpers.FileWatchdog;

class PropertyWatchdog extends FileWatchdog {
   PropertyWatchdog(String filename) {
      super(filename);
   }

   public void doOnChange() {
      (new PropertyConfigurator()).doConfigure(this.filename, LogManager.getLoggerRepository());
   }
}

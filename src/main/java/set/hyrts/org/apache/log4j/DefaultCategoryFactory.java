package set.hyrts.org.apache.log4j;

import set.hyrts.org.apache.log4j.spi.LoggerFactory;

class DefaultCategoryFactory implements LoggerFactory {
   public Logger makeNewLoggerInstance(String name) {
      return new Logger(name);
   }
}

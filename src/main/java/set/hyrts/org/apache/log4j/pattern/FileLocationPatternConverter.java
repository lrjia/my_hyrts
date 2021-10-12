package set.hyrts.org.apache.log4j.pattern;

import set.hyrts.org.apache.log4j.spi.LocationInfo;
import set.hyrts.org.apache.log4j.spi.LoggingEvent;

public final class FileLocationPatternConverter extends LoggingEventPatternConverter {
   private static final FileLocationPatternConverter INSTANCE = new FileLocationPatternConverter();

   private FileLocationPatternConverter() {
      super("File Location", "file");
   }

   public static FileLocationPatternConverter newInstance(String[] options) {
      return INSTANCE;
   }

   public void format(LoggingEvent event, StringBuffer output) {
      LocationInfo locationInfo = event.getLocationInformation();
      if (locationInfo != null) {
         output.append(locationInfo.getFileName());
      }

   }
}

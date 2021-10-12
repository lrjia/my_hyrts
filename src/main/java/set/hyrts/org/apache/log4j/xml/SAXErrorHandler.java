package set.hyrts.org.apache.log4j.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import set.hyrts.org.apache.log4j.helpers.LogLog;

public class SAXErrorHandler implements ErrorHandler {
   public void error(SAXParseException ex) {
      emitMessage("Continuable parsing error ", ex);
   }

   public void fatalError(SAXParseException ex) {
      emitMessage("Fatal parsing error ", ex);
   }

   public void warning(SAXParseException ex) {
      emitMessage("Parsing warning ", ex);
   }

   private static void emitMessage(String msg, SAXParseException ex) {
      LogLog.warn(msg + ex.getLineNumber() + " and column " + ex.getColumnNumber());
      LogLog.warn(ex.getMessage(), ex.getException());
   }
}

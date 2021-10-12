package set.hyrts.org.objectweb.asm.xml;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.ContentHandler;

final class Processor$TransformerHandlerFactory implements Processor$ContentHandlerFactory {
   private SAXTransformerFactory saxtf;
   private final Templates templates;
   private ContentHandler outputHandler;

   Processor$TransformerHandlerFactory(SAXTransformerFactory var1, Templates var2, ContentHandler var3) {
      this.saxtf = var1;
      this.templates = var2;
      this.outputHandler = var3;
   }

   public final ContentHandler createContentHandler() {
      try {
         TransformerHandler var1 = this.saxtf.newTransformerHandler(this.templates);
         var1.setResult(new SAXResult(this.outputHandler));
         return var1;
      } catch (TransformerConfigurationException var2) {
         throw new RuntimeException(var2.toString());
      }
   }
}

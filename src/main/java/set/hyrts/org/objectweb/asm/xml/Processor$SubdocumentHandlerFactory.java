package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.ContentHandler;

final class Processor$SubdocumentHandlerFactory implements Processor$ContentHandlerFactory {
   private final ContentHandler subdocumentHandler;

   Processor$SubdocumentHandlerFactory(ContentHandler var1) {
      this.subdocumentHandler = var1;
   }

   public final ContentHandler createContentHandler() {
      return this.subdocumentHandler;
   }
}

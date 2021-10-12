package set.hyrts.org.objectweb.asm.xml;

import java.io.Writer;
import org.xml.sax.ContentHandler;

final class Processor$SAXWriterFactory implements Processor$ContentHandlerFactory {
   private final Writer w;
   private final boolean optimizeEmptyElements;

   Processor$SAXWriterFactory(Writer var1, boolean var2) {
      this.w = var1;
      this.optimizeEmptyElements = var2;
   }

   public final ContentHandler createContentHandler() {
      return new Processor$SAXWriter(this.w, this.optimizeEmptyElements);
   }
}

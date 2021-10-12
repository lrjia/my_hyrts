package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

final class Processor$InputSlicingHandler extends DefaultHandler {
   private String subdocumentRoot;
   private final ContentHandler rootHandler;
   private Processor$ContentHandlerFactory subdocumentHandlerFactory;
   private boolean subdocument = false;
   private ContentHandler subdocumentHandler;

   Processor$InputSlicingHandler(String var1, ContentHandler var2, Processor$ContentHandlerFactory var3) {
      this.subdocumentRoot = var1;
      this.rootHandler = var2;
      this.subdocumentHandlerFactory = var3;
   }

   public final void startElement(String var1, String var2, String var3, Attributes var4) throws SAXException {
      if (this.subdocument) {
         this.subdocumentHandler.startElement(var1, var2, var3, var4);
      } else if (var2.equals(this.subdocumentRoot)) {
         this.subdocumentHandler = this.subdocumentHandlerFactory.createContentHandler();
         this.subdocumentHandler.startDocument();
         this.subdocumentHandler.startElement(var1, var2, var3, var4);
         this.subdocument = true;
      } else if (this.rootHandler != null) {
         this.rootHandler.startElement(var1, var2, var3, var4);
      }

   }

   public final void endElement(String var1, String var2, String var3) throws SAXException {
      if (this.subdocument) {
         this.subdocumentHandler.endElement(var1, var2, var3);
         if (var2.equals(this.subdocumentRoot)) {
            this.subdocumentHandler.endDocument();
            this.subdocument = false;
         }
      } else if (this.rootHandler != null) {
         this.rootHandler.endElement(var1, var2, var3);
      }

   }

   public final void startDocument() throws SAXException {
      if (this.rootHandler != null) {
         this.rootHandler.startDocument();
      }

   }

   public final void endDocument() throws SAXException {
      if (this.rootHandler != null) {
         this.rootHandler.endDocument();
      }

   }

   public final void characters(char[] var1, int var2, int var3) throws SAXException {
      if (this.subdocument) {
         this.subdocumentHandler.characters(var1, var2, var3);
      } else if (this.rootHandler != null) {
         this.rootHandler.characters(var1, var2, var3);
      }

   }
}

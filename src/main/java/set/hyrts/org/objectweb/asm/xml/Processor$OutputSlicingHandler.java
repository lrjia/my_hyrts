package set.hyrts.org.objectweb.asm.xml;

import java.io.IOException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

final class Processor$OutputSlicingHandler extends DefaultHandler {
   private final String subdocumentRoot = "class";
   private Processor$ContentHandlerFactory subdocumentHandlerFactory;
   private final Processor$EntryElement entryElement;
   private boolean isXml;
   private boolean subdocument = false;
   private ContentHandler subdocumentHandler;

   Processor$OutputSlicingHandler(Processor$ContentHandlerFactory var1, Processor$EntryElement var2, boolean var3) {
      this.subdocumentHandlerFactory = var1;
      this.entryElement = var2;
      this.isXml = var3;
   }

   public final void startElement(String var1, String var2, String var3, Attributes var4) throws SAXException {
      if (this.subdocument) {
         this.subdocumentHandler.startElement(var1, var2, var3, var4);
      } else if (var2.equals(this.subdocumentRoot)) {
         String var5 = var4.getValue("name");
         if (var5 == null || var5.length() == 0) {
            throw new SAXException("Class element without name attribute.");
         }

         try {
            this.entryElement.openEntry(this.isXml ? var5 + ".class.xml" : var5 + ".class");
         } catch (IOException var7) {
            throw new SAXException(var7.toString(), var7);
         }

         this.subdocumentHandler = this.subdocumentHandlerFactory.createContentHandler();
         this.subdocumentHandler.startDocument();
         this.subdocumentHandler.startElement(var1, var2, var3, var4);
         this.subdocument = true;
      }

   }

   public final void endElement(String var1, String var2, String var3) throws SAXException {
      if (this.subdocument) {
         this.subdocumentHandler.endElement(var1, var2, var3);
         if (var2.equals(this.subdocumentRoot)) {
            this.subdocumentHandler.endDocument();
            this.subdocument = false;

            try {
               this.entryElement.closeEntry();
            } catch (IOException var5) {
               throw new SAXException(var5.toString(), var5);
            }
         }
      }

   }

   public final void startDocument() throws SAXException {
   }

   public final void endDocument() throws SAXException {
   }

   public final void characters(char[] var1, int var2, int var3) throws SAXException {
      if (this.subdocument) {
         this.subdocumentHandler.characters(var1, var2, var3);
      }

   }
}

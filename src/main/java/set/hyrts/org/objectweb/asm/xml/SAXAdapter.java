package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class SAXAdapter {
   private final ContentHandler h;

   protected SAXAdapter(ContentHandler var1) {
      this.h = var1;
   }

   protected ContentHandler getContentHandler() {
      return this.h;
   }

   protected void addDocumentStart() {
      try {
         this.h.startDocument();
      } catch (SAXException var2) {
         throw new RuntimeException(var2.getMessage(), var2.getException());
      }
   }

   protected void addDocumentEnd() {
      try {
         this.h.endDocument();
      } catch (SAXException var2) {
         throw new RuntimeException(var2.getMessage(), var2.getException());
      }
   }

   protected final void addStart(String var1, Attributes var2) {
      try {
         this.h.startElement("", var1, var1, var2);
      } catch (SAXException var4) {
         throw new RuntimeException(var4.getMessage(), var4.getException());
      }
   }

   protected final void addEnd(String var1) {
      try {
         this.h.endElement("", var1, var1);
      } catch (SAXException var3) {
         throw new RuntimeException(var3.getMessage(), var3.getException());
      }
   }

   protected final void addElement(String var1, Attributes var2) {
      this.addStart(var1, var2);
      this.addEnd(var1);
   }
}

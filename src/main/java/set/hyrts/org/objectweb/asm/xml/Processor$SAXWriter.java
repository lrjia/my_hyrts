package set.hyrts.org.objectweb.asm.xml;

import java.io.IOException;
import java.io.Writer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

final class Processor$SAXWriter extends DefaultHandler implements LexicalHandler {
   private static final char[] OFF;
   private Writer w;
   private final boolean optimizeEmptyElements;
   private boolean openElement = false;
   private int ident = 0;

   Processor$SAXWriter(Writer var1, boolean var2) {
      this.w = var1;
      this.optimizeEmptyElements = var2;
   }

   public final void startElement(String var1, String var2, String var3, Attributes var4) throws SAXException {
      try {
         this.closeElement();
         this.writeIdent();
         this.w.write('<' + var3);
         if (var4 != null && var4.getLength() > 0) {
            this.writeAttributes(var4);
         }

         if (this.optimizeEmptyElements) {
            this.openElement = true;
         } else {
            this.w.write(">\n");
         }

         this.ident += 2;
      } catch (IOException var6) {
         throw new SAXException(var6);
      }
   }

   public final void endElement(String var1, String var2, String var3) throws SAXException {
      this.ident -= 2;

      try {
         if (this.openElement) {
            this.w.write("/>\n");
            this.openElement = false;
         } else {
            this.writeIdent();
            this.w.write("</" + var3 + ">\n");
         }

      } catch (IOException var5) {
         throw new SAXException(var5);
      }
   }

   public final void endDocument() throws SAXException {
      try {
         this.w.flush();
      } catch (IOException var2) {
         throw new SAXException(var2);
      }
   }

   public final void comment(char[] var1, int var2, int var3) throws SAXException {
      try {
         this.closeElement();
         this.writeIdent();
         this.w.write("<!-- ");
         this.w.write(var1, var2, var3);
         this.w.write(" -->\n");
      } catch (IOException var5) {
         throw new SAXException(var5);
      }
   }

   public final void startDTD(String var1, String var2, String var3) throws SAXException {
   }

   public final void endDTD() throws SAXException {
   }

   public final void startEntity(String var1) throws SAXException {
   }

   public final void endEntity(String var1) throws SAXException {
   }

   public final void startCDATA() throws SAXException {
   }

   public final void endCDATA() throws SAXException {
   }

   private final void writeAttributes(Attributes var1) throws IOException {
      StringBuffer var2 = new StringBuffer();
      int var3 = var1.getLength();

      for(int var4 = 0; var4 < var3; ++var4) {
         var2.append(' ').append(var1.getLocalName(var4)).append("=\"").append(esc(var1.getValue(var4))).append('"');
      }

      this.w.write(var2.toString());
   }

   private static final String esc(String var0) {
      StringBuffer var1 = new StringBuffer(var0.length());

      for(int var2 = 0; var2 < var0.length(); ++var2) {
         char var3 = var0.charAt(var2);
         switch(var3) {
         case '"':
            var1.append("&quot;");
            break;
         case '&':
            var1.append("&amp;");
            break;
         case '<':
            var1.append("&lt;");
            break;
         case '>':
            var1.append("&gt;");
            break;
         default:
            if (var3 > 127) {
               var1.append("&#").append(Integer.toString(var3)).append(';');
            } else {
               var1.append(var3);
            }
         }
      }

      return var1.toString();
   }

   private final void writeIdent() throws IOException {
      int var1 = this.ident;

      while(var1 > 0) {
         if (var1 > OFF.length) {
            this.w.write(OFF);
            var1 -= OFF.length;
         } else {
            this.w.write(OFF, 0, var1);
            var1 = 0;
         }
      }

   }

   private final void closeElement() throws IOException {
      if (this.openElement) {
         this.w.write(">\n");
      }

      this.openElement = false;
   }

   static {
      _clinit_();
      OFF = "                                                                                                        ".toCharArray();
   }

   // $FF: synthetic method
   static void _clinit_() {
   }
}

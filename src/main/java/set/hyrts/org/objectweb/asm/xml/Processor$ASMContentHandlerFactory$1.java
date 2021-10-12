package set.hyrts.org.objectweb.asm.xml;

import java.io.IOException;
import org.xml.sax.SAXException;
import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.ClassWriter;

class Processor$ASMContentHandlerFactory$1 extends ASMContentHandler {
   // $FF: synthetic field
   final ClassWriter val$cw;
   // $FF: synthetic field
   final Processor$ASMContentHandlerFactory this$0;

   Processor$ASMContentHandlerFactory$1(Processor$ASMContentHandlerFactory var1, ClassVisitor var2, ClassWriter var3) {
      super(var2);
      this.this$0 = var1;
      this.val$cw = var3;
   }

   public void endDocument() throws SAXException {
      try {
         this.this$0.os.write(this.val$cw.toByteArray());
      } catch (IOException var2) {
         throw new SAXException(var2);
      }
   }
}

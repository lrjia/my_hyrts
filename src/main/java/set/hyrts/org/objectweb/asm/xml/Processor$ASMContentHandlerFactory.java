package set.hyrts.org.objectweb.asm.xml;

import java.io.OutputStream;
import org.xml.sax.ContentHandler;
import set.hyrts.org.objectweb.asm.ClassWriter;

final class Processor$ASMContentHandlerFactory implements Processor$ContentHandlerFactory {
   final OutputStream os;

   Processor$ASMContentHandlerFactory(OutputStream var1) {
      this.os = var1;
   }

   public final ContentHandler createContentHandler() {
      ClassWriter var1 = new ClassWriter(1);
      return new Processor$ASMContentHandlerFactory$1(this, var1, var1);
   }
}

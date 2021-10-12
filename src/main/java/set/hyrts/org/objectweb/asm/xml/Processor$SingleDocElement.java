package set.hyrts.org.objectweb.asm.xml;

import java.io.IOException;
import java.io.OutputStream;

final class Processor$SingleDocElement implements Processor$EntryElement {
   private final OutputStream os;

   Processor$SingleDocElement(OutputStream var1) {
      this.os = var1;
   }

   public OutputStream openEntry(String var1) throws IOException {
      return this.os;
   }

   public void closeEntry() throws IOException {
      this.os.flush();
   }
}

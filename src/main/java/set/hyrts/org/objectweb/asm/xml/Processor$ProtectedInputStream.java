package set.hyrts.org.objectweb.asm.xml;

import java.io.IOException;
import java.io.InputStream;

final class Processor$ProtectedInputStream extends InputStream {
   private final InputStream is;

   Processor$ProtectedInputStream(InputStream var1) {
      this.is = var1;
   }

   public final void close() throws IOException {
   }

   public final int read() throws IOException {
      return this.is.read();
   }

   public final int read(byte[] var1, int var2, int var3) throws IOException {
      return this.is.read(var1, var2, var3);
   }

   public final int available() throws IOException {
      return this.is.available();
   }
}

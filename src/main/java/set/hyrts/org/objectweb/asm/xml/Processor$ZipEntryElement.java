package set.hyrts.org.objectweb.asm.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class Processor$ZipEntryElement implements Processor$EntryElement {
   private ZipOutputStream zos;

   Processor$ZipEntryElement(ZipOutputStream var1) {
      this.zos = var1;
   }

   public OutputStream openEntry(String var1) throws IOException {
      ZipEntry var2 = new ZipEntry(var1);
      this.zos.putNextEntry(var2);
      return this.zos;
   }

   public void closeEntry() throws IOException {
      this.zos.flush();
      this.zos.closeEntry();
   }
}

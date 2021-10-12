package set.hyrts.org.objectweb.asm.xml;

import java.io.IOException;
import java.io.OutputStream;

interface Processor$EntryElement {
   OutputStream openEntry(String var1) throws IOException;

   void closeEntry() throws IOException;
}

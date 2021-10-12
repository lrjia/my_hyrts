package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;

final class ASMContentHandler$SourceRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$SourceRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public void begin(String var1, Attributes var2) {
      String var3 = var2.getValue("file");
      String var4 = var2.getValue("debug");
      this.this$0.cv.visitSource(var3, var4);
   }
}

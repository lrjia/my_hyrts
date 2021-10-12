package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;

final class ASMContentHandler$OuterClassRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$OuterClassRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      String var3 = var2.getValue("owner");
      String var4 = var2.getValue("name");
      String var5 = var2.getValue("desc");
      this.this$0.cv.visitOuterClass(var3, var4, var5);
   }
}

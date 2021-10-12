package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;

final class ASMContentHandler$InnerClassRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$InnerClassRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      int var3 = this.getAccess(var2.getValue("access"));
      String var4 = var2.getValue("name");
      String var5 = var2.getValue("outerName");
      String var6 = var2.getValue("innerName");
      this.this$0.cv.visitInnerClass(var4, var5, var6, var3);
   }
}

package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;

final class ASMContentHandler$MaxRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$MaxRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      int var3 = Integer.parseInt(var2.getValue("maxStack"));
      int var4 = Integer.parseInt(var2.getValue("maxLocals"));
      this.getCodeVisitor().visitMaxs(var3, var4);
   }
}

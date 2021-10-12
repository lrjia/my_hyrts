package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;

final class ASMContentHandler$MethodParameterRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$MethodParameterRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public void begin(String var1, Attributes var2) {
      String var3 = var2.getValue("name");
      int var4 = this.getAccess(var2.getValue("access"));
      this.getCodeVisitor().visitParameter(var3, var4);
   }
}

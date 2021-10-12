package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.Label;

final class ASMContentHandler$LocalVarRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$LocalVarRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      String var3 = var2.getValue("name");
      String var4 = var2.getValue("desc");
      String var5 = var2.getValue("signature");
      Label var6 = this.getLabel(var2.getValue("start"));
      Label var7 = this.getLabel(var2.getValue("end"));
      int var8 = Integer.parseInt(var2.getValue("var"));
      this.getCodeVisitor().visitLocalVariable(var3, var4, var5, var6, var7, var8);
   }
}

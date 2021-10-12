package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.Label;

final class ASMContentHandler$TryCatchRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$TryCatchRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      Label var3 = this.getLabel(var2.getValue("start"));
      Label var4 = this.getLabel(var2.getValue("end"));
      Label var5 = this.getLabel(var2.getValue("handler"));
      String var6 = var2.getValue("type");
      this.getCodeVisitor().visitTryCatchBlock(var3, var4, var5, var6);
   }
}

package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.Label;

final class ASMContentHandler$LineNumberRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$LineNumberRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      int var3 = Integer.parseInt(var2.getValue("line"));
      Label var4 = this.getLabel(var2.getValue("start"));
      this.getCodeVisitor().visitLineNumber(var3, var4);
   }
}

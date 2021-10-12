package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;

final class ASMContentHandler$LabelRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$LabelRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      this.getCodeVisitor().visitLabel(this.getLabel(var2.getValue("name")));
   }
}

package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;

final class ASMContentHandler$LookupSwitchLabelRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$LookupSwitchLabelRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      HashMap var3 = (HashMap)this.this$0.peek();
      ((ArrayList)var3.get("labels")).add(this.getLabel(var2.getValue("name")));
      ((ArrayList)var3.get("keys")).add(var2.getValue("key"));
   }
}

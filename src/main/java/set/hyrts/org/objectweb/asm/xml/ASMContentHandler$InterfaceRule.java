package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;

final class ASMContentHandler$InterfaceRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$InterfaceRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      ((ArrayList)((HashMap)this.this$0.peek()).get("interfaces")).add(var2.getValue("name"));
   }
}

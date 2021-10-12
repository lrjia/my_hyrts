package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;

final class ASMContentHandler$ClassRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$ClassRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      int var3 = Integer.parseInt(var2.getValue("major"));
      int var4 = Integer.parseInt(var2.getValue("minor"));
      HashMap var5 = new HashMap();
      var5.put("version", new Integer(var4 << 16 | var3));
      var5.put("access", var2.getValue("access"));
      var5.put("name", var2.getValue("name"));
      var5.put("parent", var2.getValue("parent"));
      var5.put("source", var2.getValue("source"));
      var5.put("signature", var2.getValue("signature"));
      var5.put("interfaces", new ArrayList());
      this.this$0.push(var5);
   }
}

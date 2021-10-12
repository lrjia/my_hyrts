package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.MethodVisitor;

final class ASMContentHandler$MethodRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$MethodRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      this.this$0.labels = new HashMap();
      HashMap var3 = new HashMap();
      var3.put("access", var2.getValue("access"));
      var3.put("name", var2.getValue("name"));
      var3.put("desc", var2.getValue("desc"));
      var3.put("signature", var2.getValue("signature"));
      var3.put("exceptions", new ArrayList());
      this.this$0.push(var3);
   }

   public final void end(String var1) {
      ((MethodVisitor)this.this$0.pop()).visitEnd();
      this.this$0.labels = null;
   }
}

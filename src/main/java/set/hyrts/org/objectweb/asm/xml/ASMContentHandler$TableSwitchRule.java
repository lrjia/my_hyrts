package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.Label;

final class ASMContentHandler$TableSwitchRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$TableSwitchRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      HashMap var3 = new HashMap();
      var3.put("min", var2.getValue("min"));
      var3.put("max", var2.getValue("max"));
      var3.put("dflt", var2.getValue("dflt"));
      var3.put("labels", new ArrayList());
      this.this$0.push(var3);
   }

   public final void end(String var1) {
      HashMap var2 = (HashMap)this.this$0.pop();
      int var3 = Integer.parseInt((String)var2.get("min"));
      int var4 = Integer.parseInt((String)var2.get("max"));
      Label var5 = this.getLabel(var2.get("dflt"));
      ArrayList var6 = (ArrayList)var2.get("labels");
      Label[] var7 = (Label[])var6.toArray(new Label[var6.size()]);
      this.getCodeVisitor().visitTableSwitchInsn(var3, var4, var5, var7);
   }
}

package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.Label;

final class ASMContentHandler$LookupSwitchRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$LookupSwitchRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) {
      HashMap var3 = new HashMap();
      var3.put("dflt", var2.getValue("dflt"));
      var3.put("labels", new ArrayList());
      var3.put("keys", new ArrayList());
      this.this$0.push(var3);
   }

   public final void end(String var1) {
      HashMap var2 = (HashMap)this.this$0.pop();
      Label var3 = this.getLabel(var2.get("dflt"));
      ArrayList var4 = (ArrayList)var2.get("keys");
      ArrayList var5 = (ArrayList)var2.get("labels");
      Label[] var6 = (Label[])var5.toArray(new Label[var5.size()]);
      int[] var7 = new int[var4.size()];

      for(int var8 = 0; var8 < var7.length; ++var8) {
         var7[var8] = Integer.parseInt((String)var4.get(var8));
      }

      this.getCodeVisitor().visitLookupSwitchInsn(var3, var7, var6);
   }
}

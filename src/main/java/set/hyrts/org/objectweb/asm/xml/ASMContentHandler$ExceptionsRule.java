package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import java.util.HashMap;

final class ASMContentHandler$ExceptionsRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$ExceptionsRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void end(String var1) {
      HashMap var2 = (HashMap)this.this$0.pop();
      int var3 = this.getAccess((String)var2.get("access"));
      String var4 = (String)var2.get("name");
      String var5 = (String)var2.get("desc");
      String var6 = (String)var2.get("signature");
      ArrayList var7 = (ArrayList)var2.get("exceptions");
      String[] var8 = (String[])var7.toArray(new String[var7.size()]);
      this.this$0.push(this.this$0.cv.visitMethod(var3, var4, var5, var6, var8));
   }
}

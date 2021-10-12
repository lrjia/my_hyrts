package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import java.util.HashMap;

final class ASMContentHandler$InterfacesRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$InterfacesRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void end(String var1) {
      HashMap var2 = (HashMap)this.this$0.pop();
      int var3 = (Integer)var2.get("version");
      int var4 = this.getAccess((String)var2.get("access"));
      String var5 = (String)var2.get("name");
      String var6 = (String)var2.get("signature");
      String var7 = (String)var2.get("parent");
      ArrayList var8 = (ArrayList)var2.get("interfaces");
      String[] var9 = (String[])var8.toArray(new String[var8.size()]);
      this.this$0.cv.visit(var3, var4, var5, var6, var7, var9);
      this.this$0.push(this.this$0.cv);
   }
}

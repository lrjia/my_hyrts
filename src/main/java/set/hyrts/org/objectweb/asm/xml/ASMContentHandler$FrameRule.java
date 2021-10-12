package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;

final class ASMContentHandler$FrameRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$FrameRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public void begin(String var1, Attributes var2) {
      HashMap var3 = new HashMap();
      var3.put("local", new ArrayList());
      var3.put("stack", new ArrayList());
      this.this$0.push(var2.getValue("type"));
      this.this$0.push(var2.getValue("count") == null ? "0" : var2.getValue("count"));
      this.this$0.push(var3);
   }

   public void end(String var1) {
      HashMap var2 = (HashMap)this.this$0.pop();
      ArrayList var3 = (ArrayList)var2.get("local");
      int var4 = var3.size();
      Object[] var5 = var3.toArray();
      ArrayList var6 = (ArrayList)var2.get("stack");
      int var7 = var6.size();
      Object[] var8 = var6.toArray();
      String var9 = (String)this.this$0.pop();
      String var10 = (String)this.this$0.pop();
      if ("NEW".equals(var10)) {
         this.getCodeVisitor().visitFrame(-1, var4, var5, var7, var8);
      } else if ("FULL".equals(var10)) {
         this.getCodeVisitor().visitFrame(0, var4, var5, var7, var8);
      } else if ("APPEND".equals(var10)) {
         this.getCodeVisitor().visitFrame(1, var4, var5, 0, (Object[])null);
      } else if ("CHOP".equals(var10)) {
         this.getCodeVisitor().visitFrame(2, Integer.parseInt(var9), (Object[])null, 0, (Object[])null);
      } else if ("SAME".equals(var10)) {
         this.getCodeVisitor().visitFrame(3, 0, (Object[])null, 0, (Object[])null);
      } else if ("SAME1".equals(var10)) {
         this.getCodeVisitor().visitFrame(4, 0, (Object[])null, var7, var8);
      }

   }
}

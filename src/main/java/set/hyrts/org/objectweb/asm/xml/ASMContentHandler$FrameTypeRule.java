package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;

final class ASMContentHandler$FrameTypeRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$FrameTypeRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public void begin(String var1, Attributes var2) {
      ArrayList var3 = (ArrayList)((HashMap)this.this$0.peek()).get(var1);
      String var4 = var2.getValue("type");
      if ("uninitialized".equals(var4)) {
         var3.add(this.getLabel(var2.getValue("label")));
      } else {
         Integer var5 = (Integer)ASMContentHandler.TYPES.get(var4);
         if (var5 == null) {
            var3.add(var4);
         } else {
            var3.add(var5);
         }
      }

   }
}

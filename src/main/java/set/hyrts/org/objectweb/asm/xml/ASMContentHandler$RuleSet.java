package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

final class ASMContentHandler$RuleSet {
   private final HashMap rules = new HashMap();
   private final ArrayList lpatterns = new ArrayList();
   private final ArrayList rpatterns = new ArrayList();

   public void add(String var1, Object var2) {
      String var3 = var1;
      if (var1.startsWith("*/")) {
         var3 = var1.substring(1);
         this.lpatterns.add(var3);
      } else if (var1.endsWith("/*")) {
         var3 = var1.substring(0, var1.length() - 1);
         this.rpatterns.add(var3);
      }

      this.rules.put(var3, var2);
   }

   public Object match(String var1) {
      if (this.rules.containsKey(var1)) {
         return this.rules.get(var1);
      } else {
         int var2 = var1.lastIndexOf(47);
         Iterator var3 = this.lpatterns.iterator();

         String var4;
         do {
            if (!var3.hasNext()) {
               var3 = this.rpatterns.iterator();

               do {
                  if (!var3.hasNext()) {
                     return null;
                  }

                  var4 = (String)var3.next();
               } while(!var1.startsWith(var4));

               return this.rules.get(var4);
            }

            var4 = (String)var3.next();
         } while(!var1.substring(var2).endsWith(var4));

         return this.rules.get(var4);
      }
   }
}

package set.hyrts.org.objectweb.asm.tree;

import java.util.List;
import set.hyrts.org.objectweb.asm.ModuleVisitor;

public class ModuleProvideNode {
   public String service;
   public List providers;

   public ModuleProvideNode(String var1, List var2) {
      this.service = var1;
      this.providers = var2;
   }

   public void accept(ModuleVisitor var1) {
      var1.visitProvide(this.service, (String[])this.providers.toArray(new String[0]));
   }
}

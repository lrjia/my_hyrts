package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import set.hyrts.org.objectweb.asm.ModuleVisitor;

final class ASMContentHandler$ModuleRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$ModuleRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) throws SAXException {
      if ("module".equals(var1)) {
         this.this$0.push(this.this$0.cv.visitModule(var2.getValue("name"), this.getAccess(var2.getValue("access")), var2.getValue("version")));
      } else {
         ModuleVisitor var3;
         if ("main-class".equals(var1)) {
            var3 = (ModuleVisitor)this.this$0.peek();
            var3.visitMainClass(var2.getValue("name"));
         } else if ("packages".equals(var1)) {
            var3 = (ModuleVisitor)this.this$0.peek();
            var3.visitPackage(var2.getValue("name"));
         } else if ("requires".equals(var1)) {
            var3 = (ModuleVisitor)this.this$0.peek();
            int var4 = this.getAccess(var2.getValue("access"));
            if ((var4 & 8) != 0) {
               var4 = var4 & -9 | 64;
            }

            var3.visitRequire(var2.getValue("module"), var4, var2.getValue("version"));
         } else {
            ArrayList var5;
            if ("exports".equals(var1)) {
               this.this$0.push(var2.getValue("name"));
               this.this$0.push(new Integer(this.getAccess(var2.getValue("access"))));
               var5 = new ArrayList();
               this.this$0.push(var5);
            } else if ("opens".equals(var1)) {
               this.this$0.push(var2.getValue("name"));
               this.this$0.push(new Integer(this.getAccess(var2.getValue("access"))));
               var5 = new ArrayList();
               this.this$0.push(var5);
            } else if ("to".equals(var1)) {
               var5 = (ArrayList)this.this$0.peek();
               var5.add(var2.getValue("module"));
            } else if ("uses".equals(var1)) {
               var3 = (ModuleVisitor)this.this$0.peek();
               var3.visitUse(var2.getValue("service"));
            } else if ("provides".equals(var1)) {
               this.this$0.push(var2.getValue("service"));
               this.this$0.push(new Integer(0));
               var5 = new ArrayList();
               this.this$0.push(var5);
            } else if ("with".equals(var1)) {
               var5 = (ArrayList)this.this$0.peek();
               var5.add(var2.getValue("provider"));
            }
         }
      }

   }

   public void end(String var1) {
      boolean var2 = "exports".equals(var1);
      boolean var3 = "opens".equals(var1);
      boolean var4 = "provides".equals(var1);
      if (var2 | var3 | var4) {
         ArrayList var5 = (ArrayList)this.this$0.pop();
         int var6 = (Integer)this.this$0.pop();
         String var7 = (String)this.this$0.pop();
         String[] var8 = null;
         if (!var5.isEmpty()) {
            var8 = (String[])var5.toArray(new String[var5.size()]);
         }

         ModuleVisitor var9 = (ModuleVisitor)this.this$0.peek();
         if (var2) {
            var9.visitExport(var7, var6, var8);
         } else if (var3) {
            var9.visitOpen(var7, var6, var8);
         } else {
            var9.visitProvide(var7, var8);
         }
      } else if ("module".equals(var1)) {
         ((ModuleVisitor)this.this$0.pop()).visitEnd();
      }

   }
}

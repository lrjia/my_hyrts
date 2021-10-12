package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.helpers.AttributesImpl;
import set.hyrts.org.objectweb.asm.ModuleVisitor;

public final class SAXModuleAdapter extends ModuleVisitor {
   private final SAXAdapter sa;

   public SAXModuleAdapter(SAXAdapter var1) {
      super(393216);
      this.sa = var1;
   }

   public void visitMainClass(String var1) {
      AttributesImpl var2 = new AttributesImpl();
      var2.addAttribute("", "name", "name", "", var1);
      this.sa.addElement("main-class", var2);
   }

   public void visitPackage(String var1) {
      AttributesImpl var2 = new AttributesImpl();
      var2.addAttribute("", "name", "name", "", var1);
      this.sa.addElement("packages", var2);
   }

   public void visitRequire(String var1, int var2, String var3) {
      AttributesImpl var4 = new AttributesImpl();
      StringBuffer var5 = new StringBuffer();
      SAXClassAdapter.appendAccess(var2 | 2097152, var5);
      var4.addAttribute("", "module", "module", "", var1);
      var4.addAttribute("", "access", "access", "", var5.toString());
      if (var3 != null) {
         var4.addAttribute("", "access", "access", "", var3);
      }

      this.sa.addElement("requires", var4);
   }

   public void visitExport(String var1, int var2, String... var3) {
      AttributesImpl var4 = new AttributesImpl();
      StringBuffer var5 = new StringBuffer();
      SAXClassAdapter.appendAccess(var2 | 2097152, var5);
      var4.addAttribute("", "name", "name", "", var1);
      var4.addAttribute("", "access", "access", "", var5.toString());
      this.sa.addStart("exports", var4);
      if (var3 != null && var3.length > 0) {
         String[] var6 = var3;
         int var7 = var3.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String var9 = var6[var8];
            AttributesImpl var10 = new AttributesImpl();
            var10.addAttribute("", "module", "module", "", var9);
            this.sa.addElement("to", var10);
         }
      }

      this.sa.addEnd("exports");
   }

   public void visitOpen(String var1, int var2, String... var3) {
      AttributesImpl var4 = new AttributesImpl();
      StringBuffer var5 = new StringBuffer();
      SAXClassAdapter.appendAccess(var2 | 2097152, var5);
      var4.addAttribute("", "name", "name", "", var1);
      var4.addAttribute("", "access", "access", "", var5.toString());
      this.sa.addStart("opens", var4);
      if (var3 != null && var3.length > 0) {
         String[] var6 = var3;
         int var7 = var3.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String var9 = var6[var8];
            AttributesImpl var10 = new AttributesImpl();
            var10.addAttribute("", "module", "module", "", var9);
            this.sa.addElement("to", var10);
         }
      }

      this.sa.addEnd("opens");
   }

   public void visitUse(String var1) {
      AttributesImpl var2 = new AttributesImpl();
      var2.addAttribute("", "service", "service", "", var1);
      this.sa.addElement("uses", var2);
   }

   public void visitProvide(String var1, String... var2) {
      AttributesImpl var3 = new AttributesImpl();
      var3.addAttribute("", "service", "service", "", var1);
      this.sa.addStart("provides", var3);
      String[] var4 = var2;
      int var5 = var2.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String var7 = var4[var6];
         AttributesImpl var8 = new AttributesImpl();
         var8.addAttribute("", "provider", "provider", "", var7);
         this.sa.addElement("with", var8);
      }

      this.sa.addEnd("provides");
   }

   public void visitEnd() {
      this.sa.addEnd("module");
   }
}

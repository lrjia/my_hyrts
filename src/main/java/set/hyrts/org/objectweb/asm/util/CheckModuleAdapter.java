package set.hyrts.org.objectweb.asm.util;

import java.util.HashSet;
import set.hyrts.org.objectweb.asm.ModuleVisitor;

public final class CheckModuleAdapter extends ModuleVisitor {
   private boolean end;
   private final boolean isOpen;
   private final HashSet requireNames = new HashSet();
   private final HashSet exportNames = new HashSet();
   private final HashSet openNames = new HashSet();
   private final HashSet useNames = new HashSet();
   private final HashSet provideNames = new HashSet();

   public CheckModuleAdapter(ModuleVisitor var1, boolean var2) {
      super(393216, var1);
      this.isOpen = var2;
   }

   public void visitRequire(String var1, int var2, String var3) {
      this.checkEnd();
      if (var1 == null) {
         throw new IllegalArgumentException("require cannot be null");
      } else {
         checkDeclared("requires", this.requireNames, var1);
         CheckClassAdapter.checkAccess(var2, 36960);
         super.visitRequire(var1, var2, var3);
      }
   }

   public void visitExport(String var1, int var2, String... var3) {
      this.checkEnd();
      if (var1 == null) {
         throw new IllegalArgumentException("packaze cannot be null");
      } else {
         CheckMethodAdapter.checkInternalName(var1, "package name");
         checkDeclared("exports", this.exportNames, var1);
         CheckClassAdapter.checkAccess(var2, 36864);
         if (var3 != null) {
            for(int var4 = 0; var4 < var3.length; ++var4) {
               if (var3[var4] == null) {
                  throw new IllegalArgumentException("module at index " + var4 + " cannot be null");
               }
            }
         }

         super.visitExport(var1, var2, var3);
      }
   }

   public void visitOpen(String var1, int var2, String... var3) {
      this.checkEnd();
      if (this.isOpen) {
         throw new IllegalArgumentException("an open module can not use open directive");
      } else if (var1 == null) {
         throw new IllegalArgumentException("packaze cannot be null");
      } else {
         CheckMethodAdapter.checkInternalName(var1, "package name");
         checkDeclared("opens", this.openNames, var1);
         CheckClassAdapter.checkAccess(var2, 36864);
         if (var3 != null) {
            for(int var4 = 0; var4 < var3.length; ++var4) {
               if (var3[var4] == null) {
                  throw new IllegalArgumentException("module at index " + var4 + " cannot be null");
               }
            }
         }

         super.visitOpen(var1, var2, var3);
      }
   }

   public void visitUse(String var1) {
      this.checkEnd();
      CheckMethodAdapter.checkInternalName(var1, "service");
      checkDeclared("uses", this.useNames, var1);
      super.visitUse(var1);
   }

   public void visitProvide(String var1, String... var2) {
      this.checkEnd();
      CheckMethodAdapter.checkInternalName(var1, "service");
      checkDeclared("provides", this.provideNames, var1);
      if (var2 != null && var2.length != 0) {
         for(int var3 = 0; var3 < var2.length; ++var3) {
            CheckMethodAdapter.checkInternalName(var2[var3], "provider");
         }

         super.visitProvide(var1, var2);
      } else {
         throw new IllegalArgumentException("providers cannot be null or empty");
      }
   }

   public void visitEnd() {
      this.checkEnd();
      this.end = true;
      super.visitEnd();
   }

   private void checkEnd() {
      if (this.end) {
         throw new IllegalStateException("Cannot call a visit method after visitEnd has been called");
      }
   }

   private static void checkDeclared(String var0, HashSet var1, String var2) {
      if (!var1.add(var2)) {
         throw new IllegalArgumentException(var0 + " " + var2 + " already declared");
      }
   }
}

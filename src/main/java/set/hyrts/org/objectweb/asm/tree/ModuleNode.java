package set.hyrts.org.objectweb.asm.tree;

import java.util.ArrayList;
import java.util.List;
import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.ModuleVisitor;

public class ModuleNode extends ModuleVisitor {
   public String name;
   public int access;
   public String version;
   public String mainClass;
   public List packages;
   public List requires;
   public List exports;
   public List opens;
   public List uses;
   public List provides;
   // $FF: synthetic field
   static Class class$org$objectweb$asm$tree$ModuleNode = class$("set.hyrts.org.objectweb.asm.tree.ModuleNode");

   public ModuleNode(String var1, int var2, String var3) {
      super(393216);
      this.name = var1;
      this.access = var2;
      this.version = var3;
   }

   public ModuleNode(int var1, String var2, int var3, String var4, List var5, List var6, List var7, List var8, List var9) {
      super(var1);
      this.name = var2;
      this.access = var3;
      this.version = var4;
      this.requires = var5;
      this.exports = var6;
      this.opens = var7;
      this.uses = var8;
      this.provides = var9;
      if (this.getClass() != class$org$objectweb$asm$tree$ModuleNode) {
         throw new IllegalStateException();
      }
   }

   public void visitMainClass(String var1) {
      this.mainClass = var1;
   }

   public void visitPackage(String var1) {
      if (this.packages == null) {
         this.packages = new ArrayList(5);
      }

      this.packages.add(var1);
   }

   public void visitRequire(String var1, int var2, String var3) {
      if (this.requires == null) {
         this.requires = new ArrayList(5);
      }

      this.requires.add(new ModuleRequireNode(var1, var2, var3));
   }

   public void visitExport(String var1, int var2, String... var3) {
      if (this.exports == null) {
         this.exports = new ArrayList(5);
      }

      ArrayList var4 = null;
      if (var3 != null) {
         var4 = new ArrayList(var3.length);

         for(int var5 = 0; var5 < var3.length; ++var5) {
            var4.add(var3[var5]);
         }
      }

      this.exports.add(new ModuleExportNode(var1, var2, var4));
   }

   public void visitOpen(String var1, int var2, String... var3) {
      if (this.opens == null) {
         this.opens = new ArrayList(5);
      }

      ArrayList var4 = null;
      if (var3 != null) {
         var4 = new ArrayList(var3.length);

         for(int var5 = 0; var5 < var3.length; ++var5) {
            var4.add(var3[var5]);
         }
      }

      this.opens.add(new ModuleOpenNode(var1, var2, var4));
   }

   public void visitUse(String var1) {
      if (this.uses == null) {
         this.uses = new ArrayList(5);
      }

      this.uses.add(var1);
   }

   public void visitProvide(String var1, String... var2) {
      if (this.provides == null) {
         this.provides = new ArrayList(5);
      }

      ArrayList var3 = new ArrayList(var2.length);

      for(int var4 = 0; var4 < var2.length; ++var4) {
         var3.add(var2[var4]);
      }

      this.provides.add(new ModuleProvideNode(var1, var3));
   }

   public void visitEnd() {
   }

   public void accept(ClassVisitor var1) {
      ModuleVisitor var2 = var1.visitModule(this.name, this.access, this.version);
      if (var2 != null) {
         if (this.mainClass != null) {
            var2.visitMainClass(this.mainClass);
         }

         int var3;
         if (this.packages != null) {
            for(var3 = 0; var3 < this.packages.size(); ++var3) {
               var2.visitPackage((String)this.packages.get(var3));
            }
         }

         if (this.requires != null) {
            for(var3 = 0; var3 < this.requires.size(); ++var3) {
               ((ModuleRequireNode)this.requires.get(var3)).accept(var2);
            }
         }

         if (this.exports != null) {
            for(var3 = 0; var3 < this.exports.size(); ++var3) {
               ((ModuleExportNode)this.exports.get(var3)).accept(var2);
            }
         }

         if (this.opens != null) {
            for(var3 = 0; var3 < this.opens.size(); ++var3) {
               ((ModuleOpenNode)this.opens.get(var3)).accept(var2);
            }
         }

         if (this.uses != null) {
            for(var3 = 0; var3 < this.uses.size(); ++var3) {
               var2.visitUse((String)this.uses.get(var3));
            }
         }

         if (this.provides != null) {
            for(var3 = 0; var3 < this.provides.size(); ++var3) {
               ((ModuleProvideNode)this.provides.get(var3)).accept(var2);
            }
         }

      }
   }

   // $FF: synthetic method
   static Class class$(String var0) {
      try {
         return Class.forName(var0);
      } catch (ClassNotFoundException var2) {
         String var1 = var2.getMessage();
         throw new NoClassDefFoundError(var1);
      }
   }
}

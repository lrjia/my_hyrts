package set.hyrts.org.objectweb.asm.commons;

import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.MethodVisitor;

public class StaticInitMerger extends ClassVisitor {
   private String name;
   private MethodVisitor clinit;
   private final String prefix;
   private int counter;

   public StaticInitMerger(String var1, ClassVisitor var2) {
      this(393216, var1, var2);
   }

   protected StaticInitMerger(int var1, String var2, ClassVisitor var3) {
      super(var1, var3);
      this.prefix = var2;
   }

   public void visit(int var1, int var2, String var3, String var4, String var5, String[] var6) {
      this.cv.visit(var1, var2, var3, var4, var5, var6);
      this.name = var3;
   }

   public MethodVisitor visitMethod(int var1, String var2, String var3, String var4, String[] var5) {
      MethodVisitor var6;
      if ("<clinit>".equals(var2)) {
         byte var7 = 10;
         String var8 = this.prefix + this.counter++;
         var6 = this.cv.visitMethod(var7, var8, var3, var4, var5);
         if (this.clinit == null) {
            this.clinit = this.cv.visitMethod(var7, var2, var3, (String)null, (String[])null);
         }

         this.clinit.visitMethodInsn(184, this.name, var8, var3, false);
      } else {
         var6 = this.cv.visitMethod(var1, var2, var3, var4, var5);
      }

      return var6;
   }

   public void visitEnd() {
      if (this.clinit != null) {
         this.clinit.visitInsn(177);
         this.clinit.visitMaxs(0, 0);
      }

      this.cv.visitEnd();
   }
}

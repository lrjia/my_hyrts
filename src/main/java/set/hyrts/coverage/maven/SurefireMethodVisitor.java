package set.hyrts.coverage.maven;

import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;

class SurefireMethodVisitor extends MethodVisitor implements Opcodes {
   String methName;
   String desc;

   public SurefireMethodVisitor(MethodVisitor mv, String name, String desc) {
      super(327680, mv);
      this.methName = name;
      this.desc = desc;
   }

   public void visitCode() {
      this.mv.visitVarInsn(25, 0);
      this.mv.visitMethodInsn(184, "set/hyrts/coverage/maven/SurefireRewriter", this.methName, this.desc.replace("(", "(Ljava/lang/Object;"), false);
      this.mv.visitCode();
   }
}

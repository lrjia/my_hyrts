package set.hyrts.coverage.junit;

import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;

class JUnit4MethodVisitor extends MethodVisitor implements Opcodes {
   public String className;

   public JUnit4MethodVisitor(MethodVisitor mv, String className) {
      super(327680, mv);
      this.className = className;
   }

   public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
      if (name.equals("runnerForClass") && desc.equals("(Ljava/lang/Class;)Lorg/junit/runner/Runner;")) {
         this.mv.visitLdcInsn(this.className);
         this.mv.visitMethodInsn(184, "set/hyrts/coverage/junit/FTracerJUnitUtils", "transformJUnit4Runner4Class", "(Lorg/junit/runners/model/RunnerBuilder;Ljava/lang/Class;Ljava/lang/String;)Lorg/junit/runner/Runner;", itf);
      } else {
         this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
      }

   }
}

package set.hyrts.coverage.core;

import set.hyrts.org.objectweb.asm.Label;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;

class MethodVisitorStmtCov extends MethodVisitor implements Opcodes {
   int clazzId;
   int methId;
   boolean isVirtual;
   boolean isInit;
   int line;

   public MethodVisitorStmtCov(MethodVisitor mv, int clazzId, int methId, boolean isInit, int access) {
      super(327680, mv);
      this.clazzId = clazzId;
      this.methId = methId;
      this.isInit = isInit;
   }

   public void visitLabel(Label label) {
      this.mv.visitLdcInsn(this.clazzId);
      this.mv.visitLdcInsn(this.line);
      this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceStmtCovInfo", "(II)V", false);
      super.visitLabel(label);
   }

   public void visitLineNumber(int line, Label start) {
      this.line = line;
      this.mv.visitLdcInsn(this.clazzId);
      this.mv.visitLdcInsn(line);
      this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceStmtCovInfo", "(II)V", false);
      super.visitLineNumber(line, start);
   }
}

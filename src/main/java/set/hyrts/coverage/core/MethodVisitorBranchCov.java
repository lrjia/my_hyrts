package set.hyrts.coverage.core;

import java.util.HashSet;
import java.util.Set;
import set.hyrts.org.objectweb.asm.Label;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;

class MethodVisitorBranchCov extends MethodVisitor implements Opcodes {
   int clazzId;
   int methId;
   String clazzName;
   String methName;
   static int branchID = 0;
   static int switchLabelID = 0;
   static int lineNum = -1;
   static Set<Label> switchLabels = new HashSet();

   public MethodVisitorBranchCov(MethodVisitor mv, int clazzId, int methId, String clazzName, String methName, boolean isInit, int access) {
      super(327680, mv);
      this.clazzId = clazzId;
      this.methId = methId;
      this.clazzName = clazzName;
      this.methName = methName;
   }

   public void visitCode() {
      this.mv.visitCode();
      switchLabels.clear();
   }

   public void visitJumpInsn(int type, Label target) {
      if (type == 167) {
         this.mv.visitJumpInsn(type, target);
      } else {
         int curBranch = nextBranchID();
         this.mv.visitLdcInsn("false");
         this.mv.visitLdcInsn(this.clazzName + ":" + lineNum + ":" + curBranch + "");
         this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceBranchCovInfo", "(Ljava/lang/String;Ljava/lang/String;)V", false);
         this.mv.visitJumpInsn(type, target);
         this.mv.visitLdcInsn("true");
         this.mv.visitLdcInsn(this.clazzName + ":" + lineNum + ":" + curBranch + "");
         this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceBranchCovInfo", "(Ljava/lang/String;Ljava/lang/String;)V", false);
      }
   }

   public void visitTableSwitchInsn(int type, int arg2, Label defaultTarget, Label[] larray) {
      Label[] var5 = larray;
      int var6 = larray.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Label l = var5[var7];
         switchLabels.add(l);
      }

      switchLabels.remove(defaultTarget);
      this.mv.visitTableSwitchInsn(type, arg2, defaultTarget, larray);
   }

   public void visitLookupSwitchInsn(Label defaultTarget, int[] iarray, Label[] larray) {
      Label[] var4 = larray;
      int var5 = larray.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Label l = var4[var6];
         switchLabels.add(l);
      }

      switchLabels.remove(defaultTarget);
      this.mv.visitLookupSwitchInsn(defaultTarget, iarray, larray);
   }

   public void visitLabel(Label l) {
      this.mv.visitLabel(l);
      if (switchLabels.contains(l)) {
         int curSwitch = nextSwitchID();
         this.mv.visitLdcInsn("");
         this.mv.visitLdcInsn(this.clazzName + ":" + (lineNum + 1) + ":" + curSwitch);
         this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceBranchCovInfo", "(Ljava/lang/String;Ljava/lang/String;)V", false);
      }

   }

   public void visitLineNumber(int line, Label start) {
      this.mv.visitLineNumber(line, start);
      lineNum = line;
   }

   private static synchronized int nextBranchID() {
      return branchID++;
   }

   private static synchronized int nextSwitchID() {
      return switchLabelID++;
   }
}

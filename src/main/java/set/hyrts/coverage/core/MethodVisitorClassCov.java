package set.hyrts.coverage.core;

import set.hyrts.coverage.agent.ClassTransformer;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;
import set.hyrts.org.objectweb.asm.Type;
import set.hyrts.utils.Classes;

class MethodVisitorClassCov extends MethodVisitor implements Opcodes {
   int clazzId;
   int methId;
   String slashClazzName;
   String descClazzName;
   String dotClazzName;
   String methName;
   boolean isVirtual;
   boolean isInit;

   public MethodVisitorClassCov(MethodVisitor mv, int clazzId, int methId, String slashClazzName, String dotClazzName, String methName, boolean isInit, int access) {
      super(327680, mv);
      this.clazzId = clazzId;
      this.methId = methId;
      this.slashClazzName = slashClazzName;
      this.descClazzName = "L" + slashClazzName + ";";
      this.dotClazzName = dotClazzName;
      this.methName = methName;
      this.isInit = isInit;
      this.isVirtual = this.isVirtual(access);
   }

   public void visitCode() {
      this.mv.visitLdcInsn(this.clazzId);
      this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceClassCovInfo", "(I)V", false);
      super.visitCode();
   }

   public void visitLdcInsn(Object o) {
      if (o instanceof Type) {
         Type t = (Type)o;
         if (t != null && t.getSort() == 10) {
            String str = Classes.descToClassName(t.getDescriptor());
            if (!str.equals(this.slashClazzName) && !ClassTransformer.isExcluded(str)) {
               this.mv.visitLdcInsn(t);
               this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceClassCovInfo", "(Ljava/lang/Class;)V", false);
            }
         }
      }

      this.mv.visitLdcInsn(o);
   }

   public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
      this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
      if (opcode == 185 && !ClassTransformer.isExcluded(owner)) {
         Integer classId = (Integer)CoverageData.classIdMap.get(owner);
         if (classId != null) {
            this.mv.visitLdcInsn(classId);
            this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceClassCovInfo", "(I)V", false);
         } else {
            this.mv.visitLdcInsn(owner);
            this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceClassCovInfo", "(Ljava/lang/String;)V", false);
         }
      }

   }

   public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      this.mv.visitFieldInsn(opcode, owner, name, desc);
      if (opcode == 178) {
         if (!owner.equals(this.slashClazzName)) {
            this.mv.visitLdcInsn(owner);
            this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceClassCovInfo", "(Ljava/lang/String;)V", false);
         }

         if (!Classes.isPrimitive(desc)) {
            String fieldType = Classes.descToClassName(desc);
            if (!fieldType.equals(this.slashClazzName) && !ClassTransformer.isExcluded(fieldType)) {
               this.mv.visitFieldInsn(178, owner, name, desc);
               this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceClassCovInfo", "(Ljava/lang/Object;)V", false);
            }
         }
      }

   }

   public void visitMaxs(int maxStack, int maxLocals) {
      this.mv.visitMaxs(maxStack + 4, maxLocals);
   }

   public boolean isVirtual(int access) {
      return 0 == (access & 8);
   }
}

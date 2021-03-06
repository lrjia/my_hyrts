package set.hyrts.coverage.core;

import set.hyrts.coverage.agent.ClassTransformer;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.org.objectweb.asm.Label;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;
import set.hyrts.org.objectweb.asm.Type;
import set.hyrts.utils.Classes;
import set.hyrts.utils.Properties;

class MethodVisitorBlockCov extends MethodVisitor implements Opcodes {
    private static Logger logger = Logger.getLogger(MethodVisitorBlockCov.class);
    int clazzId;
    int methId;
    String slashClazzName;
    String dotClazzName;
    String methName;
    boolean isVirtual;
    boolean isInit;
    int labelId = 0;

    public MethodVisitorBlockCov(MethodVisitor mv, int clazzId, int methId, String slashClazzName, String dotClazzName, String methName, boolean isInit, int access) {
        super(327680, mv);
        this.clazzId = clazzId;
        this.methId = methId;
        this.slashClazzName = slashClazzName;
        this.dotClazzName = dotClazzName;
        this.methName = methName;
        this.isInit = isInit;
    }

    public void visitLabel(Label label) {
        super.visitLabel(label);
        this.mv.visitLdcInsn(this.clazzId);
        this.mv.visitLdcInsn(this.methId);
        this.mv.visitLdcInsn(this.labelId++);
        this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceBlockCovInfo", "(III)V", false);
    }

    public void visitCode() {
        this.mv.visitLdcInsn(this.clazzId);
        this.mv.visitLdcInsn(this.methId);
        this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceMethCovInfo", "(II)V", false);
        if (Properties.TRACE_RTINFO && this.isVirtual && !this.isInit) {
            this.mv.visitLdcInsn(this.clazzId);
            this.mv.visitLdcInsn(this.methId);
            this.mv.visitLdcInsn(this.dotClazzName);
            this.mv.visitLdcInsn(this.methName);
            this.mv.visitVarInsn(25, 0);
            this.mv.visitMethodInsn(182, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            this.mv.visitMethodInsn(182, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
            this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceMethCovInfoWithRT", "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
        }

        super.visitCode();
    }

    public void visitLdcInsn(Object o) {
        if (o instanceof Type) {
            Type t = (Type) o;
            if (t != null && t.getSort() == 10) {
                String str = Classes.descToClassName(t.getDescriptor());
                if (!str.equals(this.slashClazzName) && !ClassTransformer.isExcluded(str)) {
                    this.mv.visitLdcInsn(t);
                    this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceMethCovInfoClinit", "(Ljava/lang/Class;)V", false);
                }
            }
        }

        this.mv.visitLdcInsn(o);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
        if (opcode == 185 && !ClassTransformer.isExcluded(owner)) {
            Integer classId = (Integer) CoverageData.classIdMap.get(owner);
            if (classId != null) {
                this.mv.visitLdcInsn(classId);
                this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceMethCovInfoClinit", "(I)V", false);
            } else {
                this.mv.visitLdcInsn(owner);
                this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceMethCovInfoClinit", "(Ljava/lang/String;)V", false);
            }
        }

    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        this.mv.visitFieldInsn(opcode, owner, name, desc);
        if (opcode == 178) {
            if (!owner.equals(this.slashClazzName)) {
                this.mv.visitLdcInsn(owner);
                this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceMethCovInfoClinit", "(Ljava/lang/String;)V", false);
            }

            if (!Classes.isPrimitive(desc)) {
                String fieldType = Classes.descToClassName(desc);
                if (!fieldType.equals(this.slashClazzName) && !ClassTransformer.isExcluded(fieldType)) {
                    this.mv.visitFieldInsn(178, owner, name, desc);
                    this.mv.visitMethodInsn(184, "set/hyrts/coverage/core/Tracer", "traceMethCovInfoClinit", "(Ljava/lang/Object;)V", false);
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

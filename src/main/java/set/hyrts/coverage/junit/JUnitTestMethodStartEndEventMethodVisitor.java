package set.hyrts.coverage.junit;

import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;

class JUnitTestMethodStartEndEventMethodVisitor extends MethodVisitor implements Opcodes {
    String mName;

    public JUnitTestMethodStartEndEventMethodVisitor(MethodVisitor mv, String name) {
        super(327680, mv);
        this.mName = name;
    }

    public void visitCode() {
        if (!this.mName.equals("fireTestStarted")) {
            this.mv.visitVarInsn(25, 1);
            this.mv.visitMethodInsn(184, "set/hyrts/coverage/junit/FTracerJUnitUtils", "dumpCoverage", "(Lorg/junit/runner/Description;)V", false);
        }

        this.mv.visitCode();
    }
}

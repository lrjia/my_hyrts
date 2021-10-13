package set.hyrts.coverage.junit;

import set.hyrts.org.objectweb.asm.Label;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;

class JUnit3MethodVisitor extends MethodVisitor implements Opcodes {
    public JUnit3MethodVisitor(MethodVisitor mv) {
        super(327680, mv);
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        this.mv.visitFieldInsn(opcode, owner, name, desc);
        if (opcode == 181 && owner.equals("junit/framework/TestSuite") && name.equals("fTests") && desc.equals("Ljava/util/Vector;")) {
            Label localLabel = new Label();
            this.mv.visitVarInsn(25, 0);
            this.mv.visitVarInsn(25, 1);
            this.mv.visitMethodInsn(184, "set/hyrts/coverage/junit/FTracerJUnitUtils", "transformJUnit3SuitInit", "(Ljunit/framework/TestSuite;Ljava/lang/Class;)Z", false);
            this.mv.visitJumpInsn(154, localLabel);
            this.mv.visitInsn(177);
            this.mv.visitLabel(localLabel);
        }

    }
}

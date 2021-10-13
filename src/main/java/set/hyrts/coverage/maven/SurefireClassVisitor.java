package set.hyrts.coverage.maven;

import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;

public class SurefireClassVisitor extends ClassVisitor implements Opcodes {
    public SurefireClassVisitor(ClassVisitor cv) {
        super(327680, cv);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("execute") && desc.equals("()V")) {
            mv = new SurefireMethodVisitor((MethodVisitor) mv, name, desc);
        }

        return (MethodVisitor) mv;
    }
}

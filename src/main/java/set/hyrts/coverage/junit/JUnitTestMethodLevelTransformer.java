package set.hyrts.coverage.junit;

import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class JUnitTestMethodLevelTransformer implements ClassFileTransformer, Opcodes {
    public static final String JUNIT_NOTIFIER_CLASS = "org/junit/runner/notification/RunNotifier";
    public static String className;
    private static Logger logger = Logger.getLogger(JUnitTestMethodLevelTransformer.class);

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if (className == null) {
                return classfileBuffer;
            } else if (loader != ClassLoader.getSystemClassLoader()) {
                return classfileBuffer;
            } else if (!className.equals("org/junit/runner/notification/RunNotifier")) {
                return classfileBuffer;
            } else {
                logger.debug("transforming JUnit class: " + className);
                JUnitTestMethodLevelTransformer.className = className;
                ClassReader reader = new ClassReader(classfileBuffer);
                ClassWriter writer = new ClassWriter(2);
                ClassVisitor cv = new ClassVisitor(327680, writer) {
                    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                        MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
                        return (MethodVisitor) (!JUnitTestMethodLevelTransformer.className.equals("org/junit/runner/notification/RunNotifier") || !name.equals("fireTestStarted") && !name.equals("fireTestFinished") ? mv : new JUnitTestMethodStartEndEventMethodVisitor(mv, name));
                    }
                };
                reader.accept(cv, 8);
                byte[] result = writer.toByteArray();
                return result;
            }
        } catch (Throwable var10) {
            var10.printStackTrace();
            String message = "Exception thrown during instrumentation";
            logger.error(message, var10);
            System.err.println(message);
            System.exit(1);
            throw new RuntimeException("Should not be reached");
        }
    }
}

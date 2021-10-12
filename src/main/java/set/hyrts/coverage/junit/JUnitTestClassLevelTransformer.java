package set.hyrts.coverage.junit;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import set.hyrts.logger.FTracerLogger;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.ClassWriter;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;

public class JUnitTestClassLevelTransformer implements ClassFileTransformer, Opcodes {
   private static Logger logger = Logger.getLogger(JUnitTestClassLevelTransformer.class);
   public static final String PARENT_RUNNER_CLASS = "org/junit/runners/ParentRunner";
   public static final String PARENT_RUNNER_CLASS_DOT = "org.junit.runners.ParentRunner";
   public static final String JUNIT38_RUNNER_CLASS = "junit/framework/TestSuite";
   public static final String JUNIT38_RUNNER_CLASS_DOT = "junit.framework.TestSuite";
   public static final String JUNIT4_PREFIX = "org/junit/";
   public static final String JUNIT3_PREFIX = "junit/framework/";
   public static final String RUN_METH = "run";
   public static final String INIT = "<init>";
   public static final String RUN4CLASS = "runnerForClass";
   public static final String RUN4CLASS_DESC = "(Ljava/lang/Class;)Lorg/junit/runner/Runner;";
   public static final String JUNIT_NOTIFIER_CLASS = "org/junit/runner/notification/RunNotifier";
   public static String className;

   public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
      try {
         if (className == null) {
            return classfileBuffer;
         } else if (!className.equals("junit/framework/TestSuite") && !className.startsWith("org/junit/")) {
            return classfileBuffer;
         } else {
            logger.debug("transforming JUnit class: " + className);
            JUnitTestClassLevelTransformer.className = className;
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(2);
            ClassVisitor cv = new ClassVisitor(327680, writer) {
               public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                  MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
                  if (JUnitTestClassLevelTransformer.matchJUnit4Runner(JUnitTestClassLevelTransformer.className)) {
                     return new JUnit4MethodVisitor(mv, JUnitTestClassLevelTransformer.className);
                  } else if (JUnitTestClassLevelTransformer.matchJUnit3Suite(JUnitTestClassLevelTransformer.className, name, desc)) {
                     FTracerLogger.debug("TestClassTransformer " + JUnitTestClassLevelTransformer.className + "-" + name);
                     return new JUnit3MethodVisitor(mv);
                  } else {
                     return mv;
                  }
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

   public static boolean matchJUnit3Suite(String className, String methName, String desc) {
      return "junit/framework/TestSuite".equals(className) && "<init>".equals(methName) & desc.equals("(Ljava/lang/Class;)V");
   }

   public static boolean matchJUnit4Runner(String className) {
      return className.startsWith("org/junit/");
   }
}

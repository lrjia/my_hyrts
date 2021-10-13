package set.hyrts.coverage.agent;

import set.hyrts.coverage.core.ClassTransformVisitor;
import set.hyrts.coverage.core.CoverageData;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.ClassWriter;
import set.hyrts.utils.Classes;
import set.hyrts.utils.Properties;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ClassTransformer implements ClassFileTransformer {
    static Set<String> excludedPrefixes = new HashSet();
    private static Logger logger;

    static {
        excludedPrefixes.add("org/junit");
        excludedPrefixes.add("junit/");
        excludedPrefixes.add("org/apache/maven");
        excludedPrefixes.add("set/hyrts");
        excludedPrefixes.add("org/objectweb");
        excludedPrefixes.add("java/");
        excludedPrefixes.add("javax/");
        excludedPrefixes.add("sun/");
        excludedPrefixes.add("com/sun/");
        logger = Logger.getLogger(ClassTransformer.class);
    }

    public static boolean isExcluded(String className) {
        Iterator var1 = excludedPrefixes.iterator();

        String prefix;
        do {
            if (!var1.hasNext()) {
                return false;
            }

            prefix = (String) var1.next();
        } while (!className.startsWith(prefix));

        return true;
    }

    public static boolean isExcluded(ClassLoader loader, String className) {
        Iterator var2 = excludedPrefixes.iterator();

        while (var2.hasNext()) {
            String prefix = (String) var2.next();
            if (className.startsWith(prefix)) {
                return true;
            }
        }

        if (!Properties.TRACE_LIB) {
            URL url = loader.getResource(className + ".class");
            if (url == null || url.getProtocol().equals("jar")) {
                return true;
            }
        }

        return false;
    }

    public byte[] transform(ClassLoader loader, String slashClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if (slashClassName == null) {
                return classfileBuffer;
            } else if (loader != ClassLoader.getSystemClassLoader()) {
                return classfileBuffer;
            } else if (isExcluded(loader, slashClassName)) {
                return classfileBuffer;
            } else {
                logger.debug("transforming: " + slashClassName);
                String dotClassName = Classes.toDotClassName(slashClassName);
                int clazzId = CoverageData.registerClass(slashClassName, dotClassName);
                ClassReader reader = new ClassReader(classfileBuffer);
                ClassWriter writer = new ComputeClassWriter(FrameOptions.pickFlags(classfileBuffer));
                ClassTransformVisitor cv = new ClassTransformVisitor(clazzId, slashClassName, dotClassName, writer);
                reader.accept(cv, 8);
                byte[] result = writer.toByteArray();
                return result;
            }
        } catch (Throwable var12) {
            var12.printStackTrace();
            String message = "Exception thrown during instrumentation";
            logger.error(message, var12);
            System.err.println(message);
            System.exit(1);
            throw new RuntimeException("Should not be reached");
        }
    }
}

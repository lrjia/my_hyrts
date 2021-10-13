package set.hyrts.coverage.maven;

import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.ClassWriter;
import set.hyrts.org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class SurefireTransformer implements ClassFileTransformer, Opcodes {
    private static Logger logger = Logger.getLogger(SurefireTransformer.class);

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className == null) {
            return classfileBuffer;
        } else if (!className.equals("org/apache/maven/plugin/surefire/AbstractSurefireMojo") && !className.equals("org/apache/maven/plugin/surefire/SurefirePlugin") && !className.equals("org/apache/maven/plugin/failsafe/IntegrationTestMojo")) {
            return null;
        } else {
            ClassReader localClassReader = new ClassReader(classfileBuffer);
            ClassWriter localClassWriter = new ClassWriter(localClassReader, 2);
            SurefireClassVisitor localMavenClassVisitor = new SurefireClassVisitor(localClassWriter);
            localClassReader.accept(localMavenClassVisitor, 8);
            return localClassWriter.toByteArray();
        }
    }
}

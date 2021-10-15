package set.hyrts;

import set.hyrts.coverage.core.ClassTransformVisitor;
import set.hyrts.coverage.core.CoverageData;
import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.ClassWriter;
import set.hyrts.utils.Classes;
import set.hyrts.utils.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static set.hyrts.coverage.junit.FTracerJUnitUtils.dumpCoverage;

public class AddField {

    public static void main(String[] args) throws Exception {
        String output = "C:\\Users\\lrjia\\codeUnSync\\my-hyrts\\target\\classes\\set\\hyrts\\";
        String classDir = "C:\\Users\\lrjia\\codeUnSync\\my-hyrts\\target\\classes\\set\\hyrts\\App.class";
        ClassReader classReader = new ClassReader(new FileInputStream(classDir));
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        String slashClassName = App.class.getName();
        String dotClassName = Classes.toDotClassName(slashClassName);
        int clazzId = CoverageData.registerClass(slashClassName, dotClassName);
        Properties.TRACER_COV_TYPE = "meth-cov";
        ClassVisitor hyrtsVisitor = new ClassTransformVisitor(clazzId, slashClassName, dotClassName, classWriter);
        classReader.accept(hyrtsVisitor, ClassReader.EXPAND_FRAMES);
        byte[] newClass = classWriter.toByteArray();
        File newFile = new File(output, "App.class");
        new FileOutputStream(newFile).write(newClass);
        App.foo();
        dumpCoverage(App.class);
    }
}

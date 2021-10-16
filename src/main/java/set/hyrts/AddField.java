package set.hyrts;

import set.hyrts.coverage.core.ClassTransformVisitor;
import set.hyrts.coverage.core.CoverageData;
import set.hyrts.demo.src.ClassB;
import set.hyrts.demo.src.parent;
import set.hyrts.demo.src.son;
import set.hyrts.demo.testCase.TestCase1;
import set.hyrts.demo.testCase.TestCase2;
import set.hyrts.diff.VersionDiff;
import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.ClassWriter;
import set.hyrts.rts.HybridRTS;
import set.hyrts.utils.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static set.hyrts.coverage.junit.FTracerJUnitUtils.dumpCoverage;

public class AddField {

    public static void instrumentBytecode(String classDir, String dotClassName) throws IOException {
        String slashClassName = dotClassName.replace(".", "/");
        int clazzId = CoverageData.registerClass(slashClassName, dotClassName);

        //插桩
        ClassReader classReader = new ClassReader(new FileInputStream(classDir));
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor hyrtsVisitor = new ClassTransformVisitor(clazzId, slashClassName, dotClassName, classWriter);
        classReader.accept(hyrtsVisitor, ClassReader.EXPAND_FRAMES);
        byte[] newClass = classWriter.toByteArray();

        //写回
        File newFile = new File(classDir);
        new FileOutputStream(newFile).write(newClass);
    }

    public static void instrumentAll() {
        try {
            CoverageData.reset();
            instrumentBytecode(Properties.NEW_CLASSPATH + "son.class", son.class.getName());
            instrumentBytecode(Properties.NEW_CLASSPATH + "parent.class", parent.class.getName());
            instrumentBytecode(Properties.NEW_CLASSPATH + "ClassB.class", ClassB.class.getName());
        } catch (IOException e) {
            System.err.println("插桩错误");
            e.printStackTrace();
        }
    }

    public static void execTestCase() throws IOException {
        Properties.NEW_DIR = "./diff_old";
        instrumentAll();
        new TestCase1().runTest();
        dumpCoverage(TestCase1.class);

        instrumentAll();
        new TestCase2().runTest();
        dumpCoverage(TestCase2.class);
    }


    public static void serializeOldCode() throws Exception {
        VersionDiff.compute(null, "./diff_old", Properties.NEW_CLASSPATH);
    }

    public static void preModify() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Properties.TRACER_COV_TYPE = "meth-cov";
        Properties.FILE_CHECKSUM = Properties.RTSVariant.HyRTS + "-checksum";
        Properties.NEW_CLASSPATH = "C:\\Users\\lrjia\\codeUnSync\\my-hyrts\\target\\classes\\set\\hyrts\\demo\\src\\";

        int step = 3;

        if (step == 1) {
            //rebuild
            serializeOldCode();
            instrumentAll();
        } else if (step == 2) {
            execTestCase();
        } else if (step == 3) {
            //modify demo file and rebuild
            Properties.OLD_DIR = "./diff_old";
            Properties.NEW_DIR = "./diff_new";
            System.out.println(HybridRTS.main());
        }


    }
}

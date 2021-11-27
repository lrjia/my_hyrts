package demo;

import set.hyrts.coverage.core.ClassTransformVisitor;
import set.hyrts.coverage.core.CoverageData;
import demo.examCode.src.ClassB;
import demo.examCode.src.parent;
import demo.examCode.src.son;
import demo.examCode.testCase.TestCase1;
import demo.examCode.testCase.TestCase2;
import set.hyrts.diff.ClassFileHandler;
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
import java.util.Set;

import static set.hyrts.coverage.junit.FTracerJUnitUtils.dumpCoverage;

public class RunDemo {

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
            Set<ClassFileHandler> set= VersionDiff.parseClassPath(Properties.NEW_CLASSPATH);
            for (ClassFileHandler c :set){
                String className=c.className.replace("\\",".");
                className=className.substring(className.indexOf(targetPacketName));
                System.out.println(className);
                instrumentBytecode(c.filePath, className);
            }
        } catch (IOException e) {
            System.err.println("插桩错误");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("遍历插桩类路径错误");
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

    public static String targetPacketName;

    public static void main(String[] args) throws Exception {
        Properties.TRACER_COV_TYPE = "meth-cov";
        Properties.FILE_CHECKSUM = Properties.RTSVariant.HyRTS + "-checksum";
        Properties.NEW_CLASSPATH = "target/classes/demo/examCode/src/";
        targetPacketName="demo.examCode.src";

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

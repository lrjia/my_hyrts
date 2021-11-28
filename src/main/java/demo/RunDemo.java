package demo;

import set.hyrts.coverage.core.ClassTransformVisitor;
import set.hyrts.coverage.core.CoverageData;
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


public class RunDemo {

    public static boolean writeBack = true;

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
        if (writeBack) {
            File newFile = new File(classDir);
            new FileOutputStream(newFile).write(newClass);
        }
    }

    public static void instrumentAll() {
        try {
            CoverageData.reset();
            Set<ClassFileHandler> set = VersionDiff.parseClassPath(Properties.NEW_CLASSPATH);

            for (ClassFileHandler c : set) {
                String className = c.className.replace("\\", ".");
                className = className.substring(className.indexOf(TARGET_PACKET_NAME));
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


    public static void serializeOldCode() throws Exception {
        VersionDiff.compute(null, "./diff_old", Properties.NEW_CLASSPATH);
    }


    public static void init() {
        Properties.TRACER_COV_TYPE = "meth-cov";
        Properties.FILE_CHECKSUM = Properties.RTSVariant.HyRTS + "-checksum";
        Properties.NEW_CLASSPATH = TARGET_CLASSPATH;
    }

    static void deleteDir(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles())
                deleteDir(f);
        }
        file.delete();
    }


    /**
     * 删除之前记录的插桩信息，类校验和
     */
    static void clean() {
        deleteDir(new File("./diff_old"));
        deleteDir(new File("./diff_new"));
        new File("./diff_old").mkdir();
        new File("./diff_new").mkdir();
    }


    /**
     * 需要插桩的类路径，会将该路径下的所有类插桩
     */
    static final String TARGET_CLASSPATH = "target/classes/demo/examCode/src/";


    /**
     * 被插桩类的包名
     */
    static final String TARGET_PACKET_NAME = "demo.examCode.src";

    public static void main(String[] args) throws Exception {
        init();
        switch (args[0]) {
            case "instrument":
                //插桩代码，如果已经插桩过应该先执行 maven clean
                //插桩代码之后应该运行测试，执行 maven surefire:test
                clean();
                serializeOldCode();//记录类校验和
                instrumentAll();
                break;
            case "select":
                //运行前修改源代码，并执行 maven clean
                Properties.OLD_DIR = "./diff_old";
                Properties.NEW_DIR = "./diff_new";
                System.out.println(HybridRTS.main());
                break;
            case "clean":
                clean();
                break;
            default:
                System.err.println("unknown");
                break;
        }
    }
}

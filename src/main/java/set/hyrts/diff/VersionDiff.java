package set.hyrts.diff;

import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.utils.Properties;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class VersionDiff {
    public static Map<String, String> oldClasses = new HashMap();
    public static Map<String, String> newClasses = new HashMap();
    public static Map<String, String> oldClassHeaders = new HashMap();
    public static Map<String, String> newClassHeaders = new HashMap();
    public static Map<String, Map<String, String>> oldClassMeths = new HashMap();
    public static Map<String, Map<String, String>> newClassMeths = new HashMap();
    public static Set<String> deletedFiles = new HashSet();
    public static Set<String> addedFiles = new HashSet();
    public static Set<String> changedFiles = new HashSet();
    public static Set<String> classHeaderChanges = new HashSet();
    public static String OLDDIR;
    public static Set<String> transformedClassChanges = new HashSet();
    public static Set<String> AIMs = new HashSet();
    public static Set<String> DIMs = new HashSet();
    public static Set<String> CIMs = new HashSet();
    public static Set<String> ASMs = new HashSet();
    public static Set<String> DSMs = new HashSet();
    public static Set<String> CSMs = new HashSet();
    public static Set<String> AIs = new HashSet();
    public static Set<String> DIs = new HashSet();
    public static Set<String> CIs = new HashSet();
    public static Set<String> ASIs = new HashSet();
    public static Set<String> DSIs = new HashSet();
    public static Set<String> CSIs = new HashSet();
    public static Set<String> LCs = new HashSet();
    public static Set<String> BLKs = new HashSet();
    public static boolean transDSI = false;
    public static boolean transASI = false;
    public static boolean transCSI = false;
    public static boolean transDI = false;
    public static boolean transAI = false;
    public static boolean transCI = false;
    public static boolean transDSM = false;
    public static boolean transASM = false;
    public static boolean transCSM = false;
    public static boolean transDIM = false;
    public static boolean transAIM = false;
    public static boolean transCIM = false;
    private static Logger logger = Logger.getLogger(VersionDiff.class);

    static void clearContents() {
        oldClasses.clear();
        newClasses.clear();
        oldClassMeths.clear();
        newClassMeths.clear();
        oldClassHeaders.clear();
        newClassHeaders.clear();
        ClassInheritanceGraph.clear();
    }

    static void clearChanges() {
        deletedFiles.clear();
        addedFiles.clear();
        changedFiles.clear();
        AIMs.clear();
        DIMs.clear();
        CIMs.clear();
        ASMs.clear();
        DSMs.clear();
        CSMs.clear();
        AIs.clear();
        DIs.clear();
        CIs.clear();
        ASIs.clear();
        DSIs.clear();
        CSIs.clear();
        LCs.clear();
        BLKs.clear();
        transformedClassChanges.clear();
        classHeaderChanges.clear();
    }

    public static void main(String[] args) throws Exception {
        String oldDir = args[0];
        String newDir = args[1];
        String newClassPath = args[2];
        Properties.TRACER_COV_TYPE = "meth-cov";
        Properties.FILE_CHECKSUM = Properties.RTSVariant.HyRTS + "-checksum";
        compute(oldDir, newDir, newClassPath);
    }

    public static void compute(String oldDir, String newDir, String newClassPath) throws Exception {
        OLDDIR = oldDir;
        clearContents();
        clearChanges();
        if (oldDir != null) {
            ClassContentParser.deserializeOldContents(oldDir);
        }

        ClassContentParser.parseAndSerializeNewContents(parseClassPath(newClassPath), newDir);
        diff();
        clearContents();
        System.out.println("[HyRTS] Extracted changes: " + deletedFiles.size() + " " + addedFiles.size() + " " + changedFiles.size() + " " + classHeaderChanges.size() + " " + DSIs.size() + " " + ASIs.size() + " " + CSIs.size() + " " + DIs.size() + " " + AIs.size() + " " + CIs.size() + " " + DSMs.size() + " " + ASMs.size() + " " + CSMs.size() + " " + DIMs.size() + " " + AIMs.size() + " " + CIMs.size() + " " + getCorrespondingClassNum(DSIs) + " " + getCorrespondingClassNum(ASIs) + " " + getCorrespondingClassNum(CSIs) + " " + getCorrespondingClassNum(DIs) + " " + getCorrespondingClassNum(AIs) + " " + getCorrespondingClassNum(CIs) + " " + getCorrespondingClassNum(DSMs) + " " + getCorrespondingClassNum(ASMs) + " " + getCorrespondingClassNum(CSMs) + " " + getCorrespondingClassNum(DIMs) + " " + getCorrespondingClassNum(AIMs) + " " + getCorrespondingClassNum(CIMs));
        Iterator var3 = addedFiles.iterator();

        String blk;
        while (var3.hasNext()) {
            blk = (String) var3.next();
            logger.info("Added files: " + blk);
        }

        var3 = changedFiles.iterator();

        while (var3.hasNext()) {
            blk = (String) var3.next();
            logger.info("Changed files: " + blk);
        }

        var3 = deletedFiles.iterator();

        while (var3.hasNext()) {
            blk = (String) var3.next();
            logger.info("Deleted files: " + blk);
        }

        var3 = ASMs.iterator();

        while (var3.hasNext()) {
            blk = (String) var3.next();
            logger.info("Added static method: " + blk);
        }

        var3 = CSMs.iterator();

        while (var3.hasNext()) {
            blk = (String) var3.next();
            logger.info("Changed static method: " + blk);
        }

        var3 = DSMs.iterator();

        while (var3.hasNext()) {
            blk = (String) var3.next();
            logger.info("Deleted static method: " + blk);
        }

        var3 = AIMs.iterator();

        while (var3.hasNext()) {
            blk = (String) var3.next();
            logger.info("Added instance method: " + blk);
        }

        var3 = CIMs.iterator();

        while (var3.hasNext()) {
            blk = (String) var3.next();
            logger.info("Changed instance method: " + blk);
        }

        var3 = DIMs.iterator();

        while (var3.hasNext()) {
            blk = (String) var3.next();
            logger.info("Deleted instance method: " + blk);
        }

        var3 = LCs.iterator();

        while (var3.hasNext()) {
            blk = (String) var3.next();
            logger.info("Method lookup changes: " + blk);
        }

        var3 = BLKs.iterator();

        while (var3.hasNext()) {
            blk = (String) var3.next();
            System.out.println("[HyRTS]  Basic block changes: " + blk);
        }

    }

    public static String getRatio(int top, int bottom) {
        if (bottom == 0) {
            return "0.00%";
        } else {
            DecimalFormat format = new DecimalFormat("0.00");
            return format.format((long) (top * 100 / bottom)) + "%";
        }
    }

    public static int getCorrespondingClassNum(Set<String> input) {
        Set<String> output = new HashSet();
        Iterator var2 = input.iterator();

        while (var2.hasNext()) {
            String in = (String) var2.next();
            String[] items = in.split(":");
            output.add(items[0]);
        }

        return output.size();
    }

    public static Set<ClassFileHandler> parseClassPath(String path) throws Exception {
        Set<ClassFileHandler> result = new HashSet();
        String[] items = path.split(":");
        String[] var3 = items;
        int var4 = items.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String item = var3[var5];
            Set<ClassFileHandler> set = null;
            if (item.endsWith(".jar")) {
                set = parseJarFile(item);
            } else {
                set = parseClassDir(item);
            }

            result.addAll(set);
            set.clear();
        }

        if (result.size() == 0) {
            System.out.println("[HyRTS] No class files under analysis: property \"" + Properties.NEW_CLASSPATH_KEY + "\" must be set correctly");
        }

        return result;
    }

    public static Set<ClassFileHandler> parseJarFile(String jarPath) throws IOException, Exception {
        JarFile f = new JarFile(jarPath);
        Set<ClassFileHandler> result = new HashSet();
        Enumeration entries = f.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = (JarEntry) entries.nextElement();
            String entryName = entry.getName();
            if (entryName.endsWith(".class")) {
                String className = entryName.replace(".class", "");
                ClassFileHandler file = new ClassFileHandler(className, f);
                result.add(file);
            }
        }

        return result;
    }

    public static Set<ClassFileHandler> parseClassDir(String dir) throws IOException, Exception {
        Set<ClassFileHandler> result = new HashSet();
        File dirFile = new File(dir);
        List<File> workList = new ArrayList();
        workList.add(dirFile);

        while (true) {
            while (!workList.isEmpty()) {
                File curF = (File) workList.remove(0);
                if (curF.getName().endsWith(".class")) {
                    String className = getClassName(dir, curF);
                    ClassFileHandler file = new ClassFileHandler(className, curF.getAbsolutePath());
                    result.add(file);
                } else if (curF.isDirectory()) {
                    File[] var5 = curF.listFiles();
                    int var6 = var5.length;

                    for (int var7 = 0; var7 < var6; ++var7) {
                        File f = var5[var7];
                        workList.add(f);
                    }
                }
            }

            return result;
        }
    }

    public static String getClassName(String dir, File classFile) {
        String absolutePath = classFile.getAbsolutePath();
        String className = absolutePath.substring(dir.length() + 1).replace(".class", "");
        return className;
    }

    public static void diff() throws IOException {
        Iterator var0;
        String clazz;
        for (var0 = oldClasses.keySet().iterator(); var0.hasNext(); newClasses.remove(clazz)) {
            clazz = (String) var0.next();
            if (!newClasses.containsKey(clazz)) {
                deletedFiles.add(clazz);
            } else if (!((String) oldClasses.get(clazz)).equals(newClasses.get(clazz))) {
                changedFiles.add(clazz);
                if (!Properties.TRACER_COV_TYPE.endsWith("class-cov")) {
                    AtomicLevelDiff.diff(clazz);
                }
            }
        }

        var0 = newClasses.keySet().iterator();

        while (var0.hasNext()) {
            clazz = (String) var0.next();
            addedFiles.add(clazz);
        }

    }
}

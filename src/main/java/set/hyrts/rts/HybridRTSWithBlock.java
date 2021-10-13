package set.hyrts.rts;

import com.google.common.io.Files;
import set.hyrts.coverage.io.TracerIO;
import set.hyrts.diff.VersionDiff;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.utils.Properties;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class HybridRTSWithBlock {
    public static final String CLASS = "set/hyrts/rts/HybridRTSWithBlock";
    private static Logger logger = Logger.getLogger(HybridRTSWithBlock.class);

    public static Set<String> main() throws Exception {
        long startTime = System.currentTimeMillis();
        Set<String> excluded = new HashSet();
        VersionDiff.compute(Properties.OLD_DIR, Properties.NEW_DIR, Properties.NEW_CLASSPATH);
        if (Properties.OLD_DIR == null) {
            System.out.println("[HyRTS] No RTS analysis due to no old coverage, but is computing coverage info and checksum info for future RTS...");
            return excluded;
        } else {
            Map<String, Set<String>> old_cov_map = TracerIO.loadCovFromDirectory(Properties.OLD_DIR);
            if (old_cov_map == null) {
                return excluded;
            } else {
                Iterator var4 = old_cov_map.keySet().iterator();

                while (var4.hasNext()) {
                    String test = (String) var4.next();
                    if (!VersionDiff.changedFiles.contains(test.replace(".", "/")) && !isAffected((Set) old_cov_map.get(test))) {
                        excluded.add(test);
                        if (!Properties.OLD_DIR.equals(Properties.NEW_DIR)) {
                            File oldV = new File(TracerIO.getTestCovFilePath(Properties.OLD_DIR, test));
                            File newV = new File(TracerIO.getTestCovFilePath(Properties.NEW_DIR, test));
                            Files.copy(oldV, newV);
                        }
                    }
                }

                long endTime = System.currentTimeMillis();
                System.out.println("[HyRTS] RTS excluded " + excluded.size() + " out of " + old_cov_map.keySet().size() + " test classes using " + (endTime - startTime) + "ms ");
                return excluded;
            }
        }
    }

    public static boolean isAffected(Set<String> testCov) {
        Set<String> classCov = HybridRTS.preprocessClass(testCov);
        Set<String> methCov = processMethods(testCov);
        Set<String> blkCov = processBlocks(testCov);
        logger.debug("Class dependency: " + classCov);
        Iterator var4 = VersionDiff.deletedFiles.iterator();

        String block;
        do {
            if (!var4.hasNext()) {
                var4 = VersionDiff.classHeaderChanges.iterator();

                do {
                    if (!var4.hasNext()) {
                        var4 = VersionDiff.transformedClassChanges.iterator();

                        do {
                            if (!var4.hasNext()) {
                                var4 = VersionDiff.DSIs.iterator();

                                do {
                                    if (!var4.hasNext()) {
                                        var4 = VersionDiff.DIs.iterator();

                                        do {
                                            if (!var4.hasNext()) {
                                                var4 = VersionDiff.DSMs.iterator();

                                                do {
                                                    if (!var4.hasNext()) {
                                                        var4 = VersionDiff.LCs.iterator();

                                                        do {
                                                            if (!var4.hasNext()) {
                                                                var4 = VersionDiff.BLKs.iterator();

                                                                do {
                                                                    if (!var4.hasNext()) {
                                                                        return false;
                                                                    }

                                                                    block = (String) var4.next();
                                                                } while (!blkCov.contains(block));

                                                                return true;
                                                            }

                                                            block = (String) var4.next();
                                                        } while (!methCov.contains(block));

                                                        return true;
                                                    }

                                                    block = (String) var4.next();
                                                } while (!methCov.contains(block));

                                                return true;
                                            }

                                            block = (String) var4.next();
                                        } while (!methCov.contains(block));

                                        return true;
                                    }

                                    block = (String) var4.next();
                                } while (!methCov.contains(block));

                                return true;
                            }

                            block = (String) var4.next();
                        } while (!classCov.contains(block));

                        return true;
                    }

                    block = (String) var4.next();
                } while (!classCov.contains(block));

                return true;
            }

            block = (String) var4.next();
        } while (!classCov.contains(block));

        return true;
    }

    public static Set<String> processMethods(Set<String> testCov) {
        Set<String> set = new HashSet();
        Iterator var2 = testCov.iterator();

        while (var2.hasNext()) {
            String content = (String) var2.next();
            String[] items = content.split(" ");
            set.add(items[0]);
        }

        return set;
    }

    public static Set<String> processBlocks(Set<String> testCov) {
        Set<String> set = new HashSet();
        Iterator var2 = testCov.iterator();

        while (true) {
            String[] items;
            do {
                if (!var2.hasNext()) {
                    return set;
                }

                String content = (String) var2.next();
                items = content.split(" ");
            } while (items.length <= 1);

            for (int i = 1; i < items.length; ++i) {
                set.add(items[0] + ":" + items[i]);
            }
        }
    }
}

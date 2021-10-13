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

public class HybridRTS {
    public static final String CLASS = "set/hyrts/rts/HybridRTS";
    private static Logger logger = Logger.getLogger(HybridRTS.class);

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
                    if (!VersionDiff.changedFiles.contains(test.replace(".", "/")) && !isAffected((Set) old_cov_map.get(test), Properties.TRACER_COV_TYPE)) {
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

    public static boolean isAffected(Set<String> testCov, String covType) {
        Set classCov;
        if (covType.endsWith("class-cov")) {
            classCov = testCov;
        } else {
            classCov = preprocessClass(testCov);
        }

        logger.debug("Class dependency: " + classCov);
        Iterator var3 = VersionDiff.deletedFiles.iterator();

        String cm;
        do {
            if (!var3.hasNext()) {
                if (Properties.TRACER_COV_TYPE.endsWith("class-cov")) {
                    var3 = VersionDiff.changedFiles.iterator();

                    do {
                        if (!var3.hasNext()) {
                            return false;
                        }

                        cm = (String) var3.next();
                    } while (!classCov.contains(cm));

                    return true;
                }

                var3 = VersionDiff.classHeaderChanges.iterator();

                do {
                    if (!var3.hasNext()) {
                        var3 = VersionDiff.transformedClassChanges.iterator();

                        do {
                            if (!var3.hasNext()) {
                                var3 = VersionDiff.CSIs.iterator();

                                do {
                                    if (!var3.hasNext()) {
                                        var3 = VersionDiff.DSIs.iterator();

                                        do {
                                            if (!var3.hasNext()) {
                                                var3 = VersionDiff.CIs.iterator();

                                                do {
                                                    if (!var3.hasNext()) {
                                                        var3 = VersionDiff.DIs.iterator();

                                                        do {
                                                            if (!var3.hasNext()) {
                                                                var3 = VersionDiff.CIMs.iterator();

                                                                do {
                                                                    if (!var3.hasNext()) {
                                                                        var3 = VersionDiff.CSMs.iterator();

                                                                        do {
                                                                            if (!var3.hasNext()) {
                                                                                var3 = VersionDiff.DSMs.iterator();

                                                                                do {
                                                                                    if (!var3.hasNext()) {
                                                                                        var3 = VersionDiff.LCs.iterator();

                                                                                        do {
                                                                                            if (!var3.hasNext()) {
                                                                                                return false;
                                                                                            }

                                                                                            cm = (String) var3.next();
                                                                                        } while (!testCov.contains(cm));

                                                                                        return true;
                                                                                    }

                                                                                    cm = (String) var3.next();
                                                                                } while (!testCov.contains(cm));

                                                                                return true;
                                                                            }

                                                                            cm = (String) var3.next();
                                                                        } while (!testCov.contains(cm));

                                                                        return true;
                                                                    }

                                                                    cm = (String) var3.next();
                                                                } while (!testCov.contains(cm));

                                                                return true;
                                                            }

                                                            cm = (String) var3.next();
                                                        } while (!testCov.contains(cm));

                                                        return true;
                                                    }

                                                    cm = (String) var3.next();
                                                } while (!testCov.contains(cm));

                                                return true;
                                            }

                                            cm = (String) var3.next();
                                        } while (!testCov.contains(cm));

                                        return true;
                                    }

                                    cm = (String) var3.next();
                                } while (!testCov.contains(cm));

                                return true;
                            }

                            cm = (String) var3.next();
                        } while (!classCov.contains(cm));

                        return true;
                    }

                    cm = (String) var3.next();
                } while (!classCov.contains(cm));

                return true;
            }

            cm = (String) var3.next();
        } while (!classCov.contains(cm));

        return true;
    }

    public static Set<String> preprocessClass(Set<String> content) {
        Set<String> res = new HashSet();
        Iterator var2 = content.iterator();

        while (var2.hasNext()) {
            String item = (String) var2.next();
            res.add(item.substring(0, item.indexOf(":")));
        }

        return res;
    }
}

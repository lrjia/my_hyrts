package set.hyrts.rts;

import com.google.common.io.Files;
import set.hyrts.coverage.io.TracerIO;
import set.hyrts.diff.traditional.TradMethVersionDiff;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.utils.Properties;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MethRTS {
    public static final String CLASS = "set/hyrts/rts/MethRTS";
    private static Logger logger = Logger.getLogger(MethRTS.class);

    public static Set<String> main() throws Exception {
        long startTime = System.currentTimeMillis();
        Set<String> excluded = new HashSet();
        TradMethVersionDiff.compute(Properties.OLD_DIR, Properties.NEW_DIR, Properties.NEW_CLASSPATH);
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
                    if (!TradMethVersionDiff.changedFiles.contains(test.replace(".", "/")) && !isAffected((Set) old_cov_map.get(test))) {
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
        logger.debug("Meth dependency: " + testCov);
        Iterator var1 = TradMethVersionDiff.CSIs.iterator();

        String cm;
        do {
            if (!var1.hasNext()) {
                var1 = TradMethVersionDiff.DSIs.iterator();

                do {
                    if (!var1.hasNext()) {
                        var1 = TradMethVersionDiff.CIs.iterator();

                        do {
                            if (!var1.hasNext()) {
                                var1 = TradMethVersionDiff.DIs.iterator();

                                do {
                                    if (!var1.hasNext()) {
                                        var1 = TradMethVersionDiff.CIMs.iterator();

                                        do {
                                            if (!var1.hasNext()) {
                                                var1 = TradMethVersionDiff.CSMs.iterator();

                                                do {
                                                    if (!var1.hasNext()) {
                                                        var1 = TradMethVersionDiff.DSMs.iterator();

                                                        do {
                                                            if (!var1.hasNext()) {
                                                                var1 = TradMethVersionDiff.LCs.iterator();

                                                                do {
                                                                    if (!var1.hasNext()) {
                                                                        return false;
                                                                    }

                                                                    cm = (String) var1.next();
                                                                } while (!testCov.contains(cm));

                                                                return true;
                                                            }

                                                            cm = (String) var1.next();
                                                        } while (!testCov.contains(cm));

                                                        return true;
                                                    }

                                                    cm = (String) var1.next();
                                                } while (!testCov.contains(cm));

                                                return true;
                                            }

                                            cm = (String) var1.next();
                                        } while (!testCov.contains(cm));

                                        return true;
                                    }

                                    cm = (String) var1.next();
                                } while (!testCov.contains(cm));

                                return true;
                            }

                            cm = (String) var1.next();
                        } while (!testCov.contains(cm));

                        return true;
                    }

                    cm = (String) var1.next();
                } while (!testCov.contains(cm));

                return true;
            }

            cm = (String) var1.next();
        } while (!testCov.contains(cm));

        return true;
    }
}

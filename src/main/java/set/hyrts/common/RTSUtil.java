package set.hyrts.common;

import set.hyrts.coverage.agent.JUnitAgent;
import set.hyrts.rts.HybridRTS;
import set.hyrts.rts.HybridRTSWithBlock;
import set.hyrts.rts.MethRTS;
import set.hyrts.utils.Properties;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RTSUtil {
    public static void getRTSRes(AbstractCoverageMojo mojo) {
        Properties.OLD_DIR = mojo.oldVersionLocation;
        Properties.NEW_DIR = mojo.baseDir;
        Properties.NEW_CLASSPATH = mojo.outputDirectory.getAbsolutePath();
        Properties.TRACER_COV_TYPE = mojo.coverageLevel;
        Properties.RTS = mojo.RTS;
        if (Properties.RTS != Properties.RTSVariant.NONE)
            Properties.FILE_CHECKSUM = Properties.RTS + "-checksum";
        Set<String> excluded = new HashSet<>();
        try {
            if (mojo.RTS == Properties.RTSVariant.FRTS || mojo.RTS == Properties.RTSVariant.HyRTS || mojo.RTS == Properties.RTSVariant.HyRTSf) {
                excluded = HybridRTS.main();
            } else if (mojo.RTS == Properties.RTSVariant.HyRTSb) {
                excluded = HybridRTSWithBlock.main();
            } else if (mojo.RTS == Properties.RTSVariant.MRTS) {
                excluded = MethRTS.main();
            }
            serializeJUnit3Excludes(excluded,
                    JUnitAgent.getJUnit3ExcludePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        excluded = toJUnit4SurefireExcludeFormat(excluded);
        System.setProperty("HYRTS_EXCLUDED",
                Arrays.toString(excluded.toArray((Object[]) new String[0])));
    }

    public static void serializeJUnit3Excludes(Set<String> excluded, String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        for (String item : excluded)
            writer.write(item + "\n");
        writer.flush();
        writer.close();
    }

    public static Set<String> deSerializeJUnit3Excludes(String path) throws IOException {
        Set<String> excluded = new HashSet<>();
        File f = new File(path);
        if (!f.exists())
            return excluded;
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        while (line != null) {
            excluded.add(line);
            line = reader.readLine();
        }
        reader.close();
        return excluded;
    }

    public static Set<String> toJUnit4SurefireExcludeFormat(Set<String> excluded) {
        Set<String> transformed = new HashSet<>();
        for (String item : excluded)
            transformed.add(item.replace(".", File.separator) + ".*");
        return transformed;
    }
}

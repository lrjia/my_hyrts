package set.hyrts.coverage.maven;

import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.utils.Properties;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SurefireRewriter {
    public static final String className = "set/hyrts/coverage/maven/SurefireRewriter";
    public static final String HYRTS_EXCLUDED = "HYRTS_EXCLUDED";
    public static ConcurrentMap map = new ConcurrentHashMap();
    private static Logger logger = Logger.getLogger(SurefireRewriter.class);

    public static void execute(Object plugin) throws Exception {
        String className = plugin.getClass().getName();
        if (className.equals("org.apache.maven.plugin.surefire.SurefirePlugin") || className.equals("org.apache.maven.plugin.failsafe.IntegrationTestMojo")) {
            if (map.put(plugin, 1) == null) {
                if (System.getProperty("HYRTS_EXCLUDED") != null) {
                    checkUpdateExcludes(plugin);
                }

                checkUpdateArgLine(plugin);
            }
        }
    }

    private static void checkUpdateExcludes(Object plugin) throws Exception {
        List<String> excludeList = (List) getAttribute("excludes", plugin);
        logger.info("old excludeList: " + excludeList);
        List<String> newExcludeList = new ArrayList(Arrays.asList(System.getProperty("HYRTS_EXCLUDED").replace("[", "").replace("]", "").split(",")));
        if (excludeList != null) {
            newExcludeList.addAll(excludeList);
        }

        setAttribute("excludes", plugin, (List) newExcludeList);
        newExcludeList = (List) getAttribute("excludes", plugin);
        logger.info("new excludeList: " + newExcludeList);
    }

    private static void checkUpdateArgLine(Object plugin) throws Exception {
        String argLine = (String) getAttribute("argLine", plugin);
        String agentJar = getPathToHyRTSJar();
        String agentLine = "-javaagent:" + agentJar + "=" + Properties.AGENT_ARG;
        String newArgLine = agentLine;
        if (argLine != null) {
            newArgLine = agentLine + " " + argLine;
        }

        logger.info(">>>newArgLine: " + newArgLine);
        setAttribute("argLine", plugin, (Object) newArgLine);
    }

    public static String getPathToHyRTSJar() {
        File jarFile = new File(Properties.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String agentJar = jarFile.toString();
        return agentJar;
    }

    protected static void setAttribute(String attr, Object plugin, List<String> list) throws Exception {
        Field localField = null;

        try {
            localField = plugin.getClass().getDeclaredField(attr);
        } catch (NoSuchFieldException var5) {
            localField = plugin.getClass().getSuperclass().getDeclaredField(attr);
        }

        localField.setAccessible(true);
        localField.set(plugin, list);
    }

    protected static Object getAttribute(String name, Object plugin) throws Exception {
        Field localField = null;

        try {
            localField = plugin.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException var4) {
            localField = plugin.getClass().getSuperclass().getDeclaredField(name);
        }

        localField.setAccessible(true);
        return localField.get(plugin);
    }

    protected static void setAttribute(String fieldName, Object plugin, Object value) throws Exception {
        Field field;
        try {
            field = plugin.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException var5) {
            field = plugin.getClass().getSuperclass().getDeclaredField(fieldName);
        }

        field.setAccessible(true);
        field.set(plugin, value);
    }
}

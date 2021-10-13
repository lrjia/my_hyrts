package set.hyrts.coverage.junit;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import set.hyrts.org.apache.commons.codec.digest.DigestUtils;
import set.hyrts.org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @deprecated
 */
@Deprecated
public class JUnitTestMethodListener extends RunListener {
    public static AtomicReference<ConcurrentMap<String, Integer>> coverage_map = new AtomicReference(new ConcurrentHashMap());
    static int test = 0;
    private static Logger logger = Logger.getLogger(JUnitTestMethodListener.class);
    public String lastTestClass = "";

    public static ConcurrentMap<String, Integer> getCoverage() {
        return (ConcurrentMap) coverage_map.get();
    }

    private static String getTestName(Description d) {
        return d.getClassName() + "." + d.getMethodName();
    }

    private static String getTestClassName(Description d) {
        String testMethod = d.getClassName() + "." + d.getMethodName();
        return testMethod.substring(0, testMethod.lastIndexOf("."));
    }

    public void testRunStarted(Description description) throws Exception {
    }

    public void testRunFinished(Result result) throws Exception {
        System.out.println(">>Number of tests executed: " + result.getRunCount());
    }

    public void testStarted(Description description) throws Exception {
    }

    public void testFinished(Description description) throws Exception {
        String testName = getTestName(description);
        System.out.println("Listening " + testName);
    }

    public String getFileName(String test) {
        return DigestUtils.sha1Hex(test);
    }
}

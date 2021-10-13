package set.hyrts.coverage.junit;

import junit.framework.TestSuite;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;
import set.hyrts.coverage.io.TracerIO;
import set.hyrts.coverage.junit.runners.FTracerJUnit3Runner;
import set.hyrts.coverage.junit.runners.FTracerJUnit4Runner;
import set.hyrts.org.apache.commons.codec.digest.DigestUtils;
import set.hyrts.utils.Properties;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FTracerJUnitUtils {
    static final String className = "set/hyrts/coverage/junit/FTracerJUnitUtils";
    static final String transJUnit3SuiteInit = "transformJUnit3SuitInit";
    static final String transJUnit4Runner4Class = "transformJUnit4Runner4Class";
    static final String isExcluded = "isExcluded";
    static final String dumpCov = "dumpCoverage";
    public static Set<String> allTests = new HashSet();
    public static Set<String> runnedTests = new HashSet();
    public static Set<String> excluded = new HashSet();

    public static boolean transformJUnit3SuitInit(TestSuite suite, Class testClass) {
        if (checkStackForJUnit(FTracerJUnit3Runner.class.getName()) == 0 && checkStackForJUnit("org.junit.internal.builders.") == 0) {
            if (FTracerJUnit3Runner.JUnit3Excluded != null && FTracerJUnit3Runner.JUnit3Excluded.contains(testClass.getCanonicalName())) {
                return false;
            } else {
                suite.addTest(new FTracerJUnit3Runner(testClass));
                return false;
            }
        } else {
            return true;
        }
    }

    public static Runner transformJUnit4Runner4Class(RunnerBuilder builder, Class testClass, String runnerClass) throws Throwable {
        if (checkStackForJUnit(FTracerJUnitUtils.class.getName()) > 2) {
            return builder.runnerForClass(testClass);
        } else {
            Runner runner = builder.runnerForClass(testClass);
            return new FTracerJUnit4Runner(runner, testClass);
        }
    }

    public static void dumpCoverage(Description d) throws IOException {
        String test = d.getClassName() + "." + d.getMethodName();
        dumpCoverage(test);
    }

    public static void dumpCoverage(Class<?> testClass) throws IOException {
        String test = testClass.getCanonicalName();
        dumpCoverage(test);
    }

    private static void dumpCoverage(String test) throws IOException {
        if (!Properties.EXECUTION_ONLY && test != null && !excluded.contains(test)) {
            String fileName = TracerIO.getTestCovFilePath(Properties.NEW_DIR, test);
            if (Properties.TRACER_COV_TYPE.endsWith("meth-cov")) {
                if (!Properties.TRACE_RTINFO) {
                    TracerIO.writeMethodCov(test, fileName);
                } else {
                    TracerIO.writeMethodCovWithRT(test, fileName);
                }
            } else if (Properties.TRACER_COV_TYPE.endsWith("class-cov")) {
                TracerIO.writeClassCov(test, fileName);
            } else if (Properties.TRACER_COV_TYPE.endsWith("stmt-cov")) {
                TracerIO.writeStmtCov(test, fileName);
            } else if (Properties.TRACER_COV_TYPE.endsWith("branch-cov")) {
                TracerIO.writeBranchCov(test, fileName);
            } else if (Properties.TRACER_COV_TYPE.endsWith("block-cov")) {
                TracerIO.writeBlockCovWithRT(test, fileName);
            }

        }
    }

    public static String getFileName(String test) {
        return "test-class".equals(Properties.TEST_LEVEL) ? test : DigestUtils.sha1Hex(test);
    }

    public static boolean isParameterizedTest(String testClass) {
        return testClass.startsWith("[");
    }

    private static int checkStackForJUnit(String clazz) {
        StackTraceElement[] arrayOfStackTraceElement1 = Thread.currentThread().getStackTrace();
        int i = 0;
        StackTraceElement[] var3 = arrayOfStackTraceElement1;
        int var4 = arrayOfStackTraceElement1.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            StackTraceElement localStackTraceElement = var3[var5];
            if (localStackTraceElement.getClassName().startsWith(clazz)) {
                ++i;
            }
        }

        return i;
    }
}

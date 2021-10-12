package set.hyrts.coverage.junit.runners;

import java.io.IOException;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import set.hyrts.coverage.junit.FTracerJUnitUtils;

public class FTracerJUnit3Runner implements Test {
   private TestSuite suite;
   private Class testClass;
   public static Set<String> JUnit3Excluded;

   public FTracerJUnit3Runner(Class testClass) {
      this.testClass = testClass;
      this.suite = new TestSuite(testClass);
   }

   public int countTestCases() {
      return this.suite != null ? this.suite.countTestCases() : 0;
   }

   public void run(TestResult result) {
      this.suite.run(result);

      try {
         FTracerJUnitUtils.dumpCoverage(this.testClass);
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }
}

package set.hyrts.coverage.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashSet;
import java.util.Set;
import set.hyrts.coverage.junit.JUnitTestClassLevelTransformer;
import set.hyrts.coverage.junit.JUnitTestMethodLevelTransformer;
import set.hyrts.coverage.junit.runners.FTracerJUnit3Runner;
import set.hyrts.coverage.maven.SurefireTransformer;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.utils.Properties;

public class JUnitAgent {
   private static Logger logger = Logger.getLogger(JUnitAgent.class);

   public static void premain(String args, Instrumentation inst) throws Exception {
      logger.debug("premain executed: " + args);
      Properties.processPreMainArguments(args);
      if (Properties.TRACER_COV_TYPE != null) {
         if (!Properties.EXECUTION_ONLY) {
            inst.addTransformer(new ClassTransformer());
         }

         if ("test-meth".equals(Properties.TEST_LEVEL)) {
            inst.addTransformer(new JUnitTestMethodLevelTransformer());
         } else {
            inst.addTransformer(new JUnitTestClassLevelTransformer());
         }
      }

      updateJUnit3Excludes();
   }

   public static void agentmain(String args, Instrumentation inst) {
      logger.debug("agentmain executed");
      Properties.processAgentMainArguments(args);
      inst.addTransformer(new SurefireTransformer(), true);

      try {
         Class[] var2 = inst.getAllLoadedClasses();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Class localClass = var2[var4];
            String str = localClass.getName();
            if (str.equals("org.apache.maven.plugin.surefire.AbstractSurefireMojo") || str.equals("org/apache/maven/plugin/surefire/SurefirePlugin") || str.equals("org/apache/maven/plugin/failsafe/IntegrationTestMojo") || str.equals("org/scalatest/tools/maven/TestMojo")) {
               inst.retransformClasses(new Class[]{localClass});
            }
         }
      } catch (UnmodifiableClassException var7) {
      }

   }

   public static void updateJUnit3Excludes() throws IOException {
      String path = getJUnit3ExcludePath();
      File f = new File(path);
      if (f.exists()) {
         Set<String> excluded = new HashSet();
         BufferedReader reader = new BufferedReader(new FileReader(path));

         for(String line = reader.readLine(); line != null; line = reader.readLine()) {
            excluded.add(line);
         }

         reader.close();
         FTracerJUnit3Runner.JUnit3Excluded = excluded;
         f.delete();
      }
   }

   public static String getJUnit3ExcludePath() {
      String dir = Properties.NEW_DIR + File.separator + "hyrts-files";
      File file = new File(dir);
      file.mkdirs();
      return dir + File.separator + ".exclude";
   }
}

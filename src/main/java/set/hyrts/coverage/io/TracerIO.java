package set.hyrts.coverage.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import set.hyrts.coverage.core.CoverageData;
import set.hyrts.coverage.junit.FTracerJUnitUtils;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.utils.Classes;
import set.hyrts.utils.Properties;

public class TracerIO {
   public static final String CLASS = "set/hyrts/coverage/io/TracerIO";
   static Logger logger = Logger.getLogger(TracerIO.class);

   public static String getCovDir() {
      getFTracerDir();
      String dirName = "hyrts-files" + File.separator + Properties.TRACER_COV_TYPE;
      File covDir = new File(dirName);
      covDir.mkdir();
      return dirName;
   }

   public static String getFTracerDir() {
      String rootDirName = "hyrts-files";
      File rootDir = new File(rootDirName);
      rootDir.mkdir();
      return rootDirName;
   }

   public static void writeClassCov(String testName, String path) throws IOException {
      BufferedWriter writer = new BufferedWriter(new FileWriter(path));
      writer.write(testName + "\n");
      int classNum = CoverageData.classId;

      for(int clazzId = 0; clazzId < classNum; ++clazzId) {
         if (CoverageData.classCovArray[clazzId]) {
            writer.write((String)CoverageData.idClassMap.get(clazzId) + "\n");
            CoverageData.classCovArray[clazzId] = false;
         }
      }

      writer.flush();
      writer.close();
   }

   public static void writeMethodCov(String testName, String path) throws IOException {
      BufferedWriter writer = new BufferedWriter(new FileWriter(path));
      writer.write(testName + "\n");
      int classNum = CoverageData.classId;

      for(int clazzId = 0; clazzId < classNum; ++clazzId) {
         boolean[] meths = CoverageData.methCovArray[clazzId];
         if (meths != null) {
            int methNum = meths.length;

            for(int i = 0; i < methNum; ++i) {
               if (meths[i]) {
                  writer.write((String)CoverageData.idClassMap.get(clazzId) + ":" + (String)((ConcurrentMap)CoverageData.idMethMap.get(clazzId)).get(i) + "\n");
                  meths[i] = false;
               }
            }
         }
      }

      writer.flush();
      writer.close();
   }

   public static void writeMethodCovWithRT(String testName, String path) throws IOException {
      BufferedWriter writer = new BufferedWriter(new FileWriter(path));
      writer.write(testName + "\n");
      int classNum = CoverageData.classId;

      for(int clazzId = 0; clazzId < classNum; ++clazzId) {
         boolean[] meths = CoverageData.methCovArray[clazzId];
         if (meths != null) {
            int methNum = meths.length;

            for(int i = 0; i < methNum; ++i) {
               if (meths[i]) {
                  writer.write((String)CoverageData.idClassMap.get(clazzId) + ":" + (String)((ConcurrentMap)CoverageData.idMethMap.get(clazzId)).get(i) + "\n");
                  meths[i] = false;
               }
            }
         }
      }

      Iterator var9 = CoverageData.rtType.keySet().iterator();

      while(var9.hasNext()) {
         String rType = (String)var9.next();
         String slashClazzName = Classes.toSlashClassName(rType);
         Iterator var12 = ((Set)CoverageData.rtType.get(rType)).iterator();

         while(var12.hasNext()) {
            String meth = (String)var12.next();
            writer.write(slashClazzName + ":" + meth + "\n");
         }

         ((Set)CoverageData.rtType.get(rType)).clear();
      }

      writer.flush();
      writer.close();
   }

   public static void writeStmtCov(String testName, String path) throws IOException {
      BufferedWriter writer = new BufferedWriter(new FileWriter(path));
      writer.write(testName + "\n");
      int classNum = CoverageData.classId;

      for(int clazzId = 0; clazzId < classNum; ++clazzId) {
         BitSet stmts = CoverageData.stmtCovSet[clazzId];
         if (stmts != null) {
            int stmtNum = stmts.size();

            for(int i = 0; i < stmtNum; ++i) {
               if (stmts.get(i)) {
                  writer.write((String)CoverageData.idClassMap.get(clazzId) + ":" + i + "\n");
                  stmts.clear(i);
               }
            }
         }
      }

      writer.flush();
      writer.close();
   }

   public static void writeBranchCov(String testName, String path) throws IOException {
      BufferedWriter writer = new BufferedWriter(new FileWriter(path));
      writer.write(testName + "\n");
      Iterator var3 = CoverageData.branchCov.keySet().iterator();

      while(var3.hasNext()) {
         String branch = (String)var3.next();
         writer.write(branch + "\n");
      }

      CoverageData.branchCov.clear();
      writer.flush();
      writer.close();
   }

   public static void writeBlockCovWithRT(String testName, String path) throws IOException {
      BufferedWriter writer = new BufferedWriter(new FileWriter(path));
      writer.write(testName + "\n");
      int classNum = CoverageData.classId;

      int clazzId;
      int methNum;
      int i;
      for(clazzId = 0; clazzId < classNum; ++clazzId) {
         BitSet[] meths = CoverageData.blockCovSet[clazzId];
         if (meths != null) {
            methNum = meths.length;

            for(i = 0; i < methNum; ++i) {
               if (CoverageData.methCovArray[clazzId][i]) {
                  writer.write((String)CoverageData.idClassMap.get(clazzId) + ":" + (String)((ConcurrentMap)CoverageData.idMethMap.get(clazzId)).get(i));
                  BitSet labels = meths[i];

                  for(int j = 0; j < labels.size(); ++j) {
                     if (labels.get(j)) {
                        writer.write(" " + j);
                        labels.clear(j);
                     }
                  }

                  writer.write("\n");
               }
            }
         }
      }

      for(clazzId = 0; clazzId < classNum; ++clazzId) {
         boolean[] meths = CoverageData.methCovArray[clazzId];
         if (meths != null) {
            methNum = meths.length;

            for(i = 0; i < methNum; ++i) {
               if (meths[i]) {
                  meths[i] = false;
               }
            }
         }
      }

      Iterator var10 = CoverageData.rtType.keySet().iterator();

      while(var10.hasNext()) {
         String rType = (String)var10.next();
         String slashClazzName = Classes.toSlashClassName(rType);
         Iterator var14 = ((Set)CoverageData.rtType.get(rType)).iterator();

         while(var14.hasNext()) {
            String meth = (String)var14.next();
            writer.write(slashClazzName + ":" + meth + "\n");
         }

         ((Set)CoverageData.rtType.get(rType)).clear();
      }

      writer.flush();
      writer.close();
   }

   public static Map<String, Set<String>> loadCovFromDirectory(String dirPath) throws IOException {
      File dir = new File(dirPath + File.separator + "hyrts-files" + File.separator + Properties.TRACER_COV_TYPE);
      logger.debug("Loading from " + dir);
      if (!dir.exists()) {
         logger.info("Directory does not exist: " + dir);
         return null;
      } else {
         File[] tests = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
               return name.endsWith(".gz");
            }
         });
         Map<String, Set<String>> result = new HashMap();
         File[] var4 = tests;
         int var5 = tests.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            File f = var4[var6];
            loadCoverage(f, result);
         }

         return result;
      }
   }

   public static void loadCoverage(File file, Map<String, Set<String>> result) throws IOException {
      Set<String> coveredElements = new HashSet();
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String testName = reader.readLine();

      for(String line = reader.readLine(); line != null; line = reader.readLine()) {
         coveredElements.add(line);
      }

      reader.close();
      result.put(testName, coveredElements);
   }

   public static void cleanObsoleteTestCov() {
      System.out.println("[HyRTS] Run " + FTracerJUnitUtils.runnedTests.size() + "/" + FTracerJUnitUtils.allTests.size() + " tests for " + System.getProperty("user.dir"));
      File dir = new File("hyrts-files" + File.separator + Properties.TRACER_COV_TYPE);
      if (dir.exists()) {
         File[] tests = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
               return name.endsWith(".gz");
            }
         });
         int testCovNum = tests.length;
         File[] var3 = tests;
         int var4 = tests.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            File test = var3[var5];
            String testName = test.getName().replace(".gz", "");
            if (!FTracerJUnitUtils.allTests.contains(testName)) {
               test.delete();
               --testCovNum;
            }
         }

         if (!Properties.EXECUTION_ONLY) {
            if (FTracerJUnitUtils.allTests.size() != testCovNum) {
               System.out.println("[HyRTS] ERROR: test coverage set (" + testCovNum + ") inconsistent with mvn test set (" + FTracerJUnitUtils.allTests.size() + ")");
            } else {
               System.out.println("[HyRTS] Test coverage set consistent with mvn test set: total " + testCovNum + ", reran " + FTracerJUnitUtils.runnedTests.size());
            }
         }

      }
   }

   public static String getTestCovFilePath(String dirPath, String test) {
      File rootDir = new File(dirPath + File.separator + "hyrts-files" + File.separator + Properties.TRACER_COV_TYPE);
      rootDir.mkdirs();
      return rootDir.getAbsolutePath() + File.separator + FTracerJUnitUtils.getFileName(test) + ".gz";
   }

   public static String getHyRTSCovDir(String dirPath) {
      File rootDir = new File(dirPath + File.separator + "hyrts-files" + File.separator + Properties.TRACER_COV_TYPE);
      rootDir.mkdirs();
      return rootDir.getAbsolutePath();
   }
}

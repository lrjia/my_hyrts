package set.hyrts.coverage.core;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class Tracer {
   public static final String TRACE_CLASS_COV = "traceClassCovInfo";
   public static final String TRACE_METH_COV = "traceMethCovInfo";
   public static final String TRACE_METH_COV_RT = "traceMethCovInfoWithRT";
   public static final String TRACE_CLINIT = "traceMethCovInfoClinit";
   public static final String TRACE_STMT_COV = "traceStmtCovInfo";
   public static final String TRACE_BLOCK_COV = "traceBlockCovInfo";
   public static final String TRACE_BRANCH_COV = "traceBranchCovInfo";

   public static void traceClassCovInfo(int clazzId) {
      CoverageData.classCovArray[clazzId] = true;
   }

   public static void traceClassCovInfo(String clazz) {
      Integer cachedId = (Integer)CoverageData.stringTypeCache.get(clazz);
      if (cachedId != null) {
         if (cachedId > 0) {
            CoverageData.classCovArray[cachedId] = true;
         }
      } else {
         Integer clazzId = (Integer)CoverageData.classIdMap.get(clazz);
         if (clazzId != null) {
            CoverageData.classCovArray[clazzId] = true;
            CoverageData.stringTypeCache.put(clazz, clazzId);
         } else {
            CoverageData.stringTypeCache.put(clazz, 0);
         }
      }

   }

   public static void traceClassCovInfo(Object clazz) {
      if (clazz != null) {
         traceClassCovInfo(clazz.getClass());
      }

   }

   public static void traceClassCovInfo(Object clazz, String field) {
      Integer cachedId = (Integer)CoverageData.fieldTypeCache.get(field);
      if (cachedId != null) {
         if (cachedId > 0) {
            CoverageData.classCovArray[cachedId] = true;
         }
      } else if (clazz != null) {
         String name = clazz.getClass().getName();
         Integer clazzId = (Integer)CoverageData.dotClassIdMap.get(name);
         if (clazzId != null) {
            CoverageData.classCovArray[clazzId] = true;
            CoverageData.fieldTypeCache.put(field, clazzId);
         } else {
            CoverageData.fieldTypeCache.put(field, 0);
         }
      } else {
         CoverageData.fieldTypeCache.put(field, 0);
      }

   }

   public static void traceClassCovInfo(Class<?> clazz) {
      Integer cachedId = (Integer)CoverageData.classTypeCache.get(clazz);
      if (cachedId != null) {
         if (cachedId > 0) {
            CoverageData.classCovArray[cachedId] = true;
         }
      } else {
         String name = clazz.getName();
         Integer clazzId = (Integer)CoverageData.dotClassIdMap.get(name);
         if (clazzId != null) {
            CoverageData.classCovArray[clazzId] = true;
            CoverageData.classTypeCache.put(clazz, clazzId);
         } else {
            CoverageData.classTypeCache.put(clazz, 0);
         }
      }

   }

   public static void traceMethCovInfo(int clazzId, int methId) {
      CoverageData.methCovArray[clazzId][methId] = true;
   }

   public static void traceMethCovInfoWithRT(int clazzId, int methId, String dotClazzName, String methName, String rtype) {
      if (!rtype.equals(dotClazzName)) {
         Set<String> set = (Set)CoverageData.rtType.get(rtype);
         if (set == null) {
            Set<String> newSet = new HashSet();
            newSet.add(methName);
            CoverageData.rtType.put(rtype, newSet);
         } else {
            set.add(methName);
         }

      }
   }

   public static void traceMethCovInfoClinit(int clazzId) {
      CoverageData.methCovArray[clazzId][0] = true;
   }

   public static void traceMethCovInfoClinit(String clazz) {
      Integer cachedId = (Integer)CoverageData.stringTypeCache.get(clazz);
      if (cachedId != null) {
         if (cachedId > 0) {
            CoverageData.methCovArray[cachedId][0] = true;
         }
      } else {
         Integer clazzId = (Integer)CoverageData.classIdMap.get(clazz);
         if (clazzId != null) {
            CoverageData.methCovArray[clazzId][0] = true;
            CoverageData.stringTypeCache.put(clazz, clazzId);
         } else {
            CoverageData.stringTypeCache.put(clazz, 0);
         }
      }

   }

   public static void traceMethCovInfoClinit(Class<?> clazz) {
      Integer cachedId = (Integer)CoverageData.classTypeCache.get(clazz);
      if (cachedId != null) {
         if (cachedId > 0) {
            CoverageData.methCovArray[cachedId][0] = true;
         }
      } else {
         String name = clazz.getName();
         Integer clazzId = (Integer)CoverageData.dotClassIdMap.get(name);
         if (clazzId != null) {
            CoverageData.methCovArray[clazzId][0] = true;
            CoverageData.classTypeCache.put(clazz, clazzId);
         } else {
            CoverageData.classTypeCache.put(clazz, 0);
         }
      }

   }

   public static void traceMethCovInfoClinit(Object clazz) {
      if (clazz != null) {
         traceMethCovInfoClinit(clazz.getClass());
      }

   }

   public static void traceStmtCovInfo(int clazzId, int line) {
      CoverageData.stmtCovSet[clazzId].set(line);
   }

   public static void traceBlockCovInfo(int clazzId, int methId, int labelId) {
      CoverageData.blockCovSet[clazzId][methId].set(labelId);
   }

   public static void traceBranchCovInfo(String branch, String id) {
      String key = id;
      ConcurrentMap<String, Integer> methodMap = CoverageData.branchCov;
      if (branch.length() > 0) {
         key = id + "<" + branch + ">";
         if (branch.equals("true")) {
            String resetKey = id + "<false>";
            methodMap.put(resetKey, (Integer)methodMap.get(resetKey) - 1);
            if ((Integer)methodMap.get(resetKey) == 0) {
               methodMap.remove(resetKey);
            }
         }
      }

      if (!methodMap.containsKey(key)) {
         methodMap.put(key, 1);
      } else {
         methodMap.put(key, (Integer)methodMap.get(key) + 1);
      }

   }
}

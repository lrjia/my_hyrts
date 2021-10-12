package set.hyrts.utils;

import java.io.PrintStream;

public class Properties {
   public static final String CLASS = "set/hyrts/utils/Properties";
   public static final String TRACER_ROOT_DIR = "hyrts-files";
   public static final String GZ_EXT = ".gz";
   public static final String HYRTS_TAG = "[HyRTS] ";
   public static final String EXCLUDE = ".exclude";
   public static String TRACER_CLASS = "set/hyrts/coverage/io/Tracer";
   public static String FILE_CHECKSUM = null;
   public static String AGENT_ARG = "";
   public static String COV_TYPE_KEY = "covLevel=";
   public static final String STMT_COV = "stmt-cov";
   public static final String BLK_COV = "block-cov";
   public static final String BRANCH_COV = "branch-cov";
   public static final String CLASS_COV = "class-cov";
   public static final String METH_COV = "meth-cov";
   public static String TRACER_COV_TYPE = null;
   public static String OLD_DIR_KEY = "oldVersion=";
   public static String OLD_DIR = System.getProperty("user.dir");
   public static String NEW_DIR_KEY = "newVersion=";
   public static String NEW_DIR = System.getProperty("user.dir");
   public static String NEW_CLASSPATH_KEY = "bytecodeLocation=";
   public static String NEW_CLASSPATH = "target";
   public static String LEVEL_KEY = "hybridConfig=";
   public static String EXECUTION_ONLY_KEY = "execOnly=";
   public static boolean EXECUTION_ONLY = false;
   public static String TRACE_LIB_KEY = "traceLib=";
   public static boolean TRACE_LIB = false;
   public static String TRACE_RTINFO_KEY = "traceRTInfo=";
   public static boolean TRACE_RTINFO = true;
   public static String DEBUG_MODE_KEY = "debug=";
   public static boolean DEBUG_MODE = false;
   public static String RTS_KEY = "RTS=";
   public static Properties.RTSVariant RTS;
   public static String TEST_LEVEL_KEY;
   public static final String TM_LEVEL = "test-meth";
   public static final String TC_LEVEL = "test-class";
   public static String TEST_LEVEL;

   public static void processAgentMainArguments(String arguments) {
      if (arguments != null) {
         AGENT_ARG = arguments;
      }
   }

   public static void processPreMainArguments(String arguments) {
      if (arguments != null) {
         AGENT_ARG = arguments;
         String[] items = arguments.split(",");
         String[] var2 = items;
         int var3 = items.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String item = var2[var4];
            if (item.startsWith(RTS_KEY)) {
               RTS = Properties.RTSVariant.valueOf(item.replace(RTS_KEY, ""));
            } else if (item.startsWith(COV_TYPE_KEY)) {
               TRACER_COV_TYPE = item.replace(COV_TYPE_KEY, "");
            } else if (item.startsWith(TEST_LEVEL_KEY)) {
               TEST_LEVEL = item.replace(TEST_LEVEL_KEY, "");
            } else if (item.startsWith(TRACE_LIB_KEY)) {
               TRACE_LIB = Boolean.parseBoolean(item.replace(TRACE_LIB_KEY, ""));
            } else if (item.startsWith(TRACE_RTINFO_KEY)) {
               TRACE_RTINFO = Boolean.parseBoolean(item.replace(TRACE_RTINFO_KEY, ""));
            } else if (item.startsWith(NEW_DIR_KEY)) {
               NEW_DIR = item.replace(NEW_DIR_KEY, "");
            } else if (item.startsWith(EXECUTION_ONLY_KEY)) {
               EXECUTION_ONLY = Boolean.parseBoolean(item.replace(EXECUTION_ONLY_KEY, ""));
            } else if (item.startsWith(DEBUG_MODE_KEY)) {
               DEBUG_MODE = Boolean.parseBoolean(item.replace(DEBUG_MODE_KEY, ""));
            }
         }

         if (DEBUG_MODE) {
            PrintStream printer = System.out;
            printer.println("[HyRTS] ===============PreMain Config===============");
            printer.println("[HyRTS] Coverage level: " + TRACER_COV_TYPE);
            printer.println("[HyRTS] Test level: " + TEST_LEVEL);
            printer.println("[HyRTS] Trace library: " + TRACE_LIB);
            printer.println("[HyRTS] With RunTime Info?: " + TRACE_RTINFO);
            printer.println("[HyRTS] New version location: " + NEW_DIR);
            printer.println("[HyRTS] Execution only config: " + EXECUTION_ONLY);
         }

      }
   }

   static {
      RTS = Properties.RTSVariant.NONE;
      TEST_LEVEL_KEY = "testLevel=";
      TEST_LEVEL = "test-class";
   }

   public static enum RTSVariant {
      NONE,
      MRTS,
      HyRTS,
      FRTS,
      HyRTSf,
      HyRTSb;
   }
}

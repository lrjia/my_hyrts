package set.hyrts.logger;

public class FTracerLogger {
   static Levels level;
   static String DebugPrefix;
   static String InfoPrefix;
   static String WarnPrefix;

   public static void debug(String s) {
      if (level == Levels.DEBUG) {
         System.out.println(DebugPrefix + s);
      }

   }

   public static void info(String s) {
      if (level == Levels.DEBUG || level == Levels.INFO) {
         System.out.println(InfoPrefix + s);
      }

   }

   public static void warn(String s) {
      if (level == Levels.DEBUG || level == Levels.INFO || level == Levels.WARN) {
         System.out.println(WarnPrefix + s);
      }

   }

   static {
      level = Levels.DEBUG;
      DebugPrefix = "[HyRTS-DEBUG] ";
      InfoPrefix = "[HyRTS-INFO] ";
      WarnPrefix = "[HyRTS-WARN] ";
   }
}

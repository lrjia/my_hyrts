package set.hyrts.coverage.maven;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import set.hyrts.org.apache.log4j.Logger;

public class DynamicAgentLoader {
   private static final String TOOLS_JAR_NAME = "tools.jar";
   private static final String CLASSES_JAR_NAME = "classes.jar";
   private static final String LOADED = DynamicAgentLoader.class.getName() + " Loaded";
   private static Logger logger = Logger.getLogger(DynamicAgentLoader.class);

   public static boolean loadDynamicAgent(String jarURL, String arguments) {
      try {
         if (System.getProperty(LOADED) != null) {
            return true;
         } else {
            System.setProperty(LOADED, "");
            return loadAgent(jarURL, arguments);
         }
      } catch (Exception var3) {
         if (System.getProperty("java.version").startsWith("9")) {
            throw new RuntimeException("Agent loading failure for JDK9! Don't forget to set jdk.attach.allowAttachSelf, e.g., export MAVEN_OPTS=\"-Djdk.attach.allowAttachSelf=true\".");
         } else {
            throw new RuntimeException("Agent loading failure!");
         }
      }
   }

   public static boolean loadAgent(String aju, String arguments) throws Exception {
      URL toolsJarFile = findToolsJar();
      if (toolsJarFile == null) {
         return false;
      } else {
         Class<?> vc = loadVirtualMachine(new URL[]{toolsJarFile});
         if (vc == null) {
            return false;
         } else {
            attachAgent(vc, aju, arguments);
            return true;
         }
      }
   }

   private static void attachAgent(Class<?> vc, String aju, String arguments) throws Exception {
      String pid = getPID();
      Object vm = getAttachMethod(vc).invoke((Object)null, pid);
      getLoadAgentMethod(vc).invoke(vm, aju, arguments);
      getDetachMethod(vc).invoke(vm);
   }

   private static Method getLoadAgentMethod(Class<?> vc) throws SecurityException, NoSuchMethodException {
      return vc.getMethod("loadAgent", String.class, String.class);
   }

   private static Method getAttachMethod(Class<?> vc) throws SecurityException, NoSuchMethodException {
      return vc.getMethod("attach", String.class);
   }

   private static Method getDetachMethod(Class<?> vc) throws SecurityException, NoSuchMethodException {
      return vc.getMethod("detach");
   }

   private static Class<?> loadVirtualMachine(URL[] urls) throws Exception {
      URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
      return loader.loadClass("com.sun.tools.attach.VirtualMachine");
   }

   private static String getPID() {
      String vmName = ManagementFactory.getRuntimeMXBean().getName();
      logger.debug("vmName: " + vmName);
      return vmName.substring(0, vmName.indexOf("@"));
   }

   private static URL findToolsJar() throws MalformedURLException {
      String javaHome = System.getProperty("java.home");
      File javaHomeFile = new File(javaHome);
      File tjf = new File(javaHomeFile, "lib" + File.separator + "tools.jar");
      if (!tjf.exists()) {
         tjf = new File(System.getenv("java_home"), "lib" + File.separator + "tools.jar");
      }

      if (!tjf.exists() && javaHomeFile.getAbsolutePath().endsWith(File.separator + "jre")) {
         javaHomeFile = javaHomeFile.getParentFile();
         tjf = new File(javaHomeFile, "lib" + File.separator + "tools.jar");
      }

      if (!tjf.exists() && isMac() && javaHomeFile.getAbsolutePath().endsWith(File.separator + "Home")) {
         javaHomeFile = javaHomeFile.getParentFile();
         tjf = new File(javaHomeFile, "Classes" + File.separator + "classes.jar");
      }

      return tjf.toURI().toURL();
   }

   private static boolean isMac() {
      return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
   }
}

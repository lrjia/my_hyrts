package set.hyrts.org.apache.commons.lang3;

import java.util.HashMap;
import java.util.Map;
import set.hyrts.org.apache.commons.lang3.arch.Processor;

public class ArchUtils {
   private static final Map<String, Processor> ARCH_TO_PROCESSOR = new HashMap();

   private static void init() {
      init_X86_32Bit();
      init_X86_64Bit();
      init_IA64_32Bit();
      init_IA64_64Bit();
      init_PPC_32Bit();
      init_PPC_64Bit();
   }

   private static void init_X86_32Bit() {
      Processor processor = new Processor(Processor.Arch.BIT_32, Processor.Type.X86);
      addProcessors(processor, "x86", "i386", "i486", "i586", "i686", "pentium");
   }

   private static void init_X86_64Bit() {
      Processor processor = new Processor(Processor.Arch.BIT_64, Processor.Type.X86);
      addProcessors(processor, "x86_64", "amd64", "em64t", "universal");
   }

   private static void init_IA64_32Bit() {
      Processor processor = new Processor(Processor.Arch.BIT_32, Processor.Type.IA_64);
      addProcessors(processor, "ia64_32", "ia64n");
   }

   private static void init_IA64_64Bit() {
      Processor processor = new Processor(Processor.Arch.BIT_64, Processor.Type.IA_64);
      addProcessors(processor, "ia64", "ia64w");
   }

   private static void init_PPC_32Bit() {
      Processor processor = new Processor(Processor.Arch.BIT_32, Processor.Type.PPC);
      addProcessors(processor, "ppc", "power", "powerpc", "power_pc", "power_rs");
   }

   private static void init_PPC_64Bit() {
      Processor processor = new Processor(Processor.Arch.BIT_64, Processor.Type.PPC);
      addProcessors(processor, "ppc64", "power64", "powerpc64", "power_pc64", "power_rs64");
   }

   private static void addProcessor(String key, Processor processor) throws IllegalStateException {
      if (!ARCH_TO_PROCESSOR.containsKey(key)) {
         ARCH_TO_PROCESSOR.put(key, processor);
      } else {
         String msg = "Key " + key + " already exists in processor map";
         throw new IllegalStateException(msg);
      }
   }

   private static void addProcessors(Processor processor, String... keys) throws IllegalStateException {
      String[] var2 = keys;
      int var3 = keys.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String key = var2[var4];
         addProcessor(key, processor);
      }

   }

   public static Processor getProcessor() {
      return getProcessor(SystemUtils.OS_ARCH);
   }

   public static Processor getProcessor(String value) {
      return (Processor)ARCH_TO_PROCESSOR.get(value);
   }

   static {
      init();
   }
}

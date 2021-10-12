package set.hyrts.org.objectweb.asm.commons;

import set.hyrts.org.objectweb.asm.Label;

public interface TableSwitchGenerator {
   void generateCase(int var1, Label var2);

   void generateDefault();
}

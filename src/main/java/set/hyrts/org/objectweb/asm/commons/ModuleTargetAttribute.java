package set.hyrts.org.objectweb.asm.commons;

import set.hyrts.org.objectweb.asm.Attribute;
import set.hyrts.org.objectweb.asm.ByteVector;
import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.ClassWriter;
import set.hyrts.org.objectweb.asm.Label;

public final class ModuleTargetAttribute extends Attribute {
   public String platform;

   public ModuleTargetAttribute(String var1) {
      super("ModuleTarget");
      this.platform = var1;
   }

   public ModuleTargetAttribute() {
      this((String)null);
   }

   protected Attribute read(ClassReader var1, int var2, int var3, char[] var4, int var5, Label[] var6) {
      String var7 = var1.readUTF8(var2, var4);
      return new ModuleTargetAttribute(var7);
   }

   protected ByteVector write(ClassWriter var1, byte[] var2, int var3, int var4, int var5) {
      ByteVector var6 = new ByteVector();
      int var7 = this.platform == null ? 0 : var1.newUTF8(this.platform);
      var6.putShort(var7);
      return var6;
   }
}

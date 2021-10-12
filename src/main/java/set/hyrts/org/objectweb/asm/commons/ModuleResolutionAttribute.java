package set.hyrts.org.objectweb.asm.commons;

import set.hyrts.org.objectweb.asm.Attribute;
import set.hyrts.org.objectweb.asm.ByteVector;
import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.ClassWriter;
import set.hyrts.org.objectweb.asm.Label;

public final class ModuleResolutionAttribute extends Attribute {
   public static final int RESOLUTION_DO_NOT_RESOLVE_BY_DEFAULT = 1;
   public static final int RESOLUTION_WARN_DEPRECATED = 2;
   public static final int RESOLUTION_WARN_DEPRECATED_FOR_REMOVAL = 4;
   public static final int RESOLUTION_WARN_INCUBATING = 8;
   public int resolution;

   public ModuleResolutionAttribute(int var1) {
      super("ModuleResolution");
      this.resolution = var1;
   }

   public ModuleResolutionAttribute() {
      this(0);
   }

   protected Attribute read(ClassReader var1, int var2, int var3, char[] var4, int var5, Label[] var6) {
      int var7 = var1.readUnsignedShort(var2);
      return new ModuleResolutionAttribute(var7);
   }

   protected ByteVector write(ClassWriter var1, byte[] var2, int var3, int var4, int var5) {
      ByteVector var6 = new ByteVector();
      var6.putShort(this.resolution);
      return var6;
   }
}

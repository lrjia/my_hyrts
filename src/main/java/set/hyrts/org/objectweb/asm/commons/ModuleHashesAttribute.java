package set.hyrts.org.objectweb.asm.commons;

import java.util.ArrayList;
import java.util.List;
import set.hyrts.org.objectweb.asm.Attribute;
import set.hyrts.org.objectweb.asm.ByteVector;
import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.ClassWriter;
import set.hyrts.org.objectweb.asm.Label;

public final class ModuleHashesAttribute extends Attribute {
   public String algorithm;
   public List modules;
   public List hashes;

   public ModuleHashesAttribute(String var1, List var2, List var3) {
      super("ModuleHashes");
      this.algorithm = var1;
      this.modules = var2;
      this.hashes = var3;
   }

   public ModuleHashesAttribute() {
      this((String)null, (List)null, (List)null);
   }

   protected Attribute read(ClassReader var1, int var2, int var3, char[] var4, int var5, Label[] var6) {
      String var7 = var1.readUTF8(var2, var4);
      int var8 = var1.readUnsignedShort(var2 + 2);
      ArrayList var9 = new ArrayList(var8);
      ArrayList var10 = new ArrayList(var8);
      var2 += 4;

      for(int var11 = 0; var11 < var8; ++var11) {
         String var12 = var1.readModule(var2, var4);
         int var13 = var1.readUnsignedShort(var2 + 2);
         var2 += 4;
         byte[] var14 = new byte[var13];

         for(int var15 = 0; var15 < var13; ++var15) {
            var14[var15] = (byte)(var1.readByte(var2 + var15) & 255);
         }

         var2 += var13;
         var9.add(var12);
         var10.add(var14);
      }

      return new ModuleHashesAttribute(var7, var9, var10);
   }

   protected ByteVector write(ClassWriter var1, byte[] var2, int var3, int var4, int var5) {
      ByteVector var6 = new ByteVector();
      int var7 = var1.newUTF8(this.algorithm);
      var6.putShort(var7);
      int var8 = this.modules == null ? 0 : this.modules.size();
      var6.putShort(var8);

      for(int var9 = 0; var9 < var8; ++var9) {
         String var10 = (String)this.modules.get(var9);
         var6.putShort(var1.newModule(var10));
         byte[] var11 = (byte[])this.hashes.get(var9);
         var6.putShort(var11.length);
         byte[] var12 = var11;
         int var13 = var11.length;

         for(int var14 = 0; var14 < var13; ++var14) {
            byte var15 = var12[var14];
            var6.putByte(var15);
         }
      }

      return var6;
   }
}

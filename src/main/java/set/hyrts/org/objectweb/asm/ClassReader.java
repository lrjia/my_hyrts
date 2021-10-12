package set.hyrts.org.objectweb.asm;

import java.io.IOException;
import java.io.InputStream;

public class ClassReader {
   public static final int SKIP_CODE = 1;
   public static final int SKIP_DEBUG = 2;
   public static final int SKIP_FRAMES = 4;
   public static final int EXPAND_FRAMES = 8;
   public final byte[] b;
   private final int[] a;
   private final String[] c;
   private final int d;
   public final int header;

   public ClassReader(byte[] var1) {
      this(var1, 0, var1.length);
   }

   public ClassReader(byte[] var1, int var2, int var3) {
      this.b = var1;
      if (this.readShort(var2 + 6) > 53) {
         throw new IllegalArgumentException();
      } else {
         this.a = new int[this.readUnsignedShort(var2 + 8)];
         int var4 = this.a.length;
         this.c = new String[var4];
         int var5 = 0;
         int var6 = var2 + 10;

         for(int var7 = 1; var7 < var4; ++var7) {
            this.a[var7] = var6 + 1;
            int var8;
            switch(var1[var6]) {
            case 1:
               var8 = 3 + this.readUnsignedShort(var6 + 1);
               if (var8 > var5) {
                  var5 = var8;
               }
               break;
            case 2:
            case 7:
            case 8:
            case 13:
            case 14:
            case 16:
            case 17:
            default:
               var8 = 3;
               break;
            case 3:
            case 4:
            case 9:
            case 10:
            case 11:
            case 12:
            case 18:
               var8 = 5;
               break;
            case 5:
            case 6:
               var8 = 9;
               ++var7;
               break;
            case 15:
               var8 = 4;
            }

            var6 += var8;
         }

         this.d = var5;
         this.header = var6;
      }
   }

   public int getAccess() {
      return this.readUnsignedShort(this.header);
   }

   public String getClassName() {
      return this.readClass(this.header + 2, new char[this.d]);
   }

   public String getSuperName() {
      return this.readClass(this.header + 4, new char[this.d]);
   }

   public String[] getInterfaces() {
      int var1 = this.header + 6;
      int var2 = this.readUnsignedShort(var1);
      String[] var3 = new String[var2];
      if (var2 > 0) {
         char[] var4 = new char[this.d];

         for(int var5 = 0; var5 < var2; ++var5) {
            var1 += 2;
            var3[var5] = this.readClass(var1, var4);
         }
      }

      return var3;
   }

   void a(ClassWriter var1) {
      char[] var2 = new char[this.d];
      int var3 = this.a.length;
      Item[] var4 = new Item[var3];

      int var5;
      for(var5 = 1; var5 < var3; ++var5) {
         int var6 = this.a[var5];
         byte var7 = this.b[var6 - 1];
         Item var8 = new Item(var5);
         int var9;
         int var10;
         switch(var7) {
         case 1:
            String var11 = this.c[var5];
            if (var11 == null) {
               var6 = this.a[var5];
               var11 = this.c[var5] = this.a(var6 + 2, this.readUnsignedShort(var6), var2);
            }

            var8.a(var7, var11, (String)null, (String)null);
            break;
         case 2:
         case 7:
         case 8:
         case 13:
         case 14:
         case 16:
         case 17:
         default:
            var8.a(var7, this.readUTF8(var6, var2), (String)null, (String)null);
            break;
         case 3:
            var8.a(this.readInt(var6));
            break;
         case 4:
            var8.a(Float.intBitsToFloat(this.readInt(var6)));
            break;
         case 5:
            var8.a(this.readLong(var6));
            ++var5;
            break;
         case 6:
            var8.a(Double.longBitsToDouble(this.readLong(var6)));
            ++var5;
            break;
         case 9:
         case 10:
         case 11:
            var9 = this.a[this.readUnsignedShort(var6 + 2)];
            var8.a(var7, this.readClass(var6, var2), this.readUTF8(var9, var2), this.readUTF8(var9 + 2, var2));
            break;
         case 12:
            var8.a(var7, this.readUTF8(var6, var2), this.readUTF8(var6 + 2, var2), (String)null);
            break;
         case 15:
            var10 = this.a[this.readUnsignedShort(var6 + 1)];
            var9 = this.a[this.readUnsignedShort(var10 + 2)];
            var8.a(20 + this.readByte(var6), this.readClass(var10, var2), this.readUTF8(var9, var2), this.readUTF8(var9 + 2, var2));
            break;
         case 18:
            if (var1.A == null) {
               this.a(var1, var4, var2);
            }

            var9 = this.a[this.readUnsignedShort(var6 + 2)];
            var8.a(this.readUTF8(var9, var2), this.readUTF8(var9 + 2, var2), this.readUnsignedShort(var6));
         }

         var10 = var8.j % var4.length;
         var8.k = var4[var10];
         var4[var10] = var8;
      }

      var5 = this.a[1] - 1;
      var1.d.putByteArray(this.b, var5, this.header - var5);
      var1.e = var4;
      var1.f = (int)(0.75D * (double)var3);
      var1.c = var3;
   }

   private void a(ClassWriter var1, Item[] var2, char[] var3) {
      int var4 = this.a();
      boolean var5 = false;

      int var6;
      for(var6 = this.readUnsignedShort(var4); var6 > 0; --var6) {
         String var7 = this.readUTF8(var4 + 2, var3);
         if ("BootstrapMethods".equals(var7)) {
            var5 = true;
            break;
         }

         var4 += 6 + this.readInt(var4 + 4);
      }

      if (var5) {
         var6 = this.readUnsignedShort(var4 + 8);
         int var13 = 0;

         for(int var8 = var4 + 10; var13 < var6; ++var13) {
            int var9 = var8 - var4 - 10;
            int var10 = this.readConst(this.readUnsignedShort(var8), var3).hashCode();

            for(int var11 = this.readUnsignedShort(var8 + 2); var11 > 0; --var11) {
               var10 ^= this.readConst(this.readUnsignedShort(var8 + 4), var3).hashCode();
               var8 += 2;
            }

            var8 += 4;
            Item var15 = new Item(var13);
            var15.a(var9, var10 & Integer.MAX_VALUE);
            int var12 = var15.j % var2.length;
            var15.k = var2[var12];
            var2[var12] = var15;
         }

         var13 = this.readInt(var4 + 4);
         ByteVector var14 = new ByteVector(var13 + 62);
         var14.putByteArray(this.b, var4 + 10, var13 - 2);
         var1.z = var6;
         var1.A = var14;
      }
   }

   public ClassReader(InputStream var1) throws IOException {
      this(a(var1, false));
   }

   public ClassReader(String var1) throws IOException {
      this(a(ClassLoader.getSystemResourceAsStream(var1.replace('.', '/') + ".class"), true));
   }

   private static byte[] a(InputStream var0, boolean var1) throws IOException {
      if (var0 == null) {
         throw new IOException("Class not found");
      } else {
         try {
            byte[] var2 = new byte[var0.available()];
            int var3 = 0;

            while(true) {
               int var4 = var0.read(var2, var3, var2.length - var3);
               if (var4 == -1) {
                  byte[] var10;
                  if (var3 < var2.length) {
                     var10 = new byte[var3];
                     System.arraycopy(var2, 0, var10, 0, var3);
                     var2 = var10;
                  }

                  var10 = var2;
                  return var10;
               }

               var3 += var4;
               if (var3 == var2.length) {
                  int var5 = var0.read();
                  byte[] var6;
                  if (var5 < 0) {
                     var6 = var2;
                     return var6;
                  }

                  var6 = new byte[var2.length + 1000];
                  System.arraycopy(var2, 0, var6, 0, var3);
                  var6[var3++] = (byte)var5;
                  var2 = var6;
               }
            }
         } finally {
            if (var1) {
               var0.close();
            }

         }
      }
   }

   public void accept(ClassVisitor var1, int var2) {
      this.accept(var1, new Attribute[0], var2);
   }

   public void accept(ClassVisitor var1, Attribute[] var2, int var3) {
      int var4 = this.header;
      char[] var5 = new char[this.d];
      Context var6 = new Context();
      var6.a = var2;
      var6.b = var3;
      var6.c = var5;
      int var7 = this.readUnsignedShort(var4);
      String var8 = this.readClass(var4 + 2, var5);
      String var9 = this.readClass(var4 + 4, var5);
      String[] var10 = new String[this.readUnsignedShort(var4 + 6)];
      var4 += 8;

      for(int var11 = 0; var11 < var10.length; ++var11) {
         var10[var11] = this.readClass(var4, var5);
         var4 += 2;
      }

      String var31 = null;
      String var12 = null;
      String var13 = null;
      String var14 = null;
      String var15 = null;
      String var16 = null;
      String var17 = null;
      int var18 = 0;
      int var19 = 0;
      int var20 = 0;
      int var21 = 0;
      int var22 = 0;
      int var23 = 0;
      int var24 = 0;
      Attribute var25 = null;
      var4 = this.a();

      int var26;
      for(var26 = this.readUnsignedShort(var4); var26 > 0; --var26) {
         String var27 = this.readUTF8(var4 + 2, var5);
         if ("SourceFile".equals(var27)) {
            var12 = this.readUTF8(var4 + 8, var5);
         } else if ("InnerClasses".equals(var27)) {
            var22 = var4 + 8;
         } else {
            int var34;
            if ("EnclosingMethod".equals(var27)) {
               var14 = this.readClass(var4 + 8, var5);
               var34 = this.readUnsignedShort(var4 + 10);
               if (var34 != 0) {
                  var15 = this.readUTF8(this.a[var34], var5);
                  var16 = this.readUTF8(this.a[var34] + 2, var5);
               }
            } else if ("Signature".equals(var27)) {
               var31 = this.readUTF8(var4 + 8, var5);
            } else if ("RuntimeVisibleAnnotations".equals(var27)) {
               var18 = var4 + 8;
            } else if ("RuntimeVisibleTypeAnnotations".equals(var27)) {
               var20 = var4 + 8;
            } else if ("Deprecated".equals(var27)) {
               var7 |= 131072;
            } else if ("Synthetic".equals(var27)) {
               var7 |= 266240;
            } else if ("SourceDebugExtension".equals(var27)) {
               var34 = this.readInt(var4 + 4);
               var13 = this.a(var4 + 8, var34, new char[var34]);
            } else if ("RuntimeInvisibleAnnotations".equals(var27)) {
               var19 = var4 + 8;
            } else if ("RuntimeInvisibleTypeAnnotations".equals(var27)) {
               var21 = var4 + 8;
            } else if ("Module".equals(var27)) {
               var23 = var4 + 8;
            } else if ("ModuleMainClass".equals(var27)) {
               var17 = this.readClass(var4 + 8, var5);
            } else if ("ModulePackages".equals(var27)) {
               var24 = var4 + 10;
            } else if (!"BootstrapMethods".equals(var27)) {
               Attribute var33 = this.a(var2, var27, var4 + 8, this.readInt(var4 + 4), var5, -1, (Label[])null);
               if (var33 != null) {
                  var33.a = var25;
                  var25 = var33;
               }
            } else {
               int[] var28 = new int[this.readUnsignedShort(var4 + 8)];
               int var29 = 0;

               for(int var30 = var4 + 10; var29 < var28.length; ++var29) {
                  var28[var29] = var30;
                  var30 += 2 + this.readUnsignedShort(var30 + 2) << 1;
               }

               var6.d = var28;
            }
         }

         var4 += 6 + this.readInt(var4 + 4);
      }

      var1.visit(this.readInt(this.a[1] - 7), var7, var8, var31, var9, var10);
      if ((var3 & 2) == 0 && (var12 != null || var13 != null)) {
         var1.visitSource(var12, var13);
      }

      if (var23 != 0) {
         this.a(var1, var6, var23, var17, var24);
      }

      if (var14 != null) {
         var1.visitOuterClass(var14, var15, var16);
      }

      int var32;
      if (var18 != 0) {
         var26 = this.readUnsignedShort(var18);

         for(var32 = var18 + 2; var26 > 0; --var26) {
            var32 = this.a(var32 + 2, var5, true, var1.visitAnnotation(this.readUTF8(var32, var5), true));
         }
      }

      if (var19 != 0) {
         var26 = this.readUnsignedShort(var19);

         for(var32 = var19 + 2; var26 > 0; --var26) {
            var32 = this.a(var32 + 2, var5, true, var1.visitAnnotation(this.readUTF8(var32, var5), false));
         }
      }

      if (var20 != 0) {
         var26 = this.readUnsignedShort(var20);

         for(var32 = var20 + 2; var26 > 0; --var26) {
            var32 = this.a(var6, var32);
            var32 = this.a(var32 + 2, var5, true, var1.visitTypeAnnotation(var6.i, var6.j, this.readUTF8(var32, var5), true));
         }
      }

      if (var21 != 0) {
         var26 = this.readUnsignedShort(var21);

         for(var32 = var21 + 2; var26 > 0; --var26) {
            var32 = this.a(var6, var32);
            var32 = this.a(var32 + 2, var5, true, var1.visitTypeAnnotation(var6.i, var6.j, this.readUTF8(var32, var5), false));
         }
      }

      while(var25 != null) {
         Attribute var35 = var25.a;
         var25.a = null;
         var1.visitAttribute(var25);
         var25 = var35;
      }

      if (var22 != 0) {
         var26 = var22 + 2;

         for(var32 = this.readUnsignedShort(var22); var32 > 0; --var32) {
            var1.visitInnerClass(this.readClass(var26, var5), this.readClass(var26 + 2, var5), this.readUTF8(var26 + 4, var5), this.readUnsignedShort(var26 + 6));
            var26 += 8;
         }
      }

      var4 = this.header + 10 + 2 * var10.length;

      for(var26 = this.readUnsignedShort(var4 - 2); var26 > 0; --var26) {
         var4 = this.a(var1, var6, var4);
      }

      var4 += 2;

      for(var26 = this.readUnsignedShort(var4 - 2); var26 > 0; --var26) {
         var4 = this.b(var1, var6, var4);
      }

      var1.visitEnd();
   }

   private void a(ClassVisitor var1, Context var2, int var3, String var4, int var5) {
      char[] var6 = var2.c;
      String var7 = this.readModule(var3, var6);
      int var8 = this.readUnsignedShort(var3 + 2);
      String var9 = this.readUTF8(var3 + 4, var6);
      var3 += 6;
      ModuleVisitor var10 = var1.visitModule(var7, var8, var9);
      if (var10 != null) {
         if (var4 != null) {
            var10.visitMainClass(var4);
         }

         int var11;
         String var12;
         if (var5 != 0) {
            for(var11 = this.readUnsignedShort(var5 - 2); var11 > 0; --var11) {
               var12 = this.readPackage(var5, var6);
               var10.visitPackage(var12);
               var5 += 2;
            }
         }

         var3 += 2;

         int var13;
         for(var11 = this.readUnsignedShort(var3 - 2); var11 > 0; --var11) {
            var12 = this.readModule(var3, var6);
            var13 = this.readUnsignedShort(var3 + 2);
            String var14 = this.readUTF8(var3 + 4, var6);
            var10.visitRequire(var12, var13, var14);
            var3 += 6;
         }

         var3 += 2;

         String[] var15;
         int var16;
         int var17;
         for(var11 = this.readUnsignedShort(var3 - 2); var11 > 0; --var11) {
            var12 = this.readPackage(var3, var6);
            var13 = this.readUnsignedShort(var3 + 2);
            var17 = this.readUnsignedShort(var3 + 4);
            var3 += 6;
            var15 = null;
            if (var17 != 0) {
               var15 = new String[var17];

               for(var16 = 0; var16 < var15.length; ++var16) {
                  var15[var16] = this.readModule(var3, var6);
                  var3 += 2;
               }
            }

            var10.visitExport(var12, var13, var15);
         }

         var3 += 2;

         for(var11 = this.readUnsignedShort(var3 - 2); var11 > 0; --var11) {
            var12 = this.readPackage(var3, var6);
            var13 = this.readUnsignedShort(var3 + 2);
            var17 = this.readUnsignedShort(var3 + 4);
            var3 += 6;
            var15 = null;
            if (var17 != 0) {
               var15 = new String[var17];

               for(var16 = 0; var16 < var15.length; ++var16) {
                  var15[var16] = this.readModule(var3, var6);
                  var3 += 2;
               }
            }

            var10.visitOpen(var12, var13, var15);
         }

         var3 += 2;

         for(var11 = this.readUnsignedShort(var3 - 2); var11 > 0; --var11) {
            var10.visitUse(this.readClass(var3, var6));
            var3 += 2;
         }

         var3 += 2;

         for(var11 = this.readUnsignedShort(var3 - 2); var11 > 0; --var11) {
            var12 = this.readClass(var3, var6);
            var13 = this.readUnsignedShort(var3 + 2);
            var3 += 4;
            String[] var18 = new String[var13];

            for(int var19 = 0; var19 < var18.length; ++var19) {
               var18[var19] = this.readClass(var3, var6);
               var3 += 2;
            }

            var10.visitProvide(var12, var18);
         }

         var10.visitEnd();
      }
   }

   private int a(ClassVisitor var1, Context var2, int var3) {
      char[] var4 = var2.c;
      int var5 = this.readUnsignedShort(var3);
      String var6 = this.readUTF8(var3 + 2, var4);
      String var7 = this.readUTF8(var3 + 4, var4);
      var3 += 6;
      String var8 = null;
      int var9 = 0;
      int var10 = 0;
      int var11 = 0;
      int var12 = 0;
      Object var13 = null;
      Attribute var14 = null;

      int var17;
      for(int var15 = this.readUnsignedShort(var3); var15 > 0; --var15) {
         String var16 = this.readUTF8(var3 + 2, var4);
         if ("ConstantValue".equals(var16)) {
            var17 = this.readUnsignedShort(var3 + 8);
            var13 = var17 == 0 ? null : this.readConst(var17, var4);
         } else if ("Signature".equals(var16)) {
            var8 = this.readUTF8(var3 + 8, var4);
         } else if ("Deprecated".equals(var16)) {
            var5 |= 131072;
         } else if ("Synthetic".equals(var16)) {
            var5 |= 266240;
         } else if ("RuntimeVisibleAnnotations".equals(var16)) {
            var9 = var3 + 8;
         } else if ("RuntimeVisibleTypeAnnotations".equals(var16)) {
            var11 = var3 + 8;
         } else if ("RuntimeInvisibleAnnotations".equals(var16)) {
            var10 = var3 + 8;
         } else if ("RuntimeInvisibleTypeAnnotations".equals(var16)) {
            var12 = var3 + 8;
         } else {
            Attribute var20 = this.a(var2.a, var16, var3 + 8, this.readInt(var3 + 4), var4, -1, (Label[])null);
            if (var20 != null) {
               var20.a = var14;
               var14 = var20;
            }
         }

         var3 += 6 + this.readInt(var3 + 4);
      }

      var3 += 2;
      FieldVisitor var18 = var1.visitField(var5, var6, var7, var8, var13);
      if (var18 == null) {
         return var3;
      } else {
         int var19;
         if (var9 != 0) {
            var19 = this.readUnsignedShort(var9);

            for(var17 = var9 + 2; var19 > 0; --var19) {
               var17 = this.a(var17 + 2, var4, true, var18.visitAnnotation(this.readUTF8(var17, var4), true));
            }
         }

         if (var10 != 0) {
            var19 = this.readUnsignedShort(var10);

            for(var17 = var10 + 2; var19 > 0; --var19) {
               var17 = this.a(var17 + 2, var4, true, var18.visitAnnotation(this.readUTF8(var17, var4), false));
            }
         }

         if (var11 != 0) {
            var19 = this.readUnsignedShort(var11);

            for(var17 = var11 + 2; var19 > 0; --var19) {
               var17 = this.a(var2, var17);
               var17 = this.a(var17 + 2, var4, true, var18.visitTypeAnnotation(var2.i, var2.j, this.readUTF8(var17, var4), true));
            }
         }

         if (var12 != 0) {
            var19 = this.readUnsignedShort(var12);

            for(var17 = var12 + 2; var19 > 0; --var19) {
               var17 = this.a(var2, var17);
               var17 = this.a(var17 + 2, var4, true, var18.visitTypeAnnotation(var2.i, var2.j, this.readUTF8(var17, var4), false));
            }
         }

         while(var14 != null) {
            Attribute var21 = var14.a;
            var14.a = null;
            var18.visitAttribute(var14);
            var14 = var21;
         }

         var18.visitEnd();
         return var3;
      }
   }

   private int b(ClassVisitor var1, Context var2, int var3) {
      char[] var4 = var2.c;
      var2.e = this.readUnsignedShort(var3);
      var2.f = this.readUTF8(var3 + 2, var4);
      var2.g = this.readUTF8(var3 + 4, var4);
      var3 += 6;
      int var5 = 0;
      int var6 = 0;
      String[] var7 = null;
      String var8 = null;
      int var9 = 0;
      int var10 = 0;
      int var11 = 0;
      int var12 = 0;
      int var13 = 0;
      int var14 = 0;
      int var15 = 0;
      int var16 = 0;
      int var17 = var3;
      Attribute var18 = null;

      int var26;
      for(int var19 = this.readUnsignedShort(var3); var19 > 0; --var19) {
         String var20 = this.readUTF8(var3 + 2, var4);
         if ("Code".equals(var20)) {
            if ((var2.b & 1) == 0) {
               var5 = var3 + 8;
            }
         } else if ("Exceptions".equals(var20)) {
            var7 = new String[this.readUnsignedShort(var3 + 8)];
            var6 = var3 + 10;

            for(var26 = 0; var26 < var7.length; ++var26) {
               var7[var26] = this.readClass(var6, var4);
               var6 += 2;
            }
         } else if ("Signature".equals(var20)) {
            var8 = this.readUTF8(var3 + 8, var4);
         } else if ("Deprecated".equals(var20)) {
            var2.e |= 131072;
         } else if ("RuntimeVisibleAnnotations".equals(var20)) {
            var10 = var3 + 8;
         } else if ("RuntimeVisibleTypeAnnotations".equals(var20)) {
            var12 = var3 + 8;
         } else if ("AnnotationDefault".equals(var20)) {
            var14 = var3 + 8;
         } else if ("Synthetic".equals(var20)) {
            var2.e |= 266240;
         } else if ("RuntimeInvisibleAnnotations".equals(var20)) {
            var11 = var3 + 8;
         } else if ("RuntimeInvisibleTypeAnnotations".equals(var20)) {
            var13 = var3 + 8;
         } else if ("RuntimeVisibleParameterAnnotations".equals(var20)) {
            var15 = var3 + 8;
         } else if ("RuntimeInvisibleParameterAnnotations".equals(var20)) {
            var16 = var3 + 8;
         } else if ("MethodParameters".equals(var20)) {
            var9 = var3 + 8;
         } else {
            Attribute var21 = this.a(var2.a, var20, var3 + 8, this.readInt(var3 + 4), var4, -1, (Label[])null);
            if (var21 != null) {
               var21.a = var18;
               var18 = var21;
            }
         }

         var3 += 6 + this.readInt(var3 + 4);
      }

      var3 += 2;
      MethodVisitor var23 = var1.visitMethod(var2.e, var2.f, var2.g, var8, var7);
      if (var23 == null) {
         return var3;
      } else {
         if (var23 instanceof MethodWriter) {
            MethodWriter var24 = (MethodWriter)var23;
            if (var24.b.K == this && var8 == var24.g) {
               boolean var28 = false;
               if (var7 == null) {
                  var28 = var24.j == 0;
               } else if (var7.length == var24.j) {
                  var28 = true;

                  for(int var22 = var7.length - 1; var22 >= 0; --var22) {
                     var6 -= 2;
                     if (var24.k[var22] != this.readUnsignedShort(var6)) {
                        var28 = false;
                        break;
                     }
                  }
               }

               if (var28) {
                  var24.h = var17;
                  var24.i = var3 - var17;
                  return var3;
               }
            }
         }

         int var25;
         if (var9 != 0) {
            var25 = this.b[var9] & 255;

            for(var26 = var9 + 1; var25 > 0; var26 += 4) {
               var23.visitParameter(this.readUTF8(var26, var4), this.readUnsignedShort(var26 + 2));
               --var25;
            }
         }

         if (var14 != 0) {
            AnnotationVisitor var27 = var23.visitAnnotationDefault();
            this.a(var14, var4, (String)null, var27);
            if (var27 != null) {
               var27.visitEnd();
            }
         }

         if (var10 != 0) {
            var25 = this.readUnsignedShort(var10);

            for(var26 = var10 + 2; var25 > 0; --var25) {
               var26 = this.a(var26 + 2, var4, true, var23.visitAnnotation(this.readUTF8(var26, var4), true));
            }
         }

         if (var11 != 0) {
            var25 = this.readUnsignedShort(var11);

            for(var26 = var11 + 2; var25 > 0; --var25) {
               var26 = this.a(var26 + 2, var4, true, var23.visitAnnotation(this.readUTF8(var26, var4), false));
            }
         }

         if (var12 != 0) {
            var25 = this.readUnsignedShort(var12);

            for(var26 = var12 + 2; var25 > 0; --var25) {
               var26 = this.a(var2, var26);
               var26 = this.a(var26 + 2, var4, true, var23.visitTypeAnnotation(var2.i, var2.j, this.readUTF8(var26, var4), true));
            }
         }

         if (var13 != 0) {
            var25 = this.readUnsignedShort(var13);

            for(var26 = var13 + 2; var25 > 0; --var25) {
               var26 = this.a(var2, var26);
               var26 = this.a(var26 + 2, var4, true, var23.visitTypeAnnotation(var2.i, var2.j, this.readUTF8(var26, var4), false));
            }
         }

         if (var15 != 0) {
            this.b(var23, var2, var15, true);
         }

         if (var16 != 0) {
            this.b(var23, var2, var16, false);
         }

         while(var18 != null) {
            Attribute var29 = var18.a;
            var18.a = null;
            var23.visitAttribute(var18);
            var18 = var29;
         }

         if (var5 != 0) {
            var23.visitCode();
            this.a(var23, var2, var5);
         }

         var23.visitEnd();
         return var3;
      }
   }

   private void a(MethodVisitor var1, Context var2, int var3) {
      byte[] var4 = this.b;
      char[] var5 = var2.c;
      int var6 = this.readUnsignedShort(var3);
      int var7 = this.readUnsignedShort(var3 + 2);
      int var8 = this.readInt(var3 + 4);
      var3 += 8;
      int var9 = var3;
      int var10 = var3 + var8;
      Label[] var11 = var2.h = new Label[var8 + 2];
      this.readLabel(var8 + 1, var11);

      while(true) {
         int var12;
         int var14;
         while(var3 < var10) {
            var12 = var3 - var9;
            int var13 = var4[var3] & 255;
            switch(ClassWriter.a[var13]) {
            case 0:
            case 4:
               ++var3;
               break;
            case 1:
            case 3:
            case 11:
               var3 += 2;
               break;
            case 2:
            case 5:
            case 6:
            case 12:
            case 13:
               var3 += 3;
               break;
            case 7:
            case 8:
               var3 += 5;
               break;
            case 9:
               this.readLabel(var12 + this.readShort(var3 + 1), var11);
               var3 += 3;
               break;
            case 10:
            case 19:
               this.readLabel(var12 + this.readInt(var3 + 1), var11);
               var3 += 5;
               break;
            case 14:
               var3 = var3 + 4 - (var12 & 3);
               this.readLabel(var12 + this.readInt(var3), var11);

               for(var14 = this.readInt(var3 + 8) - this.readInt(var3 + 4) + 1; var14 > 0; --var14) {
                  this.readLabel(var12 + this.readInt(var3 + 12), var11);
                  var3 += 4;
               }

               var3 += 12;
               break;
            case 15:
               var3 = var3 + 4 - (var12 & 3);
               this.readLabel(var12 + this.readInt(var3), var11);

               for(var14 = this.readInt(var3 + 4); var14 > 0; --var14) {
                  this.readLabel(var12 + this.readInt(var3 + 12), var11);
                  var3 += 8;
               }

               var3 += 8;
               break;
            case 16:
            default:
               var3 += 4;
               break;
            case 17:
               var13 = var4[var3 + 1] & 255;
               if (var13 == 132) {
                  var3 += 6;
               } else {
                  var3 += 4;
               }
               break;
            case 18:
               this.readLabel(var12 + this.readUnsignedShort(var3 + 1), var11);
               var3 += 3;
            }
         }

         for(var12 = this.readUnsignedShort(var3); var12 > 0; --var12) {
            Label var40 = this.readLabel(this.readUnsignedShort(var3 + 2), var11);
            Label var43 = this.readLabel(this.readUnsignedShort(var3 + 4), var11);
            Label var15 = this.readLabel(this.readUnsignedShort(var3 + 6), var11);
            String var16 = this.readUTF8(this.a[this.readUnsignedShort(var3 + 8)], var5);
            var1.visitTryCatchBlock(var40, var43, var15, var16);
            var3 += 8;
         }

         var3 += 2;
         int[] var39 = null;
         int[] var41 = null;
         var14 = 0;
         int var42 = 0;
         int var44 = -1;
         int var17 = -1;
         int var18 = 0;
         int var19 = 0;
         boolean var20 = true;
         boolean var21 = (var2.b & 8) != 0;
         int var22 = 0;
         int var23 = 0;
         int var24 = 0;
         Context var25 = null;
         Attribute var26 = null;

         int var27;
         int var29;
         int var31;
         Label var32;
         int var47;
         for(var27 = this.readUnsignedShort(var3); var27 > 0; --var27) {
            String var28 = this.readUTF8(var3 + 2, var5);
            Label var10000;
            if ("LocalVariableTable".equals(var28)) {
               if ((var2.b & 2) == 0) {
                  var18 = var3 + 8;
                  var29 = this.readUnsignedShort(var3 + 8);

                  for(var47 = var3; var29 > 0; --var29) {
                     var31 = this.readUnsignedShort(var47 + 10);
                     if (var11[var31] == null) {
                        var10000 = this.readLabel(var31, var11);
                        var10000.a |= 1;
                     }

                     var31 += this.readUnsignedShort(var47 + 12);
                     if (var11[var31] == null) {
                        var10000 = this.readLabel(var31, var11);
                        var10000.a |= 1;
                     }

                     var47 += 10;
                  }
               }
            } else if ("LocalVariableTypeTable".equals(var28)) {
               var19 = var3 + 8;
            } else if ("LineNumberTable".equals(var28)) {
               if ((var2.b & 2) == 0) {
                  var29 = this.readUnsignedShort(var3 + 8);

                  for(var47 = var3; var29 > 0; --var29) {
                     var31 = this.readUnsignedShort(var47 + 10);
                     if (var11[var31] == null) {
                        var10000 = this.readLabel(var31, var11);
                        var10000.a |= 1;
                     }

                     for(var32 = var11[var31]; var32.b > 0; var32 = var32.k) {
                        if (var32.k == null) {
                           var32.k = new Label();
                        }
                     }

                     var32.b = this.readUnsignedShort(var47 + 12);
                     var47 += 4;
                  }
               }
            } else if ("RuntimeVisibleTypeAnnotations".equals(var28)) {
               var39 = this.a(var1, var2, var3 + 8, true);
               var44 = var39.length != 0 && this.readByte(var39[0]) >= 67 ? this.readUnsignedShort(var39[0] + 1) : -1;
            } else if (!"RuntimeInvisibleTypeAnnotations".equals(var28)) {
               if ("StackMapTable".equals(var28)) {
                  if ((var2.b & 4) == 0) {
                     var22 = var3 + 10;
                     var23 = this.readInt(var3 + 4);
                     var24 = this.readUnsignedShort(var3 + 8);
                  }
               } else if ("StackMap".equals(var28)) {
                  if ((var2.b & 4) == 0) {
                     var20 = false;
                     var22 = var3 + 10;
                     var23 = this.readInt(var3 + 4);
                     var24 = this.readUnsignedShort(var3 + 8);
                  }
               } else {
                  for(var29 = 0; var29 < var2.a.length; ++var29) {
                     if (var2.a[var29].type.equals(var28)) {
                        Attribute var30 = var2.a[var29].read(this, var3 + 8, this.readInt(var3 + 4), var5, var9 - 8, var11);
                        if (var30 != null) {
                           var30.a = var26;
                           var26 = var30;
                        }
                     }
                  }
               }
            } else {
               var41 = this.a(var1, var2, var3 + 8, false);
               var17 = var41.length != 0 && this.readByte(var41[0]) >= 67 ? this.readUnsignedShort(var41[0] + 1) : -1;
            }

            var3 += 6 + this.readInt(var3 + 4);
         }

         var3 += 2;
         if (var22 != 0) {
            var25 = var2;
            var2.o = -1;
            var2.p = 0;
            var2.q = 0;
            var2.r = 0;
            var2.t = 0;
            var2.s = new Object[var7];
            var2.u = new Object[var6];
            if (var21) {
               this.a(var2);
            }

            for(var27 = var22; var27 < var22 + var23 - 2; ++var27) {
               if (var4[var27] == 8) {
                  int var45 = this.readUnsignedShort(var27 + 1);
                  if (var45 >= 0 && var45 < var8 && (var4[var9 + var45] & 255) == 187) {
                     this.readLabel(var45, var11);
                  }
               }
            }
         }

         if ((var2.b & 256) != 0 && (var2.b & 8) != 0) {
            var1.visitFrame(-1, var7, (Object[])null, 0, (Object[])null);
         }

         var27 = (var2.b & 256) == 0 ? -33 : 0;
         boolean var46 = false;
         var3 = var9;

         int var51;
         int var52;
         String var57;
         int var58;
         while(var3 < var10) {
            var29 = var3 - var9;
            Label var49 = var11[var29];
            if (var49 != null) {
               Label var50 = var49.k;
               var49.k = null;
               var1.visitLabel(var49);
               if ((var2.b & 2) == 0 && var49.b > 0) {
                  var1.visitLineNumber(var49.b, var49);

                  while(var50 != null) {
                     var1.visitLineNumber(var50.b, var49);
                     var50 = var50.k;
                  }
               }
            }

            while(var25 != null && (var25.o == var29 || var25.o == -1)) {
               if (var25.o != -1) {
                  if (var20 && !var21) {
                     var1.visitFrame(var25.p, var25.r, var25.s, var25.t, var25.u);
                  } else {
                     var1.visitFrame(-1, var25.q, var25.s, var25.t, var25.u);
                  }

                  var46 = false;
               }

               if (var24 > 0) {
                  var22 = this.a(var22, var20, var21, var25);
                  --var24;
               } else {
                  var25 = null;
               }
            }

            if (var46) {
               var1.visitFrame(256, 0, (Object[])null, 0, (Object[])null);
               var46 = false;
            }

            var31 = var4[var3] & 255;
            Label[] var35;
            int var36;
            switch(ClassWriter.a[var31]) {
            case 0:
               var1.visitInsn(var31);
               ++var3;
               break;
            case 1:
               var1.visitIntInsn(var31, var4[var3 + 1]);
               var3 += 2;
               break;
            case 2:
               var1.visitIntInsn(var31, this.readShort(var3 + 1));
               var3 += 3;
               break;
            case 3:
               var1.visitVarInsn(var31, var4[var3 + 1] & 255);
               var3 += 2;
               break;
            case 4:
               if (var31 > 54) {
                  var31 -= 59;
                  var1.visitVarInsn(54 + (var31 >> 2), var31 & 3);
               } else {
                  var31 -= 26;
                  var1.visitVarInsn(21 + (var31 >> 2), var31 & 3);
               }

               ++var3;
               break;
            case 5:
               var1.visitTypeInsn(var31, this.readClass(var3 + 1, var5));
               var3 += 3;
               break;
            case 6:
            case 7:
               var51 = this.a[this.readUnsignedShort(var3 + 1)];
               boolean var56 = var4[var51 - 1] == 11;
               var57 = this.readClass(var51, var5);
               var51 = this.a[this.readUnsignedShort(var51 + 2)];
               String var59 = this.readUTF8(var51, var5);
               String var61 = this.readUTF8(var51 + 2, var5);
               if (var31 < 182) {
                  var1.visitFieldInsn(var31, var57, var59, var61);
               } else {
                  var1.visitMethodInsn(var31, var57, var59, var61, var56);
               }

               if (var31 == 185) {
                  var3 += 5;
               } else {
                  var3 += 3;
               }
               break;
            case 8:
               var51 = this.a[this.readUnsignedShort(var3 + 1)];
               var52 = var2.d[this.readUnsignedShort(var51)];
               Handle var55 = (Handle)this.readConst(this.readUnsignedShort(var52), var5);
               var58 = this.readUnsignedShort(var52 + 2);
               Object[] var60 = new Object[var58];
               var52 += 4;

               for(int var37 = 0; var37 < var58; ++var37) {
                  var60[var37] = this.readConst(this.readUnsignedShort(var52), var5);
                  var52 += 2;
               }

               var51 = this.a[this.readUnsignedShort(var51 + 2)];
               String var62 = this.readUTF8(var51, var5);
               String var38 = this.readUTF8(var51 + 2, var5);
               var1.visitInvokeDynamicInsn(var62, var38, var55, var60);
               break;
            case 9:
               var1.visitJumpInsn(var31, var11[var29 + this.readShort(var3 + 1)]);
               var3 += 3;
               break;
            case 10:
               var1.visitJumpInsn(var31 + var27, var11[var29 + this.readInt(var3 + 1)]);
               var3 += 5;
               break;
            case 11:
               var1.visitLdcInsn(this.readConst(var4[var3 + 1] & 255, var5));
               var3 += 2;
               break;
            case 12:
               var1.visitLdcInsn(this.readConst(this.readUnsignedShort(var3 + 1), var5));
               var3 += 3;
               break;
            case 13:
               var1.visitIincInsn(var4[var3 + 1] & 255, var4[var3 + 2]);
               var3 += 3;
               break;
            case 14:
               var3 = var3 + 4 - (var29 & 3);
               var51 = var29 + this.readInt(var3);
               var52 = this.readInt(var3 + 4);
               int var54 = this.readInt(var3 + 8);
               var35 = new Label[var54 - var52 + 1];
               var3 += 12;

               for(var36 = 0; var36 < var35.length; ++var36) {
                  var35[var36] = var11[var29 + this.readInt(var3)];
                  var3 += 4;
               }

               var1.visitTableSwitchInsn(var52, var54, var11[var51], var35);
               break;
            case 15:
               var3 = var3 + 4 - (var29 & 3);
               var51 = var29 + this.readInt(var3);
               var52 = this.readInt(var3 + 4);
               int[] var34 = new int[var52];
               var35 = new Label[var52];
               var3 += 8;

               for(var36 = 0; var36 < var52; ++var36) {
                  var34[var36] = this.readInt(var3);
                  var35[var36] = var11[var29 + this.readInt(var3 + 4)];
                  var3 += 8;
               }

               var1.visitLookupSwitchInsn(var11[var51], var34, var35);
               break;
            case 16:
            default:
               var1.visitMultiANewArrayInsn(this.readClass(var3 + 1, var5), var4[var3 + 3] & 255);
               var3 += 4;
               break;
            case 17:
               var31 = var4[var3 + 1] & 255;
               if (var31 == 132) {
                  var1.visitIincInsn(this.readUnsignedShort(var3 + 2), this.readShort(var3 + 4));
                  var3 += 6;
               } else {
                  var1.visitVarInsn(var31, this.readUnsignedShort(var3 + 2));
                  var3 += 4;
               }
               break;
            case 18:
               var31 = var31 < 218 ? var31 - 49 : var31 - 20;
               var32 = var11[var29 + this.readUnsignedShort(var3 + 1)];
               if (var31 != 167 && var31 != 168) {
                  var31 = var31 <= 166 ? (var31 + 1 ^ 1) - 1 : var31 ^ 1;
                  Label var33 = this.readLabel(var29 + 3, var11);
                  var1.visitJumpInsn(var31, var33);
                  var1.visitJumpInsn(200, var32);
                  var46 = true;
               } else {
                  var1.visitJumpInsn(var31 + 33, var32);
               }

               var3 += 3;
               break;
            case 19:
               var1.visitJumpInsn(200, var11[var29 + this.readInt(var3 + 1)]);
               var46 = true;
               var3 += 5;
            }

            for(var3 += 5; var39 != null && var14 < var39.length && var44 <= var29; var44 = var14 < var39.length && this.readByte(var39[var14]) >= 67 ? this.readUnsignedShort(var39[var14] + 1) : -1) {
               if (var44 == var29) {
                  var51 = this.a(var2, var39[var14]);
                  this.a(var51 + 2, var5, true, var1.visitInsnAnnotation(var2.i, var2.j, this.readUTF8(var51, var5), true));
               }

               ++var14;
            }

            while(var41 != null && var42 < var41.length && var17 <= var29) {
               if (var17 == var29) {
                  var51 = this.a(var2, var41[var42]);
                  this.a(var51 + 2, var5, true, var1.visitInsnAnnotation(var2.i, var2.j, this.readUTF8(var51, var5), false));
               }

               ++var42;
               var17 = var42 < var41.length && this.readByte(var41[var42]) >= 67 ? this.readUnsignedShort(var41[var42] + 1) : -1;
            }
         }

         if (var11[var8] != null) {
            var1.visitLabel(var11[var8]);
         }

         if ((var2.b & 2) == 0 && var18 != 0) {
            int[] var48 = null;
            if (var19 != 0) {
               var3 = var19 + 2;
               var48 = new int[this.readUnsignedShort(var19) * 3];

               for(var47 = var48.length; var47 > 0; var3 += 10) {
                  --var47;
                  var48[var47] = var3 + 6;
                  --var47;
                  var48[var47] = this.readUnsignedShort(var3 + 8);
                  --var47;
                  var48[var47] = this.readUnsignedShort(var3);
               }
            }

            var3 = var18 + 2;

            for(var47 = this.readUnsignedShort(var18); var47 > 0; --var47) {
               var31 = this.readUnsignedShort(var3);
               var51 = this.readUnsignedShort(var3 + 2);
               var52 = this.readUnsignedShort(var3 + 8);
               var57 = null;
               if (var48 != null) {
                  for(var58 = 0; var58 < var48.length; var58 += 3) {
                     if (var48[var58] == var31 && var48[var58 + 1] == var52) {
                        var57 = this.readUTF8(var48[var58 + 2], var5);
                        break;
                     }
                  }
               }

               var1.visitLocalVariable(this.readUTF8(var3 + 4, var5), this.readUTF8(var3 + 6, var5), var57, var11[var31], var11[var31 + var51], var52);
               var3 += 10;
            }
         }

         if (var39 != null) {
            for(var29 = 0; var29 < var39.length; ++var29) {
               if (this.readByte(var39[var29]) >> 1 == 32) {
                  var47 = this.a(var2, var39[var29]);
                  this.a(var47 + 2, var5, true, var1.visitLocalVariableAnnotation(var2.i, var2.j, var2.l, var2.m, var2.n, this.readUTF8(var47, var5), true));
               }
            }
         }

         if (var41 != null) {
            for(var29 = 0; var29 < var41.length; ++var29) {
               if (this.readByte(var41[var29]) >> 1 == 32) {
                  var47 = this.a(var2, var41[var29]);
                  this.a(var47 + 2, var5, true, var1.visitLocalVariableAnnotation(var2.i, var2.j, var2.l, var2.m, var2.n, this.readUTF8(var47, var5), false));
               }
            }
         }

         while(var26 != null) {
            Attribute var53 = var26.a;
            var26.a = null;
            var1.visitAttribute(var26);
            var26 = var53;
         }

         var1.visitMaxs(var6, var7);
         return;
      }
   }

   private int[] a(MethodVisitor var1, Context var2, int var3, boolean var4) {
      char[] var5 = var2.c;
      int[] var6 = new int[this.readUnsignedShort(var3)];
      var3 += 2;

      for(int var7 = 0; var7 < var6.length; ++var7) {
         var6[var7] = var3;
         int var8 = this.readInt(var3);
         int var9;
         switch(var8 >>> 24) {
         case 0:
         case 1:
         case 22:
            var3 += 2;
            break;
         case 19:
         case 20:
         case 21:
            ++var3;
            break;
         case 64:
         case 65:
            for(var9 = this.readUnsignedShort(var3 + 1); var9 > 0; --var9) {
               int var10 = this.readUnsignedShort(var3 + 3);
               int var11 = this.readUnsignedShort(var3 + 5);
               this.readLabel(var10, var2.h);
               this.readLabel(var10 + var11, var2.h);
               var3 += 6;
            }

            var3 += 3;
            break;
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
            var3 += 4;
            break;
         default:
            var3 += 3;
         }

         var9 = this.readByte(var3);
         if (var8 >>> 24 == 66) {
            TypePath var12 = var9 == 0 ? null : new TypePath(this.b, var3);
            var3 += 1 + 2 * var9;
            var3 = this.a(var3 + 2, var5, true, var1.visitTryCatchAnnotation(var8, var12, this.readUTF8(var3, var5), var4));
         } else {
            var3 = this.a(var3 + 3 + 2 * var9, var5, true, (AnnotationVisitor)null);
         }
      }

      return var6;
   }

   private int a(Context var1, int var2) {
      int var3;
      int var4;
      var3 = this.readInt(var2);
      label29:
      switch(var3 >>> 24) {
      case 0:
      case 1:
      case 22:
         var3 &= -65536;
         var2 += 2;
         break;
      case 19:
      case 20:
      case 21:
         var3 &= -16777216;
         ++var2;
         break;
      case 64:
      case 65:
         var3 &= -16777216;
         var4 = this.readUnsignedShort(var2 + 1);
         var1.l = new Label[var4];
         var1.m = new Label[var4];
         var1.n = new int[var4];
         var2 += 3;
         int var5 = 0;

         while(true) {
            if (var5 >= var4) {
               break label29;
            }

            int var6 = this.readUnsignedShort(var2);
            int var7 = this.readUnsignedShort(var2 + 2);
            var1.l[var5] = this.readLabel(var6, var1.h);
            var1.m[var5] = this.readLabel(var6 + var7, var1.h);
            var1.n[var5] = this.readUnsignedShort(var2 + 4);
            var2 += 6;
            ++var5;
         }
      case 71:
      case 72:
      case 73:
      case 74:
      case 75:
         var3 &= -16776961;
         var2 += 4;
         break;
      default:
         var3 &= var3 >>> 24 < 67 ? -256 : -16777216;
         var2 += 3;
      }

      var4 = this.readByte(var2);
      var1.i = var3;
      var1.j = var4 == 0 ? null : new TypePath(this.b, var2);
      return var2 + 1 + 2 * var4;
   }

   private void b(MethodVisitor var1, Context var2, int var3, boolean var4) {
      int var6 = this.b[var3++] & 255;
      int var7 = Type.getArgumentTypes(var2.g).length - var6;

      int var5;
      AnnotationVisitor var8;
      for(var5 = 0; var5 < var7; ++var5) {
         var8 = var1.visitParameterAnnotation(var5, "Ljava/lang/Synthetic;", false);
         if (var8 != null) {
            var8.visitEnd();
         }
      }

      for(char[] var9 = var2.c; var5 < var6 + var7; ++var5) {
         int var10 = this.readUnsignedShort(var3);

         for(var3 += 2; var10 > 0; --var10) {
            var8 = var1.visitParameterAnnotation(var5, this.readUTF8(var3, var9), var4);
            var3 = this.a(var3 + 2, var9, true, var8);
         }
      }

   }

   private int a(int var1, char[] var2, boolean var3, AnnotationVisitor var4) {
      int var5 = this.readUnsignedShort(var1);
      var1 += 2;
      if (var3) {
         while(var5 > 0) {
            var1 = this.a(var1 + 2, var2, this.readUTF8(var1, var2), var4);
            --var5;
         }
      } else {
         while(var5 > 0) {
            var1 = this.a(var1, var2, (String)null, var4);
            --var5;
         }
      }

      if (var4 != null) {
         var4.visitEnd();
      }

      return var1;
   }

   private int a(int var1, char[] var2, String var3, AnnotationVisitor var4) {
      if (var4 == null) {
         switch(this.b[var1] & 255) {
         case 64:
            return this.a(var1 + 3, var2, true, (AnnotationVisitor)null);
         case 91:
            return this.a(var1 + 1, var2, false, (AnnotationVisitor)null);
         case 101:
            return var1 + 5;
         default:
            return var1 + 3;
         }
      } else {
         switch(this.b[var1++] & 255) {
         case 64:
            var1 = this.a(var1 + 2, var2, true, var4.visitAnnotation(var3, this.readUTF8(var1, var2)));
         case 65:
         case 69:
         case 71:
         case 72:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 92:
         case 93:
         case 94:
         case 95:
         case 96:
         case 97:
         case 98:
         case 100:
         case 102:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 108:
         case 109:
         case 110:
         case 111:
         case 112:
         case 113:
         case 114:
         default:
            break;
         case 66:
            var4.visit(var3, new Byte((byte)this.readInt(this.a[this.readUnsignedShort(var1)])));
            var1 += 2;
            break;
         case 67:
            var4.visit(var3, new Character((char)this.readInt(this.a[this.readUnsignedShort(var1)])));
            var1 += 2;
            break;
         case 68:
         case 70:
         case 73:
         case 74:
            var4.visit(var3, this.readConst(this.readUnsignedShort(var1), var2));
            var1 += 2;
            break;
         case 83:
            var4.visit(var3, new Short((short)this.readInt(this.a[this.readUnsignedShort(var1)])));
            var1 += 2;
            break;
         case 90:
            var4.visit(var3, this.readInt(this.a[this.readUnsignedShort(var1)]) == 0 ? Boolean.FALSE : Boolean.TRUE);
            var1 += 2;
            break;
         case 91:
            int var6 = this.readUnsignedShort(var1);
            var1 += 2;
            if (var6 == 0) {
               return this.a(var1 - 2, var2, false, var4.visitArray(var3));
            }

            int var5;
            switch(this.b[var1++] & 255) {
            case 66:
               byte[] var7 = new byte[var6];

               for(var5 = 0; var5 < var6; ++var5) {
                  var7[var5] = (byte)this.readInt(this.a[this.readUnsignedShort(var1)]);
                  var1 += 3;
               }

               var4.visit(var3, var7);
               --var1;
               return var1;
            case 67:
               char[] var10 = new char[var6];

               for(var5 = 0; var5 < var6; ++var5) {
                  var10[var5] = (char)this.readInt(this.a[this.readUnsignedShort(var1)]);
                  var1 += 3;
               }

               var4.visit(var3, var10);
               --var1;
               return var1;
            case 68:
               double[] var14 = new double[var6];

               for(var5 = 0; var5 < var6; ++var5) {
                  var14[var5] = Double.longBitsToDouble(this.readLong(this.a[this.readUnsignedShort(var1)]));
                  var1 += 3;
               }

               var4.visit(var3, var14);
               --var1;
               return var1;
            case 69:
            case 71:
            case 72:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            default:
               var1 = this.a(var1 - 3, var2, false, var4.visitArray(var3));
               return var1;
            case 70:
               float[] var13 = new float[var6];

               for(var5 = 0; var5 < var6; ++var5) {
                  var13[var5] = Float.intBitsToFloat(this.readInt(this.a[this.readUnsignedShort(var1)]));
                  var1 += 3;
               }

               var4.visit(var3, var13);
               --var1;
               return var1;
            case 73:
               int[] var11 = new int[var6];

               for(var5 = 0; var5 < var6; ++var5) {
                  var11[var5] = this.readInt(this.a[this.readUnsignedShort(var1)]);
                  var1 += 3;
               }

               var4.visit(var3, var11);
               --var1;
               return var1;
            case 74:
               long[] var12 = new long[var6];

               for(var5 = 0; var5 < var6; ++var5) {
                  var12[var5] = this.readLong(this.a[this.readUnsignedShort(var1)]);
                  var1 += 3;
               }

               var4.visit(var3, var12);
               --var1;
               return var1;
            case 83:
               short[] var9 = new short[var6];

               for(var5 = 0; var5 < var6; ++var5) {
                  var9[var5] = (short)this.readInt(this.a[this.readUnsignedShort(var1)]);
                  var1 += 3;
               }

               var4.visit(var3, var9);
               --var1;
               return var1;
            case 90:
               boolean[] var8 = new boolean[var6];

               for(var5 = 0; var5 < var6; ++var5) {
                  var8[var5] = this.readInt(this.a[this.readUnsignedShort(var1)]) != 0;
                  var1 += 3;
               }

               var4.visit(var3, var8);
               --var1;
               return var1;
            }
         case 99:
            var4.visit(var3, Type.getType(this.readUTF8(var1, var2)));
            var1 += 2;
            break;
         case 101:
            var4.visitEnum(var3, this.readUTF8(var1, var2), this.readUTF8(var1 + 2, var2));
            var1 += 4;
            break;
         case 115:
            var4.visit(var3, this.readUTF8(var1, var2));
            var1 += 2;
         }

         return var1;
      }
   }

   private void a(Context var1) {
      String var2 = var1.g;
      Object[] var3 = var1.s;
      int var4 = 0;
      if ((var1.e & 8) == 0) {
         if ("<init>".equals(var1.f)) {
            var3[var4++] = Opcodes.UNINITIALIZED_THIS;
         } else {
            var3[var4++] = this.readClass(this.header + 2, var1.c);
         }
      }

      int var5 = 1;

      while(true) {
         int var6 = var5;
         switch(var2.charAt(var5++)) {
         case 'B':
         case 'C':
         case 'I':
         case 'S':
         case 'Z':
            var3[var4++] = Opcodes.INTEGER;
            break;
         case 'D':
            var3[var4++] = Opcodes.DOUBLE;
            break;
         case 'E':
         case 'G':
         case 'H':
         case 'K':
         case 'M':
         case 'N':
         case 'O':
         case 'P':
         case 'Q':
         case 'R':
         case 'T':
         case 'U':
         case 'V':
         case 'W':
         case 'X':
         case 'Y':
         default:
            var1.q = var4;
            return;
         case 'F':
            var3[var4++] = Opcodes.FLOAT;
            break;
         case 'J':
            var3[var4++] = Opcodes.LONG;
            break;
         case 'L':
            while(var2.charAt(var5) != ';') {
               ++var5;
            }

            var3[var4++] = var2.substring(var6 + 1, var5++);
            break;
         case '[':
            while(var2.charAt(var5) == '[') {
               ++var5;
            }

            if (var2.charAt(var5) == 'L') {
               ++var5;

               while(var2.charAt(var5) != ';') {
                  ++var5;
               }
            }

            int var10001 = var4++;
            ++var5;
            var3[var10001] = var2.substring(var6, var5);
         }
      }
   }

   private int a(int var1, boolean var2, boolean var3, Context var4) {
      char[] var5 = var4.c;
      Label[] var6 = var4.h;
      int var7;
      if (var2) {
         var7 = this.b[var1++] & 255;
      } else {
         var7 = 255;
         var4.o = -1;
      }

      var4.r = 0;
      int var8;
      if (var7 < 64) {
         var8 = var7;
         var4.p = 3;
         var4.t = 0;
      } else if (var7 < 128) {
         var8 = var7 - 64;
         var1 = this.a(var4.u, 0, var1, var5, var6);
         var4.p = 4;
         var4.t = 1;
      } else {
         var8 = this.readUnsignedShort(var1);
         var1 += 2;
         if (var7 == 247) {
            var1 = this.a(var4.u, 0, var1, var5, var6);
            var4.p = 4;
            var4.t = 1;
         } else if (var7 >= 248 && var7 < 251) {
            var4.p = 2;
            var4.r = 251 - var7;
            var4.q -= var4.r;
            var4.t = 0;
         } else if (var7 == 251) {
            var4.p = 3;
            var4.t = 0;
         } else {
            int var9;
            int var10;
            if (var7 < 255) {
               var9 = var3 ? var4.q : 0;

               for(var10 = var7 - 251; var10 > 0; --var10) {
                  var1 = this.a(var4.s, var9++, var1, var5, var6);
               }

               var4.p = 1;
               var4.r = var7 - 251;
               var4.q += var4.r;
               var4.t = 0;
            } else {
               var4.p = 0;
               var9 = this.readUnsignedShort(var1);
               var1 += 2;
               var4.r = var9;
               var4.q = var9;

               for(var10 = 0; var9 > 0; --var9) {
                  var1 = this.a(var4.s, var10++, var1, var5, var6);
               }

               var9 = this.readUnsignedShort(var1);
               var1 += 2;
               var4.t = var9;

               for(var10 = 0; var9 > 0; --var9) {
                  var1 = this.a(var4.u, var10++, var1, var5, var6);
               }
            }
         }
      }

      var4.o += var8 + 1;
      this.readLabel(var4.o, var6);
      return var1;
   }

   private int a(Object[] var1, int var2, int var3, char[] var4, Label[] var5) {
      int var6 = this.b[var3++] & 255;
      switch(var6) {
      case 0:
         var1[var2] = Opcodes.TOP;
         break;
      case 1:
         var1[var2] = Opcodes.INTEGER;
         break;
      case 2:
         var1[var2] = Opcodes.FLOAT;
         break;
      case 3:
         var1[var2] = Opcodes.DOUBLE;
         break;
      case 4:
         var1[var2] = Opcodes.LONG;
         break;
      case 5:
         var1[var2] = Opcodes.NULL;
         break;
      case 6:
         var1[var2] = Opcodes.UNINITIALIZED_THIS;
         break;
      case 7:
         var1[var2] = this.readClass(var3, var4);
         var3 += 2;
         break;
      default:
         var1[var2] = this.readLabel(this.readUnsignedShort(var3), var5);
         var3 += 2;
      }

      return var3;
   }

   protected Label readLabel(int var1, Label[] var2) {
      if (var2[var1] == null) {
         var2[var1] = new Label();
      }

      return var2[var1];
   }

   private int a() {
      int var1 = this.header + 8 + this.readUnsignedShort(this.header + 6) * 2;

      int var2;
      int var3;
      for(var2 = this.readUnsignedShort(var1); var2 > 0; --var2) {
         for(var3 = this.readUnsignedShort(var1 + 8); var3 > 0; --var3) {
            var1 += 6 + this.readInt(var1 + 12);
         }

         var1 += 8;
      }

      var1 += 2;

      for(var2 = this.readUnsignedShort(var1); var2 > 0; --var2) {
         for(var3 = this.readUnsignedShort(var1 + 8); var3 > 0; --var3) {
            var1 += 6 + this.readInt(var1 + 12);
         }

         var1 += 8;
      }

      return var1 + 2;
   }

   private Attribute a(Attribute[] var1, String var2, int var3, int var4, char[] var5, int var6, Label[] var7) {
      for(int var8 = 0; var8 < var1.length; ++var8) {
         if (var1[var8].type.equals(var2)) {
            return var1[var8].read(this, var3, var4, var5, var6, var7);
         }
      }

      return (new Attribute(var2)).read(this, var3, var4, (char[])null, -1, (Label[])null);
   }

   public int getItemCount() {
      return this.a.length;
   }

   public int getItem(int var1) {
      return this.a[var1];
   }

   public int getMaxStringLength() {
      return this.d;
   }

   public int readByte(int var1) {
      return this.b[var1] & 255;
   }

   public int readUnsignedShort(int var1) {
      byte[] var2 = this.b;
      return (var2[var1] & 255) << 8 | var2[var1 + 1] & 255;
   }

   public short readShort(int var1) {
      byte[] var2 = this.b;
      return (short)((var2[var1] & 255) << 8 | var2[var1 + 1] & 255);
   }

   public int readInt(int var1) {
      byte[] var2 = this.b;
      return (var2[var1] & 255) << 24 | (var2[var1 + 1] & 255) << 16 | (var2[var1 + 2] & 255) << 8 | var2[var1 + 3] & 255;
   }

   public long readLong(int var1) {
      long var2 = (long)this.readInt(var1);
      long var4 = (long)this.readInt(var1 + 4) & 4294967295L;
      return var2 << 32 | var4;
   }

   public String readUTF8(int var1, char[] var2) {
      int var3 = this.readUnsignedShort(var1);
      if (var1 != 0 && var3 != 0) {
         String var4 = this.c[var3];
         if (var4 != null) {
            return var4;
         } else {
            var1 = this.a[var3];
            return this.c[var3] = this.a(var1 + 2, this.readUnsignedShort(var1), var2);
         }
      } else {
         return null;
      }
   }

   private String a(int var1, int var2, char[] var3) {
      int var4 = var1 + var2;
      byte[] var5 = this.b;
      int var6 = 0;
      byte var8 = 0;
      char var9 = 0;

      while(true) {
         while(var1 < var4) {
            byte var7 = var5[var1++];
            switch(var8) {
            case 0:
               int var10 = var7 & 255;
               if (var10 < 128) {
                  var3[var6++] = (char)var10;
               } else {
                  if (var10 < 224 && var10 > 191) {
                     var9 = (char)(var10 & 31);
                     var8 = 1;
                     continue;
                  }

                  var9 = (char)(var10 & 15);
                  var8 = 2;
               }
               break;
            case 1:
               var3[var6++] = (char)(var9 << 6 | var7 & 63);
               var8 = 0;
               break;
            case 2:
               var9 = (char)(var9 << 6 | var7 & 63);
               var8 = 1;
            }
         }

         return new String(var3, 0, var6);
      }
   }

   private String a(int var1, char[] var2) {
      return this.readUTF8(this.a[this.readUnsignedShort(var1)], var2);
   }

   public String readClass(int var1, char[] var2) {
      return this.a(var1, var2);
   }

   public String readModule(int var1, char[] var2) {
      return this.a(var1, var2);
   }

   public String readPackage(int var1, char[] var2) {
      return this.a(var1, var2);
   }

   public Object readConst(int var1, char[] var2) {
      int var3 = this.a[var1];
      switch(this.b[var3 - 1]) {
      case 3:
         return new Integer(this.readInt(var3));
      case 4:
         return new Float(Float.intBitsToFloat(this.readInt(var3)));
      case 5:
         return new Long(this.readLong(var3));
      case 6:
         return new Double(Double.longBitsToDouble(this.readLong(var3)));
      case 7:
         return Type.getObjectType(this.readUTF8(var3, var2));
      case 8:
         return this.readUTF8(var3, var2);
      case 9:
      case 10:
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
      default:
         int var4 = this.readByte(var3);
         int[] var5 = this.a;
         int var6 = var5[this.readUnsignedShort(var3 + 1)];
         boolean var7 = this.b[var6 - 1] == 11;
         String var8 = this.readClass(var6, var2);
         var6 = var5[this.readUnsignedShort(var6 + 2)];
         String var9 = this.readUTF8(var6, var2);
         String var10 = this.readUTF8(var6 + 2, var2);
         return new Handle(var4, var8, var9, var10, var7);
      case 16:
         return Type.getMethodType(this.readUTF8(var3, var2));
      }
   }
}

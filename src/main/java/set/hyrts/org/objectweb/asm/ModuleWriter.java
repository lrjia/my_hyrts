package set.hyrts.org.objectweb.asm;

final class ModuleWriter extends ModuleVisitor {
   private final ClassWriter a;
   int b;
   int c;
   int d;
   private final int e;
   private final int f;
   private final int g;
   private int h;
   private int i;
   private ByteVector j;
   private int k;
   private ByteVector l;
   private int m;
   private ByteVector n;
   private int o;
   private ByteVector p;
   private int q;
   private ByteVector r;
   private int s;
   private ByteVector t;

   ModuleWriter(ClassWriter var1, int var2, int var3, int var4) {
      super(393216);
      this.a = var1;
      this.b = 16;
      this.e = var2;
      this.f = var3;
      this.g = var4;
   }

   public void visitMainClass(String var1) {
      if (this.h == 0) {
         this.a.newUTF8("ModuleMainClass");
         ++this.c;
         this.d += 8;
      }

      this.h = this.a.newClass(var1);
   }

   public void visitPackage(String var1) {
      if (this.j == null) {
         this.a.newUTF8("ModulePackages");
         this.j = new ByteVector();
         ++this.c;
         this.d += 8;
      }

      this.j.putShort(this.a.newPackage(var1));
      ++this.i;
      this.d += 2;
   }

   public void visitRequire(String var1, int var2, String var3) {
      if (this.l == null) {
         this.l = new ByteVector();
      }

      this.l.putShort(this.a.newModule(var1)).putShort(var2).putShort(var3 == null ? 0 : this.a.newUTF8(var3));
      ++this.k;
      this.b += 6;
   }

   public void visitExport(String var1, int var2, String... var3) {
      if (this.n == null) {
         this.n = new ByteVector();
      }

      this.n.putShort(this.a.newPackage(var1)).putShort(var2);
      if (var3 == null) {
         this.n.putShort(0);
         this.b += 6;
      } else {
         this.n.putShort(var3.length);
         String[] var4 = var3;
         int var5 = var3.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String var7 = var4[var6];
            this.n.putShort(this.a.newModule(var7));
         }

         this.b += 6 + 2 * var3.length;
      }

      ++this.m;
   }

   public void visitOpen(String var1, int var2, String... var3) {
      if (this.p == null) {
         this.p = new ByteVector();
      }

      this.p.putShort(this.a.newPackage(var1)).putShort(var2);
      if (var3 == null) {
         this.p.putShort(0);
         this.b += 6;
      } else {
         this.p.putShort(var3.length);
         String[] var4 = var3;
         int var5 = var3.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String var7 = var4[var6];
            this.p.putShort(this.a.newModule(var7));
         }

         this.b += 6 + 2 * var3.length;
      }

      ++this.o;
   }

   public void visitUse(String var1) {
      if (this.r == null) {
         this.r = new ByteVector();
      }

      this.r.putShort(this.a.newClass(var1));
      ++this.q;
      this.b += 2;
   }

   public void visitProvide(String var1, String... var2) {
      if (this.t == null) {
         this.t = new ByteVector();
      }

      this.t.putShort(this.a.newClass(var1));
      this.t.putShort(var2.length);
      String[] var3 = var2;
      int var4 = var2.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String var6 = var3[var5];
         this.t.putShort(this.a.newClass(var6));
      }

      ++this.s;
      this.b += 4 + 2 * var2.length;
   }

   public void visitEnd() {
   }

   void a(ByteVector var1) {
      if (this.h != 0) {
         var1.putShort(this.a.newUTF8("ModuleMainClass")).putInt(2).putShort(this.h);
      }

      if (this.j != null) {
         var1.putShort(this.a.newUTF8("ModulePackages")).putInt(2 + 2 * this.i).putShort(this.i).putByteArray(this.j.a, 0, this.j.b);
      }

   }

   void b(ByteVector var1) {
      var1.putInt(this.b);
      var1.putShort(this.e).putShort(this.f).putShort(this.g);
      var1.putShort(this.k);
      if (this.l != null) {
         var1.putByteArray(this.l.a, 0, this.l.b);
      }

      var1.putShort(this.m);
      if (this.n != null) {
         var1.putByteArray(this.n.a, 0, this.n.b);
      }

      var1.putShort(this.o);
      if (this.p != null) {
         var1.putByteArray(this.p.a, 0, this.p.b);
      }

      var1.putShort(this.q);
      if (this.r != null) {
         var1.putByteArray(this.r.a, 0, this.r.b);
      }

      var1.putShort(this.s);
      if (this.t != null) {
         var1.putByteArray(this.t.a, 0, this.t.b);
      }

   }
}

package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;
import set.hyrts.org.objectweb.asm.AnnotationVisitor;
import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.FieldVisitor;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.ModuleVisitor;
import set.hyrts.org.objectweb.asm.TypePath;

public final class SAXClassAdapter extends ClassVisitor {
   SAXAdapter sa;
   private final boolean singleDocument;

   public SAXClassAdapter(ContentHandler var1, boolean var2) {
      super(393216);
      this.sa = new SAXAdapter(var1);
      this.singleDocument = var2;
      if (!var2) {
         this.sa.addDocumentStart();
      }

   }

   public void visitSource(String var1, String var2) {
      AttributesImpl var3 = new AttributesImpl();
      if (var1 != null) {
         var3.addAttribute("", "file", "file", "", encode(var1));
      }

      if (var2 != null) {
         var3.addAttribute("", "debug", "debug", "", encode(var2));
      }

      this.sa.addElement("source", var3);
   }

   public ModuleVisitor visitModule(String var1, int var2, String var3) {
      AttributesImpl var4 = new AttributesImpl();
      var4.addAttribute("", "name", "name", "", var1);
      StringBuffer var5 = new StringBuffer();
      appendAccess(var2 | 2097152, var5);
      var4.addAttribute("", "access", "access", "", var5.toString());
      if (var3 != null) {
         var4.addAttribute("", "version", "version", "", encode(var3));
      }

      this.sa.addStart("module", var4);
      return new SAXModuleAdapter(this.sa);
   }

   public void visitOuterClass(String var1, String var2, String var3) {
      AttributesImpl var4 = new AttributesImpl();
      var4.addAttribute("", "owner", "owner", "", var1);
      if (var2 != null) {
         var4.addAttribute("", "name", "name", "", var2);
      }

      if (var3 != null) {
         var4.addAttribute("", "desc", "desc", "", var3);
      }

      this.sa.addElement("outerclass", var4);
   }

   public AnnotationVisitor visitAnnotation(String var1, boolean var2) {
      return new SAXAnnotationAdapter(this.sa, "annotation", var2 ? 1 : -1, (String)null, var1);
   }

   public AnnotationVisitor visitTypeAnnotation(int var1, TypePath var2, String var3, boolean var4) {
      return new SAXAnnotationAdapter(this.sa, "typeAnnotation", var4 ? 1 : -1, (String)null, var3, var1, var2);
   }

   public void visit(int var1, int var2, String var3, String var4, String var5, String[] var6) {
      StringBuffer var7 = new StringBuffer();
      appendAccess(var2 | 262144, var7);
      AttributesImpl var8 = new AttributesImpl();
      var8.addAttribute("", "access", "access", "", var7.toString());
      if (var3 != null) {
         var8.addAttribute("", "name", "name", "", var3);
      }

      if (var4 != null) {
         var8.addAttribute("", "signature", "signature", "", encode(var4));
      }

      if (var5 != null) {
         var8.addAttribute("", "parent", "parent", "", var5);
      }

      var8.addAttribute("", "major", "major", "", Integer.toString(var1 & '\uffff'));
      var8.addAttribute("", "minor", "minor", "", Integer.toString(var1 >>> 16));
      this.sa.addStart("class", var8);
      this.sa.addStart("interfaces", new AttributesImpl());
      if (var6 != null && var6.length > 0) {
         for(int var9 = 0; var9 < var6.length; ++var9) {
            AttributesImpl var10 = new AttributesImpl();
            var10.addAttribute("", "name", "name", "", var6[var9]);
            this.sa.addElement("interface", var10);
         }
      }

      this.sa.addEnd("interfaces");
   }

   public FieldVisitor visitField(int var1, String var2, String var3, String var4, Object var5) {
      StringBuffer var6 = new StringBuffer();
      appendAccess(var1 | 524288, var6);
      AttributesImpl var7 = new AttributesImpl();
      var7.addAttribute("", "access", "access", "", var6.toString());
      var7.addAttribute("", "name", "name", "", var2);
      var7.addAttribute("", "desc", "desc", "", var3);
      if (var4 != null) {
         var7.addAttribute("", "signature", "signature", "", encode(var4));
      }

      if (var5 != null) {
         var7.addAttribute("", "value", "value", "", encode(var5.toString()));
      }

      return new SAXFieldAdapter(this.sa, var7);
   }

   public MethodVisitor visitMethod(int var1, String var2, String var3, String var4, String[] var5) {
      StringBuffer var6 = new StringBuffer();
      appendAccess(var1, var6);
      AttributesImpl var7 = new AttributesImpl();
      var7.addAttribute("", "access", "access", "", var6.toString());
      var7.addAttribute("", "name", "name", "", var2);
      var7.addAttribute("", "desc", "desc", "", var3);
      if (var4 != null) {
         var7.addAttribute("", "signature", "signature", "", var4);
      }

      this.sa.addStart("method", var7);
      this.sa.addStart("exceptions", new AttributesImpl());
      if (var5 != null && var5.length > 0) {
         for(int var8 = 0; var8 < var5.length; ++var8) {
            AttributesImpl var9 = new AttributesImpl();
            var9.addAttribute("", "name", "name", "", var5[var8]);
            this.sa.addElement("exception", var9);
         }
      }

      this.sa.addEnd("exceptions");
      return new SAXCodeAdapter(this.sa, var1);
   }

   public final void visitInnerClass(String var1, String var2, String var3, int var4) {
      StringBuffer var5 = new StringBuffer();
      appendAccess(var4 | 1048576, var5);
      AttributesImpl var6 = new AttributesImpl();
      var6.addAttribute("", "access", "access", "", var5.toString());
      if (var1 != null) {
         var6.addAttribute("", "name", "name", "", var1);
      }

      if (var2 != null) {
         var6.addAttribute("", "outerName", "outerName", "", var2);
      }

      if (var3 != null) {
         var6.addAttribute("", "innerName", "innerName", "", var3);
      }

      this.sa.addElement("innerclass", var6);
   }

   public final void visitEnd() {
      this.sa.addEnd("class");
      if (!this.singleDocument) {
         this.sa.addDocumentEnd();
      }

   }

   static final String encode(String var0) {
      StringBuffer var1 = new StringBuffer();

      for(int var2 = 0; var2 < var0.length(); ++var2) {
         char var3 = var0.charAt(var2);
         if (var3 == '\\') {
            var1.append("\\\\");
         } else if (var3 >= ' ' && var3 <= 127) {
            var1.append(var3);
         } else {
            var1.append("\\u");
            if (var3 < 16) {
               var1.append("000");
            } else if (var3 < 256) {
               var1.append("00");
            } else if (var3 < 4096) {
               var1.append('0');
            }

            var1.append(Integer.toString(var3, 16));
         }
      }

      return var1.toString();
   }

   static void appendAccess(int var0, StringBuffer var1) {
      if ((var0 & 1) != 0) {
         var1.append("public ");
      }

      if ((var0 & 2) != 0) {
         var1.append("private ");
      }

      if ((var0 & 4) != 0) {
         var1.append("protected ");
      }

      if ((var0 & 16) != 0) {
         if ((var0 & 2097152) == 0) {
            var1.append("final ");
         } else {
            var1.append("transitive ");
         }
      }

      if ((var0 & 8) != 0) {
         var1.append("static ");
      }

      if ((var0 & 32) != 0) {
         if ((var0 & 262144) == 0) {
            if ((var0 & 4194304) != 0) {
               var1.append("transitive ");
            } else if ((var0 & 2097152) == 0) {
               var1.append("synchronized ");
            } else {
               var1.append("open ");
            }
         } else {
            var1.append("super ");
         }
      }

      if ((var0 & 64) != 0) {
         if ((var0 & 524288) == 0) {
            var1.append("bridge ");
         } else if ((var0 & 4194304) == 0) {
            var1.append("volatile ");
         } else {
            var1.append("static ");
         }
      }

      if ((var0 & 128) != 0) {
         if ((var0 & 524288) == 0) {
            var1.append("varargs ");
         } else {
            var1.append("transient ");
         }
      }

      if ((var0 & 256) != 0) {
         var1.append("native ");
      }

      if ((var0 & 2048) != 0) {
         var1.append("strict ");
      }

      if ((var0 & 512) != 0) {
         var1.append("interface ");
      }

      if ((var0 & 1024) != 0) {
         var1.append("abstract ");
      }

      if ((var0 & 4096) != 0) {
         var1.append("synthetic ");
      }

      if ((var0 & 8192) != 0) {
         var1.append("annotation ");
      }

      if ((var0 & 16384) != 0) {
         var1.append("enum ");
      }

      if ((var0 & 131072) != 0) {
         var1.append("deprecated ");
      }

      if ((var0 & '耀') != 0) {
         if ((var0 & 262144) == 0) {
            var1.append("module ");
         } else {
            var1.append("mandated ");
         }
      }

   }
}

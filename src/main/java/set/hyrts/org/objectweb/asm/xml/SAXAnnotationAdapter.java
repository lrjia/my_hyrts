package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.helpers.AttributesImpl;
import set.hyrts.org.objectweb.asm.AnnotationVisitor;
import set.hyrts.org.objectweb.asm.Type;
import set.hyrts.org.objectweb.asm.TypePath;

public final class SAXAnnotationAdapter extends AnnotationVisitor {
   SAXAdapter sa;
   private final String elementName;

   public SAXAnnotationAdapter(SAXAdapter var1, String var2, int var3, String var4, String var5) {
      this(393216, var1, var2, var3, var5, var4, -1, -1, (TypePath)null, (String[])null, (String[])null, (int[])null);
   }

   public SAXAnnotationAdapter(SAXAdapter var1, String var2, int var3, int var4, String var5) {
      this(393216, var1, var2, var3, var5, (String)null, var4, -1, (TypePath)null, (String[])null, (String[])null, (int[])null);
   }

   public SAXAnnotationAdapter(SAXAdapter var1, String var2, int var3, String var4, String var5, int var6, TypePath var7) {
      this(393216, var1, var2, var3, var5, var4, -1, var6, var7, (String[])null, (String[])null, (int[])null);
   }

   public SAXAnnotationAdapter(SAXAdapter var1, String var2, int var3, String var4, String var5, int var6, TypePath var7, String[] var8, String[] var9, int[] var10) {
      this(393216, var1, var2, var3, var5, var4, -1, var6, var7, var8, var9, var10);
   }

   protected SAXAnnotationAdapter(int var1, SAXAdapter var2, String var3, int var4, String var5, String var6, int var7) {
      this(var1, var2, var3, var4, var5, var6, var7, -1, (TypePath)null, (String[])null, (String[])null, (int[])null);
   }

   protected SAXAnnotationAdapter(int var1, SAXAdapter var2, String var3, int var4, String var5, String var6, int var7, int var8, TypePath var9, String[] var10, String[] var11, int[] var12) {
      super(var1);
      this.sa = var2;
      this.elementName = var3;
      AttributesImpl var13 = new AttributesImpl();
      if (var6 != null) {
         var13.addAttribute("", "name", "name", "", var6);
      }

      if (var4 != 0) {
         var13.addAttribute("", "visible", "visible", "", var4 > 0 ? "true" : "false");
      }

      if (var7 != -1) {
         var13.addAttribute("", "parameter", "parameter", "", Integer.toString(var7));
      }

      if (var5 != null) {
         var13.addAttribute("", "desc", "desc", "", var5);
      }

      if (var8 != -1) {
         var13.addAttribute("", "typeRef", "typeRef", "", Integer.toString(var8));
      }

      if (var9 != null) {
         var13.addAttribute("", "typePath", "typePath", "", var9.toString());
      }

      StringBuffer var14;
      int var15;
      if (var10 != null) {
         var14 = new StringBuffer(var10[0]);

         for(var15 = 1; var15 < var10.length; ++var15) {
            var14.append(" ").append(var10[var15]);
         }

         var13.addAttribute("", "start", "start", "", var14.toString());
      }

      if (var11 != null) {
         var14 = new StringBuffer(var11[0]);

         for(var15 = 1; var15 < var11.length; ++var15) {
            var14.append(" ").append(var11[var15]);
         }

         var13.addAttribute("", "end", "end", "", var14.toString());
      }

      if (var12 != null) {
         var14 = new StringBuffer();
         var14.append(var12[0]);

         for(var15 = 1; var15 < var12.length; ++var15) {
            var14.append(" ").append(var12[var15]);
         }

         var13.addAttribute("", "index", "index", "", var14.toString());
      }

      var2.addStart(var3, var13);
   }

   public void visit(String var1, Object var2) {
      Class var3 = var2.getClass();
      if (var3.isArray()) {
         AnnotationVisitor var4 = this.visitArray(var1);
         int var6;
         if (var2 instanceof byte[]) {
            byte[] var13 = (byte[])((byte[])var2);

            for(var6 = 0; var6 < var13.length; ++var6) {
               var4.visit((String)null, new Byte(var13[var6]));
            }
         } else if (var2 instanceof char[]) {
            char[] var12 = (char[])((char[])var2);

            for(var6 = 0; var6 < var12.length; ++var6) {
               var4.visit((String)null, new Character(var12[var6]));
            }
         } else if (var2 instanceof short[]) {
            short[] var11 = (short[])((short[])var2);

            for(var6 = 0; var6 < var11.length; ++var6) {
               var4.visit((String)null, new Short(var11[var6]));
            }
         } else if (var2 instanceof boolean[]) {
            boolean[] var10 = (boolean[])((boolean[])var2);

            for(var6 = 0; var6 < var10.length; ++var6) {
               var4.visit((String)null, var10[var6]);
            }
         } else if (var2 instanceof int[]) {
            int[] var9 = (int[])((int[])var2);

            for(var6 = 0; var6 < var9.length; ++var6) {
               var4.visit((String)null, new Integer(var9[var6]));
            }
         } else if (var2 instanceof long[]) {
            long[] var8 = (long[])((long[])var2);

            for(var6 = 0; var6 < var8.length; ++var6) {
               var4.visit((String)null, new Long(var8[var6]));
            }
         } else if (var2 instanceof float[]) {
            float[] var7 = (float[])((float[])var2);

            for(var6 = 0; var6 < var7.length; ++var6) {
               var4.visit((String)null, new Float(var7[var6]));
            }
         } else if (var2 instanceof double[]) {
            double[] var5 = (double[])((double[])var2);

            for(var6 = 0; var6 < var5.length; ++var6) {
               var4.visit((String)null, new Double(var5[var6]));
            }
         }

         var4.visitEnd();
      } else {
         this.addValueElement("annotationValue", var1, Type.getDescriptor(var3), var2.toString());
      }

   }

   public void visitEnum(String var1, String var2, String var3) {
      this.addValueElement("annotationValueEnum", var1, var2, var3);
   }

   public AnnotationVisitor visitAnnotation(String var1, String var2) {
      return new SAXAnnotationAdapter(this.sa, "annotationValueAnnotation", 0, var1, var2);
   }

   public AnnotationVisitor visitArray(String var1) {
      return new SAXAnnotationAdapter(this.sa, "annotationValueArray", 0, var1, (String)null);
   }

   public void visitEnd() {
      this.sa.addEnd(this.elementName);
   }

   private void addValueElement(String var1, String var2, String var3, String var4) {
      AttributesImpl var5 = new AttributesImpl();
      if (var2 != null) {
         var5.addAttribute("", "name", "name", "", var2);
      }

      if (var3 != null) {
         var5.addAttribute("", "desc", "desc", "", var3);
      }

      if (var4 != null) {
         var5.addAttribute("", "value", "value", "", SAXClassAdapter.encode(var4));
      }

      this.sa.addElement(var1, var5);
   }
}

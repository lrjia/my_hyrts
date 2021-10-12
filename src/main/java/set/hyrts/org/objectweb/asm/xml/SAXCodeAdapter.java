package set.hyrts.org.objectweb.asm.xml;

import java.util.HashMap;
import java.util.Map;
import org.xml.sax.helpers.AttributesImpl;
import set.hyrts.org.objectweb.asm.AnnotationVisitor;
import set.hyrts.org.objectweb.asm.Handle;
import set.hyrts.org.objectweb.asm.Label;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Type;
import set.hyrts.org.objectweb.asm.TypePath;
import set.hyrts.org.objectweb.asm.util.Printer;

public final class SAXCodeAdapter extends MethodVisitor {
   static final String[] TYPES;
   SAXAdapter sa;
   int access;
   private final Map labelNames;

   public SAXCodeAdapter(SAXAdapter var1, int var2) {
      super(393216);
      this.sa = var1;
      this.access = var2;
      this.labelNames = new HashMap();
   }

   public void visitParameter(String var1, int var2) {
      AttributesImpl var3 = new AttributesImpl();
      if (var1 != null) {
         var3.addAttribute("", "name", "name", "", var1);
      }

      StringBuffer var4 = new StringBuffer();
      SAXClassAdapter.appendAccess(var2, var4);
      var3.addAttribute("", "access", "access", "", var4.toString());
      this.sa.addElement("parameter", var3);
   }

   public final void visitCode() {
      if ((this.access & 1792) == 0) {
         this.sa.addStart("code", new AttributesImpl());
      }

   }

   public void visitFrame(int var1, int var2, Object[] var3, int var4, Object[] var5) {
      AttributesImpl var6 = new AttributesImpl();
      switch(var1) {
      case -1:
      case 0:
         if (var1 == -1) {
            var6.addAttribute("", "type", "type", "", "NEW");
         } else {
            var6.addAttribute("", "type", "type", "", "FULL");
         }

         this.sa.addStart("frame", var6);
         this.appendFrameTypes(true, var2, var3);
         this.appendFrameTypes(false, var4, var5);
         break;
      case 1:
         var6.addAttribute("", "type", "type", "", "APPEND");
         this.sa.addStart("frame", var6);
         this.appendFrameTypes(true, var2, var3);
         break;
      case 2:
         var6.addAttribute("", "type", "type", "", "CHOP");
         var6.addAttribute("", "count", "count", "", Integer.toString(var2));
         this.sa.addStart("frame", var6);
         break;
      case 3:
         var6.addAttribute("", "type", "type", "", "SAME");
         this.sa.addStart("frame", var6);
         break;
      case 4:
         var6.addAttribute("", "type", "type", "", "SAME1");
         this.sa.addStart("frame", var6);
         this.appendFrameTypes(false, 1, var5);
      }

      this.sa.addEnd("frame");
   }

   private void appendFrameTypes(boolean var1, int var2, Object[] var3) {
      for(int var4 = 0; var4 < var2; ++var4) {
         Object var5 = var3[var4];
         AttributesImpl var6 = new AttributesImpl();
         if (var5 instanceof String) {
            var6.addAttribute("", "type", "type", "", (String)var5);
         } else if (var5 instanceof Integer) {
            var6.addAttribute("", "type", "type", "", TYPES[(Integer)var5]);
         } else {
            var6.addAttribute("", "type", "type", "", "uninitialized");
            var6.addAttribute("", "label", "label", "", this.getLabel((Label)var5));
         }

         this.sa.addElement(var1 ? "local" : "stack", var6);
      }

   }

   public final void visitInsn(int var1) {
      this.sa.addElement(Printer.OPCODES[var1], new AttributesImpl());
   }

   public final void visitIntInsn(int var1, int var2) {
      AttributesImpl var3 = new AttributesImpl();
      var3.addAttribute("", "value", "value", "", Integer.toString(var2));
      this.sa.addElement(Printer.OPCODES[var1], var3);
   }

   public final void visitVarInsn(int var1, int var2) {
      AttributesImpl var3 = new AttributesImpl();
      var3.addAttribute("", "var", "var", "", Integer.toString(var2));
      this.sa.addElement(Printer.OPCODES[var1], var3);
   }

   public final void visitTypeInsn(int var1, String var2) {
      AttributesImpl var3 = new AttributesImpl();
      var3.addAttribute("", "desc", "desc", "", var2);
      this.sa.addElement(Printer.OPCODES[var1], var3);
   }

   public final void visitFieldInsn(int var1, String var2, String var3, String var4) {
      AttributesImpl var5 = new AttributesImpl();
      var5.addAttribute("", "owner", "owner", "", var2);
      var5.addAttribute("", "name", "name", "", var3);
      var5.addAttribute("", "desc", "desc", "", var4);
      this.sa.addElement(Printer.OPCODES[var1], var5);
   }

   public final void visitMethodInsn(int var1, String var2, String var3, String var4, boolean var5) {
      AttributesImpl var6 = new AttributesImpl();
      var6.addAttribute("", "owner", "owner", "", var2);
      var6.addAttribute("", "name", "name", "", var3);
      var6.addAttribute("", "desc", "desc", "", var4);
      var6.addAttribute("", "itf", "itf", "", var5 ? "true" : "false");
      this.sa.addElement(Printer.OPCODES[var1], var6);
   }

   public void visitInvokeDynamicInsn(String var1, String var2, Handle var3, Object... var4) {
      AttributesImpl var5 = new AttributesImpl();
      var5.addAttribute("", "name", "name", "", var1);
      var5.addAttribute("", "desc", "desc", "", var2);
      var5.addAttribute("", "bsm", "bsm", "", SAXClassAdapter.encode(var3.toString()));
      this.sa.addStart("INVOKEDYNAMIC", var5);

      for(int var6 = 0; var6 < var4.length; ++var6) {
         this.sa.addElement("bsmArg", getConstantAttribute(var4[var6]));
      }

      this.sa.addEnd("INVOKEDYNAMIC");
   }

   public final void visitJumpInsn(int var1, Label var2) {
      AttributesImpl var3 = new AttributesImpl();
      var3.addAttribute("", "label", "label", "", this.getLabel(var2));
      this.sa.addElement(Printer.OPCODES[var1], var3);
   }

   public final void visitLabel(Label var1) {
      AttributesImpl var2 = new AttributesImpl();
      var2.addAttribute("", "name", "name", "", this.getLabel(var1));
      this.sa.addElement("Label", var2);
   }

   public final void visitLdcInsn(Object var1) {
      this.sa.addElement(Printer.OPCODES[18], getConstantAttribute(var1));
   }

   private static AttributesImpl getConstantAttribute(Object var0) {
      AttributesImpl var1 = new AttributesImpl();
      var1.addAttribute("", "cst", "cst", "", SAXClassAdapter.encode(var0.toString()));
      var1.addAttribute("", "desc", "desc", "", Type.getDescriptor(var0.getClass()));
      return var1;
   }

   public final void visitIincInsn(int var1, int var2) {
      AttributesImpl var3 = new AttributesImpl();
      var3.addAttribute("", "var", "var", "", Integer.toString(var1));
      var3.addAttribute("", "inc", "inc", "", Integer.toString(var2));
      this.sa.addElement(Printer.OPCODES[132], var3);
   }

   public final void visitTableSwitchInsn(int var1, int var2, Label var3, Label... var4) {
      AttributesImpl var5 = new AttributesImpl();
      var5.addAttribute("", "min", "min", "", Integer.toString(var1));
      var5.addAttribute("", "max", "max", "", Integer.toString(var2));
      var5.addAttribute("", "dflt", "dflt", "", this.getLabel(var3));
      String var6 = Printer.OPCODES[170];
      this.sa.addStart(var6, var5);

      for(int var7 = 0; var7 < var4.length; ++var7) {
         AttributesImpl var8 = new AttributesImpl();
         var8.addAttribute("", "name", "name", "", this.getLabel(var4[var7]));
         this.sa.addElement("label", var8);
      }

      this.sa.addEnd(var6);
   }

   public final void visitLookupSwitchInsn(Label var1, int[] var2, Label[] var3) {
      AttributesImpl var4 = new AttributesImpl();
      var4.addAttribute("", "dflt", "dflt", "", this.getLabel(var1));
      String var5 = Printer.OPCODES[171];
      this.sa.addStart(var5, var4);

      for(int var6 = 0; var6 < var3.length; ++var6) {
         AttributesImpl var7 = new AttributesImpl();
         var7.addAttribute("", "name", "name", "", this.getLabel(var3[var6]));
         var7.addAttribute("", "key", "key", "", Integer.toString(var2[var6]));
         this.sa.addElement("label", var7);
      }

      this.sa.addEnd(var5);
   }

   public final void visitMultiANewArrayInsn(String var1, int var2) {
      AttributesImpl var3 = new AttributesImpl();
      var3.addAttribute("", "desc", "desc", "", var1);
      var3.addAttribute("", "dims", "dims", "", Integer.toString(var2));
      this.sa.addElement(Printer.OPCODES[197], var3);
   }

   public final void visitTryCatchBlock(Label var1, Label var2, Label var3, String var4) {
      AttributesImpl var5 = new AttributesImpl();
      var5.addAttribute("", "start", "start", "", this.getLabel(var1));
      var5.addAttribute("", "end", "end", "", this.getLabel(var2));
      var5.addAttribute("", "handler", "handler", "", this.getLabel(var3));
      if (var4 != null) {
         var5.addAttribute("", "type", "type", "", var4);
      }

      this.sa.addElement("TryCatch", var5);
   }

   public final void visitMaxs(int var1, int var2) {
      AttributesImpl var3 = new AttributesImpl();
      var3.addAttribute("", "maxStack", "maxStack", "", Integer.toString(var1));
      var3.addAttribute("", "maxLocals", "maxLocals", "", Integer.toString(var2));
      this.sa.addElement("Max", var3);
      this.sa.addEnd("code");
   }

   public void visitLocalVariable(String var1, String var2, String var3, Label var4, Label var5, int var6) {
      AttributesImpl var7 = new AttributesImpl();
      var7.addAttribute("", "name", "name", "", var1);
      var7.addAttribute("", "desc", "desc", "", var2);
      if (var3 != null) {
         var7.addAttribute("", "signature", "signature", "", SAXClassAdapter.encode(var3));
      }

      var7.addAttribute("", "start", "start", "", this.getLabel(var4));
      var7.addAttribute("", "end", "end", "", this.getLabel(var5));
      var7.addAttribute("", "var", "var", "", Integer.toString(var6));
      this.sa.addElement("LocalVar", var7);
   }

   public final void visitLineNumber(int var1, Label var2) {
      AttributesImpl var3 = new AttributesImpl();
      var3.addAttribute("", "line", "line", "", Integer.toString(var1));
      var3.addAttribute("", "start", "start", "", this.getLabel(var2));
      this.sa.addElement("LineNumber", var3);
   }

   public AnnotationVisitor visitAnnotationDefault() {
      return new SAXAnnotationAdapter(this.sa, "annotationDefault", 0, (String)null, (String)null);
   }

   public AnnotationVisitor visitAnnotation(String var1, boolean var2) {
      return new SAXAnnotationAdapter(this.sa, "annotation", var2 ? 1 : -1, (String)null, var1);
   }

   public AnnotationVisitor visitTypeAnnotation(int var1, TypePath var2, String var3, boolean var4) {
      return new SAXAnnotationAdapter(this.sa, "typeAnnotation", var4 ? 1 : -1, (String)null, var3, var1, var2);
   }

   public AnnotationVisitor visitParameterAnnotation(int var1, String var2, boolean var3) {
      return new SAXAnnotationAdapter(this.sa, "parameterAnnotation", var3 ? 1 : -1, var1, var2);
   }

   public AnnotationVisitor visitInsnAnnotation(int var1, TypePath var2, String var3, boolean var4) {
      return new SAXAnnotationAdapter(this.sa, "insnAnnotation", var4 ? 1 : -1, (String)null, var3, var1, var2);
   }

   public AnnotationVisitor visitTryCatchAnnotation(int var1, TypePath var2, String var3, boolean var4) {
      return new SAXAnnotationAdapter(this.sa, "tryCatchAnnotation", var4 ? 1 : -1, (String)null, var3, var1, var2);
   }

   public AnnotationVisitor visitLocalVariableAnnotation(int var1, TypePath var2, Label[] var3, Label[] var4, int[] var5, String var6, boolean var7) {
      String[] var8 = new String[var3.length];
      String[] var9 = new String[var4.length];

      int var10;
      for(var10 = 0; var10 < var8.length; ++var10) {
         var8[var10] = this.getLabel(var3[var10]);
      }

      for(var10 = 0; var10 < var9.length; ++var10) {
         var9[var10] = this.getLabel(var4[var10]);
      }

      return new SAXAnnotationAdapter(this.sa, "localVariableAnnotation", var7 ? 1 : -1, (String)null, var6, var1, var2, var8, var9, var5);
   }

   public void visitEnd() {
      this.sa.addEnd("method");
   }

   private final String getLabel(Label var1) {
      String var2 = (String)this.labelNames.get(var1);
      if (var2 == null) {
         var2 = Integer.toString(this.labelNames.size());
         this.labelNames.put(var1, var2);
      }

      return var2;
   }

   static {
      _clinit_();
      TYPES = new String[]{"top", "int", "float", "double", "long", "null", "uninitializedThis"};
   }

   // $FF: synthetic method
   static void _clinit_() {
   }
}

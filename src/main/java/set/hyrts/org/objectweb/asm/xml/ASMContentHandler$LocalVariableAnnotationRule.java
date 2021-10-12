package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.AnnotationVisitor;
import set.hyrts.org.objectweb.asm.Label;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.TypePath;

final class ASMContentHandler$LocalVariableAnnotationRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$LocalVariableAnnotationRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public void begin(String var1, Attributes var2) {
      String var3 = var2.getValue("desc");
      boolean var4 = Boolean.valueOf(var2.getValue("visible"));
      int var5 = Integer.parseInt(var2.getValue("typeRef"));
      TypePath var6 = TypePath.fromString(var2.getValue("typePath"));
      String[] var7 = var2.getValue("start").split(" ");
      Label[] var8 = new Label[var7.length];

      for(int var9 = 0; var9 < var8.length; ++var9) {
         var8[var9] = this.getLabel(var7[var9]);
      }

      String[] var15 = var2.getValue("end").split(" ");
      Label[] var10 = new Label[var15.length];

      for(int var11 = 0; var11 < var10.length; ++var11) {
         var10[var11] = this.getLabel(var15[var11]);
      }

      String[] var14 = var2.getValue("index").split(" ");
      int[] var12 = new int[var14.length];

      for(int var13 = 0; var13 < var12.length; ++var13) {
         var12[var13] = Integer.parseInt(var14[var13]);
      }

      this.this$0.push(((MethodVisitor)this.this$0.peek()).visitLocalVariableAnnotation(var5, var6, var8, var10, var12, var3, var4));
   }

   public void end(String var1) {
      AnnotationVisitor var2 = (AnnotationVisitor)this.this$0.pop();
      if (var2 != null) {
         var2.visitEnd();
      }

   }
}

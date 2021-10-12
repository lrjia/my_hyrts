package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.AnnotationVisitor;
import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.FieldVisitor;
import set.hyrts.org.objectweb.asm.MethodVisitor;

final class ASMContentHandler$AnnotationRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$AnnotationRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public void begin(String var1, Attributes var2) {
      String var3 = var2.getValue("desc");
      boolean var4 = Boolean.valueOf(var2.getValue("visible"));
      Object var5 = this.this$0.peek();
      if (var5 instanceof ClassVisitor) {
         this.this$0.push(((ClassVisitor)var5).visitAnnotation(var3, var4));
      } else if (var5 instanceof FieldVisitor) {
         this.this$0.push(((FieldVisitor)var5).visitAnnotation(var3, var4));
      } else if (var5 instanceof MethodVisitor) {
         this.this$0.push(((MethodVisitor)var5).visitAnnotation(var3, var4));
      }

   }

   public void end(String var1) {
      AnnotationVisitor var2 = (AnnotationVisitor)this.this$0.pop();
      if (var2 != null) {
         var2.visitEnd();
      }

   }
}

package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.AnnotationVisitor;
import set.hyrts.org.objectweb.asm.MethodVisitor;

final class ASMContentHandler$AnnotationDefaultRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$AnnotationDefaultRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public void begin(String var1, Attributes var2) {
      MethodVisitor var3 = (MethodVisitor)this.this$0.peek();
      this.this$0.push(var3 == null ? null : var3.visitAnnotationDefault());
   }

   public void end(String var1) {
      AnnotationVisitor var2 = (AnnotationVisitor)this.this$0.pop();
      if (var2 != null) {
         var2.visitEnd();
      }

   }
}

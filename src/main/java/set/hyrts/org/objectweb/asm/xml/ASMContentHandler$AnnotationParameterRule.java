package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.AnnotationVisitor;
import set.hyrts.org.objectweb.asm.MethodVisitor;

final class ASMContentHandler$AnnotationParameterRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$AnnotationParameterRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public void begin(String var1, Attributes var2) {
      int var3 = Integer.parseInt(var2.getValue("parameter"));
      String var4 = var2.getValue("desc");
      boolean var5 = Boolean.valueOf(var2.getValue("visible"));
      this.this$0.push(((MethodVisitor)this.this$0.peek()).visitParameterAnnotation(var3, var4, var5));
   }

   public void end(String var1) {
      AnnotationVisitor var2 = (AnnotationVisitor)this.this$0.pop();
      if (var2 != null) {
         var2.visitEnd();
      }

   }
}

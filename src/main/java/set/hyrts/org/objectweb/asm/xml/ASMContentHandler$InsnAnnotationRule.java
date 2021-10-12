package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.AnnotationVisitor;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.TypePath;

final class ASMContentHandler$InsnAnnotationRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$InsnAnnotationRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public void begin(String var1, Attributes var2) {
      String var3 = var2.getValue("desc");
      boolean var4 = Boolean.valueOf(var2.getValue("visible"));
      int var5 = Integer.parseInt(var2.getValue("typeRef"));
      TypePath var6 = TypePath.fromString(var2.getValue("typePath"));
      this.this$0.push(((MethodVisitor)this.this$0.peek()).visitInsnAnnotation(var5, var6, var3, var4));
   }

   public void end(String var1) {
      AnnotationVisitor var2 = (AnnotationVisitor)this.this$0.pop();
      if (var2 != null) {
         var2.visitEnd();
      }

   }
}

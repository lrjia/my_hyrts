package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.AnnotationVisitor;
import set.hyrts.org.objectweb.asm.FieldVisitor;
import set.hyrts.org.objectweb.asm.TypePath;

public final class SAXFieldAdapter extends FieldVisitor {
   SAXAdapter sa;

   public SAXFieldAdapter(SAXAdapter var1, Attributes var2) {
      super(393216);
      this.sa = var1;
      var1.addStart("field", var2);
   }

   public AnnotationVisitor visitAnnotation(String var1, boolean var2) {
      return new SAXAnnotationAdapter(this.sa, "annotation", var2 ? 1 : -1, (String)null, var1);
   }

   public AnnotationVisitor visitTypeAnnotation(int var1, TypePath var2, String var3, boolean var4) {
      return new SAXAnnotationAdapter(this.sa, "typeAnnotation", var4 ? 1 : -1, (String)null, var3, var1, var2);
   }

   public void visitEnd() {
      this.sa.addEnd("field");
   }
}

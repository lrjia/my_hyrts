package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import set.hyrts.org.objectweb.asm.AnnotationVisitor;

final class ASMContentHandler$AnnotationValueEnumRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$AnnotationValueEnumRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public void begin(String var1, Attributes var2) {
      AnnotationVisitor var3 = (AnnotationVisitor)this.this$0.peek();
      if (var3 != null) {
         var3.visitEnum(var2.getValue("name"), var2.getValue("desc"), var2.getValue("value"));
      }

   }
}

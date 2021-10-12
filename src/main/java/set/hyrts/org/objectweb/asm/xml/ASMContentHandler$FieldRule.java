package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import set.hyrts.org.objectweb.asm.FieldVisitor;

final class ASMContentHandler$FieldRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$FieldRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) throws SAXException {
      int var3 = this.getAccess(var2.getValue("access"));
      String var4 = var2.getValue("name");
      String var5 = var2.getValue("signature");
      String var6 = var2.getValue("desc");
      Object var7 = this.getValue(var6, var2.getValue("value"));
      this.this$0.push(this.this$0.cv.visitField(var3, var4, var6, var5, var7));
   }

   public void end(String var1) {
      ((FieldVisitor)this.this$0.pop()).visitEnd();
   }
}

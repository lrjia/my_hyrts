package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

final class ASMContentHandler$InvokeDynamicBsmArgumentsRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$InvokeDynamicBsmArgumentsRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) throws SAXException {
      ArrayList var3 = (ArrayList)this.this$0.peek();
      var3.add(this.getValue(var2.getValue("desc"), var2.getValue("cst")));
   }
}

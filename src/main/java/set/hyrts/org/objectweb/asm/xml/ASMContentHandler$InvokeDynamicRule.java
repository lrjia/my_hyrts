package set.hyrts.org.objectweb.asm.xml;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import set.hyrts.org.objectweb.asm.Handle;

final class ASMContentHandler$InvokeDynamicRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$InvokeDynamicRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) throws SAXException {
      this.this$0.push(var2.getValue("name"));
      this.this$0.push(var2.getValue("desc"));
      this.this$0.push(this.decodeHandle(var2.getValue("bsm")));
      this.this$0.push(new ArrayList());
   }

   public final void end(String var1) {
      ArrayList var2 = (ArrayList)this.this$0.pop();
      Handle var3 = (Handle)this.this$0.pop();
      String var4 = (String)this.this$0.pop();
      String var5 = (String)this.this$0.pop();
      this.getCodeVisitor().visitInvokeDynamicInsn(var5, var4, var3, var2.toArray());
   }
}

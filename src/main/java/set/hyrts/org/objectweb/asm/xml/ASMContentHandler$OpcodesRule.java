package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

final class ASMContentHandler$OpcodesRule extends ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;

   ASMContentHandler$OpcodesRule(ASMContentHandler var1) {
      super(var1);
      this.this$0 = var1;
   }

   public final void begin(String var1, Attributes var2) throws SAXException {
      ASMContentHandler$Opcode var3 = (ASMContentHandler$Opcode)ASMContentHandler.OPCODES.get(var1);
      if (var3 == null) {
         throw new SAXException("Invalid element: " + var1 + " at " + this.this$0.match);
      } else {
         switch(var3.type) {
         case 0:
            this.getCodeVisitor().visitInsn(var3.opcode);
            break;
         case 1:
            this.getCodeVisitor().visitIntInsn(var3.opcode, Integer.parseInt(var2.getValue("value")));
            break;
         case 2:
            this.getCodeVisitor().visitVarInsn(var3.opcode, Integer.parseInt(var2.getValue("var")));
            break;
         case 3:
            this.getCodeVisitor().visitTypeInsn(var3.opcode, var2.getValue("desc"));
            break;
         case 4:
            this.getCodeVisitor().visitFieldInsn(var3.opcode, var2.getValue("owner"), var2.getValue("name"), var2.getValue("desc"));
            break;
         case 5:
            this.getCodeVisitor().visitMethodInsn(var3.opcode, var2.getValue("owner"), var2.getValue("name"), var2.getValue("desc"), var2.getValue("itf").equals("true"));
            break;
         case 6:
            this.getCodeVisitor().visitJumpInsn(var3.opcode, this.getLabel(var2.getValue("label")));
            break;
         case 7:
            this.getCodeVisitor().visitLdcInsn(this.getValue(var2.getValue("desc"), var2.getValue("cst")));
            break;
         case 8:
            this.getCodeVisitor().visitIincInsn(Integer.parseInt(var2.getValue("var")), Integer.parseInt(var2.getValue("inc")));
            break;
         case 9:
            this.getCodeVisitor().visitMultiANewArrayInsn(var2.getValue("desc"), Integer.parseInt(var2.getValue("dims")));
            break;
         default:
            throw new Error("Internal error");
         }

      }
   }
}

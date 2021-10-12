package set.hyrts.diff;

import java.io.PrintWriter;
import java.io.StringWriter;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.tree.ClassNode;
import set.hyrts.org.objectweb.asm.util.Printer;
import set.hyrts.org.objectweb.asm.util.Textifier;
import set.hyrts.org.objectweb.asm.util.TraceMethodVisitor;

/** @deprecated */
@Deprecated
public class MyClassNode extends ClassNode {
   public MyClassNode(int level) {
      super(level);
   }

   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
      if (mv != null) {
         Printer p = new Textifier(327680) {
            public void visitMethodEnd() {
               StringWriter sw = new StringWriter();
               PrintWriter writer = new PrintWriter(sw);
               this.print(writer);
            }
         };
         mv = new TraceMethodVisitor((MethodVisitor)mv, p);
      }

      return (MethodVisitor)mv;
   }
}

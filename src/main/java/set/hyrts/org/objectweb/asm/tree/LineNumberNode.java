package set.hyrts.org.objectweb.asm.tree;

import java.util.Map;
import set.hyrts.org.objectweb.asm.MethodVisitor;

public class LineNumberNode extends AbstractInsnNode {
   public int line;
   public LabelNode start;

   public LineNumberNode(int var1, LabelNode var2) {
      super(-1);
      this.line = var1;
      this.start = var2;
   }

   public int getType() {
      return 15;
   }

   public void accept(MethodVisitor var1) {
      var1.visitLineNumber(this.line, this.start.getLabel());
   }

   public AbstractInsnNode clone(Map var1) {
      return new LineNumberNode(this.line, clone(this.start, var1));
   }
}

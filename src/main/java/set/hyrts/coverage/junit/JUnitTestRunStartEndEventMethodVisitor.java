package set.hyrts.coverage.junit;

import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;
import set.hyrts.utils.Properties;

/** @deprecated */
@Deprecated
class JUnitTestRunStartEndEventMethodVisitor extends MethodVisitor implements Opcodes {
   String mName;

   public JUnitTestRunStartEndEventMethodVisitor(MethodVisitor mv, String name) {
      super(327680, mv);
      this.mName = name;
   }

   public void visitCode() {
      if (this.mName.equals("fireTestRunStarted")) {
         if (Properties.RTS != Properties.RTSVariant.FRTS && Properties.RTS != Properties.RTSVariant.HyRTS && Properties.RTS != Properties.RTSVariant.HyRTSf) {
            if (Properties.RTS == Properties.RTSVariant.HyRTSb) {
               this.mv.visitMethodInsn(184, "set/hyrts/rts/HybridRTSWithBlock", "main", "()V", false);
            } else if (Properties.RTS == Properties.RTSVariant.MRTS) {
               this.mv.visitMethodInsn(184, "set/hyrts/rts/MethRTS", "main", "()V", false);
            }
         } else {
            this.mv.visitMethodInsn(184, "set/hyrts/rts/HybridRTS", "main", "()V", false);
         }
      } else {
         this.mv.visitMethodInsn(184, "set/hyrts/coverage/io/TracerIO", "cleanObsoleteTestCov", "()V", false);
      }

      this.mv.visitCode();
   }
}

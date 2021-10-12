package set.hyrts.org.objectweb.asm.tree;

import set.hyrts.org.objectweb.asm.TypePath;

public class TypeAnnotationNode extends AnnotationNode {
   public int typeRef;
   public TypePath typePath;
   // $FF: synthetic field
   static Class class$org$objectweb$asm$tree$TypeAnnotationNode = class$("set.hyrts.org.objectweb.asm.tree.TypeAnnotationNode");

   public TypeAnnotationNode(int var1, TypePath var2, String var3) {
      this(393216, var1, var2, var3);
      if (this.getClass() != class$org$objectweb$asm$tree$TypeAnnotationNode) {
         throw new IllegalStateException();
      }
   }

   public TypeAnnotationNode(int var1, int var2, TypePath var3, String var4) {
      super(var1, var4);
      this.typeRef = var2;
      this.typePath = var3;
   }

   // $FF: synthetic method
   static Class class$(String var0) {
      try {
         return Class.forName(var0);
      } catch (ClassNotFoundException var2) {
         String var1 = var2.getMessage();
         throw new NoClassDefFoundError(var1);
      }
   }
}

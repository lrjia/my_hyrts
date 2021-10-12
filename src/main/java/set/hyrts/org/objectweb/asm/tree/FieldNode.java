package set.hyrts.org.objectweb.asm.tree;

import java.util.ArrayList;
import java.util.List;
import set.hyrts.org.objectweb.asm.AnnotationVisitor;
import set.hyrts.org.objectweb.asm.Attribute;
import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.FieldVisitor;
import set.hyrts.org.objectweb.asm.TypePath;

public class FieldNode extends FieldVisitor {
   public int access;
   public String name;
   public String desc;
   public String signature;
   public Object value;
   public List visibleAnnotations;
   public List invisibleAnnotations;
   public List visibleTypeAnnotations;
   public List invisibleTypeAnnotations;
   public List attrs;
   // $FF: synthetic field
   static Class class$org$objectweb$asm$tree$FieldNode = class$("set.hyrts.org.objectweb.asm.tree.FieldNode");

   public FieldNode(int var1, String var2, String var3, String var4, Object var5) {
      this(393216, var1, var2, var3, var4, var5);
      if (this.getClass() != class$org$objectweb$asm$tree$FieldNode) {
         throw new IllegalStateException();
      }
   }

   public FieldNode(int var1, int var2, String var3, String var4, String var5, Object var6) {
      super(var1);
      this.access = var2;
      this.name = var3;
      this.desc = var4;
      this.signature = var5;
      this.value = var6;
   }

   public AnnotationVisitor visitAnnotation(String var1, boolean var2) {
      AnnotationNode var3 = new AnnotationNode(var1);
      if (var2) {
         if (this.visibleAnnotations == null) {
            this.visibleAnnotations = new ArrayList(1);
         }

         this.visibleAnnotations.add(var3);
      } else {
         if (this.invisibleAnnotations == null) {
            this.invisibleAnnotations = new ArrayList(1);
         }

         this.invisibleAnnotations.add(var3);
      }

      return var3;
   }

   public AnnotationVisitor visitTypeAnnotation(int var1, TypePath var2, String var3, boolean var4) {
      TypeAnnotationNode var5 = new TypeAnnotationNode(var1, var2, var3);
      if (var4) {
         if (this.visibleTypeAnnotations == null) {
            this.visibleTypeAnnotations = new ArrayList(1);
         }

         this.visibleTypeAnnotations.add(var5);
      } else {
         if (this.invisibleTypeAnnotations == null) {
            this.invisibleTypeAnnotations = new ArrayList(1);
         }

         this.invisibleTypeAnnotations.add(var5);
      }

      return var5;
   }

   public void visitAttribute(Attribute var1) {
      if (this.attrs == null) {
         this.attrs = new ArrayList(1);
      }

      this.attrs.add(var1);
   }

   public void visitEnd() {
   }

   public void check(int var1) {
      if (var1 == 262144) {
         if (this.visibleTypeAnnotations != null && this.visibleTypeAnnotations.size() > 0) {
            throw new RuntimeException();
         }

         if (this.invisibleTypeAnnotations != null && this.invisibleTypeAnnotations.size() > 0) {
            throw new RuntimeException();
         }
      }

   }

   public void accept(ClassVisitor var1) {
      FieldVisitor var2 = var1.visitField(this.access, this.name, this.desc, this.signature, this.value);
      if (var2 != null) {
         int var4 = this.visibleAnnotations == null ? 0 : this.visibleAnnotations.size();

         int var3;
         AnnotationNode var5;
         for(var3 = 0; var3 < var4; ++var3) {
            var5 = (AnnotationNode)this.visibleAnnotations.get(var3);
            var5.accept(var2.visitAnnotation(var5.desc, true));
         }

         var4 = this.invisibleAnnotations == null ? 0 : this.invisibleAnnotations.size();

         for(var3 = 0; var3 < var4; ++var3) {
            var5 = (AnnotationNode)this.invisibleAnnotations.get(var3);
            var5.accept(var2.visitAnnotation(var5.desc, false));
         }

         var4 = this.visibleTypeAnnotations == null ? 0 : this.visibleTypeAnnotations.size();

         TypeAnnotationNode var6;
         for(var3 = 0; var3 < var4; ++var3) {
            var6 = (TypeAnnotationNode)this.visibleTypeAnnotations.get(var3);
            var6.accept(var2.visitTypeAnnotation(var6.typeRef, var6.typePath, var6.desc, true));
         }

         var4 = this.invisibleTypeAnnotations == null ? 0 : this.invisibleTypeAnnotations.size();

         for(var3 = 0; var3 < var4; ++var3) {
            var6 = (TypeAnnotationNode)this.invisibleTypeAnnotations.get(var3);
            var6.accept(var2.visitTypeAnnotation(var6.typeRef, var6.typePath, var6.desc, false));
         }

         var4 = this.attrs == null ? 0 : this.attrs.size();

         for(var3 = 0; var3 < var4; ++var3) {
            var2.visitAttribute((Attribute)this.attrs.get(var3));
         }

         var2.visitEnd();
      }
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

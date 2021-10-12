package set.hyrts.org.objectweb.asm.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import set.hyrts.org.objectweb.asm.Handle;
import set.hyrts.org.objectweb.asm.Label;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Type;

public abstract class ASMContentHandler$Rule {
   // $FF: synthetic field
   final ASMContentHandler this$0;
   // $FF: synthetic field
   static Class class$org$objectweb$asm$Type = class$("set.hyrts.org.objectweb.asm.Type");
   // $FF: synthetic field
   static Class class$org$objectweb$asm$Handle = class$("set.hyrts.org.objectweb.asm.Handle");

   protected ASMContentHandler$Rule(ASMContentHandler var1) {
      this.this$0 = var1;
   }

   public void begin(String var1, Attributes var2) throws SAXException {
   }

   public void end(String var1) {
   }

   protected final Object getValue(String var1, String var2) throws SAXException {
      Object var3 = null;
      if (var2 != null) {
         if ("Ljava/lang/String;".equals(var1)) {
            var3 = this.decode(var2);
         } else if (!"Ljava/lang/Integer;".equals(var1) && !"I".equals(var1) && !"S".equals(var1) && !"B".equals(var1) && !"C".equals(var1) && !"Z".equals(var1)) {
            if ("Ljava/lang/Short;".equals(var1)) {
               var3 = new Short(var2);
            } else if ("Ljava/lang/Byte;".equals(var1)) {
               var3 = new Byte(var2);
            } else if ("Ljava/lang/Character;".equals(var1)) {
               var3 = new Character(this.decode(var2).charAt(0));
            } else if ("Ljava/lang/Boolean;".equals(var1)) {
               var3 = Boolean.valueOf(var2);
            } else if (!"Ljava/lang/Long;".equals(var1) && !"J".equals(var1)) {
               if (!"Ljava/lang/Float;".equals(var1) && !"F".equals(var1)) {
                  if (!"Ljava/lang/Double;".equals(var1) && !"D".equals(var1)) {
                     if (Type.getDescriptor(class$org$objectweb$asm$Type).equals(var1)) {
                        var3 = Type.getType(var2);
                     } else {
                        if (!Type.getDescriptor(class$org$objectweb$asm$Handle).equals(var1)) {
                           throw new SAXException("Invalid value:" + var2 + " desc:" + var1 + " ctx:" + this);
                        }

                        var3 = this.decodeHandle(var2);
                     }
                  } else {
                     var3 = new Double(var2);
                  }
               } else {
                  var3 = new Float(var2);
               }
            } else {
               var3 = new Long(var2);
            }
         } else {
            var3 = new Integer(var2);
         }
      }

      return var3;
   }

   Handle decodeHandle(String var1) throws SAXException {
      try {
         int var2 = var1.indexOf(46);
         int var3 = var1.indexOf(40, var2 + 1);
         int var4 = var1.lastIndexOf(40);
         int var5 = var1.indexOf(32, var4 + 1);
         boolean var6 = var5 != -1;
         int var7 = Integer.parseInt(var1.substring(var4 + 1, var6 ? var1.length() - 1 : var5));
         String var8 = var1.substring(0, var2);
         String var9 = var1.substring(var2 + 1, var3);
         String var10 = var1.substring(var3, var4 - 1);
         return new Handle(var7, var8, var9, var10, var6);
      } catch (RuntimeException var11) {
         throw new SAXException("Malformed handle " + var1, var11);
      }
   }

   private final String decode(String var1) throws SAXException {
      StringBuffer var2 = new StringBuffer(var1.length());

      try {
         for(int var3 = 0; var3 < var1.length(); ++var3) {
            char var4 = var1.charAt(var3);
            if (var4 == '\\') {
               ++var3;
               var4 = var1.charAt(var3);
               if (var4 == '\\') {
                  var2.append('\\');
               } else {
                  ++var3;
                  var2.append((char)Integer.parseInt(var1.substring(var3, var3 + 4), 16));
                  var3 += 3;
               }
            } else {
               var2.append(var4);
            }
         }
      } catch (RuntimeException var5) {
         throw new SAXException(var5);
      }

      return var2.toString();
   }

   protected final Label getLabel(Object var1) {
      Label var2 = (Label)this.this$0.labels.get(var1);
      if (var2 == null) {
         var2 = new Label();
         this.this$0.labels.put(var1, var2);
      }

      return var2;
   }

   protected final MethodVisitor getCodeVisitor() {
      return (MethodVisitor)this.this$0.peek();
   }

   protected final int getAccess(String var1) {
      int var2 = 0;
      if (var1.indexOf("public") != -1) {
         var2 |= 1;
      }

      if (var1.indexOf("private") != -1) {
         var2 |= 2;
      }

      if (var1.indexOf("protected") != -1) {
         var2 |= 4;
      }

      if (var1.indexOf("static") != -1) {
         var2 |= 8;
      }

      if (var1.indexOf("final") != -1) {
         var2 |= 16;
      }

      if (var1.indexOf("super") != -1) {
         var2 |= 32;
      }

      if (var1.indexOf("synchronized") != -1) {
         var2 |= 32;
      }

      if (var1.indexOf("volatile") != -1) {
         var2 |= 64;
      }

      if (var1.indexOf("bridge") != -1) {
         var2 |= 64;
      }

      if (var1.indexOf("varargs") != -1) {
         var2 |= 128;
      }

      if (var1.indexOf("transient") != -1) {
         var2 |= 128;
      }

      if (var1.indexOf("native") != -1) {
         var2 |= 256;
      }

      if (var1.indexOf("interface") != -1) {
         var2 |= 512;
      }

      if (var1.indexOf("abstract") != -1) {
         var2 |= 1024;
      }

      if (var1.indexOf("strict") != -1) {
         var2 |= 2048;
      }

      if (var1.indexOf("synthetic") != -1) {
         var2 |= 4096;
      }

      if (var1.indexOf("annotation") != -1) {
         var2 |= 8192;
      }

      if (var1.indexOf("enum") != -1) {
         var2 |= 16384;
      }

      if (var1.indexOf("deprecated") != -1) {
         var2 |= 131072;
      }

      if (var1.indexOf("mandated") != -1) {
         var2 |= 32768;
      }

      if (var1.indexOf("module") != -1) {
         var2 |= 32768;
      }

      if (var1.indexOf("open") != -1) {
         var2 |= 32;
      }

      if (var1.indexOf("transitive") != -1) {
         var2 |= 32;
      }

      return var2;
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

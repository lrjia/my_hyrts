package set.hyrts.diff;

import java.io.PrintWriter;
import java.io.StringWriter;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.Label;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.tree.ClassNode;
import set.hyrts.org.objectweb.asm.tree.FieldNode;
import set.hyrts.org.objectweb.asm.tree.MethodNode;
import set.hyrts.org.objectweb.asm.util.Printer;
import set.hyrts.org.objectweb.asm.util.Textifier;
import set.hyrts.org.objectweb.asm.util.TraceClassVisitor;
import set.hyrts.org.objectweb.asm.util.TraceMethodVisitor;

public class ContentPrinter {
   private static Logger logger = Logger.getLogger(ContentPrinter.class);

   public static String print(MethodNode node) {
      Printer printer = new Textifier(327680) {
         public void visitLineNumber(int line, Label start) {
         }
      };
      TraceMethodVisitor methodPrinter = new TraceMethodVisitor(printer);
      node.accept((MethodVisitor)methodPrinter);
      StringWriter sw = new StringWriter();
      printer.print(new PrintWriter(sw));
      printer.getText().clear();
      String methodContent = node.access + "\n" + node.signature + "\n" + sw.toString();
      logger.debug("Method " + node.name + " content: " + methodContent);
      return methodContent;
   }

   public static String print(ClassNode node) {
      StringWriter sw = new StringWriter();
      TraceClassVisitor classPrinter = new TraceClassVisitor(new PrintWriter(sw));
      node.accept(classPrinter);
      logger.debug("Class " + node.name + " content: \n" + sw.toString());
      return sw.toString();
   }

   public static String printClassHeader(ClassNode node) {
      Printer printer = new Textifier(327680) {
         public Textifier visitField(int access, String name, String desc, String signature, Object value) {
            return new Textifier();
         }

         public Textifier visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new Textifier();
         }
      };
      StringWriter sw = new StringWriter();
      TraceClassVisitor classPrinter = new TraceClassVisitor((ClassVisitor)null, printer, new PrintWriter(sw));
      node.accept(classPrinter);
      return sw.toString();
   }

   public static String print(FieldNode node) {
      return node.value == null ? null : node.value.toString();
   }
}

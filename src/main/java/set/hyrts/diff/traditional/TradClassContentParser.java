package set.hyrts.diff.traditional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import set.hyrts.diff.ClassFileHandler;
import set.hyrts.diff.ClassInheritanceGraph;
import set.hyrts.diff.ContentPrinter;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.tree.ClassNode;
import set.hyrts.org.objectweb.asm.tree.FieldNode;
import set.hyrts.org.objectweb.asm.tree.MethodNode;
import set.hyrts.utils.Properties;

public class TradClassContentParser {
   static Map<String, String> classResource = new HashMap();
   private static Logger logger = Logger.getLogger(TradClassContentParser.class);

   public static void parseAndSerializeNewContents(Set<ClassFileHandler> cFiles, String dir) throws IOException {
      classResource.clear();
      String ftracerDir = getFTracerDir(dir);
      String fileName = Properties.FILE_CHECKSUM + ".gz";
      ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(ftracerDir + File.separator + fileName)));
      oos.writeInt(cFiles.size());
      Iterator var5 = cFiles.iterator();

      while(var5.hasNext()) {
         ClassFileHandler cFile = (ClassFileHandler)var5.next();
         InputStream is = cFile.getInputStream();
         ClassNode node = new ClassNode(327680);
         ClassReader cr = new ClassReader(is);
         cr.accept(node, 2);
         String className = node.name;
         String superClass = node.superName == null ? "" : node.superName;
         oos.writeUTF(className);
         String headContent = controlledHash(ContentPrinter.printClassHeader(node));
         Map<String, String> methodMap = new HashMap();
         List<MethodNode> methods = node.methods;
         Iterator var15 = methods.iterator();

         while(var15.hasNext()) {
            MethodNode method = (MethodNode)var15.next();
            String methodId = methodName(method);
            String methodContent = controlledHash(ContentPrinter.print(method));
            int tag = (method.access & 8) == 0 ? 0 : 1;
            methodMap.put(methodId, tag + methodContent);
         }

         classResource.put(className, cFile.filePath);
         TradMethVersionDiff.newClassMeths.put(className, methodMap);
         TradMethVersionDiff.newClassHeaders.put(className, headContent);
         oos.writeUTF(superClass);
         encodeWriteLargeString(oos, headContent);
         oos.writeInt(methodMap.size());
         var15 = methodMap.keySet().iterator();

         while(var15.hasNext()) {
            String meth = (String)var15.next();
            oos.writeUTF(meth);
            encodeWriteLargeString(oos, (String)methodMap.get(meth));
         }

         is.close();
      }

      oos.flush();
      oos.close();
   }

   public static void deserializeOldContents(String dir) throws IOException {
      String ftracerDir = getFTracerDir(dir);
      String fileName = Properties.FILE_CHECKSUM + ".gz";
      File f = new File(ftracerDir + File.separator + fileName);
      if (f.exists()) {
         ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(f)));
         int classNum = ois.readInt();

         for(int i = 0; i < classNum; ++i) {
            String clazz = ois.readUTF();
            String superClass = ois.readUTF();
            String headContent = decodeReadLargeString(ois);
            Map<String, String> methContent = new HashMap();
            int methNum = ois.readInt();

            for(int j = 0; j < methNum; ++j) {
               String meth = ois.readUTF();
               String mContent = decodeReadLargeString(ois);
               methContent.put(meth, mContent);
            }

            TradMethVersionDiff.oldClassMeths.put(clazz, methContent);
            TradMethVersionDiff.oldClassHeaders.put(clazz, headContent);
            if (!superClass.equals("java/lang/Object")) {
               if (!ClassInheritanceGraph.inheritanceMap.containsKey(superClass)) {
                  ClassInheritanceGraph.inheritanceMap.put(superClass, new HashSet());
               }

               ((Set)ClassInheritanceGraph.inheritanceMap.get(superClass)).add(clazz);
            }
         }

         ois.close();
      }
   }

   public static String controlledHash(String s) throws IOException {
      return s;
   }

   public static void encodeWriteLargeString(ObjectOutputStream out, String str) throws IOException {
      byte[] data = str.getBytes("UTF-8");
      out.writeInt(data.length);
      out.write(data);
   }

   public static String decodeReadLargeString(ObjectInputStream in) throws IOException {
      int length = in.readInt();
      byte[] data = new byte[length];
      in.readFully(data);
      String str = new String(data, "UTF-8");
      return str;
   }

   public static String methodName(MethodNode method) {
      return method.name + ":" + method.desc;
   }

   public static String fieldName(FieldNode field) {
      return field.name + ":" + field.desc;
   }

   public static String getFTracerDir(String dir) {
      String rootDirName;
      if (dir.length() > 0) {
         rootDirName = dir + File.separator + "hyrts-files";
      } else {
         rootDirName = "hyrts-files";
      }

      File rootDir = new File(rootDirName);
      rootDir.mkdir();
      return rootDirName;
   }
}

package set.hyrts.diff;

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
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.tree.ClassNode;
import set.hyrts.org.objectweb.asm.tree.FieldNode;
import set.hyrts.org.objectweb.asm.tree.MethodNode;
import set.hyrts.utils.Properties;

public class ClassContentParser {
   static Map<String, String> classResource = new HashMap();
   private static Logger logger = Logger.getLogger(ClassContentParser.class);

   public static void parseAndSerializeNewContents(Set<ClassFileHandler> cFiles, String dir) throws IOException {
      classResource.clear();
      String ftracerDir = getFTracerDir(dir);
      String fileName = Properties.FILE_CHECKSUM + ".gz";
      ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(ftracerDir + File.separator + fileName)));
      oos.writeInt(cFiles.size());
      Iterator var5 = cFiles.iterator();

      while(true) {
         while(var5.hasNext()) {
            ClassFileHandler cFile = (ClassFileHandler)var5.next();
            InputStream is = cFile.getInputStream();
            ClassNode node = new ClassNode(327680);
            ClassReader cr = new ClassReader(is);
            cr.accept(node, 2);
            String className = node.name;
            String superClass = node.superName == null ? "" : node.superName;
            String checkSum = CheckSum.compute(ContentPrinter.print(node));
            oos.writeUTF(className);
            oos.writeUTF(checkSum);
            if (Properties.TRACER_COV_TYPE.endsWith("class-cov")) {
               VersionDiff.newClasses.put(className, checkSum);
            } else {
               String headCheckSum = "";
               Map<String, String> methodMap = new HashMap();
               if (checkSum.equals(VersionDiff.oldClasses.get(className))) {
                  headCheckSum = (String)VersionDiff.oldClassHeaders.get(className);
                  methodMap = (Map)VersionDiff.oldClassMeths.get(className);
                  VersionDiff.oldClasses.remove(className);
                  VersionDiff.oldClassHeaders.remove(className);
               } else {
                  headCheckSum = CheckSum.compute(ContentPrinter.printClassHeader(node));
                  List<MethodNode> methods = node.methods;
                  Iterator var16 = methods.iterator();

                  while(var16.hasNext()) {
                     MethodNode method = (MethodNode)var16.next();
                     String methodId = methodName(method);
                     String methodContent = ContentPrinter.print(method);
                     int tag = (method.access & 8) == 0 ? 0 : 1;
                     ((Map)methodMap).put(methodId, tag + CheckSum.compute(methodContent));
                  }

                  classResource.put(className, cFile.filePath);
                  VersionDiff.newClasses.put(className, checkSum);
                  VersionDiff.newClassMeths.put(className, methodMap);
                  VersionDiff.newClassHeaders.put(className, headCheckSum);
               }

               oos.writeUTF(superClass);
               oos.writeUTF(headCheckSum);
               oos.writeInt(((Map)methodMap).size());
               Iterator var21 = ((Map)methodMap).keySet().iterator();

               while(var21.hasNext()) {
                  String meth = (String)var21.next();
                  oos.writeUTF(meth);
                  oos.writeUTF((String)((Map)methodMap).get(meth));
               }

               is.close();
            }
         }

         oos.flush();
         oos.close();
         return;
      }
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
            String checkSum = ois.readUTF();
            VersionDiff.oldClasses.put(clazz, checkSum);
            if (!Properties.TRACER_COV_TYPE.endsWith("class-cov")) {
               String superClass = ois.readUTF();
               String headCheckSum = ois.readUTF();
               Map<String, String> methCheckSum = new HashMap();
               int methNum = ois.readInt();

               for(int j = 0; j < methNum; ++j) {
                  String meth = ois.readUTF();
                  String mCheckSum = ois.readUTF();
                  methCheckSum.put(meth, mCheckSum);
               }

               VersionDiff.oldClasses.put(clazz, checkSum);
               VersionDiff.oldClassMeths.put(clazz, methCheckSum);
               VersionDiff.oldClassHeaders.put(clazz, headCheckSum);
               if ((!VersionDiff.transDIM || !VersionDiff.transAIM) && !superClass.equals("java/lang/Object")) {
                  if (!ClassInheritanceGraph.inheritanceMap.containsKey(superClass)) {
                     ClassInheritanceGraph.inheritanceMap.put(superClass, new HashSet());
                  }

                  ((Set)ClassInheritanceGraph.inheritanceMap.get(superClass)).add(clazz);
               }
            }
         }

         ois.close();
      }
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

package set.hyrts.diff;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;

public class ClassFileHandler {
   public boolean isJar = false;
   public String filePath;
   public String className;
   public JarFile jarFile;

   public ClassFileHandler(String className, String filePath) throws IOException {
      this.isJar = false;
      this.className = className;
      this.filePath = filePath;
   }

   public ClassFileHandler(String className, JarFile jar) throws IOException {
      this.isJar = true;
      this.className = className;
      this.jarFile = jar;
   }

   public InputStream getInputStream() throws IOException {
      InputStream is = null;
      if (this.isJar) {
         is = this.jarFile.getInputStream(this.jarFile.getEntry(this.className + ".class"));
      } else {
         is = new FileInputStream(this.filePath);
      }

      return (InputStream)is;
   }
}

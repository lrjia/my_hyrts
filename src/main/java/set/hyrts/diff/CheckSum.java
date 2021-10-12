package set.hyrts.diff;

import java.io.IOException;
import java.io.InputStream;
import set.hyrts.org.apache.commons.codec.digest.DigestUtils;

public class CheckSum {
   public static String compute(InputStream classFileInputStream) throws IOException {
      return DigestUtils.md5Hex(classFileInputStream);
   }

   public static String compute(String content) throws IOException {
      return DigestUtils.md5Hex(content);
   }

   public static String compute(StringBuilder content) throws IOException {
      return DigestUtils.md5Hex(content.toString());
   }
}

package set.hyrts.diff;

import set.hyrts.org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

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

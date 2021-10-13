package set.hyrts.utils;

import set.hyrts.org.apache.commons.lang3.StringUtils;

public class Classes {
    public static String toDotClassName(String name) {
        return StringUtils.replace(name, "/", ".");
    }

    public static String toSlashClassName(String name) {
        return StringUtils.replace(name, ".", "/");
    }

    public static String descToClassName(String str) {
        if (str.endsWith(";")) {
            int i = str.indexOf("L");
            str = str.substring(i + 1, str.length() - 1);
        }

        return str;
    }

    public static boolean isPrimitive(String str) {
        return !str.endsWith(";");
    }
}

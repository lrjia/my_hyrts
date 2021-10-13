package set.hyrts.coverage.agent;

import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;

public class ComputeClassWriter extends ClassWriter {
    private ClassLoader l = this.getClass().getClassLoader();

    public ComputeClassWriter(int flags) {
        super(flags);
    }

    protected String getCommonSuperClass(String type1, String type2) {
        try {
            ClassReader info1;
            try {
                info1 = this.typeInfo(type1);
            } catch (NullPointerException var15) {
                throw new RuntimeException("Class not found: " + type1 + ": " + var15.toString(), var15);
            }

            ClassReader info2;
            try {
                info2 = this.typeInfo(type2);
            } catch (NullPointerException var14) {
                throw new RuntimeException("Class not found: " + type2 + ": " + var14.toString(), var14);
            }

            if ((info1.getAccess() & 512) != 0) {
                if (this.typeImplements(type2, info2, type1)) {
                    return type1;
                } else {
                    return (info2.getAccess() & 512) != 0 && this.typeImplements(type1, info1, type2) ? type2 : "java/lang/Object";
                }
            } else if ((info2.getAccess() & 512) != 0) {
                return this.typeImplements(type1, info1, type2) ? type2 : "java/lang/Object";
            } else {
                StringBuilder b1 = this.typeAncestors(type1, info1);
                StringBuilder b2 = this.typeAncestors(type2, info2);
                String result = "java/lang/Object";
                int end1 = b1.length();
                int end2 = b2.length();

                while (true) {
                    int start1 = b1.lastIndexOf(";", end1 - 1);
                    int start2 = b2.lastIndexOf(";", end2 - 1);
                    if (start1 == -1 || start2 == -1 || end1 - start1 != end2 - start2) {
                        return result;
                    }

                    String p1 = b1.substring(start1 + 1, end1);
                    String p2 = b2.substring(start2 + 1, end2);
                    if (!p1.equals(p2)) {
                        return result;
                    }

                    result = p1;
                    end1 = start1;
                    end2 = start2;
                }
            }
        } catch (IOException var16) {
            throw new RuntimeException(var16.toString());
        } catch (NullPointerException var17) {
            throw new RuntimeException(var17.toString());
        }
    }

    private StringBuilder typeAncestors(String type, ClassReader info) throws IOException {
        StringBuilder b;
        for (b = new StringBuilder(); !"java/lang/Object".equals(type); info = this.typeInfo(type)) {
            b.append(';').append(type);
            type = info.getSuperName();
        }

        return b;
    }

    private boolean typeImplements(String type, ClassReader info, String itf) throws IOException {
        while (!"java/lang/Object".equals(type)) {
            String[] itfs = info.getInterfaces();

            int i;
            for (i = 0; i < itfs.length; ++i) {
                if (itfs[i].equals(itf)) {
                    return true;
                }
            }

            for (i = 0; i < itfs.length; ++i) {
                if (this.typeImplements(itfs[i], this.typeInfo(itfs[i]), itf)) {
                    return true;
                }
            }

            type = info.getSuperName();
            info = this.typeInfo(type);
        }

        return false;
    }

    private ClassReader typeInfo(String type) throws IOException, NullPointerException {
        InputStream is = this.l.getResourceAsStream(type + ".class");

        ClassReader var3;
        try {
            if (is == null) {
                throw new NullPointerException("Class not found " + type);
            }

            var3 = new ClassReader(is);
        } finally {
            if (is != null) {
                is.close();
            }

        }

        return var3;
    }
}

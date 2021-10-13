package set.hyrts.diff;

import java.util.*;

public class ClassInheritanceGraph {
    public static Map<String, Set<String>> inheritanceMap = new HashMap();

    public static void clear() {
        inheritanceMap.clear();
    }

    public static Set<String> findLookUpMethods(String clazz, String method, Map<String, Map<String, String>> oldClassMeths) {
        Set<String> res = new HashSet();
        List<String> workList = new ArrayList();
        workList.add(clazz);
        res.add(clazz);

        while (true) {
            String curClazz;
            do {
                if (workList.isEmpty()) {
                    return res;
                }

                curClazz = (String) workList.remove(0);
            } while (!inheritanceMap.containsKey(curClazz));

            Iterator var6 = ((Set) inheritanceMap.get(curClazz)).iterator();

            while (var6.hasNext()) {
                String subClass = (String) var6.next();
                if (!((Map) oldClassMeths.get(subClass)).containsKey(method)) {
                    res.add(subClass);
                    workList.add(subClass);
                }
            }
        }
    }
}

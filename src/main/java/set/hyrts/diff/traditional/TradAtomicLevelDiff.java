package set.hyrts.diff.traditional;

import set.hyrts.diff.ClassInheritanceGraph;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TradAtomicLevelDiff {
    public static void getAllDels(String clazz) {
        Map<String, String> oldMethodMap = (Map) TradMethVersionDiff.oldClassMeths.get(clazz);
        Iterator var2 = oldMethodMap.keySet().iterator();

        while (true) {
            while (var2.hasNext()) {
                String method = (String) var2.next();
                int staticTag = ((String) oldMethodMap.get(method)).charAt(0) - 48;
                if (method.startsWith("<init>")) {
                    TradMethVersionDiff.DIs.add(clazz + ":" + method);
                } else if (method.startsWith("<clinit>")) {
                    TradMethVersionDiff.DSIs.add(clazz + ":" + method);
                } else if (staticTag == 1) {
                    TradMethVersionDiff.DSMs.add(clazz + ":" + method);
                } else {
                    TradMethVersionDiff.DIMs.add(clazz + ":" + method);
                    Set<String> impactedClasses = ClassInheritanceGraph.findLookUpMethods(clazz, method, TradMethVersionDiff.oldClassMeths);
                    Iterator var6 = impactedClasses.iterator();

                    while (var6.hasNext()) {
                        String impactedClass = (String) var6.next();
                        TradMethVersionDiff.LCs.add(impactedClass + ":" + method);
                    }
                }
            }

            return;
        }
    }

    public static void getAllAdds(String clazz) {
        Map<String, String> newMethodMap = (Map) TradMethVersionDiff.newClassMeths.get(clazz);
        Iterator var2 = newMethodMap.keySet().iterator();

        while (true) {
            while (var2.hasNext()) {
                String method = (String) var2.next();
                int staticTag = ((String) newMethodMap.get(method)).charAt(0) - 48;
                if (method.startsWith("<init>")) {
                    TradMethVersionDiff.AIs.add(clazz + ":" + method);
                } else if (method.startsWith("<clinit>")) {
                    TradMethVersionDiff.ASIs.add(clazz + ":" + method);
                } else if (staticTag == 1) {
                    TradMethVersionDiff.ASMs.add(clazz + ":" + method);
                } else {
                    TradMethVersionDiff.AIMs.add(clazz + ":" + method);
                    Set<String> impactedClasses = ClassInheritanceGraph.findLookUpMethods(clazz, method, TradMethVersionDiff.oldClassMeths);
                    Iterator var6 = impactedClasses.iterator();

                    while (var6.hasNext()) {
                        String impactedClass = (String) var6.next();
                        TradMethVersionDiff.LCs.add(impactedClass + ":" + method);
                    }
                }
            }

            return;
        }
    }

    public static boolean diff(String clazz) throws IOException {
        boolean changed = false;
        if (!((String) TradMethVersionDiff.oldClassHeaders.get(clazz)).equals(TradMethVersionDiff.newClassHeaders.get(clazz))) {
            getAllDels(clazz);
            changed = true;
            return changed;
        } else {
            Map<String, String> oldMethodMap = (Map) TradMethVersionDiff.oldClassMeths.get(clazz);
            Map<String, String> newMethodMap = (Map) TradMethVersionDiff.newClassMeths.get(clazz);
            Set<String> DI_toAdd = new HashSet();
            Set<String> DSI_toAdd = new HashSet();
            Set<String> DIM_toAdd = new HashSet();
            Set<String> DSM_toAdd = new HashSet();
            Set<String> AI_toAdd = new HashSet();
            Set<String> ASI_toAdd = new HashSet();
            Set<String> AIM_toAdd = new HashSet();
            Set<String> ASM_toAdd = new HashSet();
            Set<String> CI_toAdd = new HashSet();
            Set<String> CSI_toAdd = new HashSet();
            Set<String> CIM_toAdd = new HashSet();
            Set<String> CSM_toAdd = new HashSet();
            Set<String> LC_toAdd = new HashSet();

            Iterator var17;
            String method;
            int staticTag;
            Set impactedClasses;
            Iterator var21;
            String impactedClass;
            for (var17 = oldMethodMap.keySet().iterator(); var17.hasNext(); newMethodMap.remove(method)) {
                method = (String) var17.next();
                staticTag = ((String) oldMethodMap.get(method)).charAt(0) - 48;
                if (newMethodMap.containsKey(method)) {
                    if (!((String) oldMethodMap.get(method)).equals(newMethodMap.get(method))) {
                        if (method.startsWith("<init>")) {
                            CI_toAdd.add(clazz + ":" + method);
                        } else if (method.startsWith("<clinit>")) {
                            CSI_toAdd.add(clazz + ":" + method);
                        } else if (staticTag == 1) {
                            CSM_toAdd.add(clazz + ":" + method);
                        } else {
                            CIM_toAdd.add(clazz + ":" + method);
                        }

                        changed = true;
                    }
                } else {
                    if (method.startsWith("<init>")) {
                        DI_toAdd.add(clazz + ":" + method);
                    } else if (method.startsWith("<clinit>")) {
                        DSI_toAdd.add(clazz + ":" + method);
                    } else if (staticTag == 1) {
                        DSM_toAdd.add(clazz + ":" + method);
                    } else {
                        DIM_toAdd.add(clazz + ":" + method);
                        impactedClasses = ClassInheritanceGraph.findLookUpMethods(clazz, method, TradMethVersionDiff.oldClassMeths);
                        var21 = impactedClasses.iterator();

                        while (var21.hasNext()) {
                            impactedClass = (String) var21.next();
                            LC_toAdd.add(impactedClass + ":" + method);
                        }
                    }

                    changed = true;
                }
            }

            for (var17 = newMethodMap.keySet().iterator(); var17.hasNext(); changed = true) {
                method = (String) var17.next();
                staticTag = ((String) newMethodMap.get(method)).charAt(0) - 48;
                if (method.startsWith("<init>")) {
                    AI_toAdd.add(clazz + ":" + method);
                } else if (method.startsWith("<clinit>")) {
                    ASI_toAdd.add(clazz + ":" + method);
                } else if (staticTag == 1) {
                    ASM_toAdd.add(clazz + ":" + method);
                } else {
                    AIM_toAdd.add(clazz + ":" + method);
                    impactedClasses = ClassInheritanceGraph.findLookUpMethods(clazz, method, TradMethVersionDiff.oldClassMeths);
                    var21 = impactedClasses.iterator();

                    while (var21.hasNext()) {
                        impactedClass = (String) var21.next();
                        LC_toAdd.add(impactedClass + ":" + method);
                    }
                }
            }

            TradMethVersionDiff.AIs.addAll(AI_toAdd);
            TradMethVersionDiff.ASIs.addAll(ASI_toAdd);
            TradMethVersionDiff.AIMs.addAll(AIM_toAdd);
            TradMethVersionDiff.ASMs.addAll(ASM_toAdd);
            TradMethVersionDiff.DIs.addAll(DI_toAdd);
            TradMethVersionDiff.DSIs.addAll(DSI_toAdd);
            TradMethVersionDiff.DIMs.addAll(DIM_toAdd);
            TradMethVersionDiff.DSMs.addAll(DSM_toAdd);
            TradMethVersionDiff.CIs.addAll(CI_toAdd);
            TradMethVersionDiff.CSIs.addAll(CSI_toAdd);
            TradMethVersionDiff.CIMs.addAll(CIM_toAdd);
            TradMethVersionDiff.CSMs.addAll(CSM_toAdd);
            TradMethVersionDiff.LCs.addAll(LC_toAdd);
            return changed;
        }
    }
}

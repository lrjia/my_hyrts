package set.hyrts.diff;

import set.hyrts.utils.Properties;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AtomicLevelDiff {
    public static void diff(String clazz) throws IOException {
        if (!((String) VersionDiff.oldClassHeaders.get(clazz)).equals(VersionDiff.newClassHeaders.get(clazz))) {
            VersionDiff.classHeaderChanges.add(clazz);
        } else {
            Map<String, String> oldMethodMap = (Map) VersionDiff.oldClassMeths.get(clazz);
            Map<String, String> newMethodMap = (Map) VersionDiff.newClassMeths.get(clazz);
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

            Iterator var16;
            String method;
            int staticTag;
            String impactedClass;
            Set impactedClasses;
            Iterator var23;
            for (var16 = oldMethodMap.keySet().iterator(); var16.hasNext(); newMethodMap.remove(method)) {
                method = (String) var16.next();
                staticTag = ((String) oldMethodMap.get(method)).charAt(0) - 48;
                if (!newMethodMap.containsKey(method)) {
                    if (method.startsWith("<init>")) {
                        if (VersionDiff.transDI) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        DI_toAdd.add(clazz + ":" + method);
                    } else if (method.startsWith("<clinit>")) {
                        if (VersionDiff.transDSI) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        DSI_toAdd.add(clazz + ":" + method);
                    } else if (staticTag == 1) {
                        if (VersionDiff.transDSM) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        DSM_toAdd.add(clazz + ":" + method);
                    } else {
                        if (VersionDiff.transDIM) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        DIM_toAdd.add(clazz + ":" + method);
                        impactedClasses = ClassInheritanceGraph.findLookUpMethods(clazz, method, VersionDiff.oldClassMeths);
                        var23 = impactedClasses.iterator();

                        while (var23.hasNext()) {
                            impactedClass = (String) var23.next();
                            LC_toAdd.add(impactedClass + ":" + method);
                        }
                    }
                } else if (!((String) oldMethodMap.get(method)).equals(newMethodMap.get(method))) {
                    if (method.startsWith("<init>")) {
                        if (VersionDiff.transCI) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        CI_toAdd.add(clazz + ":" + method);
                    } else if (method.startsWith("<clinit>")) {
                        if (VersionDiff.transCSI) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        CSI_toAdd.add(clazz + ":" + method);
                    } else if (staticTag == 1) {
                        if (VersionDiff.transCSM) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        CSM_toAdd.add(clazz + ":" + method);
                    } else {
                        if (VersionDiff.transCIM) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        CIM_toAdd.add(clazz + ":" + method);
                    }

                    if (Properties.TRACER_COV_TYPE.endsWith("block-cov")) {
                        String newPath = (String) ClassContentParser.classResource.get(clazz);
                        String oldPath = VersionDiff.OLDDIR + ((String) ClassContentParser.classResource.get(clazz)).substring(VersionDiff.OLDDIR.length());
                        VersionDiff.BLKs.addAll((new CFGDiff()).diff(oldPath, newPath, method));
                    }
                }
            }

            var16 = newMethodMap.keySet().iterator();

            while (true) {
                while (var16.hasNext()) {
                    method = (String) var16.next();
                    staticTag = ((String) newMethodMap.get(method)).charAt(0) - 48;
                    if (method.startsWith("<init>")) {
                        if (VersionDiff.transAI) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        AI_toAdd.add(clazz + ":" + method);
                    } else if (method.startsWith("<clinit>")) {
                        if (VersionDiff.transASI) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        ASI_toAdd.add(clazz + ":" + method);
                    } else if (staticTag == 1) {
                        if (VersionDiff.transASM) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        ASM_toAdd.add(clazz + ":" + method);
                    } else {
                        if (VersionDiff.transAIM) {
                            VersionDiff.transformedClassChanges.add(clazz);
                            return;
                        }

                        AIM_toAdd.add(clazz + ":" + method);
                        impactedClasses = ClassInheritanceGraph.findLookUpMethods(clazz, method, VersionDiff.oldClassMeths);
                        var23 = impactedClasses.iterator();

                        while (var23.hasNext()) {
                            impactedClass = (String) var23.next();
                            LC_toAdd.add(impactedClass + ":" + method);
                        }
                    }
                }

                VersionDiff.AIs.addAll(AI_toAdd);
                VersionDiff.ASIs.addAll(ASI_toAdd);
                VersionDiff.AIMs.addAll(AIM_toAdd);
                VersionDiff.ASMs.addAll(ASM_toAdd);
                VersionDiff.DIs.addAll(DI_toAdd);
                VersionDiff.DSIs.addAll(DSI_toAdd);
                VersionDiff.DIMs.addAll(DIM_toAdd);
                VersionDiff.DSMs.addAll(DSM_toAdd);
                VersionDiff.CIs.addAll(CI_toAdd);
                VersionDiff.CSIs.addAll(CSI_toAdd);
                VersionDiff.CIMs.addAll(CIM_toAdd);
                VersionDiff.CSMs.addAll(CSM_toAdd);
                VersionDiff.LCs.addAll(LC_toAdd);
                return;
            }
        }
    }
}

package set.hyrts.coverage.core;

import set.hyrts.org.apache.log4j.Logger;

import java.util.BitSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CoverageData {
    public static final String TRACER = "set/hyrts/coverage/core/Tracer";
    public static final int MAX_CLASSNUM = 100000;
    public static final String CLINIT = "<clinit>:()V";
    public static Logger logger = Logger.getLogger(CoverageData.class);
    public static int classId = 0;
    public static ConcurrentMap<String, Integer> classIdMap = new ConcurrentHashMap();
    public static ConcurrentMap<String, Integer> dotClassIdMap = new ConcurrentHashMap();
    public static ConcurrentMap<Integer, String> idClassMap = new ConcurrentHashMap();
    public static ConcurrentMap<Integer, ConcurrentMap<Integer, String>> idMethMap = new ConcurrentHashMap();
    public static ConcurrentMap<String, Integer> fieldTypeCache = new ConcurrentHashMap();
    public static ConcurrentMap<String, Integer> stringTypeCache = new ConcurrentHashMap();
    public static ConcurrentMap<Class, Integer> classTypeCache = new ConcurrentHashMap();
    public static boolean[][] methCovArray = new boolean[100000][];
    public static boolean[] classCovArray = new boolean[100000];
    public static BitSet[] stmtCovSet = new BitSet[100000];
    public static BitSet[][] blockCovSet = new BitSet[100000][];
    public static ConcurrentMap<String, Integer> branchCov = new ConcurrentHashMap();
    public static ConcurrentMap<String, Set<String>> rtType = new ConcurrentHashMap();

    public static int registerClass(String slashClazz, String dotClazz) {
        int id = nextId();
        classIdMap.put(slashClazz, id);
        dotClassIdMap.put(dotClazz, id);
        idClassMap.put(id, slashClazz);
        return id;
    }

    public static int registerMeth(int clazzId, String meth) {
        if (!idMethMap.containsKey(clazzId)) {
            ConcurrentMap<Integer, String> map = new ConcurrentHashMap();
            map.put(0, "<clinit>:()V");
            idMethMap.put(clazzId, map);
        }

        if (meth.equals("<clinit>:()V")) {
            return 0;
        } else {
            int id = ((ConcurrentMap) idMethMap.get(clazzId)).size();
            ((ConcurrentMap) idMethMap.get(clazzId)).put(id, meth);
            return id;
        }
    }

    private static synchronized int nextId() {
        return classId++;
    }

    public static int decodeClassId(long value) {
        return (int) (value >> 32);
    }

    public static int decodeMethodId(long value) {
        return (int) (value & -1L);
    }
}

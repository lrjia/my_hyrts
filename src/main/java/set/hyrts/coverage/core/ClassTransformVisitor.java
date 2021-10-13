package set.hyrts.coverage.core;

import set.hyrts.org.objectweb.asm.ClassVisitor;
import set.hyrts.org.objectweb.asm.MethodVisitor;
import set.hyrts.org.objectweb.asm.Opcodes;
import set.hyrts.utils.Properties;

import java.util.BitSet;
import java.util.concurrent.ConcurrentMap;

public class ClassTransformVisitor extends ClassVisitor implements Opcodes {
    int clazzId;
    String slashClazzName;
    String dotClazzName;

    public ClassTransformVisitor(int clazzId, String clazzName, String dotClassName, ClassVisitor cv) {
        super(327680, cv);
        this.clazzId = clazzId;
        this.slashClazzName = clazzName;
        this.dotClazzName = dotClassName;
        CoverageData.registerMeth(clazzId, "<clinit>:()V");
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        String methName = name + ":" + desc;
        int methId = CoverageData.registerMeth(this.clazzId, methName);
        boolean isInit = name.startsWith("<init>");
        MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
        if (Properties.TRACER_COV_TYPE.endsWith("stmt-cov")) {
            return mv == null ? null : new MethodVisitorStmtCov(mv, this.clazzId, methId, isInit, access);
        } else if (Properties.TRACER_COV_TYPE.endsWith("branch-cov")) {
            return mv == null ? null : new MethodVisitorBranchCov(mv, this.clazzId, methId, this.slashClazzName, methName, isInit, access);
        } else if (Properties.TRACER_COV_TYPE.endsWith("meth-cov")) {
            return mv == null ? null : new MethodVisitorMethCov(mv, this.clazzId, methId, this.slashClazzName, this.dotClazzName, methName, isInit, access);
        } else if (Properties.TRACER_COV_TYPE.endsWith("class-cov")) {
            return mv == null ? null : new MethodVisitorClassCov(mv, this.clazzId, methId, this.slashClazzName, this.dotClazzName, methName, isInit, access);
        } else if (Properties.TRACER_COV_TYPE.endsWith("block-cov")) {
            return mv == null ? null : new MethodVisitorBlockCov(mv, this.clazzId, methId, this.slashClazzName, this.dotClazzName, methName, isInit, access);
        } else {
            return mv;
        }
    }

    public void visitEnd() {
        super.visitEnd();
        int methNum = 1;
        if (Properties.TRACER_COV_TYPE.endsWith("meth-cov")) {
            if (CoverageData.idMethMap.containsKey(this.clazzId)) {
                methNum = ((ConcurrentMap) CoverageData.idMethMap.get(this.clazzId)).size();
            }

            CoverageData.methCovArray[this.clazzId] = new boolean[methNum];
        } else if (Properties.TRACER_COV_TYPE.endsWith("block-cov")) {
            if (CoverageData.idMethMap.containsKey(this.clazzId)) {
                methNum = ((ConcurrentMap) CoverageData.idMethMap.get(this.clazzId)).size();
            }

            CoverageData.methCovArray[this.clazzId] = new boolean[methNum];
            CoverageData.blockCovSet[this.clazzId] = new BitSet[methNum];

            for (int i = 0; i < CoverageData.blockCovSet[this.clazzId].length; ++i) {
                CoverageData.blockCovSet[this.clazzId][i] = new BitSet();
            }
        } else if (Properties.TRACER_COV_TYPE.endsWith("stmt-cov")) {
            CoverageData.stmtCovSet[this.clazzId] = new BitSet();
        }

    }
}

package set.hyrts.diff;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import set.hyrts.org.apache.commons.lang3.StringUtils;
import set.hyrts.org.apache.log4j.Logger;
import set.hyrts.org.objectweb.asm.ClassReader;
import set.hyrts.org.objectweb.asm.tree.AbstractInsnNode;
import set.hyrts.org.objectweb.asm.tree.ClassNode;
import set.hyrts.org.objectweb.asm.tree.FieldInsnNode;
import set.hyrts.org.objectweb.asm.tree.FrameNode;
import set.hyrts.org.objectweb.asm.tree.IincInsnNode;
import set.hyrts.org.objectweb.asm.tree.InsnList;
import set.hyrts.org.objectweb.asm.tree.IntInsnNode;
import set.hyrts.org.objectweb.asm.tree.JumpInsnNode;
import set.hyrts.org.objectweb.asm.tree.LabelNode;
import set.hyrts.org.objectweb.asm.tree.LdcInsnNode;
import set.hyrts.org.objectweb.asm.tree.LookupSwitchInsnNode;
import set.hyrts.org.objectweb.asm.tree.MethodInsnNode;
import set.hyrts.org.objectweb.asm.tree.MethodNode;
import set.hyrts.org.objectweb.asm.tree.MultiANewArrayInsnNode;
import set.hyrts.org.objectweb.asm.tree.TableSwitchInsnNode;
import set.hyrts.org.objectweb.asm.tree.TryCatchBlockNode;
import set.hyrts.org.objectweb.asm.tree.TypeInsnNode;
import set.hyrts.org.objectweb.asm.tree.VarInsnNode;
import set.hyrts.org.objectweb.asm.util.Printer;

public final class CFGDiff {
   Map<Integer, CFGDiff.Node> id_node;
   CFGDiff.Node curNode;
   static Logger logger = Logger.getLogger(CFGDiff.class);

   public Set<String> diff(String oldPath, String newPath, String method) throws IOException, IOException {
      ClassNode oldNode = parseClass(oldPath);
      MethodNode oldMeth = null;
      MethodNode newMeth = null;
      Iterator var7 = oldNode.methods.iterator();

      while(var7.hasNext()) {
         MethodNode m = (MethodNode)var7.next();
         if (ClassContentParser.methodName(m).equals(method)) {
            oldMeth = m;
            break;
         }
      }

      ClassNode newNode = parseClass(newPath);
      Iterator var11 = newNode.methods.iterator();

      while(var11.hasNext()) {
         MethodNode m = (MethodNode)var11.next();
         if (ClassContentParser.methodName(m).equals(method)) {
            newMeth = m;
            break;
         }
      }

      Set<String> chgs = this.compareCFG(oldNode.name, oldMeth, newMeth);
      return chgs;
   }

   private static ClassNode parseClass(String classFileName) throws IOException, FileNotFoundException {
      ClassReader cr = new ClassReader(new FileInputStream(classFileName));
      ClassNode clazz = new ClassNode();
      cr.accept(clazz, 0);
      return clazz;
   }

   public Set<String> compareCFG(String className, MethodNode m1, MethodNode m2) {
      Set<String> chgs = new HashSet();
      if (m1.access == m2.access && StringUtils.equals(m1.signature, m2.signature)) {
         Map<Integer, CFGDiff.Node> graph1 = this.parseMethod(m1);
         Map<Integer, CFGDiff.Node> graph2 = this.parseMethod(m2);
         CFGDiff.Node root1 = (CFGDiff.Node)graph1.get(0);
         CFGDiff.Node root2 = (CFGDiff.Node)graph2.get(0);
         Set<CFGDiff.Node> visited = new HashSet();
         Set<Integer> diffIDs = new HashSet();
         this.DFSDiff(root1, root2, visited, diffIDs);
         Map<Integer, Integer> labelMap = this.labelMapping(m1);
         Iterator var12 = diffIDs.iterator();

         while(var12.hasNext()) {
            Integer id = (Integer)var12.next();
            chgs.add(className + ":" + ClassContentParser.methodName(m1) + ":" + labelMap.get(id));
         }

         return chgs;
      } else {
         chgs.add(className + ":" + ClassContentParser.methodName(m1) + ":0");
         return chgs;
      }
   }

   public void DFSDiff(CFGDiff.Node n1, CFGDiff.Node n2, Set<CFGDiff.Node> visited, Set<Integer> chgs) {
      if (!visited.contains(n1)) {
         visited.add(n1);
         if (n1.equals(n2) && n1.succ.size() == n2.succ.size()) {
            for(int i = 0; i < n1.succ.size(); ++i) {
               this.DFSDiff((CFGDiff.Node)n1.succ.get(i), (CFGDiff.Node)n2.succ.get(i), visited, chgs);
            }
         } else {
            chgs.add(n1.id);
         }

      }
   }

   public Map<Integer, Integer> labelMapping(MethodNode method) {
      Map<Integer, Integer> map = new HashMap();
      InsnList instructions = method.instructions;
      int labelId = 0;

      for(int i = 0; i < instructions.size(); ++i) {
         AbstractInsnNode instruction = instructions.get(i);
         if (instruction.getType() == 8) {
            map.put(i, labelId++);
         }
      }

      return map;
   }

   public Map<Integer, CFGDiff.Node> parseMethod(MethodNode method) {
      InsnList instructions = method.instructions;
      this.id_node = new LinkedHashMap();
      this.curNode = new CFGDiff.Node(0);
      this.id_node.put(0, this.curNode);

      for(int i = 0; i < instructions.size(); ++i) {
         AbstractInsnNode instruction = instructions.get(i);
         this.analyzeInstruction(instruction, i, instructions);
      }

      List<TryCatchBlockNode> trys = method.tryCatchBlocks;
      List<Integer> orderedLabels = new ArrayList();
      if (trys != null && trys.size() > 0) {
         for(int i = 0; i < instructions.size(); ++i) {
            if (this.id_node.containsKey(i)) {
               orderedLabels.add(i);
            }
         }
      }

      Iterator var14 = trys.iterator();

      while(true) {
         TryCatchBlockNode t;
         do {
            if (!var14.hasNext()) {
               logger.info(ClassContentParser.methodName(method));
               var14 = this.id_node.keySet().iterator();

               while(var14.hasNext()) {
                  int i = (Integer)var14.next();
                  CFGDiff.Node n = (CFGDiff.Node)this.id_node.get(i);
                  logger.info(n.content);
                  String item = i + "==>";

                  CFGDiff.Node next;
                  for(Iterator var18 = n.succ.iterator(); var18.hasNext(); item = item + next.id + ",") {
                     next = (CFGDiff.Node)var18.next();
                  }

                  logger.info(item);
               }

               return this.id_node;
            }

            t = (TryCatchBlockNode)var14.next();
         } while(t.type == null);

         int startIndex = instructions.indexOf(t.start);
         int handlerIndex = instructions.indexOf(t.handler);
         if (!this.id_node.containsKey(startIndex)) {
            int id = -1;

            int i;
            for(Iterator var10 = orderedLabels.iterator(); var10.hasNext(); id = i) {
               i = (Integer)var10.next();
               if (i > startIndex) {
                  startIndex = id;
                  break;
               }
            }
         }

         if (this.id_node.containsKey(handlerIndex)) {
            ((CFGDiff.Node)this.id_node.get(startIndex)).addSuccNode((CFGDiff.Node)this.id_node.get(handlerIndex));
         }
      }
   }

   public void analyzeInstruction(AbstractInsnNode instruction, int i, InsnList instructions) {
      String insns = "";
      int opcode = instruction.getOpcode();
      String mnemonic = opcode == -1 ? "" : Printer.OPCODES[instruction.getOpcode()];
      insns = insns + i + ":\t" + mnemonic + " ";
      this.curNode.append(mnemonic);
      List labels;
      int t;
      int defaultTargetId;
      LabelNode targetInstruction;
      int targetId;
      CFGDiff.Node n;
      int insnCode;
      LabelNode defaultTargetInstruction;
      switch(instruction.getType()) {
      case 1:
         if (instruction.getOpcode() == 188) {
            insns = insns + Printer.TYPES[((IntInsnNode)instruction).operand];
            this.curNode.append(Printer.TYPES[((IntInsnNode)instruction).operand]);
         } else {
            insns = insns + ((IntInsnNode)instruction).operand;
            this.curNode.append(((IntInsnNode)instruction).operand + "");
         }
         break;
      case 2:
         insns = insns + ((VarInsnNode)instruction).var;
         this.curNode.append(((VarInsnNode)instruction).var + "");
         break;
      case 3:
         insns = insns + ((TypeInsnNode)instruction).desc;
         this.curNode.append(((TypeInsnNode)instruction).desc);
         break;
      case 4:
         insns = insns + ((FieldInsnNode)instruction).owner;
         insns = insns + ".";
         insns = insns + ((FieldInsnNode)instruction).name;
         insns = insns + " ";
         insns = insns + ((FieldInsnNode)instruction).desc;
         this.curNode.append(((FieldInsnNode)instruction).owner + "." + ((FieldInsnNode)instruction).name + " " + ((FieldInsnNode)instruction).desc);
         break;
      case 5:
         insns = insns + ((MethodInsnNode)instruction).owner;
         insns = insns + ".";
         insns = insns + ((MethodInsnNode)instruction).name;
         insns = insns + " ";
         insns = insns + ((MethodInsnNode)instruction).desc;
         this.curNode.append(((MethodInsnNode)instruction).owner + "." + ((MethodInsnNode)instruction).name + " " + ((MethodInsnNode)instruction).desc);
      case 6:
      default:
         break;
      case 7:
         targetInstruction = ((JumpInsnNode)instruction).label;
         insnCode = instructions.indexOf(targetInstruction);
         insns = insns + insnCode;
         CFGDiff.Node n1;
         if (this.id_node.containsKey(insnCode)) {
            n1 = (CFGDiff.Node)this.id_node.get(insnCode);
         } else {
            n1 = new CFGDiff.Node(insnCode);
            this.id_node.put(insnCode, n1);
         }

         this.curNode.addSuccNode(n1);
         CFGDiff.Node n2;
         if (this.id_node.containsKey(i + 1)) {
            n2 = (CFGDiff.Node)this.id_node.get(i + 1);
         } else {
            n2 = new CFGDiff.Node(i + 1);
            this.id_node.put(i + 1, n2);
         }

         if (instruction.getOpcode() != 167 && instruction.getOpcode() != 168) {
            this.curNode.addSuccNode(n2);
         }
         break;
      case 8:
         insns = insns + "// label";
         if (this.id_node.containsKey(i)) {
            if (i > 0) {
               AbstractInsnNode insn = instructions.get(i - 1);
               if (insn.getType() != 7) {
                  insnCode = insn.getOpcode();
                  if (insnCode != 177 && insnCode != 176 && insnCode != 175 && insnCode != 174 && insnCode != 172 && insnCode != 173) {
                     this.curNode.addSuccNode((CFGDiff.Node)this.id_node.get(i));
                  }
               }
            }

            this.curNode = (CFGDiff.Node)this.id_node.get(i);
         }
         break;
      case 9:
         insns = insns + ((LdcInsnNode)instruction).cst;
         this.curNode.append(((LdcInsnNode)instruction).cst + "");
         break;
      case 10:
         insns = insns + ((IincInsnNode)instruction).var;
         insns = insns + " ";
         insns = insns + ((IincInsnNode)instruction).incr;
         this.curNode.append(((IincInsnNode)instruction).var + " " + ((IincInsnNode)instruction).incr);
         break;
      case 11:
         int minKey = ((TableSwitchInsnNode)instruction).min;
         labels = ((TableSwitchInsnNode)instruction).labels;

         for(t = 0; t < labels.size(); ++t) {
            defaultTargetId = minKey + t;
            targetInstruction = (LabelNode)labels.get(t);
            targetId = instructions.indexOf(targetInstruction);
            insns = insns + defaultTargetId + ": " + targetId + ", ";
            this.curNode.append(defaultTargetId + "");
            if (this.id_node.containsKey(targetId)) {
               n = (CFGDiff.Node)this.id_node.get(targetId);
            } else {
               n = new CFGDiff.Node(targetId);
               this.id_node.put(targetId, n);
            }

            this.curNode.addSuccNode(n);
         }

         defaultTargetInstruction = ((TableSwitchInsnNode)instruction).dflt;
         defaultTargetId = instructions.indexOf(defaultTargetInstruction);
         insns = insns + "default: " + defaultTargetId;
         this.curNode.append("default");
         if (this.id_node.containsKey(defaultTargetId)) {
            n = (CFGDiff.Node)this.id_node.get(defaultTargetId);
         } else {
            n = new CFGDiff.Node(defaultTargetId);
            this.id_node.put(defaultTargetId, n);
         }

         this.curNode.addSuccNode(n);
         break;
      case 12:
         List keys = ((LookupSwitchInsnNode)instruction).keys;
         labels = ((LookupSwitchInsnNode)instruction).labels;

         for(t = 0; t < keys.size(); ++t) {
            defaultTargetId = (Integer)keys.get(t);
            targetInstruction = (LabelNode)labels.get(t);
            targetId = instructions.indexOf(targetInstruction);
            insns = insns + defaultTargetId + ": " + targetId + ", ";
            this.curNode.append(defaultTargetId + "");
            if (this.id_node.containsKey(targetId)) {
               n = (CFGDiff.Node)this.id_node.get(targetId);
            } else {
               n = new CFGDiff.Node(targetId);
               this.id_node.put(targetId, n);
            }

            this.curNode.addSuccNode(n);
         }

         defaultTargetInstruction = ((LookupSwitchInsnNode)instruction).dflt;
         defaultTargetId = instructions.indexOf(defaultTargetInstruction);
         insns = insns + "default: " + defaultTargetId;
         this.curNode.append("default");
         if (this.id_node.containsKey(defaultTargetId)) {
            n = (CFGDiff.Node)this.id_node.get(defaultTargetId);
         } else {
            n = new CFGDiff.Node(defaultTargetId);
            this.id_node.put(defaultTargetId, n);
         }

         this.curNode.addSuccNode(n);
         break;
      case 13:
         insns = insns + ((MultiANewArrayInsnNode)instruction).desc;
         insns = insns + " ";
         insns = insns + ((MultiANewArrayInsnNode)instruction).dims;
         this.curNode.append(((MultiANewArrayInsnNode)instruction).desc + " " + ((MultiANewArrayInsnNode)instruction).dims);
         break;
      case 14:
         insns = insns + ((FrameNode)instruction).stack;
         break;
      case 15:
         insns = insns + "// line number information";
      case 0:
         if (instruction.getOpcode() == 191 && !this.id_node.containsKey(i + 1) && i + 1 < instructions.size()) {
            n = new CFGDiff.Node(i + 1);
            this.id_node.put(i + 1, n);
         }
      }

      logger.debug(insns);
   }

   static class Node {
      int id;
      List<CFGDiff.Node> succ = new ArrayList();
      String content = "";

      public Node(int id) {
         this.id = id;
      }

      public void append(String con) {
         this.content = this.content + con + " ";
      }

      public void newLine() {
         this.content = this.content + "\n";
      }

      public void addSuccNode(CFGDiff.Node node) {
         if (!this.succ.contains(node)) {
            this.succ.add(node);
         }

      }

      public boolean equals(CFGDiff.Node node) {
         return this.content.equals(node.content);
      }
   }
}

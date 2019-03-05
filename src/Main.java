import soot.*;
import soot.jimple.AssignStmt;
import soot.options.Options;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.scalar.FlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ArraySparseSet;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        String classPath = "inputs";
        configureSoot(classPath);// configure soot
        Scene.v().loadNecessaryClasses(); // load all the library and dependencies for given program
        SootClass mainClass = Scene.v().getMainClass();

        //get method
        SootMethod testMethod = mainClass.getMethodByName("test");
        Body methodBody = testMethod.retrieveActiveBody();
        BlockGraph blockGraph = new BriefBlockGraph(methodBody);

        /*
        //get basic blocks and generate a topological sorted list of them
        List<Block> iterList = new ArrayList<Block>();
        Iterator<Block> graphIt = blockGraph.getTails().iterator();
        while (graphIt.hasNext()) {
            Block block = graphIt.next();
            iterList.add(block);
        }
        boolean exist = false, contain = true;
        while(!exist) {
            exist = true;
            int m = iterList.size(), nexts = 0;
            for (int i = 0; i < m; i++) {
                List<Block> pred = iterList.get(i).getPreds();
                nexts += pred.size();
                int n = pred.size();
                for (int j = 0; j < n; j++) {
                    Block p = pred.get(j);
                    if (!iterList.contains(p)) {
                        exist = false;
                        List<Block> succs = p.getSuccs();
                        contain = true;
                        for(Block suc : succs) {
                            if (!iterList.contains(suc))
                                contain = false;
                        }
                        if(nexts == 1)
                            contain = true;
                        if(contain) {
                            iterList.add(p);
                        }
                    }
                }
            }
        }
        */

        //initialize LIVEOUT, UEVAR and VARKILL
        List<Block> iterList = new ArrayList<Block>(blockGraph.getBlocks());
        List<FlowSet<Value>> liveout = new ArrayList<>();
        List<FlowSet<Value>> uevar = new ArrayList<>();
        List<FlowSet<Value>> varkill = new ArrayList<>();

        for(int k = 0; k < iterList.size(); k++) {
            Block current = iterList.get(k);
            FlowSet<Value> lo = new ArraySparseSet<>();
            FlowSet<Value> uv = new ArraySparseSet<>();
            FlowSet<Value> vk = new ArraySparseSet<>();
            //LIVEOUT
            liveout.add(lo);
            //UEVAR and VARKILL
            Iterator<Unit> curUnit = current.iterator();
            while(curUnit.hasNext()) {
                Unit unit = curUnit.next();
                if(unit instanceof AssignStmt) {
                    AssignStmt assignstmt = (AssignStmt) unit;
                    for(ValueBox usebox : assignstmt.getUseBoxes()) {
                        if(usebox.getValue() instanceof Local && !vk.contains(usebox.getValue()) && !uv.contains((usebox.getValue()))) {
                            uv.add(usebox.getValue());
                        }
                    }
                    for(ValueBox defbox : assignstmt.getDefBoxes()) {
                        if(!vk.contains(defbox.getValue())) {
                            vk.add(defbox.getValue());
                        }
                    }
                }
            }
            uevar.add(uv);
            varkill.add(vk);
        }

        //iteratively compute LIVEOUT
        boolean flag = true;
        int count = 0;
        while(count < 5) {
            flag = false;
            count++;
            System.out.println(count);
            List<FlowSet<Value>> newLiveOut = new ArrayList<>();
            for (int l = 0; l < iterList.size(); l++) {
                FlowSet<Value> curLive = new ArraySparseSet<>();
                for (Block suc : iterList.get(l).getSuccs()) {
                    FlowSet<Value> liveEach = new ArraySparseSet<>();
                    int ind = iterList.indexOf((suc));
                    liveout.get(ind).difference(varkill.get(ind), liveEach);
                    uevar.get(ind).union(liveEach, liveEach);
                    curLive.union(liveEach);
                }
                newLiveOut.add(curLive);
                if (curLive != liveout.get(l)) {
                    flag = true;
                }
            }
            liveout = newLiveOut;
        }

        for(int m = 0; m < iterList.size(); m++) {
            System.out.println("+------------New block-----------+");
            Iterator<Unit> blockIt = iterList.get(m).iterator();
            while (blockIt.hasNext()) {
                Unit unit = blockIt.next();
                System.out.println(unit.toString());
            }
            FlowSet<Value> liveIn = new ArraySparseSet<>();
            liveout.get(m).difference(varkill.get(m), liveIn);
            uevar.get(m).union(liveIn, liveIn);
            String liveInStr = "";
            for(Value in : liveIn) {
                liveInStr += in.toString() + "\t";
            }
            String liveOutStr = "";
            for(Value out : liveout.get(m)) {
                liveOutStr += out.toString() + "\t";
            }
            System.out.println("Liveness Results Before: " + liveInStr);
            System.out.println("Liveness Results After: " + liveOutStr);
        }
    }

    public static void configureSoot(String classpath) {
        Options.v().set_whole_program(true);  // process whole program
        Options.v().set_allow_phantom_refs(true); // load phantom references
        Options.v().set_prepend_classpath(true); // prepend class path
        Options.v().set_src_prec(Options.src_prec_class); // process only .class files, change here to process other IR or class
        Options.v().set_output_format(Options.output_format_jimple); // output jimple format, change here to output other IR
        ArrayList<String> list = new ArrayList<>();
        list.add(classpath);
        Options.v().set_process_dir(list); // process all .class files in directory
        Options.v().setPhaseOption("cg.spark", "on"); // use spark for call graph
    }

}
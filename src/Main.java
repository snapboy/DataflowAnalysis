import soot.*;
import soot.options.Options;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;
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

        //compute UEVAR and VARKILL

        //iteratively compute LIVEOUT


        for(Block r : iterList) {
            System.out.println("New block");
            Iterator<Unit> blockIt = r.iterator();
            while (blockIt.hasNext()) {
                Unit unit = blockIt.next();
                System.out.println(unit.toString());
            }
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

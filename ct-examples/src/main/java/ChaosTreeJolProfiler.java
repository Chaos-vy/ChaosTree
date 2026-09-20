import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.naryMap.BTreeMap;
import org.openjdk.jol.info.GraphLayout;

import java.util.Map;
import java.util.TreeMap;

public class ChaosTreeJolProfiler {

    public static void main(String[] args) {
        int size = 1_000_000;
        if (args.length > 0) {
            size = Integer.parseInt(args[0]);
        }

        System.out.println(" JOL Memory Footprint Profiler (Size = " + size + ")");

        System.out.println("1. Profiling java.util.TreeMap...");
        Map<Integer, Integer> javaTree = new TreeMap<>();
        for (int i = 0; i < size; i++) javaTree.put(i, i);
        System.out.println(GraphLayout.parseInstance(javaTree).toFootprint());
        javaTree.clear();
        javaTree = null;
        System.gc();

        System.out.println("\n2. Profiling B-TreeMap...");
        Map<Integer, Integer> bTree = new BTreeMap<>();
        for (int i = 0; i < size; i++) bTree.put(i, i);
        System.out.println(GraphLayout.parseInstance(bTree).toFootprint());
        for (int i = 0; i < size / 2; i++) {
            bTree.remove(i);
        }
        System.out.println(GraphLayout.parseInstance(bTree).toFootprint());
        bTree.clear();
        bTree = null;
        System.gc();

        System.out.println("\n3. Profiling B+TreeMap...");
        Map<Integer, Integer> bPlusTree = new BPlusTreeMap<>();
        for (int i = 0; i < size; i++) bPlusTree.put(i, i);
        System.out.println(GraphLayout.parseInstance(bPlusTree).toFootprint());
        for (int i = 0; i < size / 2; i++) {
            bPlusTree.remove(i);
        }
        System.out.println(GraphLayout.parseInstance(bPlusTree).toFootprint());
    }
}

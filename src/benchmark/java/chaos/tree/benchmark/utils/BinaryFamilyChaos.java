package chaos.tree.benchmark.utils;

import chaos.tree.benchmark.annotations.Chaos;
import chaos.tree.binary.avl.AVL;
import chaos.tree.binary.bst.BST;
import chaos.tree.binary.rbt.RBT;
import chaos.tree.binary.splay.Splay;
import chaos.tree.binary.treap.Treap;
import chaos.tree.core.searchtree.binary.BinaryTree;

import java.util.TreeMap;

public class BinaryFamilyChaos {

    BinaryTree<Integer> tree;

    @Chaos(
            description = "Probing Stack Depth via Sequential BST and Heap Sizing via AVL"
    )
    public void executeSystemCollapse(long initialCap) {
        MySystemInfo.printSignature();
        System.out.println("=".repeat(50));
        System.out.println("\n[Chaos Engine] Initiating Stack Depth Degradation via BST...");
        tree = new BST<>();
        try {
            for (int i = 0; i < initialCap; i++) {
                tree.insert(i);
            }
        } catch (StackOverflowError sof) {
            System.err.println(">> SUCCESS: StackOverflowError caught at BST size: " + tree.size());
        }
        System.out.println("=".repeat(50));
        System.out.println("\n[Chaos Engine] Initiating Heap Saturation via AVL...");
        tree = new AVL<>();
        long nodeCount = 0;
        try {
            while (true) {
                tree.insert((int) nodeCount);
                nodeCount++;
            }
        } catch (OutOfMemoryError oom) {
            tree = null;
            System.gc();
            System.err.println(">> SUCCESS: OutOfMemoryError caught at allocation count: " + String.format("%,d", nodeCount));
        }
        System.out.println("=".repeat(50));
        System.out.println("\n[Chaos Engine] Initiating Heap Saturation via RBT...");
        tree = new RBT<>();
        nodeCount = 0;
        try {
            while (true) {
                tree.insert((int) nodeCount);
                nodeCount++;
            }
        } catch (OutOfMemoryError oom) {
            tree = null;
            System.gc();
            System.err.println(">> SUCCESS: OutOfMemoryError caught at allocation count: " + String.format("%,d", nodeCount));
        }

        System.out.println("=".repeat(50));
        System.out.println("\n[Chaos Engine] Initiating Heap Saturation via Splay...");
        tree = new Splay<>();
        nodeCount = 0;
        try {
            while (true) {
                tree.insert((int) nodeCount);
                nodeCount++;
            }
        } catch (OutOfMemoryError oom) {
            tree = null;
            System.gc();
            System.err.println(">> SUCCESS: OutOfMemoryError caught at allocation count: " + String.format("%,d", nodeCount));
        }
        System.out.println("=".repeat(50));
        System.out.println("\n[Chaos Engine] Initiating Heap Saturation via Treap...");
        tree = new Treap<>();//left like that local random and priority bound at INTEGER.MAX_VALUE
        nodeCount = 0;
        try {
            while (true) {
                tree.insert((int) nodeCount);
                nodeCount++;
            }
        } catch (OutOfMemoryError oom) {
            tree = null;
            System.gc();
            System.err.println(">> SUCCESS: OutOfMemoryError caught at allocation count: " + String.format("%,d", nodeCount));
        }
        System.out.println("=".repeat(50));
        System.out.println("\n[Chaos Engine] Initiating Heap Saturation via TreeMap...");
        TreeMap<Integer, Integer> truth = new TreeMap<>();
        nodeCount = 0;
        try {
            while (true) {
                truth.put((int) nodeCount, (int) nodeCount);
                nodeCount++;
            }
        } catch (OutOfMemoryError oom) {
            truth.clear();
            System.gc();
            System.err.println(">> SUCCESS: OutOfMemoryError caught at allocation count: " + String.format("%,d", nodeCount));
        }
    }
}
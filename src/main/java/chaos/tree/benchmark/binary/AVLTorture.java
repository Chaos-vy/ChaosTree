package chaos.tree.benchmark;

import chaos.tree.binary.AVL;
import chaos.tree.core.ITree;

public class AVLTorture {
    public static void main(String[] args) {
        ITree<Integer> avl = new AVL<>();
        long start = System.nanoTime();
        try {
            for (int i = 0; ; i++) {
                avl.insert(i);
                if (i % 1000 == 0) {
                    System.out.printf("Nodes=%d Height=%d%n", i, avl.height());
                }
            }
        } catch (OutOfMemoryError e) {
            long end = System.nanoTime();
            System.out.println("\n========== AVL DIED ==========");
            System.out.println("Height = " + avl.height());
            System.out.println("Size   = " + avl.size());
            System.out.println("Time   = " + ((end - start) / 1_000_000) + " ms");
        }
    }
}

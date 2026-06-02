package chaos.tree.benchmark;

import chaos.tree.binary.BST;

public class BSTTorture {
    public static void main(String[] args) {
        BST<Integer> bst = new BST<>();
        SystemInfo.printSystemInfo();
        long start = System.nanoTime();
        try {
            for (int i = 0; ; i++) {
                bst.insert(i);
                if (i % 1000 == 0) {
                    System.out.printf("Nodes=%d Height=%d%n", i, bst.height());
                }
            }
        }
        catch (StackOverflowError e) {
            System.out.println("\n========== BST DIED ==========");
            long end = System.nanoTime();
            System.out.println("Height = " + bst.height());
            System.out.println("Size   = " + bst.size());
            Runtime rt = Runtime.getRuntime();
            System.out.println("Time   = " + ((end - start) / 1_000_000) + " ms");
            long used = (rt.totalMemory() - rt.freeMemory())/ (1024 * 1024);

            System.out.println("Used Heap = " + used + " MB");
        }
    }
}
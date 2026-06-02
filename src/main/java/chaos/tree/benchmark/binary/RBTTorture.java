package chaos.tree.benchmark;

import chaos.tree.binary.RBT;
import chaos.tree.core.ITree;

public class RBTTorture {
    public static void main(String[] args) {
        ITree<Integer> rbt = new RBT<>();
        long start = System.nanoTime();
        try {
            for (int i = 0; ; i++) {
                rbt.insert(i);
                if (i % 1_000_000 == 0) {
                    System.out.printf("Nodes=%d Height=%d Time=%d sec%n",i, rbt.height(),(System.currentTimeMillis() - start)/1000);
                }
            }
        } catch (Exception e) {
            long end = System.nanoTime();
            System.out.println("\n========== RBT DIED ==========");
            System.out.println("Height = " + rbt.height());
            System.out.println("Size   = " + rbt.size());
            System.out.println("Time   = " + ((end - start) / 1_000_000) + " ms");
        }
    }
}

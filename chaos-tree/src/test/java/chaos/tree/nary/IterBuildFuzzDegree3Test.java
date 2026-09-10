package chaos.tree.nary;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IterBuildFuzzDegree3 {
    static class RangeIterator implements Iterator<Integer> {
        int cur = 0; final int n;
        RangeIterator(int n) { this.n = n; }
        public boolean hasNext() { return cur < n; }
        public Integer next() { if (!hasNext()) throw new NoSuchElementException(); return cur++; }
    }

    @Test
    public void verifier() {
        System.out.println("Testing BTreeSet...");
        runFuzz(1);
        System.out.println("Testing BPlusTreeSet...");
        runFuzz(2);
    }

    public static void runFuzz(int type) {
        int failures = 0;
        int trials = 0;
        int[] degrees = {3, 4, 5};
        float[] factors = {0.5f, 0.55f, 0.6f, 0.75f, 0.9f, 1.0f};
        for (int degree : degrees) {
            for (float f : factors) {
                for (int n = 0; n <= 2500; n++) {
                    trials++;
                    if (!runOne(n, degree, f, type)) failures++;
                }
            }
        }
        System.out.println("Type " + type + " trials=" + trials + " failures=" + failures);
        if (failures > 0) System.exit(1);
    }

    static boolean runOne(int n, int degree, float factor, int type) {
        try {
            if (type == 1) {
                BTreeSet<Integer> tree = new BTreeSet<>(degree);
                tree.buildFromSorted(new RangeIterator(n), factor);
                if (tree.size() != n) throw new RuntimeException("Size mismatch");
                validateBTreeSet(tree.root, tree.minKeys);
            } else if (type == 2) {
                BPlusTreeSet<Integer> tree = new BPlusTreeSet<>(degree);
                tree.buildFromSorted(new RangeIterator(n), factor);
                if (tree.size() != n) throw new RuntimeException("Size mismatch");
                validateBPlusTreeSet(tree.root, tree.minKeys);
            }
            return true;
        } catch (Throwable t) {
            System.out.println("FAIL n=" + n + " degree=" + degree + " factor=" + factor + " -> " + t.getMessage());
            return false;
        }
    }

    private static void validateBTreeSet(BTreeNode<?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null && node.keyCount < minKeys) throw new AssertionError("Underflow!");
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) if (node.child[i] != null) validateBTreeSet(node.child[i], minKeys);
        }
    }
    
    private static void validateBPlusTreeSet(BPlusTreeNode<?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null && node.keyCount < minKeys) throw new AssertionError("Underflow!");
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) if (node.child[i] != null) validateBPlusTreeSet(node.child[i], minKeys);
        }
    }
}

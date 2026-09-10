package chaos.tree.naryMap;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.AbstractMap;
import java.util.NoSuchElementException;

public class IterBuildFuzzDegree3MapTest {
    static class EntryRangeIterator implements Iterator<Map.Entry<Integer, Integer>> {
        int cur = 0; final int n;
        EntryRangeIterator(int n) { this.n = n; }
        public boolean hasNext() { return cur < n; }
        public Map.Entry<Integer, Integer> next() { if (!hasNext()) throw new NoSuchElementException(); int v=cur++; return new AbstractMap.SimpleEntry<>(v,v); }
    }
    @Test
    public void verifier() {
        System.out.println("Testing BTreeMap...");
        runFuzz(3);
        System.out.println("Testing BPlusTreeMap...");
        runFuzz(4);
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
            if (type == 3) {
                BTreeMap<Integer, Integer> tree = new BTreeMap<>(degree);
                tree.buildFromSorted(new EntryRangeIterator(n), factor);
                if (tree.size() != n) throw new RuntimeException("Size mismatch");
                validateBTreeMap(tree.root, tree.minKeys);
            } else if (type == 4) {
                BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(degree);
                tree.buildFromSorted(new EntryRangeIterator(n), factor);
                if (tree.size() != n) throw new RuntimeException("Size mismatch");
                validateBPlusTreeMap(tree.root, tree.minKeys);
            }
            return true;
        } catch (Throwable t) {
            System.out.println("FAIL n=" + n + " degree=" + degree + " factor=" + factor + " -> " + t.getMessage());
            return false;
        }
    }
    
    private static void validateBTreeMap(BTreeMapNode<?, ?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null && node.keyCount < minKeys) throw new AssertionError("Underflow!");
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) if (node.child[i] != null) validateBTreeMap(node.child[i], minKeys);
        }
    }
    
    private static void validateBPlusTreeMap(BPlusTreeMapNode<?, ?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null && node.keyCount < minKeys) throw new AssertionError("Underflow!");
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) if (node.child[i] != null) validateBPlusTreeMap(node.child[i], minKeys);
        }
    }
}

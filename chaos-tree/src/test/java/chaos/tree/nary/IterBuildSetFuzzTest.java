package chaos.tree.nary;

import org.junit.jupiter.api.Test;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IterBuildSetFuzzTest {

    static class RangeIterator implements Iterator<Integer> {
        int cur = 0; final int n;
        RangeIterator(int n) { this.n = n; }
        public boolean hasNext() { return cur < n; }
        public Integer next() { if (!hasNext()) throw new NoSuchElementException(); return cur++; }
    }

    @Test
    void testBTreeSetExtremeEdgeCases() {
        runFuzz(1);
    }

    @Test
    void testBPlusTreeSetExtremeEdgeCases() {
        runFuzz(2);
    }

    private void runFuzz(int type) {
        int[] degrees = {3, 4, 5, 32};
        float[] factors = {0.5f, 0.55f, 0.75f, 1.0f};

        for (int degree : degrees) {
            for (float f : factors) {
                for (int n = 0; n <= 500; n++) {
                    runOne(n, degree, f, type);
                }

                int maxN = (int) Math.pow(degree, 3);
                for (int n = maxN - 10; n <= maxN + 10; n++) {
                    if (n >= 0) runOne(n, degree, f, type);
                }
            }
        }
    }

    private void runOne(int n, int degree, float factor, int type) {
        if (type == 1) {
            BTreeSet<Integer> tree = new BTreeSet<>(degree);
            tree.buildFromSorted(new RangeIterator(n), factor);
            assertEquals(n, tree.size());
            validateBTreeSet(tree.root, tree.minKeys);
        } else if (type == 2) {
            BPlusTreeSet<Integer> tree = new BPlusTreeSet<>(degree);
            tree.buildFromSorted(new RangeIterator(n), factor);
            assertEquals(n, tree.size());
            validateBPlusTreeSet(tree.root, tree.minKeys);
        }
    }

    private void validateBTreeSet(BTreeNode<?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null && node.keyCount < minKeys) {
            throw new AssertionError("Underflow! Node has " + node.keyCount + " keys (min " + minKeys + ")");
        }
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) {
                if (node.child[i] != null) validateBTreeSet(node.child[i], minKeys);
            }
        }
    }
    
    private void validateBPlusTreeSet(BPlusTreeNode<?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null && node.keyCount < minKeys) {
            throw new AssertionError("Underflow! Node has " + node.keyCount + " keys (min " + minKeys + ")");
        }
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) {
                if (node.child[i] != null) validateBPlusTreeSet(node.child[i], minKeys);
            }
        }
    }
}

package chaos.tree.naryMap;

import org.junit.jupiter.api.Test;
import java.util.Iterator;
import java.util.Map;
import java.util.AbstractMap;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IterBuildMapFuzzTest {

    static class EntryRangeIterator implements Iterator<Map.Entry<Integer, Integer>> {
        int cur = 0; final int n;
        EntryRangeIterator(int n) { this.n = n; }
        public boolean hasNext() { return cur < n; }
        public Map.Entry<Integer, Integer> next() { if (!hasNext()) throw new NoSuchElementException(); int v=cur++; return new AbstractMap.SimpleEntry<>(v,v); }
    }

    @Test
    void testBTreeMapExtremeEdgeCases() {
        runFuzz(3);
    }

    @Test
    void testBPlusTreeMapExtremeEdgeCases() {
        runFuzz(4);
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
        if (type == 3) {
            BTreeMap<Integer, Integer> tree = new BTreeMap<>(degree);
            tree.buildFromSorted(new EntryRangeIterator(n), factor);
            assertEquals(n, tree.size());
            validateBTreeMap(tree.root, tree.minKeys);
        } else if (type == 4) {
            BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(degree);
            tree.buildFromSorted(new EntryRangeIterator(n), factor);
            assertEquals(n, tree.size());
            validateBPlusTreeMap(tree.root, tree.minKeys);
        }
    }
    
    private void validateBTreeMap(BTreeMapNode<?, ?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null && node.keyCount < minKeys) {
            throw new AssertionError("Underflow! Node has " + node.keyCount + " keys (min " + minKeys + ")");
        }
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) {
                if (node.child[i] != null) validateBTreeMap(node.child[i], minKeys);
            }
        }
    }
    
    private void validateBPlusTreeMap(BPlusTreeMapNode<?, ?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null && node.keyCount < minKeys) {
            throw new AssertionError("Underflow! Node has " + node.keyCount + " keys (min " + minKeys + ")");
        }
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) {
                if (node.child[i] != null) validateBPlusTreeMap(node.child[i], minKeys);
            }
        }
    }
}

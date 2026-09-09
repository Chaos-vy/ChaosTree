package chaos.tree.naryMap;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.FloatRange;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class BulkLoadMapPropertyTest {

    static class EntryRangeIterator implements Iterator<Map.Entry<Integer, Integer>> {
        int cur = 0; final int n;
        EntryRangeIterator(int n) { this.n = n; }
        public boolean hasNext() { return cur < n; }
        public Map.Entry<Integer, Integer> next() { if (!hasNext()) throw new NoSuchElementException(); int v=cur++; return new AbstractMap.SimpleEntry<>(v,v); }
    }

    /**
     * timestamp = 2026-09-09T18:34:45.101625748, BulkLoadMapPropertyTest:testBPlusTreeMapBulkLoad =
     *                               |-----------------------jqwik-----------------------
     * tries = 100000                | # of calls to property
     * checks = 100000               | # of not rejected calls
     * generation = RANDOMIZED       | parameters are randomly generated
     * after-failure = SAMPLE_FIRST  | try previously failed sample, then previous seed
     * when-fixed-seed = ALLOW       | fixing the random seed is allowed
     * edge-cases#mode = MIXIN       | edge cases are mixed in
     * edge-cases#total = 60         | # of all combined edge cases
     * edge-cases#tried = 60         | # of edge cases tried in current run
     * seed = -4933718074328139913   | random seed to reproduce generated values
     * @param degree = [3,128]
     * @param factor = [0.5f,1.0f]
     * @param n = [100k]
     */
    @Property(tries = 10000)
    void testBTreeMapBulkLoad(@ForAll @IntRange(min = 3, max = 128) int degree,
                              @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor,
                              @ForAll @IntRange(min = 0, max = 10000) int n) {
        BTreeMap<Integer, Integer> tree = new BTreeMap<>(degree);
        tree.buildFromSorted(new EntryRangeIterator(n), factor);
        Assertions.assertEquals(n, tree.size());
        validateBTreeMap(tree.root, tree.minKeys);
    }

    /**
     *timestamp = 2026-09-09T18:34:59.280318178, BulkLoadMapPropertyTest:testBTreeMapBulkLoad =
     *                               |-----------------------jqwik-----------------------
     * tries = 100000                | # of calls to property
     * checks = 100000               | # of not rejected calls
     * generation = RANDOMIZED       | parameters are randomly generated
     * after-failure = SAMPLE_FIRST  | try previously failed sample, then previous seed
     * when-fixed-seed = ALLOW       | fixing the random seed is allowed
     * edge-cases#mode = MIXIN       | edge cases are mixed in
     * edge-cases#total = 60         | # of all combined edge cases
     * edge-cases#tried = 60         | # of edge cases tried in current run
     * seed = 9913603941170430       | random seed to reproduce generated values
     * @param degree = [3,128]
     * @param factor = [0.5f,1.0f]
     * @param n = [100k]
     */
    @Property(tries = 10000)
    void testBPlusTreeMapBulkLoad(@ForAll @IntRange(min = 3, max = 128) int degree,
                                  @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor,
                                  @ForAll @IntRange(min = 0, max = 10000) int n) {
        BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(degree);
        tree.buildFromSorted(new EntryRangeIterator(n), factor);
        Assertions.assertEquals(n, tree.size());
        validateBPlusTreeMap(tree.root, tree.minKeys);
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

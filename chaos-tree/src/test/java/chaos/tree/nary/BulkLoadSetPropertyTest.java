package chaos.tree.nary;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.FloatRange;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class BulkLoadSetPropertyTest {

    static class RangeIterator implements Iterator<Integer> {
        int cur = 0; final int n;
        RangeIterator(int n) { this.n = n; }
        public boolean hasNext() { return cur < n; }
        public Integer next() { if (!hasNext()) throw new NoSuchElementException(); return cur++; }
    }

    /**
     * timestamp = 2026-09-09T18:04:52.124325020, BulkLoadSetPropertyTest:testBTreeSetBulkLoad =
     *                               |-----------------------jqwik-----------------------
     * tries = 100000                | # of calls to property
     * checks = 100000               | # of not rejected calls
     * generation = RANDOMIZED       | parameters are randomly generated
     * after-failure = SAMPLE_FIRST  | try previously failed sample, then previous seed
     * when-fixed-seed = ALLOW       | fixing the random seed is allowed
     * edge-cases#mode = MIXIN       | edge cases are mixed in
     * edge-cases#total = 60         | # of all combined edge cases
     * edge-cases#tried = 60         | # of edge cases tried in current run
     * seed = -7572087307078597387   | random seed to reproduce generated values
     * @param degree
     * @param factor
     * @param n
     */
    @Property(tries = 10000)
    void testBTreeSetBulkLoad(@ForAll @IntRange(min = 3, max = 128) int degree,
                              @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor,
                              @ForAll @IntRange(min = 0, max = 10000) int n) {
        BTreeSet<Integer> tree = new BTreeSet<>(degree);
        tree.buildFromSorted(new RangeIterator(n), factor);
        Assertions.assertEquals(n, tree.size());
        validateBTreeSet(tree.root, tree.minKeys);
    }

    /**
     *timestamp = 2026-09-09T18:04:53.503824400, BulkLoadSetPropertyTest:testBPlusTreeSetBulkLoad =
     *                               |-----------------------jqwik-----------------------
     * tries = 100000                | # of calls to property
     * checks = 100000               | # of not rejected calls
     * generation = RANDOMIZED       | parameters are randomly generated
     * after-failure = SAMPLE_FIRST  | try previously failed sample, then previous seed
     * when-fixed-seed = ALLOW       | fixing the random seed is allowed
     * edge-cases#mode = MIXIN       | edge cases are mixed in
     * edge-cases#total = 60         | # of all combined edge cases
     * edge-cases#tried = 60         | # of edge cases tried in current run
     * seed = 358030456051200726     | random seed to reproduce generated values
     * @param degree
     * @param factor
     * @param n
     */
    @Property(tries = 10000)
    void testBPlusTreeSetBulkLoad(@ForAll @IntRange(min = 3, max = 128) int degree,
                                  @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor,
                                  @ForAll @IntRange(min = 0, max = 10000) int n) {
        BPlusTreeSet<Integer> tree = new BPlusTreeSet<>(degree);
        tree.buildFromSorted(new RangeIterator(n), factor);
        Assertions.assertEquals(n, tree.size());
        validateBPlusTreeSet(tree.root, tree.minKeys);
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

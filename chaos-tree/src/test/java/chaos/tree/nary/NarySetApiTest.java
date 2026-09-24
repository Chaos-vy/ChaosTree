package chaos.tree.nary;

import chaos.tree.AbstractNavigableSetApiTest;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.FloatRange;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

public class NarySetApiTest extends AbstractNavigableSetApiTest {

    private static void checkInvariants(NavigableSet<Integer> tree) {
        if (tree instanceof BTreeSet) {
            validateBTree((BTreeSet<Integer>) tree);
        } else if (tree instanceof BPlusTreeSet) {
            validateBPlusTree((BPlusTreeSet<Integer>) tree);
        }
    }

    private static void validateBTree(BTreeSet<Integer> tree) {
        if (tree.root == null) {
            if (tree.size() != 0) throw new AssertionError("Root is null but size is " + tree.size());
            return;
        }
        int expectedDepth = -1;
        int sizeCount = validateBTreeNode((BTreeNode<Integer>) tree.root, true, 0, new int[]{expectedDepth}, tree.degree, tree);
        if (sizeCount != tree.size()) throw new AssertionError("Size mismatch: tracked=" + tree.size() + " actual=" + sizeCount);
    }

    private static int validateBTreeNode(BTreeNode<Integer> node, boolean isRoot, int depth, int[] expectedDepth, int degree, BTreeSet<Integer> tree) {
        int t = degree;
        int maxKeys = (t << 1) - 1;
        int minKeys = t - 1;

        if (node.keyCount > maxKeys) throw new AssertionError("Node overfull: " + node.keyCount);
        if (!isRoot && node.keyCount < minKeys) throw new AssertionError("Node underfull: " + node.keyCount);

        for (int i = 0; i < node.keyCount - 1; i++) {
            if (tree.compare((Integer) node.keys[i], (Integer) node.keys[i+1]) >= 0) {
                throw new AssertionError("Keys not strictly increasing in node");
            }
        }

        int count = node.keyCount;
        if (node.isLeaf()) {
            if (expectedDepth[0] == -1) expectedDepth[0] = depth;
            else if (expectedDepth[0] != depth) throw new AssertionError("Uneven leaf depth");
        } else {
            for (int i = 0; i <= node.keyCount; i++) {
                BTreeNode<Integer> child = (BTreeNode<Integer>) node.child[i];
                if (child == null) throw new AssertionError("Null child pointer in internal node");
                if (child.parent != node) throw new AssertionError("Incorrect parent pointer");

                if (i > 0) {
                    if (tree.compare(getMin(child), (Integer) node.keys[i-1]) <= 0) {
                        throw new AssertionError("Child keys not strictly greater than left separator");
                    }
                }
                if (i < node.keyCount) {
                    if (tree.compare(getMax(child), (Integer) node.keys[i]) >= 0) {
                        throw new AssertionError("Child keys not strictly less than right separator");
                    }
                }
                count += validateBTreeNode(child, false, depth + 1, expectedDepth, degree, tree);
            }
        }
        return count;
    }

    private static Integer getMin(BTreeNode<Integer> node) {
        while (!node.isLeaf()) node = (BTreeNode<Integer>) node.child[0];
        return (Integer) node.keys[0];
    }

    private static Integer getMax(BTreeNode<Integer> node) {
        while (!node.isLeaf()) node = (BTreeNode<Integer>) node.child[node.keyCount];
        return (Integer) node.keys[node.keyCount - 1];
    }

    private static void validateBPlusTree(BPlusTreeSet<Integer> tree) {
        if (tree.root == null) {
            if (tree.size() != 0) throw new AssertionError("Root is null but size is " + tree.size());
            return;
        }
        int expectedDepth = -1;
        int sizeCount = validateBPlusTreeNode( tree.root, true, 0, new int[]{expectedDepth}, tree.degree, tree);
        if (sizeCount != tree.size()) throw new AssertionError("Size mismatch: tracked=" + tree.size() + " actual=" + sizeCount);
        
        BPlusTreeNode<Integer> curr =tree.root;
        if (curr != null) {
            while (!curr.isLeaf()) curr = curr.child[0];
        }
        if (curr == null) throw new AssertionError("Head is null but tree not empty");
        int listCount = 0;
        Integer prevKey = null;
        while (curr != null) {
            listCount += curr.keyCount;
            for (int i = 0; i < curr.keyCount; i++) {
                Integer k = (Integer) curr.keys[i];
                if (prevKey != null && tree.compare(prevKey, k) >= 0) {
                    throw new AssertionError("Leaf list not strictly increasing");
                }
                prevKey = k;
            }
            if (curr.next != null && curr.next.prev != curr) throw new AssertionError("Broken prev link");
            curr = curr.next;
        }
        if (listCount != tree.size()) throw new AssertionError("List size mismatch");
    }

    private static int validateBPlusTreeNode(BPlusTreeNode<Integer> node, boolean isRoot, int depth, int[] expectedDepth, int degree, BPlusTreeSet<Integer> tree) {
        int t = degree;
        int maxKeys = (t << 1) - 1;
        int minKeys = t - 1;

        if (node.keyCount > maxKeys) throw new AssertionError("Node overfull: " + node.keyCount);
        if (!isRoot && node.keyCount < minKeys) throw new AssertionError("Node underfull: " + node.keyCount);

        for (int i = 0; i < node.keyCount - 1; i++) {
            if (tree.compare((Integer) node.keys[i], (Integer) node.keys[i+1]) >= 0) {
                throw new AssertionError("Keys not strictly increasing in node");
            }
        }

        if (node.isLeaf()) {
            if (expectedDepth[0] == -1) expectedDepth[0] = depth;
            else if (expectedDepth[0] != depth) throw new AssertionError("Uneven leaf depth");
            return node.keyCount;
        } else {
            int count = 0;
            for (int i = 0; i <= node.keyCount; i++) {
                BPlusTreeNode<Integer> child = (BPlusTreeNode<Integer>) node.child[i];
                if (child == null) throw new AssertionError("Null child pointer in internal node");
                if (child.parent != node) throw new AssertionError("Incorrect parent pointer");

                if (i > 0) {
                    if (tree.compare(getMinPlus(child), (Integer) node.keys[i-1]) < 0) {
                        throw new AssertionError("Child min key less than left separator");
                    }
                }
                if (i < node.keyCount) {
                    if (tree.compare(getMaxPlus(child), (Integer) node.keys[i]) >= 0) {
                        throw new AssertionError("Child max key greater than or equal to right separator");
                    }
                }
                count += validateBPlusTreeNode(child, false, depth + 1, expectedDepth, degree, tree);
            }
            return count;
        }
    }

    private static Integer getMinPlus(BPlusTreeNode<Integer> node) {
        while (!node.isLeaf()) node = node.child[0];
        return (Integer) node.keys[0];
    }

    private static Integer getMaxPlus(BPlusTreeNode<Integer> node) {
        while (!node.isLeaf()) node = node.child[node.keyCount];
        return (Integer) node.keys[node.keyCount - 1];
    }

    @Override
    protected void runScenario(NavigableSet<Integer> tree, List<Integer> initial, List<Action> actions) {
        NavigableSet<Integer> reference = new TreeSet<>();

        for (int v : initial) {
            Assertions.assertEquals(reference.add(v), tree.add(v), "initial add return mismatch for " + v);
        }
        verify("after initial load", tree, reference, 0);
        checkInvariants(tree);

        for (Action action : actions) {
            action.run(tree, reference);
            Assertions.assertEquals(reference.size(), tree.size(), "Size mismatch after " + action);
            Assertions.assertEquals(reference.isEmpty(), tree.isEmpty(), "isEmpty mismatch after " + action);
            checkInvariants(tree);
        }
        verify("final state", tree, reference, 0);
        Assertions.assertEquals(reference.comparator(), tree.comparator(), "comparator");
    }

    @Property(tries = 1000)
    void bTreeMatchesTreeSet(
            @ForAll("degrees") int degree,
            @ForAll("initialLoad") List<Integer> initial,
            @ForAll("actions") List<Action> actions) {
        runScenario(new BTreeSet<>(degree), initial, actions);
    }

    @Property(tries = 1000)
    void bPlusTreeMatchesTreeSet(
            @ForAll("degrees") int degree,
            @ForAll("initialLoad") List<Integer> initial,
            @ForAll("actions") List<Action> actions) {
        runScenario(new BPlusTreeSet<>(degree), initial, actions);
    }

    @Property(tries = 1000)
    void bTreeEdgeCases(@ForAll @IntRange(min = 3, max = 128) int degree) {
        edgeCases(() -> new BTreeSet<>(degree));
    }

    @Property(tries = 1000)
    void bPlusTreeEdgeCases(@ForAll @IntRange(min = 3, max = 128) int degree) {
        edgeCases(() -> new BPlusTreeSet<>(degree));
    }

    @Property(tries = 1000)
    void bTreeFailFastIterators(@ForAll @IntRange(min = 3, max = 128) int degree) {
        failFast(() -> new BTreeSet<>(degree));
    }

    @Property(tries = 1000)
    void bPlusTreeFailFastIterators(@ForAll @IntRange(min = 3, max = 128) int degree) {
        failFast(() -> new BPlusTreeSet<>(degree));
    }

    @Property
    void testCloneEdgeCases(@ForAll @IntRange(min = 3, max = 128) int degree,
            @ForAll @IntRange(min = 1, max = 10000) int size) {
        BTreeSet<Integer> emptyBt = new BTreeSet<>(degree);
        Assertions.assertEquals(emptyBt, emptyBt.clone());
        Assertions.assertTrue(((BTreeSet<Integer>) emptyBt.clone()).isEmpty());
        
        BPlusTreeSet<Integer> emptyBpt = new BPlusTreeSet<>(degree);
        Assertions.assertEquals(emptyBpt, emptyBpt.clone());
        Assertions.assertTrue(((BPlusTreeSet<Integer>) emptyBpt.clone()).isEmpty());

        BTreeSet<Integer> bt = new BTreeSet<>(degree);
        BPlusTreeSet<Integer> bpt = new BPlusTreeSet<>(degree);
        for (int i = 0; i < size; i++) {
            bt.add(i);
            bpt.add(i);
        }
        Assertions.assertEquals(bt, bt.clone());
        Assertions.assertNotNull(bt.display());

        Assertions.assertEquals(bpt, bpt.clone());
        Assertions.assertNotNull(bpt.display());
    }

    @Property
    void testBulkLoadAndRemoveCoverage(@ForAll @IntRange(min = 10, max = 1000) int size) {
        SortedSet<Integer> sm = new TreeSet<>();
        for (int i = 0; i < size; i++) sm.add(i);
        
        BTreeSet<Integer> bt = new BTreeSet<>(sm);
        Assertions.assertEquals(size, bt.size());
        
        BPlusTreeSet<Integer> bpt = new BPlusTreeSet<>(sm);
        Assertions.assertEquals(size, bpt.size());
        
        Iterator<Integer> it = bpt.iterator();
        while(it.hasNext()) {
            it.next();
            it.remove();
        }
        Assertions.assertTrue(bpt.isEmpty());

        Iterator<Integer> revIt = bt.descendingIterator();
        while(revIt.hasNext()) {
            revIt.next();
            revIt.remove();
        }
        Assertions.assertTrue(bt.isEmpty());
    }

    @Property
    void testBulkLoadExceptions(@ForAll @IntRange(min = 32, max = 128) int degree,
            @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor) {
        BTreeSet<Integer> bt = new BTreeSet<>(degree);
        BPlusTreeSet<Integer> bpt = new BPlusTreeSet<>(degree);
        
        Object[] keys = new Object[]{1, 2, 3};
        
        Assertions.assertDoesNotThrow(() -> new BPlusTreeSet<Integer>(degree).importFlatArray(null, factor));
        Assertions.assertDoesNotThrow(() -> new BTreeSet<Integer>(degree).importFlatArray(null, factor));
        
        Assertions.assertDoesNotThrow(() -> new BPlusTreeSet<Integer>(degree).importFlatArray(new Object[0], factor));
        Assertions.assertDoesNotThrow(() -> new BTreeSet<Integer>(degree).importFlatArray(new Object[0], factor));
        
        bt.add(5);
        Assertions.assertThrows(IllegalStateException.class, () -> bt.importFlatArray(keys, factor));
        
        bpt.add(5);
        Assertions.assertThrows(IllegalStateException.class, () -> bpt.importFlatArray(keys, factor));
    }

    @Property
    void testBulkLoadInvalidFactor(@ForAll @IntRange(min = 32, max = 128) int degree,
            @ForAll("invalidFactors") float factor) {
        Object[] keys = new Object[]{1, 2, 3};
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BTreeSet<Integer>(degree).importFlatArray(keys, factor));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BPlusTreeSet<Integer>(degree).importFlatArray(keys, factor));
    }

    @Property
    void testSingleElementBulkLoad(@ForAll @IntRange(min = 32, max = 128) int degree,
            @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor) {
        Object[] singleElement = new Object[]{42};
        BTreeSet<Integer> bt = new BTreeSet<>(degree);
        try {
            bt.importFlatArray(singleElement, factor);
            Assertions.assertEquals(1, bt.size());
            Assertions.assertTrue(bt.contains(42));
        } catch (IllegalStateException e) {
        }

        BPlusTreeSet<Integer> bpt = new BPlusTreeSet<>(degree);
        try {
            bpt.importFlatArray(singleElement, factor);
            Assertions.assertEquals(1, bpt.size());
            Assertions.assertTrue(bpt.contains(42));
        } catch (IllegalStateException e) {
        }
    }

    @Property
    void testConstructorExceptions(@ForAll @IntRange(min = -100, max = 2) int invalidDegree) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BTreeSet<>(invalidDegree));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BPlusTreeSet<>(invalidDegree));
    }

    @Property
    void testBuilderExceptions(
            @ForAll @IntRange(min = -100, max = 1) int invalidDegree,
            @ForAll("invalidFactors") float invalidFactor) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> BTreeSet.Builder.create(invalidDegree));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BPlusTreeSet.Builder.create(invalidDegree));
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> BTreeSet.Builder.newBuilder().factor(invalidFactor));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BPlusTreeSet.Builder.newBuilder().factor(invalidFactor));
    }

    @Property
    void testConstructors(@ForAll @IntRange(min = 3, max = 128) int degree) {
        List<Integer> list = Arrays.asList(1, 2, 3);
        SortedSet<Integer> sorted = new TreeSet<>(list);
        
        Assertions.assertNotNull(new BTreeSet<>(Comparator.naturalOrder()));
        Assertions.assertEquals(3, new BTreeSet<>(list).size());
        Assertions.assertEquals(3, new BTreeSet<>(sorted).size());
        Assertions.assertNotNull(new BTreeSet<>(degree, null));
        
        Assertions.assertNotNull(new BPlusTreeSet<>(Comparator.naturalOrder()));
        Assertions.assertEquals(3, new BPlusTreeSet<>(list).size());
        Assertions.assertEquals(3, new BPlusTreeSet<>(sorted).size());
        Assertions.assertNotNull(new BPlusTreeSet<>(degree, null));
    }

    @Property
    void testSubSetExceptions(@ForAll @IntRange(min = 3, max = 128) int degree) {
        verifySubSetExceptions(new BTreeSet<>(degree));
        verifySubSetExceptions(new BPlusTreeSet<>(degree));
    }

    private void verifySubSetExceptions(NavigableSet<Integer> set) {
        set.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        
        NavigableSet<Integer> sub = set.subSet(3, true, 8, true);

        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subSet(1, true, 5, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subSet(5, true, 10, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.headSet(10, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.tailSet(1, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subSet(6, true, 5, true));

        NavigableSet<Integer> descSub = sub.descendingSet();
        Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.subSet(5, true, 6, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.headSet(1, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.tailSet(10, true));
    }

    @Property
    void testFailFastDescendingIterators(@ForAll @IntRange(min = 3, max = 128) int degree) {
        verifyFailFastDescendingIterator(new BTreeSet<>(degree));
        verifyFailFastDescendingIterator(new BPlusTreeSet<>(degree));
    }

    private void verifyFailFastDescendingIterator(NavigableSet<Integer> set) {
        set.addAll(Arrays.asList(1, 2, 3, 4, 5));
        Iterator<Integer> it = set.descendingIterator();
        set.add(6);
        Assertions.assertThrows(java.util.ConcurrentModificationException.class, it::next);
    }

    @Property
    void testFailFastSubSetIterators(@ForAll @IntRange(min = 3, max = 128) int degree) {
        verifyFailFastSubSetIterators(new BTreeSet<>(degree));
        verifyFailFastSubSetIterators(new BPlusTreeSet<>(degree));
    }

    private void verifyFailFastSubSetIterators(NavigableSet<Integer> set) {
        set.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        NavigableSet<Integer> sub = set.subSet(2, true, 6, true);
        
        Iterator<Integer> subIt = sub.iterator();
        set.add(9);
        Assertions.assertThrows(java.util.ConcurrentModificationException.class, subIt::next);
        
        Iterator<Integer> descSubIt = sub.descendingIterator();
        set.remove(1);
        Assertions.assertThrows(java.util.ConcurrentModificationException.class, descSubIt::next);
    }

    @Provide
    Arbitrary<Float> invalidFactors() {
        return Arbitraries.oneOf(
                Arbitraries.floats().lessOrEqual(0.49f),
                Arbitraries.floats().greaterOrEqual(1.01f)
        );
    }
}

package chaos.tree.naryMap;

import chaos.tree.AbstractNavigableMapApiTest;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.FloatRange;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.ConcurrentModificationException;

public class NaryMapApiTest extends AbstractNavigableMapApiTest {

    private static void checkInvariants(NavigableMap<Integer, Integer> tree) {
        if (tree instanceof BTreeMap) {
            validateBTree((BTreeMap<Integer, Integer>) tree);
        } else if (tree instanceof BPlusTreeMap) {
            validateBPlusTree((BPlusTreeMap<Integer, Integer>) tree);
        }
    }

    private static void validateBTree(BTreeMap<Integer, Integer> tree) {
        if (tree.root == null) {
            if (tree.size() != 0) throw new AssertionError("Root is null but size is " + tree.size());
            return;
        }
        int expectedDepth = -1;
        int sizeCount = validateBTreeNode(tree.root, true, 0, new int[]{expectedDepth}, tree.degree, tree);
        if (sizeCount != tree.size()) throw new AssertionError("Size mismatch: tracked=" + tree.size() + " actual=" + sizeCount);
    }

    private static int validateBTreeNode(BTreeMapNode<Integer, Integer> node, boolean isRoot, int depth, int[] expectedDepth, int degree, BTreeMap<Integer, Integer> tree) {
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
                BTreeMapNode<Integer, Integer> child = node.child[i];
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

    private static Integer getMin(BTreeMapNode<Integer, Integer> node) {
        while (!node.isLeaf()) node = node.child[0];
        return (Integer) node.keys[0];
    }

    private static Integer getMax(BTreeMapNode<Integer, Integer> node) {
        while (!node.isLeaf()) node = node.child[node.keyCount];
        return (Integer) node.keys[node.keyCount - 1];
    }

    private static void validateBPlusTree(BPlusTreeMap<Integer, Integer> tree) {
        if (tree.root == null) {
            if (tree.size() != 0) throw new AssertionError("Root is null but size is " + tree.size());
            return;
        }
        int expectedDepth = -1;
        int sizeCount = validateBPlusTreeNode(tree.root, true, 0, new int[]{expectedDepth}, tree.degree, tree);
        if (sizeCount != tree.size()) throw new AssertionError("Size mismatch: tracked=" + tree.size() + " actual=" + sizeCount);
        
        BPlusTreeMapNode<Integer, Integer> curr = tree.root;
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

    private static int validateBPlusTreeNode(BPlusTreeMapNode<Integer, Integer> node, boolean isRoot, int depth, int[] expectedDepth, int degree, BPlusTreeMap<Integer, Integer> tree) {
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
                BPlusTreeMapNode<Integer, Integer> child = (BPlusTreeMapNode<Integer, Integer>) node.child[i];
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

    private static Integer getMinPlus(BPlusTreeMapNode<Integer, Integer> node) {
        while (!node.isLeaf()) node = node.child[0];
        return (Integer) node.keys[0];
    }

    private static Integer getMaxPlus(BPlusTreeMapNode<Integer, Integer> node) {
        while (!node.isLeaf()) node = node.child[node.keyCount];
        return (Integer) node.keys[node.keyCount - 1];
    }

    @Override
    protected void runScenario(NavigableMap<Integer, Integer> tree, List<Integer> initial, List<Action> actions) {
        NavigableMap<Integer, Integer> reference = new TreeMap<>();

        for (int v : initial) {
            Integer oldRef = reference.put(v, v);
            Integer oldTree = tree.put(v, v);
            Assertions.assertEquals(oldRef, oldTree, "initial put return mismatch for " + v);
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
    void bTreeMatchesTreeMap(
            @ForAll("degrees") int degree,
            @ForAll("initialLoad") List<Integer> initial,
            @ForAll("actions") List<Action> actions) {
        runScenario(new BTreeMap<>(degree), initial, actions);
    }

    @Property(tries = 1000)
    void bPlusTreeMatchesTreeMap(
            @ForAll("degrees") int degree,
            @ForAll("initialLoad") List<Integer> initial,
            @ForAll("actions") List<Action> actions) {
        runScenario(new BPlusTreeMap<>(degree), initial, actions);
    }

    @Property(tries = 1000)
    void bTreeEdgeCases(@ForAll @IntRange(min = 3, max = 128) int degree) {
        edgeCases(() -> new BTreeMap<>(degree));
    }

    @Property(tries = 1000)
    void bPlusTreeEdgeCases(@ForAll @IntRange(min = 3, max = 128) int degree) {
        edgeCases(() -> new BPlusTreeMap<>(degree));
    }

    @Property(tries = 1000)
    void bTreeFailFastIterators(@ForAll @IntRange(min = 3, max = 128) int degree) {
        failFast(() -> new BTreeMap<>(degree));
    }

    @Property(tries = 1000)
    void bPlusTreeFailFastIterators(@ForAll @IntRange(min = 3, max = 128) int degree) {
        failFast(() -> new BPlusTreeMap<>(degree));
    }

    @Property(tries = 1000)
    void testCloneAndDisplay(@ForAll @IntRange(min = 3, max = 128) int degree,
                             @ForAll @IntRange(min = 1, max = 10000) int size) {
        BTreeMap<Integer, Integer> tree1 = new BTreeMap<>(degree);
        for (int i = 0; i < size; i++) {
            tree1.put(i, i);
        }
        Assertions.assertNotNull(tree1.display());
        Assertions.assertEquals(tree1, tree1.clone());
        
        BPlusTreeMap<Integer, Integer> tree2 = new BPlusTreeMap<>(degree);
        for (int i = 0; i < size; i++) {
            tree2.put(i, i);
        }
        Assertions.assertNotNull(tree2.display());
        Assertions.assertEquals(tree2, tree2.clone());
    }

    @Property
    void testCloneEmptyTrees(@ForAll @IntRange(min = 3, max = 128) int degree) {
        BTreeMap<Integer, Integer> bt = new BTreeMap<>(degree);
        Assertions.assertEquals(bt, bt.clone());
        Assertions.assertTrue(((BTreeMap<?,?>)bt.clone()).isEmpty());
        
        BPlusTreeMap<Integer, Integer> bpt = new BPlusTreeMap<>(degree);
        Assertions.assertEquals(bpt, bpt.clone());
        Assertions.assertTrue(((BPlusTreeMap<?,?>)bpt.clone()).isEmpty());
    }

    @Property
    void testSingleElementBulkLoad(@ForAll @IntRange(min = 3, max = 128) int degree) {
        SortedMap<Integer, Integer> sm = new TreeMap<>();
        sm.put(1, 1);
        
        BTreeMap<Integer, Integer> bt = new BTreeMap<>(sm);
        Assertions.assertEquals(1, bt.size());
        
        BPlusTreeMap<Integer, Integer> bpt = new BPlusTreeMap<>(sm);
        Assertions.assertEquals(1, bpt.size());
    }

    @Property
    void testBulkLoadAndRemoveCoverage(@ForAll @IntRange(min = 3, max = 128) int degree,
                                       @ForAll @IntRange(min = 10, max = 200) int size) {
        SortedMap<Integer, Integer> sm = new TreeMap<>();
        for (int i = 0; i < size; i++) sm.put(i, i);
        
        BTreeMap<Integer, Integer> bt = new BTreeMap<>(sm);
        Assertions.assertEquals(size, bt.size());
        
        BPlusTreeMap<Integer, Integer> bpt = new BPlusTreeMap<>(sm);
        Assertions.assertEquals(size, bpt.size());
        
        Iterator<Integer> it = bpt.keySet().iterator();
        while(it.hasNext()) {
            it.next();
            it.remove();
        }
        Assertions.assertTrue(bpt.isEmpty());

        Iterator<Integer> revIt = bt.descendingKeySet().iterator();
        while(revIt.hasNext()) {
            revIt.next();
            revIt.remove();
        }
        Assertions.assertTrue(bt.isEmpty());
    }

    @Property
    void testMatrixImportValidation(@ForAll @IntRange(min = 32, max = 128) int degree,
                                    @ForAll @FloatRange(min = 0.5f, max = 1.0f) float validFactor) {
        BTreeMap<Integer, Integer> bt = new BTreeMap<>(degree);
        BPlusTreeMap<Integer, Integer> bpt = new BPlusTreeMap<>(degree);
        
        Assertions.assertDoesNotThrow(() -> bpt.importFlatMatrix(new Object[0][0], validFactor));
        Assertions.assertDoesNotThrow(() -> bt.importFlatMatrix(new Object[0][0], validFactor));

        Assertions.assertThrows(IllegalArgumentException.class, () -> bpt.importFlatMatrix(new Object[1][0], validFactor));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bpt.importFlatMatrix(new Object[][] { new Object[]{1}, new Object[0] }, validFactor));

        Assertions.assertThrows(IllegalArgumentException.class, () -> bt.importFlatMatrix(new Object[1][0], validFactor));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bt.importFlatMatrix(new Object[][] { new Object[]{1}, new Object[0] }, validFactor));
        
        Object[][] matrix = new Object[][] { new Object[]{1}, new Object[]{1} };
        BPlusTreeMap<Integer, Integer> smallBpt = new BPlusTreeMap<>(16);
        Assertions.assertThrows(IllegalStateException.class, () -> smallBpt.importFlatMatrix(matrix, validFactor));
        BTreeMap<Integer, Integer> smallBt = new BTreeMap<>(16);
        Assertions.assertThrows(IllegalStateException.class, () -> smallBt.importFlatMatrix(matrix, validFactor));
    }

    @Property
    void testFactorExceptions(@ForAll @IntRange(min = 32, max = 128) int degree,
                              @ForAll @FloatRange(min = -10.0f, max = 0.49f) float lowFactor,
                              @ForAll @FloatRange(min = 1.01f, max = 10.0f) float highFactor) {
        Object[][] matrix = new Object[][] { new Object[]{1}, new Object[]{1} };
        BPlusTreeMap<Integer, Integer> bpt = new BPlusTreeMap<>(degree);
        BTreeMap<Integer, Integer> bt = new BTreeMap<>(degree);

        Assertions.assertThrows(IllegalArgumentException.class, () -> bpt.importFlatMatrix(matrix, lowFactor));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bpt.importFlatMatrix(matrix, highFactor));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bt.importFlatMatrix(matrix, lowFactor));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bt.importFlatMatrix(matrix, highFactor));
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> BTreeMap.Builder.newBuilder().factor(lowFactor));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BTreeMap.Builder.newBuilder().factor(highFactor));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BPlusTreeMap.Builder.newBuilder().factor(lowFactor));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BPlusTreeMap.Builder.newBuilder().factor(highFactor));
    }

    @Property
    void testBuilderDegreeExceptions(@ForAll @IntRange(min = -100, max = 1) int invalidDegree) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> BTreeMap.Builder.create(invalidDegree));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BPlusTreeMap.Builder.create(invalidDegree));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BTreeMap<>(invalidDegree));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BPlusTreeMap<>(invalidDegree));
    }

    @Property
    void testStateExceptionsOnNonEmpty(@ForAll @IntRange(min = 3, max = 128) int degree,
                                       @ForAll @FloatRange(min = 0.5f, max = 1.0f) float validFactor) {
        BPlusTreeMap<Integer, Integer> bpt = new BPlusTreeMap<>(degree);
        BTreeMap<Integer, Integer> bt = new BTreeMap<>(degree);
        
        bpt.put(5, 5);
        bt.put(5, 5);
        Object[][] matrix = new Object[][] { new Object[]{1}, new Object[]{1} };
        
        Assertions.assertThrows(IllegalStateException.class, () -> bpt.importFlatMatrix(matrix, validFactor));
        Assertions.assertThrows(IllegalStateException.class, () -> bt.importFlatMatrix(matrix, validFactor));
        
        SortedMap<Integer, Integer> sm = new TreeMap<>();
        sm.put(1, 1);
        Assertions.assertThrows(IllegalStateException.class, () -> bpt.buildFromSorted(sm.entrySet().iterator(), validFactor));
        Assertions.assertThrows(IllegalStateException.class, () -> bt.buildFromSorted(sm.entrySet().iterator(), validFactor));
    }

    @Property
    void testConstructors(@ForAll @IntRange(min = 3, max = 128) int degree) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        SortedMap<Integer, Integer> sorted = new TreeMap<>(map);
        
        new BTreeMap<>(Comparator.naturalOrder());
        new BTreeMap<>(map);
        new BTreeMap<>(sorted);
        new BTreeMap<>(degree, null);
        
        new BPlusTreeMap<>(Comparator.naturalOrder());
        new BPlusTreeMap<>(map);
        new BPlusTreeMap<>(sorted);
        new BPlusTreeMap<>(degree, null);
    }

    @Property
    void testSubSetExceptions(@ForAll @IntRange(min = 3, max = 128) int degree) {
        BTreeMap<Integer, Integer> bt = new BTreeMap<>(degree);
        for(int i = 1; i <= 10; i++) bt.put(i, i);
        
        NavigableMap<Integer, Integer> sub = bt.subMap(3, true, 8, true);
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subMap(1, true, 5, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subMap(5, true, 10, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.headMap(10, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.tailMap(1, true));
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subMap(6, true, 5, true));
        
        NavigableMap<Integer, Integer> descSub = sub.descendingMap();
        Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.subMap(5, true, 6, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.headMap(1, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.tailMap(10, true));

        NavigableMap<Integer, Integer> validDescSub = descSub.subMap(7, true, 4, true);
        Assertions.assertEquals(4, validDescSub.size());
    }

    @Property
    void testFailFastSubMapAndDescendingIterators(@ForAll @IntRange(min = 3, max = 128) int degree) {
        BTreeMap<Integer, Integer> bt = new BTreeMap<>(degree);
        for(int i = 0; i < 10; i++) bt.put(i, i);
        
        Iterator<Integer> descIt = bt.descendingKeySet().iterator();
        descIt.next();
        bt.put(100, 100);
        Assertions.assertThrows(ConcurrentModificationException.class, descIt::next);
        
        bt.remove(100);
        Iterator<Integer> subIt = bt.subMap(2, 8).keySet().iterator();
        subIt.next();
        bt.put(500, 500);
        Assertions.assertThrows(ConcurrentModificationException.class, subIt::next);

        BPlusTreeMap<Integer, Integer> bpt = new BPlusTreeMap<>(degree);
        for(int i = 0; i < 10; i++) bpt.put(i, i);
        
        Iterator<Integer> descIt2 = bpt.descendingKeySet().iterator();
        descIt2.next();
        bpt.put(100, 100);
        Assertions.assertThrows(ConcurrentModificationException.class, descIt2::next);
        
        bpt.remove(100);
        Iterator<Integer> subIt2 = bpt.subMap(2, 8).keySet().iterator();
        subIt2.next();
        bpt.put(500, 500);
        Assertions.assertThrows(ConcurrentModificationException.class, subIt2::next);
    }
}

package chaos.tree.naryMap;

import chaos.tree.AbstractNavigableMapApiTest;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
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
                             @ForAll @IntRange(min = 1000, max = 10000) int size)
     {
        BTreeMap<Integer, Integer> tree1 = new BTreeMap<Integer, Integer>(degree);
         for (int i = 0; i < size; i++) {
             tree1.put(i,i);
         }
        Assertions.assertNotNull(tree1.display());
        Assertions.assertEquals(tree1, tree1.clone());
        
        BPlusTreeMap<Integer, Integer> tree2 = new BPlusTreeMap<>(4);
         for (int i = 0; i < size; i++) {
             tree2.put(i,i);
         }
        Assertions.assertNotNull(tree2.display());
        Assertions.assertEquals(tree2, tree2.clone());
    }

    @Example
    void testBulkLoadAndRemoveCoverage() {
        SortedMap<Integer, Integer> sm = new TreeMap<>();
        for (int i = 0; i < 200; i++) sm.put(i, i);
        
        BTreeMap<Integer, Integer> bt = new BTreeMap<>(sm);
        Assertions.assertEquals(200, bt.size());
        
        BPlusTreeMap<Integer, Integer> bpt = new BPlusTreeMap<>(sm);
        Assertions.assertEquals(200, bpt.size());
        
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

    @Example
    void testBulkLoadExceptions() {
        BTreeMap<Integer, Integer> bt = new BTreeMap<>(32);
        BPlusTreeMap<Integer, Integer> bpt = new BPlusTreeMap<>(32);
        
        Object[][] matrix = new Object[][] { new Object[]{1}, new Object[]{1} };
        
        Assertions.assertDoesNotThrow(() -> bpt.importFlatMatrix(new Object[0][0], 0.75f));
        Assertions.assertDoesNotThrow(() -> bt.importFlatMatrix(new Object[0][0],0.75f));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bpt.importFlatMatrix(new Object[1][0], 0.75f));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bpt.importFlatMatrix(new Object[][] { new Object[]{1}, new Object[0] }, 0.75f));

        Assertions.assertThrows(IllegalArgumentException.class, () -> bt.importFlatMatrix(new Object[1][0], 0.75f));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bt.importFlatMatrix(new Object[][] { new Object[]{1}, new Object[0] }, 0.75f));
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> bpt.importFlatMatrix(matrix, 0.4f));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bpt.importFlatMatrix(matrix, 1.1f));
        
        BPlusTreeMap<Integer, Integer> smallBpt = new BPlusTreeMap<>(16);
        Assertions.assertThrows(IllegalStateException.class, () -> smallBpt.importFlatMatrix(matrix, 0.75f));
        BTreeMap<Integer, Integer> smallBt = new BTreeMap<>(16);
        Assertions.assertThrows(IllegalStateException.class, () -> smallBt.importFlatMatrix(matrix, 0.75f));
        
        bpt.put(5, 5);
        bt.put(5,5);
        Assertions.assertThrows(IllegalStateException.class, () -> bpt.importFlatMatrix(matrix, 0.75f));
        Assertions.assertThrows(IllegalStateException.class, () -> bt.importFlatMatrix(matrix, 0.75f));
    }


    @Example
    void testBuilderExceptions() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> BTreeMap.Builder.create(1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BPlusTreeMap.Builder.create(1));
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> BTreeMap.Builder.newBuilder().factor(0.4f));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BPlusTreeMap.Builder.newBuilder().factor(1.1f));
    }

    @Example
    void testConstructors() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        SortedMap<Integer, Integer> sorted = new TreeMap<>(map);
        
        new BTreeMap<>(Comparator.naturalOrder());
        new BTreeMap<>(map);
        new BTreeMap<>(sorted);
        new BTreeMap<>(58,null);
        
        new BPlusTreeMap<>(Comparator.naturalOrder());
        new BPlusTreeMap<>(map);
        new BPlusTreeMap<>(sorted);
        new BPlusTreeMap<>(58,null);
    }

    @net.jqwik.api.Example
    void testSubSetExceptions() {
        chaos.tree.naryMap.BTreeMap<Integer, Integer> bt = new chaos.tree.naryMap.BTreeMap<>(32);
        for(int i=1; i<=10; i++) bt.put(i, i);
        
        java.util.NavigableMap<Integer, Integer> sub = bt.subMap(3, true, 8, true);
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subMap(1, true, 5, true));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subMap(5, true, 10, true));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> sub.headMap(10, true));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> sub.tailMap(1, true));
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subMap(6, true, 5, true));
        
        java.util.NavigableMap<Integer, Integer> descSub = sub.descendingMap();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.subMap(5, true, 6, true));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.headMap(1, true));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.tailMap(10, true));
    }
}

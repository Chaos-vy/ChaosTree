package chaos.tree.nary;

import chaos.tree.AbstractNavigableSetApiTest;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;

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

    @Property(tries = 5000)
    void bTreeMatchesTreeSet(
            @ForAll("degrees") int degree,
            @ForAll("initialLoad") List<Integer> initial,
            @ForAll("actions") List<Action> actions) {
        runScenario(new BTreeSet<>(degree), initial, actions);
    }

    @Property(tries = 5000)
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

    @Property(tries = 1000)
    void testCloneAndDisplay(@ForAll @IntRange(min = 32, max = 256) int arrayDegree,
                             @ForAll @IntRange(min = 1000, max = 10000) int size) {
        BTreeSet<Integer> tree1 = new BTreeSet<>(arrayDegree);
        for (int i = 0; i < size; i++) {
            tree1.add(i);
        }
        Assertions.assertNotNull(tree1.display());
        Assertions.assertEquals(tree1, tree1.clone());
        
        BPlusTreeSet<Integer> tree2 = new BPlusTreeSet<>(4);
        for (int i = 0; i < size; i++) {
            tree2.add(i);
        }
        Assertions.assertNotNull(tree2.display());
        Assertions.assertEquals(tree2, tree2.clone());
    }

    @Example
    void testBulkLoadAndRemoveCoverage() {
        SortedSet<Integer> sm = new TreeSet<>();
        for (int i = 0; i < 200; i++) sm.add(i);
        
        BTreeSet<Integer> bt = new BTreeSet<>(sm);
        Assertions.assertEquals(200, bt.size());
        
        BPlusTreeSet<Integer> bpt = new BPlusTreeSet<>(sm);
        Assertions.assertEquals(200, bpt.size());
        
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
}

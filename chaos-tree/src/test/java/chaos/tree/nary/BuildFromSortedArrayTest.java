package chaos.tree.nary;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class BuildFromSortedArrayTest {

    private static Integer[] sortedRange(int n) {
        Integer[] a = new Integer[n];
        for (int i = 0; i < n; i++) a[i] = i;
        return a;
    }
    
    private List<Integer> getLeafScan(BTreeSet<Integer> tree) {
        List<Integer> list = new ArrayList<>();
        if (tree.root == null) return list;
        inorderAddLeaves(tree.root, list);
        return list;
    }
    
    private void inorderAddLeaves(BTreeNode<Integer> node, List<Integer> list) {
        if (node.isLeaf()) {
            for (int i = 0; i < node.keyCount; i++) {
                list.add((Integer) node.keys[i]);
            }
        } else {
            for (int i = 0; i < node.keyCount; i++) {
                if (node.child[i] != null) inorderAddLeaves(node.child[i], list);
                list.add((Integer) node.keys[i]);
            }
            if (node.child[node.keyCount] != null) inorderAddLeaves(node.child[node.keyCount], list);
        }
    }

    private void validate(BTreeSet<Integer> tree) {
        if (tree.root == null) return;
        validateNode(tree.root, tree);
    }
    
    private void validateNode(BTreeNode<Integer> node, BTreeSet<Integer> tree) {
        if (node != tree.root && node.keyCount < tree.minKeys) {
            throw new AssertionError("Underflow! Node has " + node.keyCount + " keys (min " + tree.minKeys + ")");
        }
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) {
                if (node.child[i] != null) {
                    validateNode(node.child[i], tree);
                }
            }
        }
    }

    @Test
    void exactMultipleOfTargetKeys_noOrphanLeaf() {
        BTreeSet<Integer> tree = new BTreeSet<>(32);
        int targetKeys = Math.max(tree.minKeys, (int) (tree.maxKeys * 1.0f)); 
        int n = targetKeys * 5;
        tree.importFlatArray(sortedRange(n), 1.0f);

        assertEquals(n, tree.size());
        assertEquals(n, getLeafScan(tree).size(), "no orphan/empty trailing leaf");
        validate(tree);
    }

    @Test
    void smallRemainder_forcesLeafBorrow() {
        BTreeSet<Integer> tree = new BTreeSet<>(32);
        int targetKeys = 63;
        int n = targetKeys * 3 + 20; 
        tree.importFlatArray(sortedRange(n), 1.0f);

        assertEquals(n, tree.size());
        List<Integer> scan = getLeafScan(tree);
        assertEquals(n, scan.size());
        for (int i = 0; i < n; i++) assertEquals(i, scan.get(i));
        validate(tree);
    }

    @Test
    void smallRemainder_withStarvedSibling_forcesMergeAndCascade() {
        BTreeSet<Integer> tree = new BTreeSet<>(32);
        float factor = 0.5f; 
        int n = 31 * 4 + 5;  
        tree.importFlatArray(sortedRange(n), factor);

        assertEquals(n, tree.size());
        List<Integer> scan = getLeafScan(tree);
        assertEquals(n, scan.size());
        for (int i = 0; i < n; i++) assertEquals(i, scan.get(i));
        validate(tree);
    }

    @Test
    void multiLevelCascade_inSingleStep() {
        BTreeSet<Integer> tree = new BTreeSet<>(32);
        int targetKeys = 63;
        int branching = targetKeys + 1;
        int leavesForDoubleFill = branching * branching;
        int n = leavesForDoubleFill * targetKeys;
        tree.importFlatArray(sortedRange(n), 1.0f);

        assertEquals(n, tree.size());
        assertFalse(tree.root.isLeaf(), "tree should have grown past a single leaf");
        List<Integer> scan = getLeafScan(tree);
        assertEquals(n, scan.size());
        for (int i = 0; i < n; i++) assertEquals(i, scan.get(i));
        validate(tree);
    }

    @Test
    void degreeBelowFloor_rejectedAtConstruction() {
        BTreeSet<Integer> tree = new BTreeSet<>(31);
        assertThrows(IllegalStateException.class, () -> tree.importFlatArray(sortedRange(1000), 1.0f));
    }

    @Test
    void degreeFloorInPractice_largeRealisticN_neverApproachesHeightLimit() {
        BTreeSet<Integer> tree = new BTreeSet<>(32);
        int n = 5_000_000;
        tree.importFlatArray(sortedRange(n), 1.0f);

        assertEquals(n, tree.size());
        assertEquals(n, getLeafScan(tree).size());
        validate(tree);
    }

    @Test
    void phase1OnlyChecksExactZero_missesPartialInternalNodeUnderflow() {
        BTreeSet<Integer> tree = new BTreeSet<>(32);
        int n = 8065;
        tree.importFlatArray(sortedRange(n), 1.0f);

        assertEquals(n, tree.size());
        assertEquals(n, getLeafScan(tree).size(), "reads are still correct despite the bug");

        // The bug has been fixed, so this should NOT throw an AssertionError.
        // It should validate cleanly.
        assertDoesNotThrow(() -> validate(tree), 
            "The underflow bug has been patched, so validation should succeed without throwing.");
    }
}

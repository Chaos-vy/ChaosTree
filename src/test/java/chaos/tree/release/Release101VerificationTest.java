package chaos.tree.release;

import chaos.tree.binary.AVL;
import chaos.tree.binary.RBT;
import chaos.tree.nary.BPlusTree;
import chaos.tree.nary.BTree;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Core regression tests for the v1.0.1 major architectural updates.
 * Verifies the O(1) hashCode rolling sum, ISearchTree value-equality,
 * lazy stream evaluation, and LCA bounds validation.
 */
public class Release101VerificationTest {

    @Test
    public void testO1RollingHashCodeAndValueEquality() {
        AVL<Integer> avl = new AVL<>();
        RBT<Integer> rbt = new RBT<>();
        BTree<Integer> btree = new BTree<>(4);
        BPlusTree<Integer> bplus = new BPlusTree<>(4);
        int[] insertOrderAVL = {50, 25, 75, 10, 30, 60, 90};
        int[] insertOrderRBT = {10, 25, 30, 50, 60, 75, 90};
        int[] insertOrderNary = {90, 75, 60, 50, 30, 25, 10};

        for (int i : insertOrderAVL) avl.insert(i);
        for (int i : insertOrderRBT) rbt.insert(i);
        for (int i : insertOrderNary) {
            btree.insert(i);
            bplus.insert(i);
        }

        assertEquals(avl, rbt, "AVL and RBT must be equal if holding same elements");
        assertEquals(btree, bplus, "BTree and BPlusTree must be equal if holding same elements");
        assertEquals(avl, btree, "Binary and Nary trees must be equal if holding same elements");
        assertEquals(avl.hashCode(), rbt.hashCode(), "Equal trees MUST have identical hash codes");
        assertEquals(btree.hashCode(), bplus.hashCode(), "Equal trees MUST have identical hash codes");
        assertEquals(avl.hashCode(), btree.hashCode(), "Binary and Nary must produce same hash for same data");

        avl.delete(50);
        rbt.delete(50);
        assertEquals(avl.hashCode(), rbt.hashCode(), "Hash codes must remain identical after synchronized deletions");
        assertNotEquals(avl.hashCode(), btree.hashCode(), "Hash codes must differ after diverging data");
    }

    @Test
    public void testLazyRangeStreamPruning() {
        RBT<Integer> tree = new RBT<>();
        for (int i = 1; i <= 1000; i++) {
            tree.insert(i);
        }

        Stream<Integer> stream = tree.rangeStream(500, 505);
        Object[] result = stream.toArray();
        
        assertEquals(5, result.length);
        assertEquals(500, result[0]);
        assertEquals(501, result[1]);
        assertEquals(502, result[2]);
        assertEquals(503, result[3]);
        assertEquals(504, result[4]);
    }

    @Test
    public void testLCAValidation() {
        AVL<Integer> tree = new AVL<>();
        tree.insert(10);
        tree.insert(5);
        tree.insert(15);

        assertEquals(10, tree.lca(5, 15));

        assertThrows(RuntimeException.class, () -> {
            tree.lca(5, 99);
        });
    }

    @Test
    public void testEmptyTreeRetainAll() {
        AVL<Integer> avl = new AVL<>();
        BTree<Integer> btree = new BTree<>(4);

        avl.retainAll(java.util.List.of(1, 2, 3));
        btree.retainAll(java.util.List.of(1, 2, 3));
        
        assertTrue(avl.isEmpty());
        assertTrue(btree.isEmpty());
    }

    @Test
    public void testNaryHeightStandardization() {
        BTree<Integer> btree = new BTree<>(4);
        btree.insert(10);
        assertEquals(0, btree.height());
    }
}

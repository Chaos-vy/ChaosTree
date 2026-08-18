package chaos.tree.binary;

import chaos.tree.traversal.TraversalType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BSTTest extends StableStructureContractTest<BST<Integer>> {

    @Override
    protected BST<Integer> createTree() {
        return new BST<>();
    }

    @Override
    protected BST<Integer> createFromIterable(Iterable<Integer> it) {
        return new BST<>(it);
    }

    @Override
    protected BST<Integer> createCopy(BST<Integer> src) {
        return new BST<>(src);
    }

    @Test
    void sortedInsertionMustDegradeToLinearSpine() {
        for (int i = 1; i <= 1000; i++) tree.insert(i);
        assertEquals(999, tree.height());
        assertEquals(List.of(1, 2, 3), tree.toList(TraversalType.LEVEL_ORDER).subList(0, 3));
    }

    @Test
    void deleteRootMustRestructureAndPreserveInorder() {
        tree.insertAll(List.of(20, 10, 30, 5, 15, 25, 35));
        assertEquals(20, tree.toList(TraversalType.LEVEL_ORDER).get(0));
        tree.delete(20);
        assertEquals(6, tree.size());
        assertEquals(List.of(5, 10, 15, 25, 30, 35), tree.inorder());
        assertNotEquals(20, tree.toList(TraversalType.LEVEL_ORDER).get(0));
    }
}
package chaos.tree.binary;

import chaos.tree.traversal.TraversalType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreapTest extends BinaryTreeContractTest<Treap<Integer>> {

    // Test here uses black box techniques since API is more locked it does exposes and have validation test
    @Override
    protected Treap<Integer> createTree() {
        return new Treap<>();
    }

    @Override
    protected Treap<Integer> createFromIterable(Iterable<Integer> it) {
        return new Treap<>(it);
    }

    @Override
    protected Treap<Integer> createCopy(Treap<Integer> src) {
        return new Treap<>(src);
    }


    @Test
    void deterministicSeedProducesIdenticalTopology() {
        // Two trees with the same seed must output the exact same Level-Order traversal
        Treap<Integer> treeA = new Treap<>(1337L, 5000);
        Treap<Integer> treeB = new Treap<>(1337L, 5000);

        List<Integer> inputs = List.of(50, 10, 80, 20, 30, 70, 90, 40, 60);
        treeA.insertAll(inputs);
        treeB.insertAll(inputs);

        assertEquals(treeA.toList(TraversalType.LEVEL_ORDER), treeB.toList(TraversalType.LEVEL_ORDER));
    }

    @Test
    void blackBoxValidationThroughProbabilisticBalancing() {
        // Black-Box Proof: If we insert 10,000 perfectly sorted elements,
        // a standard BST will have a height of 9,999.
        // A Treap uses internal random priorities to keep this bounded to ~O(log N).
        for (int i = 1; i <= 10_000; i++) {
            tree.insert(i);
        }

        // Must not degrade to linked list
        // Expected height for 10k nodes is ~28. 60 is an incredibly safe maximum bound.
        assertTrue(tree.height() < 60, "Treap failed to probabilistically balance the tree!");

        // Ensuring that it still maintained the strict BST ordering contract
        List<Integer> inorder = tree.inorder();
        for (int i = 1; i < inorder.size(); i++) {
            assertTrue(inorder.get(i - 1) < inorder.get(i));
        }
    }
}
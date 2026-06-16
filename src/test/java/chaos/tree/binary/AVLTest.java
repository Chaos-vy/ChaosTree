package chaos.tree.binary;

import chaos.tree.binary.avl.AVL;
import chaos.tree.traversal.TraversalType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AVLTest extends StableStructureContractTest<AVL<Integer>> {

    @Override
    protected AVL<Integer> createTree() {
        return new AVL<>();
    }

    @Override
    protected AVL<Integer> createFromIterable(Iterable<Integer> it) {
        return new AVL<>(it);
    }

    @Override
    protected AVL<Integer> createCopy(AVL<Integer> src) {
        return new AVL<>(src);
    }

    @Test
    void heightMustStrictlyObeyAVLMathematicalBounds() {
        for (int i = 1; i <= 100_000; i++) tree.insert(i);
        double theoreticalMaxHeight = 1.44 * (Math.log(100_000 + 2) / Math.log(2));
        assertTrue(tree.height() <= Math.ceil(theoreticalMaxHeight));
    }

    @Test
    void leftRightHeavyShouldTriggerDoubleRotation() {
        tree.insertAll(List.of(30, 10, 20)); // LR Imbalance
        assertEquals(20, tree.toList(TraversalType.LEVEL_ORDER).getFirst());
        assertEquals(List.of(10, 20, 30), tree.inorder());
    }
}
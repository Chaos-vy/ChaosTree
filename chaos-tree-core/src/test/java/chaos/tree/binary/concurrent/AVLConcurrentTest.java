package chaos.tree.binary.concurrent;

import chaos.tree.binary.AVL;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AVLConcurrentTest extends ConcurrentBinaryTreeTest<AVL<Integer>> {
    @Override
    protected AVL<Integer> createTree() {
        return new AVL<>();
    }

    @Override
    protected void validateInvariants() {
        if (!tree.isEmpty()) {
            double theoreticalMaxHeight = 1.44 * (Math.log(tree.size() + 2) / Math.log(2));
            assertTrue(tree.height() <= Math.ceil(theoreticalMaxHeight));
        }
    }
}
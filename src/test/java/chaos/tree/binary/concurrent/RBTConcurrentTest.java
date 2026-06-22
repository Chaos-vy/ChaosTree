package chaos.tree.binary.concurrent;

import chaos.tree.binary.RBT;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RBTConcurrentTest extends ConcurrentBinaryTreeTest<RBT<Integer>> {
    @Override
    protected RBT<Integer> createTree() {
        return new RBT<>();
    }

    @Override
    protected void validateInvariants() {
        assertTrue(tree.validateRBT());
    }
}
package chaos.tree.binary.concurrent;

import chaos.tree.binary.rbt.RBT;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RBTConcurrentTest extends ConcurrentBinaryTreeTest<RBT<Integer>> {
    @Override
    protected RBT<Integer> createTree() {
        return new RBT<>();
    }

    @Override
    protected void validateInvariants() {
        // Must maintain perfect Red-Black coloring rules after concurrent shredding
        assertTrue(tree.validateRBT());
    }
}
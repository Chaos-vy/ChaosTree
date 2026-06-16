package chaos.tree.binary.concurrent;

import chaos.tree.binary.treap.Treap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TreapConcurrentTest extends ConcurrentBinaryTreeTest<Treap<Integer>> {

    @Override
    protected Treap<Integer> createTree() {
        return new Treap<>();
    }

    @Override
    protected void validateInvariants() {
        if (tree.isEmpty()) return;

        // 1. Black-Box Check: Is it still a valid Binary Search Tree?
        List<Integer> inorder = tree.inorder();
        for (int i = 1; i < inorder.size(); i++) {
            assertTrue(inorder.get(i - 1) < inorder.get(i), "Concurrent modification broke BST ordering!");
        }

        // 2. Black-Box Check: Did the heap priorities keep it balanced?
        // Under heavy concurrent load, the tree should still avoid degenerate heights.
        assertTrue(tree.height() < 60, "Concurrent modification broke Treap priority balancing!");
    }
}
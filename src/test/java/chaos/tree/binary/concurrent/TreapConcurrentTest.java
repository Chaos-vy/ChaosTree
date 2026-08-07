package chaos.tree.binary.concurrent;

import chaos.tree.binary.Treap;

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

        List<Integer> inorder = tree.inorder();
        for (int i = 1; i < inorder.size(); i++) {
            assertTrue(inorder.get(i - 1) < inorder.get(i), "Concurrent modification broke BST ordering!");
        }

        assertTrue(tree.height() < 60, "Concurrent modification broke Treap priority balancing!");
    }
}
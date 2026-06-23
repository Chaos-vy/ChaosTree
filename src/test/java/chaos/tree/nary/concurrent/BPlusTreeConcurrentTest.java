package chaos.tree.nary.concurrent;

import chaos.tree.nary.BPlusTree;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BPlusTreeConcurrentTest extends ConcurrentNaryTreeTest<BPlusTree<Integer>> {
    @Override
    protected BPlusTree<Integer> createTree(int degree) {
        return new BPlusTree<>(degree);
    }

    @Override
    protected void validateInvariants(BPlusTree<Integer> tree) {

        List<Integer> actualElements = tree.toList();
        assertEquals(actualElements.size(), tree.size());
        for (int i = 0; i < actualElements.size() - 1; i++) {
            assertTrue(actualElements.get(i) < actualElements.get(i + 1));
        }
        if (!tree.isEmpty()) {
            assertTrue(tree.height() > 0);
        }
    }
}

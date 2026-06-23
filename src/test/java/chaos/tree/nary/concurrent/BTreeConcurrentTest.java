package chaos.tree.nary.concurrent;

import chaos.tree.nary.BTree;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BTreeConcurrentTest extends ConcurrentNaryTreeTest<BTree<Integer>> {
    @Override
    protected BTree<Integer> createTree(int degree) {
        return new BTree<>(degree);
    }

    @Override
    protected void validateInvariants(BTree<Integer> tree) {
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

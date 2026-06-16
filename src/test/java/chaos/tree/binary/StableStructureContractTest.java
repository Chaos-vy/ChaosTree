package chaos.tree.binary;

import chaos.tree.core.searchtree.binary.BinaryTree;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class StableStructureContractTest<TREE extends BinaryTree<Integer>>
        extends BinaryTreeContractTest<TREE> {
    @Test
    void lcaInSameSubtree() {
        tree.insertAll(List.of(50, 20, 80, 10, 30, 70, 90));
        assertEquals(20, tree.lca(10, 30));
    }


    @Test
    void lcaAcrossRoot() {
        tree.insertAll(List.of(50, 20, 80, 10, 30, 70, 90));
        assertEquals(50, tree.lca(10, 90));
    }

    @Test
    void lcaWhereOneNodeIsAncestor() {
        tree.insertAll(List.of(50, 20, 80, 10, 30));
        assertEquals(20, tree.lca(20, 30));
    }

}
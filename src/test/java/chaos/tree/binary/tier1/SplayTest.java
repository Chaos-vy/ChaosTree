package chaos.tree.binary;

import chaos.tree.traversal.TraversalType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SplayTest extends BinaryTreeContractTest<Splay<Integer>> {

    @Override
    protected Splay<Integer> createTree() {
        return new Splay<>();
    }

    @Override
    protected Splay<Integer> createFromIterable(Iterable<Integer> it) {
        return new Splay<>(it);
    }

    @Override
    protected Splay<Integer> createCopy(Splay<Integer> src) {
        return new Splay<>(src);
    }

    @Test
    void accessedElementBecomesRoot() {
        tree.insertAll(List.of(10, 20, 30, 40, 50));
        tree.contains(30);
        assertEquals(30, tree.toList(TraversalType.LEVEL_ORDER).get(0));
    }

    @Test
    void insertedElementBecomesRoot() {
        tree.insertAll(List.of(10, 20, 30));
        tree.insert(5);
        assertEquals(5, tree.toList(TraversalType.LEVEL_ORDER).get(0));
    }

    @Test
    void temporalLocalityRepeatedAccessRequiresNoRestructuring() {
        tree.insertAll(List.of(10, 20, 30));
        tree.contains(10);

        List<Integer> structureBefore = tree.toList(TraversalType.LEVEL_ORDER);
        tree.contains(10);
        List<Integer> structureAfter = tree.toList(TraversalType.LEVEL_ORDER);

        assertEquals(structureBefore, structureAfter);
    }
}
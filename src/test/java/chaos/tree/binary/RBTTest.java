package chaos.tree.binary;

import chaos.tree.binary.rbt.RBT;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RBTTest extends StableStructureContractTest<RBT<Integer>> {

    @Override
    protected RBT<Integer> createTree() {
        return new RBT<>();
    }

    @Override
    protected RBT<Integer> createFromIterable(Iterable<Integer> it) {
        return new RBT<>(it);
    }

    @Override
    protected RBT<Integer> createCopy(RBT<Integer> src) {
        return new RBT<>(src);
    }

    @Test
    void rootIsAlwaysBlack() {
        tree.insertAll(List.of(10, 20, 30));
        assertTrue(tree.validateRBT());
    }

    @Test
    void invariantAfterSortedInsert() {
        for (int i = 0; i < 10_000; i++) tree.insert(i);
        assertTrue(tree.validateRBT());
    }

    @Test
    void heightBoundHolds() {
        for (int i = 0; i < 100_000; i++) tree.insert(i);
        assertTrue(tree.height() < 34);
    }
}
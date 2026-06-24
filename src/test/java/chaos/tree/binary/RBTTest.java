package chaos.tree.binary;

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
    void heightBoundHolds() {
        for (int i = 0; i < 100_000; i++) tree.insert(i);
        assertTrue(tree.height() < 34);
    }
}
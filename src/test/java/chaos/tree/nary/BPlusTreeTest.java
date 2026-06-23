package chaos.tree.nary;

import static org.junit.jupiter.api.Assertions.*;

class BPlusTreeTest extends NaryTreeContractTest<BPlusTree<Integer>>{

    @Override
    protected BPlusTree<Integer> createTree(int degree) {
        return new BPlusTree<>(degree);
    }

    @Override
    protected BPlusTree<Integer> createFromIterable(int degree, Iterable<Integer> it) {
        return new BPlusTree<>(degree, it);
    }

    @Override
    protected BPlusTree<Integer> createCopy(BPlusTree<Integer> source) {
        return new BPlusTree<>(source);
    }
}
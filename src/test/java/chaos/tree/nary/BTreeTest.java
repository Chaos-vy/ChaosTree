package chaos.tree.nary;



class BTreeTest extends NaryTreeContractTest<BTree<Integer>> {


    @Override
    protected BTree<Integer> createTree(int degree) {
        return new BTree<>(degree);
    }

    @Override
    protected BTree<Integer> createFromIterable(int degree, Iterable<Integer> it) {
        return new BTree<>(degree, it);
    }

    @Override
    protected BTree<Integer> createCopy(BTree<Integer> src) {
        return new BTree<>(src);
    }
}
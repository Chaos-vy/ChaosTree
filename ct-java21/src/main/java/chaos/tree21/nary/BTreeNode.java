package chaos.tree21.nary;

final class BTreeNode<E> extends AbstractNaryNode<E, BTreeNode<E>> {
    @SuppressWarnings("unchecked")
    public BTreeNode(int degree, boolean isLeaf) {
        super(degree, isLeaf, isLeaf ? null : new BTreeNode[(degree << 1) + 1]);
    }
}

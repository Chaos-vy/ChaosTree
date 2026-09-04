package chaos.tree.nary;

final class BPlusTreeNode<E> extends AbstractNaryNode<E, BPlusTreeNode<E>> {

    // The specific B+Tree Linked-List capability!
    BPlusTreeNode<E> next;
    BPlusTreeNode<E> prev;

    @SuppressWarnings("unchecked")
    public BPlusTreeNode(int degree, boolean isLeaf) {
        super(degree, isLeaf ? null : new BPlusTreeNode[(degree << 1) + 1]);
    }
}

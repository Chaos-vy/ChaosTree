package chaos.tree21.nary;

/**
 * Hybrid B+Tree node design combining Lehman &amp; Yao's B-link technique
 * with CLRS's structural invariants.
 *
 * <p>Design features:
 * <ul>
 *   <li><b>Overflow slot (+1 array capacity):</b> permits insert-then-split
 *       rather than split-then-insert, so a node's key array never needs
 *       to be resized mid-traversal.</li>
 *   <li><b>Right-link (and prevSibling for reverse traversal):</li>
 *   <li><b>Parent pointer:</b> enables bottom-up split propagation without
 *       re-descending from the root.</li>
 * </ul>
 *
 * <p>Node capacity follows the CLRS B-Tree definition of minimum degree
 * {@code t}:
 * <ul>
 *   <li>Every node holds at most {@code 2t - 1} keys.</li>
 *   <li>Every non-root node holds at least {@code t - 1} keys.</li>
 *   <li>Every internal node has between {@code t} and {@code 2t} children.</li>
 * </ul>
 *
 */
abstract sealed class AbstractNaryNode<E, N extends AbstractNaryNode<E, N>> permits BPlusTreeNode, BTreeNode {

    // Arrays sized for +1 overflow room to prevent bounds checks during splitting
    protected final Object[] keys;
    protected final N[] child;

    protected int keyCount;
    protected N parent;

    protected AbstractNaryNode(int degree, boolean isLeaf, N[] child) {
        int maxKeys = degree << 1;
        this.keys = new Object[maxKeys];
        this.child = child;
        this.keyCount = 0;
    }

    @SuppressWarnings("unchecked")
    public void setChild(int index, N node) {
        child[index] = node;
        if (node != null) node.parent = (N) this;
    }

    public boolean isLeaf() {
        return child == null;
    }
}

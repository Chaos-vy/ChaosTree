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
public abstract sealed class AbstractNaryNode<E, N extends AbstractNaryNode<E, N>> implements NaryNode<E,N> permits BPlusTreeNode, BTreeNode {

    // Arrays sized for +1 overflow room to prevent bounds checks during splitting
    protected final Object[] keys;
    protected final N[] children;

    protected int keyCount;
    protected N parent;

    @SuppressWarnings("unchecked")
    protected AbstractNaryNode(int degree, boolean isLeaf) {
        int maxKeys = (degree << 1) - 1;
        // The +1 Overflow Optimization
        this.keys = new Object[maxKeys + 1];
        // Leaf nodes don't allocate children arrays to save RAM
        this.children = isLeaf ? null : (N[]) new AbstractNaryNode[maxKeys + 2];
        this.keyCount = 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E getKey(int index) {
        return (E) keys[index];
    }

    @Override
    public void setKey(int index, E key) {
        keys[index] = key;
    }

    @Override
    public N getChild(int index) {
        return children[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setChild(int index, N child) {
        children[index] = child;
        if (child != null) {
            child.parent = (N) this;
        }
    }

    @Override
    public void setKeyCount(int keyCount) {
        this.keyCount = keyCount;
    }

    @Override
    public void keyCount_DEC1() {
        this.keyCount--;
    }

    @Override
    public void keyCount_INC1() {
        this.keyCount++;
    }
}

package chaos.tree.naryMap;

/**
 * The dragons of chaos tree
 * Don't think this 20 line of code are dragons
 * But unfortunately yes, Each line here except blank lines LOL, is enough to
 * break the NaryTreeMap ecosystem.
 * @param <K> key
 * @param <V> value
 * @param <N> parent
 */
abstract sealed class AbstractNaryMapNode<K, V, N extends AbstractNaryMapNode<K, V, N>>
        permits BTreeMapNode, BPlusTreeMapNode {
    protected final Object[] keys;
    protected final Object[] values;
    protected final N[] child;
    protected int keyCount;
    protected N parent;

    protected AbstractNaryMapNode(int degree, N[] child, boolean isLeaf) {
        int maxKeys = degree << 1;
        this.keys = new Object[maxKeys];
        boolean needsValues = isLeaf || (this instanceof BTreeMapNode);
        this.values = needsValues ? new Object[maxKeys] : null;
        this.child = child;
        this.keyCount = 0;
    }

    @SuppressWarnings("unchecked")
    void setChild(int index, N node) {
        child[index] = node;
        if (node != null) node.parent = (N) this;
    }

    boolean isLeaf() {
        return child == null;
    }
}

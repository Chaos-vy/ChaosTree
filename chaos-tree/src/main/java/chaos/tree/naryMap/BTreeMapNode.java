package chaos.tree.naryMap;

final class BTreeMapNode<K, V> extends AbstractNaryMapNode<K, V, BTreeMapNode<K, V>> {

    @SuppressWarnings("unchecked")
    public BTreeMapNode(int degree, boolean isLeaf) {
        super(degree, isLeaf ? null : new BTreeMapNode[(degree << 1) + 1], isLeaf);
    }
}

package chaos.tree21.naryMap;

final class BPlusTreeMapNode<K, V> extends AbstractNaryMapNode<K, V, BPlusTreeMapNode<K, V>> {

    BPlusTreeMapNode<K, V> next;
    BPlusTreeMapNode<K, V> prev;

    @SuppressWarnings("unchecked")
    public BPlusTreeMapNode(int degree, boolean isLeaf) {
        super(degree, isLeaf ? null : new BPlusTreeMapNode[(degree << 1) + 1], isLeaf);
    }
}

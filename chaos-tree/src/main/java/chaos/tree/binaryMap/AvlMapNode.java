package chaos.tree.binaryMap;

final class AvlMapNode<K, V> extends AbstractBinaryMapNode<K, V, AvlMapNode<K, V>> {
    int height;

    AvlMapNode(K key, V value) {
        super(key, value);
    }
}

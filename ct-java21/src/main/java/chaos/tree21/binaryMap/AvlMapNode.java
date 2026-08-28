package chaos.tree21.binaryMap;

public final class AvlMapNode<K, V> extends AbstractBinaryMapNode<K, V, AvlMapNode<K, V>> {
    private int height;

    AvlMapNode(K key, V value) {
        super(key, value);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}

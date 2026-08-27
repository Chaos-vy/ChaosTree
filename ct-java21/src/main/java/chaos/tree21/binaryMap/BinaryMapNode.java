package chaos.tree21.binaryMap;

sealed interface BinaryMapNode<K, V, N extends BinaryMapNode<K, V, N>> permits AbstractBinaryMapNode {

    K getKey();

    void setKey();

    V getValue();

    void setValue(V value);

    N getLeft();

    void setLeft(N left);

    N getRight();

    void setRight(N right);

    N getParent();

    void setParent(N parent);
}

package chaos.tree21.binary;

sealed interface BinaryNode<E, N extends BinaryNode<E, N>> permits AbstractBinaryNode {

    E getValue();

    void setValue(E value);

    N getLeft();

    void setLeft(N left);

    N getRight();

    void setRight(N right);

    N getParent();

    void setParent(N parent);
}

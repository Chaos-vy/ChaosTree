package chaos.tree.binary;

final class RbtNode<E> extends AbstractBinaryNode<E, RbtNode<E>> {

    /*
    Removed ENUM for Object header with simple boolean hack
    RED === True === RED
    BLACK === False === BLACK
    Every New Node is default == RED ==
     */
    private boolean red;

    RbtNode(E value) {
        super(value);
        this.red = true;
    }

    boolean isRed() {
        return red;
    }

    boolean isBlack() {
        return !red;
    }

    void setRed() {
        this.red = true;
    }

    void setBlack() {
        this.red = false;
    }
}

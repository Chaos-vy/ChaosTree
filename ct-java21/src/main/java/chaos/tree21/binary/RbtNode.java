package chaos.tree21.binary;

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

    public boolean isRed() { return red; }
    public boolean isBlack() { return !red; }
    public void setRed() { this.red = true; }
    public void setBlack() { this.red = false; }
}

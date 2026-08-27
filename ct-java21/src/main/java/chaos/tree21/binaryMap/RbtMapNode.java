package chaos.tree21.binaryMap;

final class RbtMapNode<K, V> extends AbstractBinaryMapNode<K, V, RbtMapNode<K, V>> {
    /*
    RED === True === RED
    BLACK === False === BLACK
    Every New Node is default == RED ==
     */
    private boolean red;
    RbtMapNode(K key, V value) {
        super(key, value);
        this.red = true;
    }

    public boolean isRed() { return red; }
    public boolean isBlack() { return !red; }
    public void setRed() { this.red = true; }
    public void setBlack() { this.red = false; }
}

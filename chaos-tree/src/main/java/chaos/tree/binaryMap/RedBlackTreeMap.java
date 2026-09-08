package chaos.tree.binaryMap;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

public final class RedBlackTreeMap<K, V> extends AbstractBinaryTreeMap<K, V, RbtMapNode<K, V>> {


    public RedBlackTreeMap() {
        super();
    }

    public RedBlackTreeMap(Comparator<? super K> comparator) {
        super(comparator);
    }

    public RedBlackTreeMap(Map<? extends K, ? extends V> m) {
        super();
        putAll(m);
    }

    public RedBlackTreeMap(SortedMap<K, ? extends V> m) {
        buildFromSorted(m.size(), m.entrySet().iterator());
    }

    @Override
    void afterNodeBuiltFromSorted(RbtMapNode<K, V> node, int level, int redLevel) {
        if (level == redLevel) node.setRed();
        else node.setBlack();
    }

    @Override
    RbtMapNode<K, V> createNode(K key, V value) {
        return new RbtMapNode<>(key, value);
    }

    @Override
    void afterInsert(RbtMapNode<K, V> x) {
        while (x != null && x != root && x.parent.isRed()) {
            RbtMapNode<K, V> parent = x.parent;
            RbtMapNode<K, V> grandParent = parent.parent;
            if (parent == grandParent.left) {
                RbtMapNode<K, V> uncle = grandParent.right;
                if (uncle != null && uncle.isRed()) {
                    parent.setBlack();
                    uncle.setBlack();
                    grandParent.setRed();
                    x = grandParent;
                } else {
                    if (x == parent.right) {
                        x = parent;
                        super.rotateLeft(x);
                        parent = x.parent;
                    }
                    parent.setBlack();
                    grandParent.setRed();
                    super.rotateRight(grandParent);
                    break;
                }
            } else {
                RbtMapNode<K, V> uncle = grandParent.left;

                if (uncle != null && uncle.isRed()) {
                    parent.setBlack();
                    uncle.setBlack();
                    grandParent.setRed();
                    x = grandParent;
                } else {
                    if (x == parent.left) {
                        x = parent;
                        super.rotateRight(x);
                        parent = x.parent;
                    }
                    parent.setBlack();
                    grandParent.setRed();
                    super.rotateLeft(grandParent);
                    break;
                }
            }
        }
        root.setBlack();
    }

    private boolean isBlack(RbtMapNode<K, V> node) {
        return node == null || node.isBlack();
    }

    private boolean isRed(RbtMapNode<K, V> node) {
        return node != null && node.isRed();
    }

    @Override
    public V remove(Object o) {
        if (isEmpty()) return null;
        @SuppressWarnings("unchecked")
        K key = (K) o;

        RbtMapNode<K, V> x = nodeFinder(key);
        if (x == null) return null;
        V oldValue = x.getValue();
        if (x.left != null && x.right != null) {
            RbtMapNode<K, V> successor = x.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            x.setPair(successor.getKey(), successor.getValue());
            x = successor;
        }
        RbtMapNode<K, V> nodeReplacer = x.left != null ? x.left : x.right;
        boolean deletedNodeWasBlack = x.isBlack();
        if (nodeReplacer != null) {
            nodeReplacer.parent = x.parent;
            if (x.parent == null) {
                root = nodeReplacer;
            } else if (x == x.parent.left) {
                x.parent.left = nodeReplacer;
            } else {
                x.parent.right = nodeReplacer;
            }
            if (deletedNodeWasBlack) {
                fixDoubleBlack(nodeReplacer);
            }
        } else if (x.parent == null) {
            root = null; // The tree is now empty
        } else {
            if (deletedNodeWasBlack) {
                fixDoubleBlack(x);
            }

            if (x == x.parent.left) {
                x.parent.left = null;
            } else {
                x.parent.right = null;
            }
            x.parent = null;
        }
        x.left = null;
        x.right = null;
        x.parent = null;

        size--;
        modCount++;
        return oldValue;
    }

    private void fixDoubleBlack(RbtMapNode<K, V> x) {
        while (x != root && isBlack(x)) {
            RbtMapNode<K, V> parent = x.parent;

            if (x == parent.left) {
                RbtMapNode<K, V> sibling = parent.right;

                if (isRed(sibling)) {
                    sibling.setBlack();
                    parent.setRed();
                    super.rotateLeft(parent);
                    sibling = parent.right; // Update sibling after rotation
                }

                if (isBlack(sibling.left) && isBlack(sibling.right)) {
                    sibling.setRed();
                    x = parent;
                } else {

                    if (isBlack(sibling.right)) {
                        if (sibling.left != null) sibling.left.setBlack();
                        sibling.setRed();
                        super.rotateRight(sibling);
                        sibling = parent.right;
                    }

                    if (parent.isRed()) sibling.setRed();
                    else sibling.setBlack();

                    parent.setBlack();
                    if (sibling.right != null) sibling.right.setBlack();
                    super.rotateLeft(parent);

                    break;
                }
            } else {

                RbtMapNode<K, V> sibling = parent.left;

                if (isRed(sibling)) {
                    sibling.setBlack();
                    parent.setRed();
                    super.rotateRight(parent);
                    sibling = parent.left;
                }

                if (isBlack(sibling.right) && isBlack(sibling.left)) {
                    sibling.setRed();
                    x = parent;
                } else {
                    if (isBlack(sibling.left)) {
                        if (sibling.right != null) sibling.right.setBlack();
                        sibling.setRed();
                        super.rotateLeft(sibling);
                        sibling = parent.left;
                    }

                    if (parent.isRed()) sibling.setRed();
                    else sibling.setBlack();

                    parent.setBlack();
                    if (sibling.left != null) sibling.left.setBlack();
                    super.rotateRight(parent);

                    break;
                }
            }
        }
        if (x != null) {
            x.setBlack();
        }
    }
}

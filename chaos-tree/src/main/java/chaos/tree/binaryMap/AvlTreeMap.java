package chaos.tree.binaryMap;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

public final class AvlTreeMap<K, V> extends AbstractBinaryTreeMap<K, V, AvlMapNode<K, V>> {

    public AvlTreeMap() {
        super();
    }

    public AvlTreeMap(Comparator<? super K> comparator) {
        super(comparator);
    }

    public AvlTreeMap(Map<? extends K, ? extends V> m) {
        super();
        putAll(m);
    }

    public AvlTreeMap(SortedMap<K, ? extends V> m) {
        buildFromSorted(m.size(), m.entrySet().iterator());
    }

    @Override
    void afterNodeBuiltFromSorted(AvlMapNode<K, V> node, int level, int redLevel) {
        int leftHeight = nodeHeight(node.left);
        int rightHeight = nodeHeight(node.right);
        node.height = Math.max(leftHeight, rightHeight) + 1;
    }

    @Override
    AvlMapNode<K, V> createNode(K key, V value) {
        return new AvlMapNode<>(key, value);
    }

    @Override
    void afterInsert(AvlMapNode<K, V> node) {
        if (node != null) {
            node = node.parent;
        }
        balanceNode(node);
    }

    private void balanceNode(AvlMapNode<K, V> node) {
        while (node != null) {
            int oldHeight = node.height;
            updateHeight(node);
            int balance = nodeHeight(node.left) - nodeHeight(node.right);
            if (balance > 1) {
                if (nodeHeight(node.left.left) >= nodeHeight(node.left.right)) {
                    node = rotateRightAVL(node);
                } else {
                    rotateLeftAVL(node.left);
                    node = rotateRightAVL(node);
                }
            } else if (balance < -1) {
                if (nodeHeight(node.right.right) >= nodeHeight(node.right.left)) {
                    node = rotateLeftAVL(node);
                } else {
                    rotateRightAVL(node.right);
                    node = rotateLeftAVL(node);
                }
            }
            if (oldHeight == node.height) {
                break;
            }
            node = node.parent;
        }
    }

    private AvlMapNode<K, V> rotateRightAVL(AvlMapNode<K, V> p) {
        AvlMapNode<K, V> newRoot = p.left;
        super.rotateRight(p);
        updateHeight(p);
        updateHeight(newRoot);
        return newRoot;
    }

    private AvlMapNode<K, V> rotateLeftAVL(AvlMapNode<K, V> p) {
        AvlMapNode<K, V> newRoot = p.right;
        super.rotateLeft(p);
        updateHeight(p);
        updateHeight(newRoot);
        return newRoot;
    }

    private void updateHeight(AvlMapNode<K, V> root) {
        root.height = 1 + Math.max(nodeHeight(root.left), nodeHeight(root.right));
    }

    private int nodeHeight(AvlMapNode<K, V> node) {
        return node == null ? -1 : node.height;
    }

    @Override
    public V remove(Object o) {
        if (isEmpty()) return null;
        @SuppressWarnings("unchecked")
        K key = (K) o;
        AvlMapNode<K, V> x = nodeFinder(key);
        if (x == null) return null;
        V oldValue = x.getValue();
        if (x.left != null && x.right != null) {
            AvlMapNode<K, V> successor = x.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            x.setPair(successor.getKey(), successor.getValue());
            x = successor;
        }

        AvlMapNode<K, V> nodeReplacer = x.left != null ? x.left : x.right;
        AvlMapNode<K, V> parentOfDeleted = x.parent;

        if (nodeReplacer != null) {
            nodeReplacer.parent = parentOfDeleted;
            if (parentOfDeleted == null) {
                root = nodeReplacer;
            } else if (x == parentOfDeleted.left) {
                parentOfDeleted.left = nodeReplacer;
            } else {
                parentOfDeleted.right = nodeReplacer;
            }
        } else if (parentOfDeleted == null) {
            root = null;
        } else {
            if (x == parentOfDeleted.left) {
                parentOfDeleted.left = null;
            } else {
                parentOfDeleted.right = null;
            }
        }
        if (parentOfDeleted != null) {
            fixUpFromBottom(parentOfDeleted);
        }

        x.left = null;
        x.right = null;
        x.parent = null;

        size--;
        modCount++;
        return oldValue;
    }

    private void fixUpFromBottom(AvlMapNode<K, V> parentOfDeleted) {
        balanceNode(parentOfDeleted);
    }
}

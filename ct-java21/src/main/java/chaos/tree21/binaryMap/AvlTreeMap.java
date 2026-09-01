package chaos.tree21.binaryMap;

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
            node = node.parent; // Skip the newly inserted leaf to allow optimization!
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
                break; // Safe early exit restored!
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

        // Guaranteed one child or none
        AvlMapNode<K, V> node_replacer = x.left != null ? x.left : x.right;
        AvlMapNode<K, V> parentOfDeleted = x.parent;

        if (node_replacer != null) {
            node_replacer.parent = parentOfDeleted;
            if (parentOfDeleted == null) {
                root = node_replacer;
            } else if (x == parentOfDeleted.left) {
                parentOfDeleted.left = node_replacer;
            } else {
                parentOfDeleted.right = node_replacer;
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
            fix_Up_from_bottom(parentOfDeleted);
        }

        size--;
        modCount++;
        return oldValue;
    }

    private void fix_Up_from_bottom(AvlMapNode<K, V> parentOfDeleted) {
        // Same bottom-up fix-up logic as insertion; this method only provides
        // the deletion-specific semantic name.
        balanceNode(parentOfDeleted);
    }
}

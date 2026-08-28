package chaos.tree21.binaryMap;

public final class AvlTreeMap<K, V> extends AbstractBinaryTreeMap<K, V, AvlMapNode<K, V>> {

    @Override
    protected AvlMapNode<K, V> createNode(K key, V value) {
        return new AvlMapNode<>(key, value);
    }

    @Override
    protected void afterInsert(AvlMapNode<K, V> node) {
        if (node != null) {
            node = node.getParent(); // Skip the newly inserted leaf to allow optimization!
        }
        balanceNode(node);
    }

    private void balanceNode(AvlMapNode<K, V> node) {
        while (node != null) {
            int oldHeight = node.getHeight();
            updateHeight(node);
            int balance = nodeHeight(node.getLeft()) - nodeHeight(node.getRight());
            if (balance > 1) {
                if (nodeHeight(node.getLeft().getLeft()) >= nodeHeight(node.getLeft().getRight())) {
                    node = rotateRightAVL(node);
                } else {
                    rotateLeftAVL(node.getLeft());
                    node = rotateRightAVL(node);
                }
            } else if (balance < -1) {
                if (nodeHeight(node.getRight().getRight()) >= nodeHeight(node.getRight().getLeft())) {
                    node = rotateLeftAVL(node);
                } else {
                    rotateRightAVL(node.getRight());
                    node = rotateLeftAVL(node);
                }
            }
            if (oldHeight == node.getHeight()) {
                break; // Safe early exit restored!
            }
            node = node.getParent();
        }
    }

    private AvlMapNode<K, V> rotateRightAVL(AvlMapNode<K, V> p) {
        AvlMapNode<K, V> newRoot = p.getLeft();
        super.rotateRight(p);
        updateHeight(p);
        updateHeight(newRoot);
        return newRoot;
    }

    private AvlMapNode<K, V> rotateLeftAVL(AvlMapNode<K, V> p) {
        AvlMapNode<K, V> newRoot = p.getRight();
        super.rotateLeft(p);
        updateHeight(p);
        updateHeight(newRoot);
        return newRoot;
    }

    public int height() {
        return nodeHeight(root);
    }

    private void updateHeight(AvlMapNode<K, V> root) {
        root.setHeight(1 + Math.max(nodeHeight(root.getLeft()), nodeHeight(root.getRight())));
    }

    private int nodeHeight(AvlMapNode<K, V> node) {
        return node == null ? -1 : node.getHeight();
    }

    @Override
    public V remove(Object o) {
        if (isEmpty()) return null;
        @SuppressWarnings("unchecked")
        K key = (K) o;
        AvlMapNode<K, V> x = nodeFinder(key);
        if (x == null) return null;
        V oldValue = x.getValue();
        cachedHashcode -= x.hashCode();
        if (x.getLeft() != null && x.getRight() != null) {
            AvlMapNode<K, V> successor = x.getRight();
            while (successor.getLeft() != null) {
                successor = successor.getLeft();
            }
            x.setPair(successor.getKey(), successor.getValue());
            x = successor;
        }

        // Guaranteed one child or none
        AvlMapNode<K, V> node_replacer = x.getLeft() != null ? x.getLeft() : x.getRight();
        AvlMapNode<K, V> parentOfDeleted = x.getParent();

        if (node_replacer != null) {
            node_replacer.setParent(parentOfDeleted);
            if (parentOfDeleted == null) {
                root = node_replacer;
            } else if (x == parentOfDeleted.getLeft()) {
                parentOfDeleted.setLeft(node_replacer);
            } else {
                parentOfDeleted.setRight(node_replacer);
            }
        } else if (parentOfDeleted == null) {
            root = null;
        } else {
            if (x == parentOfDeleted.getLeft()) {
                parentOfDeleted.setLeft(null);
            } else {
                parentOfDeleted.setRight(null);
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

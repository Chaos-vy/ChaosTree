package chaos.tree.core.searchtree.nary;

import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.nary.NaryTree;

import java.util.Objects;

public abstract class AbstractNaryTree<T extends Comparable<T>, N extends NaryNode<T, N>> implements NaryTree<T> {

    public final int degree;
    public final int minKeys;
    public final int maxKeys;
    protected N root;
    protected int size;

    protected AbstractNaryTree(int degree) {
        if (degree < 2) {
            throw new IllegalArgumentException("Degree must be at least 2");
        }
        this.degree = degree;
        this.maxKeys = 2 * degree - 1;
        this.minKeys = degree - 1;
        this.root = null;
        this.size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        this.size = 0;
        this.root = null;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public final int maxDegree() {
        return maxKeys + 1;
    }

    @Override
    public final int minDegree() {
        return minKeys + 1;
    }

    protected abstract N createNode(int degree, boolean isLeaf);

    private int findIndex(N node, T key) {
        int left = 0;
        int right = node.getKeyCount() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = node.getKey(mid).compareTo(key);
            if (cmp == 0) return mid;
            else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return left;
    }

    private void checkValue(T value) {
        Objects.requireNonNull(value);
    }

    @Override
    public void insert(T value) {
        checkValue(value);
        if (root == null) {
            root = createNode(degree, true);
            root.setKey(0, value);
            root.setKeyCount(1);
            size++;
            return;
        }

        if (root.getKeyCount() == maxKeys) {
            N newRoot = createNode(degree, false);
            newRoot.setChild(0, root);
            splitChild(newRoot, 0, root);
            root = newRoot;
            insertNonFull(this.root, value);
        }
    }

    protected void insertNonFull(N node, T value) {
        while (true) {
            int index = findIndex(node, value);

            if (index < node.getKeyCount() && value.compareTo(node.getKey(index)) == 0) {
                throw new DuplicateNodeException("Value already present in tree");
            }
            if (node.isLeaf()) {
                Object[] keys = node.getKeys();
                System.arraycopy(keys, index, keys, index + 1, node.getKeyCount() - index);
                node.setKey(index, value);
                node.setKeyCount(node.getKeyCount() + 1);
                size++;
                return;
            } else {
                N child = node.getChild(index);
                if (node.getKeyCount() == maxKeys) {
                    splitChild(node, index, child);
                    int cmp = value.compareTo(node.getKey(index));
                    if (cmp == 0) {
                        throw new DuplicateNodeException("Value already exists in the tree: " + value);
                    } else if (cmp > 0) {
                        child = node.getChild(index + 1);
                    }
                }
                node = child;
            }
        }
    }

    /**
     * Splits a full child node into two nodes, elevating the median key to the parent.
     *
     * @param parent the parent node receiving the elevated key
     * @param index  the index of the child array where the full node resides
     * @param child  the full child node being split
     */
    protected void splitChild(N parent, int index, N child) {
        N rightChild = createNode(degree, child.isLeaf());
        rightChild.setKeyCount(minKeys);

        System.arraycopy(child.getKeys(), degree, rightChild.getKeys(), 0, minKeys);

        if (!child.isLeaf()) {
            System.arraycopy(child.getChildren(), degree, rightChild.getChildren(), 0, degree);
        }
        child.setKeyCount(minKeys);

        System.arraycopy(parent.getChildren(), index + 1, parent.getChildren(), index + 2, parent.getKeyCount() - index);

        parent.setChild(index + 1, rightChild);
        System.arraycopy(parent.getKeys(), index, parent.getKeys(), index + 1, parent.getKeyCount() - index);


        //maybe removed after benchmark test
        parent.setKey(index, child.getKey(minKeys));
        for (int i = minKeys; i < maxKeys; i++) {
            child.setKey(i, null);
        }
        if (!child.isLeaf()) {
            for (int i = degree; i <= maxKeys; i++) {
                child.setChild(i, null);
            }
        }

        parent.setKeyCount(parent.getKeyCount() + 1);
    }


}
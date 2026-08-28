package chaos.tree21.binaryMap;

import chaos.tree21.core.SearchTreeMap;

import java.util.Comparator;
import java.util.Objects;

public sealed abstract class AbstractBinaryTreeMap<K, V, N extends AbstractBinaryMapNode<K, V, N>>
        implements SearchTreeMap<K, V> permits AvlTreeMap, RedBlackTreeMap {

    protected N root;
    protected int size;
    protected long modCount;
    protected int cachedHashcode =0; //rolling hashcode
    protected final Comparator<? super K> comparator;

    protected AbstractBinaryTreeMap() {
        this.comparator = null;
    }

    protected AbstractBinaryTreeMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    @SuppressWarnings("unchecked")
    protected int compare(K k1, K k2) {
        if (comparator != null) {
            return comparator.compare(k1, k2);
        }
        return ((Comparable<? super K>) k1).compareTo(k2);
    }

    /*
    Tree emptiness must be checked prior
     */
    protected N nodeFinder(K key) {
        N current = root;
        while (current != null) {
            int cmp = compare(key, current.getKey());
            if (cmp == 0) return current;
            current = cmp < 0 ? current.getLeft() : current.getRight();
        }
        return null;
    }
    /*
    Tree emptiness must be checked prior
     */
    protected N leftMostNode(){
        N current = root;
        while (current.getLeft() != null) current = current.getLeft();
        return current;
    }
    /*
    Tree emptiness must be checked prior
     */
    protected N rightMostNode(){
        N current = root;
        while (current.getRight() != null) current = current.getRight();
        return current;
    }
    protected abstract N createNode(K key, V value);

    protected void afterInsert(N node) {}

    @Override
    public V put(K key, V value) {
        if (root == null) {
            root = createNode(key, value);
            cachedHashcode+=root.hashCode();
            size = 1;
            modCount++;
            afterInsert(root);
            return null;
        }

        N parent = null;
        N current = root;
        int cmp = 0;

        while (current != null) {
            parent = current;
            cmp = compare(key, current.getKey());
            if (cmp == 0) {
                cachedHashcode -= current.hashCode();
                V oldValue = current.setValue(value);
                cachedHashcode += current.hashCode();
                return oldValue;
            }
            else if (cmp < 0) current = current.getLeft();
            else current = current.getRight();
        }
        N newNode = createNode(key, value);
        newNode.setParent(parent);

        if (cmp < 0) parent.setLeft(newNode);
        else parent.setRight(newNode);

        size++;
        modCount++;
        cachedHashcode += newNode.hashCode();
        afterInsert(newNode);
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
        modCount++;
        cachedHashcode = 0;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object o) {
        if(root == null) return false;
        try {
            @SuppressWarnings("unchecked")
            K k = (K) o;
            return nodeFinder(k).getKey()!=null;
        }
        catch (ClassCastException | NullPointerException oe){
            return false;
        }
    }

    @Override
    public boolean containsValue(Object value) {
        if (root == null) return false;
        N current = leftMostNode();
        while (current != null) {
            if (Objects.equals(value, current.getValue())) {
                return true;
            }
            current = successor(current);
        }
        return false;
    }

    protected N successor(N t) {
        if (t == null) return null;
        N p;
        if (t.getRight() != null) {
            p = t.getRight();
            while (p.getLeft() != null) {
                p = p.getLeft();
            }
        }
        else {
            p = t.getParent();
            N ch = t;
            while (p != null && ch == p.getRight()) {
                ch = p;
                p = p.getParent();
            }
        }
        return p;
    }
    protected N predecessor(N t) {
        if (t == null) return null;
        N p;
        if (t.getLeft() != null) {
            p = t.getLeft();
            while (p.getRight() != null) {
                p = p.getRight();
            }
        }
        else {
            p = t.getParent();
            N ch = t;
            while (p != null && ch == p.getLeft()) {
                ch = p;
                p = p.getParent();
            }
        }
        return p;
    }
}

package chaos.tree21.nary;

import chaos.tree21.core.SearchTreeSet;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Base Engine for B-Tree and B+Tree variants.
 * Fuses CLRS node arithmetic with Lehman & Yao concurrent/bottom-up memory layout.
 */
public non-sealed abstract class AbstractNaryTreeSet<E, N extends AbstractNaryNode<E, N>> implements SearchTreeSet<E> {

    protected final transient int degree;
    protected final transient int maxKeys;
    protected final transient int minKeys;
    protected transient N root;
    protected transient int size;
    protected transient long modCount;
    protected final Comparator<? super E> comparator;

    @SuppressWarnings("unchecked")
    protected int compare(E e1, E e2) {
        if (comparator != null) {
            return comparator.compare(e1, e2);
        }
        return ((Comparable<? super E>) e1).compareTo(e2);
    }

    protected AbstractNaryTreeSet(int degree, Comparator<? super E> comparator) {
        this.comparator = comparator;
        if (degree < 2 || degree > Integer.MAX_VALUE/2) {
            throw new IllegalArgumentException("Degree must be at least 2 and less than Integer.MAX_VALUE/2");
        }
        this.degree = degree;
        this.maxKeys = (degree << 1) - 1;
        this.minKeys = degree - 1;
    }

    protected abstract N createNode(int degree, boolean isLeaf);

    @SuppressWarnings("unchecked")
    protected int SearchNode(N node, E key) {
        if (node.keyCount < 12) { //actually faster.
            for (int i = 0; i < node.keyCount; i++) {
                int cmp = compare((E) node.keys[i], key);
                if (cmp == 0) return i;// Match found
                if (cmp > 0) return ~i;// Not found, insertion point is 'i' (Bitwise NOT to make it negative)
            }
            return ~node.keyCount;// Not found, belongs at the very end
        }
        // Arrays.binarySearch already returns ~insertionPoint for missing elements
        return Arrays.binarySearch((E[]) node.keys, 0, node.keyCount, key, comparator);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return root == null;
    }

    @Override
    public void clear() {
        this.root = null;
        this.size = 0;
        this.modCount++;
    }

}

package chaos.tree21.naryMap;

import chaos.tree21.core.SearchTreeMap;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

sealed abstract class AbstractNaryTreeMap<K, V, N extends AbstractNaryMapNode<K, V, N>>
        extends AbstractMap<K, V> implements SearchTreeMap<K, V> permits BTreeMap, BPlusTreeMap {

    protected final Comparator<? super K> comparator;

    protected final transient int degree;
    protected final transient int maxKeys;
    protected final transient int minKeys;
    protected transient N root;
    protected transient int size;
    protected transient long modCount;

    protected AbstractNaryTreeMap(int degree, Comparator<? super K> comparator){
        this.comparator = comparator;
        if (degree < 2 || degree > Integer.MAX_VALUE / 2) {
            throw new IllegalArgumentException("Degree must be at least 2 and less than Integer.MAX_VALUE/2");
        }
        this.degree = degree;
        this.maxKeys = (degree << 1) - 1;
        this.minKeys = degree - 1;
    }

    @SuppressWarnings("unchecked")
    protected int compare(K k1, K k2) {
        if (comparator != null) {
            return comparator.compare(k1, k2);
        }
        return ((Comparable<? super K>) k1).compareTo(k2);
    }

    @SuppressWarnings("unchecked")
    protected int searchNodeMap(N current, K k){
        if(current.keyCount < 12){
            for (int i = 0; i < current.keyCount; i++) {
                int cmp = compare((K)current.keys[i], k);
                if(cmp == 0) return i;
                if(cmp > 0) return ~i;
            }
            return ~current.keyCount;
        }
        return Arrays.binarySearch((K[]) current.keys, 0, current.keyCount, k, comparator);
    }

    protected int searchNodeMapValue(N current, V v){
        for (int i = 0; i < current.keyCount; i++){
            if(Objects.equals(current.values[i], v)) return i;
        }
        return -1;
    }

    abstract N createNode(int degree, boolean isLeaf);




}

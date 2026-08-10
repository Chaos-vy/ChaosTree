package chaos.tree.core.searchTreeMap.binary;

public abstract class ParentBiNodeMap<K extends Comparable<? super K>, V, N extends ParentBiNodeMap<K, V, N>> extends BiNodeMap<K, V, N> {

    private N parent;

    ParentBiNodeMap(K key, V value) {
        super(key, value);
    }

    public N getParent() {
        return parent;
    }

    public void setParent(N parent) {
        this.parent = parent;
    }
}

package chaos.tree21.core;

import java.util.NavigableMap;
import java.util.SequencedMap;

public interface SearchTreeMap<K, V> extends NavigableMap<K, V>, SequencedMap<K, V> {
    default String display() {
        return display(Style.ASCII);
    }

    String display(Style style);
}

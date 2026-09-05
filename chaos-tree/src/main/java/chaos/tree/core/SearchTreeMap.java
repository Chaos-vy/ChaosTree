package chaos.tree.core;

import java.util.NavigableMap;
import java.util.SequencedMap;

public interface SearchTreeMap<K, V> extends NavigableMap<K, V>, SequencedMap<K, V> {

    /**
     * This display is actual view of data being stored in the DS
     * this also helped me while seeing the test pass but architecture flaw
     * It supports ANSI if your terminal supports it. Currently, this feature is added
     * to only Nary Family Tree.
     *
     * @return UNICODE format of Tree Data Structure do see ct-examples.
     */
    default String display() {
        return display(Style.UNICODE);
    }

    String display(Style style);
}

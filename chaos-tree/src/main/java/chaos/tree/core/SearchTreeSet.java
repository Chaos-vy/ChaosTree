package chaos.tree.core;

import java.util.NavigableSet;
import java.util.SequencedSet;

public interface SearchTreeSet<E> extends NavigableSet<E>, SequencedSet<E> {

    /**
     * This display is actual view of data being stored in the DS
     * this also helped me while seeing the test pass but architecture flaw
     * It supports ANSI if your terminal supports it. Currently, this feature is added
     * to only Nary Family Tree.
     * @return UNICODE format of Tree Data Structure do see ct-examples.
     */
    default String display() {
        return display(Style.UNICODE);
    }

    String display(Style style);

}

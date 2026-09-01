package chaos.tree21.core;

import java.util.NavigableSet;
import java.util.SequencedSet;

public interface SearchTreeSet<E> extends NavigableSet<E>, SequencedSet<E> {

    default String display() {
        return display(Style.UNICODE);
    }

    String display(Style style);

}

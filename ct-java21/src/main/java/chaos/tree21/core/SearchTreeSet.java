package chaos.tree21.core;

import chaos.tree21.binary.AbstractBinaryTreeSet;
import chaos.tree21.nary.AbstractNaryTreeSet;
//import chaos.tree21.nary.AbstractNaryTreeSet;

import java.util.NavigableSet;
import java.util.SequencedSet;

public sealed interface SearchTreeSet<E> extends NavigableSet<E>, SequencedSet<E>
        permits AbstractBinaryTreeSet, AbstractNaryTreeSet {
    int height();

    String print();
}

package chaos.tree.binary;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.List;

public class WhiteBoxBinarySetTest extends AbstractBinarySetTest {

    @Property(tries = 5000)
    void avlTreeMaintainsInvariants(@ForAll @IntRange(min = 0, max = 1000) int initialSize, @ForAll("avlActions") List<Action<AvlTreeSet<Integer>>> actions) {
        AvlTreeSet<Integer> tree = new AvlTreeSet<>();
        for (int i = 0; i < initialSize; i++) {
            tree.add(i);
        }
        for (Action<AvlTreeSet<Integer>> action : actions) {
            tree = action.run(tree);
        }
        verifyAvlInvariants(tree.root);
    }

    @Property(tries = 5000)
    void redBlackTreeMaintainsInvariants(@ForAll @IntRange(min = 0, max = 1000) int initialSize, @ForAll("rbtActions") List<Action<RedBlackTreeSet<Integer>>> actions) {
        RedBlackTreeSet<Integer> tree = new RedBlackTreeSet<>();
        for (int i = 0; i < initialSize; i++) {
            tree.add(i);
        }
        for (Action<RedBlackTreeSet<Integer>> action : actions) {
            tree = action.run(tree);
        }
        verifyRbtInvariants(tree);
    }

    interface Action<T> {
        T run(T tree);
    }

    @Provide
    Arbitrary<List<Action<AvlTreeSet<Integer>>>> avlActions() {
        return Arbitraries.frequencyOf(
                Tuple.of(5, Arbitraries.integers().map(AddAvlAction::new)),
                Tuple.of(5, Arbitraries.integers().map(RemoveAvlAction::new))
        ).list();
    }

    private static class AddAvlAction implements Action<AvlTreeSet<Integer>> {
        private final int value;
        AddAvlAction(int value) { this.value = value; }
        @Override public AvlTreeSet<Integer> run(AvlTreeSet<Integer> tree) {
            tree.add(value);
            return tree;
        }
    }

    private static class RemoveAvlAction implements Action<AvlTreeSet<Integer>> {
        private final int value;
        RemoveAvlAction(int value) { this.value = value; }
        @Override public AvlTreeSet<Integer> run(AvlTreeSet<Integer> tree) {
            tree.remove(value);
            return tree;
        }
    }

    @Provide
    Arbitrary<List<Action<RedBlackTreeSet<Integer>>>> rbtActions() {
        return Arbitraries.frequencyOf(
                Tuple.of(5, Arbitraries.integers().map(AddRbtAction::new)),
                Tuple.of(5, Arbitraries.integers().map(RemoveRbtAction::new))
        ).list();
    }

    private static class AddRbtAction implements Action<RedBlackTreeSet<Integer>> {
        private final int value;
        AddRbtAction(int value) { this.value = value; }
        @Override public RedBlackTreeSet<Integer> run(RedBlackTreeSet<Integer> tree) {
            tree.add(value);
            return tree;
        }
    }

    private static class RemoveRbtAction implements Action<RedBlackTreeSet<Integer>> {
        private final int value;
        RemoveRbtAction(int value) { this.value = value; }
        @Override public RedBlackTreeSet<Integer> run(RedBlackTreeSet<Integer> tree) {
            tree.remove(value);
            return tree;
        }
    }
}

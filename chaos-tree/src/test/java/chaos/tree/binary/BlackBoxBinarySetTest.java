package chaos.tree.binary;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlackBoxBinarySetTest extends AbstractBinarySetTest {

    @Property(tries = 5000)
    void avlTreeMatchesHashSet(@ForAll @IntRange(min = 0, max = 1000) int initialSize, @ForAll("avlActions") List<Action<AvlTreeSet<Integer>, HashSet<Integer>>> actions) {
        AvlTreeSet<Integer> tree = new AvlTreeSet<>();
        HashSet<Integer> reference = new HashSet<>();
        
        for (int i = 0; i < initialSize; i++) {
            tree.add(i);
            reference.add(i);
        }
        
        for (Action<AvlTreeSet<Integer>, HashSet<Integer>> action : actions) {
            action.run(tree, reference);
            assertEquals(reference.size(), tree.size(), "Size mismatch");
            assertEquals(reference.isEmpty(), tree.isEmpty(), "isEmpty mismatch");

            verifyAvlInvariants(tree.root);
        }
    }

    @Property(tries = 5000)
    void redBlackTreeMatchesHashSet(@ForAll @IntRange(min = 0, max = 1000) int initialSize, @ForAll("rbtActions") List<Action<RedBlackTreeSet<Integer>, HashSet<Integer>>> actions) {
        RedBlackTreeSet<Integer> tree = new RedBlackTreeSet<>();
        HashSet<Integer> reference = new HashSet<>();
        
        for (int i = 0; i < initialSize; i++) {
            tree.add(i);
            reference.add(i);
        }
        
        for (Action<RedBlackTreeSet<Integer>, HashSet<Integer>> action : actions) {
            action.run(tree, reference);
            assertEquals(reference.size(), tree.size(), "Size mismatch");
            assertEquals(reference.isEmpty(), tree.isEmpty(), "isEmpty mismatch");

            verifyRbtInvariants(tree);
        }
    }

    interface Action<T extends Set<Integer>, R extends Set<Integer>> {
        void run(T tree, R reference);
    }

    @Provide
    Arbitrary<List<Action<AvlTreeSet<Integer>, HashSet<Integer>>>> avlActions() {
        return Arbitraries.frequencyOf(
                Tuple.of(5, Arbitraries.integers().map(AddAvlAction::new)),
                Tuple.of(5, Arbitraries.integers().map(RemoveAvlAction::new)),
                Tuple.of(2, Arbitraries.integers().map(ContainsAvlAction::new)),
                Tuple.of(1, Arbitraries.just(new ClearAvlAction()))
        ).list();
    }

    private static class AddAvlAction implements Action<AvlTreeSet<Integer>, HashSet<Integer>> {
        private final int value;
        AddAvlAction(int value) { this.value = value; }
        @Override public void run(AvlTreeSet<Integer> tree, HashSet<Integer> ref) {
            assertEquals(ref.add(value), tree.add(value), "Add return value mismatch");
        }
    }

    private static class RemoveAvlAction implements Action<AvlTreeSet<Integer>, HashSet<Integer>> {
        private final int value;
        RemoveAvlAction(int value) { this.value = value; }
        @Override public void run(AvlTreeSet<Integer> tree, HashSet<Integer> ref) {
            assertEquals(ref.remove(value), tree.remove(value), "Remove return value mismatch");
        }
    }

    private static class ContainsAvlAction implements Action<AvlTreeSet<Integer>, HashSet<Integer>> {
        private final int value;
        ContainsAvlAction(int value) { this.value = value; }
        @Override public void run(AvlTreeSet<Integer> tree, HashSet<Integer> ref) {
            assertEquals(ref.contains(value), tree.contains(value), "Contains return value mismatch");
        }
    }

    private static class ClearAvlAction implements Action<AvlTreeSet<Integer>, HashSet<Integer>> {
        @Override public void run(AvlTreeSet<Integer> tree, HashSet<Integer> ref) {
            tree.clear();
            ref.clear();
        }
    }

    @Provide
    Arbitrary<List<Action<RedBlackTreeSet<Integer>, HashSet<Integer>>>> rbtActions() {
        return Arbitraries.frequencyOf(
                Tuple.of(5, Arbitraries.integers().map(AddRbtAction::new)),
                Tuple.of(5, Arbitraries.integers().map(RemoveRbtAction::new)),
                Tuple.of(2, Arbitraries.integers().map(ContainsRbtAction::new)),
                Tuple.of(1, Arbitraries.just(new ClearRbtAction()))
        ).list();
    }

    private static class AddRbtAction implements Action<RedBlackTreeSet<Integer>, HashSet<Integer>> {
        private final int value;
        AddRbtAction(int value) { this.value = value; }
        @Override public void run(RedBlackTreeSet<Integer> tree, HashSet<Integer> ref) {
            assertEquals(ref.add(value), tree.add(value), "Add return value mismatch");
        }
    }

    private static class RemoveRbtAction implements Action<RedBlackTreeSet<Integer>, HashSet<Integer>> {
        private final int value;
        RemoveRbtAction(int value) { this.value = value; }
        @Override public void run(RedBlackTreeSet<Integer> tree, HashSet<Integer> ref) {
            assertEquals(ref.remove(value), tree.remove(value), "Remove return value mismatch");
        }
    }

    private static class ContainsRbtAction implements Action<RedBlackTreeSet<Integer>, HashSet<Integer>> {
        private final int value;
        ContainsRbtAction(int value) { this.value = value; }
        @Override public void run(RedBlackTreeSet<Integer> tree, HashSet<Integer> ref) {
            assertEquals(ref.contains(value), tree.contains(value), "Contains return value mismatch");
        }
    }

    private static class ClearRbtAction implements Action<RedBlackTreeSet<Integer>, HashSet<Integer>> {
        @Override public void run(RedBlackTreeSet<Integer> tree, HashSet<Integer> ref) {
            tree.clear();
            ref.clear();
        }
    }
}

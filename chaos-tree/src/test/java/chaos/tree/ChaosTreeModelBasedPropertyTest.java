package chaos.tree;

import chaos.tree.binary.AvlTreeSet;
import chaos.tree.binary.RedBlackTreeSet;
import chaos.tree.binaryMap.AvlTreeMap;
import chaos.tree.binaryMap.RedBlackTreeMap;
import chaos.tree.nary.BPlusTreeSet;
import chaos.tree.nary.BTreeSet;
import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.naryMap.BTreeMap;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.state.Action;
import net.jqwik.api.state.ActionChain;
import net.jqwik.api.state.ActionChainArbitrary;
import net.jqwik.api.state.Transformer;

import java.util.ArrayList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

// No Builder here — same as the parameterized suite, these are constructed like java.util.TreeSet/TreeMap.

/**
 * The test just uses random data from Jqwick to test failures nothing else.
 */
class ChaosTreeModelBasedPropertyTest {

    private static final int LO = -50;
    private static final int HI = 300;

    // SETS

    private static ActionChainArbitrary<DualSetState> setModelChain(Supplier<NavigableSet<Integer>> factory) {
        return ActionChain
                .startWith(() -> new DualSetState(factory))
                .withAction(4, (Action.Dependent<DualSetState>) state ->
                        Arbitraries.integers().between(LO, HI).map(v ->
                                Transformer.mutate("add(" + v + ")", s -> {
                                    boolean expected = s.oracle.add(v);
                                    boolean actual = s.candidate.add(v);
                                    assertEquals(expected, actual, "add(" + v + ") return value");
                                })))
                .withAction(4, (Action.Dependent<DualSetState>) state ->
                        Arbitraries.integers().between(LO, HI).map(v ->
                                Transformer.mutate("remove(" + v + ")", s -> {
                                    boolean expected = s.oracle.remove(v);
                                    boolean actual = s.candidate.remove(v);
                                    assertEquals(expected, actual, "remove(" + v + ") return value");
                                })))
                .withAction(2, Action.<DualSetState>builder().describeAs("pollFirst").justMutate(s -> {
                    Integer expected = s.oracle.pollFirst();
                    Integer actual = s.candidate.pollFirst();
                    assertEquals(expected, actual, "pollFirst()");
                }))
                .withAction(2, Action.<DualSetState>builder().describeAs("pollLast").justMutate(s -> {
                    Integer expected = s.oracle.pollLast();
                    Integer actual = s.candidate.pollLast();
                    assertEquals(expected, actual, "pollLast()");
                }))
                .withAction(3, (Action.Dependent<DualSetState>) state ->
                        Arbitraries.integers().between(LO, HI).map(v ->
                                Transformer.mutate("probe(" + v + ")", s -> {
                                    assertEquals(s.oracle.floor(v), s.candidate.floor(v), "floor(" + v + ")");
                                    assertEquals(s.oracle.ceiling(v), s.candidate.ceiling(v), "ceiling(" + v + ")");
                                    assertEquals(s.oracle.higher(v), s.candidate.higher(v), "higher(" + v + ")");
                                    assertEquals(s.oracle.lower(v), s.candidate.lower(v), "lower(" + v + ")");
                                    assertEquals(s.oracle.contains(v), s.candidate.contains(v), "contains(" + v + ")");
                                })))
                .withMaxTransformations(300);
    }

    private static void assertSetInvariant(DualSetState s) {
        assertEquals(s.oracle.size(), s.candidate.size(), "size");
        assertIterableEquals(new ArrayList<>(s.oracle), new ArrayList<>(s.candidate), "iteration order/content");
    }

    private static ActionChainArbitrary<DualMapState> mapModelChain(Supplier<NavigableMap<Integer, String>> factory) {
        return ActionChain
                .startWith(() -> new DualMapState(factory))
                .withAction(4, (Action.Dependent<DualMapState>) state ->
                        Arbitraries.integers().between(LO, HI).map(k ->
                                Transformer.mutate("put(" + k + ")", s -> {
                                    String v = "v" + k;
                                    String expected = s.oracle.put(k, v);
                                    String actual = s.candidate.put(k, v);
                                    assertEquals(expected, actual, "put(" + k + ") return value");
                                })))
                .withAction(4, (Action.Dependent<DualMapState>) state ->
                        Arbitraries.integers().between(LO, HI).map(k ->
                                Transformer.mutate("remove(" + k + ")", s -> {
                                    String expected = s.oracle.remove(k);
                                    String actual = s.candidate.remove(k);
                                    assertEquals(expected, actual, "remove(" + k + ") return value");
                                })))
                .withAction(2, Action.<DualMapState>builder().describeAs("pollFirstEntry").justMutate(s -> {
                    Map.Entry<Integer, String> expected = s.oracle.pollFirstEntry();
                    Map.Entry<Integer, String> actual = s.candidate.pollFirstEntry();
                    if (expected == null) {
                        assertNull(actual, "pollFirstEntry() should be null");
                    } else {
                        assertNotNull(actual, "pollFirstEntry() should not be null");
                        assertEquals(expected.getKey(), actual.getKey(), "pollFirstEntry() key");
                        assertEquals(expected.getValue(), actual.getValue(), "pollFirstEntry() value");
                    }
                }))
                .withAction(2, Action.<DualMapState>builder().describeAs("pollLastEntry").justMutate(s -> {
                    Map.Entry<Integer, String> expected = s.oracle.pollLastEntry();
                    Map.Entry<Integer, String> actual = s.candidate.pollLastEntry();
                    if (expected == null) {
                        assertNull(actual, "pollLastEntry() should be null");
                    } else {
                        assertNotNull(actual, "pollLastEntry() should not be null");
                        assertEquals(expected.getKey(), actual.getKey(), "pollLastEntry() key");
                        assertEquals(expected.getValue(), actual.getValue(), "pollLastEntry() value");
                    }
                }))
                .withAction(3, (Action.Dependent<DualMapState>) state ->
                        Arbitraries.integers().between(LO, HI).map(k ->
                                Transformer.mutate("probe(" + k + ")", s -> {
                                    assertEquals(s.oracle.floorKey(k), s.candidate.floorKey(k), "floorKey(" + k + ")");
                                    assertEquals(s.oracle.ceilingKey(k), s.candidate.ceilingKey(k), "ceilingKey(" + k + ")");
                                    assertEquals(s.oracle.higherKey(k), s.candidate.higherKey(k), "higherKey(" + k + ")");
                                    assertEquals(s.oracle.lowerKey(k), s.candidate.lowerKey(k), "lowerKey(" + k + ")");
                                    assertEquals(s.oracle.get(k), s.candidate.get(k), "get(" + k + ")");
                                })))
                .withMaxTransformations(300);
    }

    private static void assertMapInvariant(DualMapState s) {
        assertEquals(s.oracle.size(), s.candidate.size(), "size");
        assertIterableEquals(new ArrayList<>(s.oracle.keySet()), new ArrayList<>(s.candidate.keySet()), "key iteration order");
        for (Integer k : s.oracle.keySet()) {
            assertEquals(s.oracle.get(k), s.candidate.get(k), "value for key " + k);
        }
    }

    @Property
    void avlTreeSetMatchesModel(@ForAll("avlSetChain") ActionChain<DualSetState> chain) {
        chain.withInvariant(ChaosTreeModelBasedPropertyTest::assertSetInvariant).run();
    }

    @Provide
    ActionChainArbitrary<DualSetState> avlSetChain() {
        return setModelChain(AvlTreeSet::new);
    }

    @Property
    void redBlackTreeSetMatchesModel(@ForAll("redBlackSetChain") ActionChain<DualSetState> chain) {
        chain.withInvariant(ChaosTreeModelBasedPropertyTest::assertSetInvariant).run();
    }

    @Provide
    ActionChainArbitrary<DualSetState> redBlackSetChain() {
        return setModelChain(RedBlackTreeSet::new);
    }

    @Property
    void bTreeSetMatchesModel(@ForAll("bTreeSetChain") ActionChain<DualSetState> chain) {
        chain.withInvariant(ChaosTreeModelBasedPropertyTest::assertSetInvariant).run();
    }

    @Provide
    ActionChainArbitrary<DualSetState> bTreeSetChain() {
        return setModelChain(() -> BTreeSet.Builder.<Integer>create(32).factor(0.75f)
                .importFlatArray(new Integer[0]).build());
    }

    @Property
    void bPlusTreeSetMatchesModel(@ForAll("bPlusTreeSetChain") ActionChain<DualSetState> chain) {
        chain.withInvariant(ChaosTreeModelBasedPropertyTest::assertSetInvariant).run();
    }

    // MAPS

    @Provide
    ActionChainArbitrary<DualSetState> bPlusTreeSetChain() {
        return setModelChain(() -> BPlusTreeSet.Builder.<Integer>create(32).factor(0.75f)
                .importFlatArray(new Integer[0]).build());
    }

    @Property
    void avlTreeMapMatchesModel(@ForAll("avlMapChain") ActionChain<DualMapState> chain) {
        chain.withInvariant(ChaosTreeModelBasedPropertyTest::assertMapInvariant).run();
    }

    @Provide
    ActionChainArbitrary<DualMapState> avlMapChain() {
        return mapModelChain(AvlTreeMap::new);
    }

    @Property
    void redBlackTreeMapMatchesModel(@ForAll("redBlackMapChain") ActionChain<DualMapState> chain) {
        chain.withInvariant(ChaosTreeModelBasedPropertyTest::assertMapInvariant).run();
    }

    @Provide
    ActionChainArbitrary<DualMapState> redBlackMapChain() {
        return mapModelChain(RedBlackTreeMap::new);
    }

    @Property
    void bTreeMapMatchesModel(@ForAll("bTreeMapChain") ActionChain<DualMapState> chain) {
        chain.withInvariant(ChaosTreeModelBasedPropertyTest::assertMapInvariant).run();
    }

    @Provide
    ActionChainArbitrary<DualMapState> bTreeMapChain() {
        return mapModelChain(() -> BTreeMap.Builder.<Integer, String>create(32).factor(0.75f)
                .importFlatMatrix(new Object[][]{new Integer[0], new String[0]}).build());
    }

    @Property
    void bPlusTreeMapMatchesModel(@ForAll("bPlusTreeMapChain") ActionChain<DualMapState> chain) {
        chain.withInvariant(ChaosTreeModelBasedPropertyTest::assertMapInvariant).run();
    }

    @Provide
    ActionChainArbitrary<DualMapState> bPlusTreeMapChain() {
        return mapModelChain(() -> BPlusTreeMap.Builder.<Integer, String>create(32).factor(0.75f)
                .importFlatMatrix(new Object[][]{new Integer[0], new String[0]}).build());
    }

    static final class DualSetState {
        final NavigableSet<Integer> candidate;
        final TreeSet<Integer> oracle = new TreeSet<>();

        DualSetState(Supplier<NavigableSet<Integer>> factory) {
            this.candidate = factory.get();
        }

        @Override
        public String toString() {
            return "oracle=" + oracle;
        }
    }

    static final class DualMapState {
        final NavigableMap<Integer, String> candidate;
        final TreeMap<Integer, String> oracle = new TreeMap<>();

        DualMapState(Supplier<NavigableMap<Integer, String>> factory) {
            this.candidate = factory.get();
        }

        @Override
        public String toString() {
            return "oracle=" + oracle;
        }
    }
}
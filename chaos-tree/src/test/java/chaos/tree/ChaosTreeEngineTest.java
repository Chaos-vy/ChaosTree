package chaos.tree;

import chaos.tree.nary.BPlusTreeSet;
import chaos.tree.nary.BTreeSet;
import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.naryMap.BTreeMap;
import chaos.tree.binary.AvlTreeSet;
import chaos.tree.binary.RedBlackTreeSet;
import chaos.tree.binaryMap.AvlTreeMap;
import chaos.tree.binaryMap.RedBlackTreeMap;
/*
All 8 tree in one arena against JDK TreeSet and TreeMap haha lol!
 */
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Torture test for every Navigable{Set,Map} implementation in ChaosTree.
 *A structural test chaos
 * what the test is
 * the test guarantee that each API has contact of truthness to JDK official Sorted Set and Map
 * 1. The brutal test: Test where build from Dragon feed(array) survives or not . if it survives
 * there is no need to congratulate it is i.e., must deconstruct to exportFlatMatrix[][] now
 * the chaos commence create then destroy rebuild and verify. And it's not easy also because
 * it's tested for 100 degree [32.132] and factor {0.5f,0.89f,0.75f,1.0f};
 * what you have seen in the text, but it was subjected in my system to 2k degree and more rigorous random factor!!
 * BUGx01: Bug caught by this test are . Internal CLRS rule corruption which gave NPE and AIOOBE.
 * Till now math is lawless here.
 * 2. The test is passed only and only if it can import->verify->export->import->verify by pruning last hehe then
 * it tells that it stand out!!.
 *
 * Total time: 4min
 * chaos-test total time : 2hr38min{the 2K variant}
 * The question: why was it subjected to 2k then now 100!!?
 * the bug caugh was at t = 67 and f =0.75 size =1000
 * trailing left 0 but NaryTree Creator were fast enough though that there is extra key
 * so the bug was an extra null leaf node which can be seen from the display() fn until the bug
 * but now it has been fixed!!
 * Please do ignore my english and focus on Test You might get a change for PR rather than the chaos-vy get it. Hehe LOL
 *
 */
public class ChaosTreeEngineTest {

    private static final int SIZE = 94852;
    private static Integer[] keys;
    private static String[] values;
    private static TreeMap<Integer, String> truthMap;
    private static TreeSet<Integer> truthSet;
    private static Object[][] flatMatrix;

    private static final int[] FULL_DEGREES = new int[100];
    private static final float[] FACTORS = {0.5f, 0.89f, 0.75f, 1.0f};
    private static final int[] SAMPLE_DEGREES = {32, 33, 67, 131};

    @BeforeAll
    static void setupData() {
        for (int i = 0; i < 100; i++) FULL_DEGREES[i] = i + 32;

        keys = new Integer[SIZE];
        values = new String[SIZE];
        truthMap = new TreeMap<>();
        truthSet = new TreeSet<>();

        for (int i = 0; i < SIZE; i++) {
            keys[i] = i;
            values[i] = "Chaos-" + i;
            truthMap.put(keys[i], values[i]);
            truthSet.add(i);
        }
        flatMatrix = new Object[][]{keys, values};
    }

    static final class SetSubject {
        final String name;
        final Function<Integer[], NavigableSet<Integer>> factory;
        SetSubject(String name, Function<Integer[], NavigableSet<Integer>> factory) {
            this.name = name;
            this.factory = factory;
        }
        NavigableSet<Integer> build(Integer[] orderedKeys) { return factory.apply(orderedKeys); }
        @Override public String toString() { return name; }
    }

    static final class MapSubject {
        final String name;
        final BiFunction<Integer[], String[], NavigableMap<Integer, String>> factory;
        MapSubject(String name, BiFunction<Integer[], String[], NavigableMap<Integer, String>> factory) {
            this.name = name;
            this.factory = factory;
        }
        NavigableMap<Integer, String> build(Integer[] orderedKeys, String[] orderedValues) {
            return factory.apply(orderedKeys, orderedValues);
        }
        @Override public String toString() { return name; }
    }

    static List<SetSubject> setSubjects() {
        List<SetSubject> list = new ArrayList<>();
        for (int degree : SAMPLE_DEGREES) {
            for (float f : FACTORS) {
                final int d = degree;
                list.add(new SetSubject("BTreeSet[d=" + d + ",f=" + f + "]",
                        ks -> BTreeSet.Builder.<Integer>degree(d).factor(f).importFlatMatrix(ks).build()));
                list.add(new SetSubject("BPlusTreeSet[d=" + d + ",f=" + f + "]",
                        ks -> BPlusTreeSet.Builder.<Integer>degree(d).factor(f).importFlatMatrix(ks).build()));
            }
        }
        // internally needed sorted set instance

        list.add(new SetSubject("AvlTreeSet", ks -> new AvlTreeSet<>(new TreeSet<>(Arrays.asList(ks)))));
        list.add(new SetSubject("RedBlackTreeSet", ks -> new RedBlackTreeSet<>(new TreeSet<>(Arrays.asList(ks)))));
        return list;
    }

    static List<MapSubject> mapSubjects() {
        List<MapSubject> list = new ArrayList<>();
        for (int degree : SAMPLE_DEGREES) {
            for (float f : FACTORS) {
                final int d = degree;
                list.add(new MapSubject("BTreeMap[d=" + d + ",f=" + f + "]",
                        (ks, vs) -> BTreeMap.Builder.<Integer, String>degree(d).factor(f)
                                .importFlatMatrix(new Object[][]{ks, vs}).build()));
                list.add(new MapSubject("BPlusTreeMap[d=" + d + ",f=" + f + "]",
                        (ks, vs) -> BPlusTreeMap.Builder.<Integer, String>degree(d).factor(f)
                                .importFlatMatrix(new Object[][]{ks, vs}).build()));
            }
        }
        
        // By passing a SortedMap (TreeMap), we trigger the optimized O(N) bulk-load
        // constructor inside AvlTreeMap and RedBlackTreeMap!
        list.add(new MapSubject("AvlTreeMap", (ks, vs) -> {
            TreeMap<Integer, String> sortedMap = new TreeMap<>();
            for (int i = 0; i < ks.length; i++) sortedMap.put(ks[i], vs[i]);
            return new AvlTreeMap<>(sortedMap);
        }));
        list.add(new MapSubject("RedBlackTreeMap", (ks, vs) -> {
            TreeMap<Integer, String> sortedMap = new TreeMap<>();
            for (int i = 0; i < ks.length; i++) sortedMap.put(ks[i], vs[i]);
            return new RedBlackTreeMap<>(sortedMap);
        }));
        return list;
    }

    // Only the two iterator-bulk-loaded families — used by the insertion-order
    // sensitivity tests, since importFlatMatrix on the B-family almost certainly
    // does a sorted-array bulk build (order-insensitive by construction), while
    // Avl/RedBlack presumably insert one-by-one from the iterator (order-sensitive).
    static List<SetSubject> orderSensitiveSetSubjects() {
        return setSubjects().stream()
                .filter(s -> s.name.startsWith("Avl") || s.name.startsWith("RedBlack"))
                .collect(Collectors.toList());
    }

    static List<MapSubject> orderSensitiveMapSubjects() {
        return mapSubjects().stream()
                .filter(m -> m.name.startsWith("Avl") || m.name.startsWith("RedBlack"))
                .collect(Collectors.toList());
    }

    static Stream<SetSubject> setSubjectStream() { return setSubjects().stream(); }
    static Stream<MapSubject> mapSubjectStream() { return mapSubjects().stream(); }
    static Stream<SetSubject> orderSensitiveStream() { return orderSensitiveSetSubjects().stream(); }
    static Stream<MapSubject> orderSensitiveMapStream() { return orderSensitiveMapSubjects().stream(); }

    @Test
    void testBPlusTreeMapChaos() {
        runFullGridGauntletMap(true);
    }

    @Test
    void testBTreeMapChaos() {
        runFullGridGauntletMap(false);
    }

    @Test
    void testBTreeSetChaos() {
        runFullGridGauntletSet(false);
    }

    @Test
    void testBPlusTreeSetChaos() {
        runFullGridGauntletSet(true);
    }

    private void runFullGridGauntletSet(boolean isBPlusTree) {
        for (int degree : FULL_DEGREES) {
            for (float f : FACTORS) {
                Set<Integer> chaosTree = isBPlusTree
                        ? BPlusTreeSet.Builder.<Integer>degree(degree).factor(f).importFlatMatrix(keys).build()
                        : BTreeSet.Builder.<Integer>degree(degree).factor(f).importFlatMatrix(keys).build();
                String config = (isBPlusTree ? "B+Tree set" : "B-Tree set") + " [Degree: " + degree + ", Factor: " + f + "]";
                
                // 1. Verify initial build
                verifySizeset(chaosTree, config);
                verifyExactContainsSet(chaosTree, config);
                verifyIterationSet(chaosTree, config);
                
                // 2. CREATION = DESTRUCTION: Export to flat array and rebuild!
                Integer[] exportedKeys = chaosTree.toArray(new Integer[0]);
                assertEquals(keys.length, exportedKeys.length, config + " -> Exported array length mismatch");
                chaosTree = isBPlusTree
                        ? BPlusTreeSet.Builder.<Integer>degree(degree).factor(f).importFlatMatrix(exportedKeys).build()
                        : BTreeSet.Builder.<Integer>degree(degree).factor(f).importFlatMatrix(exportedKeys).build();
                
                // 3. Verify rebuilt tree
                String rebuiltConfig = config + " (Rebuilt)";
                verifySizeset(chaosTree, rebuiltConfig);
                verifyExactContainsSet(chaosTree, rebuiltConfig);
                verifyIterationSet(chaosTree, rebuiltConfig);

                // 4. Final destruction (random deletion)
                verifyRandomDeletionGauntletSet(chaosTree, rebuiltConfig);
            }
        }
    }

    private void runFullGridGauntletMap(boolean isBPlusTree) {
        for (int degree : FULL_DEGREES) {
            for (float f : FACTORS) {
                Map<Integer, String> chaosTree = isBPlusTree
                        ? BPlusTreeMap.Builder.<Integer, String>degree(degree).factor(f).importFlatMatrix(flatMatrix).build()
                        : BTreeMap.Builder.<Integer, String>degree(degree).factor(f).importFlatMatrix(flatMatrix).build();
                String config = (isBPlusTree ? "B+Tree" : "B-Tree") + " [Degree: " + degree + ", Factor: " + f + "]";
                
                // 1. Verify initial build
                verifySize(chaosTree, config);
                verifyExactGets(chaosTree, config);
                verifyIteration(chaosTree, config);

                // 2. CREATION = DESTRUCTION: Export to flat matrix and rebuild!
                Integer[] exportedMapKeys = chaosTree.keySet().toArray(new Integer[0]);
                String[] exportedMapValues = chaosTree.values().toArray(new String[0]);
                Object[][] exportedMatrix = new Object[][]{exportedMapKeys, exportedMapValues};
                chaosTree = isBPlusTree
                        ? BPlusTreeMap.Builder.<Integer, String>degree(degree).factor(f).importFlatMatrix(exportedMatrix).build()
                        : BTreeMap.Builder.<Integer, String>degree(degree).factor(f).importFlatMatrix(exportedMatrix).build();
                
                // 3. Verify rebuilt tree
                String rebuiltConfig = config + " (Rebuilt)";
                verifySize(chaosTree, rebuiltConfig);
                verifyExactGets(chaosTree, rebuiltConfig);
                verifyIteration(chaosTree, rebuiltConfig);

                // 4. Final destruction (random deletion)
                verifyRandomDeletionGauntlet(chaosTree, rebuiltConfig);
            }
        }
    }

    private void verifyRandomDeletionGauntletSet(Set<Integer> chaosTree, String config) {
        List<Integer> keysToRemove = new ArrayList<>(Arrays.asList(keys));
        Collections.shuffle(keysToRemove, new Random(99));
        int expectedSize = chaosTree.size();
        for (Integer key : keysToRemove) {
            assertTrue(chaosTree.remove(key), config + " -> remove() returned false for key: " + key);
            expectedSize--;
            assertEquals(expectedSize, chaosTree.size(), config + " -> size drift after removing " + key);
            assertFalse(chaosTree.contains(key), config + " -> ghost key after remove: " + key);
        }
        assertTrue(chaosTree.isEmpty(), config + " -> not empty after full drain, size=" + chaosTree.size());
    }

    private void verifyIterationSet(Set<Integer> chaosTree, String config) {
        Iterator<Integer> t = truthSet.iterator();
        Iterator<Integer> c = chaosTree.iterator();
        int step = 0;
        while (t.hasNext() && c.hasNext()) {
            assertEquals(t.next(), c.next(), config + " -> iteration mismatch at step " + step++);
        }
        assertFalse(t.hasNext() || c.hasNext(), config + " -> iteration length mismatch");
    }

    private void verifyExactContainsSet(Set<Integer> chaosTree, String config) {
        List<Integer> shuffled = new ArrayList<>(Arrays.asList(keys));
        Collections.shuffle(shuffled, new Random(42));
        for (Integer key : shuffled) {
            assertEquals(truthSet.contains(key), chaosTree.contains(key), config + " -> lookup failed for " + key);
        }
    }

    private void verifySizeset(Set<Integer> chaosTree, String config) {
        assertEquals(truthSet.size(), chaosTree.size(), config + " -> size mismatch after bulk load");
    }

    private void verifySize(Map<Integer, String> chaos, String config) {
        assertEquals(truthMap.size(), chaos.size(), config + " -> size mismatch after bulk load");
    }

    private void verifyExactGets(Map<Integer, String> chaos, String config) {
        List<Integer> shuffled = new ArrayList<>(Arrays.asList(keys));
        Collections.shuffle(shuffled, new Random(42));
        for (Integer key : shuffled) {
            assertEquals(truthMap.get(key), chaos.get(key), config + " -> lookup failed for " + key);
        }
    }

    private void verifyIteration(Map<Integer, String> chaos, String config) {
        Iterator<Map.Entry<Integer, String>> t = truthMap.entrySet().iterator();
        Iterator<Map.Entry<Integer, String>> c = chaos.entrySet().iterator();
        int step = 0;
        while (t.hasNext() && c.hasNext()) {
            Map.Entry<Integer, String> te = t.next(), ce = c.next();
            assertEquals(te.getKey(), ce.getKey(), config + " -> key mismatch at step " + step);
            assertEquals(te.getValue(), ce.getValue(), config + " -> value mismatch at step " + step);
            step++;
        }
        assertFalse(t.hasNext() || c.hasNext(), config + " -> iteration length mismatch");
    }

    private void verifyRandomDeletionGauntlet(Map<Integer, String> chaosTree, String config) {
        List<Integer> keysToRemove = new ArrayList<>(Arrays.asList(keys));
        Collections.shuffle(keysToRemove, new Random(99));
        int expectedSize = chaosTree.size();
        for (Integer key : keysToRemove) {
            String removed = chaosTree.remove(key);
            expectedSize--;
            assertNotNull(removed, config + " -> remove() returned null for " + key);
            assertEquals(expectedSize, chaosTree.size(), config + " -> size drift after removing " + key);
            assertFalse(chaosTree.containsKey(key), config + " -> ghost key after remove: " + key);
        }
        assertTrue(chaosTree.isEmpty(), config + " -> not empty after full drain, size=" + chaosTree.size());
    }

    @ParameterizedTest(name = "{0} empty-collection contract")
    @MethodSource("setSubjectStream")
    void danger_emptySet(SetSubject subject) {
        NavigableSet<Integer> s = subject.build(new Integer[0]);
        String c = subject.name;
        assertTrue(s.isEmpty(), c);
        assertEquals(0, s.size(), c);
        assertThrows(NoSuchElementException.class, s::first, c + " -> first() on empty must throw");
        assertThrows(NoSuchElementException.class, s::last, c + " -> last() on empty must throw");
        assertNull(s.floor(0), c);
        assertNull(s.ceiling(0), c);
        assertNull(s.higher(0), c);
        assertNull(s.lower(0), c);
        assertNull(s.pollFirst(), c);
        assertNull(s.pollLast(), c);
        assertFalse(s.iterator().hasNext(), c);
        assertFalse(s.contains(0), c);
        assertTrue(s.subSet(0, true, 100, true).isEmpty(), c);
        assertTrue(s.descendingSet().isEmpty(), c);
    }

    @ParameterizedTest(name = "{0} empty-map contract")
    @MethodSource("mapSubjectStream")
    void danger_emptyMap(MapSubject subject) {
        NavigableMap<Integer, String> m = subject.build(new Integer[0], new String[0]);
        String c = subject.name;
        assertTrue(m.isEmpty(), c);
        assertThrows(NoSuchElementException.class, m::firstKey, c + " -> firstKey() on empty must throw");
        assertThrows(NoSuchElementException.class, m::lastKey, c + " -> lastKey() on empty must throw");
        assertNull(m.floorEntry(0), c);
        assertNull(m.ceilingEntry(0), c);
        assertNull(m.higherEntry(0), c);
        assertNull(m.lowerEntry(0), c);
        assertNull(m.pollFirstEntry(), c);
        assertNull(m.pollLastEntry(), c);
        assertTrue(m.descendingMap().isEmpty(), c);
    }

    @ParameterizedTest(name = "{0} single-element boundary")
    @MethodSource("setSubjectStream")
    void danger_singleElementSet(SetSubject subject) {
        NavigableSet<Integer> s = subject.build(new Integer[]{42});
        String c = subject.name;
        assertEquals(42, s.first(), c);
        assertEquals(42, s.last(), c);
        assertEquals(42, s.floor(42), c);
        assertEquals(42, s.ceiling(42), c);
        assertEquals(42, s.floor(100), c);
        assertEquals(42, s.ceiling(0), c);
        assertNull(s.higher(42), c);
        assertNull(s.lower(42), c);
        assertNull(s.lower(0), c);
        assertNull(s.higher(100), c);
        assertEquals(42, s.pollFirst(), c);
        assertTrue(s.isEmpty(), c + " -> should be empty after draining the only element");
    }
    @ParameterizedTest(name = "{0} sparse floor/ceiling/higher/lower")
    @MethodSource("setSubjectStream")
    void danger_sparseNavigationSet(SetSubject subject) {
        int n = 2001;
        Integer[] sparse = new Integer[n];
        TreeSet<Integer> truth = new TreeSet<>();
        for (int i = 0; i < n; i++) { sparse[i] = i * 2; truth.add(i * 2); }
        NavigableSet<Integer> s = subject.build(sparse);
        String c = subject.name;

        int min = truth.first(), max = truth.last();
        assertNull(s.lower(min), c + " -> lower(min) must be null");
        assertNull(s.higher(max), c + " -> higher(max) must be null");
        assertNull(s.floor(min - 1), c + " -> floor(below min) must be null");
        assertNull(s.ceiling(max + 1), c + " -> ceiling(above max) must be null");
        Random rnd = new Random(7);
        for (int trial = 0; trial < 500; trial++) {
            int probe = rnd.nextInt(max + 2) - 1;
            assertEquals(truth.floor(probe), s.floor(probe), c + " -> floor(" + probe + ")");
            assertEquals(truth.ceiling(probe), s.ceiling(probe), c + " -> ceiling(" + probe + ")");
            assertEquals(truth.higher(probe), s.higher(probe), c + " -> higher(" + probe + ")");
            assertEquals(truth.lower(probe), s.lower(probe), c + " -> lower(" + probe + ")");
        }
    }

    @ParameterizedTest(name = "{0} sparse floor/ceiling/higher/lower entries")
    @MethodSource("mapSubjectStream")
    void danger_sparseNavigationMap(MapSubject subject) {
        int n = 2001;
        Integer[] sparseK = new Integer[n];
        String[] sparseV = new String[n];
        TreeMap<Integer, String> truth = new TreeMap<>();
        for (int i = 0; i < n; i++) {
            sparseK[i] = i * 2;
            sparseV[i] = "v" + (i * 2);
            truth.put(i * 2, "v" + (i * 2));
        }
        NavigableMap<Integer, String> m = subject.build(sparseK, sparseV);
        String c = subject.name;
        Random rnd = new Random(7);
        for (int trial = 0; trial < 500; trial++) {
            int probe = rnd.nextInt(truth.lastKey() + 2) - 1;
            assertEquals(truth.floorKey(probe), m.floorKey(probe), c + " -> floorKey(" + probe + ")");
            assertEquals(truth.ceilingKey(probe), m.ceilingKey(probe), c + " -> ceilingKey(" + probe + ")");
            assertEquals(truth.higherKey(probe), m.higherKey(probe), c + " -> higherKey(" + probe + ")");
            assertEquals(truth.lowerKey(probe), m.lowerKey(probe), c + " -> lowerKey(" + probe + ")");
        }
    }

    @ParameterizedTest(name = "{0} subSet view semantics")
    @MethodSource("setSubjectStream")
    void danger_subSetViewSemantics(SetSubject subject) {
        int n = 500;
        Integer[] arr = new Integer[n];
        TreeSet<Integer> truth = new TreeSet<>();
        for (int i = 0; i < n; i++) { arr[i] = i * 3; truth.add(i * 3); } // 0,3,6,...
        NavigableSet<Integer> s = subject.build(arr);
        String c = subject.name;

        int lo = 150, hi = 450; // both present as multiples of 3
        for (boolean loIncl : new boolean[]{true, false}) {
            for (boolean hiIncl : new boolean[]{true, false}) {
                assertEquals(
                        truth.subSet(lo, loIncl, hi, hiIncl),
                        s.subSet(lo, loIncl, hi, hiIncl),
                        c + " -> subSet(" + lo + "," + loIncl + "," + hi + "," + hiIncl + ") content mismatch");
            }
        }
        assertEquals(truth.headSet(hi, true), s.headSet(hi, true), c + " -> headSet inclusive");
        assertEquals(truth.headSet(hi, false), s.headSet(hi, false), c + " -> headSet exclusive");
        assertEquals(truth.tailSet(lo, true), s.tailSet(lo, true), c + " -> tailSet inclusive");
        assertEquals(truth.tailSet(lo, false), s.tailSet(lo, false), c + " -> tailSet exclusive");

        assertTrue(s.subSet(lo, true, lo, false).isEmpty(), c + " -> [lo,lo) must be empty");
        assertThrows(IllegalArgumentException.class, () -> s.subSet(hi, true, lo, true),
                c + " -> subSet(hi,lo) with hi>lo must throw IllegalArgumentException");
        NavigableSet<Integer> view = s.subSet(lo, true, hi, true);
        int viewSizeBefore = view.size();
        Integer victim = view.iterator().next();
        assertTrue(view.remove(victim), c + " -> removing an in-range element via subSet view");
        assertEquals(viewSizeBefore - 1, view.size(), c + " -> view size did not shrink");
        assertFalse(s.contains(victim), c + " -> removal via view did not propagate to backing set");
        NavigableSet<Integer> view2 = s.subSet(lo, true, hi, true);
        int view2SizeBefore = view2.size();
        Integer victim2 = s.tailSet(lo, true).headSet(hi, true).iterator().next();
        s.remove(victim2);
        assertEquals(view2SizeBefore - 1, view2.size(), c + " -> backing removal did not propagate to live view");
        assertThrows(IllegalArgumentException.class, () -> view.add(hi + 999),
                c + " -> subSet view must reject out-of-range add()");
    }

    @ParameterizedTest(name = "{0} subMap view semantics")
    @MethodSource("mapSubjectStream")
    void danger_subMapViewSemantics(MapSubject subject) {
        int n = 500;
        Integer[] ks = new Integer[n];
        String[] vs = new String[n];
        TreeMap<Integer, String> truth = new TreeMap<>();
        for (int i = 0; i < n; i++) { ks[i] = i * 3; vs[i] = "v" + (i * 3); truth.put(i * 3, "v" + (i * 3)); }
        NavigableMap<Integer, String> m = subject.build(ks, vs);
        String c = subject.name;

        int lo = 150, hi = 450;
        for (boolean loIncl : new boolean[]{true, false}) {
            for (boolean hiIncl : new boolean[]{true, false}) {
                assertEquals(
                        truth.subMap(lo, loIncl, hi, hiIncl).keySet(),
                        m.subMap(lo, loIncl, hi, hiIncl).keySet(),
                        c + " -> subMap(" + lo + "," + loIncl + "," + hi + "," + hiIncl + ") key mismatch");
            }
        }
        NavigableMap<Integer, String> view = m.subMap(lo, true, hi, true);
        view.put(lo + 3, "overwritten-through-view");
        assertEquals("overwritten-through-view", m.get(lo + 3), c + " -> put via subMap view did not propagate");
        assertThrows(IllegalArgumentException.class, () -> view.put(hi + 999, "nope"),
                c + " -> subMap view must reject out-of-range put()");
    }
    @ParameterizedTest(name = "{0} descendingSet correctness")
    @MethodSource("setSubjectStream")
    void danger_descendingSet(SetSubject subject) {
        int n = 300;
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) arr[i] = i;
        NavigableSet<Integer> s = subject.build(arr);
        String c = subject.name;

        List<Integer> descending = new ArrayList<>(s.descendingSet());
        List<Integer> expected = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) expected.add(i);
        assertEquals(expected, descending, c + " -> descendingSet() order mismatch");
        NavigableSet<Integer> desc = s.descendingSet();
        Integer polled = desc.pollFirst();
        assertEquals(n - 1, (int) polled, c + " -> descendingSet().pollFirst() should return the max");
        assertFalse(s.contains(n - 1), c + " -> descendingSet().pollFirst() did not propagate to backing set");
    }

    @ParameterizedTest(name = "{0} descendingMap correctness")
    @MethodSource("mapSubjectStream")
    void danger_descendingMap(MapSubject subject) {
        int n = 300;
        Integer[] ks = new Integer[n];
        String[] vs = new String[n];
        for (int i = 0; i < n; i++) { ks[i] = i; vs[i] = "v" + i; }
        NavigableMap<Integer, String> m = subject.build(ks, vs);
        String c = subject.name;

        List<Integer> descKeys = new ArrayList<>(m.descendingKeySet());
        List<Integer> expected = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) expected.add(i);
        assertEquals(expected, descKeys, c + " -> descendingKeySet() order mismatch");
    }

    // ---- 6. poll-drain from both ends (different code path than keyed remove()) ----

    @ParameterizedTest(name = "{0} pollFirst/pollLast drain")
    @MethodSource("setSubjectStream")
    void danger_pollDrainSet(SetSubject subject) {
        int n = 1000;
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) arr[i] = i;
        NavigableSet<Integer> s = subject.build(arr);
        String c = subject.name;
        Integer lastLow = null, lastHigh = null;
        boolean pollLow = true;
        int remaining = n;
        while (remaining > 0) {
            if (pollLow) {
                Integer v = s.pollFirst();
                if (lastLow != null) assertTrue(v > lastLow, c + " -> pollFirst() not increasing");
                lastLow = v;
            } else {
                Integer v = s.pollLast();
                if (lastHigh != null) assertTrue(v < lastHigh, c + " -> pollLast() not decreasing");
                lastHigh = v;
            }
            remaining--;
            pollLow = !pollLow;
        }
        assertTrue(s.isEmpty(), c + " -> not empty after alternating poll-drain, size=" + s.size());
    }

    // duplicate keys during bulk load

    @ParameterizedTest(name = "{0} duplicate keys collapse correctly")
    @MethodSource("orderSensitiveStream")
    void danger_duplicateKeysSet(SetSubject subject) {
        Integer[] withDupes = {5, 1, 5, 3, 1, 9, 5, 3};
        NavigableSet<Integer> s = subject.build(withDupes);
        assertEquals(new TreeSet<>(Arrays.asList(withDupes)), s, subject.name + " -> duplicates must collapse to unique keys");
    }

    @ParameterizedTest(name = "{0} duplicate keys: last value wins")
    @MethodSource("orderSensitiveMapStream")
    void danger_duplicateKeysMap(MapSubject subject) {
        Integer[] ks = {5, 1, 5, 3, 1, 9, 5, 3};
        String[] vs = {"5a", "1a", "5b", "3a", "1b", "9a", "5c", "3b"};
        NavigableMap<Integer, String> m = subject.build(ks, vs);
        String c = subject.name;
        assertEquals(4, m.size(), c + " -> size after de-duplication");
        assertEquals("5c", m.get(5), c);
        assertEquals("1b", m.get(1), c);
        assertEquals("3b", m.get(3), c);
        assertEquals("9a", m.get(9), c);
    }

    // null handling

    @ParameterizedTest(name = "{0} rejects null element, tolerates null query")
    @MethodSource("setSubjectStream")
    void danger_nullHandlingSet(SetSubject subject) {
        NavigableSet<Integer> s = subject.build(new Integer[]{1, 2, 3});
        String c = subject.name;
        assertThrows(NullPointerException.class, () -> s.add(null), c + " -> add(null) must be rejected (RESTRICTS_ELEMENTS)");
        assertFalse(assertDoesNotThrow(() -> s.contains(null), c + " -> contains(null) must not throw (ALLOWS_NULL_QUERIES)"),
                c + " -> contains(null) should report false, not true");
        assertFalse(assertDoesNotThrow(() -> s.remove(null), c + " -> remove(null) must not throw (ALLOWS_NULL_QUERIES)"),
                c + " -> remove(null) should report false, not true");
    }

    @ParameterizedTest(name = "{0} rejects null key")
    @MethodSource("mapSubjectStream")
    void danger_nullRejectionMap(MapSubject subject) {
        NavigableMap<Integer, String> m = subject.build(new Integer[]{1, 2, 3}, new String[]{"a", "b", "c"});
        assertThrows(NullPointerException.class, () -> m.put(null, "x"), subject.name + " -> put(null, x)");
    }

    @ParameterizedTest(name = "{0} allows null value")
    @MethodSource("mapSubjectStream")
    void danger_nullValueAllowedMap(MapSubject subject) {
        NavigableMap<Integer, String> m = subject.build(new Integer[]{1, 2, 3}, new String[]{"a", "b", "c"});
        String c = subject.name;
        assertDoesNotThrow(() -> m.put(2, null), c + " -> put(key, null) must be allowed (ALLOWS_NULL_VALUES)");
        assertNull(m.get(2), c + " -> get() should return the stored null value");
        assertTrue(m.containsKey(2), c + " -> containsKey() must stay true after storing a null value");
    }


    @ParameterizedTest(name = "{0} fail-fast iterator")
    @MethodSource("setSubjectStream")
    void danger_concurrentModificationSet(SetSubject subject) {
        Integer[] arr = {1, 2, 3, 4, 5};
        NavigableSet<Integer> s = subject.build(arr);
        Iterator<Integer> it = s.iterator();
        it.next();
        s.add(999);
        assertThrows(ConcurrentModificationException.class, it::next,
                subject.name + " -> iterator must fail fast after concurrent structural modification");
    }


    @ParameterizedTest(name = "{0} natural-order comparator() is null")
    @MethodSource("setSubjectStream")
    void danger_comparatorNull(SetSubject subject) {
        NavigableSet<Integer> s = subject.build(new Integer[]{1, 2, 3});
        assertNull(s.comparator(), subject.name + " -> comparator() must be null for natural ordering");
    }

    @ParameterizedTest(name = "{0} insertion-order independence")
    @MethodSource("orderSensitiveStream")
    void danger_insertionOrderIndependence(SetSubject subject) {
        int n = 5000;
        Integer[] ascending = new Integer[n];
        Integer[] descending = new Integer[n];
        Integer[] random;
        TreeSet<Integer> truth = new TreeSet<>();
        for (int i = 0; i < n; i++) { ascending[i] = i; truth.add(i); }
        for (int i = 0; i < n; i++) descending[i] = n - 1 - i;
        List<Integer> rl = new ArrayList<>(truth);
        Collections.shuffle(rl, new Random(1234));
        random = rl.toArray(new Integer[0]);

        NavigableSet<Integer> viaAscending = subject.build(ascending);
        NavigableSet<Integer> viaDescending = subject.build(descending);
        NavigableSet<Integer> viaRandom = subject.build(random);

        assertEquals(truth, viaAscending, subject.name + " -> ascending-insert content mismatch");
        assertEquals(truth, viaDescending, subject.name + " -> descending-insert content mismatch (classic unbalanced-BST pathology)");
        assertEquals(truth, viaRandom, subject.name + " -> random-insert content mismatch");
        assertEquals(new ArrayList<>(truth), new ArrayList<>(viaDescending),
                subject.name + " -> descending-insert iteration order must still be sorted");
    }

    @ParameterizedTest(name = "{0} tiny sizes 0..6")
    @MethodSource("setSubjectStream")
    void danger_tinySizes(SetSubject subject) {
        for (int n = 0; n <= 6; n++) {
            Integer[] arr = new Integer[n];
            TreeSet<Integer> truth = new TreeSet<>();
            for (int i = 0; i < n; i++) { arr[i] = i; truth.add(i); }
            NavigableSet<Integer> s = subject.build(arr);
            assertEquals(truth, s, subject.name + " -> mismatch at n=" + n);

            // destruction via remove() in reverse-insertion order and verify no chaos at each step
            for (int i = n - 1; i >= 0; i--) {
                assertTrue(s.remove(i), subject.name + " -> failed to remove " + i + " at n=" + n);
                assertEquals(i, s.size(), subject.name + " -> size drift removing " + i + " at n=" + n);
            }
        }
    }

    /*
    If the cycle is 16, and we do 12 crossings (16 * 12 = 192), here is what the offsets do:
    offset = 0 (Size 192): Perfect Chunking. The array ends exactly as a leaf finishes. Every node is perfectly legal.
    offset = -1 (Size 191): Starved Tail. The array ends one element early. The final leaf was supposed to get 15 keys
     but it only gets 14. so it does see any rebalance
    offset = +1 (Size 193): The Ghost Leaf. The lethal point comes to this ghos leaf just for one key it created a node and
    only one box which completely violated the CLRS system!!!
     */


    private static int leafCapacity(int degree, float factor) {
        int kMin = degree - 1;
        int kMax = 2 * degree - 1;
        return Math.max(kMin, (int) Math.floor(kMax * factor));
    }

    private static int cycleSize(int degree, float factor) {
        return leafCapacity(degree, factor) + 1; // T_k leaf keys + 1 routing key
    }

    // True iff, at this (degree, factor), a Ghost Leaf's left sibling has NO
    // slack -- i.e. T_k == k_min, so Lemma 4.2 (merge) is the only legal repair.
    private static boolean forcesGhostLeafMerge(int degree, float factor) {
        return leafCapacity(degree, factor) == degree - 1;
    }

    private static final int[] BOUNDARY_OFFSETS = {-1, 0, 1};

    @ParameterizedTest(name = "{0} degree={1} factor={2}: poll-drain around Ghost Leaf boundary")
    @MethodSource("degreeFactorFamilyGridSets")
    void danger_pollDrainAcrossGhostLeafBoundary(BulkSetFamily family, int degree, float factor) {
        int cycle = cycleSize(degree, factor);
        String base = family.name + "[d=" + degree + ",f=" + factor + ",cycle=" + cycle
                + ",mergeForced=" + forcesGhostLeafMerge(degree, factor) + "]";

        for (int crossing : new int[]{12, 13}) {
            for (int offset : BOUNDARY_OFFSETS) {
                int n = cycle * crossing + offset;
                if (n <= 0) continue;
                String c = base + " n=" + n + " (crossing=" + crossing + ",offset=" + offset + ")";

                Integer[] arr = new Integer[n];
                for (int i = 0; i < n; i++) arr[i] = i;

                NavigableSet<Integer> s = family.build(degree, factor, arr);

                assertEquals(n, s.size(), c + " -> size wrong immediately after bulk build");

                Integer lastLow = null, lastHigh = null;
                boolean pollLow = true;
                int remaining = n;
                while (remaining > 0) {
                    if (pollLow) {
                        Integer v = s.pollFirst();
                        assertNotNull(v, c + " -> pollFirst() returned null with " + remaining + " elements left");
                        if (lastLow != null) assertTrue(v > lastLow, c + " -> pollFirst() not increasing");
                        lastLow = v;
                    } else {
                        Integer v = s.pollLast();
                        assertNotNull(v, c + " -> pollLast() returned null with " + remaining + " elements left");
                        if (lastHigh != null) assertTrue(v < lastHigh, c + " -> pollLast() not decreasing");
                        lastHigh = v;
                    }
                    remaining--;
                    pollLow = !pollLow;
                }
                assertTrue(s.isEmpty(), c + " -> not empty after alternating poll-drain, size=" + s.size());
            }
        }
    }

    static Stream<Object[]> degreeFactorFamilyGridSets() {
        List<Object[]> combos = new ArrayList<>();
        for (BulkSetFamily fam : BULK_SET_FAMILIES) {
            for (int degree : SAMPLE_DEGREES) {
                for (float f : FACTORS) {
                    combos.add(new Object[]{fam, degree, f});
                }
            }
        }
        return combos.stream();
    }

    static final class BulkSetFamily {
        final String name;
        final BiFunction<Integer, Float, Function<Integer[], NavigableSet<Integer>>> factory;
        BulkSetFamily(String name,
                      BiFunction<Integer, Float, Function<Integer[], NavigableSet<Integer>>> factory) {
            this.name = name;
            this.factory = factory;
        }
        NavigableSet<Integer> build(int degree, float factor, Integer[] arr) {
            return factory.apply(degree, factor).apply(arr);
        }
    }

    static final class BulkMapFamily {
        final String name;
        final BiFunction<Integer, Float, BiFunction<Integer[], String[], NavigableMap<Integer, String>>> factory;
        BulkMapFamily(String name,
                      BiFunction<Integer, Float, BiFunction<Integer[], String[], NavigableMap<Integer, String>>> factory) {
            this.name = name;
            this.factory = factory;
        }
        NavigableMap<Integer, String> build(int degree, float factor, Integer[] ks, String[] vs) {
            return factory.apply(degree, factor).apply(ks, vs);
        }
    }

    static final List<BulkSetFamily> BULK_SET_FAMILIES = List.of(
            new BulkSetFamily("BTreeSet", (d, f) ->
                    ks -> BTreeSet.Builder.<Integer>degree(d).factor(f).importFlatMatrix(ks).build()),
            new BulkSetFamily("BPlusTreeSet", (d, f) ->
                    ks -> BPlusTreeSet.Builder.<Integer>degree(d).factor(f).importFlatMatrix(ks).build())
    );

    static final List<BulkMapFamily> BULK_MAP_FAMILIES = List.of(
            new BulkMapFamily("BTreeMap", (d, f) ->
                    (ks, vs) -> BTreeMap.Builder.<Integer, String>degree(d).factor(f)
                            .importFlatMatrix(new Object[][]{ks, vs}).build()),
            new BulkMapFamily("BPlusTreeMap", (d, f) ->
                    (ks, vs) -> BPlusTreeMap.Builder.<Integer, String>degree(d).factor(f)
                            .importFlatMatrix(new Object[][]{ks, vs}).build())
    );

    static Stream<Object[]> fullDegreeFactorFamilyGridSets() {
        List<Object[]> combos = new ArrayList<>();
        for (BulkSetFamily fam : BULK_SET_FAMILIES) {
            for (int degree : FULL_DEGREES) {
                for (float f : FACTORS) {
                    combos.add(new Object[]{fam, degree, f});
                }
            }
        }
        return combos.stream();
    }
    static Stream<Object[]> fullDegreeFactorFamilyGridMaps() {
        List<Object[]> combos = new ArrayList<>();
        for (BulkMapFamily fam : BULK_MAP_FAMILIES) {
            for (int degree : FULL_DEGREES) {
                for (float f : FACTORS) {
                    combos.add(new Object[]{fam, degree, f});
                }
            }
        }
        return combos.stream();
    }

    @ParameterizedTest(name = "{0} degree={1} factor={2}: Ghost Leaf boundary (full degree sweep)")
    @MethodSource("fullDegreeFactorFamilyGridSets")
    void danger_ghostLeafBoundaryFullDegreeSweepSet(BulkSetFamily family, int degree, float factor) {
        int cycle = cycleSize(degree, factor);
        int n = cycle * 3;
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) arr[i] = i;

        NavigableSet<Integer> s = family.build(degree, factor, arr);
        String c = family.name + "[d=" + degree + ",f=" + factor + ",n=" + n
                + ",mergeForced=" + forcesGhostLeafMerge(degree, factor) + "]";

        assertEquals(n, s.size(), c + " -> size wrong after boundary-exact build");
        Integer prev = null;
        for (Integer k : s) {
            if (prev != null) assertTrue(k > prev, c + " -> iteration order broken near Ghost Leaf repair point");
            prev = k;
        }
    }

    @ParameterizedTest(name = "{0} degree={1} factor={2}: Ghost Leaf boundary (full degree sweep, map)")
    @MethodSource("fullDegreeFactorFamilyGridMaps")
    void danger_ghostLeafBoundaryFullDegreeSweepMap(BulkMapFamily family, int degree, float factor) {
        int cycle = cycleSize(degree, factor);
        int n = cycle * 3;
        Integer[] ks = new Integer[n];
        String[] vs = new String[n];
        for (int i = 0; i < n; i++) { ks[i] = i; vs[i] = "v" + i; }

        NavigableMap<Integer, String> m = family.build(degree, factor, ks, vs);
        String c = family.name + "[d=" + degree + ",f=" + factor + ",n=" + n
                + ",mergeForced=" + forcesGhostLeafMerge(degree, factor) + "]";

        assertEquals(n, m.size(), c + " -> size wrong after boundary-exact build");
        Integer prevKey = null;
        for (Integer k : m.keySet()) {
            if (prevKey != null) assertTrue(k > prevKey, c + " -> iteration order broken near Ghost Leaf repair point");
            prevKey = k;
        }
    }

    @Test
    void danger_ghostLeafMergeBranchIsActuallyExercised() {
        boolean anyMergeForced = false;
        for (BulkSetFamily family : BULK_SET_FAMILIES) {
            for (int degree : SAMPLE_DEGREES) {
                for (float f : FACTORS) {
                    if (forcesGhostLeafMerge(degree, f)) {
                        anyMergeForced = true;
                        int cycle = cycleSize(degree, f);
                        int n = cycle * 3;
                        Integer[] arr = new Integer[n];
                        for (int i = 0; i < n; i++) arr[i] = i;
                        NavigableSet<Integer> s = family.build(degree, f, arr);
                        String c = family.name + "[d=" + degree + ",f=" + f + ",n=" + n + "] (merge-forced Ghost Leaf)";
                        assertEquals(n, s.size(), c + " -> size wrong after merge-forced boundary build");
                        Integer prev = null;
                        for (Integer k : s) {
                            if (prev != null) assertTrue(k > prev, c + " -> iteration order broken near merge point");
                            prev = k;
                        }
                    }
                }
            }
        }
        assertTrue(anyMergeForced,
                "No (degree, factor) in the sample grid forces the Lemma 4.2 merge branch -- "
                        + "SAMPLE_DEGREES/FACTORS changed and silently dropped merge-path coverage entirely.");
    }
}
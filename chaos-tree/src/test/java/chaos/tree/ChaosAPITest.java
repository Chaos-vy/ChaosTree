package chaos.tree;

import chaos.tree.nary.BPlusTreeSet;
import chaos.tree.nary.BTreeSet;
import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.naryMap.BTreeMap;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.FloatRange;
import net.jqwik.api.constraints.IntRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These tests are specifically intended for API-breaking analysis of my
 * tree implementations.
 * // arrayDegree is currently inconsistent in importFromMatrix() and will be upgraded.
 * fixed at >=32
 * the degree 32 is chosen as minimum for serialization through array was for most speed
 * at degree 32 a maximum ht can only be 7~8 which does not need much more balancing
 * of top to bottom and bottom to up fixup.
 * If you disagree, you can clone the repository, change the
 * Degree configuration in these tests, run the suite, and provide the
 * results.
 * I have explicitly tested the same way.
 * Genuine testers are welcome to contribute additional Degree configurations,
 * tests, bug reports, and PRs.
 * There might be uncovered test that I cannot see, or I might have already done please do report the bug
 */
class ChaosAPITest {

    // MAP

    /**
     * Every claim you see set's feat of result for chaosTree it is more rigorous
     * if you feels it is less open a PR for more rigorous test.
     * The below test result is of ChaosTree when @Property(tries = 1000000)
     * <pre>
     * timestamp = 2026-09-07T17:58:43.339854508, ChaosAPITest:naryChaosMap =
     *                               |-----------------------jqwik-----------------------
     * tries = 1000000               | # of calls to property
     * checks = 1000000              | # of not rejected calls
     * generation = RANDOMIZED       | parameters are randomly generated
     * after-failure = SAMPLE_FIRST  | try previously failed sample, then previous seed
     * when-fixed-seed = ALLOW       | fixing the random seed is allowed
     * edge-cases#mode = MIXIN       | edge cases are mixed in
     * edge-cases#total = 120        | # of all combined edge cases
     * edge-cases#tried = 120        | # of edge cases tried in current run
     * seed = 8737162768835411743    | random seed to reproduce generated values
     * </pre>
     *
     * @param arrayDegree arrayDegree
     * @param size        size
     * @param factor      factor
     * @param isBPlusTree isBPlusTree
     */

    @Property(tries = 1000)
    //Do set 1M for ChaosTest
    void naryChaosMap(
            @ForAll @IntRange(min = 32, max = 256) int arrayDegree, //Do change the builder from array concrete class if you want to test at >=2
            @ForAll @IntRange(min = 0, max = 49999) int size,
            @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor,
            @ForAll boolean isBPlusTree
    ) {
        Integer[] keys = new Integer[size];
        String[] values = new String[size];
        TreeMap<Integer, String> truth = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            keys[i] = i;
            values[i] = "Chaos-" + i;
            truth.put(i, values[i]);
        }
        Object[][] flat = {keys, values};

        if (isBPlusTree) {//creation
            BPlusTreeMap<Integer, String> tree = BPlusTreeMap.Builder.<Integer, String>create(arrayDegree)
                    .factor(factor).importFlatMatrix(flat).build();

            verifyFullMapApiSurface(tree, truth, "initial build (BPlusTreeMap)");
            //destruction
            Object[][] exportedFlat = tree.exportFlatMatrix();
            assertExportShape(exportedFlat, size);
            //recreation
            tree = BPlusTreeMap.Builder.<Integer, String>create(arrayDegree)
                    .factor(factor).importFlatMatrix(exportedFlat).build();

            verifyFullMapApiSurface(tree, truth, "post-reconstruction (BPlusTreeMap)");
            drainAndVerifyEmptyMap(tree, keys);
            //Chaos Commencement!
        } else {
            BTreeMap<Integer, String> tree = BTreeMap.Builder.<Integer, String>create(arrayDegree)
                    .factor(factor).importFlatMatrix(flat).build();

            verifyFullMapApiSurface(tree, truth, "initial build (BTreeMap)");

            Object[][] exportedFlat = tree.exportFlatMatrix();
            assertExportShape(exportedFlat, size);

            tree = BTreeMap.Builder.<Integer, String>create(arrayDegree)
                    .factor(factor).importFlatMatrix(exportedFlat).build();

            verifyFullMapApiSurface(tree, truth, "post-reconstruction (BTreeMap)");
            drainAndVerifyEmptyMap(tree, keys);
        }
    }

    private void assertExportShape(Object[][] exportedFlat, int expectedSize) {
        assertNotNull(exportedFlat, "exportFlatArray() returned null");
        assertEquals(2, exportedFlat.length, "exportFlatArray() must return [keys, values]");
        assertEquals(expectedSize, exportedFlat[0].length, "exported key array length mismatch");
        assertEquals(expectedSize, exportedFlat[1].length, "exported value array length mismatch");
    }

    private void drainAndVerifyEmptyMap(NavigableMap<Integer, String> tree, Integer[] keys) {
        List<Integer> removalPlan = new ArrayList<>(Arrays.asList(keys));
        Collections.shuffle(removalPlan);
        int expectedSize = tree.size();
        for (Integer key : removalPlan) {
            String removed = tree.remove(key);
            assertNotNull(removed, "remove() returned null for " + key);
            expectedSize--;
            assertEquals(expectedSize, tree.size(), "size drift after removing " + key);
        }
        assertTrue(tree.isEmpty(), "not empty after full drain");
        verifyEmptyMapContract(tree);
    }

    private void verifyFullMapApiSurface(NavigableMap<Integer, String> tree, TreeMap<Integer, String> truth, String phase) {
        assertEquals(truth.size(), tree.size(), phase + " -> size");
        assertEquals(truth.isEmpty(), tree.isEmpty(), phase + " -> isEmpty");

        if (!truth.isEmpty()) {
            assertTrue(tree.containsKey(truth.firstKey()), phase + " -> containsKey(firstKey)");
            assertEquals(truth.get(truth.firstKey()), tree.get(truth.firstKey()), phase + " -> get(firstKey)");
            assertTrue(tree.containsValue(truth.get(truth.firstKey())), phase + " -> containsValue");
        }
        assertFalse(tree.containsKey(-1), phase + " -> containsKey(out-of-range)");

        assertEquals(new ArrayList<>(truth.entrySet()), new ArrayList<>(tree.entrySet()), phase + " -> entrySet order");
        assertEquals(new ArrayList<>(truth.keySet()), new ArrayList<>(tree.keySet()), phase + " -> keySet order");
        assertEquals(new ArrayList<>(truth.values()), new ArrayList<>(tree.values()), phase + " -> values order");

        List<Integer> expectedDescKeys = new ArrayList<>(truth.keySet());
        Collections.reverse(expectedDescKeys);
        assertEquals(expectedDescKeys, new ArrayList<>(tree.descendingKeySet()), phase + " -> descendingKeySet order");

        if (truth.isEmpty()) {
            assertThrows(NoSuchElementException.class, tree::firstKey, phase + " -> firstKey() on empty");
            assertThrows(NoSuchElementException.class, tree::lastKey, phase + " -> lastKey() on empty");
        } else {
            assertEquals(truth.firstKey(), tree.firstKey(), phase + " -> firstKey()");
            assertEquals(truth.lastKey(), tree.lastKey(), phase + " -> lastKey()");
            assertEquals(truth.firstEntry().getKey(), tree.firstEntry().getKey(), phase + " -> firstEntry()");
            assertEquals(truth.lastEntry().getKey(), tree.lastEntry().getKey(), phase + " -> lastEntry()");
        }

        int[] probes = truth.isEmpty()
                ? new int[]{0}
                : new int[]{-1, 0, truth.firstKey(), truth.lastKey(), truth.size() / 2, truth.lastKey() + 1};
        for (int p : probes) {
            assertEquals(truth.floorKey(p), tree.floorKey(p), phase + " -> floorKey(" + p + ")");
            assertEquals(truth.ceilingKey(p), tree.ceilingKey(p), phase + " -> ceilingKey(" + p + ")");
            assertEquals(truth.higherKey(p), tree.higherKey(p), phase + " -> higherKey(" + p + ")");
            assertEquals(truth.lowerKey(p), tree.lowerKey(p), phase + " -> lowerKey(" + p + ")");
        }

        if (!truth.isEmpty()) {
            List<Integer> keyList = new ArrayList<>(truth.keySet());
            int mid = keyList.size() / 2;
            int lo = keyList.get(Math.max(0, mid - 1));
            int hi = keyList.get(Math.min(keyList.size() - 1, mid + 1));
            assertEquals(truth.headMap(hi, true).keySet(), tree.headMap(hi, true).keySet(), phase + " -> headMap inclusive");
            assertEquals(truth.tailMap(lo, true).keySet(), tree.tailMap(lo, true).keySet(), phase + " -> tailMap inclusive");
            assertEquals(truth.subMap(lo, true, hi, true).keySet(), tree.subMap(lo, true, hi, true).keySet(), phase + " -> subMap [lo,hi]");
        }

        assertNull(tree.comparator(), phase + " -> comparator() should be null for natural order");
        assertEquals(truth, tree, phase + " -> equals(truth)");
        assertEquals(truth.hashCode(), tree.hashCode(), phase + " -> hashCode() must match Map contract");
    }

    // Just to ensure that it must pass
    private void verifyEmptyMapContract(NavigableMap<Integer, String> tree) {
        assertTrue(tree.isEmpty());
        assertThrows(NoSuchElementException.class, tree::firstKey);
        assertThrows(NoSuchElementException.class, tree::lastKey);
        assertNull(tree.floorEntry(0));
        assertNull(tree.ceilingEntry(0));
        assertNull(tree.higherEntry(0));
        assertNull(tree.lowerEntry(0));
        assertTrue(tree.descendingMap().isEmpty());
    }

    // SET

    /**
     * <pre>
     *     timestamp = 2026-09-07T18:00:29.694989531, ChaosAPITest:naryChaosSet =
     *                               |-----------------------jqwik-----------------------
     * tries = 1000000               | # of calls to property
     * checks = 1000000              | # of not rejected calls
     * generation = RANDOMIZED       | parameters are randomly generated
     * after-failure = SAMPLE_FIRST  | try previously failed sample, then previous seed
     * when-fixed-seed = ALLOW       | fixing the random seed is allowed
     * edge-cases#mode = MIXIN       | edge cases are mixed in
     * edge-cases#total = 120        | # of all combined edge cases
     * edge-cases#tried = 120        | # of edge cases tried in current run
     * seed = 6563887724987797715    | random seed to reproduce generated values
     * </pre>
     *
     * @param arrayDegree
     * @param size
     * @param factor
     * @param isBPlusTree
     */

    @Property(tries = 1000)
    void naryChaosSet(
            @ForAll @IntRange(min = 32, max = 256) int arrayDegree,
            @ForAll @IntRange(min = 0, max = 49999) int size,
            @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor,
            @ForAll boolean isBPlusTree
    ) {
        Integer[] keys = new Integer[size];
        TreeSet<Integer> truth = new TreeSet<>();
        for (int i = 0; i < size; i++) {
            keys[i] = i;
            truth.add(i);
        }

        if (isBPlusTree) {
            BPlusTreeSet<Integer> tree = BPlusTreeSet.Builder.<Integer>create(arrayDegree)
                    .factor(factor).importFlatArray(keys).build();

            verifyFullSetApiSurface(tree, truth, "initial build (BPlusTreeSet)");

            Integer[] exported = tree.toArray(new Integer[0]);
            assertEquals(size, exported.length, "exported array length mismatch");

            tree = BPlusTreeSet.Builder.<Integer>create(arrayDegree)
                    .factor(factor).importFlatArray(exported).build();

            verifyFullSetApiSurface(tree, truth, "post-reconstruction (BPlusTreeSet)");
            drainAndVerifyEmptySet(tree, keys);
        } else {
            BTreeSet<Integer> tree = BTreeSet.Builder.<Integer>create(arrayDegree)
                    .factor(factor).importFlatArray(keys).build();

            verifyFullSetApiSurface(tree, truth, "initial build (BTreeSet)");

            Integer[] exported = tree.toArray(new Integer[0]);
            assertEquals(size, exported.length, "exported array length mismatch");

            tree = BTreeSet.Builder.<Integer>create(arrayDegree)
                    .factor(factor).importFlatArray(exported).build();

            verifyFullSetApiSurface(tree, truth, "post-reconstruction (BTreeSet)");
            drainAndVerifyEmptySet(tree, keys);
        }
    }

    private void drainAndVerifyEmptySet(NavigableSet<Integer> tree, Integer[] keys) {
        List<Integer> removalPlan = new ArrayList<>(Arrays.asList(keys));
        Collections.shuffle(removalPlan);
        int expectedSize = tree.size();
        for (Integer key : removalPlan) {
            assertTrue(tree.remove(key), "remove() returned false for " + key);
            expectedSize--;
            assertEquals(expectedSize, tree.size(), "size drift after removing " + key);
        }
        assertTrue(tree.isEmpty(), "not empty after full drain");
        verifyEmptySetContract(tree);
    }

    private void verifyFullSetApiSurface(NavigableSet<Integer> tree, TreeSet<Integer> truth, String phase) {
        assertEquals(truth.size(), tree.size(), phase + " -> size");
        assertEquals(truth.isEmpty(), tree.isEmpty(), phase + " -> isEmpty");

        if (!truth.isEmpty()) {
            assertTrue(tree.contains(truth.first()), phase + " -> contains(first)");
            assertTrue(tree.contains(truth.last()), phase + " -> contains(last)");
        }
        assertFalse(tree.contains(-1), phase + " -> contains(out-of-range)");
        assertTrue(tree.containsAll(truth), phase + " -> containsAll(truth)");

        assertEquals(new ArrayList<>(truth), new ArrayList<>(tree), phase + " -> forward iteration order");
        List<Integer> expectedDesc = new ArrayList<>(truth);
        Collections.reverse(expectedDesc);
        assertEquals(expectedDesc, new ArrayList<>(tree.descendingSet()), phase + " -> descendingSet order");

        if (truth.isEmpty()) {
            assertThrows(NoSuchElementException.class, tree::first, phase + " -> first() on empty");
            assertThrows(NoSuchElementException.class, tree::last, phase + " -> last() on empty");
        } else {
            assertEquals(truth.first(), tree.first(), phase + " -> first()");
            assertEquals(truth.last(), tree.last(), phase + " -> last()");
        }

        int[] probes = truth.isEmpty()
                ? new int[]{0}
                : new int[]{-1, 0, truth.first(), truth.last(), truth.size() / 2, truth.last() + 1};
        for (int p : probes) {
            assertEquals(truth.floor(p), tree.floor(p), phase + " -> floor(" + p + ")");
            assertEquals(truth.ceiling(p), tree.ceiling(p), phase + " -> ceiling(" + p + ")");
            assertEquals(truth.higher(p), tree.higher(p), phase + " -> higher(" + p + ")");
            assertEquals(truth.lower(p), tree.lower(p), phase + " -> lower(" + p + ")");
        }

        if (!truth.isEmpty()) {
            int mid = truth.size() / 2;
            List<Integer> list = new ArrayList<>(truth);
            int lo = list.get(Math.max(0, mid - 1));
            int hi = list.get(Math.min(list.size() - 1, mid + 1));
            assertEquals(truth.headSet(hi, true), tree.headSet(hi, true), phase + " -> headSet inclusive");
            assertEquals(truth.tailSet(lo, true), tree.tailSet(lo, true), phase + " -> tailSet inclusive");
            assertEquals(truth.subSet(lo, true, hi, true), tree.subSet(lo, true, hi, true), phase + " -> subSet [lo,hi]");
        }

        assertNull(tree.comparator(), phase + " -> comparator() should be null for natural order");
        assertArrayEquals(truth.toArray(), tree.toArray(), phase + " -> toArray()");
        assertEquals(truth, tree, phase + " -> equals(truth)");
        assertEquals(truth.hashCode(), tree.hashCode(), phase + " -> hashCode() must match Set contract");
    }

    private void verifyEmptySetContract(NavigableSet<Integer> tree) {
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertThrows(NoSuchElementException.class, tree::first);
        assertThrows(NoSuchElementException.class, tree::last);
        assertNull(tree.floor(0));
        assertNull(tree.ceiling(0));
        assertNull(tree.higher(0));
        assertNull(tree.lower(0));
        assertFalse(tree.iterator().hasNext());
        assertFalse(tree.contains(0));
        assertTrue(tree.subSet(0, true, 100, true).isEmpty());
        assertTrue(tree.descendingSet().isEmpty());
    }

    @Property(tries = 1000)
    void dragonFeedBrutalDestruction(
            @ForAll @IntRange(min = 32, max = 128) int degree,
            @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor,
            @ForAll @IntRange(min = 0, max = 5000) int size,
            @ForAll boolean useArrayLoad,
            @ForAll boolean useBPlusTree
    ) {
        List<Integer> sortedData = new ArrayList<>(size); // sorted dat
        for (int i = 0; i < size; i++) {
            sortedData.add(i);
        }
        NavigableSet<Integer> tree;
        if (useBPlusTree) {
            var builder = BPlusTreeSet.Builder.<Integer>create(degree).factor(factor);
            tree = useArrayLoad
                    ? builder.importFlatArray(sortedData.toArray(new Integer[0])).build()
                    : builder.importSorted(sortedData.iterator()).build();
        } else {
            var builder = BTreeSet.Builder.<Integer>create(degree).factor(factor);
            tree = useArrayLoad
                    ? builder.importFlatArray(sortedData.toArray(new Integer[0])).build()
                    : builder.importSorted(sortedData.iterator()).build();
        }

        // 3. Verify immediate structural invariants
        assertEquals(size, tree.size(), "Size mismatch immediately after bulk load");

        //verify iteration integrity
        List<Integer> iterated = new ArrayList<>(size);
        for (Integer val : tree) {
            iterated.add(val);
        }
        assertEquals(sortedData, iterated, "Iteration output doesn't match bulk load input");

        // 5. Total random demolition: remove elements in completely random order
        List<Integer> removalPlan = new ArrayList<>(sortedData);
        Collections.shuffle(removalPlan);

        int currentSize = size;
        for (Integer target : removalPlan) {
            boolean removed = tree.remove(target);
            assertTrue(removed, () -> "Failed to remove existing element " + target + " (degree=" + degree + ", factor=" + factor + ", initial_size=" + size + ")");
            currentSize--;
            assertEquals(currentSize, tree.size(), "Size did not decrement properly after removal");
        }

        assertTrue(tree.isEmpty(), "Tree should be fully empty after complete demolition");
    }

    @Property(tries = 1000)
    void dragonFeedBrutalDestructionMapCustomComparator(
            @ForAll @IntRange(min = 32, max = 2048) int degree,
            @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor,
            @ForAll @IntRange(min = 0, max = 100000) int size,
            @ForAll boolean useArrayLoad,
            @ForAll boolean useBPlusTreeMap,
            @ForAll boolean reverseOrder
    ) {
        Comparator<ChaosAPITest.ChaosKey> cmp = reverseOrder
                ? Comparator.<ChaosAPITest.ChaosKey>comparingInt(k -> k.primary).reversed()
                : Comparator.comparingInt(k -> k.primary);

        ChaosAPITest.ChaosKey[] x1 = new ChaosAPITest.ChaosKey[size];
        ChaosAPITest.ChaosKey[] x2 = new ChaosAPITest.ChaosKey[size];
        TreeMap<ChaosAPITest.ChaosKey, ChaosAPITest.ChaosKey> sortedData = new TreeMap<>(cmp);

        for (int i = 0; i < size; i++) {
            ChaosAPITest.ChaosKey k = new ChaosAPITest.ChaosKey(i, "field-" + i);
            sortedData.put(k, k);
        }
        int idx = 0;
        for (ChaosAPITest.ChaosKey k : sortedData.keySet()) {
            x1[idx] = k;
            x2[idx] = k;
            idx++;
        }
        ChaosAPITest.ChaosKey[][] flat = new ChaosAPITest.ChaosKey[][]{x1, x2};

        NavigableMap<ChaosAPITest.ChaosKey, ChaosAPITest.ChaosKey> tree;
        if (useBPlusTreeMap) {
            var builder = BPlusTreeMap.Builder.<ChaosAPITest.ChaosKey, ChaosAPITest.ChaosKey>create(degree)
                    .comparator(cmp).factor(factor);
            tree = useArrayLoad
                    ? builder.importFlatMatrix(flat).build()
                    : builder.importSorted(sortedData.entrySet().iterator()).build();
        } else {
            var builder = BTreeMap.Builder.<ChaosAPITest.ChaosKey, ChaosAPITest.ChaosKey>create(degree)
                    .comparator(cmp).factor(factor);
            tree = useArrayLoad
                    ? builder.importFlatMatrix(flat).build()
                    : builder.importSorted(sortedData.entrySet().iterator()).build();
        }

        assertEquals(size, tree.size(), "Size mismatch immediately after bulk load");

        ChaosAPITest.ChaosKey[] x1tru = new ChaosAPITest.ChaosKey[size];
        int k = 0;
        for (Map.Entry<ChaosAPITest.ChaosKey, ChaosAPITest.ChaosKey> e : tree.entrySet()) {
            x1tru[k++] = e.getKey();
        }
        assertArrayEquals(x1, x1tru, "Custom-comparator iteration order mismatch");

        if (size > 0) {
            int probeIdx = size / 2;
            ChaosAPITest.ChaosKey freshLookupKey = new ChaosAPITest.ChaosKey(x1[probeIdx].primary, x1[probeIdx].secondary);
            assertTrue(tree.containsKey(freshLookupKey),
                    "lookup via a distinct-but-equal ChaosKey instance failed");
            assertEquals(x1[probeIdx], tree.get(freshLookupKey));
        }

        List<ChaosAPITest.ChaosKey> removalPlan = new ArrayList<>();
        for (ChaosAPITest.ChaosKey orig : x1) {
            removalPlan.add(new ChaosAPITest.ChaosKey(orig.primary, orig.secondary));
        }
        Collections.shuffle(removalPlan);

        int currentSize = size;
        for (ChaosAPITest.ChaosKey target : removalPlan) {
            ChaosAPITest.ChaosKey removed = tree.remove(target);
            assertNotNull(removed, "remove() failed via distinct-but-equal instance for " + target);
            assertEquals(target.primary, removed.primary);
            currentSize--;
            assertEquals(currentSize, tree.size());
        }
        assertTrue(tree.isEmpty());
        //Finally passed!!!!
    }

    /**
     * <pre>
     *     timestamp = 2026-09-07T18:20:40.384380205, ChaosAPITest:naryChaosMapInsertAfterBulkLoad =
     *                               |-----------------------jqwik-----------------------
     * tries = 20000                 | # of calls to property
     * checks = 20000                | # of not rejected calls
     * generation = RANDOMIZED       | parameters are randomly generated
     * after-failure = SAMPLE_FIRST  | try previously failed sample, then previous seed
     * when-fixed-seed = ALLOW       | fixing the random seed is allowed
     * edge-cases#mode = MIXIN       | edge cases are mixed in
     * edge-cases#total = 480        | # of all combined edge cases
     * edge-cases#tried = 480        | # of edge cases tried in current run
     * seed = -7959454431507407185   | random seed to reproduce generated values
     * </pre>
     *
     * @param arrayDegree
     * @param factor
     * @param size
     * @param isBPlusTree
     * @param insertCount
     */
    @Property(tries = 1000)
    void naryChaosMapInsertAfterBulkLoad(
            @ForAll @IntRange(min = 32, max = 256) int arrayDegree,
            @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor,
            @ForAll @IntRange(min = 0, max = 49999) int size,
            @ForAll boolean isBPlusTree,
            @ForAll @IntRange(min = 1, max = 5000) int insertCount
    ) {
        Integer[] keys = new Integer[size];
        String[] values = new String[size];
        TreeMap<Integer, String> truth = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            keys[i] = i;
            values[i] = "Chaos-" + i;
            truth.put(i, values[i]);
        }
        Object[][] flat = {keys, values};

        NavigableMap<Integer, String> tree;
        if (isBPlusTree) {
            tree = BPlusTreeMap.Builder.<Integer, String>create(arrayDegree).factor(factor).importFlatMatrix(flat).build();
        } else {
            tree = BTreeMap.Builder.<Integer, String>create(arrayDegree).factor(factor).importFlatMatrix(flat).build();
        }

        int base = size; // keys size..size+insertCount-1 are guaranteed new
        for (int i = 0; i < insertCount; i++) {
            int newKey = base + i;
            String prior = tree.put(newKey, "INSERTED-" + newKey);
            truth.put(newKey, "INSERTED-" + newKey);
            assertNull(prior, "put() on a genuinely new key must return null, key=" + newKey);
        }
        assertEquals(truth.size(), tree.size(), "size mismatch after post-bulk-load insertion");

        assertEquals(new ArrayList<>(truth.entrySet()), new ArrayList<>(tree.entrySet()),
                "entry order corrupted after post-bulk-load insertion");

        if (size > 0) {
            int existingKey = size / 2;
            String prior = tree.put(existingKey, "OVERWRITTEN");
            assertEquals("Chaos-" + existingKey, prior, "put() overwrite must return prior value");
            assertEquals("OVERWRITTEN", tree.get(existingKey));
            tree.put(existingKey, "Chaos-" + existingKey); // restore for the drain check below
        }

        List<Integer> allKeys = new ArrayList<>(truth.keySet());
        Collections.shuffle(allKeys);
        int expectedSize = tree.size();
        for (Integer key : allKeys) {
            String removed = tree.remove(key);
            assertNotNull(removed, "remove() returned null for " + key + " after insert-then-drain");
            expectedSize--;
            assertEquals(expectedSize, tree.size());
        }
        assertTrue(tree.isEmpty());
    }

    /**
     * <pre>
     *     timestamp = 2026-09-07T18:23:16.759648726, ChaosAPITest:naryChaosMapInterspersedInsertAfterBulkLoad =
     *                               |-----------------------jqwik-----------------------
     * tries = 20000                 | # of calls to property
     * checks = 20000                | # of not rejected calls
     * generation = RANDOMIZED       | parameters are randomly generated
     * after-failure = SAMPLE_FIRST  | try previously failed sample, then previous seed
     * when-fixed-seed = ALLOW       | fixing the random seed is allowed
     * edge-cases#mode = MIXIN       | edge cases are mixed in
     * edge-cases#total = 384        | # of all combined edge cases
     * edge-cases#tried = 384        | # of edge cases tried in current run
     * seed = -6366506117042155189   | random seed to reproduce generated values
     * </pre>
     *
     * @param arrayDegree
     * @param factor
     * @param size
     * @param isBPlusTree
     * @param insertCount
     */
    @Property(tries = 1000)
    void naryChaosMapInterspersedInsertAfterBulkLoad(
            @ForAll @IntRange(min = 32, max = 256) int arrayDegree,
            @ForAll @FloatRange(min = 0.5f, max = 1.0f) float factor,
            @ForAll @IntRange(min = 1, max = 49999) int size, // min 1: need room to intersperse
            @ForAll boolean isBPlusTree,
            @ForAll @IntRange(min = 1, max = 2000) int insertCount
    ) {
        // Bulk-load with a GAP: even numbers only, 0, 2, 4, ... so odd numbers
        // are guaranteed free slots scattered through the LEFT side, MIDDLE,
        // and RIGHT side alike — not just past the max like the previous test.
        Integer[] keys = new Integer[size];
        String[] values = new String[size];
        TreeMap<Integer, String> truth = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            int k = i * 2;
            keys[i] = k;
            values[i] = "Chaos-" + k;
            truth.put(k, values[i]);
        }
        Object[][] flat = {keys, values};

        NavigableMap<Integer, String> tree;
        if (isBPlusTree) {
            tree = BPlusTreeMap.Builder.<Integer, String>create(arrayDegree).factor(factor).importFlatMatrix(flat).build();
        } else {
            tree = BTreeMap.Builder.<Integer, String>create(arrayDegree).factor(factor).importFlatMatrix(flat).build();
        }

        int maxEven = (size - 1) * 2;
        List<Integer> insertTargets = new ArrayList<>();
        Random rnd = new Random(); // jqwik seed already controls trial reproducibility upstream
        Set<Integer> chosen = new HashSet<>();
        int attempts = 0;
        while (insertTargets.size() < insertCount && attempts < insertCount * 20) {
            attempts++;
            int candidate = maxEven <= 0 ? 1 : rnd.nextInt(maxEven + 2); // may land below 0..maxEven+1
            int odd = (candidate % 2 == 0) ? candidate + 1 : candidate;
            if (odd < 0) odd = 1;
            if (chosen.add(odd)) insertTargets.add(odd);
        }

        for (int key : insertTargets) {
            String prior = tree.put(key, "INTERSPERSED-" + key);
            truth.put(key, "INTERSPERSED-" + key);
            assertNull(prior, "put() on a genuinely new interspersed key must return null, key=" + key);
        }

        assertEquals(truth.size(), tree.size(), "size mismatch after interspersed insertion");
        assertEquals(new ArrayList<>(truth.entrySet()), new ArrayList<>(tree.entrySet()),
                "entry order corrupted after interspersed (non-right-spine) insertion");

        if (size > 0) {
            int leftKey = keys[0]; // = 0, the leftmost bulk-loaded key
            String prior = tree.put(leftKey, "OVERWRITTEN-LEFT");
            assertEquals("Chaos-" + leftKey, prior, "put() overwrite on leftmost key must return prior value");
            tree.put(leftKey, "Chaos-" + leftKey); // restore
        }

        List<Integer> allKeys = new ArrayList<>(truth.keySet());
        Collections.shuffle(allKeys);
        int expectedSize = tree.size();
        for (Integer key : allKeys) {
            String removed = tree.remove(key);
            assertNotNull(removed, "remove() returned null for " + key + " after interspersed insert-then-drain");
            expectedSize--;
            assertEquals(expectedSize, tree.size());
        }
        assertTrue(tree.isEmpty());
    }

    static final class ChaosKey {
        final int primary;
        final String secondary;

        ChaosKey(int primary, String secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChaosAPITest.ChaosKey)) return false;
            ChaosAPITest.ChaosKey k = (ChaosAPITest.ChaosKey) o;
            return primary == k.primary && Objects.equals(secondary, k.secondary);
        }

        @Override
        public int hashCode() {
            return Objects.hash(primary, secondary);
        }

        @Override
        public String toString() {
            return "ChaosKey(" + primary + "," + secondary + ")";
        }
    }
}
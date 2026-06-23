package chaos.tree.nary;

import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.exception.EmptyTreeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public abstract class NaryTreeContractTest<NARY extends NaryTree<Integer>> {

    protected abstract NARY createTree(int degree);
    protected abstract NARY createFromIterable(int degree, Iterable<Integer> it);
    protected abstract NARY createCopy(NARY source);

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("Iterable constructor builds correct tree")
    void testIterableConstructorBuildsTree(int degree) {
        NARY built = createFromIterable(degree, Arrays.asList(30, 10, 50, 20, 40));
        assertEquals(5, built.size());
        assertTrue(built.contains(10));
        assertTrue(built.contains(50));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("Iterable constructor with empty list creates empty tree")
    void testIterableConstructorEmptyCreatesEmptyTree(int degree) {
        NARY built = createFromIterable(degree, Collections.emptyList());
        assertTrue(built.isEmpty());
        assertEquals(0, built.size());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    void testSequentialInsertion(int degree) {
        NARY tree = createTree(degree);
        for (int i = 1; i <= 100; i++) {
            tree.insert(i);
            assertEquals(i, tree.size());
            assertTrue(tree.contains(i));
        }

        for (int i = 1; i <= 100; i++) {
            assertTrue(tree.contains(i), "Tree lost element " + i + " during a split!");
        }
        assertFalse(tree.contains(101));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    void testReverseInsertion(int degree) {
        NARY tree = createTree(degree);
        for (int i = 100; i >= 1; i--) {
            tree.insert(i);
        }
        assertEquals(100, tree.size());
        assertTrue(tree.contains(50));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    void testDeletionLifecycle(int degree) {
        NARY tree = createTree(degree);

        IntStream.rangeClosed(1, 50).forEach(tree::insert);
        tree.delete(49);
        assertFalse(tree.contains(49));
        assertEquals(49, tree.size());

        tree.delete(25);
        tree.delete(30);
        assertFalse(tree.contains(25));
        assertFalse(tree.contains(30));

        for (int i = 1; i <= 40; i++) {
            if (tree.contains(i)) {
                tree.delete(i);
            }
        }

        assertTrue(tree.contains(45));
        assertTrue(tree.contains(50));
    }


    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("O(log N) + O(K) Range Query Extraction")
    void testRangeQuery(int degree) {
        NARY tree = createTree(degree);

        for (int i = 1; i <= 10; i++) {
            tree.insert(i * 10);
        }

        List<Integer> range1 = tree.range(30, 80);
        assertEquals(Arrays.asList(30, 40, 50, 60, 70), range1);

        List<Integer> range2 = tree.range(35, 75);
        assertEquals(Arrays.asList(40, 50, 60, 70), range2);

        List<Integer> range3 = tree.range(200, 300);
        assertTrue(range3.isEmpty());
    }


    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    void testExtremesAndBounds(int degree) {
        NARY tree = createTree(degree);
        tree.insertAll(Arrays.asList(10, 20, 30, 40, 50));

        assertEquals(10, tree.min());
        assertEquals(50, tree.max());

        assertEquals(30, tree.floor(35));
        assertEquals(40, tree.ceil(35));

        assertEquals(30, tree.floor(30));
        assertEquals(30, tree.ceil(30));

        assertNull(tree.floor(5));
        assertNull(tree.ceil(55));
    }


    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("O(N) Deep Clone Constructor")
    void testDeepCloneConstructor(int degree) {
        NARY source = createTree(degree);
        source.insertAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        NARY clone = createCopy(source);

        assertEquals(source.size(), clone.size());
        assertEquals(source.height(), clone.height());
        assertTrue(clone.contains(5));

        clone.delete(5);
        assertTrue(source.contains(5), "Source should not be affected by clone modifications");
        assertFalse(clone.contains(5));
    }
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("pollMin() should continuously extract elements in ascending order")
    void testPollMin(int degree) {
        NARY tree = createTree(degree);
        tree.insertAll(Arrays.asList(50, 10, 40, 20, 30));

        assertEquals(10, tree.pollMin());
        assertEquals(20, tree.pollMin());
        assertEquals(30, tree.pollMin());
        assertEquals(40, tree.pollMin());
        assertEquals(50, tree.pollMin());

        assertTrue(tree.isEmpty());
        assertThrows(EmptyTreeException.class, tree::pollMin);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("pollMax() should continuously extract elements in descending order")
    void testPollMax(int degree) {
        NARY tree = createTree(degree);
        tree.insertAll(Arrays.asList(15, 5, 25, 10, 20));

        assertEquals(25, tree.pollMax());
        assertEquals(20, tree.pollMax());
        assertEquals(15, tree.pollMax());
        assertEquals(10, tree.pollMax());
        assertEquals(5, tree.pollMax());

        assertTrue(tree.isEmpty());
        assertThrows(EmptyTreeException.class, tree::pollMax);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("retainAll() should drop unlisted elements and trigger merges")
    void testRetainAll(int degree) {
        NARY tree = createTree(degree);
        List<Integer> initialData = IntStream.rangeClosed(1, 20).boxed().collect(Collectors.toList());
        tree.insertAll(initialData);

        List<Integer> evens = IntStream.rangeClosed(1, 20).filter(i -> i % 2 == 0).boxed().collect(Collectors.toList());
        tree.retainAll(evens);

        assertEquals(10, tree.size());
        assertTrue(tree.containsAll(evens));
        assertFalse(tree.contains(1));
        assertFalse(tree.contains(19));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("mergeAll() should add missing elements and ignore duplicates")
    void testMergeAll(int degree) {
        NARY tree = createTree(degree);
        tree.insertAll(Arrays.asList(10, 20, 30));

        tree.mergeAll(Arrays.asList(20, 30, 40, 50));

        assertEquals(5, tree.size());
        assertTrue(tree.containsAll(Arrays.asList(10, 20, 30, 40, 50)));
    }


    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("stream() should yield all elements in strict ascending order")
    void testFullStream(int degree) {
        NARY tree = createTree(degree);
        List<Integer> data = Arrays.asList(42, 7, 99, 1, 15);
        tree.insertAll(data);

        List<Integer> streamed = tree.stream().collect(Collectors.toList());

        List<Integer> expected = new ArrayList<>(data);
        expected.sort(Integer::compareTo);

        assertEquals(expected, streamed);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("rangeStream() should yield bounded elements via aggressive branch pruning")
    void testRangeStream(int degree) {
        NARY tree = createTree(degree);
        IntStream.rangeClosed(1, 50).forEach(tree::insert);

        List<Integer> boundedStream = tree.rangeStream(20, 25).collect(Collectors.toList());

        assertEquals(Arrays.asList(20, 21, 22, 23, 24), boundedStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("forEach() should iterate without ConcurrentModificationException if read-only")
    void testForEachIteration(int degree) {
        NARY tree = createTree(degree);
        IntStream.rangeClosed(1, 10).forEach(tree::insert);

        List<Integer> captured = new ArrayList<>();
        tree.forEach(captured::add);

        assertEquals(10, captured.size());
        assertEquals(1, captured.get(0));
        assertEquals(10, captured.get(9));
    }


    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("kthSmallest() should correctly locate the value by index")
    void testKthSmallest(int degree) {
        NARY tree = createTree(degree);
        tree.insertAll(Arrays.asList(50, 40, 30, 20, 10)); // Becomes 10, 20, 30, 40, 50

        assertEquals(10, tree.kthSmallest(1));
        assertEquals(30, tree.kthSmallest(3));
        assertEquals(50, tree.kthSmallest(5));

        assertThrows(IllegalArgumentException.class, () -> tree.kthSmallest(0));
        assertThrows(IllegalArgumentException.class, () -> tree.kthSmallest(6));
    }
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("Empty tree size and simple states")
    void testEmptyTreeStates(int degree) {
        NARY tree = createTree(degree);
        assertEquals(0, tree.size());
        assertTrue(tree.isEmpty());
        assertFalse(tree.contains(10));
        assertDoesNotThrow(() -> tree.delete(99)); // Delete absent is No-Op
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("Extreme operations on empty tree must throw EmptyTreeException")
    void testEmptyTreeExceptions(int degree) {
        NARY tree = createTree(degree);
        assertThrows(EmptyTreeException.class, tree::min);
        assertThrows(EmptyTreeException.class, tree::max);
        assertThrows(EmptyTreeException.class, tree::pollMin);
        assertThrows(EmptyTreeException.class, tree::pollMax);
        assertThrows(EmptyTreeException.class, () -> tree.floor(5));
        assertThrows(EmptyTreeException.class, () -> tree.ceil(5));
        assertThrows(EmptyTreeException.class, () -> tree.successor(5));
        assertThrows(EmptyTreeException.class, () -> tree.predecessor(5));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("Null iterables and null elements must throw NullPointerException")
    void testNullGuards(int degree) {
        NARY tree = createTree(degree);
        assertThrows(NullPointerException.class, () -> tree.insertAll(null));

        List<Integer> listWithNull = Arrays.asList(10, null, 20);
        assertThrows(NullPointerException.class, () -> tree.insertAll(listWithNull));

        assertThrows(NullPointerException.class, () -> tree.range(null, 50));
        assertThrows(NullPointerException.class, () -> tree.range(10, null));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("Inserting duplicates must throw DuplicateNodeException")
    void testDuplicateInsertion(int degree) {
        NARY tree = createTree(degree);
        tree.insert(10);
        assertThrows(DuplicateNodeException.class, () -> tree.insert(10));
    }


    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("Iterator throws ConcurrentModificationException on structural change")
    void testIteratorFailFast(int degree) {
        NARY tree = createTree(degree);
        tree.insertAll(Arrays.asList(10, 20, 30));

        Iterator<Integer> it1 = tree.iterator();
        tree.insert(40);
        assertThrows(ConcurrentModificationException.class, it1::next);

        Iterator<Integer> it2 = tree.iterator();
        tree.delete(20);
        assertThrows(ConcurrentModificationException.class, it2::next);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("Exhausted iterator throws NoSuchElementException")
    void testIteratorExhaustion(int degree) {
        NARY tree = createTree(degree);
        tree.insert(10);
        Iterator<Integer> it = tree.iterator();
        assertEquals(10, it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("Predecessor and Successor tracking")
    void testPredecessorSuccessor(int degree) {
        NARY tree = createTree(degree);
        tree.insertAll(Arrays.asList(40, 20, 50, 10, 30));

        assertEquals(20, tree.successor(10));
        assertEquals(30, tree.successor(20));
        assertEquals(30, tree.successor(25)); // Successor of absent value
        assertNull(tree.successor(50));

        assertEquals(10, tree.predecessor(20));
        assertEquals(30, tree.predecessor(40));
        assertEquals(20, tree.predecessor(25)); // Predecessor of absent value
        assertNull(tree.predecessor(10));
    }


    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    @DisplayName("100,000 Randomized Operations vs java.util.TreeSet Truth")
    void randomizedInsertDeleteMatchesTruth(int degree) {
        NARY tree = createTree(degree);
        Random r = new Random(42);
        TreeSet<Integer> truth = new TreeSet<>();

        for (int i = 0; i < 100_000; i++) {
            int value = r.nextInt(2000);

            if (r.nextBoolean()) {
                try {
                    tree.insert(value);
                    truth.add(value);
                } catch (DuplicateNodeException ignored) {
                }
            } else {
                tree.delete(value);
                truth.remove(value);
            }

            assertEquals(truth.size(), tree.size(), "Size mismatch after operation " + i);
        }

        List<Integer> treeState = new ArrayList<>();
        tree.iterator().forEachRemaining(treeState::add);
        assertEquals(new ArrayList<>(truth), treeState, "Final sorted state does not match TreeSet");
    }
}
package chaos.tree.binary;

import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.exception.EmptyTreeException;
import chaos.tree.traversal.TraversalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BinaryTreeContractTest<TREE extends BinaryTree<Integer>> {

    protected TREE tree;

    protected abstract TREE createTree();

    protected abstract TREE createFromIterable(Iterable<Integer> it);

    protected abstract TREE createCopy(TREE source);

    @BeforeEach
    void setUp() {
        tree = createTree();
    }


    @Test
    void emptyTreeHasSizeZero() {
        assertEquals(0, tree.size());
    }

    @Test
    void emptyTreeIsEmpty() {
        assertTrue(tree.isEmpty());
    }

    @Test
    void emptyTreeContainsNothing() {
        assertFalse(tree.contains(10));
    }

    @Test
    void minOnEmptyTreeThrows() {
        assertThrows(EmptyTreeException.class, () -> tree.min());
    }

    @Test
    void maxOnEmptyTreeThrows() {
        assertThrows(EmptyTreeException.class, () -> tree.max());
    }

    @Test
    void deleteOnEmptyTreeIsNoOp() {
        assertDoesNotThrow(() -> tree.delete(99));
    }


    @Test
    void insertSingleNode() {
        tree.insert(1);
        assertEquals(1, tree.size());
        assertTrue(tree.contains(1));
    }

    @Test
    void insertDuplicateThrows() {
        tree.insert(10);
        assertThrows(DuplicateNodeException.class, () -> tree.insert(10));
    }

    @Test
    void insertMultipleNodes() {
        tree.insertAll(List.of(10, 20, 30));
        assertEquals(3, tree.size());
        assertTrue(tree.containsAll(List.of(10, 20, 30)));
    }

    @Test
    void insertAllNullIterableThrows() {
        assertThrows(NullPointerException.class, () -> tree.insertAll(null));
    }

    @Test
    void insertAllWithNullElementThrows() {
        List<Integer> listWithNull = Arrays.asList(10, null, 20);
        assertThrows(NullPointerException.class, () -> tree.insertAll(listWithNull));
    }


    @Test
    void deleteNonExistingIsNoOp() {
        tree.insertAll(List.of(10, 20, 5));
        tree.delete(25);
        assertEquals(3, tree.size());
    }

    @Test
    void deleteLeafNode() {
        tree.insertAll(List.of(10, 5));
        tree.delete(5);
        assertFalse(tree.contains(5));
        assertEquals(1, tree.size());
    }

    @Test
    void deleteNodeWithOneChild() {
        tree.insertAll(List.of(10, 5, 2));
        tree.delete(5);
        assertFalse(tree.contains(5));
        assertTrue(tree.contains(2));
        assertEquals(2, tree.size());
    }

    @Test
    void deleteOnlyRootNode() {
        tree.insert(10);
        tree.delete(10);
        assertEquals(0, tree.size());
        assertTrue(tree.isEmpty());
    }

    @Test
    void deleteNodeWithTwoChildren() {
        tree.insertAll(List.of(20, 10, 30, 25, 35));
        tree.delete(30);
        assertFalse(tree.contains(30));
        assertTrue(tree.contains(25));
        assertTrue(tree.contains(35));
        assertEquals(4, tree.size());
    }


    @Test
    void minReturnsSmallestValue() {
        tree.insertAll(List.of(50, 20, 80, 10, 30));
        assertEquals(10, tree.min());
    }

    @Test
    void maxReturnsLargestValue() {
        tree.insertAll(List.of(50, 20, 80, 10, 30));
        assertEquals(80, tree.max());
    }

    @Test
    void pollMinRetrievesAndRemovesSmallest() {
        tree.insertAll(List.of(50, 20, 80, 10, 30));
        assertEquals(10, tree.pollMin());
        assertEquals(4, tree.size());
        assertFalse(tree.contains(10));
        assertEquals(20, tree.min());
    }

    @Test
    void pollMaxRetrievesAndRemovesLargest() {
        tree.insertAll(List.of(50, 20, 80, 10, 30));
        assertEquals(80, tree.pollMax());
        assertEquals(4, tree.size());
        assertFalse(tree.contains(80));
        assertEquals(50, tree.max());
    }

    @Test
    void pollMinOnEmptyTreeThrows() {
        assertThrows(EmptyTreeException.class, () -> tree.pollMin());
    }

    @Test
    void pollMaxOnEmptyTreeThrows() {
        assertThrows(EmptyTreeException.class, () -> tree.pollMax());
    }

    @Test
    void rangeReturnsCorrectHalfOpenInterval() {
        tree.insertAll(List.of(50, 20, 80, 10, 30, 70, 90, 25, 35));
        assertEquals(List.of(25, 30, 35, 50), tree.range(25, 70));
    }

    @Test
    void rangeWithBoundsOutsideTreeElements() {
        tree.insertAll(List.of(20, 40, 60));
        assertEquals(List.of(20, 40, 60), tree.range(10, 70));
        assertEquals(List.of(), tree.range(70, 80));
    }

    @Test
    void rangeInvalidBoundsThrows() {
        assertThrows(IllegalArgumentException.class, () -> tree.range(50, 20));
        assertThrows(NullPointerException.class, () -> tree.range(null, 50));
    }

    @Test
    void rangeStreamReturnsCorrectHalfOpenInterval() {
        tree.insertAll(List.of(50, 20, 80, 10, 30, 70, 90, 25, 35));
        assertEquals(List.of(25, 30, 35, 50), tree.rangeStream(25, 70).toList());
    }

    @Test
    void rangeStreamWithBoundsOutsideTreeElements() {
        tree.insertAll(List.of(20, 40, 60));
        assertEquals(List.of(20, 40, 60), tree.rangeStream(10, 70).toList());
        assertEquals(List.of(), tree.rangeStream(70, 80).toList());
    }

    @Test
    void rangeStreamInvalidBoundsThrows() {
        assertThrows(IllegalArgumentException.class, () -> tree.rangeStream(50, 20));
        assertThrows(NullPointerException.class, () -> tree.rangeStream(null, 50));
    }

    @Test
    void successorReturnsNextLargerValue() {
        tree.insertAll(List.of(40, 20, 50, 10, 30));
        assertEquals(20, tree.successor(10));
        assertEquals(30, tree.successor(20));
        assertNull(tree.successor(50));
    }

    @Test
    void predecessorReturnsPreviousValue() {
        tree.insertAll(List.of(40, 20, 50, 10, 30));
        assertEquals(10, tree.predecessor(20));
        assertEquals(30, tree.predecessor(40));
        assertNull(tree.predecessor(10));
    }

    @Test
    void floorExactAndClosest() {
        tree.insertAll(List.of(40, 20, 60, 10, 30));
        assertEquals(20, tree.floor(20));
        assertEquals(30, tree.floor(35));
        assertNull(tree.floor(5));
    }

    @Test
    void ceilExactAndClosest() {
        tree.insertAll(List.of(40, 20, 60, 10, 30));
        assertEquals(20, tree.ceil(20));
        assertEquals(40, tree.ceil(35));
        assertNull(tree.ceil(95));
    }

    @Test
    void kthSmallestValidatesBounds() {
        tree.insertAll(List.of(40, 20, 60, 10, 30, 50, 70));
        assertEquals(10, tree.kthSmallest(1));
        assertEquals(70, tree.kthSmallest(7));
        assertThrows(IllegalArgumentException.class, () -> tree.kthSmallest(0));
        assertThrows(IllegalArgumentException.class, () -> tree.kthSmallest(8));
    }


    @Test
    void deleteAllRemovesSpecifiedValues() {
        tree.insertAll(List.of(10, 20, 30, 40));
        tree.deleteAll(List.of(20, 40));
        assertEquals(2, tree.size());
        assertEquals(List.of(10, 30), tree.inorder());
    }

    @Test
    void retainAllKeepsIntersectionOnly() {
        tree.insertAll(List.of(10, 20, 30, 40, 50));
        tree.retainAll(List.of(20, 40));
        assertEquals(List.of(20, 40), tree.inorder());
    }

    @Test
    void mergeAllInsertsNewValues() {
        tree.insertAll(List.of(10, 20));
        tree.mergeAll(List.of(20, 30, 40));
        assertEquals(List.of(10, 20, 30, 40), tree.inorder());
    }

    @Test
    void deleteAllSelf() {
        tree.insertAll(List.of(10, 20, 30));
        tree.deleteAll(tree);
        assertTrue(tree.isEmpty());
    }

    @Test
    void retainAllSelf() {
        tree.insertAll(List.of(10, 20, 30));
        tree.retainAll(tree);
        assertEquals(3, tree.size());
    }

    @Test
    void mergeAllSelf() {
        tree.insertAll(List.of(10, 20, 30));
        tree.mergeAll(tree);
        assertEquals(3, tree.size());
    }


    @Test
    void inorderIsSorted() {
        tree.insertAll(List.of(50, 10, 80, 20, 30));
        List<Integer> values = tree.inorder();
        for (int i = 1; i < values.size(); i++) {
            assertTrue(values.get(i - 1) < values.get(i));
        }
    }

    @Test
    void iteratorFailFastMechanics() {
        tree.insertAll(List.of(10, 20, 30));
        Iterator<Integer> it = tree.iterator();
        tree.insert(40);
        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void exhaustedIteratorThrows() {
        tree.insert(10);
        Iterator<Integer> it = tree.iterator();
        it.next();
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void emptyTraversal() {
        assertTrue(tree.inorder().isEmpty());
    }

    @Test
    void streamSortedAndCompleteness() {
        tree.insertAll(List.of(50, 10, 80, 20, 30));
        List<Integer> fromStream = tree.stream().toList();
        assertEquals(List.of(10, 20, 30, 50, 80), fromStream);
    }

    @Test
    void failFastOnDelete() {
        tree.insertAll(List.of(10, 20, 30));
        Iterator<Integer> it = tree.iterator();
        tree.delete(20);
        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void independentIterators() {
        tree.insertAll(List.of(10, 20));
        Iterator<Integer> it1 = tree.iterator();
        Iterator<Integer> it2 = tree.iterator();
        assertEquals(10, it1.next());
        assertEquals(10, it2.next());
        assertEquals(20, it1.next());
    }

    @Test
    void visitEachElementOnce() {
        tree.insertAll(List.of(10, 20, 30));
        int count = 0;
        for (Integer val : tree) {
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    void iterableConstructorBuildsTree() {
        TREE built = createFromIterable(List.of(30, 10, 50, 20, 40));
        assertEquals(5, built.size());
        assertEquals(List.of(10, 20, 30, 40, 50), built.inorder());
    }

    @Test
    void iterableConstructorEmptyCreatesEmptyTree() {
        TREE built = createFromIterable(List.of());
        assertTrue(built.isEmpty());
    }

    @Test
    void copyConstructorProducesEqualIndependentTree() {
        tree.insertAll(List.of(30, 10, 50, 20, 40));

        TREE copy = createCopy(tree);

        assertEquals(tree.size(), copy.size());
        assertEquals(tree.inorder(), copy.inorder());

        copy.insert(60);
        tree.delete(10);
        assertFalse(tree.contains(60));
        assertTrue(copy.contains(10));
    }

    @Test
    void copyConstructorOfEmptyTree() {
        TREE copy = createCopy(tree);
        assertTrue(copy.isEmpty());
    }

    @Test
    void iterableNullThrows() {
        assertThrows(NullPointerException.class, () -> createFromIterable(null));
    }

    @Test
    void copyNullThrows() {
        assertThrows(NullPointerException.class, () -> createCopy(null));
    }

    @Test
    void randomizedInsertDeleteMatchesTruth() {
        Random r = new Random(42);
        TreeSet<Integer> truth = new TreeSet<>();

        for (int i = 0; i < 100_000; i++) {
            int value = r.nextInt(1000);
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
            assertEquals(truth.size(), tree.size());
        }

        assertEquals(new ArrayList<>(truth), tree.inorder());
    }

    private boolean isDeepRecursionSafe() {
        String name = tree.getClass().getSimpleName();
        return !name.contains("BST") && !name.contains("Splay");
    }

    @Test
    void sequentialInsertAndSequentialDeleteScale() {
        if (!isDeepRecursionSafe()) return;
        int scale = 10_000;
        for (int i = 0; i < scale; i++) {
            tree.insert(i);
        }
        assertEquals(scale, tree.size());
        for (int i = 0; i < scale; i++) {
            tree.delete(i);
        }
        assertTrue(tree.isEmpty());
    }

    @Test
    void shuffledInsertDeleteScale() {
        int scale = 10_000;
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < scale; i++) list.add(i);
        Collections.shuffle(list, new Random(42));

        for (int i : list) tree.insert(i);
        assertEquals(scale, tree.size());

        Collections.shuffle(list, new Random(43));
        for (int i : list) tree.delete(i);
        assertTrue(tree.isEmpty());
    }
    // ── Traversal completeness ──────────────────────────────────────────────────

    @Test
    void allTraversalTypesReturnAllElements() {
        tree.insertAll(List.of(50, 20, 80, 10, 30));
        assertEquals(5, tree.toList(TraversalType.LEVEL_ORDER).size());
        assertEquals(5, tree.toList(TraversalType.INORDER).size());
        assertEquals(5, tree.toList(TraversalType.POSTORDER).size());
        assertEquals(5, tree.toList(TraversalType.PREORDER).size());
    }


    @Test
    void floorOnEmptyTreeThrows() {
        assertThrows(EmptyTreeException.class, () -> tree.floor(5));
    }

    @Test
    void ceilOnEmptyTreeThrows() {
        assertThrows(EmptyTreeException.class, () -> tree.ceil(5));
    }


    @Test
    void successorOfAbsentValueReturnsNextLarger() {
        tree.insertAll(List.of(10, 20, 40));
        assertEquals(40, tree.successor(25));
        assertNull(tree.successor(45));
    }

    @Test
    void predecessorOfAbsentValueReturnsPreviousSmaller() {
        tree.insertAll(List.of(10, 20, 40));
        assertEquals(20, tree.predecessor(25));
        assertNull(tree.predecessor(5));
    }


    @Test
    void kthSmallestOnSingleElementTree() {
        tree.insert(42);
        assertEquals(42, tree.kthSmallest(1));
    }

    @Test
    void clearThenReuseTreeIsFullyFunctional() {
        tree.insertAll(List.of(10, 20, 30));
        tree.clear();
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        tree.insertAll(List.of(10, 20, 30));
        assertFalse(tree.isEmpty());
        assertEquals(3, tree.size());
    }

    @Test
    void containsAllWithEmptyCollectionReturnsTrue() {
        assertDoesNotThrow(() -> tree.containsAll(List.of()));
        assertTrue(tree.containsAll(List.of()));
    }

    @Test
    void containsAllWithAbsentValueReturnsFalse() {
        tree.insertAll(List.of(10, 20, 30));
        assertFalse(tree.containsAll(List.of(10, 99)));
    }


    @Test
    void streamOnEmptyTreeReturnsEmptyList() {
        assertTrue(tree.stream().toList().isEmpty());
    }


    @Test
    void deleteAllWithNonExistentValuesIsNoOp() {
        tree.insertAll(List.of(10, 20, 30));
        tree.deleteAll(List.of(99, 100));
        assertEquals(3, tree.size());
    }


    @Test
    void mergeAllWithEmptyCollectionIsNoOp() {
        tree.insertAll(List.of(10, 20, 30));
        tree.mergeAll(List.of());
        assertEquals(3, tree.size());
    }
}
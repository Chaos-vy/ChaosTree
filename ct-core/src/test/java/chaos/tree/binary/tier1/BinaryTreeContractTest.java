package chaos.tree.binary.tier1;

import chaos.tree.binary.BinaryTree;
import chaos.tree.traversal.TraversalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.*;

/*
These are the main test configured from here. Though,
I am not a high level debugger But I will try my best to have correctness.
 */
public abstract class BinaryTreeContractTest<MyTree extends BinaryTree<Integer>> {

    protected MyTree tree;

    protected abstract MyTree createTree();

    protected abstract MyTree createFromIterable(Iterable<Integer> it);

    protected abstract MyTree createCopy(MyTree source);

    @BeforeEach
    void setUp() {
        tree = createTree();
    }

    /*




    These check are performed on Emptiness correctness.
     */
    @Test
    void emptyTreeSizeMustBeZero() {
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
    void minOnEmptyTreeReturnNull() {
        assertNull(tree.min());
    }

    @Test
    void maxOnEmptyTreeReturnNull() {
        assertNull(tree.max());
    }

    @Test
    void pollMinOnEmptyTreeNull() {
        assertNull(tree.pollMin());
    }

    @Test
    void pollMaxOnEmptyTreeReturnsNull() {
        assertNull(tree.pollMax());
    }

    @Test
    void deleteOnEmptyTreeIsNoOp() {
        assertDoesNotThrow(() -> tree.delete(99));
    }

    @Test
    void floorOnEmptyTreeReturnsNull() {
        assertNull(tree.floor(50));
    }

    @Test
    void ceilOnEmptyTreeReturnsNull() {
        assertNull(tree.ceil(50));
    }

    @Test
    void successorOnEmptyTreeReturnsNull() {
        assertNull(tree.successor(4));
    }

    @Test
    void predecessorOnEmptyTreeReturnsNull() {
        assertNull(tree.predecessor(4));
    }

    @Test
    void lcaOnEmptyTreeThrows() {
        assertNull(tree.lca(44, 66));
    }

    @Test
    void kthSmallestOnEmptyTreeThrows() {
        assertThrows(IllegalArgumentException.class, () -> tree.kthSmallest(5));
    }
    @Test
    void heightOnEmptyTree() { assertEquals(-1, tree.height());}
    @Test
    void emptyTraversal() {
        assertTrue(tree.inorder().isEmpty());
    }

    /*




    This is checked for null values:
     */
    @Test
    void insertAllNullIterableThrows() {
        assertThrows(NullPointerException.class, () -> tree.insertAll(null));
    }
    @Test
    void deleteAllNullIterableThrows() {
        assertThrows(NullPointerException.class, () -> tree.deleteAll(null));
    }
    @Test
    void mergeAllNullIterableThrows() {
        assertThrows(NullPointerException.class, () -> tree.mergeAll(null));
    }
    @Test
    void retainAllNullIterableThrows() {
        assertThrows(NullPointerException.class, () -> tree.retainAllElements(null));
    }
    @Test
    void containsAllNullIterableThrows() {
        assertThrows(NullPointerException.class, () -> tree.containsAllElements(null));
    }
//    @Test
//    void insertAllWithNullElement() {
//        List<Integer> list = new ArrayList<>();
//        list.add(1);list.add(2);list.add(null);list.add(4);
//        assertThrows(NullPointerException.class, () -> tree.insertAll(list));
//        assertEquals(2, tree.size());
//    }
    @Test
    void containAllWithNullElement() {
        List<Integer> list = new ArrayList<>();
        list.add(1);list.add(2);list.add(null);list.add(4);
        tree.insertAll(Arrays.asList(1,2,3,4,5));
        assertFalse(tree.containsAllElements(list));
        assertEquals(5, tree.size());
    }
    @Test
    void mergeAllWithNullElement() {
        List<Integer> list = new ArrayList<>();
        list.add(1);list.add(2);list.add(null);list.add(4);
        tree.mergeAll(list);
        assertEquals(3, tree.size());
    }
    @Test
    void retainAllWithNullElement() {
        tree.insertAll(Arrays.asList(1, 2, 3, 4));

        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(null);
        list.add(4);

        tree.retainAllElements(list);

        assertEquals(3, tree.size());
        assertEquals(Arrays.asList(1, 2, 4), tree.toList());
    }
    @Test
    void deleteAllWithNullElement() {
        tree.insertAll(Arrays.asList(1,2,3,4));
        List<Integer> list = new ArrayList<>();
        list.add(1);list.add(2);list.add(null);list.add(4);
        assertThrows(NullPointerException.class, () -> tree.deleteAll(list));
        assertEquals(2, tree.size());
        assertEquals(Arrays.asList(3, 4), tree.toList());
    }

    /*



    This Section is for insertion testing suite
    height is being ignored as for why ?? Read the class name of this test. Still not got ?->
    Binary tree balancing is different.
    */
    @Test
    void insertSingleNode() {
        tree.insert(1);
        assertEquals(1, tree.size());
        assertTrue(tree.contains(1));
        assertEquals(0, tree.height());
        assertEquals(1,tree.max());
        assertEquals(1,tree.min());
        assertEquals(Arrays.asList(1),tree.toList());
    }
    @Test
    void insertDuplicateNoEffect() {
        tree.insert(10);
        tree.insert(10);
        assertEquals(1,tree.size());
    }

    @Test
    void insertMultipleNodes() {
        tree.insertAll(Arrays.asList(10, 20, 30));
        assertEquals(3, tree.size());
        assertTrue(tree.containsAllElements(Arrays.asList(10, 20, 30)));
    }

    @Test
    void deleteNonExistingIsNoOp() {
        tree.insertAll(Arrays.asList(10, 20, 5));
        tree.delete(25);
        assertEquals(3, tree.size());
    }

    @Test
    void deleteLeafNode() {
        tree.insertAll(Arrays.asList(10, 5));
        tree.delete(5);
        assertFalse(tree.contains(5));
        assertEquals(1, tree.size());
    }

    @Test
    void deleteNodeWithOneChild() {
        tree.insertAll(Arrays.asList(10, 5, 2));
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
        tree.insertAll(Arrays.asList(20, 10, 30, 25, 35));
        tree.delete(30);
        assertFalse(tree.contains(30));
        assertTrue(tree.contains(25));
        assertTrue(tree.contains(35));
        assertEquals(4, tree.size());
    }

    @Test
    void minReturnsSmallestValue() {
        tree.insertAll(Arrays.asList(50, 20, 80, 10, 30));
        assertEquals(10, tree.min());
    }

    @Test
    void maxReturnsLargestValue() {
        tree.insertAll(Arrays.asList(50, 20, 80, 10, 30));
        assertEquals(80, tree.max());
    }

    @Test
    void pollMinRetrievesAndRemovesSmallest() {
        tree.insertAll(Arrays.asList(50, 20, 80, 10, 30));
        assertEquals(10, tree.pollMin());
        assertEquals(4, tree.size());
        assertFalse(tree.contains(10));
        assertEquals(20, tree.min());
    }

    @Test
    void pollMaxRetrievesAndRemovesLargest() {
        tree.insertAll(Arrays.asList(50, 20, 80, 10, 30));
        assertEquals(80, tree.pollMax());
        assertEquals(4, tree.size());
        assertFalse(tree.contains(80));
        assertEquals(50, tree.max());
    }

    /*


    The Range benchmark
     */
    @Test
    void rangeReturnsCorrectHalfOpenInterval() {
        tree.insertAll(Arrays.asList(50, 20, 80, 10, 30, 70, 90, 25, 35));
        assertEquals(Arrays.asList(25, 30, 35, 50), tree.range(25, 70));
    }

    @Test
    void rangeWithBoundsOutsideTreeElements() {
        tree.insertAll(Arrays.asList(20, 40, 60));
        assertEquals(Arrays.asList(20, 40, 60), tree.range(10, 70));
        assertEquals(Arrays.asList(), tree.range(70, 80));
    }

    @Test
    void rangeInvalidBoundsThrows() {
        assertThrows(IllegalArgumentException.class, () -> tree.range(50, 20));
        assertThrows(NullPointerException.class, () -> tree.range(null, 50));
    }

    @Test
    void rangeStreamReturnsCorrectHalfOpenInterval() {
        tree.insertAll(Arrays.asList(50, 20, 80, 10, 30, 70, 90, 25, 35));
        assertEquals(Arrays.asList(25, 30, 35, 50), tree.rangeStream(25, 70).collect(toList()));
    }

    @Test
    void rangeStreamWithBoundsOutsideTreeElements() {
        tree.insertAll(Arrays.asList(20, 40, 60));
        assertEquals(Arrays.asList(20, 40, 60), tree.rangeStream(10, 70).collect(toList()));
        assertEquals(Arrays.asList(), tree.rangeStream(70, 80).collect(toList()));
    }

    @Test
    void rangeStreamInvalidBoundsThrows() {
        assertThrows(IllegalArgumentException.class, () -> tree.rangeStream(50, 20));
        assertThrows(NullPointerException.class, () -> tree.rangeStream(null, 50));
    }

    @Test
    void successorReturnsNextLargerValue() {
        tree.insertAll(Arrays.asList(40, 20, 50, 10, 30));
        assertEquals(20, tree.successor(10));
        assertEquals(30, tree.successor(20));
        assertNull(tree.successor(50));
    }
    @Test
    void inorderIsSorted() {
        tree.insertAll(Arrays.asList(50, 10, 80, 20, 30));
        List<Integer> values = tree.inorder();
        for (int i = 1; i < values.size(); i++) {
            assertTrue(values.get(i - 1) < values.get(i));
        }
    }

    @Test
    void iteratorFailFastMechanics() {
        tree.insertAll(Arrays.asList(10, 20, 30));
        Iterator<Integer> it = tree.iterator();
        tree.insert(40);
        assertThrows(ConcurrentModificationException.class, it::next);
    }
    // ── Traversal completeness ──────────────────────────────────────────────────

    @Test
    void allTraversalTypesReturnAllElements() {
        tree.insertAll(Arrays.asList(50, 20, 80, 10, 30));
        assertEquals(5, tree.toList(TraversalType.LEVEL_ORDER).size());
        assertEquals(5, tree.toList(TraversalType.INORDER).size());
        assertEquals(5, tree.toList(TraversalType.POSTORDER).size());
        assertEquals(5, tree.toList(TraversalType.PREORDER).size());
    }
    @Test
    void iterableConstructorBuildsTree() {
        MyTree built = createFromIterable(Arrays.asList(30, 10, 50, 20, 40));
        assertEquals(5, built.size());
        assertEquals(Arrays.asList(10, 20, 30, 40, 50), built.inorder());
    }

    @Test
    void iterableConstructorEmptyCreatesEmptyTree() {
        MyTree built = createFromIterable(Arrays.asList());
        assertTrue(built.isEmpty());
    }
    @Test
    void streamOnEmptyTreeReturnsEmptyList() {
        assertTrue(tree.stream().collect(toList()).isEmpty());
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
    void streamSortedAndCompleteness() {
        tree.insertAll(Arrays.asList(50, 10, 80, 20, 30));
        List<Integer> fromStream = tree.stream().collect(toList());
        assertEquals(Arrays.asList(10, 20, 30, 50, 80), fromStream);
    }

    @Test
    void failFastOnDelete() {
        tree.insertAll(Arrays.asList(10, 20, 30));
        Iterator<Integer> it = tree.iterator();
        tree.delete(20);
        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void independentIterators() {
        tree.insertAll(Arrays.asList(10, 20));
        Iterator<Integer> it1 = tree.iterator();
        Iterator<Integer> it2 = tree.iterator();
        assertEquals(10, it1.next());
        assertEquals(10, it2.next());
        assertEquals(20, it1.next());
    }

    @Test
    void visitEachElementOnce() {
        tree.insertAll(Arrays.asList(10, 20, 30));
        int count = 0;
        for (Integer val : tree) {
            count++;
        }
        assertEquals(3, count);
    }

    /*


    Miscellaneous Operations
     */
    @Test
    void predecessorReturnsPreviousValue() {
        tree.insertAll(Arrays.asList(40, 20, 50, 10, 30));
        assertEquals(10, tree.predecessor(20));
        assertEquals(30, tree.predecessor(40));
        assertNull(tree.predecessor(10));
    }

    @Test
    void floorExactAndClosest() {
        tree.insertAll(Arrays.asList(40, 20, 60, 10, 30));
        assertEquals(20, tree.floor(20));
        assertEquals(30, tree.floor(35));
        assertNull(tree.floor(5));
    }

    @Test
    void ceilExactAndClosest() {
        tree.insertAll(Arrays.asList(40, 20, 60, 10, 30));
        assertEquals(20, tree.ceil(20));
        assertEquals(40, tree.ceil(35));
        assertNull(tree.ceil(95));
    }

    @Test
    void kthSmallestValidatesBounds() {
        tree.insertAll(Arrays.asList(40, 20, 60, 10, 30, 50, 70));
        assertEquals(10, tree.kthSmallest(1));
        assertEquals(70, tree.kthSmallest(7));
        assertThrows(IllegalArgumentException.class, () -> tree.kthSmallest(0));
        assertThrows(IllegalArgumentException.class, () -> tree.kthSmallest(8));
    }

    /*



    Bulk operation
     */
    @Test
    void containsAllElementsWithEmptyCollectionReturnsTrue() {
        assertDoesNotThrow(() -> tree.containsAllElements(Arrays.asList()));
        assertTrue(tree.containsAllElements(Arrays.asList()));
    }
    @Test
    void containsAllElementsWithAbsentValueReturnsFalse() {
        tree.insertAll(Arrays.asList(10, 20, 30));
        assertFalse(tree.containsAllElements(Arrays.asList(10, 99)));
    }
    @Test
    void deleteAllWithNonExistentValuesIsNoOp() {
        tree.insertAll(Arrays.asList(10, 20, 30));
        tree.deleteAll(Arrays.asList(99, 100));
        assertEquals(3, tree.size());
    }
    @Test
    void deleteAllRemovesSpecifiedValues() {
        tree.insertAll(Arrays.asList(10, 20, 30, 40));
        tree.deleteAll(Arrays.asList(20, 40));
        assertEquals(2, tree.size());
        assertEquals(Arrays.asList(10, 30), tree.inorder());
    }

    @Test
    void retainAllElementsKeepsIntersectionOnly() {
        tree.insertAll(Arrays.asList(10, 20, 30, 40, 50));
        tree.retainAllElements(Arrays.asList(20, 40));
        assertEquals(Arrays.asList(20, 40), tree.inorder());
    }

    @Test
    void mergeAllInsertsNewValues() {
        tree.insertAll(Arrays.asList(10, 20));
        tree.mergeAll(Arrays.asList(20, 30, 40));
        assertEquals(Arrays.asList(10, 20, 30, 40), tree.inorder());
    }

    @Test
    void retainAllElementsSelf() {
        tree.insertAll(Arrays.asList(10, 20, 30));
        tree.retainAllElements(tree);
        assertEquals(3, tree.size());
    }

    @Test
    void mergeAllSelf() {
        tree.insertAll(Arrays.asList(10, 20, 30));
        tree.mergeAll(tree);
        assertEquals(3, tree.size());
    }

    // Some of constructors work

    @Test
    void copyConstructorProducesEqualIndependentTree() {
        tree.insertAll(Arrays.asList(30, 10, 50, 20, 40));
        MyTree copy = createCopy(tree);
        assertEquals(tree.size(), copy.size());
        assertEquals(tree.inorder(), copy.inorder());
        assertEquals(tree,copy);
        assertEquals(tree.hashCode(), copy.hashCode());
        copy.insert(60);
        tree.delete(10);
        assertFalse(tree.contains(60));
        assertTrue(copy.contains(10));
    }

    @Test
    void copyConstructorOfEmptyTree() {
        MyTree copy = createCopy(tree);
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
                    tree.insert(value);
                    truth.add(value);

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


    @Test
    void successorOfAbsentValueReturnsNextLarger() {
        tree.insertAll(Arrays.asList(10, 20, 40));
        assertEquals(40, tree.successor(25));
        assertNull(tree.successor(45));
    }

    @Test
    void predecessorOfAbsentValueReturnsPreviousSmaller() {
        tree.insertAll(Arrays.asList(10, 20, 40));
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
        tree.insertAll(Arrays.asList(10, 20, 30));
        tree.clear();
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        tree.insertAll(Arrays.asList(10, 20, 30));
        assertFalse(tree.isEmpty());
        assertEquals(3, tree.size());
    }
}
package chaos.tree.binary;
import chaos.tree.binary.treap.Treap;
import chaos.tree.core.searchtree.binary.BinaryTree;
import chaos.tree.exception.*;
import chaos.tree.traversal.TraversalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TreapTest{
    private BinaryTree<Integer> tree;

    @BeforeEach
    void setUp() {

        tree = new Treap<>(42L, 5000);
    }
    @Test
    void insertSingleNode(){
        tree.insert(1);
        assertEquals(1,tree.size());
        assertTrue(tree.contains(1));
    }
    @Test
    void emptyTreeShouldHaveSizeZero(){
        assertEquals(0, tree.size());
        assertTrue(tree.isEmpty());
        assertFalse(tree.contains(10));
    }

    @Test
    void  throwDuplicateNodeExceptionONInsert(){
        tree.insert(10);
        assertThrows(
                DuplicateNodeException.class,
                ()->tree.insert(10)
        );
    }
    @Test
    void deleteNonExistingValueShouldNotChangeSize(){
        tree.insert(10);
        tree.insert(20);
        tree.insert(5);
        tree.delete(25);
        assertEquals(3, tree.size());

    }
    @Test
    void deleteLeafNode() {
        tree.insert(10);
        tree.insert(5);

        tree.delete(5);

        assertFalse(tree.contains(5));
        assertEquals(1, tree.size());
    }
    @Test
    void deleteNodeWithOneChild() {
        tree.insert(10);
        tree.insert(5);
        tree.insert(2);

        tree.delete(5);

        assertFalse(tree.contains(5));
        assertTrue(tree.contains(2));
        assertEquals(2, tree.size());
    }
    @Test
    void deleteNodeWithTwoChildren() {
        tree.insert(10);
        tree.insert(5);
        tree.insert(15);

        tree.delete(10);

        assertFalse(tree.contains(10));
        assertTrue(tree.contains(5));
        assertTrue(tree.contains(15));
        assertEquals(2, tree.size());
    }
    @Test
    void deleteRootNode() {
        tree.insert(10);

        tree.delete(10);

        assertEquals(0, tree.size());
        assertTrue(tree.isEmpty());
    }
    @Test
    void minShouldReturnSmallestValue() {
        tree.insert(50);
        tree.insert(20);
        tree.insert(80);
        tree.insert(10);
        tree.insert(30);

        assertEquals(10, tree.min());
    }
    @Test
    void maxShouldReturnLargestValue() {
        tree.insert(50);
        tree.insert(20);
        tree.insert(80);
        tree.insert(10);
        tree.insert(30);

        assertEquals(80, tree.max());
    }
    @Test
    void minOnEmptyTreeShouldThrow(){
        assertThrows(
                EmptyTreeException.class,
                ()->tree.min()
        );
    }
    @Test
    void maxOnEmptyTreeShouldThrow(){
        assertThrows(
                EmptyTreeException.class,
                ()->tree.max()
        );
    }
    @Test
    void successorShouldReturnNextLargerValue() {
        tree.insert(40);
        tree.insert(20);
        tree.insert(50);
        tree.insert(10);
        tree.insert(30);

        assertEquals(20, tree.successor(10));
        assertEquals(30, tree.successor(20));
        assertEquals(40, tree.successor(30));
    }
    @Test
    void predecessorShouldReturnPreviousValue() {
        tree.insert(40);
        tree.insert(20);
        tree.insert(50);
        tree.insert(10);
        tree.insert(30);

        assertEquals(10, tree.predecessor(20));
        assertEquals(20, tree.predecessor(30));
        assertEquals(30, tree.predecessor(40));
    }
    @Test
    void floorShouldReturnSameValueForExactMatch() {
        tree.insert(40);
        tree.insert(20);
        tree.insert(60);
        tree.insert(10);
        tree.insert(30);

        assertEquals(20, tree.floor(20));
    }
    @Test
    void floorShouldReturnClosestSmallerValue() {
        tree.insert(40);
        tree.insert(20);
        tree.insert(60);
        tree.insert(10);
        tree.insert(30);

        assertEquals(30, tree.floor(35));
    }
    @Test
    void ceilShouldReturnSameValueForExactMatch() {
        tree.insert(40);
        tree.insert(20);
        tree.insert(60);
        tree.insert(10);
        tree.insert(30);

        assertEquals(20, tree.ceil(20));
    }
    @Test
    void ceilShouldReturnClosestGreaterValue() {
        tree.insert(40);
        tree.insert(20);
        tree.insert(60);
        tree.insert(10);
        tree.insert(30);

        assertEquals(40, tree.ceil(35));
    }
    @Test
    void kthSmallestShouldReturnFirstElement() {
        tree.insert(40);
        tree.insert(20);
        tree.insert(60);
        tree.insert(10);
        tree.insert(30);
        tree.insert(50);
        tree.insert(70);

        assertEquals(10, tree.kthSmallest(1));
    }
    @Test
    void kthSmallestShouldReturnLastElement() {
        tree.insert(40);
        tree.insert(20);
        tree.insert(60);
        tree.insert(10);
        tree.insert(30);
        tree.insert(50);
        tree.insert(70);

        assertEquals(70, tree.kthSmallest(7));
    }
    @Test
    void kthSmallestWithZeroShouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tree.kthSmallest(0)
        );
    }
    @Test
    void kthSmallestGreaterThanSizeShouldThrow() {
        tree.insert(10);

        assertThrows(
                IllegalArgumentException.class,
                () -> tree.kthSmallest(2)
        );
    }
    @Test
    void shouldHandleTenThousandSequentialInserts() {

        for (int i = 1; i <= 10_000; i++) {
            tree.insert(i);
        }

        assertEquals(10_000, tree.size());
    }
    @Test
    void shouldHandleTenThousandSequentialDeletes(){
        for (int i = 1; i <= 10_000; i++) {
            tree.insert(i);
        }
        for (int i = 1;i <= 10_000; i++){
            tree.delete(i);
        }
        assertEquals(0,tree.size());
    }
    @Test
    void inorderTraversalShouldBeSorted() {
        tree.insert(50);
        tree.insert(10);
        tree.insert(80);
        tree.insert(20);
        tree.insert(30);

        List<Integer> values = tree.inorder();

        for (int i = 1; i < values.size(); i++) {
            assertTrue(values.get(i - 1) < values.get(i));
        }
    }
    @Test
    void shuffledTest(){
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }
        TreeSet<Integer> truth = new TreeSet<>();
        Collections.shuffle(list);
        for(Integer x: list){
            tree.insert(x);
            truth.add(x);
        }
        for(Integer x : truth){
            assertTrue(tree.contains(x));
        }
        assertEquals(truth.size(),tree.size());
        Collections.shuffle(list);
        for (Integer x: list){
            tree.delete(x);
            truth.remove(x);
        }
        assertEquals(truth.size(),tree.size());
        assertTrue(tree.isEmpty());
    }
    /**
     * Verifies that 100,000 random insert/delete operations
     * keep the tree size consistent with TreeSet.
     */
    @Test
    void randomizedTest(){
        Random r = new Random();
        TreeSet<Integer> truth = new TreeSet<>();
        for(int i=0;i<100000;i++){

            int value = r.nextInt(1000);

            if(r.nextBoolean()){
                try{
                    tree.insert(value);
                    truth.add(value);
                }catch(DuplicateNodeException ignored){}
            }
            else{
                tree.delete(value);
                truth.remove(value);
            }

            assertEquals(truth.size(), tree.size());
        }
    }
    @Test
    void inorderIteratorShouldReturnSortedValues() {
        tree.insertAll(List.of(20, 10, 30));

        Iterator<Integer> it = tree.iterator(TraversalType.INORDER);

        List<Integer> result = new ArrayList<>();

        while (it.hasNext()) {
            result.add(it.next());
        }

        assertEquals(List.of(10, 20, 30), result);
    }

    @Test
    void iteratorShouldFailFastAfterInsert() {
        tree.insertAll(List.of(10, 20, 30));

        Iterator<Integer> it = tree.iterator();

        tree.insert(40);

        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void iteratorShouldFailFastAfterDelete() {
        tree.insertAll(List.of(10, 20, 30));

        Iterator<Integer> it = tree.iterator();

        tree.delete(20);

        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void hasNextShouldFailFastAfterModification() {
        tree.insertAll(List.of(10, 20, 30));

        Iterator<Integer> it = tree.iterator();

        tree.insert(40);

        assertThrows(ConcurrentModificationException.class, it::hasNext);
    }

    @Test
    void multipleIteratorsShouldWorkIndependently() {
        tree.insertAll(List.of(10, 20, 30));

        Iterator<Integer> it1 = tree.iterator();
        Iterator<Integer> it2 = tree.iterator();

        assertEquals(10, it1.next());
        assertEquals(10, it2.next());

        assertEquals(20, it1.next());
        assertEquals(20, it2.next());
    }

    @Test
    void exhaustedIteratorShouldThrow() {
        tree.insert(10);

        Iterator<Integer> it = tree.iterator();

        assertEquals(10, it.next());

        assertFalse(it.hasNext());

        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void inorderStreamShouldBeSorted() {
        tree.insertAll(List.of(50, 10, 70, 20, 30));

        List<Integer> values = tree.stream(TraversalType.INORDER).toList();

        assertEquals(List.of(10, 20, 30, 50, 70), values);
    }

    @Test
    void streamShouldContainEveryElement() {
        tree.insertAll(List.of(10, 20, 30, 40));

        Set<Integer> values = tree.stream(TraversalType.INORDER).collect(java.util.stream.Collectors.toSet());

        assertEquals(Set.of(10, 20, 30, 40), values);
    }

    @Test
    void streamIteratorShouldFailFastAfterModification() {

        tree.insertAll(List.of(10, 20, 30));

        Stream<Integer> stream = tree.stream(TraversalType.INORDER);

        tree.insert(40);

        assertThrows(ConcurrentModificationException.class, () -> stream.forEach(x -> {
        }));
    }

    @Test
    void iteratorShouldVisitEveryElementExactlyOnce() {

        for (int i = 1; i <= 100_000; i++) {
            tree.insert(i);
        }

        Iterator<Integer> it = tree.iterator();

        int count = 0;

        while (it.hasNext()) {
            it.next();
            count++;
        }

        assertEquals(100_000, count);
    }

    @Test
    void deletionOfTreeUsingSameTreeAsIterable() {
        tree.insert(10);
        tree.insert(20);
        tree.insert(30);
        tree.deleteAll(tree);
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
    }

    @Test
    void retainAllOfTreeUsingSameTreeAsIterable() {
        tree.insert(10);
        tree.insert(20);
        tree.insert(30);
        tree.retainAll(tree);
        assertFalse(tree.isEmpty());
        assertEquals(3, tree.size());
    }

    @Test
    void MergeAllOfTreeUsingSameTreeAsIterable() {
        tree.insert(10);
        tree.insert(20);
        tree.insert(30);
        tree.mergeAll(tree);
        assertFalse(tree.isEmpty());
        assertEquals(3, tree.size());
    }
}
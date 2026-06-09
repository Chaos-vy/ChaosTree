package chaos.tree.binary;

import chaos.tree.binary.avl.AVL;
import chaos.tree.core.searchtree.binary.BinaryTree;
import chaos.tree.exception.*;
import chaos.tree.traversal.TraversalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AVLTest {
    private BinaryTree<Integer> tree;

    @BeforeEach
    void setUp() {
        tree = new AVL<>();
    }

    @Test
    void insertSingleNode() {
        tree.insert(1);
        assertEquals(1, tree.size());
        assertTrue(tree.contains(1));
    }

    @Test
    void insertAllShouldInsertEveryValue() {
        tree.insertAll(List.of(10, 20, 30));

        assertEquals(3, tree.size());
        assertTrue(tree.contains(10));
        assertTrue(tree.contains(20));
        assertTrue(tree.contains(30));
    }

    @Test
    void emptyTreeShouldHaveSizeZero() {
        assertEquals(0, tree.size());
        assertTrue(tree.isEmpty());
        assertFalse(tree.contains(10));
    }

    @Test
    void throwDuplicateNodeExceptionONInsert() {
        tree.insert(10);
        assertThrows(DuplicateNodeException.class, () -> tree.insert(10));
    }

    @Test
    void deleteNonExistingValueShouldNotChangeSize() {
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
    void deleteAllShouldRemoveEveryValue() {
        tree.insertAll(List.of(10, 20, 30, 40));

        tree.deleteAll(List.of(20, 40));

        assertEquals(2, tree.size());
        assertFalse(tree.contains(20));
        assertFalse(tree.contains(40));
    }

    @Test
    void mergeAllShouldIgnoreDuplicates() {
        tree.insertAll(List.of(10, 20));

        tree.mergeAll(List.of(20, 30, 40));

        assertEquals(4, tree.size());

        assertTrue(tree.contains(10));
        assertTrue(tree.contains(20));
        assertTrue(tree.contains(30));
        assertTrue(tree.contains(40));
    }

    @Test
    void retainAllShouldKeepOnlySpecifiedValues() {
        tree.insertAll(List.of(10, 20, 30, 40));

        tree.retainAll(List.of(20, 40));

        assertEquals(2, tree.size());

        assertFalse(tree.contains(10));
        assertTrue(tree.contains(20));
        assertFalse(tree.contains(30));
        assertTrue(tree.contains(40));
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
    void minOnEmptyTreeShouldThrow() {
        assertThrows(EmptyTreeException.class, () -> tree.min());
    }

    @Test
    void maxOnEmptyTreeShouldThrow() {
        assertThrows(EmptyTreeException.class, () -> tree.max());
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
        assertThrows(IllegalArgumentException.class, () -> tree.kthSmallest(0));
    }

    @Test
    void kthSmallestGreaterThanSizeShouldThrow() {
        tree.insert(10);

        assertThrows(IllegalArgumentException.class, () -> tree.kthSmallest(2));
    }

    @Test
    void lcaShouldReturnParentForSameSubtree() {
        tree.insert(50);
        tree.insert(20);
        tree.insert(80);
        tree.insert(10);
        tree.insert(30);
        tree.insert(70);
        tree.insert(90);

        assertEquals(20, tree.lca(10, 30));
    }

    @Test
    void lcaShouldReturnRootForDifferentSubtrees() {
        tree.insert(50);
        tree.insert(20);
        tree.insert(80);
        tree.insert(10);
        tree.insert(30);
        tree.insert(70);
        tree.insert(90);

        assertEquals(50, tree.lca(10, 90));
    }

    @Test
    void lcaShouldReturnAncestorNode() {
        tree.insert(50);
        tree.insert(20);
        tree.insert(80);
        tree.insert(10);
        tree.insert(30);

        assertEquals(20, tree.lca(20, 30));
    }

    @Test
    void shouldHandleTenThousandSequentialInserts() {

        for (int i = 1; i <= 10_000; i++) {
            tree.insert(i);
        }

        assertEquals(10_000, tree.size());
    }

    @Test
    void shouldHandleTenThousandSequentialDeletes() {
        for (int i = 1; i <= 10_000; i++) {
            tree.insert(i);
        }
        for (int i = 1; i <= 10_000; i++) {
            tree.delete(i);
        }
        assertEquals(0, tree.size());
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
    void shuffledTest() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            list.add(i);
        }
        TreeSet<Integer> truth = new TreeSet<>();
        Collections.shuffle(list);
        for (Integer x : list) {
            tree.insert(x);
            truth.add(x);
        }
        assertTrue(tree.height() < 40);
        for (Integer x : truth) {
            assertTrue(tree.contains(x));
        }
        assertEquals(truth.size(), tree.size());
        Collections.shuffle(list);
        for (Integer x : list) {
            tree.delete(x);
            truth.remove(x);
        }
        assertEquals(truth.size(), tree.size());
        assertTrue(tree.isEmpty());
    }

    /**
     * Verifies that 100,000 random insert/delete operations
     * keep the tree size consistent with TreeSet.
     */
    @Test
    void randomizedTest() {
        Random r = new Random();
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
    }

    @Test
    void heightShouldRemainLogarithmic() {
        for (int i = 1; i <= 100000; i++) {
            tree.insert(i);
        }
        assertTrue(tree.height() < 17);
    }

    @Test
    void llRotationScenario() {
        tree.insert(1);
        tree.insert(2);
        tree.insert(3);
        assertEquals(2, tree.toList(TraversalType.LEVEL_ORDER).getFirst());
    }

    @Test
    void rrRotationScenario() {
        tree.insert(10);
        tree.insert(8);
        tree.insert(6);
        assertEquals(8, tree.toList(TraversalType.LEVEL_ORDER).getFirst());
    }

    @Test
    void lrRotationScenario() {
        tree.insert(10);
        tree.insert(8);
        tree.insert(9);
        assertEquals(9, tree.toList(TraversalType.LEVEL_ORDER).getFirst());
    }

    @Test
    void rlRotationScenario() {
        tree.insert(10);
        tree.insert(15);
        tree.insert(13);
        assertEquals(13, tree.toList(TraversalType.LEVEL_ORDER).getFirst());
    }

    @Test
    void containsAllShouldReturnTrueWhenAllExist() {
        tree.insertAll(List.of(10, 20, 30));

        assertTrue(tree.containsAll(List.of(10, 20)));
    }

    @Test
    void containsAllShouldReturnFalseWhenOneMissing() {
        tree.insertAll(List.of(10, 20));

        assertFalse(tree.containsAll(List.of(10, 30)));
    }

    @Test
    void shouldHandleStringValuesInNaturalOrder() {
        BinaryTree<String> strings = new AVL<>();

        strings.insert("delta");
        strings.insert("alpha");
        strings.insert("charlie");
        strings.insert("bravo");
        strings.insert("echo");

        assertEquals(List.of("alpha", "bravo", "charlie", "delta", "echo"), strings.inorder());
        assertEquals("alpha", strings.min());
        assertEquals("echo", strings.max());
        assertEquals("charlie", strings.floor("coconut"));
        assertEquals("delta", strings.ceil("coconut"));
        assertEquals("delta", strings.successor("charlie"));
        assertEquals("bravo", strings.predecessor("charlie"));
        assertEquals("charlie", strings.kthSmallest(3));
    }

    @Test
    void shouldHandleCustomComparableObjectsAcrossCoreOperations() {
        BinaryTree<Employee> employees = new AVL<>();
        Employee junior = new Employee(10, "Junior", 25);
        Employee analyst = new Employee(20, "Analyst", 30);
        Employee lead = new Employee(30, "Lead", 35);
        Employee principal = new Employee(40, "Principal", 40);
        Employee architect = new Employee(50, "Architect", 45);

        employees.insert(lead);
        employees.insert(analyst);
        employees.insert(principal);
        employees.insert(junior);
        employees.insert(architect);

        assertEquals(List.of(junior, analyst, lead, principal, architect), employees.inorder());
        assertEquals(junior, employees.min());
        assertEquals(architect, employees.max());
        assertTrue(employees.contains(new Employee(30, "Lead copy", 35)));
        assertEquals(lead, employees.floor(new Employee(35, "Between lead and principal", 37)));
        assertEquals(principal, employees.ceil(new Employee(35, "Between lead and principal", 37)));
        assertEquals(principal, employees.successor(lead));
        assertEquals(analyst, employees.predecessor(lead));
        assertEquals(lead, employees.lca(junior, architect));
        assertEquals(lead, employees.kthSmallest(3));

        employees.delete(new Employee(20, "Analyst copy", 30));

        assertFalse(employees.contains(analyst));
        assertEquals(List.of(junior, lead, principal, architect), employees.inorder());
        assertEquals(4, employees.size());
    }

    @Test
    void shouldRejectNullsForObjectTrees() {
        BinaryTree<Employee> employees = new AVL<>();
        Employee employee = new Employee(1, "Employee", 30);
        employees.insert(employee);

        assertThrows(NullPointerException.class, () -> employees.insertAll(null));
        assertThrows(NullPointerException.class, () -> employees.contains(null));
        assertThrows(NullPointerException.class, () -> employees.deleteAll(null));
        assertThrows(NullPointerException.class, () -> employees.floor(null));
        assertThrows(NullPointerException.class, () -> employees.ceil(null));
        assertThrows(NullPointerException.class, () -> employees.successor(null));
        assertThrows(NullPointerException.class, () -> employees.predecessor(null));
        assertThrows(NullPointerException.class, () -> employees.lca(employee, null));
        assertThrows(NullPointerException.class, () -> employees.lca(null, employee));
        assertThrows(NullPointerException.class, () -> employees.insert((Employee) null));
        assertThrows(NullPointerException.class, () -> employees.insertAll(Arrays.asList(new Employee(2, "Other", 31), null)));
    }

    @Test
    void shouldThrowObjectSpecificFailures() {
        BinaryTree<Employee> employees = new AVL<>();
        Employee employee = new Employee(1, "Employee", 30);

        assertThrows(EmptyTreeException.class, employees::min);
        assertThrows(EmptyTreeException.class, employees::max);
        assertThrows(EmptyTreeException.class, () -> employees.floor(employee));
        assertThrows(EmptyTreeException.class, () -> employees.ceil(employee));
        assertThrows(EmptyTreeException.class, () -> employees.successor(employee));
        assertThrows(EmptyTreeException.class, () -> employees.predecessor(employee));
        assertThrows(EmptyTreeException.class, () -> employees.lca(employee, employee));
        assertThrows(IllegalArgumentException.class, () -> employees.kthSmallest(1));

        employees.insert(employee);

        assertThrows(DuplicateNodeException.class, () -> employees.insert(new Employee(1, "Same id", 99)));
        assertThrows(NodeNotFoundException.class, () -> employees.lca(employee, new Employee(99, "Missing", 99)));
        assertThrows(IllegalArgumentException.class, () -> employees.kthSmallest(0));
        assertThrows(IllegalArgumentException.class, () -> employees.kthSmallest(2));
    }

    @Test
    void duplicateComparableKeyShouldFailEvenWhenObjectsAreDifferent() {
        BinaryTree<PriorityTicket> tickets = new AVL<>();
        tickets.insert(new PriorityTicket(1, "first"));

        assertThrows(DuplicateNodeException.class, () -> tickets.insert(new PriorityTicket(1, "second")));
    }

    @Test
    void compareToFailureShouldPropagateForInvalidComparableObject() {
        BinaryTree<ExplodingComparable> values = new AVL<>();
        values.insert(new ExplodingComparable(10, false));

        assertThrows(IllegalStateException.class, () -> values.insert(new ExplodingComparable(20, true)));
    }

    private record Employee(int id, String name, int age) implements Comparable<Employee> {
        @Override
        public int compareTo(Employee other) {
            return Integer.compare(id, other.id);
        }
    }

    private record PriorityTicket(int priority, String description) implements Comparable<PriorityTicket> {
        @Override
        public int compareTo(PriorityTicket other) {
            return Integer.compare(priority, other.priority);
        }
    }

    private record ExplodingComparable(int value, boolean failOnCompare) implements Comparable<ExplodingComparable> {
        @Override
        public int compareTo(ExplodingComparable other) {
            if (failOnCompare || other.failOnCompare) {
                throw new IllegalStateException("Invalid comparable state");
            }
            return Integer.compare(value, other.value);
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

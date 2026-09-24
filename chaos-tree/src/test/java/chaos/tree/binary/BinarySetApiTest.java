package chaos.tree.binary;

import chaos.tree.AbstractNavigableSetApiTest;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class BinarySetApiTest extends AbstractNavigableSetApiTest {

    @Property(tries = 1000)
    void rbtMatchesTreeSet(
            @ForAll("initialLoad") List<Integer> initial,
            @ForAll("actions") List<Action> actions) {
        runScenario(new RedBlackTreeSet<>(), initial, actions);
    }

    @Property(tries = 1000)
    void avlMatchesTreeSet(
            @ForAll("initialLoad") List<Integer> initial,
            @ForAll("actions") List<Action> actions) {
        runScenario(new AvlTreeSet<>(), initial, actions);
    }

    @Property(tries = 1000)
    void rbtEdgeCases() {
        edgeCases(RedBlackTreeSet::new);
    }

    @Property(tries = 1000)
    void avlEdgeCases() {
        edgeCases(AvlTreeSet::new);
    }

    @Property(tries = 1000)
    void rbtFailFastIterators() {
        failFast(RedBlackTreeSet::new);
    }

    @Property(tries = 1000)
    void avlFailFastIterators() {
        failFast(AvlTreeSet::new);
    }

    @Property(tries = 100)
    void testCloneAndDisplay(@ForAll @IntRange(min = 50, max = 100) int size) {
        AvlTreeSet<Integer> tree1 = new AvlTreeSet<>();
        for (int i = 0; i < size; i++) tree1.add(i);
        Assertions.assertNotNull(tree1.display());
        Assertions.assertEquals(tree1, tree1.clone());
        
        RedBlackTreeSet<Integer> tree2 = new RedBlackTreeSet<>();
        for (int i = 0; i < size; i++) tree2.add(i);
        Assertions.assertNotNull(tree2.display());
        Assertions.assertEquals(tree2, tree2.clone());
    }

    @Property
    void testCloneEmptyTrees() {
        AvlTreeSet<Integer> avl = new AvlTreeSet<>();
        Assertions.assertEquals(avl, avl.clone());
        Assertions.assertTrue(((AvlTreeSet<?>)avl.clone()).isEmpty());
        
        RedBlackTreeSet<Integer> rbt = new RedBlackTreeSet<>();
        Assertions.assertEquals(rbt, rbt.clone());
        Assertions.assertTrue(((RedBlackTreeSet<?>)rbt.clone()).isEmpty());
    }

    @Property
    void testSubSetExceptions() {
        AvlTreeSet<Integer> avl = new AvlTreeSet<>();
        for(int i = 1; i <= 10; i++) avl.add(i);
        
        java.util.NavigableSet<Integer> sub = avl.subSet(3, true, 8, true);
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subSet(1, true, 5, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subSet(5, true, 10, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.headSet(10, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.tailSet(1, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sub.subSet(6, true, 5, true));
        
        java.util.NavigableSet<Integer> descSub = sub.descendingSet();
        Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.subSet(5, true, 6, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.headSet(1, true));
        Assertions.assertThrows(IllegalArgumentException.class, () -> descSub.tailSet(10, true));
    }

    @Property
    void testFailFastSubSetAndDescendingIterators() {
        RedBlackTreeSet<Integer> rbt = new RedBlackTreeSet<>();
        for(int i = 0; i < 10; i++) rbt.add(i);
        
        java.util.Iterator<Integer> descIt = rbt.descendingSet().iterator();
        descIt.next();
        rbt.add(100);
        Assertions.assertThrows(java.util.ConcurrentModificationException.class, descIt::next);
        
        rbt.remove(100);
        java.util.Iterator<Integer> subIt = rbt.subSet(2, 8).iterator();
        subIt.next();
        rbt.add(500);
        Assertions.assertThrows(java.util.ConcurrentModificationException.class, subIt::next);
    }
}

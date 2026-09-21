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

    @Example
    void rbtEdgeCases() {
        edgeCases(RedBlackTreeSet::new);
    }

    @Example
    void avlEdgeCases() {
        edgeCases(AvlTreeSet::new);
    }

    @Example
    void rbtFailFastIterators() {
        failFast(RedBlackTreeSet::new);
    }

    @Example
    void avlFailFastIterators() {
        failFast(AvlTreeSet::new);
    }

    @Property(tries = 100)
    void testCloneAndDisplay(@ForAll @IntRange(min = 50,max = 100) int size) {
        AvlTreeSet<Integer> tree1 = new AvlTreeSet<>();
        for (int i = 0; i < size; i++) {
            tree1.add(i);
        }
        Assertions.assertNotNull(tree1.display());
        Assertions.assertEquals(tree1, tree1.clone());
        RedBlackTreeSet<Integer> tree2 = new RedBlackTreeSet<>();
        for (int i = 0; i < size; i++) {
            tree2.add(i);
        }
        Assertions.assertNotNull(tree2.display());
        Assertions.assertEquals(tree2, tree2.clone());
    }
}

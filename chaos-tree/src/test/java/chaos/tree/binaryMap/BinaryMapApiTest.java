package chaos.tree.binaryMap;

import chaos.tree.AbstractNavigableMapApiTest;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class BinaryMapApiTest extends AbstractNavigableMapApiTest {

    @Property(tries = 1000)
    void rbtMatchesTreeMap(
            @ForAll("initialLoad") List<Integer> initial,
            @ForAll("actions") List<Action> actions) {
        runScenario(new RedBlackTreeMap<>(), initial, actions);
    }

    @Property(tries = 1000)
    void avlMatchesTreeMap(
            @ForAll("initialLoad") List<Integer> initial,
            @ForAll("actions") List<Action> actions) {
        runScenario(new AvlTreeMap<>(), initial, actions);
    }

    @Property(tries = 1000)
    void rbtEdgeCases() {
        edgeCases(RedBlackTreeMap::new);
    }

    @Property(tries = 1000)
    void avlEdgeCases() {
        edgeCases(AvlTreeMap::new);
    }

    @Property(tries = 1000)
    void rbtFailFastIterators() {
        failFast(RedBlackTreeMap::new);
    }

    @Property(tries = 1000)
    void avlFailFastIterators() {
        failFast(AvlTreeMap::new);
    }

    @Property(tries = 1000)
    void rbtFunctionsThatMutateTheMap() {
        mutatingFunctions(RedBlackTreeMap::new);
    }

    @Property(tries = 1000)
    void avlFunctionsThatMutateTheMap() {
        mutatingFunctions(AvlTreeMap::new);
    }

    @Property(tries = 100)
    void testCloneAndDisplay(@ForAll @IntRange(min = 50,max = 100) int size) {
        AvlTreeMap<Integer,Integer> tree1 = new AvlTreeMap<>();
        for (int i = 0; i < size; i++) {
            tree1.put(i,i);
        }
        Assertions.assertNotNull(tree1.display());
        Assertions.assertEquals(tree1, tree1.clone());
        RedBlackTreeMap<Integer,Integer> tree2 = new RedBlackTreeMap<>();
        for (int i = 0; i < size; i++) {
            tree2.put(i,i);
        }
        Assertions.assertNotNull(tree2.display());
        Assertions.assertEquals(tree2, tree2.clone());
    }
}

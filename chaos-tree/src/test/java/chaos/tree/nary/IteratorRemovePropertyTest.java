package chaos.tree.nary;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IteratorRemovePropertyTest {

    @Property(tries = 5000)
    void testBPlusTreeSetIteratorRemove(
            @ForAll @IntRange(min = 3, max = 32) int degree,
            @ForAll @Size(max = 2000) List<@IntRange(min = -1000, max = 1000) Integer> elements,
            @ForAll @Size(max = 2000) List<Boolean> removeDecisions) {

        BPlusTreeSet<Integer> target = new BPlusTreeSet<>(degree);
        TreeSet<Integer> reference = new TreeSet<>();

        for (Integer el : elements) {
            target.add(el);
            reference.add(el);
        }

        Iterator<Integer> targetIt = target.iterator();
        Iterator<Integer> refIt = reference.iterator();
        int decisionIdx = 0;

        while (targetIt.hasNext() && refIt.hasNext()) {
            Integer tVal = targetIt.next();
            Integer rVal = refIt.next();
            assertEquals(rVal, tVal);

            boolean remove = decisionIdx < removeDecisions.size() ? removeDecisions.get(decisionIdx++) : false;
            if (remove) {
                targetIt.remove();
                refIt.remove();
            }
        }

        assertEquals(refIt.hasNext(), targetIt.hasNext());
        assertEquals(reference.size(), target.size());
        assertEquals(new ArrayList<>(reference), new ArrayList<>(target));

        // CLRS White-Box Validation
        validateBPlusTreeSet(target.root, target.minKeys);
    }

    @Property(tries = 5000)
    void testBTreeSetIteratorRemove(
            @ForAll @IntRange(min = 3, max = 32) int degree,
            @ForAll @Size(max = 2000) List<@IntRange(min = -1000, max = 1000) Integer> elements,
            @ForAll @Size(max = 2000) List<Boolean> removeDecisions) {

        BTreeSet<Integer> target = new BTreeSet<>(degree);
        TreeSet<Integer> reference = new TreeSet<>();

        for (Integer el : elements) {
            target.add(el);
            reference.add(el);
        }

        Iterator<Integer> targetIt = target.iterator();
        Iterator<Integer> refIt = reference.iterator();
        int decisionIdx = 0;

        while (targetIt.hasNext() && refIt.hasNext()) {
            Integer tVal = targetIt.next();
            Integer rVal = refIt.next();
            assertEquals(rVal, tVal);

            boolean remove = decisionIdx < removeDecisions.size() ? removeDecisions.get(decisionIdx++) : false;
            if (remove) {
                targetIt.remove();
                refIt.remove();
            }
        }

        assertEquals(refIt.hasNext(), targetIt.hasNext());
        assertEquals(reference.size(), target.size());
        assertEquals(new ArrayList<>(reference), new ArrayList<>(target));

        validateBTreeSet(target.root, target.minKeys);
    }

    private void validateBPlusTreeSet(BPlusTreeNode<?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null) {
            assert node.keyCount >= minKeys : "Node violates minKeys condition";
        }
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) {
                if (node.child[i] != null) {
                    assert node.child[i].parent == node : "Parent pointer is incorrect";
                    validateBPlusTreeSet(node.child[i], minKeys);
                }
            }
        }
    }

    private void validateBTreeSet(BTreeNode<?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null) {
            assert node.keyCount >= minKeys : "Node violates minKeys condition";
        }
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) {
                if (node.child[i] != null) {
                    assert node.child[i].parent == node : "Parent pointer is incorrect";
                    validateBTreeSet(node.child[i], minKeys);
                }
            }
        }
    }
}

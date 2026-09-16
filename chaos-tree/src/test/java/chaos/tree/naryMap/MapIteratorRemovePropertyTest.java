package chaos.tree.naryMap;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapIteratorRemovePropertyTest {

    @Property(tries = 5000)
    void testBPlusTreeMapIteratorRemove(
            @ForAll @IntRange(min = 3, max = 32) int degree,
            @ForAll @Size(max = 2000) List<@IntRange(min = -1000, max = 1000) Integer> elements,
            @ForAll @Size(max = 2000) List<Boolean> removeDecisions) {
            
        BPlusTreeMap<Integer, String> target = new BPlusTreeMap<>(degree);
        TreeMap<Integer, String> reference = new TreeMap<>();

        for (Integer el : elements) {
            target.put(el, "V" + el);
            reference.put(el, "V" + el);
        }

        Iterator<Map.Entry<Integer, String>> targetIt = target.entrySet().iterator();
        Iterator<Map.Entry<Integer, String>> refIt = reference.entrySet().iterator();
        int decisionIdx = 0;

        while (targetIt.hasNext() && refIt.hasNext()) {
            Map.Entry<Integer, String> tVal = targetIt.next();
            Map.Entry<Integer, String> rVal = refIt.next();
            assertEquals(rVal.getKey(), tVal.getKey());

            boolean remove = decisionIdx < removeDecisions.size() ? removeDecisions.get(decisionIdx++) : false;
            if (remove) {
                targetIt.remove();
                refIt.remove();
            }
        }
        
        assertEquals(refIt.hasNext(), targetIt.hasNext());
        assertEquals(reference.size(), target.size());
        assertEquals(new ArrayList<>(reference.keySet()), new ArrayList<>(target.keySet()));
        
        // CLRS White-Box Validation
        validateBPlusTreeMap(target.root, target.minKeys);
    }

    @Property(tries = 5000)
    void testBTreeMapIteratorRemove(
            @ForAll @IntRange(min = 3, max = 32) int degree,
            @ForAll @Size(max = 2000) List<@IntRange(min = -1000, max = 1000) Integer> elements,
            @ForAll @Size(max = 2000) List<Boolean> removeDecisions) {
            
        BTreeMap<Integer, String> target = new BTreeMap<>(degree);
        TreeMap<Integer, String> reference = new TreeMap<>();

        for (Integer el : elements) {
            target.put(el, "V" + el);
            reference.put(el, "V" + el);
        }

        Iterator<Map.Entry<Integer, String>> targetIt = target.entrySet().iterator();
        Iterator<Map.Entry<Integer, String>> refIt = reference.entrySet().iterator();
        int decisionIdx = 0;

        while (targetIt.hasNext() && refIt.hasNext()) {
            Map.Entry<Integer, String> tVal = targetIt.next();
            Map.Entry<Integer, String> rVal = refIt.next();
            assertEquals(rVal.getKey(), tVal.getKey());

            boolean remove = decisionIdx < removeDecisions.size() ? removeDecisions.get(decisionIdx++) : false;
            if (remove) {
                targetIt.remove();
                refIt.remove();
            }
        }
        
        assertEquals(refIt.hasNext(), targetIt.hasNext());
        assertEquals(reference.size(), target.size());
        assertEquals(new ArrayList<>(reference.keySet()), new ArrayList<>(target.keySet()));
        
        // CLRS White-Box Validation
        validateBTreeMap(target.root, target.minKeys);
    }

    private void validateBPlusTreeMap(BPlusTreeMapNode<?, ?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null) {
            assert node.keyCount >= minKeys : "Node violates minKeys condition";
        }
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) {
                if (node.child[i] != null) {
                    assert node.child[i].parent == node : "Parent pointer is incorrect";
                    validateBPlusTreeMap(node.child[i], minKeys);
                }
            }
        }
    }

    private void validateBTreeMap(BTreeMapNode<?, ?> node, int minKeys) {
        if (node == null) return;
        if (node.parent != null) {
            assert node.keyCount >= minKeys : "Node violates minKeys condition";
        }
        if (!node.isLeaf()) {
            for (int i = 0; i <= node.keyCount; i++) {
                if (node.child[i] != null) {
                    assert node.child[i].parent == node : "Parent pointer is incorrect";
                    validateBTreeMap(node.child[i], minKeys);
                }
            }
        }
    }
}

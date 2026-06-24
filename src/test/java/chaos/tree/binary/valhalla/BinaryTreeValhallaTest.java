package chaos.tree.binary.valhalla;

import chaos.tree.binary.BinaryTree;
import chaos.tree.exception.DuplicateNodeException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Valhalla Compatibility Verification
 * Tests that ChaosTree's API contract holds for value-class-like types —
 * immutable, identity-free, Comparable-only semantics.
 * Target: JEP 401 (JDK 28+). These tests verify design intent, not runtime
 * value class behavior.
 */
public abstract class BinaryTreeValhallaTest<TREE extends BinaryTree<BinaryTreeValhallaTest.ValueObject>> {

    protected abstract TREE createTree();

    /**
     * A custom wrapper class that forces new memory allocations.
     * We do NOT use caching or interning (like Integer.valueOf).
     */
    public static class ValueObject implements Comparable<ValueObject> {
        private final int value;

        public ValueObject(int value) {
            this.value = value;
        }

        @Override
        public int compareTo(ValueObject o) {
            return Integer.compare(this.value, o.value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ValueObject that = (ValueObject) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(value);
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    @Test
    void testSearchIgnoresObjectIdentity() {
        TREE tree = createTree();

        ValueObject insertedObj = new ValueObject(42);
        tree.insert(insertedObj);

        ValueObject searchObj = new ValueObject(42);

        assertNotSame(insertedObj, searchObj); // just to CNF

        assertTrue(tree.contains(searchObj));
    }

    @Test
    void testDuplicateRejectionIgnoresIdentity() {
        TREE tree = createTree();

        tree.insert(new ValueObject(100));

        ValueObject duplicateValue = new ValueObject(100);

        assertThrows(DuplicateNodeException.class, () -> tree.insert(duplicateValue));
    }

    @Test
    void testDeletionIgnoresObjectIdentity() {
        TREE tree = createTree();

        tree.insert(new ValueObject(999));
        assertEquals(1, tree.size());

        ValueObject deleteObj = new ValueObject(999);
        tree.delete(deleteObj);

        assertEquals(0, tree.size());
        assertFalse(tree.contains(new ValueObject(999)));
    }

    @Test
    void testRangeIgnoresObjectIdentity() {
        TREE tree = createTree();

        tree.insert(new ValueObject(10));
        tree.insert(new ValueObject(20));
        tree.insert(new ValueObject(30));
        tree.insert(new ValueObject(40));
        tree.insert(new ValueObject(50));

        ValueObject lowerBound = new ValueObject(20);
        ValueObject upperBound = new ValueObject(50);

        List<ValueObject> results = tree.range(lowerBound, upperBound);

        assertEquals(3, results.size());
        assertEquals(20, results.get(0).value);
        assertEquals(30, results.get(1).value);
        assertEquals(40, results.get(2).value);
    }

    @Test
    void testRangeLeftBoundaryIgnoresObjectIdentity() {
        TREE tree = createTree();

        tree.insert(new ValueObject(5));
        tree.insert(new ValueObject(15));
        tree.insert(new ValueObject(25));
        tree.insert(new ValueObject(35));
        tree.insert(new ValueObject(45));
        ValueObject exactLeft = new ValueObject(15);
        ValueObject rightCeiling = new ValueObject(40);

        List<ValueObject> resultsExact = tree.range(exactLeft, rightCeiling);
        assertEquals(3, resultsExact.size());
        assertEquals(15, resultsExact.get(0).value);
        assertEquals(25, resultsExact.get(1).value);
        assertEquals(35, resultsExact.get(2).value);

        ValueObject missingLeft = new ValueObject(10);
        List<ValueObject> resultsMissing = tree.range(missingLeft, rightCeiling);
        assertEquals(3, resultsMissing.size());
        assertEquals(15, resultsMissing.get(0).value);
        assertEquals(25, resultsMissing.get(1).value);
        assertEquals(35, resultsMissing.get(2).value);

        ValueObject absoluteMinLeft = new ValueObject(5);
        List<ValueObject> resultsMin = tree.range(absoluteMinLeft, new ValueObject(20));
        assertEquals(2, resultsMin.size());
        assertEquals(5, resultsMin.get(0).value);
        assertEquals(15, resultsMin.get(1).value);
    }
}

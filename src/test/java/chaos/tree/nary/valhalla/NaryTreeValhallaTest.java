package chaos.tree.nary.valhalla;

import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.nary.NaryTree;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proof of JEP 401 (Project Valhalla) Compatibility.
 * * This test guarantees that the ChaosTree engine strictly uses .compareTo()
 * and never relies on `==` (object identity/memory addresses). If the engine
 * ever uses `==`, these tests will fail because we intentionally allocate
 * mathematically equal objects at completely different memory addresses.
 */
public abstract class NaryTreeValhallaTest<NARY extends NaryTree<NaryTreeValhallaTest.ValueObject>> {

    protected abstract NARY createTree(int degree);

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

    @ParameterizedTest
    @ValueSource(ints = {4, 32, 128})
    void testSearchIgnoresObjectIdentity(int degree) {
        NARY tree = createTree(degree);

        ValueObject insertedObj = new ValueObject(42);
        tree.insert(insertedObj);

        ValueObject searchObj = new ValueObject(42);

        assertNotSame(insertedObj, searchObj);//just to CNF

        assertTrue(tree.contains(searchObj));
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 32, 128})
    void testDuplicateRejectionIgnoresIdentity(int degree) {
        NARY tree = createTree(degree);

        tree.insert(new ValueObject(100));

        ValueObject duplicateValue = new ValueObject(100);

        assertThrows(DuplicateNodeException.class, () -> tree.insert(duplicateValue));
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 32, 128})
    void testDeletionIgnoresObjectIdentity(int degree) {
        NARY tree = createTree(degree);

        tree.insert(new ValueObject(999));
        assertEquals(1, tree.size());

        ValueObject deleteObj = new ValueObject(999);
        tree.delete(deleteObj);

        assertEquals(0, tree.size());
        assertFalse(tree.contains(new ValueObject(999)));
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 32, 128})
    void testRangeIgnoresObjectIdentity(int degree) {
        NARY tree = createTree(degree);

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

    @ParameterizedTest
    @ValueSource(ints = {4, 32, 128})
    void testRangeLeftBoundaryIgnoresObjectIdentity(int degree) {
        NARY tree = createTree(degree);

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
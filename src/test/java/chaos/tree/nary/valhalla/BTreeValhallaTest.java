package chaos.tree.nary.valhalla;

import chaos.tree.nary.BTree;
import org.junit.jupiter.api.DisplayName;

/**
 * Valhalla Compatibility Verification
 * Tests that ChaosTree's API contract holds for value-class-like types —
 * immutable, identity-free, Comparable-only semantics.
 * Target: JEP 401 (JDK 28+). These tests verify design intent, not runtime
 * value class behavior.
 */
@DisplayName("B+ Tree Valhalla (JEP 401) Compatibility Tests")
public class BTreeValhallaTest extends NaryTreeValhallaTest<BTree<NaryTreeValhallaTest.ValueObject>> {

    @Override
    protected BTree<ValueObject> createTree(int degree) {
        return new BTree<>(degree);
    }
}
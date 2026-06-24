package chaos.tree.nary.valhalla;

import chaos.tree.nary.BPlusTree;
import org.junit.jupiter.api.DisplayName;
/**
 * Valhalla Compatibility Verification
 * Tests that ChaosTree's API contract holds for value-class-like types —
 * immutable, identity-free, Comparable-only semantics.
 * Target: JEP 401 (JDK 28+). These tests verify design intent, not runtime
 * value class behavior.
 */
@DisplayName("B+ Tree Valhalla (JEP 401) Compatibility Tests")
public class BPlusTreeValhallaTest extends NaryTreeValhallaTest<BPlusTree<NaryTreeValhallaTest.ValueObject>> {

    @Override
    protected BPlusTree<ValueObject> createTree(int degree) {
        // Injects the B+ Tree engine into the inherited abstract test suite
        return new BPlusTree<>(degree);
    }
}

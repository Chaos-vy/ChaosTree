package chaos.tree.binary.valhalla;

import chaos.tree.binary.Treap;
import org.junit.jupiter.api.DisplayName;

/**
 * Valhalla Compatibility Verification
 * Tests that ChaosTree's API contract holds for value-class-like types —
 * immutable, identity-free, Comparable-only semantics.
 * Target: JEP 401 (JDK 28+). These tests verify design intent, not runtime
 * value class behavior.
 */
@DisplayName("Treap Valhalla (JEP 401) Compatibility Tests")
public class TreapValhallaTest extends BinaryTreeValhallaTest<Treap<BinaryTreeValhallaTest.ValueObject>> {
    @Override
    protected Treap<BinaryTreeValhallaTest.ValueObject> createTree() {
        return new Treap<>();
    }
}

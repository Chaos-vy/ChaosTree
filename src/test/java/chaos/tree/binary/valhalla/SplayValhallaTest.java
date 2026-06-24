package chaos.tree.binary.valhalla;

import chaos.tree.binary.Splay;
import org.junit.jupiter.api.DisplayName;

/**
 * Valhalla Compatibility Verification
 * Tests that ChaosTree's API contract holds for value-class-like types —
 * immutable, identity-free, Comparable-only semantics.
 * Target: JEP 401 (JDK 28+). These tests verify design intent, not runtime
 * value class behavior.
 */
@DisplayName("Splay Valhalla (JEP 401) Compatibility Tests")
public class SplayValhallaTest extends BinaryTreeValhallaTest<Splay<BinaryTreeValhallaTest.ValueObject>> {
    @Override
    protected Splay<BinaryTreeValhallaTest.ValueObject> createTree() {
        return new Splay<>();
    }
}

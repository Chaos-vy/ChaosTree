package chaos.tree.nary.valhalla;

import chaos.tree.nary.BPlusTree;
import org.junit.jupiter.api.DisplayName;

/**
 * Executes the strict JEP 401 Value-Class compatibility tests
 * specifically against the B+ Tree engine.
 */
@DisplayName("B+ Tree Valhalla (JEP 401) Compatibility Tests")
public class BPlusTreeValhallaTest extends NaryTreeValhallaTest<BPlusTree<NaryTreeValhallaTest.ValueObject>> {

    @Override
    protected BPlusTree<ValueObject> createTree(int degree) {
        // Injects the B+ Tree engine into the inherited abstract test suite
        return new BPlusTree<>(degree);
    }
}

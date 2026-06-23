package chaos.tree.nary.valhalla;

import chaos.tree.nary.BTree;
import org.junit.jupiter.api.DisplayName;

/**
 * Executes the strict JEP 401 Value-Class compatibility tests
 * specifically against the B Tree engine.
 */
@DisplayName("B+ Tree Valhalla (JEP 401) Compatibility Tests")
public class BTreeValhallaTest extends NaryTreeValhallaTest<BTree<NaryTreeValhallaTest.ValueObject>> {

    @Override
    protected BTree<ValueObject> createTree(int degree) {
        return new BTree<>(degree);
    }
}
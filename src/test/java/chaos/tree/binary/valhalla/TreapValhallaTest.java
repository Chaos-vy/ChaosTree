package chaos.tree.binary.valhalla;

import chaos.tree.binary.Treap;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Treap Valhalla (JEP 401) Compatibility Tests")
public class TreapValhallaTest extends BinaryTreeValhallaTest<Treap<BinaryTreeValhallaTest.ValueObject>> {
    @Override
    protected Treap<BinaryTreeValhallaTest.ValueObject> createTree() {
        return new Treap<>();
    }
}

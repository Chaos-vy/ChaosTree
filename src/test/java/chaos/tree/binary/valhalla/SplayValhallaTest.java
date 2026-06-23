package chaos.tree.binary.valhalla;

import chaos.tree.binary.Splay;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Splay Valhalla (JEP 401) Compatibility Tests")
public class SplayValhallaTest extends BinaryTreeValhallaTest<Splay<BinaryTreeValhallaTest.ValueObject>> {
    @Override
    protected Splay<BinaryTreeValhallaTest.ValueObject> createTree() {
        return new Splay<>();
    }
}

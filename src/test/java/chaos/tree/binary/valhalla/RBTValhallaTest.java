package chaos.tree.binary.valhalla;

import chaos.tree.binary.RBT;
import org.junit.jupiter.api.DisplayName;

@DisplayName("RBT Valhalla (JEP 401) Compatibility Tests")
public class RBTValhallaTest extends BinaryTreeValhallaTest<RBT<BinaryTreeValhallaTest.ValueObject>> {
    @Override
    protected RBT<BinaryTreeValhallaTest.ValueObject> createTree() {
        return new RBT<>();
    }
}

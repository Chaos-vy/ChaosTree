package chaos.tree.binary.valhalla;

import chaos.tree.binary.AVL;
import org.junit.jupiter.api.DisplayName;

@DisplayName("AVL Valhalla (JEP 401) Compatibility Tests")
public class AVLValhallaTest extends BinaryTreeValhallaTest<AVL<BinaryTreeValhallaTest.ValueObject>> {
    @Override
    protected AVL<BinaryTreeValhallaTest.ValueObject> createTree() {
        return new AVL<>();
    }
}

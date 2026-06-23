package chaos.tree.binary.valhalla;

import chaos.tree.binary.BST;
import org.junit.jupiter.api.DisplayName;

@DisplayName("BST Valhalla (JEP 401) Compatibility Tests")
public class BSTValhallaTest extends BinaryTreeValhallaTest<BST<BinaryTreeValhallaTest.ValueObject>> {
    @Override
    protected BST<BinaryTreeValhallaTest.ValueObject> createTree() {
        return new BST<>();
    }
}

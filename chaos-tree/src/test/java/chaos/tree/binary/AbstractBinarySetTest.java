package chaos.tree.binary;

import static org.junit.jupiter.api.Assertions.*;
/*
Avl height must be balanced
RBT -5 rule must be checked where it checks 4
- count of black node
- red-red conflict
- root not black
- null must be black
- every node color(R|B) -> not a part
- Binary tree rule must be also checked
 */
public abstract class AbstractBinarySetTest {

    protected int verifyAvlInvariants(AvlNode<Integer> node) {
        if (node == null) return -1;

        int leftHeight = verifyAvlInvariants(node.left);
        int rightHeight = verifyAvlInvariants(node.right);

        assertTrue(Math.abs(leftHeight - rightHeight) <= 1, "AVL Height violated at node " + node.value);
        assertEquals(Math.max(leftHeight, rightHeight) + 1, node.height, "Node height is incorrect at node " + node.value);

        if (node.left != null) {
            assertSame(node, node.left.parent, "Parent pointer incorrect for left child of " + node.value);
            assertTrue(node.left.value.compareTo(node.value) < 0, "BST invariant violated (left >= root)");
        }
        if (node.right != null) {
            assertSame(node, node.right.parent, "Parent pointer incorrect for right child of " + node.value);
            assertTrue(node.right.value.compareTo(node.value) > 0, "BST invariant violated (right <= root)");
        }

        return Math.max(leftHeight, rightHeight) + 1;
    }

    protected void verifyRbtInvariants(RedBlackTreeSet<Integer> tree) {
        if (tree.root == null) return;
        assertFalse(tree.root.isRed(), "RBT root is not black");
        verifyRbtNode(tree.root, 0);
    }

    private int verifyRbtNode(RbtNode<Integer> node, int blackCount) {
        if (node == null) return blackCount + 1;

        if (!node.isRed()) {
            blackCount++;
        }

        if (node.isRed()) {
            if (node.left != null) assertFalse(node.left.isRed(), "Red node has red left child causing RR conflict");
            if (node.right != null) assertFalse(node.right.isRed(), "Red node has red right child causing RR conflicts");
        }

        if (node.left != null) {
            assertSame(node, node.left.parent, "Parent pointer for left child seems incorrect");
            assertTrue(node.left.value.compareTo(node.value) < 0, "BST law violated [Flawed]");
        }
        if (node.right != null) {
            assertSame(node, node.right.parent, "Parent pointer for right child maybe incorrect");
            assertTrue(node.right.value.compareTo(node.value) > 0, "BST law violated[Flawed]");
        }

        int leftBlackCount = verifyRbtNode(node.left, blackCount);
        int rightBlackCount = verifyRbtNode(node.right, blackCount);
        assertEquals(leftBlackCount, rightBlackCount, "Paths from node " + node.value + " have different black counts");

        return leftBlackCount;
    }
}

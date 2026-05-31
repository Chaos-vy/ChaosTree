package chaos.tree.binary;

import chaos.tree.core.ITree;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RBTTest {
    RBT<Integer> rbt = new RBT<>();
    @Test
    void testBlackHeightIsBalanced(){
        rbt.insertAll(List.of(1,2,3,4,5,6,7,8,9));
        assertTrue(rbt.isBalanced());
        rbt.delete(4);
        rbt.delete(1);
        rbt.insert(45);
        assertTrue(rbt.isBalanced());
    }
}
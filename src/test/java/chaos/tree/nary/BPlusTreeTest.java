package chaos.tree.nary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


class BPlusTreeTest extends NaryTreeContractTest<BPlusTree<Integer>>{

    @Override
    protected BPlusTree<Integer> createTree(int degree) {
        return new BPlusTree<>(degree);
    }

    @Override
    protected BPlusTree<Integer> createFromIterable(int degree, Iterable<Integer> it) {
        return new BPlusTree<>(degree, it);
    }

    @Override
    protected BPlusTree<Integer> createCopy(BPlusTree<Integer> source) {
        return new BPlusTree<>(source);
    }

    @Test
    public void testFloorFallback() {
        BPlusTree<Integer> bplus = new BPlusTree<>(4);
        // Create a specific leaf structure to test the left-node fallback
        int[] inserts = {10, 20, 30, 40, 50, 60, 70, 80};
        for (int i : inserts) {
            bplus.insert(i);
        }
        
        // 45 is strictly before 50, so if routed to a leaf starting at 50, 
        // it must correctly fallback to the rightmost element of the previous leaf (40)
        assertEquals(40, bplus.floor(45));
    }
}
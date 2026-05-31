package chaos.tree.binary;

import chaos.tree.exception.DuplicateNodeException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BSTTest {

    @Test
    void insertionShouldMaintainSortedOrder() {
        BST<Integer> bst = new BST<>();
        bst.insertAll(List.of(5, 3, 7, 1, 4));
        assertEquals(List.of(1, 3, 4, 5, 7), bst.inorder());
    }
    @Test
    void deletionShouldMaintainSortedOrder(){
        BST<Integer> bst = new BST<>();
        bst.insertAll(List.of(5, 3, 7, 1, 4));
        bst.delete(3);
        assertEquals(List.of(1,4,5,7), bst.inorder());
    }
    @Test
    void sizeMustMaintainAfterEachInsertAndDelete(){
        BST<Integer> bst = new BST<>();
        bst.insertAll(List.of(5, 3, 7, 1, 4));
        assertEquals(5,bst.size());
        bst.delete(3);
        assertEquals(4,bst.size());
    }
    @Test
    void heightMustBeMaintained(){
        BST<Integer> bst = new BST<>();
        bst.insertAll(List.of(5, 3, 7, 1, 4));
        assertEquals(2,bst.height());
    }
    @Test
    void clearMustSetMetadataToInitial(){
        BST<Integer> bst = new BST<>();
        bst.insertAll(List.of(5, 3, 7, 1, 4));
        bst.clear();
        assertEquals(0,bst.size());
        assertEquals(-1,bst.height());
    }
    @Test
    void searchMustPositiveResult(){
        BST<Integer> bst = new BST<>();
        bst.insertAll(List.of(5, 3, 7, 1, 4));
        assertTrue(bst.search(4));
    }
    @Test
    void DuplicateNodeException(){
        BST<Integer> bst = new BST<>();
        bst.insertAll(List.of(5, 3, 7, 1, 4));
        assertThrows(DuplicateNodeException.class, () -> {
            bst.insert(3);
        });
    }
}
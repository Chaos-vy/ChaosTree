package chaos.tree.binary;

import chaos.tree.exception.DuplicateNodeException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AVLTest {

    @Test
    void insertionShouldMaintainSortedOrder() {
        AVL<Integer> avl = new AVL<>();
        avl.insertAll(List.of(5, 3, 7, 1, 4));
        assertEquals(List.of(1, 3, 4, 5, 7), avl.inorder());
    }
    @Test
    void deletionShouldMaintainSortedOrder(){
        AVL<Integer> avl = new AVL<>();
        avl.insertAll(List.of(5, 3, 7, 1, 4));
        avl.delete(3);
        assertEquals(List.of(1,4,5,7), avl.inorder());
    }
    @Test
    void sizeMustMaintainAfterEachInsertAndDelete(){
        AVL<Integer> avl = new AVL<>();
        avl.insertAll(List.of(5, 3, 7, 1, 4));
        assertEquals(5,avl.size());
        avl.delete(3);
        assertEquals(4,avl.size());
    }
    @Test
    void heightMustBeMaintained(){
        AVL<Integer> avl = new AVL<>();
        avl.insertAll(List.of(5, 3, 7, 1, 4));
        assertEquals(2,avl.height());
    }
    @Test
    void clearMustSetMetadataToInitial(){
        AVL<Integer> avl = new AVL<>();
        avl.insertAll(List.of(5, 3, 7, 1, 4));
        avl.clear();
        assertEquals(0,avl.size());
        assertEquals(-1,avl.height());
    }
    @Test
    void searchMustPositiveResult(){
        AVL<Integer> avl = new AVL<>();
        avl.insertAll(List.of(5, 3, 7, 1, 4));
        assertTrue(avl.search(4));
    }
    @Test
    void DuplicateNodeException(){
        AVL<Integer> avl = new AVL<>();
        avl.insertAll(List.of(5, 3, 7, 1, 4));
        assertThrows(DuplicateNodeException.class, () -> {
            avl.insert(3);
        });
    }
    @Test
    void heightMustBeBalanced(){
        AVL<Integer> avl = new AVL<>();
        avl.insertAll(List.of(1,2,3,4,5,6,7,8,9,10));
        assertEquals(3,avl.height());
    }
}
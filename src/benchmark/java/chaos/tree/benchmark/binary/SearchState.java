package chaos.tree.benchmark.binary;

import java.util.concurrent.TimeUnit;

import chaos.tree.binary.bst.BST;
import chaos.tree.binary.rbt.RBT;
import chaos.tree.binary.splay.Splay;
import chaos.tree.binary.treap.Treap;
import chaos.tree.binary.avl.AVL;
import org.openjdk.jmh.annotations.*;

/**
 * JMH benchmark to measure the worst-case performance of binary search tree operations.
 *
 * <p>By inserting sorted elements sequentially during the {@link Level#Iteration} setup,
 * we construct a degenerate right-skewed linear structure for the unbalanced BST tree,
 * while balanced trees (AVL, RBT, Splay, Treap) maintain their logarithmic height bounds.
 * We measure both worst-case searches and mutating insert/delete sequences.</p>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class SearchState {

    @Param({"100", "500", "1000", "2000", "5000"})
    int size;

    BST<Integer> bst;
    AVL<Integer> avl;
    RBT<Integer> rbt;
    Splay<Integer> splay;
    Treap<Integer> treap;

    int searchValue;
    int insertValue;

    @Setup(Level.Iteration)
    public void setup() {
        bst = new BST<>();
        avl = new AVL<>();
        rbt = new RBT<>();
        splay = new Splay<>();
        treap = new Treap<>();

        // Worst-case setup: sequentially insert sorted elements to create a degenerate structure
        for (int i = 1; i <= size; i++) {
            bst.insert(i);
            avl.insert(i);
            rbt.insert(i);
            splay.insert(i);
            treap.insert(i);
        }

        // Deepest leaf for worst-case lookup/insertion
        searchValue = size;
        insertValue = size + 1;
    }

    // --- Search (Worst Case) ---

    @Benchmark
    public boolean bstSearch() {
        return bst.contains(searchValue);
    }

    @Benchmark
    public boolean avlSearch() {
        return avl.contains(searchValue);
    }

    @Benchmark
    public boolean rbtSearch() {
        return rbt.contains(searchValue);
    }

    @Benchmark
    public boolean splaySearch() {
        return splay.contains(searchValue);
    }

    @Benchmark
    public boolean treapSearch() {
        return treap.contains(searchValue);
    }

    // --- Insert / Delete (Worst Case) ---

    @Benchmark
    public void bstInsertDelete() {
        bst.insert(insertValue);
        bst.delete(insertValue);
    }

    @Benchmark
    public void avlInsertDelete() {
        avl.insert(insertValue);
        avl.delete(insertValue);
    }

    @Benchmark
    public void rbtInsertDelete() {
        rbt.insert(insertValue);
        rbt.delete(insertValue);
    }

    @Benchmark
    public void splayInsertDelete() {
        splay.insert(insertValue);
        splay.delete(insertValue);
    }

    @Benchmark
    public void treapInsertDelete() {
        treap.insert(insertValue);
        treap.delete(insertValue);
    }
}

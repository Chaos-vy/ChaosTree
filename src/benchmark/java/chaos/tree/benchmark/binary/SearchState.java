package chaos.tree.benchmark.binary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import chaos.tree.binary.bst.BST;
import chaos.tree.binary.rbt.RBT;
import chaos.tree.binary.splay.Splay;
import chaos.tree.binary.treap.Treap;
import chaos.tree.binary.avl.AVL;
import org.openjdk.jmh.annotations.*;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
public class SearchState {

    @Param({"100", "1000", "5000", "10000", "50000"})
    int size;

    BST<Integer> bst;
    AVL<Integer> avl;
    RBT<Integer> rbt;
    Splay<Integer> splay;
    Treap<Integer> treap;

    int[] searchValues;
    int searchIndex;

    private int nextSearch() {
        int idx = searchIndex;
        searchIndex = (searchIndex + 1) % size;
        return searchValues[idx];
    }

    @Setup(Level.Trial)
    public void setup() {

        bst = new BST<>();
        avl = new AVL<>();
        rbt = new RBT<>();
        splay = new Splay<>();
        treap = new Treap<>(50000);



        List<Integer> shuffled = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            shuffled.add(i);
        }
        Collections.shuffle(
                shuffled,
                new Random(42)
        );

        bst.insertAll(shuffled);
        avl.insertAll(shuffled);
        rbt.insertAll(shuffled);
        splay.insertAll(shuffled);
        treap.insertAll(shuffled);

        searchValues = new int[size];
        Random rng = new Random(42);
        for (int i = 0; i < size; i++) {
            searchValues[i] = rng.nextInt(size) + 1;
        }
    }

    @Benchmark
    public boolean bstSearch() {
        return bst.contains(nextSearch());
    }

    @Benchmark
    public boolean avlSearch() {
        return avl.contains(nextSearch());
    }

    @Benchmark
    public boolean rbtSearch() {
        return rbt.contains(nextSearch());
    }

    @Benchmark
    public boolean splaySearch() {
        return splay.contains(nextSearch());
    }

    @Benchmark
    public boolean treapSearch() {
        return treap.contains(nextSearch());
    }
}

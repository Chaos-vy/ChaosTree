package chaos.tree.benchmark.binary;

import chaos.tree.binary.avl.AVL;
import chaos.tree.binary.bst.BST;
import chaos.tree.binary.rbt.RBT;
import chaos.tree.binary.splay.Splay;
import chaos.tree.binary.treap.Treap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks the allocation and structural generation speeds of trees.
 * Tests both $O(n \log n)$ randomized insertion from an Iterable, and
 * $O(n)$ structural deep cloning.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ConstructionBenchmark extends AbstractBinaryBenchmark {

    private List<Integer> sourceData;

    private BST<Integer> prebuiltBst;
    private AVL<Integer> prebuiltAvl;
    private RBT<Integer> prebuiltRbt;
    private Splay<Integer> prebuiltSplay;
    private Treap<Integer> prebuiltTreap;

    // Java Tree Set
    private TreeSet<Integer> prebuiltJavaTreeSet;

    @Setup(Level.Trial)
    public void setup() {
        sourceData = new ArrayList<>(size);
        int[] shuffled = getShuffledInts(size);
        for (int v : shuffled) {
            sourceData.add(v);
        }

        prebuiltBst = new BST<>(sourceData);
        prebuiltAvl = new AVL<>(sourceData);
        prebuiltRbt = new RBT<>(sourceData);
        prebuiltSplay = new Splay<>(sourceData);
        prebuiltTreap = new Treap<>(sourceData);

        prebuiltJavaTreeSet = new TreeSet<>(sourceData);
    }

    // O(nlogn) CLONE
    @Benchmark
    public TreeSet<Integer> javaTreeSetFromIterable() {
        return new TreeSet<>(sourceData);
    }

    @Benchmark
    public BST<Integer> bstFromIterable() {
        return new BST<>(sourceData);
    }

    @Benchmark
    public AVL<Integer> avlFromIterable() {
        return new AVL<>(sourceData);
    }

    @Benchmark
    public RBT<Integer> rbtFromIterable() {
        return new RBT<>(sourceData);
    }

    @Benchmark
    public Splay<Integer> splayFromIterable() {
        return new Splay<>(sourceData);
    }

    @Benchmark
    public Treap<Integer> treapFromIterable() {
        return new Treap<>(sourceData);
    }

    // 2. O(n) STRUCTURAL DEEP CLONING

    @Benchmark
    public TreeSet<Integer> javaTreeSetClone() {
        return new TreeSet<>(prebuiltJavaTreeSet);
    }

    @Benchmark
    public BST<Integer> bstClone() {
        return new BST<>(prebuiltBst);
    }

    @Benchmark
    public AVL<Integer> avlClone() {
        return new AVL<>(prebuiltAvl);
    }

    @Benchmark
    public RBT<Integer> rbtClone() {
        return new RBT<>(prebuiltRbt);
    }

    @Benchmark
    public Splay<Integer> splayClone() {
        return new Splay<>(prebuiltSplay);
    }

    @Benchmark
    public Treap<Integer> treapClone() {
        return new Treap<>(prebuiltTreap);
    }


}
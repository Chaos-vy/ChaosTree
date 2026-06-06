package chaos.tree.benchmark;

import chaos.tree.binary.avl.AVL;
import chaos.tree.binary.bst.BST;
import chaos.tree.binary.rbt.RBT;
import chaos.tree.binary.splay.Splay;
import chaos.tree.binary.treap.Treap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
@Fork(1)
public class BinaryTreeBenchmark {

    private BST<Integer> bst;
    private AVL<Integer> avl;
    private RBT<Integer> rbt;
    private Splay<Integer> splay;
    private Treap<Integer> treap;

    @Param({"100", "1000", "10000"})
    private int size;

    @Setup(Level.Iteration)
    public void setup() {
        int[] shuffledData = IntStream.range(0, size).toArray();
        Random rng = new Random(42);
        for (int i = size - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = shuffledData[i];
            shuffledData[i] = shuffledData[j];
            shuffledData[j] = tmp;
        }

        bst = new BST<>();
        avl = new AVL<>();
        rbt = new RBT<>();
        splay = new Splay<>();
        treap = new Treap<>();

        for (int val : shuffledData) {
            bst.insert(val);
            avl.insert(val);
            rbt.insert(val);
            splay.insert(val);
            treap.insert(val);
        }

        int benchmarkValue = shuffledData[size / 2];
    }

    @Benchmark
    public void bstInsert() {
        bst.insert(size + bstCounter++);
    }
    private int bstCounter=0;

    @Benchmark
    public void avlInsert() {
        avl.insert(size + avlCounter++);
    }
    private int avlCounter=0;

    @Benchmark
    public void rbtInsert() {
        rbt.insert(size + rbtCounter++);
    }
    private int rbtCounter=0;

    @Benchmark
    public void splayInsert() {splay.insert(size + splayCounter++);}
    private int splayCounter = 0;

    @Benchmark
    public void treapInsert() {treap.insert(size + treapCounter++);}
    private int treapCounter=0;

    @Benchmark
    public void bstDelete() {
        bst.delete(size / 2);
    }

    @Benchmark
    public void avlDelete() {
        avl.delete(size / 2);
    }

    @Benchmark
    public void rbtDelete() {
        rbt.delete(size / 2);
    }

    @Benchmark
    public void splayDelete() {
        splay.delete(size / 2);
    }

    @Benchmark
    public void treapDelete() {
        treap.delete(size / 2);
    }

    @Benchmark
    public boolean bstSearch() {
        return bst.contains(size / 2);
    }

    @Benchmark
    public boolean avlSearch() {
        return avl.contains(size / 2);
    }

    @Benchmark
    public boolean rbtSearch() {
        return rbt.contains(size / 2);
    }

    @Benchmark
    public void splaySearch() {
        splay.contains(size / 2);
    }

    @Benchmark
    public void treapSearch() {
        treap.contains(size / 2);
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder().include(BinaryTreeBenchmark.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
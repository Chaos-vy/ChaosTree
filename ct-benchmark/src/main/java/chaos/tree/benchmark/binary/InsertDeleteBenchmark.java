package chaos.tree.benchmark.binary;

import chaos.tree.binary.AVL;
import chaos.tree.binary.BST;
import chaos.tree.binary.RBT;
import chaos.tree.binary.Treap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * I like to wish the benchmark data as per this configuration
 * warmup >= 3
 * warmup time >= 5
 * measurement iteration >= 5
 * measurement time >= 5s
 * fork >=5
 */
public class InsertDeleteBenchmark extends AbstractBinaryBenchmark {

    private Set<Integer> bst;
    private Set<Integer> avl;
    private Set<Integer> rbt;
    private Set<Integer> treap;
    private Set<Integer> javaTreeSet; // Standard Java Library baseline

    /** Pool of keys guaranteed absent from the populated tree, interleaved within its key range. */
    private int[] mutationPool;
    private int poolMask;

    // Independent cursors per benchmark so one method's call count never
    // affects another's position in the pool.
    private int avlCursor;
    private int bstCursor;
    private int treapCursor;
    private int rbtCursor;
    private int treeSetCursor;

    @Setup(Level.Trial)
    public void buildStaticTrees() {
        bst = new BST<>();
        avl = new AVL<>();
        rbt = new RBT<>();
        treap = new Treap<>();
        javaTreeSet = new TreeSet<>();

        // Populate every tree with the same `size` distinct EVEN keys,
        // shuffled for insertion order, spanning [0, 2*size).
        int[] evenData = new int[size];
        for (int i = 0; i < size; i++) {
            evenData[i] = 2 * i;
        }
        shuffleInPlace(evenData, 999L);

        for (int v : evenData) {
            bst.add(v);
            avl.add(v);
            rbt.add(v);
            treap.add(v);
            javaTreeSet.add(v);
        }

        // Mutation pool: every ODD key in the same [0, 2*size) range.
        // Guaranteed absent from every tree at all times outside the
        // brief window between insert() and remove() within one invocation.
        // If you can think of better do think that but I amy need a reason LOL
        int poolSize = Integer.highestOneBit(Math.max(size, 2));
        if (poolSize < size) poolSize <<= 1;
        poolMask = poolSize - 1;

        mutationPool = new int[poolSize];
        for (int i = 0; i < poolSize; i++) {
            mutationPool[i] = 2 * i + 1;
        }
        shuffleInPlace(mutationPool, 12345L); // fixed seed: reproducible runs

        avlCursor = 0;
        rbtCursor = 0;
        treeSetCursor = 0;
        bstCursor = 0;
        treapCursor = 0;
    }

    private static void shuffleInPlace(int[] arr, long seed) {
        Random rnd = new Random(seed);
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    @Benchmark
    public void javaTreeSetInsertDelete(Blackhole bh) {
        int key = mutationPool[treeSetCursor++ & poolMask];
        bh.consume(javaTreeSet.add(key));
        bh.consume(javaTreeSet.remove(key));
    }

    @Benchmark
    public void bstInsertDelete(Blackhole bh) {
        int key = mutationPool[bstCursor++ & poolMask];
        bh.consume(bst.add(key));
        bh.consume(bst.remove(key));
    }

    @Benchmark
    public void avlInsertDelete(Blackhole bh) {
        int key = mutationPool[avlCursor++ & poolMask];
        bh.consume(avl.add(key));
        bh.consume(avl.remove(key));
    }

    @Benchmark
    public void rbtInsertDelete(Blackhole bh) {
        int key = mutationPool[rbtCursor++ & poolMask];
        bh.consume(rbt.add(key));
        bh.consume(rbt.remove(key));
    }

    @Benchmark
    public void treapInsertDelete(Blackhole bh) {
        int key = mutationPool[treapCursor++ & poolMask];
        bh.consume(treap.add(key));
        bh.consume(treap.remove(key));
    }
}
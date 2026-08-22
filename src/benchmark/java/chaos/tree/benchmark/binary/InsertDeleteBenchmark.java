package chaos.tree.benchmark.binary;

import chaos.tree.binary.AVL;
import chaos.tree.binary.BST;
import chaos.tree.binary.RBT;
import chaos.tree.binary.Treap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Set;
import java.util.TreeSet;


public class InsertDeleteBenchmark extends AbstractBinaryBenchmark {

    private Set<Integer> bst;
    private Set<Integer> avl;
    private Set<Integer> rbt;
    private Set<Integer> treap;
    private Set<Integer> javaTreeSet; // Standard Java Library
    private int[] mutationPool;
    private int poolMask;
    private int avlCursor;
    private int bstCursor;
    private int treapCursor;
    private int rbtCursor;
    private int treeSetCursor;

    @Setup(Level.Trial)
    public void buildStaticTrees() {
        int[] data = getShuffledInts(size);

        bst = new BST<>();
        avl = new AVL<>();
        rbt = new RBT<>();
        treap = new Treap<>();
        javaTreeSet = new TreeSet<>();

        for (int v : data) {
            bst.add(v);
            avl.add(v);
            rbt.add(v);
            treap.add(v);
            javaTreeSet.add(v);
        }
        int poolSize = Integer.highestOneBit(Math.max(size, 2)) ;
        if (poolSize < size) poolSize <<= 1;
        poolMask = poolSize - 1;

        mutationPool = new int[poolSize];
        for (int i = 0; i < poolSize; i++) {
            mutationPool[i] = size + i;
        }
        shuffleInPlace(mutationPool);

        avlCursor = 0;
        rbtCursor = 0;
        treeSetCursor = 0;
        bstCursor = 0;
        treapCursor = 0;
    }

    private static void shuffleInPlace(int[] arr) {
        java.util.Random rnd = new java.util.Random(12345L); // fixed seed: reproducible runs
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
        bh.consume(bst.add(key));
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
        bh.consume(treap.add(key));
    }
}
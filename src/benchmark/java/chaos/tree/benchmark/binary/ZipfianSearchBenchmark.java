package chaos.tree.benchmark.binary;

import chaos.tree.binary.avl.AVL;
import chaos.tree.binary.bst.BST;
import chaos.tree.binary.rbt.RBT;
import chaos.tree.binary.splay.Splay;
import chaos.tree.binary.treap.Treap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

import java.util.Arrays;
import java.util.Random;
import java.util.TreeSet;

/**
 * Measures search latency under a highly skewed, real-world access pattern.
 * * <p>Uses a Zipfian distribution where a small subset of "hot" nodes receive
 * the vast majority of search traffic, accurately modeling database caches,
 * network routers, and temporal locality.</p>
 */
public class ZipfianSearchBenchmark extends AbstractBinaryBenchmark {

    private static final int QUERY_POOL_SIZE = 1048576; // 2^20
    private static final int QUERY_MASK = QUERY_POOL_SIZE - 1;
    @Param({"1.0"})
    double zipfSkew;
    private BST<Integer> bst;
    private AVL<Integer> avl;
    private RBT<Integer> rbt;
    private Splay<Integer> splay;
    private Treap<Integer> treap;
    private TreeSet<Integer> javaTreeSet;
    private int[] queries;
    private int[] data;
    private int queryIndex = 0;

    @Setup(Level.Trial)
    public void setupDataAndTrees() {
        data = getShuffledInts(size, 84L);

        bst = new BST<>();
        avl = new AVL<>();
        rbt = new RBT<>();
        treap = new Treap<>();
        javaTreeSet = new TreeSet<>();

        for (int v : data) {
            bst.insert(v);
            avl.insert(v);
            rbt.insert(v);
            treap.insert(v);
            javaTreeSet.add(v);
        }
        queries = generateZipfianQueries(data, QUERY_POOL_SIZE, zipfSkew);
    }

    @Setup(Level.Iteration)
    public void resetSplay() {
        splay = new Splay<>();
        for (int v : data) {
            splay.insert(v);
        }
    }

    @Benchmark
    public boolean javaTreeSetSearch() {
        return javaTreeSet.contains(queries[queryIndex++ & QUERY_MASK]);
    }

    @Benchmark
    public boolean bstSearch() {
        return bst.contains(queries[queryIndex++ & QUERY_MASK]);
    }

    @Benchmark
    public boolean avlSearch() {
        return avl.contains(queries[queryIndex++ & QUERY_MASK]);
    }

    @Benchmark
    public boolean rbtSearch() {
        return rbt.contains(queries[queryIndex++ & QUERY_MASK]);
    }

    @Benchmark
    public boolean splaySearch() {
        return splay.contains(queries[queryIndex++ & QUERY_MASK]);
    }

    @Benchmark
    public boolean treapSearch() {
        return treap.contains(queries[queryIndex++ & QUERY_MASK]);
    }

    /**
     * Pre-calculates an array of queries following a Zipfian distribution.
     * Generates queries using Inverse Transform Sampling.
     */
    private int[] generateZipfianQueries(int[] population, int numQueries, double skew) {
        int n = population.length;
        double[] cdf = new double[n];
        double sum = 0.0;
        for (int i = 1; i <= n; i++) {
            sum += 1.0 / Math.pow(i, skew);
            cdf[i - 1] = sum;
        }
        for (int i = 0; i < n; i++) {
            cdf[i] /= sum;
        }

        int[] generatedQueries = new int[numQueries];
        Random rng = new Random(42);
        for (int i = 0; i < numQueries; i++) {
            double u = rng.nextDouble();
            int idx = Arrays.binarySearch(cdf, u);
            if (idx < 0) {
                idx = -(idx + 1);
            }
            if (idx >= n) {
                idx = n - 1;
            }
            generatedQueries[i] = population[idx];
        }

        return generatedQueries;
    }
}
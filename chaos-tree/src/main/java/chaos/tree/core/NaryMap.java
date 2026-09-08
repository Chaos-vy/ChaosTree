package chaos.tree.core;

import java.util.Iterator;
import java.util.Map;

/**
 * Interface defining the bulk-loading contract for all N-ary Maps
 * (e.g., BTreeMap, BPlusTreeMap).
 *
 * Provides O(N) operations to build and reconstruct the tree from 2D Matrices
 * and Iterators. Uses PECS (Producer Extends) for iterators to maximize
 * generic compatibility.
 *
 * <p>
 *     <strong>
 *         It must be noted that ChaosTree NaryTree are build in one sweep
 *         unlike textbook of DBMS, ChaosTree Build are different it uses a single sweep
 *         then rebalances top to down and down to up to fully,
 *         support correct buildup in O(N) time.
 *     </strong>
 *</p>
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface NaryMap<K, V> extends SearchTreeMap<K, V> {

    /**
     * Builds the tree from a sorted iterator of map entries.
     * <p>
     * <strong>WARNING: Sorted iterated must be passed. it was not designed
     * to check whether the iterator passed is sorted or not, It's your responsibility for
     * data feed. Having a check in build, decreases the speed</strong>
     * @param it an iterator providing entries in sorted key order (Producer Extends)
     * @param factor the fill factor for the nodes
     */
    void buildFromSorted(Iterator<? extends Map.Entry<? extends K, ? extends V>> it, float factor);

    /**
     * <strong>WARNING: THE TRUE DRAGON OF CHAOSTREE.</strong>
     * <p>
     * This is a high-performance, bare-metal array ingestion engine. It is hungry for raw
     * array throughput, but it is extremely unforgiving. Use with absolute precision.
     * <p>
     * <strong>THE FLAT MATRIX RULES:</strong>
     * <ul>
     * <li><strong>Matrix Layout:</strong> The {@code flatMatrix} parameter must be exactly 2D: {@code flatMatrix[0]} contains the keys, and {@code flatMatrix[1]} contains the values.</li>
     * <li><strong>Array Integrity:</strong> Neither array can be null, and both must be of exactly equal length.</li>
     * <li><strong>No Null Keys:</strong> A key must never be null. If a value is empty/missing, you must explicitly place {@code null} in the value array at that index.</li>
     * <li><strong>Strictly Sorted:</strong> The keys array <strong>MUST</strong> be strictly sorted according to the tree's comparator. Feeding unsorted data will instantly and silently corrupt the entire tree structure.</li>
     * <li><strong>Minimum Degree:</strong> This API relies on chunked array-copying and only services trees with a {@code degree >= 32}.</li>
     * </ul>
     * <p>
     * <strong>FILL FACTOR:</strong>
     * The {@code factor} determines node occupancy and has strict limits between {@code 0.5f} and {@code 1.0f}.
     * A factor of {@code 0.75f} is highly recommended for bulk loading. This packs the nodes densely while leaving
     * exactly enough buffer room to prevent future insertions from triggering massive, cascading split operations.
     * <p>
     *
     * @param flatMatrix A 2D array where {@code flatMatrix[0]} is the sorted keys and {@code flatMatrix[1]} is the mapped values.
     * @param factor     The node fill factor, restricted to the range {@code [0.5, 1.0]}.
     */
    void importFlatMatrix(Object[][] flatMatrix, float factor);

    /**
     * <strong>THE MASTER EXPORTER OF CHAOSTREE</strong>
     * <p>
     * Rips the entire internal state of the tree into a highly optimized, contiguous 2D array matrix
     * in strictly sorted order. This bypasses {@code Map.Entry} instantiation entirely by directly
     * flatMatrix in memory into flat arrays.
     * <p>
     * <strong>Matrix Layout:</strong>
     * <ul>
     * <li>{@code matrix[0]} &rarr; Array of strictly sorted keys.</li>
     * <li>{@code matrix[1]} &rarr; Array of corresponding values.</li>
     * </ul>
     * <p>
     * Unlike the ingestion engine, this extraction process is universally safe and natively
     * supports trees of <strong>all degrees</strong> with zero restrictions.
     * <p>
     * <strong>Note:</strong> If you intend to reconstruct a tree by feeding this matrix back
     * into the engine, you must review the strict limitations (such as {@code degree >= 32})
     * documented in {@link #importFlatMatrix}.
     *
     * @return A 2D {@code Object[][]} representing the flat matrix of keys and values.
     */
    Object[][] exportFlatMatrix();
}

package chaos.tree.core;

/**
 * Root contract for all tree data structures in the ChaosTree library.
 *
 * <p>This interface defines the minimal structural operations that every tree
 * implementation must expose, regardless of its balancing strategy, traversal
 * behavior, or node layout. Higher-level tree abstractions build on top of this
 * contract to add search, insertion, deletion, and traversal features.</p>
 *
 * <p>Implementations are not required to be thread-safe.</p>
 *
 * @author Vinay Singh
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ITree {
    /**
     * Returns the number of elements currently stored in this tree.
     *
     * @return the number of elements in the tree
     */
    int size();

    /**
     * Checks whether this tree contains no elements.
     *
     * @return {@code true} if the tree is empty, otherwise {@code false}
     */
    boolean isEmpty();

    /**
     * Removes all elements from this tree.
     */
    void clear();
}

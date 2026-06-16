# BinaryTree API Reference

The `BinaryTree<T>` interface inherits from `ISearchTree<T>`, `ITree`, and `Traversal<T>`. It provides a comprehensive set of operations for binary search trees.

## API Summary

### Core Operations
| Method                      | Description                                                      |
|-----------------------------|------------------------------------------------------------------|
| `void insert(T value)`      | Inserts a value into the tree. Throws if duplicate.              |
| `void delete(T value)`      | Deletes a value from the tree. Ignores if missing.               |
| `boolean contains(T value)` | Returns `true` if the value exists in the tree.                  |
| `int size()`                | Returns the total number of elements in the tree.                |
| `int height()`              | Returns the max depth of the tree (-1 if empty, 0 if only root). |
| `boolean isEmpty()`         | Returns `true` if the tree has no elements.                      |
| `void clear()`              | Removes all elements from the tree.                              |

### Positional Queries
| Method                   | Description                                            |
|--------------------------|--------------------------------------------------------|
| `T min()`                | Returns the minimum (leftmost) value.                  |
| `T max()`                | Returns the maximum (rightmost) value.                 |
| `T floor(T value)`       | Returns the largest value <= the given value.          |
| `T ceil(T value)`        | Returns the smallest value >= the given value.         |
| `T successor(T value)`   | Returns the smallest value strictly > the given value. |
| `T predecessor(T value)` | Returns the largest value strictly < the given value.  |
| `T kthSmallest(int k)`   | Returns the k-th smallest element (1-indexed).         |
| `T lca(T a, T b)`        | Returns the Lowest Common Ancestor of two values.      |

### Bulk Operations
| Method                                              | Description                                                      |
|-----------------------------------------------------|------------------------------------------------------------------|
| `void insertAll(Iterable<? extends T> values)`      | Inserts all values. Fails on first duplicate.                    |
| `void deleteAll(Iterable<? extends T> values)`      | Deletes all specified values in iteration order.                 |
| `boolean containsAll(Iterable<? extends T> values)` | Returns `true` if all values exist in the tree.                  |
| `void retainAll(Iterable<? extends T> values)`      | Retains only elements present in both the tree and the iterable. |
| `void mergeAll(Iterable<? extends T> values)`       | Inserts all values, silently ignoring any duplicates.            |

### Visualization
| Method              | Description                                                                                                                                                |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `String toString()` | Returns an ASCII-art visual representation of the tree structure, including algorithm-specific metadata (e.g., AVL heights, RBT colors, Treap priorities). |

### Traversal & Streaming
| Method                                     | Description                                             |
|--------------------------------------------|---------------------------------------------------------|
| `List<T> inorder()`                        | Returns elements sorted (Left-Node-Right).              |
| `List<T> preorder()`                       | Returns elements in structural order (Node-Left-Right). |
| `List<T> postorder()`                      | Returns elements bottom-up (Left-Right-Node).           |
| `List<T> levelorder()`                     | Returns elements top-down, level-by-level (BFS).        |
| `Iterator<T> iterator()`                   | Default in-order, fail-fast iterator.                   |
| `Iterator<T> iterator(TraversalType type)` | Fail-fast iterator using the specified traversal order. |
| `Stream<T> stream()`                       | Default sequential in-order Stream.                     |
| `Stream<T> stream(TraversalType type)`     | Sequential Stream using the specified traversal order.  |

## Practical Usage Example

Below is a practical example demonstrating how to initialize a tree via the `BinaryTree` interface and utilize the query and traversal APIs.

```java
import chaos.tree.binary.rbt.RBT;
import chaos.tree.core.searchtree.binary.BinaryTree;
import chaos.tree.traversal.TraversalType;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreeUsageExample {
    public static void main(String[] args) {
        // Initialize an RBT using the generic BinaryTree interface
        BinaryTree<Integer> rbt = new RBT<>();

        // Core Operations
        rbt.insert(50);
        rbt.insert(30);
        rbt.insert(70);
        rbt.insert(20);
        rbt.insert(40);
        
        boolean hasThirty = rbt.contains(30);
        rbt.delete(20);

        // Positional Queries
        Integer minimum = rbt.min();
        Integer maximum = rbt.max();
        Integer floorValue = rbt.floor(35);
        Integer ceilValue = rbt.ceil(35);
        Integer nextValue = rbt.successor(30);
        Integer secondSmallest = rbt.kthSmallest(2);
        Integer commonAncestor = rbt.lca(30, 70);

        // Traversal Lists
        List<Integer> sortedList = rbt.inorder();
        List<Integer> structuralList = rbt.preorder();

        // Custom Iterators
        // Note: Import TraversalType before specifying the order
        Iterator<Integer> levelIterator = rbt.iterator(TraversalType.LEVEL_ORDER);
        while (levelIterator.hasNext()) {
            Integer val = levelIterator.next();
            // Process val
        }

        // Java Streams Integration
        Stream<Integer> postOrderStream = rbt.stream(TraversalType.POSTORDER);
        List<Integer> filtered = postOrderStream
            .filter(v -> v > 35)
            .collect(Collectors.toList());
            
        // Bulk Operations
        rbt.mergeAll(List.of(80, 90, 100)); // Ignores duplicates if any

        // Visualization
        String asciiTree = rbt.toString();
        // Returns a multi-line ASCII string showing tree topology and
        // algorithm metadata (e.g., "50 (BLACK)" for RBT, height for AVL, etc.)
    }
}
```

# BinaryTree API Reference

The `BinaryTree<T>` interface inherits from `ISearchTree<T>`, `ITree`, and `Traversal<T>`. I built it to provide a massive, comprehensive set of operations for my binary search trees.

← Back to [README](README.md)

---

## API Summary

### Core Operations
| Method                      | Description                                                      | Exceptions Thrown                                          |
|-----------------------------|------------------------------------------------------------------|------------------------------------------------------------|
| `void insert(T value)`      | Inserts a value into the tree.                                   | `NullPointerException` (if null), `DuplicateNodeException` |
| `void delete(T value)`      | Deletes a value from the tree. Ignores if missing.               | `NullPointerException` (if null)                           |
| `boolean contains(T value)` | Returns `true` if the value exists in the tree.                  | `NullPointerException` (if null)                           |
| `int size()`                | Returns the total number of elements in the tree.                | *None*                                                     |
| `int height()`              | Returns the max depth of the tree (-1 if empty, 0 if only root). | *None*                                                     |
| `boolean isEmpty()`         | Returns `true` if the tree has no elements.                      | *None*                                                     |
| `void clear()`              | Removes all elements from the tree.                              | *None*                                                     |

### Positional Queries & Extraction
| Method                   | Description                                            | Exceptions Thrown                                  |
|--------------------------|--------------------------------------------------------|----------------------------------------------------|
| `T min()`                | Returns the minimum (leftmost) value.                  | `EmptyTreeException`                               |
| `T max()`                | Returns the maximum (rightmost) value.                 | `EmptyTreeException`                               |
| `T pollMin()`            | Retrieves and removes the minimum value.               | `EmptyTreeException`                               |
| `T pollMax()`            | Retrieves and removes the maximum value.               | `EmptyTreeException`                               |
| `T floor(T value)`       | Returns the largest value <= the given value.          | `NullPointerException` (if null)                   |
| `T ceil(T value)`        | Returns the smallest value >= the given value.         | `NullPointerException` (if null)                   |
| `T successor(T value)`   | Returns the smallest value strictly > the given value. | `NullPointerException` (if null)                   |
| `T predecessor(T value)` | Returns the largest value strictly < the given value.  | `NullPointerException` (if null)                   |
| `T kthSmallest(int k)`   | Returns the k-th smallest element (1-indexed).         | `IndexOutOfBoundsException` (if k < 1 or k > size) |
| `T lca(T a, T b)`        | Returns the Lowest Common Ancestor of two values.      | `NullPointerException` (if args null)              |

### Range Queries
| Method                                          | Description                                                                 | Exceptions Thrown                                                 |
|-------------------------------------------------|-----------------------------------------------------------------------------|-------------------------------------------------------------------|
| `List<T> range(T fromInclusive, T toExclusive)` | Returns a list of values within the half-open range `[from, to)`.           | `NullPointerException`, `IllegalArgumentException` (if from > to) |
| `Stream<T> rangeStream(T from, T to)`           | Returns a lazy sequential stream of values within the half-open range.      | `NullPointerException`, `IllegalArgumentException` (if from > to) |

### Bulk Operations
| Method                                              | Description                                                      | Exceptions Thrown                                              |
|-----------------------------------------------------|------------------------------------------------------------------|----------------------------------------------------------------|
| `void insertAll(Iterable<? extends T> values)`      | Inserts all values.                                              | `NullPointerException` (if any null), `DuplicateNodeException` |
| `void deleteAll(Iterable<? extends T> values)`      | Deletes all specified values in iteration order.                 | `NullPointerException` (if any null)                           |
| `boolean containsAll(Iterable<? extends T> values)` | Returns `true` if all values exist in the tree.                  | `NullPointerException` (if any null)                           |
| `void retainAll(Iterable<? extends T> values)`      | Retains only elements present in both the tree and the iterable. | `NullPointerException` (if iterable is null)                   |
| `void mergeAll(Iterable<? extends T> values)`       | Inserts all values, silently ignoring any duplicates.            | `NullPointerException` (if any null)                           |

### Visualization
| Method                             | Description                                                                                                                                                |
|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `String toString()`                | Returns an ASCII-art visual representation of the tree structure, including algorithm-specific metadata (e.g., AVL heights, RBT colors, Treap priorities). |
| `String toString(PrintStyle type)` | Returns a string representation using the specified `PrintStyle` (e.g., `ASCII` or `UNICODE` box-drawing).                                                 |

### Traversal & Streaming
| Method                                     | Description                                             | Exceptions Thrown                                         |
|--------------------------------------------|---------------------------------------------------------|-----------------------------------------------------------|
| `List<T> toList()`                         | Returns all elements in inorder (natural sorted) order. | *None*                                                    |
| `List<T> toList(TraversalType type)`       | Returns all elements in the specified traversal order.  | *None*                                                    |
| `List<T> inorder()`                        | Returns elements sorted (Left-Node-Right).              | *None*                                                    |
| `List<T> preorder()`                       | Returns elements in structural order (Node-Left-Right). | *None*                                                    |
| `List<T> postorder()`                      | Returns elements bottom-up (Left-Right-Node).           | *None*                                                    |
| `List<T> levelorder()`                     | Returns elements top-down, level-by-level (BFS).        | *None*                                                    |
| `Iterator<T> iterator()`                   | Default in-order, fail-fast iterator.                   | `ConcurrentModificationException` (on concurrent writes)  |
| `Iterator<T> iterator(TraversalType type)` | Fail-fast iterator using the specified traversal order. | `ConcurrentModificationException` (on concurrent writes)  |
| `Stream<T> stream()`                       | Default sequential in-order Stream.                     | `ConcurrentModificationException` (on concurrent writes)  |
| `Stream<T> stream(TraversalType type)`     | Sequential Stream using the specified traversal order.  | `ConcurrentModificationException` (on concurrent writes)  |

## Practical Usage Example

Below is a practical example demonstrating how to initialize a tree via the `BinaryTree` interface and utilize the query and traversal APIs. **You can copy and run**

```java
import chaos.tree.binary.RBT;
import chaos.tree.binary.BinaryTree;
import chaos.tree.core.searchtree.PrintStyle;
import chaos.tree.traversal.TraversalType;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class main {
    public static void main(String[] args) {
        // Initialize an RBT using the generic BinaryTree interface
        BinaryTree<Integer> rbt = new RBT<>();
        // Core Operations
        rbt.insert(50);
        rbt.insert(30);
        rbt.insert(70);
        rbt.insert(20);
        rbt.insert(40);
        System.out.println(rbt);
        System.out.println("Contains 30 :"+rbt.contains(30));
        rbt.delete(20);

        Integer minimum = rbt.min();
        Integer maximum = rbt.max();
        Integer floorValue = rbt.floor(35);
        Integer ceilValue = rbt.ceil(35);
        Integer nextValue = rbt.successor(30);
        Integer secondSmallest = rbt.kthSmallest(2);
        Integer commonAncestor = rbt.lca(30, 70);
        Integer extractedMin = rbt.pollMin(); // Retrieves and removes the minimum
        System.out.println("Floor value: "+floorValue+"\nCeil Value: "+ceilValue+"\nNext value: "+nextValue+
                "\nSecondSmallest: "+secondSmallest+"\nextracted min: "+ extractedMin+"\nlca = "+commonAncestor);
        // Range Queries
        System.out.println("Range: "+rbt.range(30,60));
        List<Integer> sortedList = rbt.toList(); // Modern method
        List<Integer> structuralList = rbt.toList(TraversalType.LEVEL_ORDER);
        System.out.println("Sorted: "+sortedList);
        System.out.println("StructuredList: "+structuralList);

        // Custom Iterators
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
        System.out.println(rbt.toString(PrintStyle.UNICODE));
    }
}
```

## Output:

```text
50(B)
+-- 30(B)
|   +-- 20(R)
|   \-- 40(R)
\-- 70(B)

Contains 30 :true
Floor value: 30
Ceil Value: 40
Next value: 40
SecondSmallest: 40
extracted min: 30
lca = 50
Range: [40, 50]
Sorted: [40, 50, 70]
StructuredList: [50, 40, 70]
50(B)
├── 40(B)
└── 80(R)
    ├── 70(B)
    └── 100(B)
        └── 90(R)


Process finished with exit code 0
```

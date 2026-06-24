# NaryTree API Reference

The `NaryTree<T>` interface inherits from `ISearchTree<T>` and `ITree`. We designed it to give you a comprehensive set of operations specifically optimized for multi-way search trees like B-Trees and B+ Trees.

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
| `int minDegree()`        | Returns the minimum degree (t) of the N-ary tree.      | *None*                                             |
| `int maxDegree()`        | Returns the maximum degree (2t) of the N-ary tree.     | *None*                                             |
| `T min()`                | Returns the minimum (leftmost) value.                  | `EmptyTreeException`                               |
| `T max()`                | Returns the maximum (rightmost) value.                 | `EmptyTreeException`                               |
| `T pollMin()`            | Retrieves and removes the minimum value.               | `EmptyTreeException`                               |
| `T pollMax()`            | Retrieves and removes the maximum value.               | `EmptyTreeException`                               |
| `T floor(T value)`       | Returns the largest value <= the given value.          | `NullPointerException` (if null)                   |
| `T ceil(T value)`        | Returns the smallest value >= the given value.         | `NullPointerException` (if null)                   |
| `T successor(T value)`   | Returns the smallest value strictly > the given value. | `NullPointerException` (if null)                   |
| `T predecessor(T value)` | Returns the largest value strictly < the given value.  | `NullPointerException` (if null)                   |
| `T kthSmallest(int k)`   | Returns the k-th smallest element (1-indexed).         | `IndexOutOfBoundsException` (if k < 1 or k > size) |

### Range Queries (N-ary Optimized)
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
| `String toString()`                | Returns an ASCII-art visual representation of the N-ary tree blocks and topology.                                                                          |
| `String toString(PrintStyle type)` | Returns a string representation using the specified `PrintStyle` (e.g., `ASCII` or `UNICODE` box-drawing).                                                 |

### Traversal & Streaming
| Method                   | Description                                             | Exceptions Thrown                                                                                     |
|--------------------------|---------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| `List<T> toList()`       | Returns all elements in inorder (natural sorted) order. | *None*                                                                                                |
| `Iterator<T> iterator()` | Default in-order, fail-fast iterator.                   | `ConcurrentModificationException` (ConcurrentModificationException (best-effort fail-fast detection)) |
| `Stream<T> stream()`     | Default sequential in-order Stream.                     | `ConcurrentModificationException` (on concurrent writes)                                              |

## Practical Usage Example

Below is a practical example demonstrating how to initialize an N-ary tree and utilize the query and range APIs. Range queries are specifically optimized in `BPlusTree` via horizontal leaf-chain traversal.

```java
import chaos.tree.core.searchtree.PrintStyle;
import chaos.tree.nary.BPlusTree;
import chaos.tree.nary.NaryTree;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
public class main {
    public static void main(String[] args) {
        // Initialize a B+ Tree with degree t=32
        NaryTree<Integer> bpt = new BPlusTree<>(32);
        // Core Operations
        bpt.insert(50);
        bpt.insert(30);
        bpt.insert(70);
        bpt.insert(60);
        bpt.insert(20);
        bpt.insert(40);
        System.out.println("Total size: "+bpt.size());
        System.out.println("Contains: "+bpt.contains(30));
        bpt.delete(20);
        // Positional Queries & Extraction
        System.out.println("Min element: "+bpt.min());
        System.out.println("Max Element: "+bpt.max());
        Integer floorValue = bpt.floor(35);
        Integer ceilValue = bpt.ceil(35);
        Integer nextValue = bpt.successor(30);
        Integer secondSmallest = bpt.kthSmallest(2);
        Integer extractedMin = bpt.pollMin(); // Retrieves and removes the minimum
        System.out.println("Floor value: "+floorValue+"\nCeil Value: "+ceilValue+"\nNext value: "+nextValue+
                "\nSecondSmallest: "+secondSmallest+"\nextracted min: "+ extractedMin);
        // Optimized Range Queries (Highly efficient in B+ Tree)
        List<Integer> midRange = bpt.range(30, 60); // [40, 50]
        System.out.println("Mid range: "+midRange);
        // Streaming massive datasets lazily without blowing up heap memory
        long count = bpt.rangeStream(20, 80)
                        .filter(v -> v % 2 == 0)
                        .count();

        System.out.println("count = "+count);
        // Traversal Lists
        List<Integer> sortedList = bpt.toList();
        // Custom Iterators
        Iterator<Integer> iterator = bpt.iterator();
        while (iterator.hasNext()) {
            Integer val = iterator.next();
            // Process val
        }
        System.out.println(bpt.toString(PrintStyle.UNICODE));
        for(Integer x: bpt){
            System.out.println(x);
        }
        // Java Streams Integration
        Stream<Integer> sortedStream = bpt.stream();
        List<Integer> filtered = sortedStream
                .filter(v -> v > 35)
                .collect(Collectors.toList());
        System.out.println("filtered: "+filtered);
        // Bulk Operations
        bpt.mergeAll(List.of(80, 90, 100)); // Ignores duplicates if any
        System.out.println(bpt);
    }
}


```

### Output:

```text
Total size: 6
Contains: true
Min element: 30
Max Element: 70
Floor value: 30
Ceil Value: 40
Next value: 40
SecondSmallest: 40
extracted min: 30
Mid range: [40, 50]
count = 4
└── [40, 50, 60, 70]

40
50
60
70
filtered: [40, 50, 60, 70]
\-- [40, 50, 60, 70, 80, 90, 100]


Process finished with exit code 0

```

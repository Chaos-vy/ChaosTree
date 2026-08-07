# Java `NavigableSet` Compatibility

Starting in version `1.1.0`, the `SearchTree` interface directly extends `java.util.NavigableSet`. This means **every tree implementation in ChaosTree (both Binary and N-ary) is a fully compliant Java Collection.** 

You can drop a ChaosTree instance directly into any existing system that expects a `Set`, `SortedSet`, or `NavigableSet` without any adapters!

## Supported APIs

All standard element retrieval, modification, and bulk operations are heavily optimized:

### Core Collection Operations
- `add(E e)` — Inserts a value.
- `remove(Object o)` — Deletes a value.
- `contains(Object o)` — Fast existence check.
- `size()`, `isEmpty()`, `clear()`, `toArray()`

### Bulk Operations
- `addAll(Collection<?> c)` — Sequentially inserts all elements.
- `containsAll(Collection<?> c)` — Returns `true` only if every element exists in the tree.
- `removeAll(Collection<?> c)` — Drops all elements present in the target collection.
- `retainAll(Collection<?> c)` — Keeps only the elements present in the target collection.

### Navigable Lookups
- `first()` / `last()` — Gets the minimum / maximum element.
- `lower(E e)` / `higher(E e)` — Gets the strict predecessor / successor.
- `floor(E e)` / `ceiling(E e)` — Gets the inclusive predecessor / successor.
- `pollFirst()` / `pollLast()` — Retrieves and removes the min / max element.

---

## Unsupported APIs (Live Views)

To guarantee maximum performance and maintain the "Mechanical Sympathy" philosophy, ChaosTree does not implement **live view** subsets. These methods explicitly throw `UnsupportedOperationException` to prevent accidental performance degradation:

- ❌ `subSet()`, `headSet()`, `tailSet()`
- ❌ `descendingSet()`, `descendingIterator()`

*(If you need to iterate over a subset of elements, use `tree.rangeStream(from, to)` instead!)*

---

## Example 1: Binary Family (`AVL`)

Because `AVL` implements `SearchTree`, it can be instantiated exactly like a `TreeSet`.

```java
import chaos.tree.binary.AVL;
import java.util.NavigableSet;
import java.util.List;

public class BinaryExample {
    public static void main(String[] args) {
        // Instantiate as a NavigableSet
        NavigableSet<Integer> tree = new AVL<>();
        
        // Use standard Collection bulk operations
        tree.addAll(List.of(10, 20, 30, 40, 50));
        
        // Use NavigableSet routing
        System.out.println(tree.first());     // 10
        System.out.println(tree.last());      // 50
        System.out.println(tree.lower(30));   // 20
        System.out.println(tree.ceiling(35)); // 40
        
        // Polling
        tree.pollFirst(); // Removes 10
    }
}
```

---

## Example 2: N-ary Family (`BPlusTree`)

The N-ary family (`BTree` and `BPlusTree`) also inherits full Collection compatibility. This is incredibly powerful for in-memory database indexing.

```java
import chaos.tree.nary.BPlusTree;
import java.util.Set;
import java.util.List;

public class NaryExample {
    public static void main(String[] args) {
        // Instantiate a B+ Tree with degree 1024 
        // minimum is 2, default is 32 maximum is your choice but it's int.
        Set<String> databaseIndex = new BPlusTree<>(1024);
        
        databaseIndex.addAll(List.of(
            "Apple", "Banana", "Cherry", "Date", "Elderberry"
        ));
        
        // Native Set validations
        if (databaseIndex.contains("Banana")) {
            System.out.println("Banana found!");
        }
        
        // Fast Collection intersections
        databaseIndex.retainAll(List.of("Apple", "Cherry", "Zebra"));
        
        // Tree now only contains ["Apple", "Cherry"]
        System.out.println(databaseIndex.size()); // 2
    }
}
```
ALL the above source code are compiled then put into consideration. For more rich API do Read
* [NaryFamily/README.md](NaryFamily/README.md)
* [BinaryFamily/README.md](BinaryFamily/README.md)
# N-ary Family

The N-ary Family is the high-density contiguous-memory module of ChaosTree — featuring two production-grade multi-way search tree implementations sharing a single generic API (`NaryTree<T extends Comparable<T>>`). Designed for high-density contiguous-memory storage and efficient cache utilization, every tree supports insertion, deletion, search, positional queries, $O(1)$ degree querying, range scanning, bulk operations (insertAll, deleteAll, retainAll), fail-fast iterators, Java Streams, and O(n) deep cloning. You pick the algorithm; the API stays the same.

---

## Trees at a Glance

| Tree          | Internal Nodes | Leaf Nodes | Range Query Speed | Best For                                                 |
|---------------|----------------|------------|:-----------------:|----------------------------------------------------------|
| **B-Tree**    | Keys & Data    | Data only  | $O(\log n + m)$   | General purpose multi-way searching, minimizing height   |
| **B+ Tree**   | Routing keys   | Data + Linked Leaf Chain | $O(\log_t n + m)$ | Range queries, massive scale iteration, contiguous memory|

---

## Documentation

| Document                                         | Content                                                                                         |
|--------------------------------------------------|-------------------------------------------------------------------------------------------------|
| **[README](README.md)**                          | Navigation hub — trees at a glance, quick start, constructors, and concurrency                  |
| **[API](API.md)**                                | Every method signature, parameters, return type, and usage example                              |
| **[Benchmark](Benchmark.md)**                    | JMH results detailing node saturation limits, degree scaling, and B+Tree range-scan performance |
| **[Complexity](Complexity.md)**                  | Time and space complexity per operation, detailing core algorithmic divergences                 |
| **[Limits](Limits.md)**                          | Extreme heap saturation limits, stack depth immunity, and density comparisons                   |
| **[DegreeOptimization](DegreeOptimization.md)**  | L1/L2 cache physics, degree selection guide, and GC allocation tradeoffs                        |
| **[Design-decision](../ADR/README.md)** | Architecture decisions — N-ary node paradox, `Object[]` usage, and API segregation              |

## Quick Start

### Insert, Search, Delete

```java
import chaos.tree.nary.BPlusTree;

// Degree (t) = 4 (Max 7 elements per node, Min 3)
BPlusTree<Integer> tree = new BPlusTree<>(4);
BPlusTree<Integer> tree0 = new BPlusTree<>(); //Default degree(t) = 32

tree.insert(30);
tree.insert(10);
tree.insert(50);

tree.contains(30);  // true
tree.contains(99);  // false

tree.delete(10);
tree.size();         // 2
```

### Bulk Insert from a Collection

```java
import chaos.tree.nary.BTree;
import java.util.List;

BTree<String> tree0 = new BTree<>(List.of("delta", "alpha", "charlie", "bravo"));// Default degree(t) = 32
tree.size();  // 4

BTree<String> tree = new BTree<>(128, List.of("delta", "alpha", "charlie", "bravo"));
tree.size();  // 4

```

### Positional Queries

```java
tree.min();              // "alpha"
tree.max();              // "delta"
tree.floor("cat");       // "bravo"   — greatest key less than "cat"
tree.ceil("cat");        // "charlie" — smallest key greater than "cat"
tree.successor("bravo"); // "charlie"
tree.kthSmallest(2);     // "bravo"
tree.minDegree();        // 32
tree.maxDegree();        // 64
```

### Range Queries & Streaming
B+ Trees heavily out-perform B-Trees here due to their $O(1)$ linked-leaf architecture.

```java
// Returns all elements between "alpha" and "delta" (inclusive-exclusive)
List<String> range = tree.range("alpha", "delta");

// Or use standard stream operations (default is sorted inorder)
long count = tree.stream()
                 .filter(v -> v.startsWith("c"))
                 .count();

// NaryTree specific range stream for massive datasets
tree.rangeStream("a", "d").forEach(System.out::println);
```

### Deep Clone

```java
import chaos.tree.nary.BPlusTree;

BPlusTree<Integer> original = new BPlusTree<>(4, List.of(30, 10, 50, 20, 40));
BPlusTree<Integer> clone = new BPlusTree<>(original);  // O(n) structural copy

clone.insert(99);
original.contains(99);  // false — fully independent
```

---

> For full API → [API.md](API.md)

---

## Constructor API

Every N-ary tree requires a degree (`t`) parameter at construction. 
* $t \ge 2$
* Minimum children per internal node = $t$
* Maximum children per node = $2t$
* Maximum keys per node = $2t - 1$

| Constructor                                | Description                                 |
|--------------------------------------------|---------------------------------------------|
| `new Tree<>(int degree)`                   | Empty tree with specified degree            |
| `new Tree<>(int degree, Iterable<T>)`      | Bulk-insert all elements in iteration order |
| `new Tree<>(Tree<T>)`                      | O(n) deep clone, inherits the original degree|

### Per-Tree Constructors

```java
// B-Tree
new BTree<>(16)
new BTree<>(16, List.of(3, 1, 2))
new BTree<>(existingBTree)

// B+ Tree
new BPlusTree<>(64)
new BPlusTree<>(64, List.of(3, 1, 2))
new BPlusTree<>(existingBPlusTree)
```

---

## Concurrency

The N-ary Family is not thread-safe by default. However, unlike the Binary Splay tree (which restructures on read), both `BTree` and `BPlusTree` execute pure, read-only traversals during `contains()`, `floor()`, `ceil()`, and `rangeStream()` operations. 

This means they are **100% safe to wrap in a `java.util.concurrent.locks.ReadWriteLock`**.
* **Read Lock:** Apply during `contains()`, `min()`, `max()`, `range()`, etc. Multiple threads can traverse the internal node arrays simultaneously without contention.
* **Write Lock:** Apply during `insert()`, `delete()`, `clear()`, `insertAll()`.

> Note: Iterators are fail-fast. If a write-lock thread mutates the tree while a read-lock thread holds an active Iterator/Stream, the iterator will throw a `ConcurrentModificationException`.

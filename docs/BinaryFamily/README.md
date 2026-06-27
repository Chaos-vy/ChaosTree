# Binary Family

The Binary Family is the core module of ChaosTree — five production-grade binary search tree implementations sharing a single generic API (`BinaryTree<T extends Comparable<T>>`). Every tree supports insertion, deletion, search, four traversal orders, positional queries (floor, ceil, successor, predecessor, kth-smallest, LCA), bulk operations (insertAll, deleteAll, retainAll, mergeAll), fail-fast iterators, Java Streams, and O(n) deep cloning via copy constructors. You pick the algorithm; the API stays the same.

---

## Trees at a Glance

| Tree      | Balance Strategy             |    Height Bound    | Best For                                                 |
|-----------|------------------------------|:------------------:|----------------------------------------------------------|
| **BST**   | None                         |     O(n) worst     | Baselines, education, pre-sorted-safe workloads          |
| **AVL**   | Strict height (ΔH ≤ 1)       |     1.44 log₂n     | Read-heavy workloads — fastest search                    |
| **RBT**   | Color invariant              |      2 log₂n       | Write-heavy workloads — fewer rotations on insert/delete |
| **Splay** | Move-to-root on access       | Amortized O(log n) | Temporal locality — hot keys migrate to root             |
| **Treap** | Random priorities (max-heap) | Expected O(log n)  | Simple probabilistic balance, reproducible via seed      |


---

## Documentation

| Document                                      | Content                                                                                     |
|-----------------------------------------------|---------------------------------------------------------------------------------------------|
| **[README](README.md)**                       | Navigation hub — trees at a glance, quick start, constructors, traversal and concurrency    |
| **[API](API.md)**                             | Every method signature, parameters, return type, and usage example                          |
| **[Benchmark](Benchmark.md)**                 | JMH results with L1 cache, branch miss, and instructions-per-op profiling                   |
| **[Complexity](Complexity.md)**               | Time and space complexity per operation across all 5 trees                                  |
| **[Design-decision](../ADR/README.md)**     | Architecture decisions — why CRTP, DeleteResult, Color enum, SearchResult, afterDelete hook |
| **[Limits](Limits.md)**                       | OOM and SOF boundaries from Chaos Engine stress tests per tree                              |
| **[Test](Test.md)**                           | 387 binary tests — all passed.                                                          |



## Quick Start

### Insert, Search, Delete

```java
import chaos.tree.binary.AVL;

AVL<Integer> tree = new AVL<>();

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
import chaos.tree.binary.RBT;
import java.util.List;

RBT<String> tree = new RBT<>(List.of("delta", "alpha", "charlie", "bravo"));
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
tree.lca("alpha", "charlie"); // "bravo" (or the actual LCA node value)
```

### Traversal & Streaming

```java
import chaos.tree.traversal.TraversalType;

List<Integer> sorted   = tree.inorder();
List<Integer> preorder = tree.preorder();

// Iterator with explicit traversal order
Iterator<Integer> it = tree.iterator(TraversalType.LEVEL_ORDER);

// Stream API
long count = tree.stream(TraversalType.POSTORDER)
                 .filter(v -> v > 20)
                 .count();
```

### Deep Clone

```java
import chaos.tree.binary.AVL;

AVL<Integer> original = new AVL<>(List.of(30, 10, 50, 20, 40));
AVL<Integer> clone = new AVL<>(original);  // O(n) structural copy

clone.insert(99);
original.contains(99);  // false — fully independent
```

---

> For full API → [API.md](API.md)

---

## Constructor API

Every tree provides three standard constructor forms:

| Constructor               | Description                                 |
|---------------------------|---------------------------------------------|
| `new Tree<>()`            | Empty tree                                  |
| `new Tree<>(Iterable<T>)` | Bulk-insert all elements in iteration order |
| `new Tree<>(Tree<T>)`     | O(n) deep clone via pre-order node copying  |

### Treap additional constructors

>Treap constructors are designed to provide absolute control over the tree's
probabilistic balancing engine. By exposing hooks for custom random seeds,
deterministic execution paths can be locked down for deterministic debugging and
regression testing. Furthermore, providing explicit bounds on the priority range
allows developers to restrict entropy constraints, optimizing CPU execution
cycles and maximizing L1/L2 cache efficiency based on known dataset limits.

| Constructor                | Description                                                                           |
|----------------------------|---------------------------------------------------------------------------------------|
| `new Treap<>()`            | Initialise the seed with random seed, `INTEGER.MAX_VALUE` as priority bound.          |
| `new Treap<>(long)`        | Initialise the seed with user defined seed, `INTEGER.MAX_VALUE` as priority bound.    |
| `new Treap<>(long, int)`   | Initialise the seed with user defined seed, user defined priority bound.              |
| `new Treap<>(Random, int)` | Initialise the Random with user defined Random instance, user defined priority bound. |

### Per-Tree Constructors

```java
// BST
new BST<>()
new BST<>(List.of(3, 1, 2))
new BST<>(existingBst)

// AVL
new AVL<>()
new AVL<>(List.of(3, 1, 2))
new AVL<>(existingAvl)

// RBT
new RBT<>()
new RBT<>(List.of(3, 1, 2))
new RBT<>(existingRbt)

// Splay
new Splay<>()
new Splay<>(List.of(3, 1, 2))
new Splay<>(existingSplay)

// Treap (additional seed/priority-bound constructors)
new Treap<>()                        // random seed, MAX_VALUE bound
new Treap<>(42L)                     // fixed seed for reproducibility
new Treap<>(42L, 5000)               // fixed seed + custom priority bound
new Treap<>(randomInstance, 5000)     // bring-your-own Random engine
new Treap<>(List.of(3, 1, 2))        // bulk insert, random seed
new Treap<>(existingTreap)           // deep clone, new Random engine, same priority bound
```

---

## Traversal

All trees default to **in-order** (sorted) traversal for `iterator()`, `stream()`, and `toString()`.

Four traversal orders are supported:

| Order       | Enum                        | Visit Pattern                   |
|-------------|-----------------------------|---------------------------------|
| In-order    | `TraversalType.INORDER`     | Left → Node → Right (sorted)    |
| Pre-order   | `TraversalType.PREORDER`    | Node → Left → Right (structure) |
| Post-order  | `TraversalType.POSTORDER`   | Left → Right → Node (bottom-up) |
| Level-order | `TraversalType.LEVEL_ORDER` | BFS top-down, left-to-right     |

```java
// Explicit traversal order
Iterator<Integer> it = tree.iterator(TraversalType.PREORDER);
Stream<Integer> s    = tree.stream(TraversalType.LEVEL_ORDER);

// Shorthand (defaults to INORDER)
for (int val : tree) { ... }
tree.stream().forEach(System.out::println);
```

**Fail-fast iterators:** All iterators track structural modifications via `modCount`. If the tree is modified after iterator creation (insert, delete, clear), the iterator throws `ConcurrentModificationException` on the next call to `next()`. This matches `java.util` collection behavior.

---

## Concurrency

BinaryFamily is not thread-safe by default. BST, AVL, RBT, and Treap can be made safe with an external `ReadWriteLock`.

**Splay cannot use `ReadWriteLock`.** `contains()` is a structural write by design. Removing splay-on-search breaks the amortized O(log n) guarantee which is Splay's entire value proposition. A read-only `contains()` gives you O(n) worst-case on adversarial access patterns with no amortized recovery. The `ReadWriteLock` benefit doesn't justify breaking the core guarantee.
**Concurrent Splay = clone-per-thread.**

I am currently planning to build a true, fully lock-free Concurrent RBT for v1.1.0 on a completely separate hierarchy. (I explicitly decided *not* to build a concurrent AVL or Splay—see my ADRs for the full breakdown on why).

> Full thread-safety analysis, external sync patterns, and verified stress
> test results → [Benchmark.md](Benchmark.md)


## Known Limits

- **BST** degrades to O(n) with sorted input and hits `StackOverflowError` at ~19,654 nodes on sorted/degenerate input. Random input survives far deeper. All balanced trees are immune — they are heap-limited, not stack-limited.
- **All trees** hit `OutOfMemoryError` at ~126M nodes on a 5.8 GB heap (12 GB DDR5 system). Per-node memory: 3 fields (BST/AVL/Treap) or 4 fields (RBT/Splay).

> Full stress test data → [limits.md](Limits.md)

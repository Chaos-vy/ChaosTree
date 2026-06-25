# Complexity Guarantees

We hate hidden performance traps. This document breaks down the exact time and space complexities for every single operation supported by the Binary Family, so you know exactly what you're paying for when you call our APIs.

← Back to [README](README.md)

---

## Notation

| Symbol | Meaning                                  |
|--------|------------------------------------------|
| **n**  | Number of elements in the tree           |
| **h**  | Height of the tree                       |
| **k**  | Number of elements in the input iterable |
| **m**  | Number of elements in the queried range/retain set |

### The Height Factor (`h`)

| Tree      |   Worst-Case h    | Notes                                                      |
|-----------|:-----------------:|------------------------------------------------------------|
| **BST**   |         n         | Sorted input → degenerate chain                            |
| **AVL**   |    1.44 log₂n     | Strict balance factor ≤ 1                                  |
| **RBT**   |    2 log₂(n+1)    | Longest path ≤ 2× shortest path                            |
| **Splay** |         n         | Single-op worst case; amortized O(log n) over any sequence |
| **Treap** | O(log n) expected | Probabilistic; depends on random priorities                |

---

## Core Operations

| Operation     | BST  |   AVL    |   RBT    |       Splay        |       Treap       |
|---------------|:----:|:--------:|:--------:|:------------------:|:-----------------:|
| `insert(T)`   | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `delete(T)`   | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `contains(T)` | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |

> **BST note:** h = O(n) with sorted input, O(log n) with random input.
>
> **Splay note:** `contains()` restructures the tree (splays accessed node to root). Worst-case single operation is O(n), but any sequence of m operations is O(m log n).

---

## Positional Queries & Extraction

All positional queries perform a standard vertical traversal from the root down to the target node or leaf.

| Operation        | BST  |   AVL    |   RBT    |       Splay        |       Treap       |
|------------------|:----:|:--------:|:--------:|:------------------:|:-----------------:|
| `min()`          | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `max()`          | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `pollMin()`      | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `pollMax()`      | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `floor(T)`       | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `ceil(T)`        | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `successor(T)`   | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `predecessor(T)` | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `kthSmallest(k)` | O(n) |   O(n)   |   O(n)   |        O(n)        |       O(n)        |
| `lca(T, T)`      | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `height()`       | O(n) |   O(1)   |   O(n)   |        O(n)        |       O(n)        |

> **Why is `kthSmallest` O(n)?** 
> We intentionally perform a brute-force in-order traversal that stops at the k-th element. The alternative would be storing augmented rank data (subtree sizes) on every single node, which would massively bloat our memory footprint. We chose memory density over O(log n) rank queries.
>
> **Why is `height()` O(n)?** 
> The public `height()` method recursively computes the depth across the entire structure. 

---

## Range Queries

| Operation       |  BST   |      AVL     |      RBT     |        Splay         |        Treap         |
|-----------------|:------:|:------------:|:------------:|:--------------------:|:--------------------:|
| `range(T, T)`   | O(h+m) | O(log n + m) | O(log n + m) | O(log n + m) amortized | O(log n + m) expected |
| `rangeStream()` | O(h)   |   O(log n)   |   O(log n)   | O(log n) amortized   | O(log n) expected    |

> `rangeStream()` cost shown is the initialization overhead to locate the starting element. Iterating the stream subsequently costs an amortized O(1) per element.

---

## Bulk Operations

| Operation               |      Time       | Notes                                                                    |
|-------------------------|:---------------:|--------------------------------------------------------------------------|
| `insertAll(Iterable)`   |  O(k × insert)  | Inserts each element. Throws `DuplicateNodeException` on first duplicate |
| `deleteAll(Iterable)`   |  O(k × delete)  | Deletes each element. Missing values silently ignored                    |
| `containsAll(Iterable)` | O(k × contains) | Short-circuits on first miss                                             |
| `retainAll(Iterable)`   |   O(n log n)    | Snapshots tree, deletes all elements not in the retain set               |
| `mergeAll(Iterable)`    |  O(k × insert)  | Like `insertAll` but silently skips duplicates                           |

---

## Construction

| Constructor            |     Time      |  Space   | Notes                                                 |
|------------------------|:-------------:|:--------:|-------------------------------------------------------|
| `new Tree<>()`         |     O(1)      |   O(1)   | Empty tree                                            |
| `new Tree<>(Iterable)` | O(k × insert) |   O(k)   | Bulk insert via `insertAll`                           |
| `new Tree<>(Tree)`     |   **O(n)**    | **O(n)** | Pre-order structural clone — bypasses insert pipeline |

> **Why is the copy constructor drastically faster?** 
> When you construct a new tree by passing in an existing `Tree`, we bypass the insertion pipeline entirely. We execute a single pre-order traversal and physically clone the nodes, blindly copying their values, colors, and balance factors. Zero comparisons, zero rotations, and zero rebalancing. It is the fastest possible way to duplicate a tree.

---

## Traversal & Visualization

| Operation                 |    Time    |    Space     | Notes                                              |
|---------------------------|:----------:|:------------:|----------------------------------------------------|
| `toList()`                |    O(n)    |     O(n)     | Returns `List<T>` containing all elements          |
| `toList(TraversalType)`   |    O(n)    |     O(n)     | Returns `List<T>` in specified traversal order     |
| `inorder()`               |    O(n)    |     O(n)     | Returns `List<T>` in sorted order                  |
| `preorder()`              |    O(n)    |     O(n)     | Returns `List<T>` in pre-order                     |
| `postorder()`             |    O(n)    |     O(n)     | Returns `List<T>` in post-order                    |
| `levelorder()`            |    O(n)    |     O(n)     | Returns `List<T>` via BFS                          |
| `iterator()`              | O(n) total |     O(h)     | Lazy; each `next()` is amortized O(1)              |
| `iterator(TraversalType)` | O(n) total | O(h) or O(n) | LEVEL_ORDER uses O(n) queue; others use O(h) stack |
| `stream()`                |    O(n)    |     O(h)     | Backed by iterator; sequential only                |
| `toString()`              |    O(n)    |     O(n)     | ASCII tree representation                          |

> **Fail-fast guarantee:** Iterators track `modCount`. Any structural modification (insert, delete, clear) after iterator creation causes `ConcurrentModificationException` on the next `next()` call.

---

## Space Complexity

### Per-Node Memory

| Tree      | Node Class  |                  Fields                   | Overhead vs BST |
|-----------|-------------|:-----------------------------------------:|:---------------:|
| **BST**   | `BSTNode`   |            value, left, right             |    baseline     |
| **AVL**   | `AVLNode`   |      value, left, right, **height**       |     +1 int      |
| **Treap** | `TreapNode` |     value, left, right, **priority**      |     +1 int      |
| **RBT**   | `RBTNode`   | value, left, right, **parent**, **color** | +1 ref, +1 enum |
| **Splay** | `SplayNode` |      value, left, right, **parent**       |     +1 ref      |

### Total Tree Space
| Tree  | Space | Notes                              |
| ----- | :---: | ---------------------------------- |
| BST   |  O(n) | 3 fields/node                      |
| AVL   |  O(n) | 4 fields/node (`height`)           |
| Treap |  O(n) | 4 fields/node (`priority`)         |
| RBT   |  O(n) | 5 fields/node (`parent` + `color`) |
| Splay |  O(n) | 4 fields/node (`parent`)           |

> **The Cost of Metadata at Scale:** 
> When you're managing 100 million nodes, the extra parent pointer required by Red-Black and Splay trees instantly consumes an additional ~800 MB of heap space (assuming an 8-byte reference on a standard 64-bit JVM). This is exactly why we decoupled our node hierarchy and didn't force parent pointers onto the BST, AVL, or Treap.

---

## Rebalancing Cost

Hidden constant factors behind the O(log n) — what each tree does after insert/delete:

| Tree      | After Insert                                                     | After Delete                          |
|-----------|------------------------------------------------------------------|---------------------------------------|
| **BST**   | No Rebalance                                                     | No Rebalance                          |
| **AVL**   | Walk up to root, ≤ 2 rotations                                   | Walk up to root, ≤ O(log n) rotations |
| **RBT**   | ≤ 2 rotations + O(log n) color flips                             | ≤ 3 rotations + O(log n) color flips  |
| **Splay** | Splay inserted node to root (O(log n) rotations)                 | Splay parent of deleted node to root  |
| **Treap** | Rotate up until heap property restored (expected O(1) rotations) | Rotate down to leaf, then remove      |

> **The Real-World AVL vs RBT Tradeoff:**
> Theoretically, both guarantee O(log n). But mechanically? AVL trees execute a maximum of 2 rotations on insert, but they are forced to walk back up the tree updating integer heights on every ancestor. Red-Black trees also max out at 2 rotations and do some bit-flipping for colors, but entirely avoid the height-updating overhead. Our JMH benchmarks prove that in practice, AVL is marginally faster for lookups, while RBT dominates in write-heavy workloads.

---

## Summary Table

| Operation   | BST Worst | Balanced Worst | Splay Amortized | Treap Expected |
|-------------|:---------:|:--------------:|:---------------:|:--------------:|
| `Search`    |   O(n)    |    O(log n)    |    O(log n)     |    O(log n)    |
| `Insert`    |   O(n)    |    O(log n)    |    O(log n)     |    O(log n)    |
| `Delete`    |   O(n)    |    O(log n)    |    O(log n)     |    O(log n)    |
| `Min/Max`   |   O(n)    |    O(log n)    |    O(log n)     |    O(log n)    |
| `Clone`     |   O(n)    |      O(n)      |      O(n)       |      O(n)      |
| `Traversal` |   O(n)    |      O(n)      |      O(n)       |      O(n)      |
| `Space`     |   O(n)    |      O(n)      |      O(n)       |      O(n)      |

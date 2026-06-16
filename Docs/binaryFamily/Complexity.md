# Complexity

Time and space complexity for every operation in the Binary Family.

← Back to [README](README.md)

---

## Notation

| Symbol | Meaning                                  |
|--------|------------------------------------------|
| **n**  | Number of elements in the tree           |
| **h**  | Height of the tree                       |
| **k**  | Number of elements in the input iterable |
| **m**  | Number of elements in the retain set     |

### Height bounds per tree:

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

## Positional Queries

All positional queries traverse from root to the target node or leaf.

| Operation        | BST  |   AVL    |   RBT    |       Splay        |       Treap       |
|------------------|:----:|:--------:|:--------:|:------------------:|:-----------------:|
| `min()`          | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `max()`          | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `floor(T)`       | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `ceil(T)`        | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `successor(T)`   | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `predecessor(T)` | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `kthSmallest(k)` | O(n) |   O(n)   |   O(n)   |        O(n)        |       O(n)        |
| `lca(T, T)`      | O(h) | O(log n) | O(log n) | O(log n) amortized | O(log n) expected |
| `height()`       | O(n) |   O(n)   |   O(n)   |        O(n)        |       O(n)        |

> **`kthSmallest`** performs an in-order traversal stopping at the k-th element. No augmented rank data is stored, so this is O(n) worst case.
>
> **`height()`** recursively computes height from every node. AVL nodes cache their own height internally, but the public `height()` method recomputes from root for all trees.

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

> **clone is faster than iterable construction:** The copy constructor calls `cloneStructure()` which does a single pre-order traversal, copying each node's value and metadata (height, color, priority) directly. No comparisons, no rotations, no rebalancing. The iterable constructor must insert each element through the full insertion pipeline including BST traversal + rebalancing.
>
> For parent-pointer trees (RBT, Splay), `cloneStructure` performs an additional O(n) parent-wiring pass.

---

## Traversal

| Operation                 |    Time    |    Space     | Notes                                              |
|---------------------------|:----------:|:------------:|----------------------------------------------------|
| `inorder()`               |    O(n)    |     O(n)     | Returns `List<T>` in sorted order                  |
| `preorder()`              |    O(n)    |     O(n)     | Returns `List<T>` in pre-order                     |
| `postorder()`             |    O(n)    |     O(n)     | Returns `List<T>` in post-order                    |
| `levelorder()`            |    O(n)    |     O(n)     | Returns `List<T>` via BFS                          |
| `iterator()`              | O(n) total |     O(h)     | Lazy; each `next()` is amortized O(1)              |
| `iterator(TraversalType)` | O(n) total | O(h) or O(n) | LEVEL_ORDER uses O(n) queue; others use O(h) stack |
| `stream()`                |    O(n)    |     O(h)     | Backed by iterator; sequential only                |

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

| Tree  | Space | Notes                    |
|-------|:-----:|--------------------------|
| BST   | O(n)  | 3 fields/node            |
| AVL   | O(n)  | 4 fields/node            |
| Treap | O(n)  | 4 fields/node            |
| RBT   | O(n)  | 5 fields/node (heaviest) |
| Splay | O(n)  | 4 fields/node            |

> At scale: with 100M nodes, the extra parent pointer in RBT/Splay adds ~800 MB (8 bytes × 100M) on a 64-bit JVM with compressed oops disabled.

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

> **AVL vs RBT insert tradeoff:** AVL does ≤ 2 rotations but must update height on every ancestor. RBT does ≤ 2 rotations with O(log n) color flips but no height tracking. In practice, AVL insert is marginally faster; RBT delete is marginally faster.

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

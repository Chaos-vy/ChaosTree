# Complexity

Here is the time and space complexity for every single operation in my N-ary Family (`BTree`, `BPlusTree`).

← Back to [README](README.md)

---

## Notation

| Symbol | Meaning                                  |
|--------|------------------------------------------|
| **n**  | Number of elements in the tree           |
| **t**  | Degree of the tree (minimum branch factor)|
| **h**  | Height of the tree: $O(\log_t n)$        |
| **k**  | Number of elements in the input iterable |
| **m**  | Number of elements in the queried range  |

### Parameterized Node Capacity
In an N-ary tree, every node contains an array of elements. 
* **Min Elements/Node:** $t - 1$ (except root)
* **Max Elements/Node:** $2t - 1$
* **Max Children/Node:** $2t$

*Note: All complexities shown map the cost of disk/memory block fetches combined with the CPU cost of manipulating the arrays inside each block.*

---

## Core Operations

| Operation     | B-Tree | B+ Tree | Notes |
|---------------|:------:|:-------:|-------|
| `insert(T)`   | $O(t \log_t n)$ | $O(t \log_t n)$ | Searching a node is $O(\log t)$, but shifting array elements costs $O(t)$ per layer. |
| `delete(T)`   | $O(t \log_t n)$ | $O(t \log_t n)$ | Merging or shifting borrows incurs array copy overhead of $O(t)$. |
| `contains(T)` | $O(\log n)$ | $O(\log n)$ | Traversing $h$ nodes, doing binary search inside each node: $\log_t n \times \log t = \log n$. |

### Core Algorithmic Divergences
Even though they share the same Big-O notation, the way these two trees actually execute algorithms is fundamentally different:
* **Early Termination (`contains`)**: My **B-Tree** can find a value in an internal routing node and return `true` immediately without ever reaching a leaf. My **B+ Tree**, however, MUST traverse all the way down to the leaf layer every single time, because its internal nodes only hold routing copies of the keys.
* **Deletion Logic (`delete`)**: If my **B-Tree** deletes a key from an internal node, it has to execute a complex algo to find the predecessor in the left subtree, swap the keys, and then delete the leaf. The **B+ Tree** is much simpler: it always deletes directly from the leaf layer.

> **Why $O(t \log_t n)$ for mutation?** While finding the insertion index is fast via binary search, inserting into an array of size $2t$ requires shifting elements memory-right via `System.arraycopy`, taking $O(t)$ time per modified node block.

---

## Positional Queries & Extraction

| Operation        | B-Tree | B+ Tree |
|------------------|:------:|:-------:|
| `minDegree()`    | $O(1)$ | $O(1)$ |
| `maxDegree()`    | $O(1)$ | $O(1)$ |
| `min()`          | $O(\log_t n)$ | **$O(1)$** |
| `max()`          | $O(\log_t n)$ | $O(\log_t n)$ |
| `pollMin()`      | $O(t \log_t n)$ | $O(t \log_t n)$ |
| `pollMax()`      | $O(t \log_t n)$ | $O(t \log_t n)$ |
| `floor(T)`       | $O(\log n)$ | $O(\log n)$ |
| `ceil(T)`        | $O(\log n)$ | $O(\log n)$ |
| `successor(T)`   | $O(\log n)$ | $O(\log n)$ |
| `predecessor(T)` | $O(\log n)$ | $O(\log n)$ |
| `kthSmallest(k)` | $O(n)$ | $O(n)$ |
| `height()`       | $O(\log_t n)$ | $O(\log_t n)$ |

> **Note on `height()`**: Multi-way trees grow strictly bottom-up, meaning all leaves are at the exact same depth. Therefore, computing the height only requires traversing down the leftmost spine of the tree, giving me a fast $O(\log_t n)$ time complexity rather than the $O(n)$ full-traversal required by the Binary family.

---

## Range Queries

Range queries are where `BPlusTree` dominates because of its contiguous leaf chain.

| Operation       | B-Tree | B+ Tree |
|-----------------|:------:|:-------:|
| `range(T, T)`   | $O(\log n + m)$ | $O(\log_t n + m)$ |
| `rangeStream()` | $O(\log n)$ | $O(\log_t n)$ |

> **B-Tree Range:** Must traverse up and down internal nodes to satisfy the in-order bounds, resulting in standard tree search overheads during iteration.
>
> **B+ Tree Range:** Drops to the leaf layer in $O(\log_t n)$, then simply iterates horizontally across the `next` pointers. Drops to the leaf layer in O(log_t n), then traverses horizontally through next pointers in O(m).

---

## Bulk Operations

| Operation               | Time | Notes |
|-------------------------|:----:|-------|
| `insertAll(Iterable)`   | $O(k \times t \log_t n)$ | Inserts each element individually. |
| `deleteAll(Iterable)`   | $O(k \times t \log_t n)$ | Deletes each element individually. |
| `containsAll(Iterable)` | $O(k \log n)$ | Short-circuits on first miss. |
| `retainAll(Iterable)`   | $O(n \log n)$ | In-memory filtering and deletion. |
| `mergeAll(Iterable)`    | $O(k \times t \log_t n)$ | Silently skips duplicates. |

---

## Traversal & Visualization

| Operation                 | Time | Space | Notes |
|---------------------------|:----:|:-----:|-------|
| `toList()`                | $O(n)$ | $O(n)$ | Materializes all elements. |
| `iterator()`              | $O(n)$ | $O(\log_t n)$ or $O(1)$ | **B-Tree:** Uses $O(\log_t n)$ stack space for depth-first traversal. <br> **B+ Tree:** Uses $O(1)$ space by simply walking the horizontal `next` leaf pointers. |
| `stream()`                | $O(n)$ | $O(\log_t n)$ or $O(1)$ | Backed by iterator. Space matches iterator complexity. |
| `toString()`              | $O(n)$ | $O(n)$ | Allocates a multi-line string for block topology. |

---

## Space Complexity

### Memory Overhead Per Block

| Node Class        | Fields                                                                                              |
|-------------------|-----------------------------------------------------------------------------------------------------|
| **BTreeNode**     | `int keyCount`, `Object[] keys`, `BTreeNode[] children`, `boolean isLeaf`                           |
| **BPlusTreeNode** | `int keyCount`, `Object[] keys`, `BPlusTreeNode[] children`, `boolean isLeaf`, `BPlusTreeNode next` |

### Space Invariants

| Characteristic | Space | Notes |
|----------------|:-----:|-------|
| Memory Layout  | $O(n)$ | Elements are packed contiguously into blocks, improving spatial locality and reducing pointer traversal overhead. |
| Wasted Space   | $O(n)$ | Nodes may be up to 50% empty due to $t-1$ minimum load factor. |
| Pointer Overhead | $O(n/t)$ | Significantly lower pointer overhead than Binary trees. A degree 64 tree stores 127 elements with only 128 pointers, whereas an AVL tree requires 254 pointers for the same data. |

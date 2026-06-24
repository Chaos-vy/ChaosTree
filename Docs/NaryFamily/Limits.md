## Extreme Memory Limits & Saturation Profiling

To figure out the absolute physical limits of our N-ary framework, we put our contiguous-memory data structures (`BTree`, `BPlusTree`) through bare-metal heap saturation tests against a strictly capped JVM.

← Back to [README](README.md)

---
## Hardware & Environment
* **CPU:** i5 13450HX 16 Cores (Intel Hybrid Architecture)
* **RAM:** 24 GB DDR5 (4800 MT/s)
* **JVM Allocation:** 16,000 MB Maximum Heap (`-Xmx`)
* **JVM Flags:** `-XX:+UseCompressedOops`
---

## 1. Absolute Capacity Limits & JVM Interoperability

The N-ary collections utilize a standard 32-bit signed `int` for internal `size` tracking, establishing a hard structural capacity ceiling of **$2,147,483,647$ elements** ($2^{31}-1$).

**Why do we use an `int` instead of a `long`?**
This limitation is driven by JVM array boundaries. The standard Java Collection framework is hardcoded to use 32-bit array buffers. While we could technically use a `long` to track larger capacities internally, the moment a user calls `stream().collect()` or `toList()`, the JVM must allocate a contiguous backing array. And since the JVM strictly limits array indices to 32-bit integers, we would crash immediately. Maintaining a 32-bit `volatile int` ensures we stay fully interoperable with standard Java limits.

---

## 2. Stack Depth vs. Heap Limits

Unlike Binary Search Trees (BST), which can suffer catastrophic `StackOverflowError` crashes if they get too deep, our N-ary family is mathematically immune to stack overflow.

* **Stack Depth Immunity:** A B-Tree of order 100 containing 1 billion elements will have a maximum tree height of just 5! All our traversal and mutation logic stays incredibly shallow.
* **Heap Bounds Only:** Because the tree height is so shallow, these trees are solely limited by your available JVM Heap memory. You will never see a `StackOverflowError` from them.

---

## 3. Thread Safety Limits

We designed the N-ary Family to be **not natively thread-safe**, but they are structurally built to support incredibly high-throughput concurrency if you wrap them in an external `ReadWriteLock`.

* **No Structural Read-Mutation:** Unlike the Binary `Splay` tree, neither `BTree` nor `BPlusTree` restructure during read queries (`contains()`, `rangeStream()`).
* **High Read Concurrency:** Multiple threads holding a read-lock can simultaneously traverse the internal `Object[]` arrays without contention, achieving near-linear read scaling on multi-core systems.
* **Write Contention:** Because we have to do $O(t)$ array shifting during insertions (via `System.arraycopy`), write locks hold the monitor slightly longer than they do for simple binary pointer rewires. If you have an extreme, hyper-aggressive write-heavy workload, our Binary `RBT` might actually slightly outperform our N-ary writes just because it doesn't have to shift arrays.

---

## 4. Iterator & Modification Limits

The ChaosTree iterators and stream spliterators are strictly **fail-fast**.

* **ConcurrentModificationException:** All topological modifications (insertions, deletions) increment a `long modCount` (Note: `modCount` is `long` to prevent silent overflow wraps at 2.1 billion ops). If an active iterator detects a mismatch between its expected state and the tree's global state, it will instantly throw a `ConcurrentModificationException`.
* **Range Stream Dominance:** The `BPlusTree` range iterator uses a purely horizontal $O(1)$ linked-list traversal, bypassing internal tree routing entirely.

---

## 5. The N-ary Density Advantage

Through micro-architectural CPU testing (`perf`) and heap saturation testing, the N-ary memory architecture (`children = null` for leaves and exact-capacity `Object[]`) was benchmarked directly against the best-in-class Binary Family tree (the Red-Black Tree).

| Data Structure  | Max Elements Retained (16 GB Heap) | Storage Architecture         | Failure Mode                 |
|-----------------|------------------------------------|------------------------------|------------------------------|
| **`ArrayList`** | **798,381,092**               | Contiguous Object[] (1.5x growth)| `OutOfMemoryError` (Heap) |
| **ChaosTree B+ Tree**| **696,602,738**                    | `Object[]` arrays (Order 100)| `OutOfMemoryError` (Heap)    |
| **ChaosTree RBT** | 357,000,000                        | 4-pointer Nodes              | `OutOfMemoryError` (Heap)    |

### Engineering Takeaways
1. **B+Tree vs ArrayList:** `ArrayList` reaches 798M elements because it is a flat, single contiguous array with zero per-entry object overhead. The B+Tree packs 696M elements in the exact same heap — only ~13% fewer — despite carrying full tree structure, internal routing nodes, and leaf-chain linked-list pointers. This proves the N-ary engine is architecturally comparable to Java's most memory-efficient core collection.
2. **B+Tree vs RBT:** The B+Tree stores nearly **double** the elements of an RBT (696M vs 357M) in the same heap. An order-100 B-Tree stores up to 99 elements sequentially with only 100 pointers, whereas an RBT requires a separate JVM object (with a 16-byte object header) plus pointer fields for every single element.
3. **Zero ArrayList Waste:** By explicitly rejecting `java.util.ArrayList` (which grows by 1.5x and wastes spare capacity), the ChaosTree N-ary nodes maintain absolute, deterministic heap boundaries, culminating in industrial-grade 696-million element saturation limits.

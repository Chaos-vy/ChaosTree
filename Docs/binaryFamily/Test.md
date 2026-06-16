# ChaosTree Test Architecture & Coverage

This document details the testing strategy, architecture, and coverage of the ChaosTree binary family. It is designed to provide contributors with a complete understanding of what guarantees the test suite provides, how those guarantees are enforced, and what scenarios are explicitly out of scope.

← Back to [README](README.md)

---

## 1. Overview

The ChaosTree test suite validates five distinct tree algorithms (BST, AVL, Red-Black, Splay, Treap) through a unified contractual testing framework. 

* **Test Count:** 314 active unit tests.
* **Code Size:** 

| Type      | LOC   |
|-----------|-------|
| Test      | 727   |
| Code      | 1.576 |
| Benchmark | 697   |
* **Test-to-Production Ratio:** 0.90 : 1

Collectively, the suite proves that regardless of the underlying balancing algorithm, every tree in the `BinaryFamily` perfectly adheres to the strict contract of a generic search tree. It verifies structural integrity, traversal correctness, mathematical invariants, fail-fast mechanics, and bulk-mutation stability under both isolated and concurrent loads.

---

## 2. Test Architecture

The suite relies heavily on inheritance to enforce contractual guarantees while avoiding test duplication.

### BinaryTreeContractTest

Instead of writing identical insertion and deletion tests for five different trees, all shared behavior is centralized in an abstract `BinaryTreeContractTest`. Every concrete tree test class (e.g., `AVLTest`, `SplayTest`) extends this base class and supplies its specific tree instance via a factory method. This ensures that every tree implementation inherits the exact same rigorous behavioral guarantees for core operations, iterations, and streams.

### StableStructureContractTest

Certain operations assume a tree's structure remains immutable during read-only queries. `StableStructureContractTest` extends `BinaryTreeContractTest` to test queries like Least Common Ancestor (LCA). Splay trees and Treaps are explicitly excluded from this suite—Splay because its `contains()` operation structurally mutates the tree on every access, and Treap because its randomized priorities cause structural variations that make static hierarchy queries like LCA functionally different than standard deterministic trees.

### ConcurrentBinaryTreeTest

Concurrency testing requires a completely different lifecycle. Contract tests operate in a vacuum to prove deterministic logic; concurrent tests prove thread-safety under chaos. `ConcurrentBinaryTreeTest` operates its own suite with heavy thread pools, latches, and race-condition triggers. 

* **BST Excluded:** It has no balancing invariant to corrupt.
* **Splay Excluded:** Its read operations perform structural writes, meaning `ReadWriteLock` separation is architecturally invalid.

---

## 3. Contract Test Coverage

The following behaviors are comprehensively validated at the `BinaryTreeContractTest` level for every tree in the family:

| Category                        | What's Tested                                                                                                                                                                                                                               |
|:--------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Empty Tree**                  | Evaluates `size()`, `isEmpty()`, `contains()`, `min()`, and `max()`. Proves that calling `delete()` on an empty tree behaves as a safe no-op.                                                                                               |
| **Insert**                      | Single insertions, bulk insertions from iterables, verification that duplicate insertions throw `DuplicateNodeException`, and that inserting a `null` value fast-fails.                                                                     |
| **Delete**                      | Deletion of leaf nodes, nodes with one child, nodes with two children, and the root node. Proves non-existing deletions are safely ignored.                                                                                                 |
| **Min/Max**                     | Verifies retrieval of correct extremes and proves that querying extremes on an empty tree throws `EmptyTreeException`.                                                                                                                      |
| **Successor/Predecessor**       | Validates that navigational queries return the correct sorted adjacent elements based on structural traversal.                                                                                                                              |
| **Floor/Ceil**                  | Ensures queries return exact matches when present, and nearest candidates when the exact match is missing.                                                                                                                                  |
| **Kth Smallest**                | Validates retrieval of the first and last elements, and proves that 0-index or out-of-bounds queries throw appropriate exceptions.                                                                                                          |
| **LCA**                         | Validates Lowest Common Ancestor logic for nodes in the same subtree, nodes splitting across the root, and when one query node is the ancestor of the other (Restricted to BST, AVL, RBT).                                                  |
| **Bulk Operations**             | Ensures correct behavior for `insertAll()`, `deleteAll()`, `containsAll()`, `retainAll()`, and `mergeAll()` across collections.                                                                                                             |
| **Self-Referential Operations** | Proves the tree safely handles identity operations like `deleteAll(self)`, `retainAll(self)`, and `mergeAll(self)`.                                                                                                                         |
| **Traversal**                   | Verifies that standard traversals yield sorted elements, empty trees yield empty traversals, and that `stream()` yields complete and sorted results.                                                                                        |
| **Iterator**                    | Validates sorted iteration, fail-fast `ConcurrentModificationException` on mid-iteration inserts/deletes, independent state between concurrent iterators, proper `hasNext()` boundaries, and ensures every element is visited exactly once. |
| **Constructors**                | Tests default construction, bulk iterable construction (including null/empty handling), and deep-copy constructors. Proves structural equality, memory independence, and full operational parity between the clone and its source.          |
| **Scale**                       | Subjects every tree to 10k sequential inserts, 10k sequential deletes, 10k shuffled mutations, and 100k randomized mixed operations against a `TreeSet` truth mirror.                                                                       |

---

## 4. Tree-Specific Tests

Beyond the contract, each tree undergoes specific invariant validation.

### AVL
* **Height Bound Validation:** Proves the tree height remains $\le 1.44 \times \log_2(n)$ after both sorted insertions and heavily randomized insertion workloads.
* **Why:** Height variation ($\Delta H \le 1$) is the defining mathematical invariant of AVL. If the height bound holds, the rotations are proven correct.

### Red-Black Tree
* **Validation Check (`validateRBT()`):** Executed after sorted inserts and again after 100k randomized mixed operations.
* **Height Bound:** Verifies the depth remains below 34 at 100,000 nodes, adhering to the theoretical bound of $\approx 2 \times \log_2(n)$.
* **What's Verified:** Proves the root is strictly Black, no Red node has a Red child, and every path from the root to a leaf node traverses the exact same number of Black nodes.

### Splay Tree
* **Root Migration:** Proves that an accessed element becomes the root immediately after a `contains()` query, and that newly inserted elements immediately become the root.
* **Structural Fluidity:** Proves that standard BST ordering remains mathematically valid after a splay rotation.
* **Why:** Structural assertions are strictly limited to the root because Splay shape dynamically mutates on every access.

### Treap
* **Validation Check (`validateTreap()`):** Executed heavily after both insertions and deletions, and after 10k randomized operations.
* **What's Verified:** Recursively proves the Max-Heap property—ensuring that every node possesses a priority strictly greater than or equal to both of its children, while simultaneously maintaining BST key ordering.

---

## 5. Concurrent Test Coverage

The `ConcurrentBinaryTreeTest` proves the external thread-safety of the trees when guarded by appropriate external locking mechanisms (e.g., `ReadWriteLock`). Covered trees are AVL, RBT, and Treap.

### Phase 1: Heavy Contention Writes
* **Workload:** 4 threads executing 20,000 inserts each (Total: 80,000 elements).
* **Guarantees Proven:** No data is lost to race conditions (final size is exactly 80,000) and the balancing invariants (Color, Height, Priority) remain uncorrupted under multi-threaded assault.

### Phase 2: Mixed Read/Write Churn
* **Workload:** 2 writer threads actively deleting values `0–39999`, while 2 reader threads simultaneously query values `40000–79999`.
* **Guarantees Proven:** Prevents structural corruption. In-order traversals remain perfectly sorted, and no reader thread experiences a `NullPointerException` or deadlocks while traversing the actively mutating tree structure. 
* **Design Note:** The read and write domains are intentionally non-overlapping. This guarantees that `contains()` assertions remain deterministic (readers query values that writers are forbidden from deleting).

---

## 6. TreeSet as Truth Mirror

For scale testing, the suite employs `java.util.TreeSet` as an absolute correctness oracle.

* **Usage:** We strictly compare structural realities: `truth.size() == tree.size()` and `truth.contains(x) == tree.contains(x)`.
* **Philosophy:** `TreeSet` is never utilized to evaluate performance, GC pressure, or speed inside the unit tests. Its sole purpose is to mathematically prove that after 100,000 randomized operations, ChaosTree holds the exact same data as the JDK standard library.

---

## 7. What the Suite Does Not Test

To maintain suite speed and conceptual focus, several domains are explicitly excluded from unit tests.

### Splay Locality Testing
We do not test temporal locality or Zipfian distribution speeds in unit tests. Proving the amortized $O(1)$ Splay advantage requires a dedicated distribution-aware hardware benchmarking suite, which belongs in `benchmark.md`, not `test.md`.

### Garbage Collection Pressure
Testing allocation rates, node header sizes, and L1/L2 cache evictions is deferred to JMH performance testing with GC profilers. 

### Serialization
BinaryFamily nodes do not currently implement `java.io.Serializable`. Consequently, there is no serialization or deserialization validation.

### Concurrent Splay
Concurrent Splay tests are fundamentally impossible without strict clone-per-thread isolation. Because read operations perform structural pointer mutations, testing concurrent access against a single Splay tree would result in instant corruption.

### Iterator Removal
`Iterator.remove()` throws an `UnsupportedOperationException` and is not part of the public API contract. Therefore, mid-iteration removals via the iterator are not tested.

### Extension Opportunities
Future contributors looking to expand test coverage should consider adding property-based testing (e.g., jqwik) to auto-generate edge cases, and building rigorous JMH suites tailored specifically for testing temporal-locality distributions on the Splay tree.

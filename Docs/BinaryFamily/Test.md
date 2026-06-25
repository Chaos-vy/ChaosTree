# ChaosTree Test Architecture & Coverage

This document details the testing strategy, architecture, and coverage of the ChaosTree Binary Family. It is designed to provide contributors with a complete understanding of what the test suite validates, how those validations are enforced, and what scenarios are explicitly out of scope.

← Back to [README](README.md)

---

## 1. Overview

The ChaosTree test suite validates five distinct binary tree algorithms (BST, AVL, Red-Black, Splay, Treap) through a unified contractual testing framework.

### Binary Family Test Coverage
* **Active Unit Tests:** 387 (Binary Family specific)

### Repository-Wide Test Coverage
The following metrics represent the entire ChaosTree project, including both the Binary and N-ary families:

* **Total Project Unit Tests:** 579 (387 Binary + 182 N-ary + 10 Fuzz Tests)

Collectively, the suite confirms that regardless of the underlying balancing algorithm, every tree in the `BinaryFamily` adheres to the contract of a generic search tree. It verifies structural integrity, traversal correctness, mathematical invariants, fail-fast mechanics, and bulk-mutation stability under both isolated and concurrent workloads.

---

## 2. Test Architecture

The suite relies heavily on inheritance to enforce contractual behavior while avoiding test duplication.

### BinaryTreeContractTest

Instead of writing identical insertion and deletion tests for five different trees, all shared behavior is centralized in an abstract `BinaryTreeContractTest`. Every concrete tree test class (e.g., `AVLTest`, `SplayTest`) extends this base class and supplies its specific tree instance via a factory method. This ensures that every tree implementation inherits the exact same behavioral verifications for core operations, iterations, and streams.

### StableStructureContractTest

Certain operations assume a tree's structure remains immutable during read-only queries. `StableStructureContractTest` extends `BinaryTreeContractTest` to test queries like Least Common Ancestor (LCA). Splay trees and Treaps are explicitly excluded from this suite—Splay because its `contains()` operation structurally mutates the tree on every access, and Treap because its randomized priorities cause structural variations that make static hierarchy queries like LCA functionally different than standard deterministic trees.

### ConcurrentBinaryTreeTest

Concurrency testing requires a separate lifecycle. Contract tests operate in isolation to verify deterministic logic; concurrent tests provide confidence in thread-safety under heavy contention. `ConcurrentBinaryTreeTest` operates its own suite with heavy thread pools, latches, and race-condition triggers. 

* **BST Excluded:** It has no balancing invariant to protect.
* **Splay Excluded:** Its read operations perform structural writes, meaning `ReadWriteLock` separation is architecturally invalid.

---

## 3. Contract Test Coverage

The following behaviors are comprehensively validated at the `BinaryTreeContractTest` level for every tree in the family:

| Category                        | What's Tested                                                                                                                                                                                                                               |
|:--------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Empty Tree**                  | Evaluates `size()`, `isEmpty()`, `contains()`, `min()`, and `max()`. Confirms that calling `delete()` on an empty tree behaves as a safe no-op.                                                                                               |
| **Insert**                      | Single insertions, bulk insertions from iterables, verification that duplicate insertions throw `DuplicateNodeException`, and that inserting a `null` value fast-fails.                                                                     |
| **Delete**                      | Deletion of leaf nodes, nodes with one child, nodes with two children, and the root node. Verifies non-existing deletions are safely ignored.                                                                                                 |
| **Min/Max**                     | Verifies retrieval of correct extremes and confirms that querying extremes on an empty tree throws `EmptyTreeException`.                                                                                                                      |
| **Successor/Predecessor**       | Validates that navigational queries return the correct sorted adjacent elements based on structural traversal.                                                                                                                              |
| **Floor/Ceil**                  | Ensures queries return exact matches when present, and nearest candidates when the exact match is missing.                                                                                                                                  |
| **Kth Smallest**                | Validates retrieval of the first and last elements, and verifies that 0-index or out-of-bounds queries throw appropriate exceptions.                                                                                                          |
| **LCA**                         | Validates Lowest Common Ancestor logic for nodes in the same subtree, nodes splitting across the root, and when one query node is the ancestor of the other (Restricted to BST, AVL, RBT).                                                  |
| **Bulk Operations**             | Ensures correct behavior for `insertAll()`, `deleteAll()`, `containsAll()`, `retainAll()`, and `mergeAll()` across collections.                                                                                                             |
| **Self-Referential Operations** | Verifies the tree safely handles identity operations like `deleteAll(self)`, `retainAll(self)`, and `mergeAll(self)`.                                                                                                                         |
| **Traversal**                   | Verifies that standard traversals yield sorted elements, empty trees yield empty traversals, and that `stream()` yields complete and sorted results.                                                                                        |
| **Iterator**                    | Validates sorted iteration, fail-fast `ConcurrentModificationException` on mid-iteration inserts/deletes, independent state between concurrent iterators, proper `hasNext()` boundaries, and ensures every element is visited exactly once. |
| **Constructors**                | Tests default construction, bulk iterable construction (including null/empty handling), and deep-copy constructors. Verifies structural equality, memory independence, and full operational parity between the clone and its source.          |
| **Scale**                       | Subjects every tree to 10k sequential inserts, 10k sequential deletes, 10k shuffled mutations, and 100k randomized mixed operations against a `TreeSet` truth mirror.                                                                       |

---

## 4. Tree-Specific Tests

Beyond the contract, each tree undergoes specific invariant validation.

### AVL
* **Height Bound Validation:** Confirms the tree height remains $\le 1.44 \times \log_2(n)$ after both sorted insertions and heavily randomized insertion workloads.
* **Why:** Height variation ($\Delta H \le 1$) is the defining mathematical invariant of AVL. Validating the height bound provides high confidence that the rotations are correct.

### Red-Black Tree
* **Height Bound:** Verifies the depth remains below 34 at 100,000 nodes, adhering to the theoretical bound of $\approx 2 \times \log_2(n)$.
* **What's Verified:** Height-bound adherence is the primary correctness signal. If the tree exceeds 34 levels at 100k nodes, the coloring or rotation logic is broken. Internal color and black-height invariant validation is evaluated via the JMH benchmark suite rather than unit tests.

### Splay Tree
* **Root Migration:** Confirms that an accessed element becomes the root immediately after a `contains()` query, and that newly inserted elements immediately become the root.
* **Structural Fluidity:** Verifies that standard BST ordering remains valid after a splay rotation.
* **Why:** Structural assertions are strictly limited to the root because Splay shape dynamically mutates on every access.

### Treap
* **Validation Check (`validateTreap()`):** Executed after both insertions and deletions, and after 10k randomized operations.
* **What's Verified:** Recursively checks the Max-Heap property—ensuring that every node possesses a priority strictly greater than or equal to both of its children, while simultaneously maintaining BST key ordering.

---

## 5. Concurrent Test Coverage

The `ConcurrentBinaryTreeTest` validates the external thread-safety of the trees when guarded by appropriate external locking mechanisms (e.g., `ReadWriteLock`). Covered trees are AVL, RBT, and Treap.

### Phase 1: Heavy Contention Writes
* **Workload:** 4 threads executing 20,000 inserts each (Total: 80,000 elements).
* **Behaviors Verified:** Validates that no data is lost to race conditions (final size is exactly 80,000) and that the balancing invariants (Height, Priority) remain uncorrupted under multi-threaded assault.

### Phase 2: Mixed Read/Write Churn
* **Workload:** 2 writer threads actively deleting values `0–39999`, while 2 reader threads simultaneously query values `40000–79999`.
* **Behaviors Verified:** Confirms structural stability. In-order traversals remain sorted, and readers do not experience a `NullPointerException` or deadlocks while traversing the actively mutating tree structure. 
* **Design Note:** The read and write domains are intentionally non-overlapping. This ensures that `contains()` assertions remain deterministic (readers query values that writers are forbidden from deleting).

---

## 6. TreeSet as Truth Mirror

For scale testing, the suite employs `java.util.TreeSet` as a correctness oracle.

* **Usage:** The test suite compares structural realities: `truth.size() == tree.size()` and `truth.contains(x) == tree.contains(x)`.
* **Philosophy:** `TreeSet` is never utilized to evaluate performance, GC pressure, or speed inside the unit tests. Its sole purpose is to verify that after 100,000 randomized operations, ChaosTree holds the exact same data as the JDK standard library.

---

## 7. What the Suite Does Not Test

To maintain suite speed and conceptual focus, several domains are explicitly excluded from unit tests.

### Splay Locality Testing
We do not test temporal locality or Zipfian distribution speeds in unit tests. Validating the amortized $O(1)$ Splay advantage requires a dedicated distribution-aware hardware benchmarking suite, which belongs in the benchmark suite, not the unit tests.

### Garbage Collection Pressure
Testing allocation rates, node header sizes, and L1/L2 cache evictions is deferred to JMH performance testing with GC profilers. 

### Serialization
BinaryFamily nodes do not currently implement `java.io.Serializable`. Consequently, there is no serialization or deserialization validation.

### Concurrent Splay
Concurrent Splay tests are fundamentally impossible without strict clone-per-thread isolation. Because read operations perform structural pointer mutations, testing concurrent access against a single Splay tree without isolation is outside the scope of thread-safety validations.

### Iterator Removal
`Iterator.remove()` throws an `UnsupportedOperationException` and is not part of the public API contract. Therefore, mid-iteration removals via the iterator are not tested.

### Extension Opportunities
Future contributors looking to expand test coverage should consider adding property-based testing (e.g., jqwik) to auto-generate edge cases, and building rigorous JMH suites tailored specifically for testing temporal-locality distributions on the Splay tree.

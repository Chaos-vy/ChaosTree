# ChaosTree Test Architecture & Coverage: N-ary Family

This document details the testing strategy, architecture, and coverage of the ChaosTree N-ary Family (`BTree` and `BPlusTree`). It is designed to provide contributors with a complete understanding of what the test suite validates, how those validations are enforced across different node degrees, and what scenarios are explicitly out of scope.

← Back to [README](README.md)

---

## 1. Overview

The ChaosTree test suite validates multi-way branching algorithms through a unified contractual testing framework, heavily parameterized to ensure invariants hold across varying node capacities.

### N-ary Family Test Coverage
* **Active Unit Tests:** 182 (N-ary Family specific)

### Repository-Wide Test Coverage
The following metrics represent the entire ChaosTree project, including both the Binary and N-ary families:

* **Total Project Unit Tests:** 579 (387 Binary + 182 N-ary + 10 Fuzz Tests)

Collectively, the suite validates that regardless of the underlying multi-way algorithm (B-Tree or B+ Tree), the structures adhere to the expected `NaryTree` and `ISearchTree` contracts. The suite verifies structural integrity, occupancy constraints, node splitting and merging behavior, range-query correctness, and stability under both isolated and contention-heavy workloads.

---

## 2. Test Architecture

The suite relies heavily on inheritance and JUnit 5 parameterization to enforce contractual behavior across massive permutations of state.

### NaryTreeContractTest

Instead of writing identical tests for `BTree` and `BPlusTree`, all shared behavior is centralized in the abstract `NaryTreeContractTest`. Every concrete tree test class (e.g., `BTreeTest`, `BPlusTreeTest`) extends this base class and supplies its specific tree instance via a factory method. 

### Degree Parameterization

Multi-way trees exhibit completely different geometric shapes depending on their `degree`. A bug in the root-split logic might only trigger at `degree=4`, while a child-borrowing bug might only manifest at `degree=128`. 

To solve this, almost all tests in `NaryTreeContractTest` are marked as `@ParameterizedTest` and fed an array of degrees (e.g., `4, 8, 32, 128`). This ensures that the exact same operation is validated on narrow, tall trees and wide, shallow trees alike.

### Concurrent Tests

Concurrency testing requires a separate lifecycle. `BTreeConcurrentTest` and `BPlusTreeConcurrentTest` operate their own suites with heavy thread pools and latches to provide confidence in correctness under contention-heavy workloads when guarded by an external `ReadWriteLock`.

---

## 3. Contract Test Coverage

The following behaviors are comprehensively validated at the `NaryTreeContractTest` level across various degrees:

| Category                 | What's Tested                                                                                                                                                                                                              |
|:-------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Empty Tree**           | Evaluates `size()`, `isEmpty()`, `contains()`, `min()`, and `max()`. Confirms that calling `delete()` on an empty tree behaves as a safe no-op.                                                                            |
| **Insert & Split**       | Validates that inserting beyond `2t-1` capacity successfully triggers a median-key promotion and splits the node. Confirms `DuplicateNodeException` prevents duplicate keys.                                               |
| **Delete & Merge**       | Validates deletion from leaves and internal nodes. Verifies the complex N-ary fallback logic: borrowing from left sibling, borrowing from right sibling, and merging siblings when both sit at the minimum `t-1` capacity. |
| **Range Queries**        | Ensures `range()` and `rangeStream()` return correct, fully sorted sub-lists across block boundaries. Crucial for N-ary trees where elements span multiple contiguous blocks.                                              |
| **Min/Max & Polling**    | Verifies retrieval and extraction of extremes. Specifically validates that `pollMin()` successfully triggers underflow-handling down the leftmost spine of the tree.                                                       |
| **Constructors**         | Tests default construction, bulk iterable construction, and deep-copy constructors. Verifies structural equality, memory independence, and full operational parity between the clone and its source.                       |
| **Scale & Truth Mirror** | Subjects every tree to massive randomized workloads (e.g., 100k mixed inserts and deletes) against a `TreeSet` truth mirror. Verifies that `truth.size() == nary.size()` and `truth.contains(x) == nary.contains(x)`.      |

---

## 4. Tree-Specific Tests

Beyond the shared contract, each tree undergoes specific structural validation related to its unique architecture.

### B-Tree
* **Internal Data Storage:** Validates that search queries successfully terminate at internal routing nodes without always traversing down to the leaf layer.
* **Predecessor/Successor Deletion:** When an internal node's key is deleted, the suite validates that it is correctly replaced by its predecessor from the left child or successor from the right child.

### B+ Tree
* **Leaf-Chain Integrity:** Validates the `next` pointer linked-list architecture. Ensures that sequential iterators and range queries successfully traverse the linked leaves without climbing back up the internal routing nodes.
* **Routing vs. Data Isolation:** Confirms that all user data strictly resides in the leaf nodes, and that internal nodes only store copies of keys for routing purposes.

---

## 5. Concurrent Test Coverage

The suite validates the external thread-safety of the N-ary trees when guarded by an external `ReadWriteLock`.

### Phase 1: Heavy Contention Writes
* **Workload:** 4 threads executing concurrent inserts on varying degrees.
* **Behaviors Verified:** Validates that no data is lost to race conditions and that node-splitting logic (which reallocates contiguous memory blocks) does not corrupt sibling pointers or lose keys under multi-threaded assault.

### Phase 2: Mixed Read/Write Churn
* **Workload:** Multiple writer threads actively deleting subsets of values while reader threads simultaneously query non-overlapping ranges.
* **Behaviors Verified:** Confirms structural stability. Prevents `IndexOutOfBoundsException` during array-shifts, ensuring readers do not crash while iterating through nodes that are being actively merged or shifted by writers.

---

## 6. What the Suite Does Not Test

To maintain suite speed and conceptual focus, several domains are explicitly excluded from unit tests.

### I/O Paging & Disk Persistence
Standard B-Trees are often associated with disk-based databases. **ChaosTree is strictly an in-memory search tree.** The suite does not test block serialization, memory-mapped files, or page-faults. The "blocks" in ChaosTree are `Object[]` arrays residing purely on the JVM heap.

### Garbage Collection Pressure
Multi-way trees generate unique GC profiles during splits and merges due to array reallocation. Testing allocation rates and array-copy latency is deferred to the JMH performance testing suite with GC profilers, rather than unit tests.

### Serialization
N-ary nodes do not currently implement `java.io.Serializable`. Consequently, there is no serialization or deserialization validation.

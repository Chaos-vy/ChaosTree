# 🌳 ChaosTree Library Status

This document outlines the current development status and upcoming release schedule for ChaosTree.

## Current Stable Release: `v1.0.1`
**Released:** **2 July 2026**

### Project Release Policy

ChaosTree maintains a minimum **7-day interval** between Maven Central releases. This release cadence allows fixes, documentation improvements, and API polish to accumulate into a single patch release rather than publishing every individual change immediately.

**Exception:** Critical issues affecting correctness, data integrity, or security may be released immediately outside the normal schedule.

### Confirmed Fixes & Improvements

The following changes have already been implemented, validated by the automated test suite, and are currently awaiting the next scheduled patch release.

#### Correctness
* **`BPlusTree` Range Edge Cases:** Hardened `floor()` fallback logic. Leaf traversal now correctly evaluates the previous leaf boundary when the floor value resides strictly before the leftmost element of the currently routed leaf.
* **Empty Tree Resilience:** Fixed an issue where `retainAll()` on an empty tree incorrectly propagated `EmptyTreeException` instead of safely returning.
* **N-ary Height Standardization:** Standardized `NaryTree` `height()` so that a single-node tree consistently reports a height of `0`, matching the binary tree family.

#### API Improvements
* **Value-Equality (`equals()` & `hashCode()`):** Implemented true Java Collection Contract equality. Structurally independent tree families (e.g., `AVL` vs `RBT`, `BTree` vs `BPlusTree`) now correctly evaluate as equal when containing identical elements. Backed by an incrementally maintained O(1) rolling hash.
* **Lazy Range Streams (`rangeStream()`):** Substituted eager element evaluation with a true `BoundedInOrderIterator`, making binary tree range streams lazy and O(log N + K) scalable.
* **`lca()` Optimization:** Upgraded lowest common ancestor complexity to O(H) by rejecting missing nodes upfront via `contains()` validation, eliminating the previous O(N²) worst-case validation penalty.

#### Developer Experience
* **Improved Bounds Diagnostics:** Enhanced the `IllegalArgumentException` thrown by `kthSmallest()` to report the valid bounds (for example, `k=50 is out of bounds [1, 25]`).
* **Package Documentation:** Added package-level documentation (`package-info.java`) across the public API.
* **Complexity Documentation:** Updated `ISearchTree` JavaDocs to document O(k) complexity for `kthSmallest()` across all implementations, defending the architectural commitment to maximum contiguous memory density.
* **Structural Immutability:** Declared `BPlusTree` as `final`, aligning it with the other concrete tree implementations.
* **Regression Testing:** Added `Release101VerificationTest` to permanently enforce hashing, equality, and streaming boundaries which increased total test count from 579 -> 585.

# 🗺️ ChaosTree Library Status

This document outlines the current development status and upcoming release schedule for ChaosTree.

## Upcoming Patch Release: `v1.0.1`

### Project Release Policy

ChaosTree maintains a minimum **7-day interval** between Maven Central releases. This release cadence allows fixes, documentation improvements, and API polish to accumulate into a single patch release rather than publishing every individual change immediately.

**Exception:** Critical issues affecting correctness, data integrity, or security may be released immediately outside the normal schedule.

### Confirmed Fixes & Improvements

The following changes have already been implemented, validated by the automated test suite, and are currently awaiting the next scheduled patch release.

* **`BPlusTree` Range Edge Cases:** Hardened `floor()` fallback logic. Leaf traversal now correctly evaluates the previous leaf boundary when the floor value resides strictly before the leftmost element of the currently routed leaf.
* **Empty Tree Resilience:** Fixed an issue where `retainAll()` on an empty tree incorrectly propagated `EmptyTreeException` instead of safely returning.
* **Improved Bounds Diagnostics:** Enhanced the `IllegalArgumentException` thrown by `kthSmallest()` to report the valid bounds (for example, `k=50 is out of bounds [1, 25]`).
* **N-ary Height Standardization:** Standardized `NaryTree` `height()` so that a single-node tree consistently reports a height of `0`, matching the binary tree family.
* **Structural Immutability:** Declared `BPlusTree` as `final`, aligning it with the other concrete tree implementations.
* **Package Documentation:** Added package-level documentation (`package-info.java`) across the public API.

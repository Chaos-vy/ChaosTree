# Changelog

All notable changes to ChaosTree will be documented in this file. I believe in being transparent about what I add, change, and fix.

---

## [1.2.0] - 2026-08-09

## ChaosTree v1.2.0 — Zero-Allocation Engine & JDK 11 Support

### JDK 11 Support
- Full codebase compatibility with **JDK 11**.
- Removed Java 14+ `record` classes from core traversal paths.
- Performance improvement
- Removed enhanced switch expression.
- New Benchmark data

---
## [1.1.0] - 2026-08-07
### Added
- Added `NavigableSet` compatibility.
- The following operations are intentionally unsupported and throw `UnsupportedOperationException`:
    - `descendingSet()`
    - `descendingIterator()`
    - `subSet(T, boolean, T, boolean)`
    - `headSet(T, boolean)`
    - `tailSet(T, boolean)`
    - `subSet(T, T)`
    - `headSet(T)`
    - `tailSet(T)`

### Changed
- Moved common Maven configuration from `pom.xml` to the Super POM.
- Renamed `containsAll()` to `containsAllElements()` to avoid ambiguity with the `NavigableSet` API.
- Renamed `retainAll()` to `retainAllElements()` to avoid ambiguity with the `NavigableSet` API.
- `BinaryTree` `insert` and `delete` operation is now Iterative not limited to SOF.
- ADR and JavaDocs improvement.
- CI/CD now tests across a full OS matrix on JDK 17, 21, and 25 using the stable setup-java@v4 action.
- Interface `ITree`-> `Tree` and `ISearchTree` -> `SearchTree` now renamed.

## [1.0.1] - 2026-07-02

### Added
- Package-level documentation (`package-info.java`).
- Regression tests for equality, hashing, and range streaming.

### Changed
- Implemented Java Collection contract (`equals()` / `hashCode()`) across all tree implementations.
- `rangeStream()` is now lazily evaluated.
- Optimized `lca()` to O(H).
- Standardized `NaryTree.height()` for consistency.
- Improved `kthSmallest()` error messages.
- Declared `BPlusTree` as `final`.
- Updated API complexity documentation.

### Fixed
- Fixed `BPlusTree.floor()` edge cases during leaf traversal.
- Fixed `retainAll()` behavior on empty trees.

### Testing
- Increased test suite from **579 → 585** tests.

## [1.0.0] - Initial Release - 26-06-2026
### Added

#### Binary Trees

> Binary Tree, AVL Tree, Red Black Tree (RBT), Splay and Treap

#### N-ary Trees

> B-Tree and B+Tree

#### Core API

* Tree contract
* SearchTree contract
* BinaryTree API
* NaryTree API

#### Features

* Collection-style operations
* Stream support
* Range queries
* Priority operations (`pollMin`, `pollMax`)
* Multiple traversal strategies
* Tree visualization support


#### Quality

* 515 automated tests
* Verified JDK 17 baseline compatibility (tested across JDK 17, 21, 25. 26)
* Randomized fuzz testing
* Concurrent access validation
* JPMS module support
* JavaDoc documentation
* Maven Central publishing support

### Notes

This is the first public release of ChaosTree. I am incredibly proud of the architecture and the rigorous testing that went into this release.



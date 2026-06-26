_# Changelog

All notable changes to ChaosTree will be documented in this file. We believe in being transparent about what we add, change, and fix.

---

## [1.0.0] - Initial Release 
26 June 2026
### Added

#### Binary Trees

* BST
* AVL
* RBT
* Treap
* Splay

#### N-ary Trees

* BTree
* BPlusTree

#### Core API

* ITree abstraction
* ISearchTree contract
* BinaryTree API
* NaryTree API

#### Features

* Collection-style operations
* Stream support
* Range queries
* Priority operations (`pollMin`, `pollMax`)
* Multiple traversal strategies
* Tree visualization support

#### Documentation & Benchmarks

* Data-driven N-ary telemetry and micro-architectural analysis: [NaryFamily/Benchmark.md](docs/NaryFamily/Benchmark.md)
* In-depth CPU Cache physics & degree (t) selection guide: [NaryFamily/DegreeOptimization.md](docs/NaryFamily/DegreeOptimization.md)
* Added and updated [Architecture Decision Records (ADRs)](docs/ADR/README.md) covering node architecture, recursive vs iterative boundaries, and internal routing.
* Explicit tracking of benchmark environments (i5, JDK 21).

#### Quality

* 579 automated tests
* Verified JDK 17 baseline compatibility (tested across JDK 17, 21, 25. 26)
* Randomized fuzz testing
* Concurrent access validation
* JPMS module support
* JavaDoc documentation
* Maven Central publishing support

### Notes

This is the first public release of ChaosTree. We are incredibly proud of the architecture and the rigorous testing that went into this release._

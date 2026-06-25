# Architecture Decision Records (ADRs)

This directory contains our formalized Architecture Decision Records (ADRs). Think of these as our engineering diary—they document all the non-obvious choices we made while building ChaosTree, covering what we built, what we rejected, and exactly why we made those calls.

← Back to [BinaryFamily README](../BinaryFamily/README.md) | [NaryFamily README](../NaryFamily/README.md)

---

## Decision Log

|                              ADR                              | Title                                                                    |
|:-------------------------------------------------------------:|:-------------------------------------------------------------------------|
|         **[ADR-001](ADR-001-DeleteResult-Record.md)**         | `DeleteResult` Record over `boolean[]`                                   |
|          **[ADR-002](ADR-002-modCount-as-long.md)**           | Modification Count (`modCount`) as `long`                                |
|            **[ADR-003](ADR-003-CRTP-Pattern.md)**             | CRTP Pattern (`BiNode<T, N extends BiNode<T,N>>`)                        |
|          **[ADR-004](ADR-004-volatile-Retained.md)**          | `volatile` Retained in `AbstractBiTree` (Rejected in `AbstractNaryTree`) |
|         **[ADR-005](ADR-005-Leaf-children-null.md)**          | Leaf `children = null` (N-ary Node Memory Paradox)                       |
|     **[ADR-006](ADR-006-Object-Array-over-ArrayList.md)**     | `Object[]` over `ArrayList` in N-ary Engine                              |
| **[ADR-007](ADR-007-Rejecting-the-FlatTree-Architecture.md)** | `flatTree` Retired                                                       |
|  **[ADR-008](ADR-008-Stream-Traversal-API-Segregation.md)**   | Stream & Traversal API Segregation                                       |
|    **[ADR-009](ADR-009-BiNode-vs-ParentBiNode-Split.md)**     | `BiNode` vs `ParentBiNode` Split (Parent Pointer Separation)             |
|    **[ADR-010](ADR-010-SearchResult-NodeSearchResult.md)**    | `SearchResult` & `NodeSearchResult` — Single-Pass Traversal Records      |
|        **[ADR-011](ADR-011-Value-Store-over-Map.md)**         | Value-Store vs Key-Value Map API                                         |
---

## Long-Term Architectural Philosophy

We built ChaosTree entirely around a strict set of guiding principles:

1. **Encapsulation over Convenience:** We absolutely refuse to leak internal layout (`root()`, `Node` classes) just to make it easier for people to hack custom extensions. The API contract is paramount.
2. **Performance Through Measurement, Not Assumptions:** We rely heavily on JMH and hardware profiling. Algorithmic theory is great, but we validate it against actual L1 cache geometry, not just textbook Big-O notation.
3. **Immutability Where Possible:** Our algorithms aim to be structurally pure unless they are explicitly mutating the tree.
4. **Mechanical Sympathy:** Our design choices always favor how the CPU actually *wants* to execute code. We obsess over minimizing branch mispredictions and pointer-chasing cache misses.
5. **Explicit Tradeoff Documentation:** Every single tree in this library has a breaking point. We document those limits explicitly, rather than relying on one-size-fits-all marketing hype.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.chaos-vy/chaos-tree.svg?label=maven%20central)](https://search.maven.org/artifact/io.github.chaos-vy/chaos-tree)
[![GitHub release](https://img.shields.io/github/v/release/Chaos-vy/ChaosTree)](https://github.com/Chaos-vy/ChaosTree/releases)
[![License](https://img.shields.io/github/license/Chaos-vy/ChaosTree)](LICENSE)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/Chaos-vy/ChaosTree/actions)

## What is ChaosTree?

**ChaosTree is a Java Sorted Set/Map library built around multiple search-tree data structures, including AVL Trees, Red-Black Trees, B-Trees, and B+ Trees.**

The library provides both **Set and Map implementations**, with APIs designed around the semantics of the JDK's `NavigableSet`, `NavigableMap`, `SequencedSet`, and `SequencedMap` contracts.

In addition to the standard collection APIs, ChaosTree provides specialized construction APIs for users who want direct control over the initial structure of N-ary trees, Do read 

* `buildFromSorted(Iterator, factor)`
* `importFlatMatrix(Object[][], factor)`

These APIs allow users to control the target node occupancy through a configurable `factor` in the supported range **[0.5, 1.0]**, while maintaining the structural invariants required by the underlying B-Tree/B+Tree design.

### Correctness & Validation

ChaosTree is validated through multiple layers of testing:

* **Guava Testlib** compatibility testing
* **jqwik** property-based testing
* Randomized differential testing against reference collections
* White-box structural validation of tree nodes
* Direct validation of B-Tree/B+Tree structural invariants
* Exception and iterator-contract testing
* Serialization and cloning tests

The structural tests inspect the internal tree representation rather than relying solely on externally observable behavior. This provides an additional layer of validation for node occupancy, ordering, topology, and balancing invariants.

Performance claims are backed by reproducible JMH benchmark configurations. If a referenced benchmark source is missing from the repository due to project cleanup, it can be restored or replaced with an updated benchmark.

### Why ChaosTree?

* **Cache-Locality First:** The N-ary engine packs data tightly into pre-allocated exact-capacity arrays, drastically improving L1/L2 CPU cache hit rates and memory load stalls by nearly 40% during large range scans.
* **Strictly Compatible:** Leverages the new JDK 21 `SequencedCollection`, `SequencedSet`, and `SequencedMap` interfaces. It passes the Guava Testlib (214,000+ tests) to enforce identical semantics to `java.util.TreeMap` and `TreeSet`.
* **Public Bulk Load:** I do explicitly provide two powerful API through which user is allowed to build the N-ary tree family, It only works at empty tree. Need sorted data. Verified tested.
* **Serializable & Cloneable** Each tree supports Serialization **(Bulk load O(N))** as well as Cloneable.


## Requirements

- **Minimum JDK: 0xCAFEBABE 0000 0041 | JDK 21+**
- **Build Tool: Maven 3.8+** 

**Details about ChaosTree:** https://chaos-vy.github.io/ChaosTree/index.html 


*(Note: As strictly sorted structures, `addFirst()` and `addLast()` are unsupported and fail-fast).*

## Getting ChaosTree

### Maven coordinates

```xml
<dependency>
    <groupId>io.github.chaos-vy</groupId>
    <artifactId>chaos-tree</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Gradle

```kotlin
implementation("io.github.chaos-vy:chaos-tree:2.0.0")
```

## Quick start

Create a highly-optimized `BPlusTreeMap` to leverage the N-ary engine:

```java
import chaos.tree.naryMap.BPlusTreeMap;
import java.util.NavigableMap;

public class Main {
    public static void main(String[] args) {
        // Degree 64 B+Tree Map
        NavigableMap<Integer, String> map = new BPlusTreeMap<>();

        map.put(1, "Chaos");
        map.put(2, "Tree");
        map.put(3, "Performance");

        // Instant range scans traversing the contiguous leaf-linked list
        NavigableMap<Integer, String> subMap = map.subMap(1, true, 3, true);
        System.out.println(subMap);
    }
}
```


## Data Structures and Architecture

ChaosTree is split into two foundational engines:

* **The N-ary Family (Sets & Maps):** `BTree`, `BPlusTree`. Built for maximum read throughput, large-scale range scans, and zero GC churn. The `BPlusTree` pushes all real data to a contiguous double linked-list at the bottom layer, allowing high read through put.
* **The Binary Family (Sets):** `AVL`, `RBT`, . Built for fast point-queries and everyday data storage where the extreme caching of the N-ary engine is not required.

## Testing & Thread-Safety

I wanted ChaosTree to be correct just as much as I wanted it to be fast. It is validated by these following testing suite:

* **Guava Testlib:** ChaosTree passes 214,000+ generated test cases validating exact `java.util.NavigableMap` and `NavigableSet` for all tree.
* **The Fuzz Test:** Trees are subjected to hundreds of thousands of completely randomized property tests via `jqwik` to verify structural invariants against a source-of-truth (`java.util.TreeMap`). Due to Nary API node structure of 32 the new node never got created in Guava So I explicitly designed the verify API which verify explicitly for that.
* **Strict Contracts:** Enforces fail-fast `ConcurrentModificationException` iterator semantics, exact size counting, and strict Null-Pointer guards on custom Comparators.

## Documentation

* **Architecture Decision Records:** [`docs/ADR.html`](https://chaos-vy.github.io/ChaosTree/utils/ADR.html)
* **JMH GC Profiling & The 82ms Pause:** [`docs/utils/JMH-Report.html`](https://chaos-vy.github.io/ChaosTree/utils/JMH-Report.html)
* **Throughput & CPU Benchmarks:** [`docs/Benchmark_Analysis.html`](https://chaos-vy.github.io/ChaosTree/utils/Benchmark_Analysis.html)
* **The Testing Journey:** [`docs/Test_Journey.html`](https://chaos-vy.github.io/ChaosTree/utils/build/Test_Journey.html)
* **Release history:** [`CHANGELOG.md`](CHANGELOG.md)
* **Contributing guide:** [`CONTRIBUTING.md`](CONTRIBUTING.md)

## Support and contributions

* **Bugs and features:** GitHub Issues
* **Discussion:** GitHub Discussions

Pull requests and well-scoped issue reports for compatibility, correctness, and maintenance work are welcome! 


---


[![Maven Central](https://img.shields.io/maven-central/v/io.github.chaos-vy/chaos-tree.svg?label=maven%20central)](https://search.maven.org/artifact/io.github.chaos-vy/chaos-tree)
[![GitHub release](https://img.shields.io/github/v/release/Chaos-vy/ChaosTree)](https://github.com/Chaos-vy/ChaosTree/releases)
[![License](https://img.shields.io/github/license/Chaos-vy/ChaosTree)](LICENSE)

# 🌳 ChaosTree: A Fast Java Search Tree Library

**ChaosTree** is a highly optimized in-memory search tree library for Java.

I build ChaosTree because I wanted to see what happens when I take textbook data structures and optimize them for real-world JVM memory and CPU caches. It includes both classic Binary Trees (AVL, RBT, etc.) and cache-friendly N-ary Trees (B-Tree, B+ Tree).

---
## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.chaos-vy</groupId>
    <artifactId>chaos-tree</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Gradle (Kotlin)

```kotlin
implementation("io.github.chaos-vy:chaos-tree:1.2.0")
```

### Gradle (Groovy)

```groovy
implementation 'io.github.chaos-vy:chaos-tree:1.2.0'
```
## ☕ Requirements

- Minimum JDK: 11
- Recommended JDK: 11+
- Build Tool: Maven 3.8+

* ChaosTree is compiled using `--release 11`.
* Compatibility testing is performed on JDK 11, JDK 17, JDK 21 and JDK 25 to verify consistent behavior across modern Java runtimes.
* Performance benchmarks are executed on JDK 11 & JDK 21.

---

## 🚀 Quick Start: Modern API Usage

ChaosTree provides a rich, modern, Java Collections-style API. It completely encapsulates pointer arithmetic and exposes functional paradigms like Streams, Range Queries, and Priority Polling.

### 1. Fast Range Scanning (N-ary Engine)

By packing data tightly into arrays, the N-ary trees are extremely friendly to your CPU's L1/L2 caches, making range queries really fast.
```java
// Create a B+ Tree (degree must be greater than 1)(CLRS)
NaryTree<Integer> index = new BPlusTree<>(32);
BPlusTree<List<Integer>> xyz = new BPlusTree<>(); //Default degree(t) = 32.
index.insertAll(hugeDataset); //Huge dataset must be Iterable

// Fast Range Extraction (O(log N) search + O(K) memory block copy)
List<Integer> results = index.range(100, 500);

// Modern Lazy Evaluation via Streams
index.rangeStream(100, 500)
     .filter(val -> val % 2 == 0)
     .forEach(System.out::println);
```
**Do read the Benchmark** [Report](BenchmarkReport/README.md)

### JDK 11 (Latency in ns)
| Tree Type  | Degree | Avg (ns/op) | p50 | p90 | p99 | p99.9 | pMax      |
|:-----------|:-------|:------------|:----|:----|:----|:------|:----------|
| **B+Tree** | t=8    | 119.5       | 121 | 124 | 137 | 1,433 | 116,224   |
| **B+Tree** | t=32   | 108.5       | 116 | 130 | 134 | 4,160 | 121,088   |
| **B+Tree** | t=64   | 102.4       | 95  | 112 | 125 | 1,075 | 30,976    |
| **B+Tree** | t=128  | 102.0       | 94  | 110 | 114 | 1,402 | 2,519,040 |

### JDK 21 (Latency in ns)
| Tree Type  | Degree | Avg (ns/op) | p50 | p90 | p99 | p99.9 | pMax   |
|:-----------|:-------|:------------|:----|:----|:----|:------|:-------|
| **B+Tree** | t=8    | 117.2       | 112 | 119 | 126 | 985   | 13,856 |
| **B+Tree** | t=32   | 111.2       | 109 | 113 | 120 | 1,133 | 14,080 |
| **B+Tree** | t=64   | 108.7       | 107 | 111 | 127 | 1,228 | 13,888 |
| **B+Tree** | t=128  | 105.5       | 106 | 109 | 121 | 1,043 | 15,200 |

### 2. Classic Binary Trees (Binary Engine)

Great for everyday data storage, building priority queues, or when I just want a classic, fast binary tree.

```java
// Create a classic auto-balancing Red-Black Tree
BinaryTree<String> tree0 = new RBT<>();
tree0.insertAll(Arrays.asList("Chaos", "Tree", "Java", "Performance"));

// Priority Queue Behaviors (O(log N) extraction)
String smallest = tree0.pollMin(); 
String largest = tree0.pollMax();

// Deep Structural Traversals via Stream API
tree0.stream(TraversalType.LEVEL_ORDER)
    .forEach(System.out::println);
````
### Tree Visualization

```java
BinaryTree<String> tree = new RBT<>();
tree.insertAll(List.of("Chaos", "java", "first", "library"));

System.out.println(tree);
```

Output:

```text
first(B)
+-- Chaos(B)
\-- java(B)
    \-- library(R)
```
**Do read the Benchmark** [Report](BenchmarkReport/README.md)

---
## 🌟 Available Data Structures (v1.2.0)

* **The Binary Family:** BST, AVL, RBT, Splay, Treap
* **The N-ary Family:** BTree, BPlusTree

---

## ⚙️ Architecture & Design Choices

### 1. Low Memory Footprint

ChaosTree strictly routes data using `.compareTo()` and never relies on Object Identity (`==`). Unlike standard `ArrayList` implementations that pad extra capacity, the N-ary engine allocates exact-capacity `Object[]` backing arrays. This eliminates wrapper object overhead and packs data tightly.

### 2. No Hidden ArrayList Overhead

The N-ary engine uses exact-capacity `Object[]` storage and `System.arraycopy()` intrinsics, avoiding the spare-capacity growth strategy used by dynamic arrays.

### 📈 Large-Scale Memory Stress Test

**Environment:**
* JDK 26
* Ubuntu 26.04
* -Xmx16g

**Results:**

* Binary Search Tree: ~357 million integer records before OOM
* N-ary Tree (degree 100): ~695 million integer records before OOM
* Under identical heap constraints, the N-ary engine demonstrated substantially higher storage density and scaled to nearly twice as many records before memory exhaustion.

### 3. The B+ Tree routing Advantage

The BPlusTree pushes all real data to a contiguous linked-list at the bottom layer. Internal nodes act primarily as routing structures, keeping the tree shallow and making large range scans incredibly smooth.

---

## 📊 Performance Benchmarks

ChaosTree has been extensively profiled to understand how it interacts with modern CPU caches.
### Test Environment:

* **CPU:** Intel Core i5 13450HX (24GB DDR5)
* **Java:** JDK 11
* **Tooling:** JMH + LinuxPerfNormProfiler


### 1. The N-ary Engine: Range Query Performance

### Range Scan Performance — Extracting 1,000,000 Elements

**Data size:** 10,000,000 | **Degree:** 128 | **Scan:** 10% of the entire tree

| Tree Type   |  Average (ns/op) | L1 Cache Reads / op | CPU Instructions / op | Max Latency (pMax) |
|:------------|-----------------:|--------------------:|----------------------:|-------------------:|
| **B-Tree**  |     8,135,801 ns |        68.0 Million |           200 Million |            62.7 ms |
| **B+ Tree** | **5,867,062 ns** |    **51.8 Million** |       **172 Million** |        **45.5 ms** |

The B+ Tree completed the benchmark approximately 25% faster.

### Reason

**Reduced Traversal Overhead**

The B+ Tree performs range scans directly through its linked leaf layer, reducing the amount of tree traversal required during sequential access.

**Improved Memory Locality**

The linked-leaf structure improves cache locality and enables more effective hardware prefetching during large range scans.

---

## 2. The Binary Engine: Read Throughput & Point Queries

(Random lookups across 100,000 elements)

The binary trees in ChaosTree are tuned for fast lookups without sacrificing strict data guarantees.

| Implementation | Average Time |
|----------------|--------------|
| **AVL**        | 129 ns/op    |
| **RBT**        | 129 ns/op    |
| **Treap**      | 218 ns/op    |
| **BST**        | 122 ns/op    |
| **Splay**      | 351 ns/op    |

**ChaosTree RBT:** Provides Red-Black Tree balancing while storing user values directly within tree nodes, reducing per-element memory overhead compared to key-value entry based structures.

**ChaosTree AVL:** Maintains a stricter balancing invariant than Red-Black Trees, yielding a lower theoretical maximum height (≈1.44 log₂N versus ≈2 log₂N). This can improve lookup performance in read-heavy workloads.

### ChaosTree vs TreeSet

When compared against the JDK's standard `TreeSet` (which is a Red-Black tree), my `BPlusTree` range scan at 1M elements completes in 263K ns/op vs TreeMap's traversal-based approach — approximately 25% faster due to leaf-chain locality, but since it doesn't wrap everything in heavy `Map.Entry` objects, it uses significantly less memory when you just need a Set.

> Results are representative of the test environment described above and may vary across hardware, JVM versions, and workloads.

### CPU Hardware Counters Reveal:

**Instruction Pipeline:** The B+ Tree eliminates stack-traversal overhead, executing tens of thousands fewer CPU instructions per operation.

**Hardware Pre-Fetching:** By riding the contiguous leaf linked-list, the CPU hardware pre-fetcher perfectly anticipates memory accesses, slashing memory load stalls by nearly 40%.

(**Note:** Results shown are representative of the test environment above and may vary across JVM versions, hardware, and workloads).

## 🛡️ Testing & Thread-Safety

I wanted ChaosTree to be correct just as much as I wanted it to be fast. It is validated by a 585-test suite:

**The Fuzz Test:** Trees are subjected to hundreds of thousands of completely randomized insertions, deletions, and sequential bursts to verify structure against a source-of-truth (`java.util.TreeSet`).

**Strict Contracts:** Enforces fail-fast `ConcurrentModificationException` iterator semantics, exact size counting, and strict Null-Pointer guards.

**Thread-Safety Validation:** Trees are stress-tested with 8 threads performing inserts, deletes, and lookups under external monitor synchronization to ensure correctness when wrapped in external locks. (Note: True fine-grained lock-free trees are on the roadmap!).

---
## 📚 Documentation

Detailed design documents and architectural decisions are available in the `Docs/` directory.

### Core Documentation

| Document                                       | Description                                                                                   |
|------------------------------------------------|-----------------------------------------------------------------------------------------------|
| [`Docs/Architecture.md`](docs/Architecture.md) | High-level overview of ChaosTree's architecture, package organization, and design philosophy. |
| [`CONTRIBUTING.md`](CONTRIBUTING.md)           | For open source contribution and respective guidelines.                                       |
| [`CHANGELOG.md`](CHANGELOG.md)                 | Release history and notable changes across versions.                                          |

### Architecture Decision Records (ADR)

The [`ADR/`](ADR) directory contains records explaining significant architectural and API decisions.

Examples include:

* Why the API is organized as `Tree → SearchTree → BinaryTree / NaryTree`
* Why internal node implementations are hidden behind JPMS boundaries
* Why traversal APIs are exclusive to the Binary family
* Why range-query operations belong to the common `SearchTree` contract

### Binary Family Documentation

The [`Docs/BinaryFamily/`](docs/BinaryFamily/README.md) directory contains implementation and usage details for:

* BST
* AVL
* RBT
* Treap
* Splay

Topics include balancing strategies, invariants, complexity guarantees, and implementation notes.

### N-ary Family Documentation

The [`Docs/NaryFamily/`](docs/NaryFamily/README.md) directory contains implementation and usage details for:

* BTree
* BPlusTree

Topics include node splitting, merging, degree constraints, leaf-link traversal, and range-query behavior.

### API Documentation

Generated JavaDoc documentation is available with every release and provides complete API references for all public interfaces and implementations.

---
## 🗺️ Roadmap: The Future of ChaosTree

ChaosTree is actively evolving to support advanced fine-grained, lock-free concurrency models.

* **v1.2.0:** Foundational Binary and N-ary Search Trees. (Current)
* **v2.0.0:** Feature of map <K,V> key value pair.
* **v3.0.0:** Thinking....

---

### 📝 A Note from the Author

## A Personal Note

ChaosTree started as a personal exploration of data structures during the summer. One thing naturally led to another—linked lists to binary trees, binary trees to self-balancing trees, and eventually to B-Trees and B+ Trees. Somewhere along the way, it stopped being a collection of implementations and became a library.

One moment that stayed with me was seeing an individual's name in the Java Collections documentation. It made me realize that libraries are built by people who simply decide to start somewhere.

This is where I started.

**— Vinay**

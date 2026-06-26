# 🌳 ChaosTree: A Fast, No-Nonsense Java Search Tree Library

**ChaosTree** is a highly optimized in-memory search tree library for Java.

We built ChaosTree because we wanted to see what happens when you take textbook data structures and optimize them for real-world JVM memory and CPU caches. It includes both classic Binary Trees (AVL, RBT, etc.) and cache-friendly N-ary Trees (B-Tree, B+ Tree).

---
## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.chaos-vy</groupId>
    <artifactId>chaos-tree</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle (Kotlin)

```kotlin
implementation("io.github.chaos-vy:chaos-tree:1.0.0")
```

### Gradle (Groovy)

```groovy
implementation 'io.github.chaos-vy:chaos-tree:1.0.0'
```
## ☕ Requirements

- Minimum JDK: 17
- Recommended JDK: 17+
- Build Tool: Maven 3.8+

* ChaosTree is compiled using `--release 21`.
* Compatibility testing is performed on JDK 17, JDK 21, and JDK 26 to verify consistent behavior across modern Java runtimes.
* Performance benchmarks are executed on JDK 21.

---

## 🚀 Quick Start: Modern API Usage

ChaosTree provides a rich, modern, Java Collections-style API. It completely encapsulates pointer arithmetic and exposes functional paradigms like Streams, Range Queries, and Priority Polling.

### 1. Fast Range Scanning (N-ary Engine)

By packing data tightly into arrays, the N-ary trees are extremely friendly to your CPU's L1/L2 caches, making range queries really fast.
```java
// Create a B+ Tree (degree must be greater than 1)
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

### 2. Classic Binary Trees (Binary Engine)

Great for everyday data storage, building priority queues, or when you just want a classic, fast binary tree.

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
---
## 🌟 Available Data Structures (v1.0.0)

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

We've spent a lot of time profiling ChaosTree to see how it interacts with actual hardware caches.

### Test Environment:

* **CPU:** Intel Core i5 13450HX (24GB DDR5)
* **Java:** JDK 21
* **Tooling:** JMH + LinuxPerfNormProfiler


### 1. The N-ary Engine: Range Query Performance

(Extracting 1,000,000 contiguous elements)

| Implementation       | Average Time  |
|----------------------|---------------|
| B-Tree (Degree 128)  | 352,218 ns/op |
| B+ Tree (Degree 128) | 263,157 ns/op |

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
| **AVL**        | 43 ns/op     |
| **RBT**        | 46 ns/op     |
| **Treap**      | 54 ns/op     |
| **BST**        | 64 ns/op     |
| **Splay**      | 686 ns/op    |

**ChaosTree RBT:** Provides Red-Black Tree balancing while storing user values directly within tree nodes, reducing per-element memory overhead compared to key-value entry based structures.

**ChaosTree AVL:** Maintains a stricter balancing invariant than Red-Black Trees, yielding a lower theoretical maximum height (≈1.44 log₂N versus ≈2 log₂N). This can improve lookup performance in read-heavy workloads.

### ChaosTree vs java.util.TreeMap

When compared against the JDK's standard `TreeMap` (which is a Red-Black tree), our `BPlusTree` range scan at 1M elements completes in 263K ns/op vs TreeMap's traversal-based approach — approximately 25% faster due to leaf-chain locality, but since it doesn't wrap everything in heavy `Map.Entry` objects, it uses significantly less memory when you just need a Set.

> Results are representative of the test environment described above and may vary across hardware, JVM versions, and workloads.

### CPU Hardware Counters Reveal:

**Instruction Pipeline:** The B+ Tree eliminates stack-traversal overhead, executing tens of thousands fewer CPU instructions per operation.

**Hardware Pre-Fetching:** By riding the contiguous leaf linked-list, the CPU hardware pre-fetcher perfectly anticipates memory accesses, slashing memory load stalls by nearly 40%.

(**Note:** Results shown are representative of the test environment above and may vary across JVM versions, hardware, and workloads).

## 🛡️ Testing & Thread-Safety

We wanted ChaosTree to be correct just as much as we wanted it to be fast. It is validated by a 579-test suite:

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

The [`Docs/ADR/`](docs/ADR) directory contains records explaining significant architectural and API decisions.

Examples include:

* Why the API is organized as `ITree → ISearchTree → BinaryTree / NaryTree`
* Why internal node implementations are hidden behind JPMS boundaries
* Why traversal APIs are exclusive to the Binary family
* Why range-query operations belong to the common `ISearchTree` contract

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

ChaosTree is actively evolving to support advanced  fine-grained, lock-free concurrency models.

* **v1.0.0:** Foundational Binary and N-ary Search Trees. (Current)
* **v1.1.0:** Concurrent B+ Tree
* **v1.2.0:** Concurrent Red-Black Tree

---

## 📏 Codebase Metrics

We believe in keeping the core engine clean, lean, and highly tested. Here is the exact breakdown of the ChaosTree v1.0.0 repository:

| Module        | Files | Blank | Comments | Code (LOC) |
|---------------|-------|-------|----------|------------|
| **Production**| 34    | 587   | 1,941    | **2,740**  |
| **Tests**     | 27    | 410   | 93       | **1,610**  |
| **Benchmarks**| 17    | 196   | 92       | **1,003**  |
| **Total**     | 78    | 1,193 | 2,126    | **5,353**  |

**Production Ratio (`(Tests + Benchmarks) : Production Code`):** `0.95 : 1`

For every line of production logic we write, we maintain nearly a full line of test and micro-architectural benchmark code.

---

### 📝 A Note from the Author

## A Personal Note

ChaosTree started as a personal exploration of data structures during the summer. One thing naturally led to another—linked lists to binary trees, binary trees to self-balancing trees, and eventually to B-Trees and B+ Trees. Somewhere along the way, it stopped being a collection of implementations and became a library.

One moment that stayed with me was seeing an individual's name in the Java Collections documentation. It made me realize that libraries are built by people who simply decide to start somewhere.

This is where I started.

**— Vinay**

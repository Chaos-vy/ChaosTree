# ChaosTree Roadmap 

This document outlines the upcoming features, architectural shifts, and performance optimizations planned for future releases of ChaosTree.

## Upcoming in `v1.2.0` (The Performance & Compatibility Update)

### 1. Zero-Padding Memory Optimization 
* **Status:** In Development
* **Details:** Refactoring the `NaryNode` internal layout by utilizing implicit `children == null` leaf checks. This drops the object footprint down to a perfect **24-byte boundary** (eliminating 7 bytes of dead JVM padding per node). This will drastically improve L1/L2 cache hit rates for massive B-Trees and B+Trees.

### 2. JDK 11 Enterprise Support 
* **Status:** In Development
* **Details:** Downgrading the compiler target from Java 17 to **Java 11**. By replacing internal Java 16 `record` types with standard POJOs, ChaosTree will become fully compatible with legacy enterprise systems while preserving ultra-fast Java 9 `VarHandle` concurrency support.

### 3. Cloud Performance Suite (JMH & JFR) 
* **Status:** Planned
* **Details:** Execution of a massive JMH (Java Microbenchmark Harness) suite on cloud infrastructure (n2 instances). This will include Java Flight Recorder (JFR) profiling to formally document CPU cache misses, allocation rates, and thread-contention metrics for the concurrent trees.

---

## 🗺️ Future Horizons (v2.0+)

### The `Map` Phase (NavigableMap Architecture) 🗺️
* **Details:** Currently, ChaosTree operates strictly as a `NavigableSet`. The next major architectural phase will introduce the `NavigableMap` interface. This will allow developers to store `Key-Value` pairs (e.g., `BTreeMap<K, V>`).

### Advanced Concurrent Engine 
* **Details:** Expanding the lock-free and fine-grained locking mechanisms to support massive read-heavy workloads with minimal latency.

---
*Got a feature request or performance suggestion? Open an issue on our GitHub!*

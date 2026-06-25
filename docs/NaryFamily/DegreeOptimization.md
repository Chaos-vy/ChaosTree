# Degree Optimization & Cache Locality

This document explains how to choose the optimal degree (`t`) parameter for your workload. We'll look at how this maps to real CPU hardware caches (L1/L2) and back it up with actual benchmark data.

← Back to [README](README.md)

---

## What is Degree (t)?

Every N-ary tree constructor requires a minimum degree parameter `t`:
- **Minimum keys per node:** `t - 1`
- **Maximum keys per node:** `2t - 1`
- **Maximum children per internal node:** `2t`

Choosing `t` isn't just about picking a random number. It's about finding the sweet spot where your tree's width and height balance against cache hierarchy and memory-access behavior. 

---

## 1. The Hardware Physics

### Cache Hierarchy (i5-13450HX, JDK 21 — Benchmark Environment)

| Cache | Size     | Latency    | Notes                                          |
|-------|----------|------------|------------------------------------------------|
| L1d   | 48 KB    | ~5 cycles  | Per P-core data cache                          |
| L2    | 2 MB     | ~14 cycles | Per P-core                                     |
| L3    | 24 MB    | ~40 cycles | Shared across all cores                        |
| RAM   | DDR5     | ~60 ns     | Accessed on LLC miss                           |

**Cache line size = 64 bytes.** This is the atomic unit of memory the CPU loads. A single reference (compressed OOPs enabled) = **4 bytes**. One cache line holds **16 references**.

### How Degree Maps to Cache Lines

When the engine calls `searchNode(node, key)`, it binary-searches the node's key array. The number of cache lines that must be loaded from memory depends directly on the key count:

| Degree ($t$) | Max Keys | Key Array Size (Compressed OOPs) | Full Array Cache Lines | Binary Search Cache Lines |
|:------------:|:--------:|:---------------------------------:|:----------------------:|:-------------------------:|
| t = 4        | 7        | 28 B                              | **< 1 line**           | 1                         |
| t = 8        | 15       | 60 B                              | **1 line**             | 1                         |
| t = 16       | 31       | 124 B                             | **2 lines**            | 1–2                       |
| **t = 32**   | **63**   | **252 B**                         | **4 lines**            | **2–3**                   |
| t = 64       | 127      | 508 B                             | 8 lines                | 2–3                       |
| t = 100      | 199      | 796 B                             | 13 lines               | 3                         |
| t = 128      | 255      | 1,020 B                           | 16 lines               | 3–4                       |
| t = 200      | 399      | 1,596 B                           | 25 lines               | 4                         |
| t = 512      | 1,023    | 4,092 B                           | 64 lines               | 5–6                       |

**Binary Search Cache Lines** = the minimum lines touched during a log₂(keys) search. Not all 4 lines need loading — binary search jumps to the middle, then one half. Only the lines containing the accessed elements get loaded.

**At t=32:** The full key scan costs 4 cache lines, but binary search typically touches only 2–3. The entire node fits in 4–5 lines of L1 (including children pointers). **This is the smallest degree where tree height is shallow enough that the pointer-chasing cost drops below the array-scan cost.**

---

## 2. The Benchmark Evidence

All measurements at **1M elements**, single-threaded random reads.

### L1 Cache Loads Per Random Read Operation (P-Core)

| Degree | BTree L1 Loads | BPlusTree L1 Loads | Tree Height (BTree ~) |
|:------:|:--------------:|:------------------:|:---------------------:|
| t=4    | 79.90          | 80.38              | ~13                   |
| t=8    | 59.49          | 59.11              | ~9                    |
| t=16   | 52.72          | 52.14              | ~6                    |
| **t=32**   | **45.80**  | **45.05**          | **~4**                |
| t=64   | 45.94          | 44.98              | ~3                    |
| t=100  | 38.92          | 38.02              | ~3                    |
| t=128  | 38.93          | 37.93              | ~3                    |
| t=200  | 38.88          | 38.00              | ~3                    |
| t=512  | 38.95          | 37.97              | ~2                    |

**The curve reveals two distinct phases:**
1. **t=4 → t=32:** L1 loads drop sharply (80 → 46) as tree height compresses from ~13 levels to ~4. Each level eliminated saves one full node's worth of pointer chasing.
2. **t=32 → t=512:** L1 loads flatten (~46 → ~39). Height is now 3–4 levels regardless of further degree increase. The savings become marginal.

**t=32 is the inflection point** where the sharp drop ends. However — as the data shows — **t=64 delivers virtually identical L1 loads (45.94 vs 45.80 for BTree, 44.98 vs 45.05 for BPlusTree).** The real boundary is the **t=32–t=64 window**, not a single value.

### t=32 vs t=64 — Head-to-Head

| Metric                   | BTree t=32   | BTree t=64   | BPlusTree t=32 | BPlusTree t=64 |
|--------------------------|:------------:|:------------:|:--------------:|:--------------:|
| Random Read (ns/op)      | **500.1**    | 509.4        | 519.0          | **528.5**      |
| Range Scan (ns/op)       | 474,222      | **368,055**  | 334,197        | **329,611**    |
| P-Core L1 loads (random) | 45.80        | 45.94        | 45.05          | 44.98          |
| P-Core LLC misses (random)| **12.06**   | 14.10        | **17.82**      | 16.08          |
| P-Core instructions      | 840.1        | 842.8        | 843.4          | 838.4          |

**Random read:** t=32 wins by a tiny margin. Since the node footprint is smaller, more nodes can comfortably fit into the L2 cache at the same time.

**Range scan:** t=64 is the clear winner here (about 22% faster for BTree). At t=64, BTree internal nodes are up to 127 keys wide. That means the tree can serve a lot more sequential elements per node visit, slashing the routing overhead.

**The honest answer:** Neither t=32 nor t=64 is strictly "better." **If you're doing a lot of random point queries, stick to t=32. If you're doing range scans or mixed workloads, t=64 is probably your best bet.**

### LLC Misses Per Random Read Operation (P-Core)

| Degree | BTree LLC Misses | BPlusTree LLC Misses |
|:------:|:----------------:|:--------------------:|
| t=4    | 12.72            | 20.31                |
| t=8    | 11.52            | 17.01                |
| t=16   | 11.56            | 15.64                |
| **t=32** | **12.06**      | **17.82**            |
| t=64   | 14.10            | 16.08                |
| t=128  | 16.74            | 18.44                |
| t=200  | 19.55            | 20.31                |
| t=512  | **28.01**        | **28.72**            |

**Key insight:** Cache misses spike when you use extremely high degrees. At t=512, the key array alone is 4KB — which is usually too big for the L1 cache to prefetch efficiently. Even though the tree is incredibly shallow (only 2 levels tall), every single node access floods the cache, causing misses to almost double compared to t=32.

**The sweet spot is usually around t=16 to t=64.** If you go wider than t=128, you end up evicting useful data from the fast L1 cache before the CPU is done with it.

---

## 3. The Latency Plateau

### Random Read Latency — All Degrees (ns/op, 1M elements)

| Degree | BTree (ns/op) | BPlusTree (ns/op) |
|:------:|:-------------:|:-----------------:|
| t=4    | 616.9         | 653.8             |
| t=8    | 533.7         | 565.0             |
| t=16   | 515.0         | 532.1             |
| **t=32**   | **500.1** | **519.0**         |
| t=64   | 509.4         | 528.5             |
| t=100  | 513.0         | 507.5             |
| t=128  | 508.7         | 522.0             |
| t=200  | 513.5         | 515.5             |
| t=512  | 516.8         | 511.8             |

The plateau from **t=32 to t=512 spans only 17 ns/op** — a 3.4% performance band. Within this band, the choice of degree has negligible impact on random read latency. The critical choice is getting **above t=32** to exit the height-dominated regime. What you choose above t=32 is tuning, not architecture.

---

## 4. Range Scan — Where Degree Choice Matters More

For range scans, the degree choice interacts with a different hardware constraint: **memory bandwidth**.

### Range Scan Latency (ns/op, 1M elements)

| Degree | BPlusTree | BTree    |
|:------:|:---------:|:--------:|
| t=4    | 508,106   | —        |
| t=32   | 334,197   | 474,222  |
| t=64   | 329,611   | 368,055  |
| t=100  | 324,136   | 397,852  |
| t=128  | 325,116   | 363,765  |
| t=200  | **316,370** | 698,127 |
| t=512  | **312,585** | 599,736 |

**BPlusTree range scans continue improving beyond t=32 all the way to t=512.** Why? Because BPlusTree range traversal is purely horizontal (leaf-chain pointer following) — increasing the degree packs more keys per leaf, so each cache line fetched yields more sorted values before the next pointer chase. This is a pure memory bandwidth win.

**BTree range scans spike at t=200 and t=512 (698K and 600K ns/op)** because BTree must navigate internal nodes during traversal. At t=200, each internal node is 1.6 KB — spanning 25 cache lines. Navigating those nodes for every range step floods L1 and causes massive cache evictions.

---

## 5. GC Allocation — Degree vs. Object Header Tax

From `narygc.csv` (bulk load of 5M elements):

| Degree | Tree     | Allocation (Bytes/Op) | GC Count | Construction (ms) |
|:------:|:--------:|:---------------------:|:--------:|:-----------------:|
| t=4    | BPlusTree| 142,511,030           | 10       | 5,207             |
| t=32   | BPlusTree| 37,475,302            | 1        | 3,801             |
| t=128  | BPlusTree| 31,777,971            | 1        | 3,843             |

At t=4, millions of tiny 7-element `Object[]` arrays are allocated. Each carries a **16-byte JVM object header** — massive per-element overhead. At t=32, nodes are 63 elements wide: the object header tax drops to 16B / 63 = **0.25 bytes per element** compared to 16B / 7 = **2.28 bytes per element** at t=4. That is a 9× reduction in header overhead, directly reflected in the 73% drop in GC allocation bytes.

---

## 6. Practical Degree Selection Guide

| Workload                             | Recommended Degree | Reason                                                              |
|--------------------------------------|--------------------|---------------------------------------------------------------------|
| General purpose (default)            | **t=32 or t=64**   | Both exit the height-dominated regime. t=32 lower LLC pressure; t=64 better range scans |
| Read-heavy, point queries            | **t=32**           | Lowest LLC misses at this height. Fewer evictions from L1/L2       |
| Read-heavy, RAM-fitting dataset      | **t=32 – t=64**    | L1 sweet spot — plateau maximum, LLC misses still low              |
| Range scan dominant (BPlusTree only) | **t=100 – t=200**  | Leaf density maximizes keys per cache line during horizontal traversal |
| Range scan dominant (BTree)          | **t=64**           | t=64 is 22% faster than t=32 for BTree range scans due to wider internal nodes |
| Bulk construction + GC sensitive     | **t=32 – t=128**   | 73% less GC allocation vs t=4, single GC cycle during construction  |
| Extremely large dataset (>100M)      | **t=128**          | Reduces tree height to 3 levels, minimizing LLC pressure at scale   |
| Avoid                                | **t < 8**          | Height-dominated — L1 loads 2× higher, tree behaves like BST        |
| Avoid (BTree range scan)             | **t > 128**        | Internal node scan floods L1, range latency spikes dramatically      |

> **Rule of thumb:** If you are unsure, use **t=32** for BTree and **t=100** for BPlusTree range-intensive workloads. These are the values used in all official ChaosTree benchmarks.

---

> Related: [Benchmark.md](Benchmark.md) · [Complexity.md](Complexity.md) · [Limits.md](Limits.md) · [ADR-006](../ADR/ADR-006-Object-Array-over-ArrayList.md)

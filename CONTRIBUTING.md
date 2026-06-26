# Contributing to ChaosTree

Thanks for your interest in ChaosTree! We're excited to have you here. This document outlines how we build things, what kind of help we're looking for, and how you can get your code merged smoothly.

---

## Project Philosophy

ChaosTree is built from first principles:

- **Zero external dependencies** — no third-party libraries, no wrappers
- **Every decision is evidence-backed** — performance claims require benchmark data or hardware counter evidence
- **Architecture is documented** — significant decisions have ADRs in `Docs/ADR/`

We stick to these principles pretty strictly. If a pull request conflicts with them, we'll likely have to ask for changes or respectfully close it.

---

## What We Need Help With

Because the core mathematical architecture of ChaosTree is considered feature-complete and highly sensitive, we are strictly limiting outside contributions to the following areas:

- Bug reports with a clear reproduction case
- Documentation corrections, expansions, or improvements
- Benchmark results and profiling data on different hardware architectures
- Typo fixes in Javadoc or markdown files

---

## Things We Do Not Accept

Please save your valuable time! We will respectfully close PRs that attempt to introduce the following:

- **Any core architectural changes** to the `binary/`, `nary/`, or `core/` packages
- New tree algorithms or data structures
- External dependencies of any kind
- Implementations copied from textbooks or other libraries
- Performance claims without benchmark evidence
- API changes or modifications to existing interfaces

---

## Before Opening a Pull Request

For **bug fixes, documentation, or benchmark results**: feel free to open a PR directly.

For **anything involving Java code changes**: please open an issue first. Be aware that unless it is a mathematically proven bug fix, PRs modifying the core tree engines will not be merged. We want to avoid you spending hours writing code that we cannot accept!

---

## Setup

**Requirements:**
- JDK 17 minimum
- Maven 3.8+

**Run tests:**
```bash
mvn clean test
```

Please make sure all tests pass on JDK 21 before opening a PR. We also verify the project against JDK 17, 21, 25, and 26. Regressions on any supported JDK are treated as bugs, so keeping the build green is important.

Performance benchmarks are maintained separately. Because different subsystems use different JMH configurations and profiling methodologies (GC, Linux `perf`, allocation profiling, etc.), there is no single benchmark command contributors are expected to run. If your pull request makes a performance claim, please describe how it was measured and include the relevant benchmark output.

---

## How We Write Code

- No `var` — explicit types only
- Package structure: follow existing `binary/` and `nary/` organization
- Keep internal classes hidden — users shouldn't be able to touch the `core/` package directly
- Conventional commits: `fix:`, `feat:`, `docs:`, `test:`, `perf:`
- New public API requires Javadoc with `@since` tag
- New functionality requires tests; new performance claims require benchmark evidence

---

## ADR Reference

The `Docs/ADR/` directory contains the architectural history of the project. These are for historical context and educational purposes. **We are not currently accepting PRs that attempt to rewrite or alter these established decisions.**

|                              ADR                              | Title                                                                    |
|:-------------------------------------------------------------:|:-------------------------------------------------------------------------|
|         **[ADR-001](docs/ADR/ADR-001-DeleteResult-Record.md)**         | `DeleteResult` Record over `boolean[]`                                   |
|          **[ADR-002](docs/ADR/ADR-002-modCount-as-long.md)**           | Modification Count (`modCount`) as `long`                                |
|            **[ADR-003](docs/ADR/ADR-003-CRTP-Pattern.md)**             | CRTP Pattern (`BiNode<T, N extends BiNode<T,N>>`)                        |
|          **[ADR-004](docs/ADR/ADR-004-volatile-Retained.md)**          | `volatile` Retained in `AbstractBiTree` (Rejected in `AbstractNaryTree`) |
|         **[ADR-005](docs/ADR/ADR-005-Leaf-children-null.md)**          | Leaf `children = null` (N-ary Node Memory Paradox)                       |
|     **[ADR-006](docs/ADR/ADR-006-Object-Array-over-ArrayList.md)**     | `Object[]` over `ArrayList` in N-ary Engine                              |
| **[ADR-007](docs/ADR/ADR-007-Rejecting-the-FlatTree-Architecture.md)** | `flatTree` Retired                                                       |
|  **[ADR-008](docs/ADR/ADR-008-Stream-Traversal-API-Segregation.md)**   | Stream & Traversal API Segregation                                       |
|    **[ADR-009](docs/ADR/ADR-009-BiNode-vs-ParentBiNode-Split.md)**     | `BiNode` vs `ParentBiNode` Split (Parent Pointer Separation)             |
|    **[ADR-010](docs/ADR/ADR-010-SearchResult-NodeSearchResult.md)**    | `SearchResult` & `NodeSearchResult` — Single-Pass Traversal Records      |
|        **[ADR-011](docs/ADR/ADR-011-Value-Store-over-Map.md)**         | Value-Store vs Key-Value Map API                                         |

---

## Reporting Bugs

Open a GitHub issue with:

1. JDK version and OS
2. Minimal reproduction case
3. Expected behaviour vs actual behaviour
4. Stack trace if applicable

For performance regressions, include JMH output.

---

## Hardware Benchmark Contributions

If you run the benchmark suite on different hardware and want to contribute results:

- Include full environment: CPU, RAM, JDK version, OS, heap flags
- Raw JMH output preferred over summarized numbers
- LinuxPerfNormProfiler output is welcome if available

These contributions are incredibly helpful to us because they validate that ChaosTree's performance characteristics hold up across different architectures!

---

*ChaosTree prioritizes correctness first, performance second, API consistency third.*
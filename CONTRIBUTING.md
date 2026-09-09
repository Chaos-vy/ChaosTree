# Contributing to ChaosTree

Thanks for your interest in ChaosTree! I'm excited to have you here. This document outlines how I build things, what
kind of help I'm looking for, and how you can get your code merged smoothly.

---

## Project rules

ChaosTree is built on zero dependency:

- **Zero external dependencies** — no third-party libraries, no wrappers, no IntelliJ annotations are used here.
- **Every decision is evidence-backed** — performance claims require benchmark data or hardware counter evidence - it's
  tiring ofcourse but for a library I do need the benchmark. you just need the benchmark to be done of specific change
  old and new result must be pasted then it will taken into consideration.
- **Architecture is documented** — significant decisions have ADRs - (These are old parts of chaos tree) I am currently
  in progress to buildup new ADR thanks for your patience.

I stick to these principles pretty strictly. If a pull request conflicts with them, I'll likely have to ask for changes
or respectfully close it.

---

## What I Need Help With

Because the core mathematical architecture of ChaosTree is considered feature-complete and highly sensitive, I am
strictly limiting outside contributions to the following areas:

- **Bugs from buildFromSorted() iterative load** is currently top most priority I do hold, for that only minimum degree I moved to >=3.
- Bug reports with a clear reproduction case
- Documentation corrections, expansions, or improvements
- Benchmark results and profiling data on different hardware architectures
- Typo fixes in Javadoc or markdown files (This one need a proper discussion currently in 2.0.0, I am not publishing
  docs) there is explicit html table for API drafted.

---

## Things I Do Not Accept But can be a point of Discussion

Please save your valuable time! I will respectfully close PRs that attempt to introduce the following:

- **Any core architectural changes** to the buildFromSorted/Matrix () fn, discussion can be done ;).
- New tree algorithms or data structures not accepted now.
- External dependencies of any kind - never!
- Performance claims without benchmark evidence -most important (**Reason** I also did benchmark so I found out many old fn so I override them to make to single pass or optimized one)
- API changes or modifications to existing interfaces can be discussed.

---

## Before Opening a Pull Request

For **bug fixes, documentation, or benchmark results**: feel free to open a PR directly.

For **anything involving Java code changes**: please open an issue first. Be aware that unless it is a mathematically
proven bug fix, PRs modifying the core tree engines will not be merged. I want to avoid you spending hours writing code
that I cannot accept! yes It kills devs time please do think of this lone dev :(
**Important** It is highly needed that you provide the stack trace :( Before I literally know eht the problem is **AIOOBE, NPE, CCE or any other Exception**

---

## Setup

**Requirements:**

- JDK 21 minimum
- Maven 3.8+

**Run tests:**

```bash
mvn clean test
```

Please make sure all tests pass on JDK 21 before opening a PR. I also verify the project against JDK 21, 25. Regressions
on any supported JDK are treated as bugs, so keeping the build green is important. The test cases are GuavaTestLib and
myCustom PBT

Performance benchmarks are maintained separately. Because different subsystems use different JMH configurations and
profiling methodologies (GC, Linux `perf`, allocation profiling, etc.), there is no single benchmark command
contributors are expected to run. If your pull request makes a performance claim, please describe how it was measured
and include the relevant benchmark output. If you feel you can end up writing a good benchmark please dev come your PR
is welcome but can you explain me I am also studying :)

---

This time I am making my second time simple HTML and Css Github page to put ADR and documentation hope you all like it.
I don't need a AI to devlop it. It's not because of any reason it's just that I am able to maintain it that's why.
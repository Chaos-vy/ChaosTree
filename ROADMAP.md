# ChaosTree Roadmap

## Origin

I started ChaosTree while studying DSA — not to build a library, but to
actually understand what I was reading.

Textbooks give you the algorithm. They do not give you what happens when you
implement it in real Java, under real memory constraints, with a real type
system pushing back. I wanted that friction. I wanted to know why a Splay
tree splays, not just that it does. I wanted to feel the difference between
a rotation being a pointer rewire and a rotation being an array shift.

So I started writing trees. No frameworks, no shortcuts, no autocomplete
filling in the blanks. Every line was deliberate.

---

## The Grind — How the Architecture Was Forced Into Existence

### BST — The Starting Point

BST was written as a learning exercise, nothing more. A node class, a root
reference, recursive insert and search. It worked. It was also completely
standalone — its `Node` was private, its traversals were private, nothing
was designed to be shared. That was fine. There was nothing to share it with.

### AVL — Where the Redundancy Appeared

Writing AVL immediately exposed the problem. The traversal logic — `inorder`,
`preorder`, `levelOrder`, `toList()` — was identical to BST's. Copy-pasted
line for line. The recursive insert skeleton was the same structure. The
only thing genuinely new was the height tracking and the four rotation cases.

At this point the duplication was uncomfortable but ignorable. Two trees,
two files. The cost felt manageable.

It was not.

### AbstractBiTree — The First Structural Collapse

The moment a third tree became necessary, the copy-paste strategy collapsed
under its own weight. Three trees meant three copies of every traversal
method. A bug fix meant three edits. A new traversal meant three
implementations. The structure was unsustainable.

`AbstractBiTree` was not planned. It was forced. Every piece of logic that
was identical across BST and AVL was pulled upward into a shared abstract
base. Traversals lived there. `size`, `isEmpty`, `clear` lived there.
`modCount` for fail-fast iteration lived there. The concrete trees kept only
what was genuinely theirs — their balancing logic, their rotation cases,
their structural invariants.

The class signature that emerged:

```
AbstractBiTree<T, N extends BiNode<T, N>>
```

The `N` parameter — the CRTP self-referential node type — exists because
`AVL` needs `getLeft()` to return an `AVLNode`, not a raw `BiNode`. Without
it, every node access required a downcast. The type parameter enforces this
at compile time instead.

This is when BST stopped being a learning exercise and started being an
architectural baseline.

### RBT — The Parent Problem

Red-Black Tree broke the node design.

RBT's deletion algorithm requires upward traversal — from a node back to its
parent, then its grandparent. BST and AVL never needed this. Their algorithms
descend only, relying on the recursive call stack to implicitly track the
path back up.

RBT cannot do that. `fixDoubleBlack` needs to know the sibling of a node.
Finding a sibling requires knowing the parent. There is no call-stack trick
that substitutes for an explicit parent reference at arbitrary points in a
non-recursive deletion fixup.

This forced a split in the node hierarchy:

- `BiNode<T, N>` — no parent pointer. Used by BST, AVL, Treap.
- `ParentBiNode<T, N>` — carries an explicit parent reference. Used by RBT
  and Splay.

The split is not aesthetic. AVL and Treap pay zero memory cost for a pointer
they will never use. RBT and Splay pay for exactly what they need.

Node sizes confirmed via GC profiling (`-prof gc`):

| Tree          | Bytes/node |
|---------------|------------|
| BST           | 24         |
| AVL           | 32         |
| RBT           | 32         |
| Splay         | 32         |
| Treap         | 32         |
| TreeSet (ref) | 40         |

The 8-byte gap between BST (24) and AVL/RBT/Splay (32) is the object header
plus the height or color field. Treap carries an additional 8 bytes for the
priority field. Every byte is accounted for.

### Splay and Treap — Closing the Family

Splay slotted into `ParentBiNode` — its zig-zig and zig-zag rotations
require parent awareness identical to RBT's fixup traversals.

Treap slotted into `BiNode` — priority-driven rotations descend only, no
upward traversal required.

By the time all five trees were written, the hierarchy had stabilized:

```
BinaryTree<T>
└── AbstractBiTree<...>
    ├── BST<T>
    ├── AbstractRotateTree<...>
    │   ├── AVL<T>
    │   └── Treap<T>
    └── AbstractParentRotateTree<...>
        ├── RBT<T>
        └── Splay<T>
```

No layer in this hierarchy was designed upfront. Each one was extracted when
the alternative — duplication or downcast — became architecturally
unacceptable.

### Test Duplication — The Final Collapse

Five trees. Five test files. Thirty behavioral contracts each. The same
`insert_then_contains_returnsTrue`, `delete_root_treeRemainsValid`,
`inorder_yieldsSortedSequence` — copy-pasted across every file.

One contract change meant five edits. A missed edit meant silent behavioral
divergence between trees that were supposed to be equivalent.

`BinaryTreeContractTest` — an abstract JUnit base — eliminated this. Every
contract is written once. Each concrete tree subclasses the base and
inherits full verification automatically. The only methods each subclass
provides are `createTree()` and `treeType()`.

That is when the codebase stopped feeling like five trees and started feeling
like a library.

---

## 1.0.0 — First Public Release

**Status:** In progress — BinaryFamily complete, NaryFamily next.

Shipping BinaryFamily and NaryFamily together in 1.0.0 is a deliberate
architectural proof. The shared root must support both strict binary
structures and multi-way branching without leaking binary semantics upward.
Releasing both validates that contract before the API is frozen.

### BinaryFamily — Complete

| Tree  | Node Base    | Highlight                                            |
|-------|--------------|------------------------------------------------------|
| BST   | BiNode       | Baseline. No balancing. Learning reference.          |
| AVL   | BiNode       | Strictest height bound.                              |
| RBT   | ParentBiNode | Best search at scale. Lowest mixed-workload latency. |
| Splay | ParentBiNode | Amortized O(log n) via structural reads.             |
| Treap | BiNode       | Fastest search and insert under external sync.       |

### NaryFamily — Next

---

## 1.1.0 — Concurrent RBT

**Concurrent Red-Black Tree only.** No other BinaryFamily tree receives a
concurrent implementation at this version.

This is a data-driven decision, not a preference.

The `ConcurrentBenchmark` suite (8 threads, external monitor) produced:

**Search latency (ns/op):**

| Tree  | 10k   | 100k  |
|-------|-------|-------|
| AVL   | 2,406 | —     |
| RBT   | 1,462 | 2,195 |
| Treap | 2,040 | —     |

**Mixed workload at 100k (ns/op):**

| Tree  | Latency | Variance |
|-------|---------|----------|
| AVL   | ~20,000 | moderate |
| RBT   | ~17,960 | lowest   |
| Treap | ~21,000 | moderate |

RBT leads at every scale that matters.

**Why not AVL:** Cascading rotations under contention lengthen critical
sections. More rotations per mutation means longer lock hold time, directly
increasing thread contention in a fine-grained implementation.

**Why not Treap:** The rotation path cannot be determined in advance, making fine-grained lock
ordering and invariant verification substantially more complex than RBT.

**Why not Splay (permanently excluded):** Every `contains()` is a structural
write. An exclusive lock is required for every read. Meaningful read concurrency is not achievable without abandoning the core
self-adjusting behavior that defines a Splay Tree.

**Implementation:** Concurrent RBT will be implemented iteratively. Fine-grained
locking requires lock acquisition and release boundaries to be explicit and
auditable in a single stack frame. Recursive implementations acquire locks on
the call stack and release on unwind — lock-on-unwind risk under failure paths
is not acceptable. An iterative while-loop makes every lock boundary visible.

---

## 1.2.0 — Concurrent NaryFamily

---

## 2.0.0 — Spatial + Trie

---
## What Building This Actually Taught Me

Most people learn Java from tutorials, courses, or framework-driven projects. They learn the syntax, the keywords, and the APIs. They learn how to use the language. What they often do not experience is the resistance that appears when you try to build something large enough that the language itself starts pushing back.

Building ChaosTree forced me into those corners.

`volatile` is not just a keyword associated with thread safety — I learned what it actually guarantees at the memory-model level because I needed `root`, `size`, and `modCount` to have cross-thread visibility without introducing locking. That understanding came from solving a real problem rather than memorizing a definition.

`long` versus `int` for `modCount` stopped being a trivia question and became an engineering tradeoff. An `int` overflows after roughly 2.1 billion modifications. Thinking through the consequences of that limit forced me to reason about long-lived systems rather than textbook examples.

CRTP-style generics in Java — `AbstractBiTree<T, N extends BiNode<T, N>>` — is not a pattern commonly encountered in introductory material. I arrived at it because repetitive node casting became architecturally unacceptable. The type system pushed back, and I had to understand it well enough to make it work in my favor.

This is what building at the boundary of a language teaches. You stop reading the language and start reading through it — understanding why certain guarantees exist, what abstractions cost, and where those abstractions begin to break down.

ChaosTree started as a DSA exercise. It became proof that one of the deepest ways to learn a language is to build something substantial enough that the language itself begins to challenge your assumptions.

And this is only the beginning.

Concurrent Tree is next. The boundary does not stop here — it just moves.
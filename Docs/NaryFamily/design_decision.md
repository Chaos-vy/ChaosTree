Split convention: mid = keyCount() / 2
Promotes the upper-middle key on odd counts,
true middle on even counts.
Same rule for all orders, all sizes.
No special casing anywhere in NaryFamily.

---
Architectural Decision Record (ADR): The N-ary Node Memory Paradox

1. The Context & The Problem

In building a high-performance, contiguous-memory Search Tree Engine (B-Tree, B+Tree, 2-3 Tree), we faced a critical architectural paradox regarding Node design. B-Tree nodes are massive (often holding hundreds of keys). Furthermore, Leaf nodes sit at the bottom of the tree and inherently have zero children.

Because the engine requires a unified way to traverse nodes, we had to choose how to represent these nodes in memory. We faced a three-way standoff between OOP Purity, CPU Cache Speed, and Heap Memory Efficiency.

2. The Contenders (The Pain Points)

Path A: True OOP Polymorphism (The "Clean" Code)

Design: Create an NaryNode Interface. Implement it with InternalNode (holds a children array) and LeafNode (holds next/prev pointers, no children array).

The Allure: 100% memory efficient. No wasted arrays. Follows SOLID principles perfectly.

The Hidden Pain: It completely destroys CPU performance.

The CPU instruction pipeline stalls due to Virtual Method Dispatch (the JVM has to figure out which class it's looking at every time you call getChild()).

Requires ugly instanceof checks or complex Visitor patterns in the Engine.

Breaks cache locality.

Verdict: Rejected. High-performance databases cannot afford OOP overhead.

Path B: The Monolithic CRTP (The "Database" Tax)

Design: A single abstract class NaryNode using Curiously Recurring Template Pattern (CRTP). Every node gets instantiated with new Object[maxKeys] and new NaryNode[maxChildren].

The Allure: Insanely fast. No instanceof checks. Perfectly predictable CPU execution pipeline.

The Hidden Pain: Massive Memory Tax. In a B-Tree, over 50% of all nodes are leaves! If degree = 100, every single leaf node instantiates a 200-element array on the heap that it will never use. This causes severe JVM Garbage Collection thrashing and memory bloat.

Verdict: Rejected. While C++ can afford this by using flat memory pages, Java's object overhead makes this array instantiation lethal.

Path C: Pseudo-Polymorphism via Lazy Allocation (The Chosen Path)

Design: Maintain the Monolithic CRTP NaryNode structure, but pass an isLeaf boolean directly into the base constructor.

if (isLeaf) {
    this.children = null; // Zero heap allocation!
} else {
    this.children = (N[]) new NaryNode[maxChildren];
}


The Allure: The Holy Grail of trade-offs.

We keep the lightning-fast, monolithic CRTP engine. No instanceof, no virtual dispatch overhead.

The Java Heap memory tax drops to practically zero (an 8-byte null reference instead of a massive array instantiation).

Extensible: BPlusTreeNode can cleanly extend it, adding its next pointer, while inheriting the lazy-loaded array logic for free.

The Hidden Pain: Slight risk of NullPointerException if the engine code has a bug and attempts to route through a leaf node.

Verdict: Accepted.

3. The Resolution

We explicitly chose Path C: Pseudo-Polymorphism.

We have intentionally violated pure Object-Oriented design principles (by making a class hold fields it might not use) in order to achieve Systems-Level performance. By leveraging children = null, we outsmarted the Java Virtual Machine's heap allocation penalty, granting us the memory footprint of Polymorphism with the CPU speed of a Monolith.

Follow-up Note: How Massive Databases Do This

Real database storage engines (InnoDB, Postgres) take this a step further. Because they operate in C/C++, they do not use Objects at all. They allocate raw 4KB memory blocks (matching the OS disk page).

An internal node uses the bytes at the end of the page for child pointers.

A leaf node ignores child pointers and uses those exact same bytes to pack in more row data.

Our Java children = null optimization is the closest JVM equivalent to this raw memory manipulation without resorting to Unsafe ByteBuffer pointer arithmetic.
Architectural Decision Record (ADR): Stream & Traversal API Segregation

1. The Context

In the early design of the ChaosTree library, the root interface ISearchTree demanded that all tree implementations support arbitrary traversal types via Stream<T> stream(TraversalType type).

As the library expanded to include high-performance N-ary trees (specifically the B+ Tree), this design became a fatal flaw. B+ Trees contain "Ghost Routers" (deleted keys kept only for traffic routing). Running a structural traversal (like Pre-order) on a B+ Tree would yield duplicate and deleted data, violating the fundamental contract of a Search Tree.

2. The Contradiction & The Problem

We faced an API contradiction:

Option A: Keep TraversalType in ISearchTree, forcing BPlusTree to throw UnsupportedOperationException for 3 out of 4 traversal types. This violates the Liskov Substitution Principle.

Option B: Remove TraversalType entirely, crippling the BinaryTree implementations that genuinely rely on Pre/Post/Level-order traversals for serialization and visual debugging.

3. The Resolution: Interface Segregation

We resolved this by applying the Interface Segregation Principle (ISP). We defined a strict baseline contract and allowed specific tree families to extend it orthogonally based on their mathematical strengths.

The Baseline: ISearchTree<T>

Contract: Guarantees sorted data retrieval.

API: Provides a single, parameter-less default Stream<T> stream().

Implementation: Defaults to an Inorder traversal. Safe for all trees, hiding the underlying complexity (Stack vs Linked-List) from the client.

Branch 1: BinaryTree<T> extends ISearchTree<T>

Contract: Guarantees strict binary node shapes.

API: Adds Stream<T> stream(TraversalType type).

Use Case: Exposes deep structural traversals (Pre-order for serialization, Post-order for deletion, Level-order for visualization).

Branch 2: NaryTree<T> extends ISearchTree<T>

Contract: Guarantees contiguous, multi-way data blocks.

API: Adds Stream<T> rangeStream(T fromInclusive, T toExclusive).

Use Case: Exposes database-grade bounded scanning. A B+ Tree fulfills this not by traversing nodes, but by Binary Searching for fromInclusive and riding the $O(1)$ Linked-List until it hits toExclusive.

4. Verdict

The API is now perfectly consistent. A client programming against ISearchTree gets the universal guarantee of a sorted stream(). If the client specifically needs to serialize a binary shape, they cast to or require a BinaryTree. If they need database range queries, they require an NaryTree.

No exceptions are thrown. No contracts are violated. The library is mathematically pure.
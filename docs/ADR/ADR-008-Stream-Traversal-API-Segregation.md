# ADR-008: Stream & Traversal API Segregation

In the early days of ChaosTree, my root interface `ISearchTree` forced every single tree to support every kind of traversal (Pre-order, Post-order, Level-order) via `Stream<T> stream(TraversalType type)`. 

When I started building my high-performance N-ary trees (specifically the `BPlusTree`), this design suddenly became a fatal flaw. `BPlusTree`s use "Ghost Routers"—deleted keys that are kept around strictly to route traffic. If you ran a structural Pre-order traversal on a `BPlusTree`, it would spit out all these deleted ghost keys and duplicate routing data. It completely violated the fundamental contract of what a Search Tree is supposed to do.

I fixed this by leaning hard into the **Interface Segregation Principle (ISP)**. I defined a strict, absolute baseline contract in `ISearchTree`, and then allowed specific tree families to add their own specialized methods based on what they are mathematically good at.

I was basically staring down a massive API contradiction:

1. **Binary Trees** are great at intricate structural traversals.
2. **N-ary Trees** are designed for dumping massive amounts of contiguous data. Forcing them to track multi-way recursive traversal states (especially Post-order) completely destroys the cache-locality benefits that make B-Trees fast in the first place.

Here is how I broke up the API:

1. **The Baseline (`ISearchTree<T>`):** Guarantees sorted data retrieval. Provides a single, parameter-less `Stream<T> stream()` defaulting to an Inorder traversal, which is mathematically safe for all trees.
2. **Binary (`BinaryTree<T>`):** Guarantees strict binary node shapes. Adds `stream(TraversalType type)` to expose deep structural traversals (Pre-order for serialization, Post-order for deletion).

* **Pros:** Total mathematical purity. I never have to throw an `UnsupportedOperationException`, and I never violate my own data contracts.
* **Cons:** If you program against the base `ISearchTree` interface, you can't access structural traversals without explicitly downcasting to a `BinaryTree`.
* **Resolution:** I preserve the Liskov Substitution Principle perfectly. If a user just wants their data sorted, the base interface gives it to them. If they specifically need to do a structural Pre-order serialization, they should be specifically asking for a `BinaryTree` anyway.

My API stays perfectly consistent across completely different data structures, and I don't have to rely on ugly `UnsupportedOperationException` hacks.

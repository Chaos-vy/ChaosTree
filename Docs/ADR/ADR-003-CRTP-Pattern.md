# ADR-003: F-Bounded Polymorphism (CRTP) (BiNode<T, N extends BiNode<T,N>>)

Our Binary Family actually consists of five completely different tree architectures (AVL, RBT, Treap, Splay, BST). While they all share the same basic pointer mechanics (left and right children) and rotational logic (left-rotate, right-rotate), each tree needs its own specialized node class to hold algorithm-specific metadata. For instance, an `AVLNode` needs to track `height`, while an `RBTNode` needs to track `color`. 

Without F-Bounded Polymorphism, shared algorithms would have to operate on the base node type, forcing concrete implementations to repeatedly downcast returned nodes, our shared abstract rotation methods would be forced to return a generic base `BiNode`. This would force all our concrete subclasses to constantly perform ugly runtime casting.

**Decision:**  
We implemented F-Bounded Polymorphism (commonly known in C++ as the Curiously Recurring Template Pattern or CRTP), defining the base node as `BiNode<T, N extends BiNode<T,N>>` and the abstract tree as `AbstractBiTree<T, N extends BiNode<T, N>>`.

**Rationale:**  
Using F-Bounded Polymorphism allows our abstract superclasses to understand the exact, concrete type of the node they are operating on. When `AbstractRotateTree` performs a rotation, it takes an `N` and it returns an `N`. So when the AVL tree specifies `N` as `AVLNode`, the generic type system guarantees at compile time that a rotation operating on an `AVLNode` will cleanly return an `AVLNode`.

Without this pattern, a rotation method shared across all these trees would look like this:

```java
BiNode<T> rotateLeft(BiNode<T> node)
```

...which forces you to downcast every single time you use it.

**Tradeoffs:**  
* **Pros:** It completely eliminates the need for downcasting anywhere in our tree hierarchy. We get perfect compile-time verification of node types across all our shared rotation logic.
* **Cons:** The generic signatures become visually terrifying and highly verbose for us as framework maintainers to read and write. 
* **Resolution:** We decided that the complexity was worth it because it's completely isolated to our internal `Abstract` layers. The public API surface remains beautifully simple (`BinaryTree<String> tree = new RBT<>()`), completely masking the F-Bounded mechanics from the end-user.
* **Note:** We also apply this exact same F-Bounded pattern to the N-ary hierarchy. Even though N-ary trees don't rotate, they still benefit massively from keeping concrete node types safe during splits, merges, and traversals.

**Consequences:**  
We get high-performance, strictly type-safe rotation logic across the entire library while preserving compile-time type safety throughout the hierarchy.

### Base Node Abstractions

We could have taken the easy way out and had all shared algorithms operate on and return a base node type.

Example:

```java
BiNode<T> rotateLeft(BiNode<T> node)
```

Concrete implementations would then be forced to downcast:

```java
AVLNode<T> rotated = (AVLNode<T>) rotateLeft(node);
```

We rejected this approach because type verification would happen at runtime instead of compile time. Having to write `(AVLNode<T>)` over and over again throughout our core balancing logic just felt architecturally sloppy.

# ADR-011: Value-Store vs Key-Value Map API

ChaosTree strictly implements a value-store API (`T extends Comparable<T>`) rather than the standard Java `Map<K, V>` interface.

## Rationale
In the standard Java ecosystem, search trees are overwhelmingly used as key-value stores (e.g., `java.util.TreeMap`). However, implementing a `Map<K, V>` interface inherently requires wrapping user data into `Map.Entry<K, V>` objects or equivalent key-value tuples per node.

Every key-value entry requires an additional object (or equivalent wrapper) to hold both the key and value. On HotSpot, that object carries its own object header in addition to the key and value references.

By enforcing a pure value-store API, I eliminate the need for wrapper objects. The tree operates directly on the user's `T` references, slashing the heap footprint and maximizing the amount of contiguous data I can pack into my arrays.

This keeps the core API focused on ordered search, traversal, and range queries rather than associative mapping.
## Tradeoffs
* **Pros:** Massive reduction in JVM object header bloat, significantly higher element saturation limits, and far better hardware cache locality.
* **Cons:** It limits drop-in compatibility for users migrating from `java.util.TreeMap` who specifically need Key-Value association.

## Resolution
Users who strictly need key-value semantics can still use ChaosTree by implementing a composite `Comparable` wrapper class that holds both the key and the value, resolving equality and comparison solely via the key. While this reintroduces the wrapper object overhead, it leaves the choice in the hands of the developer rather than forcing the tax upon all users of the library.

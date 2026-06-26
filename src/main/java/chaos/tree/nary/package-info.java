/**
 * Contains high-degree N-ary search tree implementations optimized for extreme memory density and sequential access.
 * <p>
 * This package provides industry-standard data structures like B-Trees and B+ Trees.
 * These trees dramatically reduce tree height by packing multiple elements into wide arrays,
 * maximizing hardware cache-line locality (Mechanical Sympathy). The {@code BPlusTree} in
 * particular utilizes a linked-leaf design for blisteringly fast sequential range queries.
 * </p>
 */
package chaos.tree.nary;

/**
 * Defines the ChaosTree framework, providing robust and strictly encapsulated
 * implementations of binary and N-ary search trees.
 */
module chaos.tree {
    requires static org.jetbrains.annotations;

    // Core APIs and Exceptions
    exports chaos.tree.core;
    exports chaos.tree.core.searchtree;
    exports chaos.tree.exception;
    exports chaos.tree.traversal;

    exports chaos.tree.binary;

    exports chaos.tree.nary;

    /*
     * ENCAPSULATION ENFORCEMENT:
     * The following packages are explicitly hidden from module consumers.
     * This ensures no one can bypass the BinaryTree/NaryTree interfaces
     * to access the underlying engine implementations directly:
     *
     * - chaos.tree.core.searchtree.binary (Hides AbstractBiTree)
     * - chaos.tree.core.searchtree.binary.node
     * - chaos.tree.core.searchtree.binary.rotation
     * - chaos.tree.core.searchtree.nary (Hides AbstractNaryTree)
     */
}

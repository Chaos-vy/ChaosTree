package chaos.tree.traversal;

import java.util.List;

public interface Traversal<T extends Comparable<T>> {
    /**
     * Returns the tree as List in preorder format.
     * @return the tree as List in preorder
     */
     List<T> preorder();

    /**
     * Returns the tree as List in inorder format.
     * @return the tree as List in inorder
     */
     List<T> inorder();

    /**
     * Returns the tree as List in postorder format.
     * @return the tree as List in postorder
     */
     List<T> postorder();

    /**
     * Returns the tree as List in levelorder format.
     * @return the tree as List in levelorder
     */
     List<T> levelOrder();
}

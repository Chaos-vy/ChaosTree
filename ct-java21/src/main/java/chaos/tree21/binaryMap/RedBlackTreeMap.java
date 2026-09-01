package chaos.tree21.binaryMap;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

public final class RedBlackTreeMap<K, V> extends AbstractBinaryTreeMap<K, V, RbtMapNode<K, V>> {


    public RedBlackTreeMap() {
        super();
    }

    public RedBlackTreeMap(Comparator<? super K> comparator) {
        super(comparator);
    }

    public RedBlackTreeMap(Map<? extends K, ? extends V> m) {
        super();
        putAll(m);
    }

    public RedBlackTreeMap(SortedMap<K, ? extends V> m) {
        buildFromSorted(m.size(), m.entrySet().iterator());
    }

    @Override
    void afterNodeBuiltFromSorted(RbtMapNode<K, V> node, int level, int redLevel) {
        if (level == redLevel) node.setRed();
        else node.setBlack();
    }

    @Override
    RbtMapNode<K, V> createNode(K key, V value) {
        return new RbtMapNode<>(key, value);
    }

    @Override
    void afterInsert(RbtMapNode<K, V> x) {
        // I need to only care if the parent is also RED (a Red-Red violation!)
        while (x != null && x != root && x.parent.isRed()) {
            RbtMapNode<K, V> parent = x.parent;
            // Grandparent is mathematically guaranteed to exist because parent is RED (root is always black)
            RbtMapNode<K, V> grandParent = parent.parent;
            //Left Symmetry
            if (parent == grandParent.left) {
                RbtMapNode<K, V> uncle = grandParent.right;
                // Case 1: Uncle is RED (The Recolor Case)
                if (uncle != null && uncle.isRed()) {
                    parent.setBlack();
                    uncle.setBlack();
                    grandParent.setRed();
                    x = grandParent; // Push the red violation up the tree and loop again!
                } else {
                    // Case 2: Uncle is BLACK (The Triangle Case)
                    if (x == parent.right) {
                        x = parent;
                        super.rotateLeft(x);
                        parent = x.parent;
                    }
                    // Case 3: Uncle is BLACK (The Line Case)
                    parent.setBlack();
                    grandParent.setRed();
                    super.rotateRight(grandParent);
                    break;
                }
            } else {
                // Symmetrical cases for the Right side
                RbtMapNode<K, V> uncle = grandParent.left;

                if (uncle != null && uncle.isRed()) {
                    parent.setBlack();
                    uncle.setBlack();
                    grandParent.setRed();
                    x = grandParent;
                } else {
                    if (x == parent.left) {
                        x = parent;
                        super.rotateRight(x);
                        parent = x.parent;
                    }
                    parent.setBlack();
                    grandParent.setRed();
                    super.rotateLeft(grandParent);
                    break;
                }
            }
        }
        //Everything ends with this guy.
        root.setBlack();
    }

    private boolean isBlack(RbtMapNode<K, V> node) {
        return node == null || node.isBlack();
    }

    private boolean isRed(RbtMapNode<K, V> node) {
        return node != null && node.isRed();
    }

    @Override
    public V remove(Object o) {
        if (isEmpty()) return null;
        @SuppressWarnings("unchecked")
        K key = (K) o;

        RbtMapNode<K, V> x = nodeFinder(key);
        if (x == null) return null; //No key, no operation LOL it's look like a dialogue.
        V oldValue = x.getValue();
        if (x.left != null && x.right != null) {
            RbtMapNode<K, V> successor = x.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            x.setPair(successor.getKey(), successor.getValue());
            x = successor;
        }
        RbtMapNode<K, V> node_replacer = x.left != null ? x.left : x.right;
        boolean deletedNodeWasBlack = x.isBlack();
        if (node_replacer != null) {
            node_replacer.parent = x.parent;
            if (x.parent == null) {
                root = node_replacer;
            } else if (x == x.parent.left) {
                x.parent.left = node_replacer;
            } else {
                x.parent.right = node_replacer;
            }
            if (deletedNodeWasBlack) {
                fixDoubleBlack(node_replacer);
            }
        } else if (x.parent == null) {
            root = null; // The tree is now empty
        } else {
            // Leaf Node Deletion: We must fix the black weight BEFORE unlinking!
            if (deletedNodeWasBlack) {
                fixDoubleBlack(x);
            }

            if (x == x.parent.left) {
                x.parent.left = null;
            } else {
                x.parent.right = null;
            }
            x.parent = null;
        }

        // Clearing GC references for Iterator stability
        x.left = null;
        x.right = null;
        x.parent = null;

        size--;
        modCount++;
        return oldValue;
    }

    private void fixDoubleBlack(RbtMapNode<K, V> x) {
        // Bubble the "Phantom Black" weight up until we hit a Red node or the Root
        while (x != root && isBlack(x)) {
            RbtMapNode<K, V> parent = x.parent;

            if (x == parent.left) {
                RbtMapNode<K, V> sibling = parent.right;

                // Case 1: Sibling is RED
                // We rotate to force the sibling to be BLACK, which pushes us into Case 2, 3, or 4
                if (isRed(sibling)) {
                    sibling.setBlack();
                    parent.setRed();
                    super.rotateLeft(parent);
                    sibling = parent.right; // Update sibling after rotation
                }

                // Case 2: Both of the sibling's children (nephews) are BLACK
                if (isBlack(sibling.left) && isBlack(sibling.right)) {
                    sibling.setRed();
                    x = parent; // Push the Double-Black weight up to the parent!
                } else {
                    // Case 3: Sibling is BLACK, Right nephew is BLACK (Left nephew is RED)
                    if (isBlack(sibling.right)) {
                        if (sibling.left != null) sibling.left.setBlack();
                        sibling.setRed();
                        super.rotateRight(sibling);
                        sibling = parent.right; // Update sibling
                    }

                    // Case 4: Sibling is BLACK, Right nephew is RED
                    // This is the TERMINAL CASE. We need to fix the tree and instantly BREAK!
                    if (parent.isRed()) sibling.setRed();
                    else sibling.setBlack();

                    parent.setBlack();
                    if (sibling.right != null) sibling.right.setBlack();
                    super.rotateLeft(parent);

                    break; // EARLY EXIT!
                }
            } else {
                // Symmetrical cases for when 'x' is the Right child
                RbtMapNode<K, V> sibling = parent.left;

                if (isRed(sibling)) {
                    sibling.setBlack();
                    parent.setRed();
                    super.rotateRight(parent);
                    sibling = parent.left;
                }

                if (isBlack(sibling.right) && isBlack(sibling.left)) {
                    sibling.setRed();
                    x = parent;
                } else {
                    if (isBlack(sibling.left)) {
                        if (sibling.right != null) sibling.right.setBlack();
                        sibling.setRed();
                        super.rotateLeft(sibling);
                        sibling = parent.left;
                    }

                    if (parent.isRed()) sibling.setRed();
                    else sibling.setBlack();

                    parent.setBlack();
                    if (sibling.left != null) sibling.left.setBlack();
                    super.rotateRight(parent);

                    break; // EARLY EXIT!
                }
            }
        }
        if (x != null) {
            x.setBlack();
        }
    }
}

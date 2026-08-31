package chaos.tree21.binary;

import java.util.*;

public final class RedBlackTreeSet<E> extends AbstractBinaryTreeSet<E, RbtNode<E>> {

    public RedBlackTreeSet() {
        super();
    }
    public RedBlackTreeSet(Comparator<? super E> comparator) {
        super(comparator);
    }

    public RedBlackTreeSet(Collection<? extends E> m) {
        super();
        addAll(m);
    }
    public RedBlackTreeSet(SortedSet<? extends E> s) {
        buildFromSorted(s.size(), s.iterator());
    }

    @Override
    protected void afterNodeBuiltFromSorted(RbtNode<E> node, int level, int redLevel) {
        if (level == redLevel) node.setRed();
        else node.setBlack();
    }

    @Override
    protected RbtNode<E> createNode(E val) {
        return new RbtNode<>(val);
    }

    /*
     * <ol>
     *     <li>The root is BLACK</li>
     *     <li>Node can be either BLACK or RED</li>
     *     <li>All leaf node must be null/Black</li>
     *     <li>A RED node cannot have a red children</li>
     *     <li>Every path from a node to any descendant leaves contains the same no of BLACK node</li>
     * </ol>
     */
    @Override
    public boolean add(E val) {
        if (root == null) {
            compare(val, val);
            root = new RbtNode<>(val);
            root.setBlack();//A RBT rule must be Obeyed So I put the definition of RBT for not to forget.
            size++;
            cachedHashcode += val.hashCode();
            modCount++;
            return true;
        }
        RbtNode<E> p = null;
        RbtNode<E> curr = root;
        int cmp = 0;
        while (curr != null) {
            p = curr;
            cmp = compare(val, curr.value);
            if (cmp == 0) return false;
            else if (cmp < 0) curr = curr.left;
            else curr = curr.right;
        }
        //Remember every node in RBT I made its default color is RED... true.... RED.....
        RbtNode<E> newNode = new RbtNode<>(val);
        newNode.parent = p;
        if (cmp < 0) {
            p.left = newNode;
        } else {
            p.right = newNode;
        }
        fix_Up_from_bottom_Insertion(newNode);
        size++;
        modCount++;
        cachedHashcode += val.hashCode();
        return true;
    }

    private void fix_Up_from_bottom_Insertion(RbtNode<E> x) {
        // I need to only care if the parent is also RED (a Red-Red violation!)
        while (x != null && x != root && x.parent.isRed()) {
            RbtNode<E> parent = x.parent;
            // Grandparent is mathematically guaranteed to exist because parent is RED (root is always black)
            RbtNode<E> grandParent = parent.parent;
            //Left Symmetry
            if (parent == grandParent.left) {
                RbtNode<E> uncle = grandParent.right;
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
                RbtNode<E> uncle = grandParent.left;

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

    @Override
    public boolean remove(Object o) {
        //I am approaching the same way as of AVL tree delete. Reference: CLRS or take the AVL tree.
        if (isEmpty()) return false;
        @SuppressWarnings("unchecked")
        E val = (E) o;
        RbtNode<E> x = nodeFinder(val);
        if (x == null) return false;

        if (x.left != null && x.right != null) {
            RbtNode<E> successor = x.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            x.value = successor.value;
            x = successor;
        }

        //Guaranteed one child or none
        RbtNode<E> node_replacer = x.left != null ? x.left : x.right;
        boolean deletedNodeWasBlack = x.isBlack(); //This must be stored.

        if (node_replacer != null) {
            node_replacer.parent = x.parent;
            if (x.parent == null) {
                root = node_replacer;
            } else if (x == x.parent.left) {
                x.parent.left = node_replacer;
            } else {
                x.parent.right = node_replacer;
            }

            // If the deleted node was Black, the tree lost a black weight. Fix it!
            if (deletedNodeWasBlack) {
                fixDoubleBlack(node_replacer);
            }
        } else if (x.parent == null) {
            root = null; // The tree is now empty
        } else {
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

        //just clearing GC but do I need let me guess
        x.left = null; //since I already removed all attachment to x to reach to x
        x.right = null; //JVM GC is smart enough it will collect in GC
        x.parent = null; //Don't think JVM won't do. it will, even though it has reference
        //L,R,P becoz x has become part of garbage.
        /*
        The lesson I got here we need to do because of iterator Stability
        */
        size--;
        modCount++;
        cachedHashcode -= val.hashCode();
        return true;
    }

    private boolean isBlack(RbtNode<E> node) {
        return node == null || node.isBlack();
    }

    private boolean isRed(RbtNode<E> node) {
        return node != null && node.isRed();
    }

    private void fixDoubleBlack(RbtNode<E> x) {
        // Bubble the "Phantom Black" weight up until we hit a Red node or the Root
        while (x != root && isBlack(x)) {
            RbtNode<E> parent = x.parent;

            if (x == parent.left) {
                RbtNode<E> sibling = parent.right;

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
                RbtNode<E> sibling = parent.left;

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

    /**
     * Internal mathematical debugger. Validates all Red-Black structural invariants.
     * Throws IllegalStateException if any rule is broken.
     * These test are made to make endure the tree remains stable
     */
    void verifyInvariants() {
        if (root == null) return;
        if (root.isRed()) throw new IllegalStateException("RBT Corruption: Root is RED");
        validateRbtRules(root, null, null);
    }

    private int validateRbtRules(RbtNode<E> node, E min, E max) {
        if (node == null) return 1; // Null leaves have black height of 1

        // Rule 1: Binary Search Tree Property
        if (min != null && compare(node.value, min) <= 0) {
            throw new IllegalStateException("RBT Corruption: BST Violation. Node " + node.value + " is <= min bound " + min);
        }
        if (max != null && compare(node.value, max) >= 0) {
            throw new IllegalStateException("RBT Corruption: BST Violation. Node " + node.value + " is >= max bound " + max);
        }

        // Rule 2: Double Red Violation
        if (node.isRed()) {
            if (node.left != null && node.left.isRed()) {
                throw new IllegalStateException("RBT Corruption: Double Red! Node " + node.value + " and Left Child " + node.left.value + " are both RED.");
            }
            if (node.right != null && node.right.isRed()) {
                throw new IllegalStateException("RBT Corruption: Double Red! Node " + node.value + " and Right Child " + node.right.value + " are both RED.");
            }
        }

        int leftBlackHeight = validateRbtRules(node.left, min, node.value);
        int rightBlackHeight = validateRbtRules(node.right, node.value, max);

        // Rule 3: Perfect Black Height
        if (leftBlackHeight != rightBlackHeight) {
            throw new IllegalStateException("RBT Corruption: Black Height Mismatch at Node " + node.value +
                    "! Left BH=" + leftBlackHeight + ", Right BH=" + rightBlackHeight);
        }

        return leftBlackHeight + (node.isRed() ? 0 : 1);
    }

}

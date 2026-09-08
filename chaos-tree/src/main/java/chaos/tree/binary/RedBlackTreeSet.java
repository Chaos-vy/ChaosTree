package chaos.tree.binary;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;

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
    void afterNodeBuiltFromSorted(RbtNode<E> node, int level, int redLevel) {
        if (level == redLevel) node.setRed();
        else node.setBlack();
    }

    @Override
    RbtNode<E> createNode(E val) {
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
            root.setBlack();
            size++;
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
        RbtNode<E> newNode = new RbtNode<>(val);
        newNode.parent = p;
        if (cmp < 0) {
            p.left = newNode;
        } else {
            p.right = newNode;
        }
        fixUpFromBottom_Insertion(newNode);
        size++;
        modCount++;
        return true;
    }

    private void fixUpFromBottom_Insertion(RbtNode<E> x) {
        while (x != null && x != root && x.parent.isRed()) {
            RbtNode<E> parent = x.parent;
            RbtNode<E> grandParent = parent.parent;
            if (parent == grandParent.left) {
                RbtNode<E> uncle = grandParent.right;
                if (uncle != null && uncle.isRed()) {
                    parent.setBlack();
                    uncle.setBlack();
                    grandParent.setRed();
                    x = grandParent;
                } else {
                    if (x == parent.right) {
                        x = parent;
                        super.rotateLeft(x);
                        parent = x.parent;
                    }
                    parent.setBlack();
                    grandParent.setRed();
                    super.rotateRight(grandParent);
                    break;
                }
            } else {
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
        root.setBlack();
    }

    @Override
    public boolean remove(Object o) {
        if (root == null || o == null) return false;
        try {
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

            RbtNode<E> nodeReplacer = x.left != null ? x.left : x.right;
            boolean deletedNodeWasBlack = x.isBlack(); //This must be stored.

            if (nodeReplacer != null) {
                nodeReplacer.parent = x.parent;
                if (x.parent == null) {
                    root = nodeReplacer;
                } else if (x == x.parent.left) {
                    x.parent.left = nodeReplacer;
                } else {
                    x.parent.right = nodeReplacer;
                }

                if (deletedNodeWasBlack) {
                    fixDoubleBlack(nodeReplacer);
                }
            } else if (x.parent == null) {
                root = null;
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

            
            x.left = null; 
            x.right = null; 
            x.parent = null; 

            size--;
            modCount++;
            return true;
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }

    private boolean isBlack(RbtNode<E> node) {
        return node == null || node.isBlack();
    }

    private boolean isRed(RbtNode<E> node) {
        return node != null && node.isRed();
    }

    private void fixDoubleBlack(RbtNode<E> x) {
        while (x != root && isBlack(x)) {
            RbtNode<E> parent = x.parent;

            if (x == parent.left) {
                RbtNode<E> sibling = parent.right;

                if (isRed(sibling)) {
                    sibling.setBlack();
                    parent.setRed();
                    super.rotateLeft(parent);
                    sibling = parent.right;
                }

                if (isBlack(sibling.left) && isBlack(sibling.right)) {
                    sibling.setRed();
                    x = parent;
                } else {
                    if (isBlack(sibling.right)) {
                        if (sibling.left != null) sibling.left.setBlack();
                        sibling.setRed();
                        super.rotateRight(sibling);
                        sibling = parent.right; // Update sibling
                    }

                    if (parent.isRed()) sibling.setRed();
                    else sibling.setBlack();

                    parent.setBlack();
                    if (sibling.right != null) sibling.right.setBlack();
                    super.rotateLeft(parent);

                    break;
                }
            } else {
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

                    break;
                }
            }
        }
        if (x != null) {
            x.setBlack();
        }
    }
}

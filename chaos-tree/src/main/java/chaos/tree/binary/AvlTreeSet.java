package chaos.tree.binary;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;

public final class AvlTreeSet<E> extends AbstractBinaryTreeSet<E, AvlNode<E>> {

    public AvlTreeSet() {
        super();
    }

    public AvlTreeSet(Comparator<? super E> comparator) {
        super(comparator);
    }

    public AvlTreeSet(Collection<? extends E> m) {
        super();
        addAll(m);
    }

    public AvlTreeSet(SortedSet<? extends E> s) {
        buildFromSorted(s.size(), s.iterator());
    }

    @Override
    void afterNodeBuiltFromSorted(AvlNode<E> node, int level, int redLevel) {
        int leftHeight = nodeHeight(node.left);
        int rightHeight = nodeHeight(node.right);
        node.height = Math.max(leftHeight, rightHeight) + 1;
    }

    @Override
    AvlNode<E> createNode(E val) {
        return new AvlNode<>(val);
    }

    @Override
    public boolean add(E val) {
        if (root == null) {
            compare(val, val); // JDK Semantic: Type (and possibly null) check!
            root = new AvlNode<>(val);
            size++;
            modCount++;
            return true;
        }
        AvlNode<E> p = null;
        AvlNode<E> curr = root;
        int cmp = 0;
        while (curr != null) {
            p = curr;
            cmp = compare(val, curr.value);
            if (cmp == 0) {
                return false;
            } else if (cmp < 0) {
                curr = curr.left;
            } else {
                curr = curr.right;
            }
        }
        AvlNode<E> newNode = new AvlNode<>(val);
        newNode.parent = p;
        if (cmp < 0) {
            p.left = newNode;
        } else {
            p.right = newNode;
        }
        fix_Up_from_bottom(p);
        size++;
        modCount++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (isEmpty() || o == null) return false;
        try {
            @SuppressWarnings("unchecked")
            E val = (E) o;
            AvlNode<E> x = nodeFinder(val);
            if (x == null) return false;
            //Just delete from here no tension haha LOL I feel like playing here node very much fun.
            //Just to be aware of optimization I need to add comment
            if (x.right != null && x.left != null) {
                AvlNode<E> successor = x.right; // Yes I do delete by successor method
                while (successor.left != null) { //because it's optimized for iterator purpose
                    successor = successor.left;
                }
                x.value = successor.value;
                x = successor; //Now guaranteed this little node in the tree will be alone to one or none hehe LOL
            }
        /*
          I need to remember this of little chaos cases in future use.
          1: if all L and R are null
          2: if L is null -> part of right node to be attached and markup of parent!
          3: if R is null -> part of left node to be attached and markup of parent!
         */
            AvlNode<E> replacement = x.left != null ? x.left : x.right;

            //This block is for part when case 2 and case 3 falls
            if (replacement != null) {
                replacement.parent = x.parent;
                if (x.parent == null) {//most critical one
                    root = replacement;
                } else if (x == x.parent.left) {
                    x.parent.left = replacement;
                } else {
                    x.parent.right = replacement;
                }
                fix_Up_from_bottom(replacement.parent);

            } else if (x.parent == null) { // when the little node has direct reach to root
                root = null;
            } else { // when the little node is left with no L and R
                AvlNode<E> parent = x.parent;
                if (x == parent.left) {
                    parent.left = null;
                } else {
                    parent.right = null;
                }
                //Lemme think do I
                fix_Up_from_bottom(parent);
            }

            //just clearing GC but do I need let me guess
            x.left = null; //since I already removed all attachment to x to reach to x
            x.right = null; //JVM GC is smart enough it will collect in GC
            x.parent = null; //Don't think JVM won't do. it will, even though it has reference
            //L,R,P because x has become part of garbage.
            /*
            The lesson I got here we need to do because iterator Stability
             */
            size--;
            modCount++;
            return true;
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }

    }

    private void fix_Up_from_bottom(AvlNode<E> node) {
        while (node != null) {
            int oldHeight = node.height;
            updateHeight(node);
            int balance = nodeHeight(node.left) - nodeHeight(node.right);
            if (balance > 1) {
                if (nodeHeight(node.left.left) >= nodeHeight(node.left.right)) {
                    node = rotateRightAVL(node);
                } else {
                    rotateLeftAVL(node.left);
                    node = rotateRightAVL(node);
                }
            } else if (balance < -1) {
                if (nodeHeight(node.right.right) >= nodeHeight(node.right.left)) {
                    node = rotateLeftAVL(node);
                } else {
                    rotateRightAVL(node.right);
                    node = rotateLeftAVL(node);
                }
            }
            if (oldHeight == node.height) {
                break;
            }
            node = node.parent;
        }
    }

    // --- AVL-Specific Rotation Wrappers --- These are main parts.

    private AvlNode<E> rotateRightAVL(AvlNode<E> p) {
        AvlNode<E> newRoot = p.left;
        super.rotateRight(p);
        updateHeight(p);
        updateHeight(newRoot);
        return newRoot;
    }

    private AvlNode<E> rotateLeftAVL(AvlNode<E> p) {
        AvlNode<E> newRoot = p.right;
        super.rotateLeft(p);
        updateHeight(p);
        updateHeight(newRoot);
        return newRoot;
    }

    private void updateHeight(AvlNode<E> root) {
        root.height = 1 + Math.max(nodeHeight(root.left), nodeHeight(root.right));
    }

    private int nodeHeight(AvlNode<E> node) {
        return node == null ? -1 : node.height;
    }

}

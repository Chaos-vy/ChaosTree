package chaos.tree.binary.rbt;

import chaos.tree.core.binary.AbstractParentRotateTree;
import chaos.tree.exception.DuplicateNodeException;

public class RBT<T extends Comparable<T>> extends AbstractParentRotateTree<T, RBTNode<T>> {

    private static final boolean RED = false;
    private static final boolean BLACK = true;

    @Override
    protected RBTNode<T> createNode(T value) {
        return new RBTNode<>(value);
    }

    @Override
    protected String nodeText(RBTNode<T> node) {
        return node.getValue() + (node.getColor() ? "(B)" : "(R)");
    }

    private boolean isRed(RBTNode<T> node) {
        return node != null && node.getColor() == RED;
    }

    private void setColor(RBTNode<T> node, boolean color) {
        if (node != null) node.setColor(color);
    }

    @Override
    public void insert(T value) {
        root = insert(root, value);
        setColor(root, BLACK);
        size++;
    }

    @Override
    protected RBTNode<T> insert(RBTNode<T> node, T value) {
        if (node == null) {
            return createNode(value);
        }
        int cmp = compare(value, node);

        if (cmp == 0) {
            throw new DuplicateNodeException("Value already present in tree");
        }
        if (cmp > 0) {
            node.setRight(insert(node.getRight(), value));
            node.getRight().setParent(node);
        } else {
            node.setLeft(insert(node.getLeft(), value));
            node.getLeft().setParent(node);
        }
        return afterInsert(node);
    }

    private void refactorUncleRed(RBTNode<T> node) {
        setColor(node, RED);
        setColor(node.getLeft(), BLACK);
        setColor(node.getRight(), BLACK);
    }

    private void refactorUncleBlack(RBTNode<T> node, RBTNode<T> child) {
        setColor(node, BLACK);
        setColor(child, RED);
    }

    @Override
    protected RBTNode<T> afterInsert(RBTNode<T> node) {

        RBTNode<T> left = node.getLeft();
        RBTNode<T> right = node.getRight();

        if (isRed(left) && isRed(left.getLeft())) {
            if (!isRed(right)) {
                node = rightRotate(node);
                refactorUncleBlack(node, node.getRight());
            } else {
                refactorUncleRed(node);
            }
        } else if (isRed(left) && isRed(left.getRight())) {
            if (!isRed(right)) {
                node.setLeft(leftRotate(left));
                node = rightRotate(node);
                refactorUncleBlack(node, node.getRight());
            } else {
                refactorUncleRed(node);
            }
        } else if (isRed(right) && isRed(right.getRight())) {
            if (!isRed(left)) {
                node = leftRotate(node);
                refactorUncleBlack(node, node.getLeft());
            } else {
                refactorUncleRed(node);
            }
        } else if (isRed(right) && isRed(right.getLeft())) {
            if (!isRed(left)) {
                node.setRight(rightRotate(right));
                node = leftRotate(node);
                refactorUncleBlack(node, node.getLeft());
            } else {
                refactorUncleRed(node);
            }
        }
        return node;
    }

    @Override
    public void delete(T value) {
        RBTNode<T> target = findNode(root, value);
        if (target == null) {
            return;
        }
        deleteNode(target);
        size--;
    }
    private void deleteNode(RBTNode<T> node) {
        if (node.getLeft() != null && node.getRight() != null) {
            RBTNode<T> successor = getMinNode(node.getRight());
            node.setValue(successor.getValue());
            deleteNode(successor);
            return;
        }

        RBTNode<T> child = node.getLeft() != null ? node.getLeft() : node.getRight();

        if (isRed(node)) {
            rewireParent(node.getParent(), node, null);
            return;
        }

        if (isRed(child)) {
            child.setColor(BLACK);
            rewireParent(node.getParent(), node, child);
            return;
        }

        if (node == root) { root = null; return; }

        fixDoubleBlack(node);
        rewireParent(node.getParent(), node, null);
    }

    private void rewireParent(RBTNode<T> parent, RBTNode<T> node, RBTNode<T> child) {
        if (parent == null){
            root = child;
            if (child != null) child.setParent(null);
            return;
        }
        boolean isLeftNode = parent.getLeft()==node;
        if(child != null){
            child.setParent(parent);
        }
        if(isLeftNode){
            parent.setLeft(child);
        }
        else {
            parent.setRight(child);
        }
    }
    private void fixDoubleBlack(RBTNode<T> node) {

        if (node == null) return;
        if (node == root) return;

        RBTNode<T> parent = node.getParent();
        boolean isLeftChild = parent.getLeft() == node;
        RBTNode<T> sibling = isLeftChild ? parent.getRight() : parent.getLeft();
        if (sibling == null) {
            fixDoubleBlack(parent);
            return;
        }
        if (isRed(sibling)) {
            sibling.setColor(BLACK);
            parent.setColor(RED);
            if (isLeftChild) {
                leftRotate(parent);
            } else {
                rightRotate(parent);
            }
            fixDoubleBlack(node);
            return;
        }
        boolean siblingHasRedChild = isRed(sibling.getLeft()) ||isRed(sibling.getRight());
        if (!siblingHasRedChild) {
            sibling.setColor(RED);
            if (isRed(parent)) {
                parent.setColor(BLACK);
            } else {
                fixDoubleBlack(parent);
            }
            return;
        }
        if (isLeftChild) {
            if (isRed(sibling.getRight())) {
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                sibling.getRight().setColor(BLACK);
                leftRotate(parent);
            }
            else {
                sibling.getLeft().setColor(BLACK);

                rightRotate(sibling);

                sibling = parent.getRight();
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                leftRotate(parent);
            }
        } else {
            if (isRed(sibling.getLeft())) {
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                sibling.getLeft().setColor(BLACK);
                rightRotate(parent);
            }
            else {
                sibling.getRight().setColor(BLACK);
                leftRotate(sibling);
                sibling = parent.getLeft();
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                rightRotate(parent);
            }
        }
    }

}

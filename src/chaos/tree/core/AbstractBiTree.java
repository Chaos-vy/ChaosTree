package chaos.tree.core;

import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.exception.EmptyTreeException;

import java.util.*;

public abstract class AbstractBiTree<T extends Comparable<T>,N extends BiNode<T,N>> implements ITree<T> {

    /**
     * Root of the tree
     */
    protected N root;

    /**
     * Total element in tree
     */
    protected int size;

    /**
     *Construct an empty Binary tree
     */
    protected AbstractBiTree(){}

    /**
     * Creates a new node with the specified value.
     *
     * @param value the value to store in the node
     * @return the newly created node
     */
    protected abstract N createNode(T value);

    /**
     * Does the update of metadata of respective tree after insert
     * @param root the root associated to tree
     * @return the root of tree
     */
    protected N afterInsert(N root){
        return root;
    }

    /**
     * Does the update of metadata of respective tree after delete
     * @param root the root associated to tree
     * @return the root of tree
     */
    protected N afterDelete(N root) {
        return root;
    }

    public void insertAll(List<T> values) {
        values.forEach(this::insert);
    }

    /**
     * Inserts a value into the subtree rooted at the specified node.
     *
     * @param root the root of the current subtree
     * @param value the value to insert
     * @return the updated subtree root
     */
    protected N insert(N root, T value){
        if (root == null) {
            return createNode(value);
        }
        if (compare(value, root) == 0) {
            throw new DuplicateNodeException("Value already present in tree");
        }

        if (compare(value, root) > 0) {
            root.setRight(insert(root.getRight(), value));
        } else {
            root.setLeft(insert(root.getLeft(), value));
        }
        return afterInsert(root);
    }

    /**
     * Deletes a value from the subtree rooted at the specified node.
     *
     * @param root the root of the current subtree
     * @param value the value to delete
     * @return the updated subtree root
     */
    protected N delete(N root, T value){
        if(root == null) return null;
        if(compare(value, root)>0){
            root.setRight(delete(root.getRight(),value));
        }
        else if(compare(value, root)<0){
            root.setLeft(delete(root.getLeft(),value));
        }
        else{
            if(root.getLeft()==null && root.getRight()==null){
                return null;
            }
            if(root.getRight()==null && root.getLeft()!=null){
                return root.getLeft();
            }
            if(root.getRight()!=null && root.getLeft()==null){
                return root.getRight();
            }
            N successor = getMinNode(root.getRight());
            root.setValue(successor.getValue());
            root.setRight(delete(root.getRight(), successor.getValue()));

        }
        return afterDelete(root);
    }

    /**
     * Compares the values of two nodes.
     *
     * @param a the first node to compare
     * @param b the second node to compare
     * @return a negative integer, zero, or a positive integer
     *         if the first node value is less than, equal to,
     *         or greater than the second node value
     */
    protected int compare(N a, N b) {
        return a.getValue().compareTo(b.getValue());
    }

    /**
     * Compares the values of two nodes.
     *
     * @param value the value to compare
     * @param curr the second node to compare
     * @return a negative integer, zero, or a positive integer
     *         if the first node value is less than, equal to,
     *         or greater than the second node value
     */
    protected int compare(T value, N curr){
        return value.compareTo(curr.getValue());
    }

    /**
     * Search the entire tree for the given value
     *
     * @param value the value to be searched
     * @return true if element exist otherwise false
     */
    protected boolean contain(T value){
        return contain(root,value);
    }

    private boolean contain(N root, T value) {
        if(root == null) return false;
        int cp = value.compareTo(root.getValue());
        if(cp ==0)return true;
        if(cp >0){
            return contain(root.getRight(), value);
        }
        else{
            return contain(root.getLeft(), value);
        }
    }

    @Override
    public void insert(T value) {
        root = insert(root, value);
        size++;
    }

    @Override
    public boolean search(T value){
        return contain(value);
    }

    @Override
    public boolean delete(T value){
        if(!contain(value)){
            return false;
        }
        root = delete(root,value);
        size--;
        return true;
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public int height(){
        return height(root);
    }

    protected int height(N root) {
        if(root ==null)return -1;
        int left = 1+height(root.getLeft());
        int right = 1+height(root.getRight());
        return Math.max(left,right);
    }

    @Override
    public boolean isEmpty(){
        return root==null;
    }

    @Override
    public void clear(){
        root=null;
        size=0;
    }

    /**
     * Return the minimum value present in the tree.
     * @return the minimum value
     */
    public T getMin(){
        if(isEmpty()){
            throw new EmptyTreeException("Tree is empty");
        }
        return getMinNode(root).getValue();
    }

    /**
     * Return the maximum value present in the tree.
     * @return the maximum value
     */
    public T getMax(){
        if(isEmpty()){
            throw new EmptyTreeException("Tree is empty");
        }
        return getMaxNode(root).getValue();
    }
    @Override
    public List<T> preorder(){
        List<T> preorder = new ArrayList<>();
        preorder(root,preorder);
        return preorder;
    }

    private void preorder(N root, List<T> preorder) {
        if(root==null)return;
        preorder.add(root.getValue());
        preorder(root.getLeft(),preorder);
        preorder(root.getRight(),preorder);
    }

    @Override
    public List<T> inorder(){
        List<T> inorder = new ArrayList<>();
        inorder(root,inorder);
        return inorder;
    }

    private void inorder(N root, List<T> inorder) {
        if(root ==null)return;
        inorder(root.getLeft(),inorder);
        inorder.add(root.getValue());
        inorder(root.getRight(),inorder);
    }

    @Override
    public List<T> postorder(){
        List<T> postorder = new ArrayList<>();
        postorder(root,postorder);
        return postorder;
    }

    private void postorder(N root, List<T> postorder) {
        if(root==null)return;
        postorder(root.getLeft(),postorder);
        postorder(root.getRight(),postorder);
        postorder.add(root.getValue());
    }

    @Override
    public List<T> levelOrder(){

        List<T> levelOrder = new ArrayList<>();
        if (root==null){
            return levelOrder;
        }
        Queue<N> queue = new ArrayDeque<>();
        queue.offer(root);
        while (!queue.isEmpty()){
            N curr = queue.poll();
            levelOrder.add(curr.getValue());
            if(curr.getLeft()!=null){
                queue.offer(curr.getLeft());
            }
            if(curr.getRight()!=null){
                queue.offer(curr.getRight());
            }
        }
        return levelOrder;
    }

    /**
     * Returns the min node from the current node.
     *
     * @param node the node which determines the source
     * @return Min node if present else null
     */
    protected N getMinNode(N node){
        if(node==null)return null;
        while (node.getLeft()!=null){
            node =node.getLeft();
        }
        return node;
    }
    /**
     * Returns the max node from the current node.
     *
     * @param node the node which determines the source
     * @return Max node if present else null
     */
    protected N getMaxNode(N node){
        if(node==null)return null;
        while (node.getRight()!=null){
            node=node.getRight();
        }
        return node;
    }

}

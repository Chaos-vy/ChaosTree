package chaos.tree.core.binary;
import chaos.tree.core.binary.node.BiNode;
import chaos.tree.exception.*;
import chaos.tree.exception.EmptyTreeException;
import java.util.*;

/**
 * This implementation is not thread safe.
 * For concurrent access, external synchronization is required.
 */
public abstract class AbstractBiTree<T extends Comparable<T>,N extends BiNode<T,N>> implements BinaryTree<T> {

    /**
     * Root of the tree
     */
    protected N root;
    @Override
    public T root(){
        treeIsEmpty();
        return root.getValue();
    }
    /**
     * Total element in tree
     */
    protected int size;

    /**
     *Construct an empty Binary tree
     */
    protected AbstractBiTree(){}

    protected void checkValue(T value){
        Objects.requireNonNull(value, "Value cannot be null");
    }

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

    @Override
    public void insert(T value) {
        checkValue(value);
        root = insert(root, value);
        size++;
    }

    @Override
    public void insertAll(Iterable<? extends T> values) {
        Objects.requireNonNull(values);
        for (T value : values) {
            insert(value);
        }
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

    record SearchResult<T>(boolean contains, T floor, T ceil) {
    }

    private SearchResult<T> search(T value) {
        return search(root, value, null, null);
    }
    private SearchResult<T> search(N node, T value, T floor, T ceil) {
        if (node == null) return new SearchResult<>(false, floor, ceil);
        int cp = value.compareTo(node.getValue());
        if (cp == 0) return new SearchResult<>(true, node.getValue(), node.getValue());
        if (cp > 0) return search(node.getRight(), value, node.getValue(), ceil);
        else return search(node.getLeft(), value, floor, node.getValue());
    }

    @Override
    public boolean contains(T value){
        checkValue(value);
        return search(value).contains;
    }

    @Override
    public void delete(T value){
        checkValue(value);
        final boolean[] isDeleted = new boolean[1];
        root = delete(root,value, isDeleted);
        if(isDeleted[0])size--;
    }

    /**
     * Deletes a value from the subtree rooted at the specified node.
     *
     * @param node the node from where the tree propagates for deletion
     * @param value the value to delete
     * @param isDeleted true when the node is found and deleted else false
     * @return the updated subtree
     */

    protected N delete(N node, T value, boolean[] isDeleted) {
        if(node == null) return null;

        int compare = compare(value, node);

        if(compare >0){
            node.setRight(delete(node.getRight(), value, isDeleted));
        }
        else if(compare <0){
            node.setLeft(delete(node.getLeft(), value, isDeleted));
        }
        else{
            isDeleted[0]=true;

            if (node.getLeft() == null) return node.getRight();
            if (node.getRight() == null) return node.getLeft();

            N successor = getMinNode(node.getRight());
            node.setValue(successor.getValue());
            node.setRight(delete(node.getRight(), successor.getValue(), isDeleted));
            isDeleted[0] = true;

        }
        return afterDelete(node);
    }
    /**
     * Does the update of metadata of respective tree after delete
     * @param node the node associated to tree
     */
    protected N afterDelete(N node) {
        return node;
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
    protected void treeIsEmpty(){
        if(isEmpty()){
            throw new EmptyTreeException("Tree is empty");
        }
    }

    /**
     * Return the minimum value present in the tree.
     * @return the minimum value
     */
    @Override
    public T min(){
        treeIsEmpty();
        return getMinNode(root).getValue();
    }

    /**
     * Return the maximum value present in the tree.
     * @return the maximum value
     */
    @Override
    public T max(){
        treeIsEmpty();
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

    /**
     * Return the node with the same value
     * @param node the source of subtree node to search
     * @param value the value of the node to be searched
     * @return node with the same value otherwise null
     */
    protected N findNode(N node, T value) {
        while (node != null) {
            int cmp = compare(value, node);
            if (cmp == 0) return node;
            node = cmp > 0 ? node.getRight() : node.getLeft();
        }
        return null;
    }

    private static final String BRANCH = "+-- ";
    private static final String LAST_BRANCH = "\\-- ";
    private static final String VERTICAL = "|   ";
    private static final String SPACE = "    ";
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        buildString(root, "", true, sb);
        return sb.toString();
    }
    protected String nodeText(N node) {
        return String.valueOf(node.getValue());
    }
    private void buildString(N node, String prefix, boolean isTail, StringBuilder sb) {
        if (node == null) {
            return;
        }
        sb.append(prefix).append(isTail ? LAST_BRANCH : BRANCH).append(nodeText(node)).append('\n');

        boolean hasLeft = node.getLeft() != null;
        boolean hasRight = node.getRight() != null;

        if (!hasLeft && !hasRight) {
            return;
        }

        String childPrefix = prefix + (isTail ? SPACE : VERTICAL);

        if (hasLeft && hasRight) {
            buildString(node.getLeft(), childPrefix, false, sb);
            buildString(node.getRight(), childPrefix, true, sb);

        } else if (hasLeft) {
            buildString(node.getLeft(), childPrefix, true, sb);

        } else {
            buildString(node.getRight(), childPrefix, true, sb);
        }
    }
    @Override
    public T floor(T value){
        treeIsEmpty();
        checkValue(value);
        return search(value).floor;
    }
    @Override
    public T ceil(T value) {
        treeIsEmpty();
        checkValue(value);
        return search(value).ceil;
    }

    @Override
    public T successor(T value) {
        treeIsEmpty();
        checkValue(value);
        N node = root;
        N successor = null;
        while (node != null) {
            int cmp = compare(value, node);
            if (cmp < 0) {
                successor = node;
                node = node.getLeft();
            } else if (cmp > 0) {
                node = node.getRight();
            } else {
                if (node.getRight() != null)
                    return getMinNode(node.getRight()).getValue();
                break;
            }
        }
        return successor == null ? null : successor.getValue();
    }

    @Override
    public T predecessor(T value) {
        treeIsEmpty();
        checkValue(value);
        N node = root;
        N predecessor = null;
        while (node!=null){
            int cmp = compare(value,node);
            if(cmp>0){
                predecessor=node;
                node = node.getRight();
            }
            else if(cmp<0){
                node=node.getLeft();
            }
            else {
                if(node.getLeft()!=null) predecessor= getMaxNode(node.getLeft());
                break;
            }
        }
        return predecessor==null? null: predecessor.getValue();
    }

    @Override
    public T lca(T a, T b) {
        treeIsEmpty();
        checkValue(a);
        checkValue(b);
        if(!contains(a) || !contains(b)){
            throw new NodeNotFoundException("Node not Found");
        }
        return lca(root,a,b).getValue();
    }

    private N lca(N node, T a, T b) {
        if (node == null) return null;
        int cmpA = compare(a, node);
        int cmpB = compare(b, node);
        if (cmpA > 0 && cmpB > 0) return lca(node.getRight(), a, b);
        if (cmpA < 0 && cmpB < 0) return lca(node.getLeft(), a, b);
        return node;
    }

    @Override
    public T kthSmallest(int k) {
        if(k<1 || k>size){
            throw new IllegalArgumentException("Out of Bound");
        }
        final int[] count = new int[]{k};
        N result = kthSmallest(root, count);
        return result.getValue();
    }
    private N kthSmallest(N node, int[] count){
        if(node==null)return null;
        N left = kthSmallest(node.getLeft(), count);
        if (left != null) return left;
        count[0]--;
        if (count[0] == 0) return node;
        return kthSmallest(node.getRight(), count);
    }
}

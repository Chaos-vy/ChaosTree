import chaos.tree.binary.AvlTreeSet;
import chaos.tree.binaryMap.AvlTreeMap;
import chaos.tree.core.Style;
import chaos.tree.nary.BPlusTreeSet;
import chaos.tree.naryMap.BPlusTreeMap;

/**
 * The example covers here is the new {@code display()} fn of Chaos Tree.
 *
 * <p><strong>Points to be noted:</strong></p>
 * <ol>
 *     <li>Binary Tree renders Tree like structure without ANSI.</li>
 *     <li>Nary Tree supports ANSI for Tree like structure.</li>
 *     <li>It does demonstrate the exact picture of the Tree at that snapshot.</li>
 *     <li>
 *         The {@code display()} fn default load is {@link Style#UNICODE}, so
 *         if a user renders something like ??? in their terminal, don't worry,
 *         it just means you have a bad laptop!! Well, jokes apart, it means
 *         your terminal does not support UNICODE. LOL!!
 *     </li>
 * </ol>
 *
 * <p><strong>Solution:</strong> Just use {@code display(Style.ASCII)}.
 * It's that simple!!</p>
 */
public class Example_01_display_fn_use {
    public static void main(String[] args) {
        AvlTreeSet<Integer> tree0 = new AvlTreeSet<>((e1,e2)->e2-e1);
        AvlTreeMap<Integer, Character> tree1 = new AvlTreeMap<>();
        BPlusTreeSet<Integer> tree2 = new BPlusTreeSet<>(4);
        BPlusTreeMap<Integer,Character> tree3 = new BPlusTreeMap<>(4);
        for (int i = 0; i < 26; i++) {
            tree0.add(i);
            tree1.put(i, (char)(i+'a'));
        }
        tree2.addAll(tree0);
        tree3.putAll(tree1);

        /**
         * <pre>
         * 15
         * ├── 19
         * │   ├── 23
         * │   │   ├── 24
         * │   │   │   └── 25
         * │   │   └── 21
         * │   │       ├── 22
         * │   │       └── 20
         * │   └── 17
         * │       ├── 18
         * │       └── 16
         * └── 7
         *     ├── 11
         *     │   ├── 13
         *     │   │   ├── 14
         *     │   │   └── 12
         *     │   └── 9
         *     │       ├── 10
         *     │       └── 8
         *     └── 3
         *         ├── 5
         *         │   ├── 6
         *         │   └── 4
         *         └── 1
         *             ├── 2
         *             └── 0
         * </pre>
         */
        System.out.println(tree0.display());
        /**
         * <pre>
         * [15=p]
         * +-- [7=h]
         * |   +-- [3=d]
         * |   |   +-- [1=b]
         * |   |   |   +-- [0=a]
         * |   |   |   \-- [2=c]
         * |   |   \-- [5=f]
         * |   |       +-- [4=e]
         * |   |       \-- [6=g]
         * |   \-- [11=l]
         * |       +-- [9=j]
         * |       |   +-- [8=i]
         * |       |   \-- [10=k]
         * |       \-- [13=n]
         * |           +-- [12=m]
         * |           \-- [14=o]
         * \-- [19=t]
         *     +-- [17=r]
         *     |   +-- [16=q]
         *     |   \-- [18=s]
         *     \-- [23=x]
         *         +-- [21=v]
         *         |   +-- [20=u]
         *         |   \-- [22=w]
         *         \-- [24=y]
         *             \-- [25=z]
         * </pre>
         */
        System.out.println(tree1.display(Style.ASCII));
        /**
         * <pre>
         *     └── [6, 10, 14, 18, 22]
         *         ├── [0, 1, 2, 3, 4, 5]
         *         ├── [6, 7, 8, 9]
         *         ├── [10, 11, 12, 13]
         *         ├── [14, 15, 16, 17]
         *         ├── [18, 19, 20, 21]
         *         └── [22, 23, 24, 25]
         * </pre>
         */
        System.out.println(tree2.display());
        /**
         * <pre>
         * └── [[6], [12], [18], [24]]
         *     ├── [[0=a], [1=b], [2=c], [3=d], [4=e], [5=f]]
         *     ├── [[6=g], [7=h], [8=i], [9=j], [10=k], [11=l]]
         *     ├── [[12=m], [13=n], [14=o], [15=p], [16=q], [17=r]]
         *     ├── [[18=s], [19=t], [20=u], [21=v], [22=w], [23=x]]
         *     └── [[24=y], [25=z]]
         * </pre>
         */
        System.out.println(tree3.display());

        /**
         * Suppose My dev did this don't worry you are gonna pay for the toString generator cost.
         */
        System.out.println(tree3);
        /**
         *{0=a, 1=b, 2=c, 3=d, 4=e, 5=f, 6=g, 7=h, 8=i, 9=j, 10=k, 11=l, 12=m, 13=n, 14=o, 15=p, 16=q, 17=r, 18=s, 19=t, 20=u, 21=v, 22=w, 23=x, 24=y, 25=z}
         */
    }
}

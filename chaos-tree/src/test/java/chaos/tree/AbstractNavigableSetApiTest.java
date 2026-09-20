package chaos.tree;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Provide;
import net.jqwik.api.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;


public abstract class AbstractNavigableSetApiTest {

    private static Arbitrary<Integer> values() {
        return Arbitraries.frequencyOf(
                Tuple.of(18, Arbitraries.integers().between(-20, 620)),
                Tuple.of(1, Arbitraries.of(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1, 1)));
    }

    private static Arbitrary<Action> point(String name, PointFn fn) {
        return values().map(v -> new Op(name + "(" + v + ")", (t, r) -> fn.run(t, r, v)));
    }


    private static Arbitrary<Action> bulk(String name, int maxSize, BulkFn fn) {
        return values().list().ofMaxSize(maxSize).map(xs -> new Op(name + xs, (t, r) -> fn.run(t, r, xs)));
    }

    private static Arbitrary<Action> noArg(String name, Action body) {
        return Arbitraries.just(new Op(name + "()", body));
    }


    private static Arbitrary<Action> viewOp() {
        return Combinators.combine(
                        values(), values(),
                        Arbitraries.of(true, false), Arbitraries.of(true, false),
                        Arbitraries.of(ViewKind.values()), Arbitraries.of(ViewMutation.values()),
                        values())
                .as((a, b, ai, bi, kind, mut, x) -> new Op(
                        "view[" + kind + " a=" + a + (ai ? "]" : ")") + " b=" + b + (bi ? "]" : ")")
                                + " then " + mut + " x=" + x,
                        (t, r) -> withView(kind.name(), t, r, s -> kind.make(s, a, ai, b, bi), (tv, rv) -> {
                            verify("view " + kind, tv, rv, x);

                            withView("nested desc.tail", tv, rv, s -> s.descendingSet().tailSet(x, ai),
                                    (t2, r2) -> verify("nested desc.tail", t2, r2, x));
                            withView("nested head.desc", tv, rv, s -> s.headSet(x, bi).descendingSet(),
                                    (t2, r2) -> verify("nested head.desc", t2, r2, x));
                            withView("nested sub(x,b)", tv, rv, s -> s.subSet(x, true, b, bi),
                                    (t2, r2) -> verify("nested sub(x,b)", t2, r2, x));

                            mut.apply(tv, rv, x);
                            verify("view after " + mut, tv, rv, x);
                            assertEquals(drain(r.iterator()), drain(t.iterator()),
                                    "write-through mismatch on backing set after view." + mut);
                        })));
    }


    private static void withView(String label,
                                 NavigableSet<Integer> t, NavigableSet<Integer> r,
                                 UnaryOperator<NavigableSet<Integer>> spec,
                                 BiConsumer<NavigableSet<Integer>, NavigableSet<Integer>> then) {
        NavigableSet<Integer> rv = null, tv = null;
        Class<?> rex = null, tex = null;
        try {
            rv = spec.apply(r);
        } catch (RuntimeException e) {
            rex = e.getClass();
        }
        try {
            tv = spec.apply(t);
        } catch (RuntimeException e) {
            tex = e.getClass();
        }
        assertEquals(rex, tex, label + ": view creation outcome (expected = TreeSet)");
        if (rv != null) {
            then.accept(tv, rv);
        }
    }

    protected static void verify(String label, NavigableSet<Integer> t, NavigableSet<Integer> r, int p) {
        assertEquals(r.size(), t.size(), label + ": size");
        assertEquals(r.isEmpty(), t.isEmpty(), label + ": isEmpty");

        same(label + ": contains", () -> r.contains(p), () -> t.contains(p));
        same(label + ": lower", () -> r.lower(p), () -> t.lower(p));
        same(label + ": floor", () -> r.floor(p), () -> t.floor(p));
        same(label + ": ceiling", () -> r.ceiling(p), () -> t.ceiling(p));
        same(label + ": higher", () -> r.higher(p), () -> t.higher(p));
        same(label + ": first", r::first, t::first);
        same(label + ": last", r::last, t::last);

        List<Integer> expected = drain(r.iterator());
        assertEquals(expected, drain(t.iterator()), label + ": iterator");
        assertEquals(drain(r.descendingIterator()), drain(t.descendingIterator()), label + ": descendingIterator");
        assertEquals(drain(r.descendingSet().iterator()), drain(t.descendingSet().iterator()),
                label + ": descendingSet().iterator()");
        assertEquals(drain(r.descendingSet().descendingSet().iterator()),
                drain(t.descendingSet().descendingSet().iterator()), label + ": descendingSet().descendingSet()");

        assertEquals(expected, Arrays.asList(t.toArray()), label + ": toArray()");
        assertEquals(expected, Arrays.asList(t.toArray(new Integer[0])), label + ": toArray(T[0])");
        assertEquals(expected, Arrays.asList(t.toArray(Integer[]::new)), label + ": toArray(IntFunction)");
        Integer[] big = new Integer[expected.size() + 3];
        Arrays.fill(big, -1);
        Integer[] bigRef = big.clone();
        assertSame(big, t.toArray(big), label + ": toArray(T[]) must reuse a large enough array");
        r.toArray(bigRef);
        assertArrayEquals(bigRef, big, label + ": toArray(T[]) oversized (null terminator, rest untouched)");

        assertEquals(expected, new ArrayList<>(t), label + ": stream");
        assertEquals(expected, t.parallelStream().collect(Collectors.toList()),
                label + ": parallel stream keeps encounter order");
        assertEquals(r.stream().mapToLong(Integer::longValue).sum(),
                t.stream().mapToLong(Integer::longValue).sum(), label + ": stream sum");
        List<Integer> viaForEach = new ArrayList<>(t);
        assertEquals(expected, viaForEach, label + ": forEach");
        int mask = Spliterator.SORTED | Spliterator.DISTINCT | Spliterator.ORDERED;
        assertEquals(r.spliterator().characteristics() & mask, t.spliterator().characteristics() & mask,
                label + ": spliterator characteristics (SORTED|DISTINCT|ORDERED)");

        assertEquals(r, t, label + ": ref.equals(tree)");
        assertEquals(t, r, label + ": tree.equals(ref)");
        assertEquals(r.hashCode(), t.hashCode(), label + ": hashCode");
    }


    protected static void edgeCases(Supplier<NavigableSet<Integer>> factory) {
        for (int size : new int[]{0, 1, 2, 100}) {
            NavigableSet<Integer> t = factory.get();
            NavigableSet<Integer> r = new TreeSet<>();
            for (int i = 0; i < size; i++) {
                t.add(i * 2);
                r.add(i * 2);
            }
            String s = " [size=" + size + "]";

            same("add(null)" + s, () -> r.add(null), () -> t.add(null));
            same("remove(null)" + s, () -> r.remove(null), () -> t.remove(null));
            same("contains(null)" + s, () -> r.contains(null), () -> t.contains(null));
            same("lower(null)" + s, () -> r.lower(null), () -> t.lower(null));
            same("floor(null)" + s, () -> r.floor(null), () -> t.floor(null));
            same("ceiling(null)" + s, () -> r.ceiling(null), () -> t.ceiling(null));
            same("higher(null)" + s, () -> r.higher(null), () -> t.higher(null));
            same("headSet(null)" + s, () -> drain(r.headSet(null).iterator()), () -> drain(t.headSet(null).iterator()));
            same("tailSet(null)" + s, () -> drain(r.tailSet(null).iterator()), () -> drain(t.tailSet(null).iterator()));
            same("subSet(null,5)" + s, () -> drain(r.subSet(null, 5).iterator()), () -> drain(t.subSet(null, 5).iterator()));
            same("subSet(5,null)" + s, () -> drain(r.subSet(5, null).iterator()), () -> drain(t.subSet(5, null).iterator()));
            same("subSet(null,true,5,true)" + s, () -> drain(r.subSet(null, true, 5, true).iterator()),
                    () -> drain(t.subSet(null, true, 5, true).iterator()));
            same("containsAll(null)" + s, () -> r.containsAll(null), () -> t.containsAll(null));
            same("removeAll(null)" + s, () -> r.removeAll(null), () -> t.removeAll(null));
            same("retainAll(null)" + s, () -> r.retainAll(null), () -> t.retainAll(null));
            same("addAll(null)" + s, () -> r.addAll(null), () -> t.addAll(null));
            same("removeIf(null)" + s, () -> r.removeIf(null), () -> t.removeIf(null));
            same("forEach(null)" + s, () -> {
                r.forEach(null);
                return null;
            }, () -> {
                t.forEach(null);
                return null;
            });
            same("toArray((T[]) null)" + s, () -> r.toArray((Integer[]) null), () -> t.toArray((Integer[]) null));
            same("toArray(String[0]) wrong type" + s,
                    () -> Arrays.asList(r.toArray(new String[0])), () -> Arrays.asList(t.toArray(new String[0])));

            // inverted / degenerate ranges
            same("subSet(5,1)" + s, () -> drain(r.subSet(5, 1).iterator()), () -> drain(t.subSet(5, 1).iterator()));
            same("subSet(5,true,1,true)" + s, () -> drain(r.subSet(5, true, 1, true).iterator()),
                    () -> drain(t.subSet(5, true, 1, true).iterator()));
            same("subSet(4,true,4,true)" + s, () -> drain(r.subSet(4, true, 4, true).iterator()),
                    () -> drain(t.subSet(4, true, 4, true).iterator()));
            same("subSet(4,false,4,false)" + s, () -> drain(r.subSet(4, false, 4, false).iterator()),
                    () -> drain(t.subSet(4, false, 4, false).iterator()));
            same("descendingSet().subSet(1,5)" + s, () -> drain(r.descendingSet().subSet(1, 5).iterator()),
                    () -> drain(t.descendingSet().subSet(1, 5).iterator()));

            for (int k : new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE}) {
                same("lower(" + k + ")" + s, () -> r.lower(k), () -> t.lower(k));
                same("higher(" + k + ")" + s, () -> r.higher(k), () -> t.higher(k));
                same("floor(" + k + ")" + s, () -> r.floor(k), () -> t.floor(k));
                same("ceiling(" + k + ")" + s, () -> r.ceiling(k), () -> t.ceiling(k));
                same("headSet(" + k + ",false)" + s, () -> drain(r.headSet(k, false).iterator()),
                        () -> drain(t.headSet(k, false).iterator()));
                same("tailSet(" + k + ",false)" + s, () -> drain(r.tailSet(k, false).iterator()),
                        () -> drain(t.tailSet(k, false).iterator()));
            }

            same("containsAll(self)" + s, () -> r.containsAll(r), () -> t.containsAll(t));
            same("addAll(self)" + s, () -> r.addAll(r), () -> t.addAll(t));
            same("retainAll(self)" + s, () -> r.retainAll(r), () -> t.retainAll(t));

            verify("edge cases end state" + s, t, r, 3);

            same("addAll(list containing null)" + s,
                    () -> r.addAll(Arrays.asList(1, null, 3)), () -> t.addAll(Arrays.asList(1, null, 3)));
        }
    }

    protected static void failFast(Supplier<NavigableSet<Integer>> factory) {
        NavigableSet<Integer> t = factory.get();
        for (int i = 0; i < 100; i++) {
            t.add(i);
        }

        Iterator<Integer> afterAdd = t.iterator();
        afterAdd.next();
        t.add(1000);
        assertThrows(ConcurrentModificationException.class, afterAdd::next, "add during iteration");

        Iterator<Integer> afterRemove = t.iterator();
        afterRemove.next();
        t.remove(50);
        assertThrows(ConcurrentModificationException.class, afterRemove::next, "remove during iteration");

        Iterator<Integer> afterDescAdd = t.descendingIterator();
        afterDescAdd.next();
        t.add(2000);
        assertThrows(ConcurrentModificationException.class, afterDescAdd::next, "add during descending iteration");

        Iterator<Integer> own = t.iterator();
        own.next();
        own.remove();
        own.next();
    }

    private static Object call(Supplier<?> s) {
        try {
            return s.get();
        } catch (RuntimeException e) {
            return new Thrown(e.getClass());
        }
    }

    /**
     * Runs both sides; passes iff both return equal values or both throw the same exception class.
     */
    private static void same(String what, Supplier<?> expected, Supplier<?> actual) {
        assertEquals(call(expected), call(actual), what + " (expected = TreeSet)");
    }

    // Action plumbing

    private static List<Integer> drain(Iterator<Integer> it) {
        List<Integer> out = new ArrayList<>();
        while (it.hasNext()) {
            out.add(it.next());
        }
        return out;
    }

    private static List<Integer> removeEveryKth(Iterator<Integer> it, int k) {
        List<Integer> removed = new ArrayList<>();
        int i = 0;
        while (it.hasNext()) {
            int v = it.next();
            if (++i % k == 0) {
                it.remove();
                removed.add(v);
            }
        }
        return removed;
    }

    private static String doubleRemove(Iterator<Integer> it) {
        if (!it.hasNext()) {
            return "empty";
        }
        it.next();
        it.remove();
        it.remove(); // -> IllegalStateException
        return "returned";
    }

    private static void iteratorMisuse(NavigableSet<Integer> t, NavigableSet<Integer> r,
                                       Function<NavigableSet<Integer>, Iterator<Integer>> how, String name) {
        same(name + ": remove() before next()",
                () -> {
                    how.apply(r).remove();
                    return "returned";
                },
                () -> {
                    how.apply(t).remove();
                    return "returned";
                });
        same(name + ": next() past end",
                () -> {
                    Iterator<Integer> it = how.apply(r);
                    while (it.hasNext()) it.next();
                    it.next();
                    return "returned";
                },
                () -> {
                    Iterator<Integer> it = how.apply(t);
                    while (it.hasNext()) it.next();
                    it.next();
                    return "returned";
                });
        same(name + ": remove() twice", () -> doubleRemove(how.apply(r)), () -> doubleRemove(how.apply(t)));
    }


    // Views


    protected void runScenario(NavigableSet<Integer> tree, List<Integer> initial, List<Action> actions) {
        NavigableSet<Integer> reference = new TreeSet<>();

        for (int v : initial) {
            assertEquals(reference.add(v), tree.add(v), "initial add return mismatch for " + v);
        }
        verify("after initial load", tree, reference, 0);

        for (Action action : actions) {
            action.run(tree, reference);
            assertEquals(reference.size(), tree.size(), "Size mismatch after " + action);
            assertEquals(reference.isEmpty(), tree.isEmpty(), "isEmpty mismatch after " + action);
        }
        verify("final state", tree, reference, 0);
        assertEquals(reference.comparator(), tree.comparator(), "comparator");
    }


    @Provide
    Arbitrary<Integer> degrees() {
        return Arbitraries.of(4, 5, 8, 16, 32, 64);
    }

    /*
     * Ascending, descending and random bulk loads (each stresses different split/merge paths).
     */
    @Provide
    Arbitrary<List<Integer>> initialLoad() {
        Arbitrary<List<Integer>> asc = Arbitraries.integers().between(0, 500)
                .map(n -> IntStream.range(0, n).boxed().collect(Collectors.toList()));
        Arbitrary<List<Integer>> desc = Arbitraries.integers().between(0, 500).map(n -> {
            List<Integer> l = IntStream.range(0, n).boxed().collect(Collectors.toList());
            Collections.reverse(l);
            return l;
        });
        Arbitrary<List<Integer>> random = values().list().ofMaxSize(500);
        return Arbitraries.oneOf(asc, desc, random);
    }


    @Provide
    Arbitrary<List<Action>> actions() {
        return Arbitraries.frequencyOf(
                Tuple.of(10, point("add", (t, r, v) -> same("add", () -> r.add(v), () -> t.add(v)))),
                Tuple.of(10, point("remove", (t, r, v) -> same("remove", () -> r.remove(v), () -> t.remove(v)))),
                Tuple.of(5, point("contains", (t, r, v) -> same("contains", () -> r.contains(v), () -> t.contains(v)))),

                Tuple.of(5, point("lower", (t, r, v) -> same("lower", () -> r.lower(v), () -> t.lower(v)))),
                Tuple.of(5, point("floor", (t, r, v) -> same("floor", () -> r.floor(v), () -> t.floor(v)))),
                Tuple.of(5, point("ceiling", (t, r, v) -> same("ceiling", () -> r.ceiling(v), () -> t.ceiling(v)))),
                Tuple.of(5, point("higher", (t, r, v) -> same("higher", () -> r.higher(v), () -> t.higher(v)))),

                Tuple.of(2, noArg("first", (t, r) -> same("first", r::first, t::first))),
                Tuple.of(2, noArg("last", (t, r) -> same("last", r::last, t::last))),
                Tuple.of(2, noArg("pollFirst", (t, r) -> same("pollFirst", r::pollFirst, t::pollFirst))),
                Tuple.of(2, noArg("pollLast", (t, r) -> same("pollLast", r::pollLast, t::pollLast))),
                Tuple.of(1, noArg("clear", (t, r) -> {
                    t.clear();
                    r.clear();
                })),

                Tuple.of(4, bulk("addAll", 60, (t, r, xs) ->
                        same("addAll", () -> r.addAll(xs), () -> t.addAll(xs)))),
                Tuple.of(2, bulk("addAll(sorted)", 60, (t, r, xs) ->
                        same("addAll(sorted)", () -> r.addAll(new TreeSet<>(xs)), () -> t.addAll(new TreeSet<>(xs))))),
                Tuple.of(3, bulk("removeAll", 30, (t, r, xs) ->
                        same("removeAll", () -> r.removeAll(xs), () -> t.removeAll(xs)))),
                Tuple.of(1, bulk("retainAll", 200, (t, r, xs) ->
                        same("retainAll", () -> r.retainAll(xs), () -> t.retainAll(xs)))),
                Tuple.of(2, bulk("containsAll", 30, (t, r, xs) ->
                        same("containsAll", () -> r.containsAll(xs), () -> t.containsAll(xs)))),
                Tuple.of(2, point("removeIf(mod7)", (t, r, v) -> {
                    int m = Math.floorMod(v, 7);
                    same("removeIf", () -> r.removeIf(x -> Math.floorMod(x, 7) == m),
                            () -> t.removeIf(x -> Math.floorMod(x, 7) == m));
                })),

                Tuple.of(2, point("iterator.removeEveryKth", (t, r, v) -> {
                    int k = Math.floorMod(v, 5) + 1;
                    same("iterator.removeEveryKth", () -> removeEveryKth(r.iterator(), k),
                            () -> removeEveryKth(t.iterator(), k));
                })),
                Tuple.of(2, point("descendingIterator.removeEveryKth", (t, r, v) -> {
                    int k = Math.floorMod(v, 5) + 1;
                    same("descendingIterator.removeEveryKth", () -> removeEveryKth(r.descendingIterator(), k),
                            () -> removeEveryKth(t.descendingIterator(), k));
                })),
                Tuple.of(2, noArg("iteratorMisuse", (t, r) -> {
                    iteratorMisuse(t, r, NavigableSet::iterator, "iterator");
                    iteratorMisuse(t, r, NavigableSet::descendingIterator, "descendingIterator");
                })),

                Tuple.of(6, viewOp()),

                Tuple.of(1, noArg("getFirst", (t, r) -> same("getFirst", r::getFirst, t::getFirst))),
                Tuple.of(1, noArg("getLast", (t, r) -> same("getLast", r::getLast, t::getLast))),
                Tuple.of(1, noArg("removeFirst", (t, r) -> same("removeFirst", r::removeFirst, t::removeFirst))),
                Tuple.of(1, noArg("removeLast", (t, r) -> same("removeLast", r::removeLast, t::removeLast))),
                Tuple.of(1, point("addFirst", (t, r, v) -> same("addFirst",
                        () -> {
                            r.addFirst(v);
                            return null;
                        }, () -> {
                            t.addFirst(v);
                            return null;
                        }))),
                Tuple.of(1, point("addLast", (t, r, v) -> same("addLast",
                        () -> {
                            r.addLast(v);
                            return null;
                        }, () -> {
                            t.addLast(v);
                            return null;
                        }))),
                Tuple.of(1, point("reversed", (t, r, v) -> verify("reversed()", t.reversed(), r.reversed(), v))),

                Tuple.of(3, point("verifyAll", (t, r, v) -> {
                    verify("base", t, r, v);
                    same("comparator", r::comparator, t::comparator);
                }))
        ).list().ofMaxSize(120);
    }


    private enum ViewKind {
        SUB4((s, a, ai, b, bi) -> s.subSet(a, ai, b, bi)),
        SUB2((s, a, ai, b, bi) -> (NavigableSet<Integer>) s.subSet(a, b)),
        HEAD2((s, a, ai, b, bi) -> s.headSet(b, bi)),
        HEAD1((s, a, ai, b, bi) -> (NavigableSet<Integer>) s.headSet(b)),
        TAIL2((s, a, ai, b, bi) -> s.tailSet(a, ai)),
        TAIL1((s, a, ai, b, bi) -> (NavigableSet<Integer>) s.tailSet(a)),
        DESC((s, a, ai, b, bi) -> s.descendingSet()),
        DESC_SUB4((s, a, ai, b, bi) -> s.descendingSet().subSet(a, ai, b, bi)),
        SUB4_DESC((s, a, ai, b, bi) -> s.subSet(a, ai, b, bi).descendingSet()),
        DESC_HEAD((s, a, ai, b, bi) -> s.descendingSet().headSet(b, bi));

        private final ViewFactory factory;

        ViewKind(ViewFactory factory) {
            this.factory = factory;
        }

        NavigableSet<Integer> make(NavigableSet<Integer> s, int a, boolean ai, int b, boolean bi) {
            return factory.make(s, a, ai, b, bi);
        }
    }


    private enum ViewMutation {
        ADD((t, r, x) -> same("view.add", () -> r.add(x), () -> t.add(x))),   // out-of-range -> IAE
        REMOVE((t, r, x) -> same("view.remove", () -> r.remove(x), () -> t.remove(x))),
        POLL_FIRST((t, r, x) -> same("view.pollFirst", r::pollFirst, t::pollFirst)),
        POLL_LAST((t, r, x) -> same("view.pollLast", r::pollLast, t::pollLast)),
        CLEAR((t, r, x) -> same("view.clear", () -> {
            r.clear();
            return null;
        }, () -> {
            t.clear();
            return null;
        })),
        REMOVE_IF((t, r, x) -> same("view.removeIf",
                () -> r.removeIf(e -> Math.floorMod(e, 3) == 0), () -> t.removeIf(e -> Math.floorMod(e, 3) == 0))),
        ITER_REMOVE((t, r, x) -> same("view.iterator.remove",
                () -> removeEveryKth(r.iterator(), 2), () -> removeEveryKth(t.iterator(), 2))),
        DESC_ITER_REMOVE((t, r, x) -> same("view.descendingIterator.remove",
                () -> removeEveryKth(r.descendingIterator(), 3), () -> removeEveryKth(t.descendingIterator(), 3))),
        DESC_POLL_FIRST((t, r, x) -> same("view.descendingSet.pollFirst",
                () -> r.descendingSet().pollFirst(), () -> t.descendingSet().pollFirst())),
        ADD_ALL((t, r, x) -> {
            List<Integer> xs = Arrays.asList(x, x + 1, x + 2, x - 1);
            same("view.addAll", () -> r.addAll(xs), () -> t.addAll(xs));
        });

        private final Mutation mutation;

        ViewMutation(Mutation mutation) {
            this.mutation = mutation;
        }

        void apply(NavigableSet<Integer> t, NavigableSet<Integer> r, int x) {
            mutation.apply(t, r, x);
        }
    }


    protected interface Action {
        void run(NavigableSet<Integer> tree, NavigableSet<Integer> reference);
    }

    private interface PointFn {
        void run(NavigableSet<Integer> t, NavigableSet<Integer> r, int v);
    }

    private interface BulkFn {
        void run(NavigableSet<Integer> t, NavigableSet<Integer> r, List<Integer> xs);
    }

    private interface ViewFactory {
        NavigableSet<Integer> make(NavigableSet<Integer> s, int a, boolean ai, int b, boolean bi);
    }

    private interface Mutation {
        void apply(NavigableSet<Integer> t, NavigableSet<Integer> r, int x);
    }


    private record Op(String name, Action body) implements Action {
        @Override
        public void run(NavigableSet<Integer> t, NavigableSet<Integer> r) {
            body.run(t, r);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private record Thrown(Class<?> type) {
    }
}
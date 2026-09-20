package chaos.tree;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Provide;
import net.jqwik.api.Tuple;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;


public abstract class AbstractNavigableMapApiTest {

    private static final double NULL_VALUE_PROBABILITY = 0.05;

    private static Arbitrary<Integer> keys() {
        return Arbitraries.frequencyOf(
                Tuple.of(18, Arbitraries.integers().between(-20, 620)),
                Tuple.of(1, Arbitraries.of(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1, 1)));
    }

    private static Arbitrary<Integer> vals() {
        Arbitrary<Integer> base = Arbitraries.integers().between(0, 9);
        return NULL_VALUE_PROBABILITY > 0 ? base.injectNull(NULL_VALUE_PROBABILITY) : base;
    }

    private static Integer initialValue(int k) {
        return NULL_VALUE_PROBABILITY > 0 && Math.floorMod(k, 11) == 10 ? null : Math.floorMod(k, 10);
    }

    private static Arbitrary<Action> kv(String name, KvFn fn) {
        return Combinators.combine(keys(), vals())
                .as((k, v) -> new Op(name + "(" + k + ", " + v + ")", (t, r) -> fn.run(t, r, k, v)));
    }

    private static Arbitrary<Action> mapOp(MapOp op) {
        return kv(op.name(), op::apply);
    }


    private static Arbitrary<Action> anyMapOp() {
        return Combinators.combine(Arbitraries.of(MapOp.values()).filter(o -> o != MapOp.CLEAR), keys(), vals())
                .as((op, k, v) -> new Op(op + "(" + k + ", " + v + ")", (t, r) -> op.apply(t, r, k, v)));
    }

    private static Arbitrary<Action> bulk(String name, int maxSize, BulkFn fn) {
        return keys().list().ofMaxSize(maxSize).map(xs -> new Op(name + xs, (t, r) -> fn.run(t, r, xs)));
    }

    private static Arbitrary<Action> noArg(String name, Action body) {
        return Arbitraries.just(new Op(name + "()", body));
    }

    private static Map<Integer, Integer> payload(List<Integer> xs) {
        Map<Integer, Integer> m = new LinkedHashMap<>();
        for (int i = 0; i < xs.size(); i++) {
            int x = xs.get(i);
            m.put(x, NULL_VALUE_PROBABILITY > 0 && i % 17 == 16 ? null : Math.floorMod(x + i, 10));
        }
        return m;
    }

    private static int salt(Integer v) {
        return v == null ? 7 : v;
    }

    private static Integer mapping(int k, Integer v) {
        int s = salt(v);
        return s % 4 == 0 ? null : k + s;
    }

    private static Integer remap(Integer old, Integer v) {
        int s = salt(v);
        if (s % 4 == 0) {
            return null;
        }
        return old == null ? s : old + s;
    }

    private static Integer mergeFn(Integer a, Integer b, Integer v) {
        return salt(v) % 4 == 0 ? null : a + b;
    }

    private static Integer replacer(Integer key, Integer old) {
        return old == null ? key : old + Math.floorMod(key, 3);
    }

    private static Integer bump(Integer v) {
        return v == null ? 0 : (v + 1) % 10;
    }


    private static Object snapshotTest(NavigableMap<Integer, Integer> m, int k, Integer v) {
        Map.Entry<Integer, Integer> e = m.ceilingEntry(k);
        if (e == null) {
            return "none";
        }
        Object before = norm(e);
        m.put(e.getKey(), v);
        return Arrays.asList(before, norm(e));
    }

    @SuppressWarnings("unchecked")
    private static void applyCollOp(CollOp op, CollKind kind,
                                    NavigableMap<Integer, Integer> t, NavigableMap<Integer, Integer> r,
                                    int x, List<Integer> xs) {
        Collection<?> tc = kind.select.apply(t);
        Collection<?> rc = kind.select.apply(r);
        Object probe = kind.probe(x);
        List<Object> probes = xs.stream().map(kind::probe).collect(Collectors.toList());
        Predicate<Object> pred = kind.predicate();
        int k = Math.floorMod(x, 4) + 1;

        switch (op) {
            case REMOVE -> same("remove", () -> rc.remove(probe), () -> tc.remove(probe));
            case CONTAINS -> same("contains", () -> rc.contains(probe), () -> tc.contains(probe));
            case REMOVE_IF -> same("removeIf", () -> rc.removeIf(pred), () -> tc.removeIf(pred));
            case REMOVE_ALL -> same("removeAll", () -> rc.removeAll(probes), () -> tc.removeAll(probes));
            case RETAIN_ALL -> same("retainAll", () -> rc.retainAll(probes), () -> tc.retainAll(probes));
            case CONTAINS_ALL -> same("containsAll", () -> rc.containsAll(probes), () -> tc.containsAll(probes));
            case CLEAR -> same("clear", () -> {
                rc.clear();
                return null;
            }, () -> {
                tc.clear();
                return null;
            });
            case ADD -> same("add",
                    () -> ((Collection<Object>) rc).add(probe), () -> ((Collection<Object>) tc).add(probe));
            case ADD_ALL -> {
                List<Object> two = Arrays.asList(probe, kind.probe(x + 1));
                same("addAll", () -> ((Collection<Object>) rc).addAll(two), () -> ((Collection<Object>) tc).addAll(two));
            }
            case ITER_REMOVE -> same("iterator.remove",
                    () -> removeEveryKth(rc.iterator(), k), () -> removeEveryKth(tc.iterator(), k));
            case SET_VALUE -> {
                if (kind.cat == Cat.ENTRIES) {
                    Integer nv = Math.floorMod(x, 11) == 10 ? null : Math.floorMod(x, 10);
                    same("entry.setValue", () -> setValueEveryKth(rc.iterator(), k, nv),
                            () -> setValueEveryKth(tc.iterator(), k, nv));
                }
            }
        }

        assertEquals(rc.size(), tc.size(), kind + "." + op + ": collection size");
        assertEquals(drainNorm(rc), drainNorm(tc), kind + "." + op + ": collection content/order");
        assertEquals(drainNorm(r.entrySet()), drainNorm(t.entrySet()),
                "write-through mismatch on backing map after " + kind + "." + op);
    }

    private static Arbitrary<Action> collectionViewOp() {
        return Combinators.combine(
                        Arbitraries.of(CollKind.values()), Arbitraries.of(CollOp.values()),
                        keys(), keys().list().ofMaxSize(20))
                .as((kind, op, x, xs) -> new Op(
                        "coll[" + kind + "." + op + " x=" + x + " xs=" + xs + "]",
                        (t, r) -> applyCollOp(op, kind, t, r, x, xs)));
    }

    private static Arbitrary<ViewSpec> viewSpecs() {
        return Combinators.combine(keys(), keys(), Arbitraries.of(true, false), Arbitraries.of(true, false),
                Arbitraries.of(MapViewKind.values())).as(ViewSpec::new);
    }

    private static Arbitrary<Action> mapViewOp() {
        return Combinators.combine(
                        viewSpecs(), Arbitraries.of(MapOp.values()), keys(), vals(),
                        Arbitraries.of(CollKind.values()), Arbitraries.of(CollOp.values()),
                        keys().list().ofMaxSize(10))
                .as((spec, op, x, y, ck, co, xs) -> new Op(
                        "mapView[" + spec + "] then " + op + "(" + x + "," + y + ") then " + ck + "." + co
                                + " x=" + x + " xs=" + xs,
                        (t, r) -> withView(spec.toString(), t, r, spec::make, (tv, rv) -> {
                            verify("view " + spec, tv, rv, x);

                            withView("nested desc.tail", tv, rv, m -> m.descendingMap().tailMap(x, spec.ai()),
                                    (t2, r2) -> verify("nested desc.tail", t2, r2, x));
                            withView("nested head.desc", tv, rv, m -> m.headMap(x, spec.bi()).descendingMap(),
                                    (t2, r2) -> verify("nested head.desc", t2, r2, x));
                            withView("nested sub(x,b)", tv, rv, m -> m.subMap(x, true, spec.b(), spec.bi()),
                                    (t2, r2) -> verify("nested sub(x,b)", t2, r2, x));

                            op.apply(tv, rv, x, y);
                            verify("view after " + op, tv, rv, x);

                            applyCollOp(co, ck, tv, rv, x, xs);
                            verify("view after " + ck + "." + co, tv, rv, x);

                            assertEquals(drainNorm(r.entrySet()), drainNorm(t.entrySet()),
                                    "write-through mismatch on backing map after " + op + " / " + ck + "." + co);
                        })));
    }

    private static void withView(String label,
                                 NavigableMap<Integer, Integer> t, NavigableMap<Integer, Integer> r,
                                 UnaryOperator<NavigableMap<Integer, Integer>> spec,
                                 BiConsumer<NavigableMap<Integer, Integer>, NavigableMap<Integer, Integer>> then) {
        NavigableMap<Integer, Integer> rv = null, tv = null;
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
        assertEquals(rex, tex, label + ": view creation outcome (expected = TreeMap)");
        if (rv != null) {
            then.accept(tv, rv);
        }
    }

    private static void iteratorMisuse(NavigableMap<Integer, Integer> t, NavigableMap<Integer, Integer> r, IterKind kind) {
        same(kind + ": remove() before next()",
                () -> {
                    kind.how.apply(r).remove();
                    return "returned";
                },
                () -> {
                    kind.how.apply(t).remove();
                    return "returned";
                });
        same(kind + ": next() past end",
                () -> {
                    Iterator<?> it = kind.how.apply(r);
                    while (it.hasNext()) it.next();
                    it.next();
                    return "returned";
                },
                () -> {
                    Iterator<?> it = kind.how.apply(t);
                    while (it.hasNext()) it.next();
                    it.next();
                    return "returned";
                });
        same(kind + ": remove() twice", () -> doubleRemove(kind.how.apply(r)), () -> doubleRemove(kind.how.apply(t)));
    }

    private static String doubleRemove(Iterator<?> it) {
        if (!it.hasNext()) {
            return "empty";
        }
        it.next();
        it.remove();
        it.remove();
        return "returned";
    }


    private static List<Object> removeEveryKth(Iterator<?> it, int k) {
        List<Object> removed = new ArrayList<>();
        int i = 0;
        while (it.hasNext()) {
            Object o = norm(it.next());
            if (++i % k == 0) {
                it.remove();
                removed.add(o);
            }
        }
        return removed;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> setValueEveryKth(Iterator<?> it, int k, Integer newValue) {
        List<Object> out = new ArrayList<>();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry<Integer, Integer> e = (Map.Entry<Integer, Integer>) it.next();
            if (++i % k == 0) {
                Integer old = e.setValue(newValue);
                out.add(Arrays.asList(e.getKey(), old, e.getValue()));
            } else {
                out.add(norm(e));
            }
        }
        return out;
    }

    /*
    api list
    containskey,get,getordef,conval,lwkey,flrkey,ceilkey,hikey,lowentr,flrentry,firstkey/entry,lastkey/entry
     */

    protected static void verify(String label, NavigableMap<Integer, Integer> t, NavigableMap<Integer, Integer> r, int p) {
        int m = Math.floorMod(p, 10);

        assertEquals(r.size(), t.size(), label + ": size");
        assertEquals(r.isEmpty(), t.isEmpty(), label + ": isEmpty");


        same(label + ": containsKey", () -> r.containsKey(p), () -> t.containsKey(p));
        same(label + ": get", () -> r.get(p), () -> t.get(p));
        same(label + ": getOrDefault", () -> r.getOrDefault(p, -999), () -> t.getOrDefault(p, -999));
        same(label + ": containsValue", () -> r.containsValue(m), () -> t.containsValue(m));
        same(label + ": containsValue(null)", () -> r.containsValue(null), () -> t.containsValue(null));

        same(label + ": lowerKey", () -> r.lowerKey(p), () -> t.lowerKey(p));
        same(label + ": floorKey", () -> r.floorKey(p), () -> t.floorKey(p));
        same(label + ": ceilingKey", () -> r.ceilingKey(p), () -> t.ceilingKey(p));
        same(label + ": higherKey", () -> r.higherKey(p), () -> t.higherKey(p));
        same(label + ": lowerEntry", () -> norm(r.lowerEntry(p)), () -> norm(t.lowerEntry(p)));
        same(label + ": floorEntry", () -> norm(r.floorEntry(p)), () -> norm(t.floorEntry(p)));
        same(label + ": ceilingEntry", () -> norm(r.ceilingEntry(p)), () -> norm(t.ceilingEntry(p)));
        same(label + ": higherEntry", () -> norm(r.higherEntry(p)), () -> norm(t.higherEntry(p)));
        same(label + ": firstKey", r::firstKey, t::firstKey);
        same(label + ": lastKey", r::lastKey, t::lastKey);
        same(label + ": firstEntry", () -> norm(r.firstEntry()), () -> norm(t.firstEntry()));
        same(label + ": lastEntry", () -> norm(r.lastEntry()), () -> norm(t.lastEntry()));

        same(label + ": firstEntry().setValue", () -> r.firstEntry().setValue(1), () -> t.firstEntry().setValue(1));
        same(label + ": lastEntry().setValue", () -> r.lastEntry().setValue(1), () -> t.lastEntry().setValue(1));
        same(label + ": floorEntry().setValue", () -> r.floorEntry(p).setValue(1), () -> t.floorEntry(p).setValue(1));

        List<Object> entries = drainNorm(r.entrySet());
        List<Object> keysL = drainNorm(r.keySet());
        List<Object> valuesL = drainNorm(r.values());
        assertEquals(entries, drainNorm(t.entrySet()), label + ": entrySet order/content");
        assertEquals(keysL, drainNorm(t.keySet()), label + ": keySet order");
        assertEquals(valuesL, drainNorm(t.values()), label + ": values order");
        assertEquals(drainNorm(r.navigableKeySet()), drainNorm(t.navigableKeySet()), label + ": navigableKeySet");
        assertEquals(drainNorm(r.descendingKeySet()), drainNorm(t.descendingKeySet()), label + ": descendingKeySet");
        assertEquals(drainNorm(r.descendingMap().entrySet()), drainNorm(t.descendingMap().entrySet()),
                label + ": descendingMap().entrySet()");
        assertEquals(drainNorm(r.descendingMap().keySet()), drainNorm(t.descendingMap().keySet()),
                label + ": descendingMap().keySet()");
        assertEquals(drainNorm(r.descendingMap().values()), drainNorm(t.descendingMap().values()),
                label + ": descendingMap().values()");
        assertEquals(drainNorm(r.descendingMap().descendingMap().entrySet()),
                drainNorm(t.descendingMap().descendingMap().entrySet()), label + ": descendingMap().descendingMap()");

        Iterator<Map.Entry<Integer, Integer>> ri = r.entrySet().iterator();
        Iterator<Map.Entry<Integer, Integer>> ti = t.entrySet().iterator();
        while (ri.hasNext() && ti.hasNext()) {
            Map.Entry<Integer, Integer> a = ri.next();
            Map.Entry<Integer, Integer> b = ti.next();
            assertEquals(a.getKey(), b.getKey(), label + ": entry key");
            assertEquals(a.getValue(), b.getValue(), label + ": entry value");
            assertEquals(a, b, label + ": ref entry.equals(tree entry)");
            assertEquals(b, a, label + ": tree entry.equals(ref entry)");
            assertEquals(a.hashCode(), b.hashCode(), label + ": entry hashCode");
        }

        assertEquals(keysL, Arrays.asList(t.keySet().toArray()), label + ": keySet.toArray()");
        assertEquals(keysL, Arrays.asList(t.keySet().toArray(new Integer[0])), label + ": keySet.toArray(T[0])");
        assertEquals(valuesL, Arrays.asList(t.values().toArray()), label + ": values.toArray()");
        assertEquals(valuesL, Arrays.asList(t.values().toArray(new Integer[0])), label + ": values.toArray(T[0])");
        assertEquals(entries, normAll(Arrays.asList(t.entrySet().toArray())), label + ": entrySet.toArray()");
        assertEquals(entries, normAll(Arrays.asList(t.entrySet().toArray(new Map.Entry[0]))),
                label + ": entrySet.toArray(T[0])");
        Integer[] big = new Integer[keysL.size() + 3];
        Arrays.fill(big, -1);
        Integer[] bigRef = big.clone();
        assertSame(big, t.keySet().toArray(big), label + ": keySet.toArray(T[]) must reuse a large enough array");
        r.keySet().toArray(bigRef);
        assertArrayEquals(bigRef, big, label + ": keySet.toArray(T[]) oversized (null terminator, rest untouched)");

        assertEquals(keysL, new ArrayList<>(t.keySet()), label + ": keySet.stream");
        assertEquals(valuesL, new ArrayList<>(t.values()), label + ": values.stream");
        assertEquals(entries, t.entrySet().stream().map(AbstractNavigableMapApiTest::norm).collect(Collectors.toList()),
                label + ": entrySet.stream");
        assertEquals(keysL, t.keySet().parallelStream().collect(Collectors.toList()),
                label + ": parallel keySet stream keeps encounter order");
        assertEquals(entries, t.entrySet().parallelStream()
                        .map(AbstractNavigableMapApiTest::norm).collect(Collectors.toList()),
                label + ": parallel entrySet stream keeps encounter order");
        List<Object> viaForEach = new ArrayList<>();
        t.forEach((k, v) -> viaForEach.add(Arrays.asList(k, v)));
        assertEquals(entries, viaForEach, label + ": forEach");
//Equality of Object contracts
        assertEquals(r, t, label + ": ref.equals(tree)");
        assertEquals(t, r, label + ": tree.equals(ref)");
        assertEquals(r.hashCode(), t.hashCode(), label + ": map hashCode");
        assertEquals(new HashMap<>(r), t, label + ": HashMap.equals(tree)");
        assertEquals(t, new HashMap<>(r), label + ": tree.equals(HashMap)");
        assertEquals(r.entrySet(), t.entrySet(), label + ": ref.entrySet().equals(tree.entrySet())");
        assertEquals(t.entrySet(), r.entrySet(), label + ": tree.entrySet().equals(ref.entrySet())");
        assertEquals(r.entrySet().hashCode(), t.entrySet().hashCode(), label + ": entrySet hashCode");
        assertEquals(r.keySet(), t.keySet(), label + ": ref.keySet().equals(tree.keySet())");
        assertEquals(t.keySet(), r.keySet(), label + ": tree.keySet().equals(ref.keySet())");
        assertEquals(r.keySet().hashCode(), t.keySet().hashCode(), label + ": keySet hashCode");
        HashMap<Integer, Integer> other = new HashMap<>(r);
        other.put(p, 12345);
        same(label + ": equals(different map)", () -> r.equals(other), () -> t.equals(other));
        same(label + ": equals(non-map)", () -> r.equals("x"), () -> t.equals("x"));
        same(label + ": equals(null)", () -> r.equals(null), () -> t.equals(null));

        verifyKeys(label + ": navigableKeySet", t.navigableKeySet(), r.navigableKeySet(), p);
        verifyKeys(label + ": descendingKeySet", t.descendingKeySet(), r.descendingKeySet(), p);
    }

    private static void verifyKeys(String label, NavigableSet<Integer> t, NavigableSet<Integer> r, int p) {
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
                label + ": descendingSet()");
        assertEquals(expected, Arrays.asList(t.toArray()), label + ": toArray()");
        assertEquals(r, t, label + ": ref.equals(tree)");
        assertEquals(t, r, label + ": tree.equals(ref)");
        assertEquals(r.hashCode(), t.hashCode(), label + ": hashCode");

        same(label + ": subSet", () -> drain(r.subSet(p, true, p + 50, false).iterator()),
                () -> drain(t.subSet(p, true, p + 50, false).iterator()));
        same(label + ": headSet", () -> drain(r.headSet(p, true).iterator()), () -> drain(t.headSet(p, true).iterator()));
        same(label + ": tailSet", () -> drain(r.tailSet(p, false).iterator()), () -> drain(t.tailSet(p, false).iterator()));
    }

    private static void sameView(String what, NavigableMap<Integer, Integer> r, NavigableMap<Integer, Integer> t,
                                 Function<NavigableMap<Integer, Integer>, Map<Integer, Integer>> view) {
        same(what, () -> drainNorm(view.apply(r).entrySet()), () -> drainNorm(view.apply(t).entrySet()));
    }
    //null checklist

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static void edgeCases(Supplier<NavigableMap<Integer, Integer>> factory) {
        for (int size : new int[]{0, 1, 2, 100}) {
            NavigableMap<Integer, Integer> t = factory.get();
            NavigableMap<Integer, Integer> r = new TreeMap<>();
            for (int i = 0; i < size; i++) {
                Integer v = initialValue(i);
                t.put(i * 2, v);
                r.put(i * 2, v);
            }
            String s = " [size=" + size + "]";

            same("put(null,1)" + s, () -> r.put(null, 1), () -> t.put(null, 1));
            same("get(null)" + s, () -> r.get(null), () -> t.get(null));
            same("containsKey(null)" + s, () -> r.containsKey(null), () -> t.containsKey(null));
            same("remove(null)" + s, () -> r.remove(null), () -> t.remove(null));
            same("remove(null,1)" + s, () -> r.remove(null, 1), () -> t.remove(null, 1));
            same("replace(null,1)" + s, () -> r.replace(null, 1), () -> t.replace(null, 1));
            same("replace(null,1,2)" + s, () -> r.replace(null, 1, 2), () -> t.replace(null, 1, 2));
            same("getOrDefault(null,1)" + s, () -> r.getOrDefault(null, 1), () -> t.getOrDefault(null, 1));
            same("putIfAbsent(null,1)" + s, () -> r.putIfAbsent(null, 1), () -> t.putIfAbsent(null, 1));

            same("computeIfAbsent(null)" + s, () -> r.computeIfAbsent(null, k -> 1), () -> t.computeIfAbsent(null, k -> 1));
            same("computeIfPresent(null)" + s, () -> r.computeIfPresent(null, (k, o) -> 1),
                    () -> t.computeIfPresent(null, (k, o) -> 1));
            same("compute(null)" + s, () -> r.compute(null, (k, o) -> 1), () -> t.compute(null, (k, o) -> 1));
            same("merge(null,1)" + s, () -> r.merge(null, 1, (a, b) -> 1), () -> t.merge(null, 1, (a, b) -> 1));
            same("lowerKey(null)" + s, () -> r.lowerKey(null), () -> t.lowerKey(null));
            same("floorKey(null)" + s, () -> r.floorKey(null), () -> t.floorKey(null));
            same("ceilingKey(null)" + s, () -> r.ceilingKey(null), () -> t.ceilingKey(null));
            same("higherKey(null)" + s, () -> r.higherKey(null), () -> t.higherKey(null));
            same("lowerEntry(null)" + s, () -> norm(r.lowerEntry(null)), () -> norm(t.lowerEntry(null)));
            same("floorEntry(null)" + s, () -> norm(r.floorEntry(null)), () -> norm(t.floorEntry(null)));
            same("ceilingEntry(null)" + s, () -> norm(r.ceilingEntry(null)), () -> norm(t.ceilingEntry(null)));
            same("higherEntry(null)" + s, () -> norm(r.higherEntry(null)), () -> norm(t.higherEntry(null)));
            sameView("headMap(null)" + s, r, t, m -> m.headMap(null));
            sameView("headMap(null,true)" + s, r, t, m -> m.headMap(null, true));
            sameView("tailMap(null)" + s, r, t, m -> m.tailMap(null));
            sameView("tailMap(null,true)" + s, r, t, m -> m.tailMap(null, true));
            sameView("subMap(null,5)" + s, r, t, m -> m.subMap(null, 5));
            sameView("subMap(5,null)" + s, r, t, m -> m.subMap(5, null));
            sameView("subMap(null,true,5,true)" + s, r, t, m -> m.subMap(null, true, 5, true));
            sameView("subMap(5,true,null,true)" + s, r, t, m -> m.subMap(5, true, null, true));

            same("putAll(null)" + s, () -> {
                r.putAll(null);
                return null;
            }, () -> {
                t.putAll(null);
                return null;
            });
            same("computeIfAbsent(5,null)" + s, () -> r.computeIfAbsent(5, null), () -> t.computeIfAbsent(5, null));
            same("computeIfPresent(5,null)" + s, () -> r.computeIfPresent(5, null), () -> t.computeIfPresent(5, null));
            same("compute(5,null)" + s, () -> r.compute(5, null), () -> t.compute(5, null));
            same("merge(5,1,null)" + s, () -> r.merge(5, 1, null), () -> t.merge(5, 1, null));
            same("merge(5,null,fn)" + s, () -> r.merge(5, null, (a, b) -> a), () -> t.merge(5, null, (a, b) -> a));
            same("forEach(null)" + s, () -> {
                r.forEach(null);
                return null;
            }, () -> {
                t.forEach(null);
                return null;
            });
            same("replaceAll(null)" + s, () -> {
                r.replaceAll(null);
                return null;
            }, () -> {
                t.replaceAll(null);
                return null;
            });
            same("keySet().removeIf(null)" + s, () -> r.keySet().removeIf(null), () -> t.keySet().removeIf(null));
            same("values().removeIf(null)" + s, () -> r.values().removeIf(null), () -> t.values().removeIf(null));
            same("entrySet().removeIf(null)" + s, () -> r.entrySet().removeIf(null), () -> t.entrySet().removeIf(null));
            same("keySet().removeAll(null)" + s, () -> r.keySet().removeAll(null), () -> t.keySet().removeAll(null));
            same("values().retainAll(null)" + s, () -> r.values().retainAll(null), () -> t.values().retainAll(null));
            same("entrySet().containsAll(null)" + s, () -> r.entrySet().containsAll(null), () -> t.entrySet().containsAll(null));

            same("containsValue(null)" + s, () -> r.containsValue(null), () -> t.containsValue(null));
            same("values().contains(null)" + s, () -> r.values().contains(null), () -> t.values().contains(null));
            same("values().remove(null)" + s, () -> r.values().remove(null), () -> t.values().remove(null));
            same("entrySet().contains(k,null)" + s, () -> r.entrySet().contains(new AbstractMap.SimpleEntry<>(2, null)),
                    () -> t.entrySet().contains(new AbstractMap.SimpleEntry<>(2, null)));

            same("keySet().add" + s, () -> r.keySet().add(1), () -> t.keySet().add(1));
            same("values().add" + s, () -> r.values().add(1), () -> ((Collection) t.values()).add(1));
            same("entrySet().add" + s, () -> r.entrySet().add(new AbstractMap.SimpleEntry<>(1, 1)),
                    () -> t.entrySet().add(new AbstractMap.SimpleEntry<>(1, 1)));

            sameView("subMap(5,1)" + s, r, t, m -> m.subMap(5, 1));
            sameView("subMap(5,true,1,true)" + s, r, t, m -> m.subMap(5, true, 1, true));
            sameView("subMap(4,true,4,true)" + s, r, t, m -> m.subMap(4, true, 4, true));
            sameView("subMap(4,false,4,false)" + s, r, t, m -> m.subMap(4, false, 4, false));
            sameView("subMap(4,true,4,false)" + s, r, t, m -> m.subMap(4, true, 4, false));
            sameView("descendingMap().subMap(1,5)" + s, r, t, m -> m.descendingMap().subMap(1, 5));
            sameView("descendingMap().subMap(5,1)" + s, r, t, m -> m.descendingMap().subMap(5, 1));
            same("subMap(10,20).put(5) out of range" + s,
                    () -> r.subMap(10, 20).put(5, 1), () -> t.subMap(10, 20).put(5, 1));
            same("headMap(10).put(10) exclusive bound" + s,
                    () -> r.headMap(10).put(10, 1), () -> t.headMap(10).put(10, 1));
            same("headMap(10,true).put(10) inclusive bound" + s,
                    () -> r.headMap(10, true).put(10, 1), () -> t.headMap(10, true).put(10, 1));
            same("tailMap(10,false).put(10) exclusive bound" + s,
                    () -> r.tailMap(10, false).put(10, 1), () -> t.tailMap(10, false).put(10, 1));

            for (int k : new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE}) {
                same("lowerKey(" + k + ")" + s, () -> r.lowerKey(k), () -> t.lowerKey(k));
                same("higherKey(" + k + ")" + s, () -> r.higherKey(k), () -> t.higherKey(k));
                same("floorEntry(" + k + ")" + s, () -> norm(r.floorEntry(k)), () -> norm(t.floorEntry(k)));
                same("ceilingEntry(" + k + ")" + s, () -> norm(r.ceilingEntry(k)), () -> norm(t.ceilingEntry(k)));
                sameView("headMap(" + k + ",false)" + s, r, t, m -> m.headMap(k, false));
                sameView("tailMap(" + k + ",false)" + s, r, t, m -> m.tailMap(k, false));
            }

            if (size > 0) {
                Map rawR = r, rawT = t;
                same("get(String)" + s, () -> rawR.get("x"), () -> rawT.get("x"));
                same("containsKey(String)" + s, () -> rawR.containsKey("x"), () -> rawT.containsKey("x"));
                same("remove(String)" + s, () -> rawR.remove("x"), () -> rawT.remove("x"));
                same("keySet().contains(String)" + s, () -> r.keySet().contains("x"), () -> t.keySet().contains("x"));
                same("entrySet().contains(wrong key type)" + s,
                        () -> r.entrySet().contains(new AbstractMap.SimpleEntry<>("x", 1)),
                        () -> t.entrySet().contains(new AbstractMap.SimpleEntry<>("x", 1)));
            }
            same("containsValue(String)" + s, () -> r.containsValue("x"), () -> t.containsValue("x"));
            same("values().contains(String)" + s, () -> r.values().contains("x"), () -> t.values().contains("x"));
            same("entrySet().contains(garbage)" + s, () -> r.entrySet().contains("garbage"), () -> t.entrySet().contains("garbage"));
            same("entrySet().remove(garbage)" + s, () -> r.entrySet().remove("garbage"), () -> t.entrySet().remove("garbage"));

            same("putAll(self)" + s, () -> {
                r.putAll(r);
                return null;
            }, () -> {
                t.putAll(t);
                return null;
            });
            same("keySet().containsAll(keySet())" + s, () -> r.keySet().containsAll(r.keySet()),
                    () -> t.keySet().containsAll(t.keySet()));
            same("keySet().retainAll(keySet())" + s, () -> r.keySet().retainAll(r.keySet()),
                    () -> t.keySet().retainAll(t.keySet()));

            verify("edge cases end state" + s, t, r, 3);

            Map<Integer, Integer> withNullKey = new LinkedHashMap<>();
            withNullKey.put(1, 1);
            withNullKey.put(null, 2);
            withNullKey.put(3, 3);
            same("putAll(map containing null key)" + s,
                    () -> {
                        r.putAll(withNullKey);
                        return null;
                    }, () -> {
                        t.putAll(withNullKey);
                        return null;
                    });
        }
    }

    protected static void failFast(Supplier<NavigableMap<Integer, Integer>> factory) {
        for (IterKind kind : IterKind.values()) {
            String w = kind.name();

            NavigableMap<Integer, Integer> m1 = filled(factory);
            Iterator<?> afterPut = kind.how.apply(m1);
            afterPut.next();
            m1.put(1000, 1);
            assertThrows(ConcurrentModificationException.class, afterPut::next, w + ": put(new key) during iteration");

            NavigableMap<Integer, Integer> m2 = filled(factory);
            Iterator<?> afterRemove = kind.how.apply(m2);
            afterRemove.next();
            m2.remove(50);
            assertThrows(ConcurrentModificationException.class, afterRemove::next, w + ": remove during iteration");

            NavigableMap<Integer, Integer> m3 = filled(factory);
            Iterator<?> afterClear = kind.how.apply(m3);
            afterClear.next();
            m3.clear();
            assertThrows(ConcurrentModificationException.class, afterClear::next, w + ": clear during iteration");

            NavigableMap<Integer, Integer> m4 = filled(factory);
            Iterator<?> afterReplace = kind.how.apply(m4);
            afterReplace.next();
            m4.put(3, 999);
            afterReplace.next();

            NavigableMap<Integer, Integer> m5 = filled(factory);
            Iterator<?> own = kind.how.apply(m5);
            own.next();
            own.remove();
            own.next();
        }
    }


    private static NavigableMap<Integer, Integer> filled(Supplier<NavigableMap<Integer, Integer>> factory) {
        NavigableMap<Integer, Integer> m = factory.get();
        for (int i = 0; i < 100; i++) {
            m.put(i, i % 10);
        }
        return m;
    }

    protected static void mutatingFunctions(Supplier<NavigableMap<Integer, Integer>> factory) {
        for (int mode = 0; mode < 4; mode++) {
            NavigableMap<Integer, Integer> t = factory.get();
            NavigableMap<Integer, Integer> r = new TreeMap<>();
            for (int i = 0; i < 10; i++) {
                t.put(i, i);
                r.put(i, i);
            }
            int mm = mode;
            same("function that mutates the map, mode " + mode, () -> mutating(r, mm), () -> mutating(t, mm));
        }
    }

    private static Object mutating(NavigableMap<Integer, Integer> m, int mode) {
        return switch (mode) {
            case 0 -> m.computeIfAbsent(1000, k -> {
                m.put(2000, 1);
                return 5;
            });
            case 1 -> m.computeIfPresent(3, (k, o) -> {
                m.put(2000, 1);
                return 5;
            });
            case 2 -> m.compute(3, (k, o) -> {
                m.put(2000, 1);
                return 5;
            });
            default -> m.merge(3, 1, (a, b) -> {
                m.put(2000, 1);
                return 5;
            });
        };
    }

    private static Object call(Supplier<?> s) {
        try {
            return s.get();
        } catch (RuntimeException e) {
            return new Thrown(e.getClass());
        }
    }


    private static void same(String what, Supplier<?> expected, Supplier<?> actual) {
        assertEquals(call(expected), call(actual), what + " (expected = TreeMap)");
    }

    private static Object norm(Object o) {
        if (o instanceof Map.Entry<?, ?> e) {
            return Arrays.asList(e.getKey(), e.getValue());
        }
        return o;
    }

    private static List<Object> normAll(List<?> l) {
        return l.stream().map(AbstractNavigableMapApiTest::norm).collect(Collectors.toList());
    }

    private static List<Object> drainNorm(Iterator<?> it) {
        List<Object> out = new ArrayList<>();
        while (it.hasNext()) {
            out.add(norm(it.next()));
        }
        return out;
    }

    private static List<Object> drainNorm(Collection<?> c) {
        return drainNorm(c.iterator());
    }

    private static List<Integer> drain(Iterator<Integer> it) {
        List<Integer> out = new ArrayList<>();
        while (it.hasNext()) {
            out.add(it.next());
        }
        return out;
    }

    protected void runScenario(NavigableMap<Integer, Integer> tree, List<Integer> initial, List<Action> actions) {
        NavigableMap<Integer, Integer> reference = new TreeMap<>();

        for (int k : initial) {
            Integer v = initialValue(k);
            assertEquals(reference.put(k, v), tree.put(k, v), "initial put return mismatch for key " + k);
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
        return Arbitraries.of(3, 4, 5, 8, 16, 32, 64); //any degree can be taken
    }

    @Provide
    Arbitrary<List<Integer>> initialLoad() {
        Arbitrary<List<Integer>> asc = Arbitraries.integers().between(0, 500)
                .map(n -> IntStream.range(0, n).boxed().collect(Collectors.toList()));
        Arbitrary<List<Integer>> desc = Arbitraries.integers().between(0, 500).map(n -> {
            List<Integer> l = IntStream.range(0, n).boxed().collect(Collectors.toList());
            Collections.reverse(l);
            return l;
        });
        Arbitrary<List<Integer>> random = keys().list().ofMaxSize(500);
        return Arbitraries.oneOf(asc, desc, random);
    }


    @Provide
    Arbitrary<List<Action>> actions() {
        return Arbitraries.frequencyOf(
                Tuple.of(12, mapOp(MapOp.PUT)),
                Tuple.of(8, mapOp(MapOp.REMOVE)),
                Tuple.of(40, anyMapOp()),
                Tuple.of(1, mapOp(MapOp.CLEAR)),

                Tuple.of(4, bulk("putAll", 60, (t, r, xs) -> same("putAll",
                        () -> {
                            r.putAll(payload(xs));
                            return null;
                        },
                        () -> {
                            t.putAll(payload(xs));
                            return null;
                        }))),
                Tuple.of(2, bulk("putAll(sorted)", 60, (t, r, xs) -> same("putAll(sorted)",
                        () -> {
                            r.putAll(new TreeMap<>(payload(xs)));
                            return null;
                        },
                        () -> {
                            t.putAll(new TreeMap<>(payload(xs)));
                            return null;
                        }))),
                Tuple.of(6, collectionViewOp()),

                Tuple.of(6, mapViewOp()),

                Tuple.of(2, noArg("iteratorMisuse", (t, r) -> {
                    for (IterKind kind : IterKind.values()) {
                        iteratorMisuse(t, r, kind);
                    }
                })),

                Tuple.of(1, kv("putFirst", (t, r, k, v) -> same("putFirst",
                        () -> {
                            r.putFirst(k, v);
                            return null;
                        }, () -> {
                            t.putFirst(k, v);
                            return null;
                        }))),
                Tuple.of(1, kv("putLast", (t, r, k, v) -> same("putLast",
                        () -> {
                            r.putLast(k, v);
                            return null;
                        }, () -> {
                            t.putLast(k, v);
                            return null;
                        }))),
                Tuple.of(1, kv("reversed", (t, r, k, v) -> verify("reversed()", t.reversed(), r.reversed(), k))),
                Tuple.of(1, noArg("sequencedViews", (t, r) -> {
                    same("sequencedKeySet", () -> drainNorm(r.sequencedKeySet()), () -> drainNorm(t.sequencedKeySet()));
                    same("sequencedKeySet.reversed", () -> drainNorm(r.sequencedKeySet().reversed()),
                            () -> drainNorm(t.sequencedKeySet().reversed()));
                    same("sequencedValues", () -> drainNorm(r.sequencedValues()), () -> drainNorm(t.sequencedValues()));
                    same("sequencedValues.reversed", () -> drainNorm(r.sequencedValues().reversed()),
                            () -> drainNorm(t.sequencedValues().reversed()));
                    same("sequencedEntrySet", () -> drainNorm(r.sequencedEntrySet()), () -> drainNorm(t.sequencedEntrySet()));
                    same("sequencedEntrySet.reversed", () -> drainNorm(r.sequencedEntrySet().reversed()),
                            () -> drainNorm(t.sequencedEntrySet().reversed()));
                })),

                Tuple.of(3, kv("verifyAll", (t, r, k, v) -> {
                    verify("base", t, r, k);
                    same("comparator", r::comparator, t::comparator);
                }))
        ).list().ofMaxSize(120);
    }

    // put,putif,get,getdef,contkey/val,remove,replace,compute all 3, merge,
    private enum MapOp {
        PUT((t, r, k, v) -> same("put", () -> r.put(k, v), () -> t.put(k, v))),
        PUT_IF_ABSENT((t, r, k, v) -> same("putIfAbsent", () -> r.putIfAbsent(k, v), () -> t.putIfAbsent(k, v))),
        GET((t, r, k, v) -> same("get", () -> r.get(k), () -> t.get(k))),
        GET_OR_DEFAULT((t, r, k, v) -> same("getOrDefault", () -> r.getOrDefault(k, v), () -> t.getOrDefault(k, v))),
        CONTAINS_KEY((t, r, k, v) -> same("containsKey", () -> r.containsKey(k), () -> t.containsKey(k))),
        CONTAINS_VALUE((t, r, k, v) -> same("containsValue", () -> r.containsValue(v), () -> t.containsValue(v))),
        REMOVE((t, r, k, v) -> same("remove(k)", () -> r.remove(k), () -> t.remove(k))),
        REMOVE_KV((t, r, k, v) -> same("remove(k,v)", () -> r.remove(k, v), () -> t.remove(k, v))),
        REPLACE((t, r, k, v) -> same("replace(k,v)", () -> r.replace(k, v), () -> t.replace(k, v))),
        REPLACE_KOV((t, r, k, v) -> same("replace(k,old,new)",
                () -> r.replace(k, v, bump(v)), () -> t.replace(k, v, bump(v)))),
        COMPUTE_IF_ABSENT((t, r, k, v) -> same("computeIfAbsent",
                () -> r.computeIfAbsent(k, kk -> mapping(kk, v)), () -> t.computeIfAbsent(k, kk -> mapping(kk, v)))),
        COMPUTE_IF_PRESENT((t, r, k, v) -> same("computeIfPresent",
                () -> r.computeIfPresent(k, (kk, old) -> remap(old, v)),
                () -> t.computeIfPresent(k, (kk, old) -> remap(old, v)))),
        COMPUTE((t, r, k, v) -> same("compute",
                () -> r.compute(k, (kk, old) -> remap(old, v)), () -> t.compute(k, (kk, old) -> remap(old, v)))),
        MERGE((t, r, k, v) -> same("merge",
                () -> r.merge(k, v, (a, b) -> mergeFn(a, b, v)), () -> t.merge(k, v, (a, b) -> mergeFn(a, b, v)))),
        REPLACE_ALL((t, r, k, v) -> same("replaceAll",
                () -> {
                    r.replaceAll(AbstractNavigableMapApiTest::replacer);
                    return null;
                },
                () -> {
                    t.replaceAll(AbstractNavigableMapApiTest::replacer);
                    return null;
                })),
        CLEAR((t, r, k, v) -> same("clear", () -> {
            r.clear();
            return null;
        }, () -> {
            t.clear();
            return null;
        })),

        LOWER_KEY((t, r, k, v) -> same("lowerKey", () -> r.lowerKey(k), () -> t.lowerKey(k))),
        LOWER_ENTRY((t, r, k, v) -> same("lowerEntry", () -> norm(r.lowerEntry(k)), () -> norm(t.lowerEntry(k)))),
        FLOOR_KEY((t, r, k, v) -> same("floorKey", () -> r.floorKey(k), () -> t.floorKey(k))),
        FLOOR_ENTRY((t, r, k, v) -> same("floorEntry", () -> norm(r.floorEntry(k)), () -> norm(t.floorEntry(k)))),
        CEILING_KEY((t, r, k, v) -> same("ceilingKey", () -> r.ceilingKey(k), () -> t.ceilingKey(k))),
        CEILING_ENTRY((t, r, k, v) -> same("ceilingEntry", () -> norm(r.ceilingEntry(k)), () -> norm(t.ceilingEntry(k)))),
        HIGHER_KEY((t, r, k, v) -> same("higherKey", () -> r.higherKey(k), () -> t.higherKey(k))),
        HIGHER_ENTRY((t, r, k, v) -> same("higherEntry", () -> norm(r.higherEntry(k)), () -> norm(t.higherEntry(k)))),

        FIRST_KEY((t, r, k, v) -> same("firstKey", r::firstKey, t::firstKey)),
        LAST_KEY((t, r, k, v) -> same("lastKey", r::lastKey, t::lastKey)),
        FIRST_ENTRY((t, r, k, v) -> same("firstEntry", () -> norm(r.firstEntry()), () -> norm(t.firstEntry()))),
        LAST_ENTRY((t, r, k, v) -> same("lastEntry", () -> norm(r.lastEntry()), () -> norm(t.lastEntry()))),
        POLL_FIRST_ENTRY((t, r, k, v) -> same("pollFirstEntry", () -> norm(r.pollFirstEntry()), () -> norm(t.pollFirstEntry()))),
        POLL_LAST_ENTRY((t, r, k, v) -> same("pollLastEntry", () -> norm(r.pollLastEntry()), () -> norm(t.pollLastEntry()))),
        DESC_POLL_FIRST_ENTRY((t, r, k, v) -> same("descendingMap.pollFirstEntry",
                () -> norm(r.descendingMap().pollFirstEntry()), () -> norm(t.descendingMap().pollFirstEntry()))),
        DESC_POLL_LAST_ENTRY((t, r, k, v) -> same("descendingMap.pollLastEntry",
                () -> norm(r.descendingMap().pollLastEntry()), () -> norm(t.descendingMap().pollLastEntry()))),
        NAV_KEYSET_POLL_FIRST((t, r, k, v) -> same("navigableKeySet.pollFirst",
                () -> r.navigableKeySet().pollFirst(), () -> t.navigableKeySet().pollFirst())),
        DESC_KEYSET_POLL_LAST((t, r, k, v) -> same("descendingKeySet.pollLast",
                () -> r.descendingKeySet().pollLast(), () -> t.descendingKeySet().pollLast())),

        SNAPSHOT_ENTRY((t, r, k, v) -> same("snapshot entry", () -> snapshotTest(r, k, v), () -> snapshotTest(t, k, v)));

        private final KvFn fn;

        MapOp(KvFn fn) {
            this.fn = fn;
        }

        void apply(NavigableMap<Integer, Integer> t, NavigableMap<Integer, Integer> r, int k, Integer v) {
            fn.run(t, r, k, v);
        }
    }

    private enum Cat {KEYS, VALUES, ENTRIES}

    private enum CollKind {
        KEY_SET(m -> m.keySet(), Cat.KEYS),
        NAV_KEY_SET(NavigableMap::navigableKeySet, Cat.KEYS),
        DESC_KEY_SET(NavigableMap::descendingKeySet, Cat.KEYS),
        VALUES(m -> m.values(), Cat.VALUES),
        ENTRY_SET(m -> m.entrySet(), Cat.ENTRIES),
        DESC_MAP_KEY_SET(m -> m.descendingMap().keySet(), Cat.KEYS),
        DESC_MAP_VALUES(m -> m.descendingMap().values(), Cat.VALUES),
        DESC_MAP_ENTRY_SET(m -> m.descendingMap().entrySet(), Cat.ENTRIES);

        private final Function<NavigableMap<Integer, Integer>, Collection<?>> select;
        private final Cat cat;

        CollKind(Function<NavigableMap<Integer, Integer>, Collection<?>> select, Cat cat) {
            this.select = select;
            this.cat = cat;
        }


        Object probe(int x) {
            Integer value = Math.floorMod(x, 11) == 10 ? null : Math.floorMod(x, 10);
            return switch (cat) {
                case KEYS -> x;
                case VALUES -> value;
                case ENTRIES -> new AbstractMap.SimpleEntry<Integer, Integer>(x, value);
            };
        }

        @SuppressWarnings("unchecked")
        Predicate<Object> predicate() {
            return switch (cat) {
                case KEYS -> o -> Math.floorMod((Integer) o, 3) == 0;
                case VALUES -> o -> o == null || Math.floorMod((Integer) o, 3) == 0;
                case ENTRIES -> o -> Math.floorMod(((Map.Entry<Integer, Integer>) o).getKey(), 3) == 0;
            };
        }
    }

    private enum CollOp {
        REMOVE, CONTAINS, REMOVE_IF, REMOVE_ALL, RETAIN_ALL, CONTAINS_ALL, CLEAR,
        ADD,
        ADD_ALL,
        ITER_REMOVE,
        SET_VALUE
    }


    private enum MapViewKind {
        SUB4((m, a, ai, b, bi) -> m.subMap(a, ai, b, bi)),
        SUB2((m, a, ai, b, bi) -> (NavigableMap<Integer, Integer>) m.subMap(a, b)),
        HEAD2((m, a, ai, b, bi) -> m.headMap(b, bi)),
        HEAD1((m, a, ai, b, bi) -> (NavigableMap<Integer, Integer>) m.headMap(b)),
        TAIL2((m, a, ai, b, bi) -> m.tailMap(a, ai)),
        TAIL1((m, a, ai, b, bi) -> (NavigableMap<Integer, Integer>) m.tailMap(a)),
        DESC((m, a, ai, b, bi) -> m.descendingMap()),
        DESC_SUB4((m, a, ai, b, bi) -> m.descendingMap().subMap(a, ai, b, bi)),
        SUB4_DESC((m, a, ai, b, bi) -> m.subMap(a, ai, b, bi).descendingMap()),
        DESC_HEAD((m, a, ai, b, bi) -> m.descendingMap().headMap(b, bi)),
        DESC_TAIL((m, a, ai, b, bi) -> m.descendingMap().tailMap(a, ai));

        private final MapViewFactory factory;

        MapViewKind(MapViewFactory factory) {
            this.factory = factory;
        }

        NavigableMap<Integer, Integer> make(NavigableMap<Integer, Integer> m, int a, boolean ai, int b, boolean bi) {
            return factory.make(m, a, ai, b, bi);
        }
    }


    private enum IterKind {
        KEYS(m -> m.keySet().iterator()),
        VALUES(m -> m.values().iterator()),
        ENTRIES(m -> m.entrySet().iterator()),
        DESC_KEYS(m -> m.descendingKeySet().iterator()),
        DESC_ENTRIES(m -> m.descendingMap().entrySet().iterator()),
        NAV_KEYS_DESC_ITER(m -> m.navigableKeySet().descendingIterator());

        private final Function<NavigableMap<Integer, Integer>, Iterator<?>> how;

        IterKind(Function<NavigableMap<Integer, Integer>, Iterator<?>> how) {
            this.how = how;
        }
    }

    protected interface Action {
        void run(NavigableMap<Integer, Integer> tree, NavigableMap<Integer, Integer> reference);
    }

    private interface KvFn {
        void run(NavigableMap<Integer, Integer> t, NavigableMap<Integer, Integer> r, int k, Integer v);
    }

    private interface BulkFn {
        void run(NavigableMap<Integer, Integer> t, NavigableMap<Integer, Integer> r, List<Integer> xs);
    }

    private interface MapViewFactory {
        NavigableMap<Integer, Integer> make(NavigableMap<Integer, Integer> m, int a, boolean ai, int b, boolean bi);
    }

    private record Op(String name, Action body) implements Action {
        @Override
        public void run(NavigableMap<Integer, Integer> t, NavigableMap<Integer, Integer> r) {
            body.run(t, r);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private record ViewSpec(int a, int b, boolean ai, boolean bi, MapViewKind kind) {
        NavigableMap<Integer, Integer> make(NavigableMap<Integer, Integer> m) {
            return kind.make(m, a, ai, b, bi);
        }

        @Override
        public String toString() {
            return kind + (ai ? "[" : "(") + a + ", " + b + (bi ? "]" : ")");
        }
    }

    private record Thrown(Class<?> type) {
    }
}
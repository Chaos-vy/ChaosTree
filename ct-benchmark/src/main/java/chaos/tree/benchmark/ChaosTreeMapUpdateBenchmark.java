package chaos.tree.benchmark;

import chaos.tree.binaryMap.AvlTreeMap;
import chaos.tree.binaryMap.RedBlackTreeMap;
import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.naryMap.BTreeMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


/*
 * Benchmark adapted from the OpenJDK TreeMapUpdate benchmark.
 *
 * Original OpenJDK benchmark:
 * Copyright (c) 2020, 2025, Oracle and/or its affiliates.
 *
 * The workload and benchmark structure are derived from OpenJDK's
 * TreeMapUpdate benchmark and adapted to compare multiple NavigableMap
 * implementations.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@State(Scope.Thread)
public class ChaosTreeMapUpdateBenchmark {

    @Param({"JavaTreeMap",  "BPlusTreeMap"})
    public String mapType;

    @Param({"TreeMap"})
    public String mode;

    @Param({ "100000"})
    public int size;

    @Param({"true", "false"})
    public boolean comparator;

    @Param({"false"})
    public boolean preFill;

    @Param({"0"})
    public long seed;

    private Supplier<NavigableMap<Integer, Integer>> supplier;

    private UnaryOperator<NavigableMap<Integer, Integer>> transformer;

    private Integer[] keys;

    @Setup
    public void setUp() {
        switch(mode) {
            case "TreeMap":
                transformer = map -> map;
                break;
            case "descendingMap":
                transformer = NavigableMap::descendingMap;
                break;
            case "subMap":
                transformer = map -> map.tailMap(0, true);
                break;
            default:
                throw new IllegalStateException(mode);
        }

        Supplier<NavigableMap<Integer, Integer>> baseSupplier;

        if (comparator) {
            baseSupplier = switch (mapType) {
                case "JavaTreeMap"      -> () -> new TreeMap<>(Comparator.reverseOrder());
                case "AvlTreeMap"       -> () -> new AvlTreeMap<>(Comparator.reverseOrder());
                case "RedBlackTreeMap"  -> () -> new RedBlackTreeMap<>(Comparator.reverseOrder());
                case "BTreeMap"         -> () -> new BTreeMap<>(Comparator.reverseOrder());
                case "BPlusTreeMap"     -> () -> new BPlusTreeMap<>(Comparator.reverseOrder());
                default -> throw new IllegalStateException(mapType);
            };
        } else {
            baseSupplier = switch (mapType) {
                case "JavaTreeMap"      -> TreeMap::new;
                case "AvlTreeMap"       -> AvlTreeMap::new;
                case "RedBlackTreeMap"  -> RedBlackTreeMap::new;
                case "BTreeMap"         -> BTreeMap::new;
                case "BPlusTreeMap"     -> BPlusTreeMap::new;
                default -> throw new IllegalStateException(mapType);
            };
        }
        
        supplier = baseSupplier;
        
        keys = IntStream.range(0, size).boxed().toArray(Integer[]::new);
        Random rnd = seed == 0 ? new Random() : new Random(seed);
        Collections.shuffle(Arrays.asList(keys), rnd);
        
        if (preFill) {
            NavigableMap<Integer, Integer> template = baseSupplier.get();
            for (Integer k : keys) {
                template.put(k, k);
            }
            supplier = () -> {
                NavigableMap<Integer, Integer> map = baseSupplier.get();
                map.putAll(template);
                return map;
            };
        }
    }

    @Benchmark
    public Map<Integer, Integer> baseline() {
        return transformer.apply(supplier.get());
    }

    @Benchmark
    public Map<Integer, Integer> put(Blackhole bh) {
        Map<Integer, Integer> map = transformer.apply(supplier.get());
        Integer[] keys = this.keys;
        for (Integer key : keys) {
            bh.consume(map.put(key, key));
        }
        return map;
    }

    @Benchmark
    public Map<Integer, Integer> putIfAbsent(Blackhole bh) {
        Map<Integer, Integer> map = transformer.apply(supplier.get());
        Integer[] keys = this.keys;
        for (Integer key : keys) {
            bh.consume(map.putIfAbsent(key, key));
        }
        return map;
    }

    @Benchmark
    public Map<Integer, Integer> computeIfAbsent(Blackhole bh) {
        Map<Integer, Integer> map = transformer.apply(supplier.get());
        Integer[] keys = this.keys;
        for (Integer key : keys) {
            bh.consume(map.computeIfAbsent(key, k -> k));
        }
        return map;
    }

    @Benchmark
    public Map<Integer, Integer> compute(Blackhole bh) {
        Map<Integer, Integer> map = transformer.apply(supplier.get());
        Integer[] keys = this.keys;
        for (Integer key : keys) {
            bh.consume(map.compute(key, (k, old) -> k));
        }
        return map;
    }

    @Benchmark
    public Map<Integer, Integer> computeIfPresent(Blackhole bh) {
        Map<Integer, Integer> map = transformer.apply(supplier.get());
        Integer[] keys = this.keys;
        for (Integer key : keys) {
            bh.consume(map.computeIfPresent(key, (k, old) -> k));
        }
        return map;
    }

    @Benchmark
    public Map<Integer, Integer> merge(Blackhole bh) {
        Map<Integer, Integer> map = transformer.apply(supplier.get());
        Integer[] keys = this.keys;
        for (Integer key : keys) {
            bh.consume(map.merge(key, key, (k1, k2) -> k1));
        }
        return map;
    }
}

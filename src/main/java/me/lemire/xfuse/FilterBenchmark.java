package me.lemire.xfuse;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.Arena;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class FilterBenchmark {

    private XorFilter.BinaryFuse8Filter filter;
    private Arena arena;
    private long[] testKeys;
    private final int NUM_KEYS = 1000000;

    @Setup
    public void setup() {
        // Create 1000,000 keys
        testKeys = new long[NUM_KEYS];
        for (int i = 0; i < testKeys.length; i++) {
            testKeys[i] = i * 2; // even numbers
        }

        arena = Arena.ofConfined();
        filter = new XorFilter.BinaryFuse8Filter(arena);

        try {
            if (!filter.allocate(testKeys.length)) {
                throw new RuntimeException("Failed to allocate filter");
            }

            if (!filter.populate(testKeys)) {
                throw new RuntimeException("Failed to populate filter");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @TearDown
    public void tearDown() {
        if (filter != null) {
            filter.close();
        }
        if (arena != null) {
            arena.close();
        }
    }

@Benchmark
@OperationsPerInvocation(NUM_KEYS)
public void benchmarkContainsExisting(Blackhole blackhole) throws Throwable {
    // Query existing keys
    for (long key : testKeys) {
        if(!filter.contains(key)) {
            throw new RuntimeException("Key should exist: " + key);
        }
    }
}

    @Benchmark
    @OperationsPerInvocation(NUM_KEYS)
    public void benchmarkContainsNonExisting(Blackhole blackhole) throws Throwable {
        // Query non-existing keys (odd numbers)
        int fp = 0;
        for (int i = 0; i < testKeys.length; i++) {
            long key = i * 2 + 1; // odd numbers
            if(filter.contains(key)) {
                fp++;
            }
        }
        if(fp > 10000) {
            throw new RuntimeException("Too many false positives: " + fp);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @OperationsPerInvocation(NUM_KEYS)
    public void benchmarkContainsExistingThroughput(Blackhole blackhole) throws Throwable {
        // Query existing keys
        for (long key : testKeys) {
            if(!filter.contains(key)) {
                throw new RuntimeException("Key should exist: " + key);
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @OperationsPerInvocation(NUM_KEYS)
    public void benchmarkContainsNonExistingThroughput(Blackhole blackhole) throws Throwable {
        // Query non-existing keys (odd numbers)
        int fp = 0;
        for (int i = 0; i < testKeys.length; i++) {
            long key = i * 2 + 1; // odd numbers
            if(filter.contains(key)) {
                fp++;
            }
        }
        if(fp > 10000) {
            throw new RuntimeException("Too many false positives: " + fp);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FilterBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}

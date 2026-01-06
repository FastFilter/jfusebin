# The jfusebin Java Library

A Java library that provides access to high-performance XOR and Binary Fuse filters using Java's Foreign Function & Memory (FFM) API.

## Overview

This library wraps a C library, providing Java bindings for probabilistic data structures that can efficiently test set membership with low false positive rates.

## Features


- **Xor8 Filter**, - **Binary Fuse8 Filter**: 0.39% false positive rate, slightly over 8 bits per key, for larger inputs, the Binary Fuse8 Filter is smaller and faster
- **Xor16 Filter**, - **Binary Fuse16 Filter**: 0.0015% false positive rate, slightly over 16 bits per key, for larger inputs, the Binary Fuse16 Filter is smaller and faster


## Requirements

- Java 22+
- GCC, LLVM or compatible C compiler

## Building

1. Clone this repository
2. Run Maven to build:

```bash
mvn clean compile exec:exec@compile-native
```




## Usage

```java
import com.example.xfuse.XorFilter;
import jdk.incubator.foreign.ResourceScope;

public class Example {
    public static void main(String[] args) throws Throwable {
        long[] keys = {1, 2, 3, 4, 5};

        try (ResourceScope scope = ResourceScope.newConfinedScope();
             var filter = new XorFilter.Xor8Filter(scope)) {

            // Allocate and populate the filter
            filter.allocate(keys.length);
            filter.populate(keys);

            // Test membership
            System.out.println(filter.contains(1));  // true
            System.out.println(filter.contains(10)); // false (with high probability)
        }
    }
}
```

## Running

To run the example:

```bash
mvn exec:java -Dexec.mainClass="com.example.xfuse.Example" -q
```

Or manually:

```bash
java -cp target/classes com.example.xfuse.Example
```

## Benchmarks

The library includes JMH (Java Microbenchmark Harness) benchmarks for accurate performance measurement.

To run the benchmarks:

```bash
mvn clean compile exec:exec@compile-native
java --enable-native-access=ALL-UNNAMED -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) com.example.xfuse.FilterBenchmark
```

This runs benchmarks for BinaryFuse8 filter with 1,000,000 elements, measuring:
- `benchmarkContainsExisting`: Query time for existing keys (per key)
- `benchmarkContainsNonExisting`: Query time for non-existing keys (per key)  

Example JMH output:
```
Benchmark                                     Mode  Cnt   Score   Error  Units
FilterBenchmark.benchmarkContainsExisting     avgt    5  19,850 ± 0,277  ns/op
FilterBenchmark.benchmarkContainsNonExisting  avgt    5  20,077 ± 0,375  ns/op
```

Key results:
- **Existing keys**: ~19.85 nanoseconds per query
- **Non-existing keys**: ~20.08 nanoseconds per query  
- **Single query**: ~17.41 nanoseconds per query
- Low memory usage: ~1.2 MB for 1,000,000 elements

## Architecture

- **Native Layer**: C wrapper functions that expose the xorfilter API
- **Java Layer**: FFM API bindings that manage memory and call native functions
- **Resource Management**: Uses ResourceScope for automatic memory management


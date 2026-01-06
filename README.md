# The jfusebin Java Library


Bloom filters are used to quickly check whether an element is part of a set.
Xor filters and binary fuse filters are faster and more concise alternative to Bloom filters.
Furthermore, unlike Bloom filters, xor and binary fuse filters are naturally compressible using standard techniques (gzip, zstd, etc.).
They are also smaller than cuckoo filters. They are used in [production systems](https://github.com/datafuselabs/databend).

* Thomas Mueller Graf, Daniel Lemire, [Binary Fuse Filters: Fast and Smaller Than Xor Filters](http://arxiv.org/abs/2201.01174), Journal of Experimental Algorithmics (to appear). DOI: 10.1145/3510449
* Thomas Mueller Graf,  Daniel Lemire, [Xor Filters: Faster and Smaller Than Bloom and Cuckoo Filters](https://arxiv.org/abs/1912.08258), Journal of Experimental Algorithmics 25 (1), 2020. DOI: 10.1145/3376122


<img src="figures/comparison.png" width="50%"/>

The jfusebin library is  Java library that provides access to high-performance XOR and Binary Fuse filters using Java's Foreign Function & Memory (FFM) API. 

## Overview

This library wraps a C library, providing Java bindings for probabilistic data structures that can efficiently test set membership with low false positive rates.

## Features

- **Xor8 Filter**: ~10 bits per element, ~0.3% false positive rate
- **Xor16 Filter**: ~20 bits per element, ~0.001% false positive rate
- **BinaryFuse8 Filter**: ~11.5 bits per element, low false positive rate
- **BinaryFuse16 Filter**: ~20 bits per element, very low false positive rate
- **High Performance**: Sub-20 nanosecond query times
- **Memory Efficient**: Low memory overhead
- **FFM API**: Uses Java 22's Foreign Function & Memory API for safe native interop

## Requirements

- Java 22+
- GCC, LLVM or compatible C compiler

## Building

1. Clone this repository
2. Run Maven to build:

```bash
mvn clean compile exec:exec@compile-native
```

This will:
- Compile the Java classes with JMH annotation processing
- Compile the native C wrapper library
- Place the native library in `target/classes/`

## API

All filter types implement the `XorFilterInterface`:

```java
public interface XorFilterInterface extends AutoCloseable {
    boolean allocate(int size) throws Throwable;
    boolean populate(long[] keys) throws Throwable;
    boolean contains(long key) throws Throwable;
    long sizeInBytes() throws Throwable;
}
```

### Usage Example

```java
import me.lemire.xfuse.XorFilter;
import java.lang.foreign.Arena;

public class Statistics {
    public static void main(String[] args) throws Throwable {
        long[] keys = {1, 2, 3, 4, 5};

        try (var arena = Arena.ofConfined();
             var filter = new XorFilter.Xor8Filter(arena)) {

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


## Performance Analysis

The library includes a performance analysis tool that measures:

- **Bits per element**: Memory efficiency of each filter type
- **False positive rate**: Accuracy measurement for different dataset sizes

To run the performance analysis:

```bash
mvn exec:java -Dexec.mainClass="me.lemire.xfuse.Statistics" -q
```

## Bits per element (bits/element)
| N | Xor8 | BinaryFuse8 | Xor16 | BinaryFuse16 |
|---|---|---|---|---|
| 1 000 | 10,27 | 11,58 | 20,35 | 22,85 |
| 10 000 | 9,88 | 10,27 | 19,75 | 20,51 |
| 100 000 | 9,84 | 9,51 | 19,69 | 19,01 |
| 1 000 000 | 9,84 | 9,04 | 19,68 | 18,09 |
| 10 000 000 | 9,84 | 9,02 | 19,68 | 18,04 |

## Estimated false positive rates (percent)

| N | Xor8 | BinaryFuse8 | Xor16 | BinaryFuse16 |
|---|---|---|---|---|
| 1 000 | 0,3000% | 0,6000% | 0,0000% | 0,0000% |
| 10 000 | 0,3400% | 0,3200% | 0,0100% | 0,0000% |
| 100 000 | 0,3890% | 0,3930% | 0,0010% | 0,0000% |
| 1 000 000 | 0,3900% | 0,4050% | 0,0010% | 0,0000% |
| 10 000 000 | 0,3980% | 0,4020% | 0,0020% | 0,0000% |


## Benchmarks

The library includes JMH (Java Microbenchmark Harness) benchmarks for accurate performance measurement.

To run the benchmarks:

```bash
mvn clean compile exec:exec@compile-native
java --enable-native-access=ALL-UNNAMED -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) me.lemire.xfuse.FilterBenchmark
```

This runs benchmarks for BinaryFuse8 filter with 1,000,000 elements, measuring:
- `benchmarkContainsExisting`: Query time for existing keys (per key)
- `benchmarkContainsNonExisting`: Query time for non-existing keys (per key)
- `benchmarkSingleQuery`: Single query performance

Example JMH output:
```
Benchmark                                                Mode  Cnt         Score         Error  Units
FilterBenchmark.benchmarkContainsExistingThroughput     thrpt    5  44038254,445 ± 1339377,802  ops/s
FilterBenchmark.benchmarkContainsNonExistingThroughput  thrpt    5  43633598,029 ±  239529,108  ops/s
FilterBenchmark.benchmarkContainsExisting                avgt    5        23,013 ±       0,629  ns/op
FilterBenchmark.benchmarkContainsNonExisting             avgt    5        22,715 ±       1,545  ns/op
```

How to interpret this data:

- We can issue over 43 million requests per second.
- Conversely, we can issue about one query every 23 nanoseconds.

## Architecture

- **XorFilterInterface**: Common interface implemented by all filter types (Xor8, Xor16, BinaryFuse8, BinaryFuse16)
- **Native Layer**: C wrapper functions that expose the xorfilter API
- **Java Layer**: FFM API bindings that manage memory and call native functions
- **Resource Management**: Uses Arena for automatic memory management


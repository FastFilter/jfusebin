# XorFilter Java Library

A Java library that provides access to high-performance XOR filters using Java's Foreign Function & Memory (FFM) API.

## Overview

This library wraps the [xorfilter](https://github.com/FastFilter/xorfilter) C library, providing Java bindings for probabilistic data structures that can efficiently test set membership with low false positive rates.

## Features

- **Xor8 Filter**: ~0.3% false positive rate, 8 bits per key
- **Xor16 Filter**: Lower false positive rate, 16 bits per key
- **Binary Fuse8 Filter**: Alternative implementation with different performance characteristics
- Uses Java 21's incubator FFM API for direct native calls
- Memory-efficient and high-performance

## Requirements

- Java 21+
- GCC or compatible C compiler
- macOS/Linux (Windows support would require additional configuration)

## Building

1. Clone this repository
2. Run Maven to build:

```bash
mvn clean compile
```

This will:
- Compile the native C wrapper library
- Compile the Java classes
- Place the native library in `target/classes/`

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
mvn exec:java -Dexec.mainClass="com.example.xfuse.Example" -Dexec.args=""
```

Or manually:

```bash
java --add-modules jdk.incubator.foreign --enable-native-access ALL-UNNAMED -cp target/classes com.example.xfuse.Example
```

## Architecture

- **Native Layer**: C wrapper functions that expose the xorfilter API
- **Java Layer**: FFM API bindings that manage memory and call native functions
- **Resource Management**: Uses ResourceScope for automatic memory management

## Performance

The filters provide:
- Fast construction and lookup
- Low memory overhead (~8-16 bits per key)
- Configurable false positive rates

## Limitations

- Currently supports only Xor8 filter in the Java API (Xor16 and BinaryFuse8 can be added similarly)
- Requires manual memory management through ResourceScope
- Native library must be compiled for the target platform

## License

This project wraps the xorfilter library. Please refer to the original library's license for usage terms.
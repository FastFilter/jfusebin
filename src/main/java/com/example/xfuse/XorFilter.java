package com.example.xfuse;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteOrder;

/**
 * Java wrapper for xorfilter using Java 22 FFM API
 */
public class XorFilter {

    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup lookup;

    static {
        // Load the native library
        try {
            System.load(System.getProperty("user.dir") + "/target/classes/libxfuse.dylib");
        } catch (UnsatisfiedLinkError e) {
            // Try loading from classpath
            System.loadLibrary("xfuse");
        }
        lookup = SymbolLookup.loaderLookup();
    }

    // Function descriptors for xor8 functions
    private static final FunctionDescriptor xor8_allocate_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor xor8_populate_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor xor8_contain_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.JAVA_LONG,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor xor8_free_desc = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor xor8_size_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_LONG,
        ValueLayout.ADDRESS
    );

    // Method handles
    private static final MethodHandle xor8_allocate;
    private static final MethodHandle xor8_populate;
    private static final MethodHandle xor8_contain;
    private static final MethodHandle xor8_free;
    private static final MethodHandle xor8_size;

    static {
        try {
            xor8_allocate = linker.downcallHandle(
                lookup.find("xfuse_xor8_allocate").orElseThrow(),
                xor8_allocate_desc
            );
            xor8_populate = linker.downcallHandle(
                lookup.find("xfuse_xor8_populate").orElseThrow(),
                xor8_populate_desc
            );
            xor8_contain = linker.downcallHandle(
                lookup.find("xfuse_xor8_contain").orElseThrow(),
                xor8_contain_desc
            );
            xor8_free = linker.downcallHandle(
                lookup.find("xfuse_xor8_free").orElseThrow(),
                xor8_free_desc
            );
            xor8_size = linker.downcallHandle(
                lookup.find("xfuse_xor8_size_in_bytes").orElseThrow(),
                xor8_size_desc
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize native method handles", e);
        }
    }

    // Struct layout for xor8_t
    private static final MemoryLayout XOR8_LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_LONG.withName("seed"),
        ValueLayout.JAVA_LONG.withName("blockLength"),
        ValueLayout.ADDRESS.withName("fingerprints")
    );

    /**
     * Xor8 filter implementation
     */
    public static class Xor8Filter implements AutoCloseable {
        private final MemorySegment filterSegment;
        private final Arena arena;

        public Xor8Filter(Arena arena) {
            this.arena = arena;
            this.filterSegment = arena.allocate(XOR8_LAYOUT);
        }

        public boolean allocate(int size) throws Throwable {
            return (boolean) xor8_allocate.invokeExact(size, filterSegment);
        }

        public boolean populate(long[] keys) throws Throwable {
            try (Arena tempArena = Arena.ofConfined()) {
                MemorySegment keysSegment = tempArena.allocateFrom(ValueLayout.JAVA_LONG, keys);
                return (boolean) xor8_populate.invokeExact(keysSegment, keys.length, filterSegment);
            }
        }

        public boolean contains(long key) throws Throwable {
            return (boolean) xor8_contain.invokeExact(key, filterSegment);
        }

        public long sizeInBytes() throws Throwable {
            return (long) xor8_size.invokeExact(filterSegment);
        }

        @Override
        public void close() {
            try {
                xor8_free.invokeExact(filterSegment);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to free xor8 filter", e);
            }
        }
    }

}
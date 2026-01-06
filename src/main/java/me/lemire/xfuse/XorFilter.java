package me.lemire.xfuse;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteOrder;

/**
 * Java wrapper for xorfilter using Java 22 FFM API
 */
public class XorFilter {

    /**
     * Common interface for all XOR filter types
     */
    public interface XorFilterInterface extends AutoCloseable {
        boolean allocate(int size) throws Throwable;
        boolean populate(long[] keys) throws Throwable;
        boolean contains(long key) throws Throwable;
        long sizeInBytes() throws Throwable;
    }

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

    // Function descriptors for binary_fuse16 functions
    private static final FunctionDescriptor binary_fuse16_allocate_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor binary_fuse16_populate_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor binary_fuse16_contain_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.JAVA_LONG,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor binary_fuse16_free_desc = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor binary_fuse16_size_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_LONG,
        ValueLayout.ADDRESS
    );

    // Function descriptors for xor16 functions
    private static final FunctionDescriptor xor16_allocate_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor xor16_populate_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor xor16_contain_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.JAVA_LONG,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor xor16_free_desc = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor xor16_size_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_LONG,
        ValueLayout.ADDRESS
    );

    // Function descriptors for binary_fuse8 functions
    private static final FunctionDescriptor binary_fuse8_allocate_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor binary_fuse8_populate_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor binary_fuse8_contain_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.JAVA_LONG,
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor binary_fuse8_free_desc = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS
    );

    private static final FunctionDescriptor binary_fuse8_size_desc = FunctionDescriptor.of(
        ValueLayout.JAVA_LONG,
        ValueLayout.ADDRESS
    );

    // Method handles
    private static final MethodHandle xor8_allocate;
    private static final MethodHandle xor8_populate;
    private static final MethodHandle xor8_contain;
    private static final MethodHandle xor8_free;
    private static final MethodHandle xor8_size;

    private static final MethodHandle binary_fuse16_allocate;
    private static final MethodHandle binary_fuse16_populate;
    private static final MethodHandle binary_fuse16_contain;
    private static final MethodHandle binary_fuse16_free;
    private static final MethodHandle binary_fuse16_size;

    private static final MethodHandle xor16_allocate;
    private static final MethodHandle xor16_populate;
    private static final MethodHandle xor16_contain;
    private static final MethodHandle xor16_free;
    private static final MethodHandle xor16_size;

    private static final MethodHandle binary_fuse8_allocate;
    private static final MethodHandle binary_fuse8_populate;
    private static final MethodHandle binary_fuse8_contain;
    private static final MethodHandle binary_fuse8_free;
    private static final MethodHandle binary_fuse8_size;

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
            binary_fuse16_allocate = linker.downcallHandle(
                lookup.find("xfuse_binary_fuse16_allocate").orElseThrow(),
                binary_fuse16_allocate_desc
            );
            binary_fuse16_populate = linker.downcallHandle(
                lookup.find("xfuse_binary_fuse16_populate").orElseThrow(),
                binary_fuse16_populate_desc
            );
            binary_fuse16_contain = linker.downcallHandle(
                lookup.find("xfuse_binary_fuse16_contain").orElseThrow(),
                binary_fuse16_contain_desc
            );
            binary_fuse16_free = linker.downcallHandle(
                lookup.find("xfuse_binary_fuse16_free").orElseThrow(),
                binary_fuse16_free_desc
            );
            binary_fuse16_size = linker.downcallHandle(
                lookup.find("xfuse_binary_fuse16_size_in_bytes").orElseThrow(),
                binary_fuse16_size_desc
            );
            xor16_allocate = linker.downcallHandle(
                lookup.find("xfuse_xor16_allocate").orElseThrow(),
                xor16_allocate_desc
            );
            xor16_populate = linker.downcallHandle(
                lookup.find("xfuse_xor16_populate").orElseThrow(),
                xor16_populate_desc
            );
            xor16_contain = linker.downcallHandle(
                lookup.find("xfuse_xor16_contain").orElseThrow(),
                xor16_contain_desc
            );
            xor16_free = linker.downcallHandle(
                lookup.find("xfuse_xor16_free").orElseThrow(),
                xor16_free_desc
            );
            xor16_size = linker.downcallHandle(
                lookup.find("xfuse_xor16_size_in_bytes").orElseThrow(),
                xor16_size_desc
            );
            binary_fuse8_allocate = linker.downcallHandle(
                lookup.find("xfuse_binary_fuse8_allocate").orElseThrow(),
                binary_fuse8_allocate_desc
            );
            binary_fuse8_populate = linker.downcallHandle(
                lookup.find("xfuse_binary_fuse8_populate").orElseThrow(),
                binary_fuse8_populate_desc
            );
            binary_fuse8_contain = linker.downcallHandle(
                lookup.find("xfuse_binary_fuse8_contain").orElseThrow(),
                binary_fuse8_contain_desc
            );
            binary_fuse8_free = linker.downcallHandle(
                lookup.find("xfuse_binary_fuse8_free").orElseThrow(),
                binary_fuse8_free_desc
            );
            binary_fuse8_size = linker.downcallHandle(
                lookup.find("xfuse_binary_fuse8_size_in_bytes").orElseThrow(),
                binary_fuse8_size_desc
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

    // Struct layout for binary_fuse16_t
    private static final MemoryLayout BINARY_FUSE16_LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_LONG.withName("Seed"),
        ValueLayout.JAVA_INT.withName("Size"),
        ValueLayout.JAVA_INT.withName("SegmentLength"),
        ValueLayout.JAVA_INT.withName("SegmentLengthMask"),
        ValueLayout.JAVA_INT.withName("SegmentCount"),
        ValueLayout.JAVA_INT.withName("SegmentCountLength"),
        ValueLayout.JAVA_INT.withName("ArrayLength"),
        ValueLayout.ADDRESS.withName("Fingerprints")
    );

    // Struct layout for xor16_t
    private static final MemoryLayout XOR16_LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_LONG.withName("seed"),
        ValueLayout.JAVA_LONG.withName("blockLength"),
        ValueLayout.ADDRESS.withName("fingerprints")
    );

    // Struct layout for binary_fuse8_t
    private static final MemoryLayout BINARY_FUSE8_LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_LONG.withName("Seed"),
        ValueLayout.JAVA_INT.withName("Size"),
        ValueLayout.JAVA_INT.withName("SegmentLength"),
        ValueLayout.JAVA_INT.withName("SegmentLengthMask"),
        ValueLayout.JAVA_INT.withName("SegmentCount"),
        ValueLayout.JAVA_INT.withName("SegmentCountLength"),
        ValueLayout.JAVA_INT.withName("ArrayLength"),
        ValueLayout.ADDRESS.withName("Fingerprints")
    );

    /**
     * Xor8 filter implementation
     */
    public static class Xor8Filter implements XorFilterInterface {
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

    /**
     * BinaryFuse16 filter implementation
     */
    public static class BinaryFuse16Filter implements XorFilterInterface {
        private final MemorySegment filterSegment;
        private final Arena arena;

        public BinaryFuse16Filter(Arena arena) {
            this.arena = arena;
            this.filterSegment = arena.allocate(BINARY_FUSE16_LAYOUT);
        }

        public boolean allocate(int size) throws Throwable {
            return (boolean) binary_fuse16_allocate.invokeExact(size, filterSegment);
        }

        public boolean populate(long[] keys) throws Throwable {
            try (Arena tempArena = Arena.ofConfined()) {
                MemorySegment keysSegment = tempArena.allocateFrom(ValueLayout.JAVA_LONG, keys);
                return (boolean) binary_fuse16_populate.invokeExact(keysSegment, keys.length, filterSegment);
            }
        }

        public boolean contains(long key) throws Throwable {
            return (boolean) binary_fuse16_contain.invokeExact(key, filterSegment);
        }

        public long sizeInBytes() throws Throwable {
            return (long) binary_fuse16_size.invokeExact(filterSegment);
        }

        @Override
        public void close() {
            try {
                binary_fuse16_free.invokeExact(filterSegment);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to free binary_fuse16 filter", e);
            }
        }
    }

    /**
     * Xor16 filter implementation
     */
    public static class Xor16Filter implements XorFilterInterface {
        private final MemorySegment filterSegment;
        private final Arena arena;

        public Xor16Filter(Arena arena) {
            this.arena = arena;
            this.filterSegment = arena.allocate(XOR16_LAYOUT);
        }

        public boolean allocate(int size) throws Throwable {
            return (boolean) xor16_allocate.invokeExact(size, filterSegment);
        }

        public boolean populate(long[] keys) throws Throwable {
            try (Arena tempArena = Arena.ofConfined()) {
                MemorySegment keysSegment = tempArena.allocateFrom(ValueLayout.JAVA_LONG, keys);
                return (boolean) xor16_populate.invokeExact(keysSegment, keys.length, filterSegment);
            }
        }

        public boolean contains(long key) throws Throwable {
            return (boolean) xor16_contain.invokeExact(key, filterSegment);
        }

        public long sizeInBytes() throws Throwable {
            return (long) xor16_size.invokeExact(filterSegment);
        }

        @Override
        public void close() {
            try {
                xor16_free.invokeExact(filterSegment);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to free xor16 filter", e);
            }
        }
    }

    /**
     * BinaryFuse8 filter implementation
     */
    public static class BinaryFuse8Filter implements XorFilterInterface {
        private final MemorySegment filterSegment;
        private final Arena arena;

        public BinaryFuse8Filter(Arena arena) {
            this.arena = arena;
            this.filterSegment = arena.allocate(BINARY_FUSE8_LAYOUT);
        }

        public boolean allocate(int size) throws Throwable {
            return (boolean) binary_fuse8_allocate.invokeExact(size, filterSegment);
        }

        public boolean populate(long[] keys) throws Throwable {
            try (Arena tempArena = Arena.ofConfined()) {
                MemorySegment keysSegment = tempArena.allocateFrom(ValueLayout.JAVA_LONG, keys);
                return (boolean) binary_fuse8_populate.invokeExact(keysSegment, keys.length, filterSegment);
            }
        }

        public boolean contains(long key) throws Throwable {
            return (boolean) binary_fuse8_contain.invokeExact(key, filterSegment);
        }

        public long sizeInBytes() throws Throwable {
            return (long) binary_fuse8_size.invokeExact(filterSegment);
        }

        @Override
        public void close() {
            try {
                binary_fuse8_free.invokeExact(filterSegment);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to free binary_fuse8 filter", e);
            }
        }
    }

}

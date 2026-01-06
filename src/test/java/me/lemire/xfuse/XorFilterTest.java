package me.lemire.xfuse;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.Arena;

public class XorFilterTest {

    @Test
    public void testXor8Filter() throws Throwable {
        long[] keys = {1, 2, 3, 4, 5, 100, 200, 300};

        try (Arena arena = Arena.ofConfined();
             XorFilter.Xor8Filter filter = new XorFilter.Xor8Filter(arena)) {

            assertTrue(filter.allocate(keys.length));
            assertTrue(filter.populate(keys));

            // Test contains
            assertTrue(filter.contains(1));
            assertTrue(filter.contains(100));
            assertFalse(filter.contains(10)); // not in set
            assertFalse(filter.contains(999)); // not in set

            // Test size
            assertTrue(filter.sizeInBytes() > 0);
        }
    }

    @Test
    public void testXor16Filter() throws Throwable {
        long[] keys = {1, 2, 3, 4, 5, 100, 200, 300};

        try (Arena arena = Arena.ofConfined();
             XorFilter.Xor16Filter filter = new XorFilter.Xor16Filter(arena)) {

            assertTrue(filter.allocate(keys.length));
            assertTrue(filter.populate(keys));

            // Test contains
            assertTrue(filter.contains(1));
            assertTrue(filter.contains(100));
            assertFalse(filter.contains(10)); // not in set
            assertFalse(filter.contains(999)); // not in set

            // Test size
            assertTrue(filter.sizeInBytes() > 0);
        }
    }

    @Test
    public void testBinaryFuse8Filter() throws Throwable {
        long[] keys = {1, 2, 3, 4, 5, 100, 200, 300};

        try (Arena arena = Arena.ofConfined();
             XorFilter.BinaryFuse8Filter filter = new XorFilter.BinaryFuse8Filter(arena)) {

            assertTrue(filter.allocate(keys.length));
            assertTrue(filter.populate(keys));

            // Test contains
            assertTrue(filter.contains(1));
            assertTrue(filter.contains(100));
            assertFalse(filter.contains(10)); // not in set
            assertFalse(filter.contains(999)); // not in set

            // Test size
            assertTrue(filter.sizeInBytes() > 0);
        }
    }

    @Test
    public void testBinaryFuse16Filter() throws Throwable {
        long[] keys = {1, 2, 3, 4, 5, 100, 200, 300};

        try (Arena arena = Arena.ofConfined();
             XorFilter.BinaryFuse16Filter filter = new XorFilter.BinaryFuse16Filter(arena)) {

            assertTrue(filter.allocate(keys.length));
            assertTrue(filter.populate(keys));

            // Test contains
            assertTrue(filter.contains(1));
            assertTrue(filter.contains(100));
            assertFalse(filter.contains(10)); // not in set
            assertFalse(filter.contains(999)); // not in set

            // Test size
            assertTrue(filter.sizeInBytes() > 0);
        }
    }
}

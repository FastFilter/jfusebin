package com.example.xfuse;

import java.lang.foreign.Arena;
import java.util.Arrays;

/**
 * Example usage of XorFilter
 */
public class Example {
    public static void main(String[] args) throws Throwable {
        // Create some test keys
        long[] keys = {1, 2, 3, 4, 5, 100, 200, 300};

        try (var arena = Arena.ofConfined();
             var filter = new XorFilter.Xor8Filter(arena)) {

            // Allocate the filter
            if (!filter.allocate(keys.length)) {
                System.err.println("Failed to allocate filter");
                return;
            }

            // Populate with keys
            if (!filter.populate(keys)) {
                System.err.println("Failed to populate filter");
                return;
            }

            System.out.println("Filter size: " + filter.sizeInBytes() + " bytes");

            // Test membership
            System.out.println("Contains 1: " + filter.contains(1));
            System.out.println("Contains 10: " + filter.contains(10)); // Should be false
            System.out.println("Contains 100: " + filter.contains(100));
        }
    }
}
package me.lemire.xfuse;

import java.lang.foreign.Arena;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.List;

/**
 * Statistics usage of XorFilter with performance analysis for all filter types
 */
public class Statistics {
    public static void main(String[] args) throws Throwable {
        int[] sizes = {1_000, 10_000, 100_000, 1_000_000, 10_000_000};

        // Filter factories in the desired column order
        List<String> names = List.of("Xor8", "BinaryFuse8", "Xor16", "BinaryFuse16");
        List<Function<Arena, XorFilter.XorFilterInterface>> factories = List.of(
            arena -> new XorFilter.Xor8Filter(arena),
            arena -> new XorFilter.BinaryFuse8Filter(arena),
            arena -> new XorFilter.Xor16Filter(arena),
            arena -> new XorFilter.BinaryFuse16Filter(arena)
        );

        List<double[]> bitsTable = new ArrayList<>();
        List<double[]> fpTable = new ArrayList<>();

        for (int size : sizes) {
            // Create test keys (even numbers)
            long[] keys = new long[size];
            for (int i = 0; i < size; i++) {
                keys[i] = i * 2L;
            }

            double[] bitsRow = new double[names.size()];
            double[] fpRow = new double[names.size()];

            for (int i = 0; i < factories.size(); i++) {
                String name = names.get(i);
                Function<Arena, XorFilter.XorFilterInterface> factory = factories.get(i);
                Metrics m = evaluateFilter(name, factory, keys, size);
                if (m.ok) {
                    bitsRow[i] = m.bitsPerElement;
                    fpRow[i] = m.falsePositiveRate;
                } else {
                    bitsRow[i] = Double.NaN;
                    fpRow[i] = Double.NaN;
                }
            }

            bitsTable.add(bitsRow);
            fpTable.add(fpRow);
        }

        // Print first table: bits per element
        System.out.println("\n# Bits per element (bits/element)");
        // Header
        StringBuilder header = new StringBuilder();
        header.append("| N ");
        for (String n : names) header.append("| ").append(n).append(" ");
        header.append("|");
        System.out.println(header.toString());
        // Separator
        StringBuilder sep = new StringBuilder();
        sep.append("|---");
        for (int i = 0; i < names.size(); i++) sep.append("|---");
        sep.append("|");
        System.out.println(sep.toString());

        for (int r = 0; r < sizes.length; r++) {
            StringBuilder row = new StringBuilder();
            row.append("| ").append(String.format("%,d", sizes[r])).append(" ");
            double[] bitsRow = bitsTable.get(r);
            for (int c = 0; c < bitsRow.length; c++) {
                if (Double.isNaN(bitsRow[c])) {
                    row.append("| - ");
                } else {
                    row.append("| ").append(String.format("%.2f", bitsRow[c])).append(" ");
                }
            }
            row.append("|");
            System.out.println(row.toString());
        }

        // Print second table: false positive rates
        System.out.println("\n# Estimated false positive rates (percent)");
        System.out.println(header.toString());
        System.out.println(sep.toString());
        for (int r = 0; r < sizes.length; r++) {
            StringBuilder row = new StringBuilder();
            row.append("| ").append(String.format("%,d", sizes[r])).append(" ");
            double[] fprows = fpTable.get(r);
            for (int c = 0; c < fprows.length; c++) {
                if (Double.isNaN(fprows[c])) {
                    row.append("| - ");
                } else {
                    row.append("| ").append(String.format("%.4f%%", fprows[c] * 100.0)).append(" ");
                }
            }
            row.append("|");
            System.out.println(row.toString());
        }
    }

    private static void testFilter(String filterName, Function<Arena, XorFilter.XorFilterInterface> filterFactory, long[] keys, int numElements) {
        try (var arena = Arena.ofConfined();
             var filter = filterFactory.apply(arena)) {

            System.out.println("\n-- " + filterName + " --");

            // Allocate and populate the filter
            if (!filter.allocate(keys.length)) {
                System.err.println("Failed to allocate " + filterName + " filter");
                return;
            }

            if (!filter.populate(keys)) {
                System.err.println("Failed to populate " + filterName + " filter");
                return;
            }

            // Calculate bits per element
            long filterSizeBytes = filter.sizeInBytes();
            double bitsPerElement = (filterSizeBytes * 8.0) / numElements;
            System.out.println("Filter size: " + filterSizeBytes + " bytes");
            System.out.printf("Bits per element: %.2f%n", bitsPerElement);

            // Test false positive rate
            estimateFalsePositiveRate(filter, numElements);
        } catch (Throwable e) {
            System.err.println("Error testing " + filterName + ": " + e.getMessage());
        }
    }

    private static class Metrics {
        double bitsPerElement;
        double falsePositiveRate;
        boolean ok;
        String errorMsg;
    }

    private static Metrics evaluateFilter(String filterName, Function<Arena, XorFilter.XorFilterInterface> filterFactory, long[] keys, int numElements) {
        Metrics m = new Metrics();
        m.ok = false;
        try (var arena = Arena.ofConfined(); var filter = filterFactory.apply(arena)) {
            if (!filter.allocate(keys.length)) {
                m.errorMsg = "allocate failed";
                return m;
            }
            if (!filter.populate(keys)) {
                m.errorMsg = "populate failed";
                return m;
            }

            long filterSizeBytes = filter.sizeInBytes();
            m.bitsPerElement = (filterSizeBytes * 8.0) / numElements;

            // False positive test using odd numbers
            int testSamples = Math.min(100_000, numElements);
            int falsePositives = 0;
            for (int i = 0; i < testSamples; i++) {
                long testKey = (i * 2L) + 1;
                boolean res = filter.contains(testKey);
                if (res) falsePositives++;
            }
            m.falsePositiveRate = (double) falsePositives / testSamples;
            m.ok = true;
            return m;
        } catch (Throwable e) {
            m.errorMsg = e.getMessage();
            return m;
        }
    }

    private static void estimateFalsePositiveRate(XorFilter.XorFilterInterface filter, int numElements) {
        // Test with keys that are NOT in the original set (odd numbers)
        int testSamples = Math.min(100_000, numElements); // Test up to 100k samples
        int falsePositives = 0;

        for (int i = 0; i < testSamples; i++) {
            // Use odd numbers that are guaranteed not to be in the original set
            long testKey = (i * 2L) + 1;
            boolean result = false;

            try {
                result = filter.contains(testKey);
            } catch (Throwable e) {
                System.err.println("Error during false positive test: " + e.getMessage());
                continue;
            }

            if (result) {
                falsePositives++;
            }
        }

        double falsePositiveRate = (double) falsePositives / testSamples;
        System.out.println("False positive tests: " + testSamples);
        System.out.println("False positives found: " + falsePositives);
        System.out.printf("Estimated false positive rate: %.4f%% (%.2f per thousand)%n",
                         falsePositiveRate * 100, falsePositiveRate * 1000);
    }
}

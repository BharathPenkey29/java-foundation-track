package com.signia.training.a01basics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Assignment 1 - Q5
 *
 * Duplicate Detection - Three Ways
 *
 * (a) Nested loops       -> O(n^2)
 * (b) Sort + scan        -> O(n log n)
 * (c) HashSet            -> O(n) average
 */
public class DuplicateDetection {

    private static final String INPUT_FILE =
            "src/main/resources/generated/mrns-100k.txt";

    private static final int EXPECTED_DUPLICATES = 250;

    /*
     * Assignment says to run the O(n^2) version on a reduced
     * input if it takes more than 30 seconds.
     */
    private static final long MAX_NESTED_LOOP_TIME_NANOS =
            30_000_000_000L;

    /*
     * We use a smaller sample for the nested-loop algorithm
     * when necessary.
     */
    private static final int REDUCED_INPUT_SIZE = 10_000;


    public static void main(String[] args) {

        Path inputPath =
                Paths.get(INPUT_FILE);

        if (!Files.exists(inputPath)) {

            System.out.println(
                    "Input file not found: "
                            + inputPath
            );

            System.exit(1);
        }

        /*
         * Read the fixture once.
         */
        List<String> mrnList;

        try {

            mrnList =
                    Files.readAllLines(
                            inputPath,
                            StandardCharsets.UTF_8
                    );

        } catch (IOException e) {

            System.out.println(
                    "Unable to read input file: "
                            + e.getMessage()
            );

            System.exit(1);
            return;
        }

        String[] mrns =
                mrnList.toArray(new String[0]);

        System.out.println(
                "============================================================"
        );

        System.out.println(
                "DUPLICATE DETECTION - THREE APPROACHES"
        );

        System.out.println(
                "============================================================"
        );

        System.out.println(
                "Input file: " + inputPath
        );

        System.out.println(
                "Input size: " + mrns.length
        );

        System.out.println(
                "Expected duplicate values: "
                        + EXPECTED_DUPLICATES
        );

        System.out.println();


        /*
         * ========================================================
         * A. Nested Loop
         * ========================================================
         */

        System.out.println(
                "------------------------------------------------------------"
        );

        System.out.println(
                "(A) NESTED LOOP - O(n^2)"
        );

        System.out.println(
                "------------------------------------------------------------"
        );

        DuplicateResult nestedResult =
                runNestedLoopWithLimit(mrns);

        printResult(nestedResult);


        /*
         * ========================================================
         * B. Sort + Scan
         * ========================================================
         */

        System.out.println(
                "------------------------------------------------------------"
        );

        System.out.println(
                "(B) SORT + SCAN - O(n log n)"
        );

        System.out.println(
                "------------------------------------------------------------"
        );

        long sortStart =
                System.nanoTime();

        DuplicateResult sortResult =
                findDuplicatesBySorting(mrns);

        long sortEnd =
                System.nanoTime();

        sortResult.setElapsedNanos(
                sortEnd - sortStart
        );

        printResult(sortResult);


        /*
         * ========================================================
         * C. HashSet
         * ========================================================
         */

        System.out.println(
                "------------------------------------------------------------"
        );

        System.out.println(
                "(C) HASHSET - O(n) average"
        );

        System.out.println(
                "------------------------------------------------------------"
        );

        long hashStart =
                System.nanoTime();

        DuplicateResult hashResult =
                findDuplicatesByHashSet(mrns);

        long hashEnd =
                System.nanoTime();

        hashResult.setElapsedNanos(
                hashEnd - hashStart
        );

        printResult(hashResult);


        /*
         * ========================================================
         * Agreement Checks
         * ========================================================
         */

        System.out.println(
                "------------------------------------------------------------"
        );

        System.out.println(
                "VALIDATION"
        );

        System.out.println(
                "------------------------------------------------------------"
        );

        /*
         * IMPORTANT:
         *
         * If nested-loop had to use reduced input,
         * its duplicate set cannot be compared against
         * the complete 100,000-line results.
         */
        if (nestedResult.isFullInputRun()) {

            assert nestedResult.getDuplicates()
                    .equals(sortResult.getDuplicates())
                    : "Nested loop and sort results disagree.";

            assert nestedResult.getDuplicates()
                    .equals(hashResult.getDuplicates())
                    : "Nested loop and HashSet results disagree.";

            System.out.println(
                    "PASS | All three implementations agree."
            );

        } else {

            /*
             * Nested loop used reduced input.
             *
             * Compare the two full-input implementations.
             */
            assert sortResult.getDuplicates()
                    .equals(hashResult.getDuplicates())
                    : "Sort and HashSet results disagree.";

            System.out.println(
                    "PASS | Sort and HashSet agree on all "
                            + mrns.length
                            + " records."
            );

            System.out.println(
                    "INFO | Nested-loop used reduced input, "
                            + "so its duplicate set was not compared "
                            + "with the full-input results."
            );
        }


        /*
         * The fixture explicitly contains 250 planted
         * duplicate values.
         */
        assert sortResult.getDuplicates().size()
                == EXPECTED_DUPLICATES
                : "Expected "
                + EXPECTED_DUPLICATES
                + " duplicate values but found "
                + sortResult.getDuplicates().size();

        assert hashResult.getDuplicates().size()
                == EXPECTED_DUPLICATES
                : "Expected "
                + EXPECTED_DUPLICATES
                + " duplicate values but found "
                + hashResult.getDuplicates().size();

        System.out.println(
                "PASS | Expected duplicate count = "
                        + EXPECTED_DUPLICATES
        );


        /*
         * ========================================================
         * First duplicate
         * ========================================================
         */

        System.out.println();

        System.out.println(
                "First duplicate found:"
        );

        System.out.println(
                "  Sort + Scan : "
                        + sortResult.getFirstDuplicate()
        );

        System.out.println(
                "  HashSet     : "
                        + hashResult.getFirstDuplicate()
        );


        /*
         * ========================================================
         * Final timing comparison
         * ========================================================
         */

        System.out.println();

        System.out.println(
                "============================================================"
        );

        System.out.println(
                "TIMING SUMMARY"
        );

        System.out.println(
                "============================================================"
        );

        System.out.printf(
                "Nested loop : %,.3f ms%n",
                nestedResult.getElapsedNanos()
                        / 1_000_000.0
        );

        System.out.printf(
                "Sort + scan : %,.3f ms%n",
                sortResult.getElapsedNanos()
                        / 1_000_000.0
        );

        System.out.printf(
                "HashSet     : %,.3f ms%n",
                hashResult.getElapsedNanos()
                        / 1_000_000.0
        );

        System.out.println();

        System.out.println(
                "Nested-loop comparisons: "
                        + String.format(
                        "%,d",
                        nestedResult.getComparisons()
                )
        );

        if (!nestedResult.isFullInputRun()) {

            System.out.println(
                    "NOTE: Nested-loop was run on "
                            + "reduced input because the full "
                            + "O(n^2) run exceeded the 30-second "
                            + "limit."
            );
        }

        System.out.println(
                "============================================================"
        );
    }


    /*
     * ============================================================
     * (A) Nested Loop
     * ============================================================
     *
     * Time:
     * O(n^2)
     *
     * Space:
     * O(d), where d = number of duplicate values.
     *
     * A HashSet is used only to store the duplicate results.
     */
    private static DuplicateResult findDuplicatesNested(
            String[] mrns) {

        Set<String> duplicates =
                new HashSet<>();

        String firstDuplicate = null;

        long comparisons = 0;

        for (int i = 0;
             i < mrns.length;
             i++) {

            for (int j = i + 1;
                 j < mrns.length;
                 j++) {

                comparisons++;

                if (mrns[i].equals(mrns[j])) {

                    duplicates.add(
                            mrns[i]
                    );

                    if (firstDuplicate == null) {

                        firstDuplicate =
                                mrns[i];
                    }
                }
            }
        }

        return new DuplicateResult(
                firstDuplicate,
                duplicates,
                comparisons
        );
    }


    /*
     * ============================================================
     * Nested Loop with 30-second handling
     * ============================================================
     */

    private static DuplicateResult runNestedLoopWithLimit(
            String[] mrns) {

        /*
         * First attempt the full input.
         */
        long start =
                System.nanoTime();

        DuplicateResult result =
                findDuplicatesNested(mrns);

        long elapsed =
                System.nanoTime() - start;

        result.setElapsedNanos(elapsed);

        if (elapsed <= MAX_NESTED_LOOP_TIME_NANOS) {

            result.setFullInputRun(true);

            System.out.println(
                    "Nested-loop completed on full input."
            );

            return result;
        }


        /*
         * Full input took more than 30 seconds.
         *
         * Re-run on reduced input.
         */
        System.out.println(
                "WARNING: Full nested-loop run exceeded "
                        + "30 seconds."
        );

        System.out.println(
                "Running nested-loop on reduced input of "
                        + REDUCED_INPUT_SIZE
                        + " records."
        );

        int reducedSize =
                Math.min(
                        REDUCED_INPUT_SIZE,
                        mrns.length
                );

        String[] reducedMrns =
                Arrays.copyOf(
                        mrns,
                        reducedSize
                );

        long reducedStart =
                System.nanoTime();

        DuplicateResult reducedResult =
                findDuplicatesNested(
                        reducedMrns
                );

        long reducedElapsed =
                System.nanoTime()
                        - reducedStart;

        reducedResult.setElapsedNanos(
                reducedElapsed
        );

        reducedResult.setFullInputRun(
                false
        );

        return reducedResult;
    }


    /*
     * ============================================================
     * (B) Sort + Scan
     * ============================================================
     *
     * Time:
     * O(n log n)
     *
     * Space:
     * O(n) because we make a copy of the input.
     */
    private static DuplicateResult findDuplicatesBySorting(
            String[] mrns) {

        String[] sorted =
                Arrays.copyOf(
                        mrns,
                        mrns.length
                );

        Arrays.sort(sorted);

        Set<String> duplicates =
                new HashSet<>();

        String firstDuplicate = null;

        for (int i = 1;
             i < sorted.length;
             i++) {

            if (sorted[i].equals(
                    sorted[i - 1])) {

                String duplicate =
                        sorted[i];

                duplicates.add(
                        duplicate
                );

                if (firstDuplicate == null) {

                    firstDuplicate =
                            duplicate;
                }
            }
        }

        return new DuplicateResult(
                firstDuplicate,
                duplicates,
                0
        );
    }


    /*
     * ============================================================
     * (C) HashSet
     * ============================================================
     *
     * Time:
     * O(n) average
     *
     * Space:
     * O(n)
     *
     * HashSet provides average O(1) insertion and lookup.
     */
    private static DuplicateResult findDuplicatesByHashSet(
            String[] mrns) {

        Set<String> seen =
                new HashSet<>();

        Set<String> duplicates =
                new HashSet<>();

        String firstDuplicate = null;

        for (String mrn : mrns) {

            /*
             * add() returns false when the value already exists.
             */
            if (!seen.add(mrn)) {

                duplicates.add(mrn);

                if (firstDuplicate == null) {

                    firstDuplicate = mrn;
                }
            }
        }

        return new DuplicateResult(
                firstDuplicate,
                duplicates,
                0
        );
    }


    /*
     * ============================================================
     * Print result
     * ============================================================
     */

    private static void printResult(
            DuplicateResult result) {

        System.out.println(
                "First duplicate: "
                        + result.getFirstDuplicate()
        );

        System.out.println(
                "Duplicate values: "
                        + result.getDuplicates().size()
        );

        System.out.printf(
                "Time: %,.3f ms%n",
                result.getElapsedNanos()
                        / 1_000_000.0
        );

        if (result.getComparisons() > 0) {

            System.out.println(
                    "Comparisons: "
                            + String.format(
                            "%,d",
                            result.getComparisons()
                    )
            );
        }
    }


    /*
     * ============================================================
     * Result class
     * ============================================================
     */

    private static class DuplicateResult {

        private final String firstDuplicate;

        private final Set<String> duplicates;

        private final long comparisons;

        private long elapsedNanos;

        private boolean fullInputRun;

        public DuplicateResult(
                String firstDuplicate,
                Set<String> duplicates,
                long comparisons) {

            this.firstDuplicate =
                    firstDuplicate;

            this.duplicates =
                    duplicates;

            this.comparisons =
                    comparisons;

            this.elapsedNanos = 0;

            this.fullInputRun = true;
        }

        public String getFirstDuplicate() {
            return firstDuplicate;
        }

        public Set<String> getDuplicates() {
            return duplicates;
        }

        public long getComparisons() {
            return comparisons;
        }

        public long getElapsedNanos() {
            return elapsedNanos;
        }

        public void setElapsedNanos(
                long elapsedNanos) {

            this.elapsedNanos =
                    elapsedNanos;
        }

        public boolean isFullInputRun() {
            return fullInputRun;
        }

        public void setFullInputRun(
                boolean fullInputRun) {

            this.fullInputRun =
                    fullInputRun;
        }
    }
}
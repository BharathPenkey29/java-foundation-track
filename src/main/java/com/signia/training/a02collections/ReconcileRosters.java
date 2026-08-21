package com.signia.training.a02collections;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SequencedSet;

public class ReconcileRosters {

    private static final Path YESTERDAY_FILE =
            Path.of("src/main/resources/a02/yesterday.csv");
    //               ("src/main/resources/generated/roster-500k-yesterday.csv");

    private static final Path TODAY_FILE =
            Path.of("src/main/resources/a02/today.csv");
    //              ("src/main/resources/generated/roster-500k-today.csv");

    private static final Path SUMMARY_FILE =
            Path.of("reconciliation-summary.txt");

    public static void main(String[] args) throws IOException {

        Roster yesterdayRoster = loadRoster(YESTERDAY_FILE);
        Roster todayRoster = loadRoster(TODAY_FILE);

        SequencedSet<String> yesterday =
                yesterdayRoster.uniqueMrns;

        SequencedSet<String> today =
                todayRoster.uniqueMrns;

        /*
         * =========================================================
         * MUTATION BUG DEMONSTRATION
         * =========================================================
         *
         * retainAll() modifies the set on which it is called.
         *
         * The following is the BROKEN approach:
         *
         * Set<String> unchanged = yesterday;
         * unchanged.retainAll(today);
         *
         * After retainAll(), yesterday itself would contain only
         * the unchanged MRNs.
         *
         * Therefore calculating:
         *
         * yesterday - today
         *
         * afterward would produce the WRONG answer.
         *
         * We keep the broken code commented out because the
         * assignment explicitly asks us to demonstrate the bug
         * and preserve the broken version in the source.
         *
         * BROKEN CODE:
         *
         * Set<String> unchanged = yesterday;
         * unchanged.retainAll(today);
         *
         * Set<String> removed = new LinkedHashSet<>(yesterday);
         * removed.removeAll(today);
         *
         * Wrong result:
         * removed = []
         */

        demonstrateMutationBug(yesterday, today);

        /*
         * =========================================================
         * CORRECT SET OPERATIONS
         * =========================================================
         *
         * retainAll() and removeAll() are destructive operations.
         * Therefore every operation is performed on a defensive
         * copy.
         */

        // unchanged = yesterday INTERSECTION today
        SequencedSet<String> unchanged =
                new LinkedHashSet<>(yesterday);

        unchanged.retainAll(today);

        // added = today - yesterday
        SequencedSet<String> added =
                new LinkedHashSet<>(today);

        added.removeAll(yesterday);

        // removed = yesterday - today
        SequencedSet<String> removed =
                new LinkedHashSet<>(yesterday);

        removed.removeAll(today);

        /*
         * =========================================================
         * CONSOLE REPORT
         * =========================================================
         */

        System.out.println();
        System.out.println("===== RECONCILIATION =====");

        printBucket("ADDED", added);
        printBucket("REMOVED", removed);
        printBucket("UNCHANGED", unchanged);

        System.out.println();
        System.out.println("===== DUPLICATES =====");

        printBucket(
                "YESTERDAY DUPLICATES",
                yesterdayRoster.duplicates
        );

        printBucket(
                "TODAY DUPLICATES",
                todayRoster.duplicates
        );

        /*
         * =========================================================
         * WRITE HUMAN-READABLE SUMMARY
         * =========================================================
         */

        writeSummary(
                added,
                removed,
                unchanged,
                yesterdayRoster.duplicates,
                todayRoster.duplicates
        );

        System.out.println();
        System.out.println(
                "Summary written to: " +
                        SUMMARY_FILE
        );
    }

    /**
     * Demonstrates why retainAll() and removeAll() must be used
     * on defensive copies.
     */
    private static void demonstrateMutationBug(
            SequencedSet<String> yesterday,
            SequencedSet<String> today) {

        /*
         * We intentionally create a copy so that the actual
         * yesterday set remains available for the correct solution.
         *
         * The copy represents the original broken approach.
         */
        SequencedSet<String> brokenYesterday =
                new LinkedHashSet<>(yesterday);

        brokenYesterday.retainAll(today);

        /*
         * At this point brokenYesterday contains only the
         * intersection.
         */
        SequencedSet<String> wrongRemoved =
                new LinkedHashSet<>(brokenYesterday);

        wrongRemoved.removeAll(today);

        System.out.println(
                "===== MUTATION BUG DEMONSTRATION ====="
        );

        System.out.println(
                "After retainAll(), yesterday becomes:"
        );

        System.out.println(brokenYesterday);

        System.out.println(
                "Wrong removed result: " +
                        wrongRemoved
        );
    }

    /**
     * Reads a roster file once.
     *
     * LinkedHashSet is deliberately used because the assignment
     * requires the first 10 members of each bucket in source-file
     * order.
     *
     * The same pass also detects duplicates, so the file does not
     * need to be loaded twice.
     */
    private static Roster loadRoster(Path file)
            throws IOException {

        SequencedSet<String> uniqueMrns =
                new LinkedHashSet<>();

        SequencedSet<String> duplicates =
                new LinkedHashSet<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(file)) {

            // Skip CSV header.
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String mrn = line.trim();

                /*
                 * Set.add() returns false when the MRN already
                 * exists in the set.
                 *
                 * Therefore false means this is a duplicate.
                 */
                if (!uniqueMrns.add(mrn)) {
                    duplicates.add(mrn);
                }
            }
        }

        return new Roster(
                uniqueMrns,
                duplicates
        );
    }

    /**
     * Prints:
     *
     * - total number of members
     * - earliest member
     * - latest member
     * - first 10 members in source order
     *
     * SequencedSet gives us getFirst() and getLast() without
     * manually iterating to find the first or last element.
     */
    private static void printBucket(
            String name,
            SequencedSet<String> bucket) {

        System.out.println();
        System.out.println(name + ": " + bucket.size());

        if (bucket.isEmpty()) {
            System.out.println("  No members");
            return;
        }

        System.out.println(
                "  Earliest: " +
                        bucket.getFirst()
        );

        System.out.println(
                "  Latest: " +
                        bucket.getLast()
        );

        System.out.println("  First 10:");

        int count = 0;

        for (String mrn : bucket) {

            if (count == 10) {
                break;
            }

            System.out.println("    " + mrn);

            count++;
        }
    }

    /**
     * Creates reconciliation-summary.txt in a format that a
     * non-engineer can understand.
     */
    private static void writeSummary(
            SequencedSet<String> added,
            SequencedSet<String> removed,
            SequencedSet<String> unchanged,
            SequencedSet<String> yesterdayDuplicates,
            SequencedSet<String> todayDuplicates)
            throws IOException {

        StringBuilder summary = new StringBuilder();

        summary.append("ROSTER RECONCILIATION SUMMARY\n");
        summary.append("============================\n\n");

        summary.append(
                "This report compares yesterday's roster with today's roster.\n"
        );

        summary.append(
                "It identifies newly added patients, patients removed from "
                        + "the roster, patients present on both days, and "
                        + "duplicate records within each file.\n\n"
        );

        appendBucketSummary(
                summary,
                "Added",
                added
        );

        appendBucketSummary(
                summary,
                "Removed",
                removed
        );

        appendBucketSummary(
                summary,
                "Unchanged",
                unchanged
        );

        appendBucketSummary(
                summary,
                "Duplicates in yesterday's file",
                yesterdayDuplicates
        );

        appendBucketSummary(
                summary,
                "Duplicates in today's file",
                todayDuplicates
        );

        summary.append("\nEnd of reconciliation report.\n");

        Files.writeString(
                SUMMARY_FILE,
                summary.toString()
        );
    }

    /**
     * Adds one bucket to the human-readable summary.
     */
    private static void appendBucketSummary(
            StringBuilder summary,
            String title,
            SequencedSet<String> bucket) {

        summary.append(title)
                .append(": ")
                .append(bucket.size())
                .append("\n");

        if (bucket.isEmpty()) {
            summary.append("None\n\n");
            return;
        }

        summary.append("Earliest: ")
                .append(bucket.getFirst())
                .append("\n");

        summary.append("Latest: ")
                .append(bucket.getLast())
                .append("\n");

        summary.append("First 10 members:\n");

        int count = 0;

        for (String mrn : bucket) {

            if (count == 10) {
                break;
            }

            summary.append("  ")
                    .append(mrn)
                    .append("\n");

            count++;
        }

        summary.append("\n");
    }

    /**
     * Holds the results obtained from reading one roster file.
     */
    private static class Roster {

        private final SequencedSet<String> uniqueMrns;
        private final SequencedSet<String> duplicates;

        private Roster(
                SequencedSet<String> uniqueMrns,
                SequencedSet<String> duplicates) {

            this.uniqueMrns = uniqueMrns;
            this.duplicates = duplicates;
        }
    }
}
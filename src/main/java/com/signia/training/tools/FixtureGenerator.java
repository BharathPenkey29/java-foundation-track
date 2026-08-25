package com.signia.training.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates the large fixture files that are deliberately not committed to git.
 * <p>
 * This class is <em>tooling</em>. It is not part of any assignment, you are not marked on it,
 * and you do not need to modify it. Read it if you like.
 * <p>
 * Everything it produces is seeded, so two runs on two machines produce byte-identical files.
 * That matters: your timing numbers and your reviewer's need to describe the same input.
 * <p>
 * Run it with:
 * <pre>
 *   mvn -q compile exec:java -Dexec.mainClass=com.signia.training.tools.FixtureGenerator
 * </pre>
 * Output lands in {@code src/main/resources/generated/}, which is git-ignored.
 */
public final class FixtureGenerator {

    private static final long SEED = 20260818L;
    private static final Path OUTPUT_DIR = Path.of("src", "main", "resources", "generated");

    private static final String[] STEP_NAMES = {
            "login", "navigate", "fetchRoster", "fetchVisits",
            "extractTable", "mergeReport", "upload", "logout"
    };

    private FixtureGenerator() {
        // Utility class: never instantiated.
    }

    public static void main(String[] args) throws IOException {
        Files.createDirectories(OUTPUT_DIR);

        writeMrnsWithDuplicates(OUTPUT_DIR.resolve("mrns-100k.txt"), 100_000, 250);
        writeLargeRunLog(OUTPUT_DIR.resolve("run-log-1m.txt"), 1_000_000);
        writeRosterPair(OUTPUT_DIR.resolve("roster-500k-yesterday.csv"),
                        OUTPUT_DIR.resolve("roster-500k-today.csv"), 500_000);

        System.out.println();
        System.out.println("Fixtures written to " + OUTPUT_DIR.toAbsolutePath());
        System.out.println("These files are git-ignored. Regenerate them rather than committing them.");
    }

    /**
     * Assignment 1, Question 5. One MRN per line, with a known number of planted duplicates.
     * The planted values are spread across the file rather than clustered, so a solution that
     * happens to look only at neighbouring entries will not find them all.
     */
    private static void writeMrnsWithDuplicates(Path target, int total, int duplicateCount) throws IOException {
        Random random = new Random(SEED);
        List<String> values = new ArrayList<>(total);

        int unique = total - duplicateCount;
        for (int i = 0; i < unique; i++) {
            values.add(String.format("MRN%07d", i));
        }
        for (int i = 0; i < duplicateCount; i++) {
            values.add(values.get(random.nextInt(unique)));
        }
        // Fisher-Yates, so the duplicates are scattered rather than sitting in a block at the end.
        for (int i = values.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String swap = values.get(i);
            values.set(i, values.get(j));
            values.set(j, swap);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            for (String value : values) {
                writer.write(value);
                writer.newLine();
            }
        }
        System.out.printf("%-28s %,10d lines (%d planted duplicates)%n",
                target.getFileName(), values.size(), duplicateCount);
    }

    /**
     * Assignment 2, Question 6. Same {@code timestamp|jobId|stepName|status} shape as the small
     * committed run-log.txt, just large enough that an O(n log n) sort is measurably slower than
     * an O(n log k) heap.
     */
    private static void writeLargeRunLog(Path target, int lines) throws IOException {
        Random random = new Random(SEED + 1);
        LocalDateTime clock = LocalDateTime.of(2026, 8, 1, 0, 0, 0);

        // Deliberately uneven, so the top-K answer is stable and worth finding.
        int[] failureWeight = { 3, 2, 6, 6, 14, 1, 11, 1 };

        try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            for (int i = 0; i < lines; i++) {
                int stepIndex = random.nextInt(STEP_NAMES.length);
                clock = clock.plusSeconds(1 + random.nextInt(3));
                String status = random.nextInt(100) < failureWeight[stepIndex] ? "FAILED" : "OK";

                writer.write(clock.toString());
                writer.write('|');
                writer.write("JOB-" + (100_000 + i / 8));
                writer.write('|');
                writer.write(STEP_NAMES[stepIndex]);
                writer.write('|');
                writer.write(status);
                writer.newLine();
            }
        }
        System.out.printf("%-28s %,10d lines%n", target.getFileName(), lines);
    }

    /**
     * Assignment 2, Question 2. Two roster files large enough that an O(n^2) comparison is
     * hopeless and a careless second copy of every set is felt on the default heap.
     */
    private static void writeRosterPair(Path yesterday, Path today, int size) throws IOException {
        Random random = new Random(SEED + 2);

        try (BufferedWriter writer = Files.newBufferedWriter(yesterday, StandardCharsets.UTF_8)) {
            writer.write("mrn");
            writer.newLine();
            for (int i = 0; i < size; i++) {
                writer.write(String.format("MRN%07d", i));
                writer.newLine();
            }
            // 1,000 in-file duplicates, so a Set alone cannot answer every question asked.
            for (int i = 0; i < 1_000; i++) {
                writer.write(String.format("MRN%07d", random.nextInt(size)));
                writer.newLine();
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(today, StandardCharsets.UTF_8)) {
            writer.write("mrn");
            writer.newLine();
            // Drop the first 2,000 (discharged), keep the rest, then add 3,000 new admissions.
            for (int i = 2_000; i < size; i++) {
                writer.write(String.format("MRN%07d", i));
                writer.newLine();
            }
            for (int i = size; i < size + 3_000; i++) {
                writer.write(String.format("MRN%07d", i));
                writer.newLine();
            }
        }
        System.out.printf("%-28s %,10d + 1,000 duplicate lines%n", yesterday.getFileName(), size);
        System.out.printf("%-28s %,10d lines (2,000 removed, 3,000 added)%n", today.getFileName(), size - 2_000 + 3_000);
    }
}

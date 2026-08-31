package com.signia.training.a04advanced;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Assignment 4 Q3 driver.
 *
 * Rebuilds Assignment 2 Q1 using Stream API.
 */
public class Q3_StreamDemo {

    private static final Path INPUT_FILE =
            Path.of(
                    "src",
                    "main",
                    "resources",
                    "a02",
                    "visits.csv"
            );

    public static void main(String[] args) {

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "A4 Q3 - SAME REPORT USING STREAMS"
        );

        System.out.println(
                "=========================================="
        );

        List<Q3_Visit> visits;

        try {

            visits =
                    loadVisits(INPUT_FILE);

        } catch (IOException exception) {

            System.out.println(
                    "Unable to read visits file: "
                            + INPUT_FILE
            );

            System.exit(1);

            return;
        }

        System.out.println();
        System.out.println(
                "--- VISITS PER PROVIDER ---"
        );

        Q3_Report
                .visitsPerProvider(visits)
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry.comparingByKey()
                )
                .forEach(
                        entry ->
                                System.out.println(
                                        entry.getKey()
                                                + " -> "
                                                + entry.getValue()
                                )
                );

        System.out.println();
        System.out.println(
                "--- TOTAL MINUTES ---"
        );

        Q3_Report
                .totalMinutesPerProvider(visits)
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<String, Integer>comparingByValue()
                                .reversed()
                                .thenComparing(
                                        Map.Entry::getKey
                                )
                )
                .forEach(
                        entry ->
                                System.out.println(
                                        entry.getKey()
                                                + " -> "
                                                + entry.getValue()
                                                + " minutes"
                                )
                );

        System.out.println();
        System.out.println(
                "--- AVERAGE DURATION ---"
        );

        Q3_Report
                .averageDurationPerProvider(visits)
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry.comparingByKey()
                )
                .forEach(
                        entry ->
                                System.out.println(
                                        entry.getKey()
                                                + " -> "
                                                + entry.getValue()
                                )
                );

        System.out.println();
        System.out.println(
                "--- TOP 3 PROVIDERS ---"
        );

        Q3_Report
                .topThreeProviders(visits)
                .forEach(
                        entry ->
                                System.out.println(
                                        entry.getKey()
                                                + " -> "
                                                + entry.getValue()
                                                + " minutes"
                                )
                );

        System.out.println();
        System.out.println(
                "--- COMPLETED / CANCELLED SPLIT ---"
        );

        Map<Boolean, List<Q3_Visit>> split =
                Q3_Report.completedCancelledSplit(
                        visits
                );

        System.out.println(
                "Completed: "
                        + split
                        .getOrDefault(
                                true,
                                List.of()
                        )
                        .size()
        );

        System.out.println(
                "Cancelled/Other: "
                        + split
                        .getOrDefault(
                                false,
                                List.of()
                        )
                        .size()
        );

        System.out.println();
        System.out.println(
                "--- BUSIEST DATE PER PROVIDER ---"
        );

        Q3_Report
                .busiestDatePerProvider(visits)
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry.comparingByKey()
                )
                .forEach(
                        entry ->
                                System.out.println(
                                        entry.getKey()
                                                + " -> "
                                                + entry.getValue()
                                )
                );

        System.out.println();
        System.out.println(
                "--- OPTIONAL DEMONSTRATION ---"
        );

        System.out.println(
                Q3_Report.findProviderSummary(
                        visits,
                        "K. Rao"
                )
        );

        System.out.println(
                Q3_Report.findProviderSummary(
                        visits,
                        "Unknown Provider"
                )
        );

        System.out.println();
        System.out.println(
                "--- STRETCH: CUSTOM COLLECTOR ---"
        );

        Q3_Statistics statistics =
                Q3_Report.statistics(
                        visits
                );

        System.out.println(
                "Count: "
                        + statistics.count()
        );

        System.out.println(
                "Sum: "
                        + statistics.sum()
        );

        System.out.println(
                "Min: "
                        + statistics.min()
        );

        System.out.println(
                "Max: "
                        + statistics.max()
        );

        System.out.println(
                "Average: "
                        + statistics.average()
        );

        System.out.println();
        System.out.println(
                "Q3 stream report completed."
        );
    }

    /**
     * Reads the Assignment 2 visits fixture.
     */
    private static List<Q3_Visit> loadVisits(
            Path inputFile
    ) throws IOException {

        List<Q3_Visit> visits =
                new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             inputFile,
                             StandardCharsets.UTF_8
                     )) {

            String header =
                    reader.readLine();

            if (header == null) {

                return visits;
            }

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] columns =
                        line.split(",", -1);

                if (columns.length != 6) {

                    throw new IOException(
                            "Invalid visit row: "
                                    + line
                    );
                }

                visits.add(
                        new Q3_Visit(
                                columns[0].trim(),
                                columns[1].trim(),
                                columns[2].trim(),
                                LocalDate.parse(
                                        columns[3].trim()
                                ),
                                Integer.parseInt(
                                        columns[4].trim()
                                ),
                                columns[5].trim()
                        )
                );
            }
        }

        return visits;
    }
}
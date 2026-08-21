package com.signia.training.a02collections;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SortAndRangeQueries {

    private static final Path VISITS_FILE =
            Path.of("src/main/resources/a02/visits.csv");

    public static void main(String[] args) throws IOException {

        List<Visit> visits = loadVisits();

        /*
         * ==========================================================
         * Q3.1 — SORT BY DATE DESC, PROVIDER ASC, DURATION DESC
         * ==========================================================
         *
         * Important:
         *
         * We want:
         *
         * date       -> descending
         * provider   -> ascending
         * duration   -> descending
         *
         * The reversed() must be applied only to the individual
         * comparator that needs descending order.
         *
         * If we did:
         *
         * comparator.reversed()
         *
         * after building the entire chain, the WHOLE chain would
         * be reversed.
         */

        Comparator<Visit> comparatorVersion =
                Comparator
                        .comparing(
                                Visit::getDate,
                                Comparator.reverseOrder()
                        )
                        .thenComparing(
                                Visit::getProvider,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                        .thenComparing(
                                Visit::getDurationMins,
                                Comparator.reverseOrder()
                        );

        List<Visit> comparatorSorted =
                new ArrayList<>(visits);

        comparatorSorted.sort(comparatorVersion);

        /*
         * ==========================================================
         * WRONG .reversed() DEMONSTRATION
         * ==========================================================
         *
         * This reverses the entire comparator chain.
         *
         * Therefore it does NOT mean:
         *
         * date DESC
         * provider ASC
         * duration DESC
         *
         * Instead, all ordering directions are reversed.
         */

        Comparator<Visit> wrongComparator =
                Comparator
                        .comparing(Visit::getDate)
                        .thenComparing(
                                Visit::getProvider,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                        .thenComparingInt(
                                Visit::getDurationMins
                        )
                        .reversed();

        /*
         * ==========================================================
         * Q3.2 — HAND-WRITTEN compare() IMPLEMENTATION
         * ==========================================================
         */

        Comparator<Visit> handWrittenComparator =
                new Comparator<Visit>() {

                    @Override
                    public int compare(
                            Visit first,
                            Visit second) {

                        /*
                         * Date descending.
                         *
                         * Compare second against first.
                         */
                        int result =
                                second.getDate()
                                        .compareTo(first.getDate());

                        if (result != 0) {
                            return result;
                        }

                        /*
                         * Provider ascending with nulls last.
                         */
                        String firstProvider =
                                first.getProvider();

                        String secondProvider =
                                second.getProvider();

                        if (firstProvider == null
                                && secondProvider != null) {

                            return 1;

                        }

                        if (firstProvider != null
                                && secondProvider == null) {

                            return -1;

                        }

                        if (firstProvider != null
                                && secondProvider != null) {

                            result =
                                    firstProvider.compareTo(
                                            secondProvider
                                    );

                            if (result != 0) {
                                return result;
                            }
                        }

                        /*
                         * Duration descending.
                         */
                        return Integer.compare(
                                second.getDurationMins(),
                                first.getDurationMins()
                        );
                    }
                };

        List<Visit> handWrittenSorted =
                new ArrayList<>(visits);

        handWrittenSorted.sort(
                handWrittenComparator
        );

        /*
         * ==========================================================
         * PROVE BOTH SORT IMPLEMENTATIONS ARE IDENTICAL
         * ==========================================================
         *
         * List.equals() compares the elements in the same order.
         *
         * Since Visit does not override equals(), we should compare
         * the actual output representation instead of relying on
         * object equality.
         */

        boolean identical =
                visitsToOutput(comparatorSorted)
                        .equals(
                                visitsToOutput(
                                        handWrittenSorted
                                )
                        );

        System.out.println(
                "Comparator outputs identical: "
                        + identical
        );

        if (!identical) {
            throw new IllegalStateException(
                    "Comparator implementations produced different output."
            );
        }

        System.out.println();
        System.out.println(
                "===== SORTED BY DATE DESC, PROVIDER ASC, DURATION DESC ====="
        );

        printVisits(comparatorSorted);

        /*
         * ==========================================================
         * Q3.3 — NATURAL ORDER USING Comparable
         * ==========================================================
         *
         * Visit implements Comparable<Visit>.
         *
         * Natural order = visitId ascending.
         *
         * No Comparator is supplied.
         */

        List<Visit> naturalOrder =
                new ArrayList<>(visits);

        Collections.sort(naturalOrder);

        System.out.println();
        System.out.println(
                "===== NATURAL ORDER - VISIT ID ====="
        );

        printVisits(naturalOrder);

        /*
         * ==========================================================
         * Q3.4 — TREE MAP DATE INDEX
         * ==========================================================
         *
         * TreeMap keeps LocalDate keys sorted automatically.
         */

        TreeMap<LocalDate, List<Visit>> visitsByDate =
                new TreeMap<>();

        for (Visit visit : visits) {

            visitsByDate
                    .computeIfAbsent(
                            visit.getDate(),
                            key -> new ArrayList<>()
                    )
                    .add(visit);
        }

        System.out.println();
        System.out.println(
                "===== TREE MAP DATE INDEX ====="
        );

        for (Map.Entry<LocalDate, List<Visit>> entry :
                visitsByDate.entrySet()) {

            System.out.println(
                    entry.getKey()
                            + " -> "
                            + entry.getValue().size()
                            + " visits"
            );
        }

        /*
         * ==========================================================
         * Q3.5 — DATE RANGE QUERY
         * ==========================================================
         *
         * Requirement:
         *
         * visitsBetween(from, to)
         *
         * The requested dates are:
         *
         * 2026-08-11
         * through
         * 2026-08-13
         *
         * We want BOTH dates included.
         *
         * TreeMap.subMap(from, true, to, true)
         * gives us an inclusive range.
         */

        LocalDate from =
                LocalDate.of(2026, 8, 11);

        LocalDate to =
                LocalDate.of(2026, 8, 13);

        List<Visit> rangeVisits =
                visitsBetween(
                        visitsByDate,
                        from,
                        to
                );

        System.out.println();
        System.out.println(
                "===== VISITS BETWEEN "
                        + from
                        + " AND "
                        + to
                        + " ====="
        );

        printVisits(rangeVisits);

        /*
         * ==========================================================
         * DEMONSTRATE subMap()
         * ==========================================================
         *
         * Inclusive start and inclusive end.
         */

        System.out.println();
        System.out.println("===== SUBMAP =====");

        for (Map.Entry<LocalDate, List<Visit>> entry :
                visitsByDate.subMap(
                        from,
                        true,
                        to,
                        true
                ).entrySet()) {

            System.out.println(
                    entry.getKey()
                            + " -> "
                            + entry.getValue().size()
            );
        }

        /*
         * ==========================================================
         * DEMONSTRATE headMap()
         * ==========================================================
         *
         * headMap(to, true)
         *
         * means everything <= to.
         */

        System.out.println();
        System.out.println("===== HEADMAP =====");

        for (Map.Entry<LocalDate, List<Visit>> entry :
                visitsByDate
                        .headMap(to, true)
                        .entrySet()) {

            System.out.println(
                    entry.getKey()
                            + " -> "
                            + entry.getValue().size()
            );
        }

        /*
         * ==========================================================
         * DEMONSTRATE tailMap()
         * ==========================================================
         *
         * tailMap(from, true)
         *
         * means everything >= from.
         */

        System.out.println();
        System.out.println("===== TAILMAP =====");

        for (Map.Entry<LocalDate, List<Visit>> entry :
                visitsByDate
                        .tailMap(from, true)
                        .entrySet()) {

            System.out.println(
                    entry.getKey()
                            + " -> "
                            + entry.getValue().size()
            );
        }

        /*
         * ==========================================================
         * COMPARABLE VS COMPARATOR
         * ==========================================================
         */

        System.out.println();
        System.out.println(
                "===== Comparable vs Comparator ====="
        );

        System.out.println(
                "Comparable defines a type's natural ordering. "
                        + "Visit uses visitId as its natural order, "
                        + "so Collections.sort(list) can sort Visit "
                        + "objects without receiving a Comparator."
        );

        System.out.println();

        System.out.println(
                "Comparator is appropriate when a type needs "
                        + "multiple possible orderings or when the "
                        + "ordering does not belong to the class itself. "
                        + "For example, the visit report needs date "
                        + "descending, provider ascending, and duration "
                        + "descending."
        );

        /*
         * ==========================================================
         * Q3 STRETCH
         * ==========================================================
         *
         * Parse a runtime string such as:
         *
         * date:desc,provider:asc,duration:desc
         *
         * and build the Comparator dynamically.
         */

        String specification =
                "date:desc,provider:asc,duration:desc";

        Comparator<Visit> configurableComparator =
                buildComparator(specification);

        List<Visit> configurableSorted =
                new ArrayList<>(visits);

        configurableSorted.sort(
                configurableComparator
        );

        System.out.println();
        System.out.println(
                "===== CONFIGURABLE SORT ====="
        );

        System.out.println(
                "Specification: "
                        + specification
        );

        printVisits(configurableSorted);

        /*
         * Verify that the stretch produces the same result
         * as our original comparator.
         */

        boolean stretchMatches =
                visitsToOutput(
                        comparatorSorted
                ).equals(
                        visitsToOutput(
                                configurableSorted
                        )
                );

        System.out.println();

        System.out.println(
                "Configurable comparator matches "
                        + "original comparator: "
                        + stretchMatches
        );

        if (!stretchMatches) {

            throw new IllegalStateException(
                    "Stretch comparator produced different output."
            );
        }
    }

    /*
     * ==============================================================
     * LOAD VISITS
     * ==============================================================
     */

    private static List<Visit> loadVisits()
            throws IOException {

        List<Visit> visits =
                new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             VISITS_FILE
                     )) {

            /*
             * Skip CSV header.
             */
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] parts =
                        line.split(",");

                String visitId =
                        parts[0].trim();

                String mrn =
                        parts[1].trim();

                String provider =
                        parts[2].trim();

                LocalDate date =
                        LocalDate.parse(
                                parts[3].trim()
                        );

                int durationMins =
                        Integer.parseInt(
                                parts[4].trim()
                        );

                String status =
                        parts[5].trim();

                visits.add(
                        new Visit(
                                visitId,
                                mrn,
                                provider,
                                date,
                                durationMins,
                                status
                        )
                );
            }
        }

        return visits;
    }

    /*
     * ==============================================================
     * RANGE QUERY
     * ==============================================================
     *
     * subMap(from, true, to, true)
     *
     * true  -> inclusive
     * false -> exclusive
     */

    private static List<Visit> visitsBetween(
            TreeMap<LocalDate, List<Visit>> visitsByDate,
            LocalDate from,
            LocalDate to) {

        List<Visit> result =
                new ArrayList<>();

        for (Map.Entry<LocalDate, List<Visit>> entry :
                visitsByDate
                        .subMap(
                                from,
                                true,
                                to,
                                true
                        )
                        .entrySet()) {

            result.addAll(
                    entry.getValue()
            );
        }

        return result;
    }

    /*
     * ==============================================================
     * PRINT VISITS
     * ==============================================================
     */

    private static void printVisits(
            List<Visit> visits) {

        for (Visit visit : visits) {

            System.out.println(visit);
        }
    }

    /*
     * ==============================================================
     * CREATE COMPARABLE OUTPUT
     * ==============================================================
     *
     * Used to prove that the two different comparator implementations
     * produce byte-identical output.
     */

    private static String visitsToOutput(
            List<Visit> visits) {

        StringBuilder output =
                new StringBuilder();

        for (Visit visit : visits) {

            output
                    .append(visit)
                    .append(System.lineSeparator());
        }

        return output.toString();
    }

    /*
     * ==============================================================
     * Q3 STRETCH — BUILD COMPARATOR FROM STRING
     * ==============================================================
     *
     * Example:
     *
     * "date:desc,provider:asc,duration:desc"
     *
     * Supported fields:
     *
     * date
     * provider
     * duration
     * visitId
     *
     * Supported directions:
     *
     * asc
     * desc
     *
     * No Stream API is used.
     */

    private static Comparator<Visit> buildComparator(
            String specification) {

        if (specification == null
                || specification.isBlank()) {

            throw new IllegalArgumentException(
                    "Sort specification cannot be empty."
            );
        }

        Comparator<Visit> result =
                null;

        String[] rules =
                specification.split(",");

        for (String rule : rules) {

            String[] parts =
                    rule.trim().split(":");

            if (parts.length != 2) {

                throw new IllegalArgumentException(
                        "Invalid sort rule: "
                                + rule
                                + ". Expected field:direction"
                );
            }

            String field =
                    parts[0]
                            .trim()
                            .toLowerCase();

            String direction =
                    parts[1]
                            .trim()
                            .toLowerCase();

            Comparator<Visit> current;

            /*
             * Build comparator for the requested field.
             */
            switch (field) {

                case "date":

                    current =
                            Comparator.comparing(
                                    Visit::getDate
                            );

                    break;

                case "provider":

                    /*
                     * nullsLast is required by Q3.
                     */
                    current =
                            Comparator.comparing(
                                    Visit::getProvider,
                                    Comparator.nullsLast(
                                            Comparator.naturalOrder()
                                    )
                            );

                    break;

                case "duration":

                    current =
                            Comparator.comparingInt(
                                    Visit::getDurationMins
                            );

                    break;

                case "visitid":

                    current =
                            Comparator.comparing(
                                    Visit::getVisitId
                            );

                    break;

                default:

                    throw new IllegalArgumentException(
                            "Unknown sort field: "
                                    + field
                    );
            }

            /*
             * Apply direction ONLY to this field's comparator.
             *
             * This is important.
             *
             * We do NOT call reversed() after the entire chain.
             */
            if (direction.equals("desc")) {

                current =
                        current.reversed();

            } else if (!direction.equals("asc")) {

                throw new IllegalArgumentException(
                        "Unknown sort direction: "
                                + direction
                );
            }

            /*
             * Add this comparator to the existing chain.
             */
            if (result == null) {

                result = current;

            } else {

                result =
                        result.thenComparing(
                                current
                        );
            }
        }

        return result;
    }
}
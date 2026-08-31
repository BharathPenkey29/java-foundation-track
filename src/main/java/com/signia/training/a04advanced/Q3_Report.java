package com.signia.training.a04advanced;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Assignment 4 Q3 stream-based report implementation.
 *
 * The goal is to rebuild Assignment 2 Q1 using streams.
 *
 * Important:
 * The report deliberately does not use parallelStream().
 *
 * Parallel streams can help when:
 * - the data set is large,
 * - operations are CPU-heavy,
 * - work is independent,
 * - splitting/merging overhead is small.
 *
 * They can be slower for:
 * - small data sets,
 * - I/O-heavy operations,
 * - cheap transformations,
 * - order-sensitive work,
 * - operations with expensive coordination.
 */
public final class Q3_Report {

    private Q3_Report() {
    }

    /**
     * Visits per provider.
     *
     * Required:
     * groupingBy + counting
     */
    public static Map<String, Long> visitsPerProvider(
            List<Q3_Visit> visits) {

        return visits.stream()
                .collect(
                        Collectors.groupingBy(
                                Q3_Visit::provider,
                                Collectors.counting()
                        )
                );
    }

    /**
     * Total duration in minutes per provider.
     *
     * Required:
     * groupingBy + summingInt
     */
    public static Map<String, Integer> totalMinutesPerProvider(
            List<Q3_Visit> visits) {

        return visits.stream()
                .collect(
                        Collectors.groupingBy(
                                Q3_Visit::provider,
                                Collectors.summingInt(
                                        Q3_Visit::durationMins
                                )
                        )
                );
    }

    /**
     * Average duration per provider.
     *
     * Required:
     * groupingBy + averagingInt
     */
    public static Map<String, Double> averageDurationPerProvider(
            List<Q3_Visit> visits) {

        return visits.stream()
                .collect(
                        Collectors.groupingBy(
                                Q3_Visit::provider,
                                Collectors.averagingInt(
                                        Q3_Visit::durationMins
                                )
                        )
                );
    }

    /**
     * Top three providers by total minutes.
     *
     * Required:
     * sorted + limit
     *
     * Tie-breaking:
     * provider name ascending.
     */
    public static List<Map.Entry<String, Integer>> topThreeProviders(
            List<Q3_Visit> visits) {

        return totalMinutesPerProvider(visits)
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
                .limit(3)
                .toList();
    }

    /**
     * Completed/cancelled split.
     *
     * Required:
     * partitioningBy
     *
     * true  -> COMPLETED
     * false -> everything else
     */
    public static Map<Boolean, List<Q3_Visit>> completedCancelledSplit(
            List<Q3_Visit> visits) {

        return visits.stream()
                .collect(Collectors.partitioningBy(visit -> "COMPLETED".equals(visit.status())));
    }

    /**
     * Every distinct diagnosis.
     *
     * Required:
     * flatMap + distinct + sorted
     *
     * Note:
     * If the Assignment 2 Visit model does not contain diagnoses,
     * this operation is represented by a helper mapping supplied
     * by the caller.
     */
    public static List<String> distinctDiagnoses(
            List<Q3_Visit> visits,
            Function<Q3_Visit, List<String>> diagnosisExtractor) {

        return visits.stream()
                .flatMap(visit -> diagnosisExtractor.apply(visit).stream()
                )
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Busiest date for each provider.
     *
     * The inner map contains:
     *
     * provider -> busiest date
     *
     * If two dates have the same number of visits,
     * the earlier date wins.
     *
     * Required:
     * Collectors.toMap with a merge function.
     */
    public static Map<String, String> busiestDatePerProvider(
            List<Q3_Visit> visits) {

        Map<String, Map<LocalDate, Long>> counts =
                visits.stream()
                        .collect(
                                Collectors.groupingBy(
                                        Q3_Visit::provider,
                                        Collectors.groupingBy(
                                                Q3_Visit::date,
                                                Collectors.counting()
                                        )
                                )
                        );

        return counts.entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry ->
                                        entry.getValue()
                                                .entrySet()
                                                .stream()
                                                .max(
                                                        Map.Entry
                                                                .<LocalDate, Long>comparingByValue()
                                                                .thenComparing(
                                                                        Map.Entry::getKey,
                                                                        Comparator.reverseOrder()
                                                                )
                                                )
                                                .map(
                                                        Map.Entry::getKey
                                                )
                                                .map(
                                                        LocalDate::toString
                                                )
                                                .orElse("N/A"),
                                /*
                                 * Merge function is required by the assignment.
                                 *
                                 * The provider keys should normally be unique,
                                 * so this merge is defensive.
                                 */
                                (left, right) -> left,
                                LinkedHashMap::new
                        )
                );
    }

    /**
     * Optional example:
     * safely find a provider.
     *
     * Required:
     * Optional.map()
     * Optional.filter()
     * Optional.orElseGet()
     *
     * No Optional.get().
     */
    public static String findProviderSummary(
            List<Q3_Visit> visits,
            String provider) {

        return visits.stream()
                .filter(visit -> provider.equals(visit.provider()))
                .findFirst()
                .map(visit -> "Provider " + visit.provider() + " has visits.")
                .filter(summary -> !summary.isBlank())
                .orElseGet(() -> "Provider " + provider + " has no visits.");
    }

    /**
     * Demonstrates Optional for a possibly missing busiest date.
     */
    public static String busiestDateOrFallback(
            List<Q3_Visit> visits,
            String provider) {

        Optional<LocalDate> busiestDate =
                visits.stream()
                        .filter(
                                visit ->
                                        provider.equals(
                                                visit.provider()
                                        )
                        )
                        .collect(
                                Collectors.groupingBy(
                                        Q3_Visit::date,
                                        Collectors.counting()
                                )
                        )
                        .entrySet()
                        .stream()
                        .max(
                                Map.Entry
                                        .<LocalDate, Long>comparingByValue()
                                        .thenComparing(
                                                Map.Entry::getKey
                                        )
                        )
                        .map(
                                Map.Entry::getKey
                        );

        return busiestDate
                .map(LocalDate::toString)
                .filter(
                        value ->
                                !value.isBlank()
                )
                .orElseGet(
                        () ->
                                "No visits"
                );
    }

    /**
     * Stretch:
     *
     * One custom collector computes:
     * count
     * sum
     * min
     * max
     * average
     *
     * in a single stream collection.
     */
    public static Collector<
            Q3_Visit,
            Q3_Statistics,
            Q3_Statistics
            > statisticsCollector() {

        return Collector.of(
                Q3_Statistics::new,

                Q3_Statistics::accept,

                Q3_Statistics::combine,

                Q3_Statistics::finish
        );
    }

    /**
     * Calculates statistics for the supplied visits.
     */
    public static Q3_Statistics statistics(
            List<Q3_Visit> visits) {

        return visits.stream()
                .collect(
                        statisticsCollector()
                );
    }

    /**
     * Convenience method for distinct providers.
     */
    public static Set<String> providers(
            List<Q3_Visit> visits) {

        return visits.stream()
                .map(Q3_Visit::provider)
                .collect(
                        Collectors.toCollection(
                                TreeSet::new
                        )
                );
    }
}
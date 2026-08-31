package com.signia.training.a04advanced;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Q6 demonstration program.
 */
public final class Q6_IntervalDemo {

    private static final Path VISIT_FILE =
            Path.of(
                    "src",
                    "main",
                    "resources",
                    "a04",
                    "visit-intervals.csv");

    private static final Path WORKING_HOURS_FILE =
            Path.of(
                    "src",
                    "main",
                    "resources",
                    "a04",
                    "working-hours.txt");

    private Q6_IntervalDemo() {
    }

    public static void main(String[] args)
            throws IOException {

        System.out.println(
                "==========================================");

        System.out.println(
                "A4 Q6 - MERGE VISIT INTERVALS");

        System.out.println(
                "==========================================");

        List<Q6_VisitInterval> intervals =
                readVisits(VISIT_FILE);

        Q6_WorkingHours workingHours =
                readWorkingHours(
                        WORKING_HOURS_FILE);

        System.out.println();

        System.out.println(
                "Input: "
                        + VISIT_FILE);

        System.out.println(
                "Working hours: "
                        + workingHours.start()
                        + "-"
                        + workingHours.end());

        System.out.println(
                "Events read: "
                        + intervals.size());

        Map<String, List<Q6_MergedInterval>>
                schedules =
                Q6_IntervalMerger.mergeByProvider(
                        intervals);

        /*
         * Process providers in deterministic order.
         */
        List<String> providers =
                schedules.keySet()
                        .stream()
                        .sorted()
                        .toList();

        for (String provider : providers) {

            System.out.println();
            System.out.println(
                    "==========================================");

            System.out.println(
                    "PROVIDER: "
                            + provider);

            System.out.println(
                    "==========================================");

            List<Q6_VisitInterval> providerIntervals =
                    intervals.stream()
                            .filter(
                                    interval ->
                                            interval.provider()
                                                    .equals(provider))
                            .toList();

            List<Q6_VisitInterval[]> conflicts =
                    Q6_IntervalMerger
                            .findDoubleBookings(
                                    providerIntervals);

            System.out.println();
            System.out.println(
                    "--- DOUBLE-BOOKINGS ---");

            System.out.println(
                    "Count: "
                            + conflicts.size());

            for (Q6_VisitInterval[] conflict
                    : conflicts) {

                System.out.println(
                        conflict[0]
                                + " OVERLAPS "
                                + conflict[1]);
            }

            List<Q6_MergedInterval> merged =
                    schedules.get(provider);

            System.out.println();
            System.out.println(
                    "--- MERGED BUSY BLOCKS ---");

            for (Q6_MergedInterval interval : merged) {

                System.out.println(
                        interval.start()
                                + "-"
                                + interval.end());
            }

            long bookedMinutes =
                    Q6_IntervalMerger
                            .bookedMinutes(merged);

            long freeMinutes =
                    Q6_IntervalMerger
                            .freeMinutes(
                                    merged,
                                    workingHours);

            Q6_MergedInterval largestGap =
                    Q6_IntervalMerger
                            .largestFreeGap(
                                    merged,
                                    workingHours);

            System.out.println();
            System.out.println(
                    "--- TIME SUMMARY ---");

            System.out.println(
                    "Booked minutes: "
                            + bookedMinutes);

            System.out.println(
                    "Free minutes: "
                            + freeMinutes);

            if (largestGap == null) {

                System.out.println(
                        "Largest free gap: none");

            } else {

                System.out.println(
                        "Largest free gap: "
                                + largestGap.start()
                                + "-"
                                + largestGap.end()
                                + " ("
                                + largestGap.durationMinutes()
                                + " minutes)");
            }
        }

        System.out.println();
        System.out.println(
                "==========================================");

        System.out.println(
                "BINARY SEARCH");

        System.out.println(
                "==========================================");

        LocalTime queryTime =
                LocalTime.of(14, 20);

        System.out.println(
                "Query time: "
                        + queryTime);

        for (String provider : providers) {

            List<Q6_MergedInterval> merged =
                    schedules.get(provider);

            boolean handWritten =
                    Q6_IntervalMerger.isBusyAt(
                            merged,
                            queryTime);

            int collectionsResult =
                    Q6_IntervalMerger
                            .collectionsBinarySearch(
                                    merged,
                                    queryTime);

            System.out.println();

            System.out.println(
                    provider);

            System.out.println(
                    "Hand-written binary search: "
                            + handWritten);

            System.out.println(
                    "Collections.binarySearch result: "
                            + collectionsResult);

            if (collectionsResult < 0) {

                int insertionPoint =
                        Q6_IntervalMerger
                                .insertionPointFromBinarySearch(
                                        collectionsResult);

                System.out.println(
                        "Insertion point: "
                                + insertionPoint);

                System.out.println(
                        "Meaning: the requested start time "
                                + "would be inserted at index "
                                + insertionPoint
                                + ".");
            }
        }

        System.out.println();
        System.out.println(
                "==========================================");

        System.out.println(
                "STRETCH - FREE AT 14:20 FOR 45 MINUTES");

        System.out.println(
                "==========================================");

        List<Q6_FreeProvider> freeProviders =
                findFreeProviders(
                        schedules,
                        workingHours,
                        queryTime,
                        45);

        if (freeProviders.isEmpty()) {

            System.out.println(
                    "No provider qualifies.");

        } else {

            for (Q6_FreeProvider provider
                    : freeProviders) {

                System.out.println(
                        provider);
            }
        }

        System.out.println();
        System.out.println(
                "==========================================");

        System.out.println(
                "A4 Q6 COMPLETED");

        System.out.println(
                "==========================================");
    }

    /**
     * Reads visit intervals from CSV.
     */
    private static List<Q6_VisitInterval> readVisits(
            Path path)
            throws IOException {

        List<Q6_VisitInterval> result =
                new ArrayList<>();

        List<String> lines =
                Files.readAllLines(path);

        for (String rawLine : lines) {

            String line =
                    rawLine.trim();

            if (line.isEmpty()) {
                continue;
            }

            /*
             * Header:
             *
             * provider,start,end
             */
            if (line.equalsIgnoreCase(
                    "provider,start,end")) {
                continue;
            }

            String[] parts =
                    line.split(",", -1);

            if (parts.length != 3) {

                throw new IllegalArgumentException(
                        "Invalid visit row: "
                                + line);
            }

            String provider =
                    parts[0].trim();

            LocalTime start =
                    LocalTime.parse(
                            parts[1].trim());

            LocalTime end =
                    LocalTime.parse(
                            parts[2].trim());

            result.add(
                    new Q6_VisitInterval(
                            provider,
                            start,
                            end));
        }

        return List.copyOf(result);
    }

    /**
     * Reads working hours from:
     *
     * start=08:00
     * end=17:00
     */
    private static Q6_WorkingHours readWorkingHours(
            Path path)
            throws IOException {

        LocalTime start = null;
        LocalTime end = null;

        for (String rawLine :
                Files.readAllLines(path)) {

            String line =
                    rawLine.trim();

            if (line.isEmpty()
                    || line.startsWith("#")) {
                continue;
            }

            String[] parts =
                    line.split("=", 2);

            if (parts.length != 2) {
                continue;
            }

            String key =
                    parts[0].trim();

            String value =
                    parts[1].trim();

            if (key.equals("start")) {

                start =
                        LocalTime.parse(value);

            } else if (key.equals("end")) {

                end =
                        LocalTime.parse(value);
            }
        }

        if (start == null || end == null) {

            throw new IllegalArgumentException(
                    "Working-hours file must contain "
                            + "start and end.");
        }

        return new Q6_WorkingHours(
                start,
                end);
    }

    /**
     * Stretch implementation.
     *
     * A provider must:
     *
     * 1. Be free at the requested time.
     * 2. Remain free for at least requiredMinutes.
     * 3. Have the requested period inside working hours.
     *
     * The sweep is performed across all providers.
     */
    private static List<Q6_FreeProvider> findFreeProviders(
            Map<String, List<Q6_MergedInterval>> schedules,
            Q6_WorkingHours workingHours,
            LocalTime requestedTime,
            long requiredMinutes) {

        LocalTime requiredEnd =
                requestedTime.plusMinutes(
                        requiredMinutes);

        /*
         * Requested period must fit in working hours.
         */
        if (requestedTime.isBefore(
                workingHours.start())
                || requiredEnd.isAfter(
                workingHours.end())) {

            return List.of();
        }

        List<Q6_FreeProvider> result =
                new ArrayList<>();

        /*
         * Sweep across all providers.
         *
         * Each provider contributes either:
         *
         * - a busy interval containing requestedTime, or
         * - the next busy interval after requestedTime.
         */
        for (Map.Entry<String,
                List<Q6_MergedInterval>> entry
                : schedules.entrySet()) {

            String provider =
                    entry.getKey();

            List<Q6_MergedInterval> intervals =
                    entry.getValue();

            if (Q6_IntervalMerger.isBusyAt(
                    intervals,
                    requestedTime)) {

                continue;
            }

            LocalTime nextBusy =
                    null;

            for (Q6_MergedInterval interval :
                    intervals) {

                if (interval.start()
                        .isAfter(requestedTime)) {

                    nextBusy =
                            interval.start();

                    break;
                }
            }

            LocalTime freeUntil =
                    nextBusy == null
                            ? workingHours.end()
                            : nextBusy;

            long freeMinutes =
                    Duration.between(
                                    requestedTime,
                                    freeUntil)
                            .toMinutes();

            if (freeMinutes >= requiredMinutes) {

                result.add(
                        new Q6_FreeProvider(
                                provider,
                                requestedTime,
                                freeUntil,
                                freeMinutes));
            }
        }

        return result.stream()
                .sorted(
                        Comparator.comparing(
                                Q6_FreeProvider::provider))
                .toList();
    }
}
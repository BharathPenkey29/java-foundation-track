package com.signia.training.a04advanced;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Q6 interval-merging and search implementation.
 *
 * Responsibilities:
 *
 * 1. Detect actual double-bookings before merging.
 * 2. Sort intervals by start time.
 * 3. Merge overlapping and touching intervals in one linear pass.
 * 4. Calculate booked minutes.
 * 5. Calculate free minutes inside working hours.
 * 6. Find the largest free gap.
 * 7. Perform hand-written binary search.
 * 8. Verify the result using Collections.binarySearch.
 */
public final class Q6_IntervalMerger {

    private static final Comparator<Q6_MergedInterval> BY_START =
            Comparator.comparing(Q6_MergedInterval::start);

    private Q6_IntervalMerger() {
        // Utility class.
    }

    /**
     * Detects actual double-bookings BEFORE intervals are merged.
     *
     * Touching intervals are not reported.
     *
     * Example:
     *
     * [10:00, 10:15)
     * [10:15, 11:00)
     *
     * are touching but not overlapping.
     */
    public static List<Q6_VisitInterval[]> findDoubleBookings(
            List<Q6_VisitInterval> intervals) {

        List<Q6_VisitInterval[]> conflicts = new ArrayList<>();

        List<Q6_VisitInterval> sorted = new ArrayList<>(intervals);

        sorted.sort(
                Comparator.comparing(Q6_VisitInterval::start)
                        .thenComparing(Q6_VisitInterval::end));

        /*
         * Compare every pair.
         *
         * The input for this assignment is small, so the
         * conflict detection is intentionally straightforward.
         *
         * Most importantly, this happens BEFORE merging.
         */
        for (int i = 0; i < sorted.size(); i++) {

            Q6_VisitInterval first =
                    sorted.get(i);

            for (int j = i + 1; j < sorted.size(); j++) {

                Q6_VisitInterval second =
                        sorted.get(j);

                /*
                 * Double-bookings only make sense for
                 * the same provider.
                 */
                if (!first.provider()
                        .equals(second.provider())) {

                    continue;
                }

                /*
                 * Because intervals are sorted by start,
                 * once the next interval starts at or after
                 * first.end, no later interval can overlap first.
                 */
                if (!second.start()
                        .isBefore(first.end())) {

                    break;
                }

                /*
                 * IMPORTANT:
                 *
                 * overlaps() deliberately treats touching
                 * intervals as NOT overlapping.
                 */
                if (first.overlaps(second)) {
                    conflicts.add(
                            new Q6_VisitInterval[]{
                                    first,
                                    second
                            });
                }
            }
        }

        return List.copyOf(conflicts);
    }

    /**
     * Merges intervals for one provider.
     *
     * Sorting is O(n log n).
     *
     * The actual merge is ONE linear O(n) pass.
     *
     * Overlapping intervals are merged.
     *
     * Touching intervals are also merged because the assignment
     * explicitly requires touching intervals to collapse into one
     * busy block.
     *
     * IMPORTANT:
     * Touching intervals are merged here, but they were NOT treated
     * as double-bookings during the conflict-detection phase.
     */
    public static List<Q6_MergedInterval> merge(
            List<Q6_VisitInterval> intervals) {

        if (intervals.isEmpty()) {
            return List.of();
        }

        List<Q6_VisitInterval> sorted =
                new ArrayList<>(intervals);

        sorted.sort(
                Comparator.comparing(Q6_VisitInterval::start)
                        .thenComparing(Q6_VisitInterval::end));

        List<Q6_MergedInterval> merged = new ArrayList<>();

        LocalTime currentStart = sorted.get(0).start();

        LocalTime currentEnd = sorted.get(0).end();

        /*
         * Single linear pass after sorting.
         */
        for (int i = 1; i < sorted.size(); i++) {

            Q6_VisitInterval next =
                    sorted.get(i);

            /*
             * Overlap:
             *
             * next.start < currentEnd
             *
             * Touching:
             *
             * next.start == currentEnd
             *
             * Both cases are merged.
             */
            if (!next.start().isAfter(currentEnd)) {

                if (next.end().isAfter(currentEnd)) {
                    currentEnd = next.end();
                }

            } else {

                merged.add(
                        new Q6_MergedInterval(
                                currentStart,
                                currentEnd));

                currentStart =
                        next.start();

                currentEnd =
                        next.end();
            }
        }

        merged.add(
                new Q6_MergedInterval(
                        currentStart,
                        currentEnd));

        return List.copyOf(merged);
    }

    /**
     * Groups all intervals by provider and merges each provider
     * independently.
     */
    public static Map<String, List<Q6_MergedInterval>>
    mergeByProvider(
            List<Q6_VisitInterval> intervals) {

        Map<String, List<Q6_VisitInterval>> grouped = new HashMap<>();

        for (Q6_VisitInterval interval : intervals) {

            grouped.computeIfAbsent(
                            interval.provider(),
                            ignored -> new ArrayList<>())
                    .add(interval);
        }

        Map<String, List<Q6_MergedInterval>> result = new HashMap<>();

        for (Map.Entry<String, List<Q6_VisitInterval>> entry
                : grouped.entrySet()) {

            result.put(
                    entry.getKey(),
                    merge(entry.getValue()));
        }

        return Map.copyOf(result);
    }

    /**
     * Calculates total booked minutes.
     */
    public static long bookedMinutes(
            List<Q6_MergedInterval> merged) {

        long total = 0;

        for (Q6_MergedInterval interval : merged) {
            total += interval.durationMinutes();
        }

        return total;
    }

    /**
     * Calculates total free minutes inside working hours.
     */
    public static long freeMinutes(
            List<Q6_MergedInterval> merged,
            Q6_WorkingHours workingHours) {

        long workingMinutes =
                workingHours.totalMinutes();

        long bookedInsideWorkingHours = 0;

        for (Q6_MergedInterval interval : merged) {

            LocalTime clippedStart =
                    max(interval.start(),
                            workingHours.start());

            LocalTime clippedEnd =
                    min(interval.end(),
                            workingHours.end());

            if (clippedStart.isBefore(clippedEnd)) {

                bookedInsideWorkingHours +=
                        Duration.between(
                                        clippedStart,
                                        clippedEnd)
                                .toMinutes();
            }
        }

        return workingMinutes
                - bookedInsideWorkingHours;
    }

    /**
     * Finds the largest free gap inside working hours.
     */
    public static Q6_MergedInterval largestFreeGap(
            List<Q6_MergedInterval> merged,
            Q6_WorkingHours workingHours) {

        if (merged.isEmpty()) {

            return new Q6_MergedInterval(
                    workingHours.start(),
                    workingHours.end());
        }

        LocalTime largestStart = null;
        LocalTime largestEnd = null;
        long largestMinutes = -1;

        LocalTime cursor =
                workingHours.start();

        for (Q6_MergedInterval interval : merged) {

            LocalTime busyStart = max(interval.start(),
                            workingHours.start());

            LocalTime busyEnd = min(interval.end(),
                            workingHours.end());

            if (!busyStart.isBefore(busyEnd)) {
                continue;
            }

            if (cursor.isBefore(busyStart)) {

                long gapMinutes =
                        Duration.between(
                                        cursor,
                                        busyStart)
                                .toMinutes();

                if (gapMinutes > largestMinutes) {

                    largestMinutes = gapMinutes;

                    largestStart = cursor;

                    largestEnd = busyStart;
                }
            }

            if (busyEnd.isAfter(cursor)) {
                cursor = busyEnd;
            }
        }

        /*
         * Check the gap after the final appointment.
         */
        if (cursor.isBefore(workingHours.end())) {

            long gapMinutes = Duration.between(
                                    cursor,
                                    workingHours.end())
                            .toMinutes();

            if (gapMinutes > largestMinutes) {

                largestStart = cursor;
                largestEnd =
                        workingHours.end();
            }
        }

        /*
         * No free time.
         */
        if (largestStart == null) {
            return null;
        }

        return new Q6_MergedInterval(
                largestStart,
                largestEnd);
    }

    /**
     * Hand-written binary search.
     *
     * O(log n).
     *
     * Returns true when the requested time belongs to
     * one of the merged busy intervals.
     */
    public static boolean isBusyAt(
            List<Q6_MergedInterval> merged,
            LocalTime time) {

        int low = 0;
        int high = merged.size() - 1;

        while (low <= high) {
            int middle = low + (high - low) / 2;

            Q6_MergedInterval interval = merged.get(middle);

            if (time.isBefore(interval.start())) {

                high = middle - 1;

            } else if (!time.isBefore(interval.end())) {

                /*
                 * time >= end
                 *
                 * Because the interval is half-open,
                 * time == end is NOT busy.
                 */
                low = middle + 1;

            } else {
                /*
                 * start <= time < end
                 */
                return true;
            }
        }

        return false;
    }

    /**
     * Verifies the hand-written binary search against
     * Collections.binarySearch.
     *
     * The comparator orders intervals by their start time.
     */
    public static int collectionsBinarySearch(
            List<Q6_MergedInterval> merged,
            LocalTime time) {

        Q6_MergedInterval probe = new Q6_MergedInterval(time, time.plusNanos(1));

        return java.util.Collections.binarySearch(merged, probe, BY_START);
    }

    /**
     * Explains the negative result from Collections.binarySearch.
     *
     * For a missing value:
     *
     * result = -(insertionPoint) - 1
     *
     * Therefore:
     *
     * insertionPoint = -result - 1
     */
    public static int insertionPointFromBinarySearch(
            int searchResult) {

        if (searchResult >= 0) {
            return searchResult;
        }

        return -searchResult - 1;
    }

    /**
     * Stretch:
     *
     * Find providers who are free at a requested time for at least
     * the requested number of minutes.
     *
     * This uses the merged schedules for all providers.
     */
    public static List<Q6_FreeProvider> findProvidersFreeAtLeast(
            Map<String, List<Q6_MergedInterval>> schedules,
            LocalTime requestedTime,
            long requiredMinutes) {

        if (requiredMinutes < 0) {
            throw new IllegalArgumentException("Required minutes cannot be negative.");
        }

        List<Q6_FreeProvider> result = new ArrayList<>();

        for (Map.Entry<String, List<Q6_MergedInterval>> entry : schedules.entrySet()) {

            String provider = entry.getKey();

            List<Q6_MergedInterval> intervals = entry.getValue();

            /*
             * Binary search determines whether the provider
             * is currently busy.
             */
            if (isBusyAt(intervals, requestedTime)) {
                continue;
            }

            /*
             * Find the next busy interval after requestedTime.
             */
            LocalTime nextBusyStart = null;

            for (Q6_MergedInterval interval : intervals) {

                if (interval.start()
                        .isAfter(requestedTime)) {

                    nextBusyStart =
                            interval.start();

                    break;
                }
            }

            /*
             * If there is no later appointment, the free period
             * continues indefinitely for this schedule.
             *
             * In this assignment we use the working day as the
             * meaningful boundary, so the demo filters it through
             * working hours.
             */
            if (nextBusyStart == null) {
                continue;
            }

            long freeMinutes =
                    Duration.between(
                                    requestedTime,
                                    nextBusyStart)
                            .toMinutes();

            if (freeMinutes >= requiredMinutes) {

                result.add(
                        new Q6_FreeProvider(
                                provider,
                                requestedTime,
                                nextBusyStart,
                                freeMinutes));
            }
        }

        return result.stream()
                .sorted(
                        Comparator.comparing(
                                Q6_FreeProvider::provider))
                .toList();
    }

    private static LocalTime max(
            LocalTime first,
            LocalTime second) {

        return first.isAfter(second) ? first : second;
    }

    private static LocalTime min(
            LocalTime first,
            LocalTime second) {

        return first.isBefore(second) ? first : second;
    }
}
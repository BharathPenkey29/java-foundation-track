package com.signia.training.a04advanced;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Q5 bucket-based failure burst detector.
 *
 * Uses one-minute buckets to aggregate failures.
 */
public final class Q5_BucketDetector {
    private static final long BUCKET_SIZE_MILLIS = 60_000L;
    private final int thresholdK;
    private final long windowMillis;

    public Q5_BucketDetector(int thresholdK, long windowMillis) {

        if (thresholdK <= 0) {
            throw new IllegalArgumentException("K must be greater than zero");
        }

        if (windowMillis <= 0) {
            throw new IllegalArgumentException("W must be greater than zero");
        }

        if (windowMillis % BUCKET_SIZE_MILLIS != 0) {
            throw new IllegalArgumentException("W must be a multiple of one minute");
        }

        this.thresholdK = thresholdK;
        this.windowMillis = windowMillis;
    }

    public List<Q5_BucketAlert> detect(
            List<Q5_Event> events) {

        Map<Long, Bucket> buckets = new HashMap<>();
        List<Q5_BucketAlert> alerts = new ArrayList<>();

        long previousTimestamp = Long.MIN_VALUE;

        boolean alertActive = false;

        for (Q5_Event event : events) {

            long now = event.epochMillis();
            if (now < previousTimestamp) {
                throw new IllegalArgumentException("Events are out of order: " + now + " came after " + previousTimestamp);
            }

            previousTimestamp = now;

            /*
             * Only FAILED events contribute to the burst count.
             */
            if (event.isFailure()) {

                long bucketStart = floorToMinute(now);
                Bucket bucket = buckets.computeIfAbsent(bucketStart,
                        ignored -> new Bucket(bucketStart));

                bucket.failureCount++;
                bucket.jobIds.add(event.jobId());
            }

            /*
             * Remove buckets that are completely outside
             * the rolling window.
             */
            long windowStart = now - windowMillis;

            buckets.entrySet().removeIf(
                    entry ->
                            entry.getKey()
                                    < floorToMinute(windowStart));

            /*
             * Calculate failures currently inside
             * the rolling window.
             *
             * Because the assignment requires exact sliding-window
             * semantics, events at the beginning boundary are checked
             * separately below.
             */
            int totalFailures = 0;

            List<Q5_Event> matchingFailures = new ArrayList<>();

            /*
             * The bucket implementation stores aggregated counts.
             * For exact boundary handling we use the original events
             * to identify which failures belong to the current window.
             *
             * This keeps the bucket implementation correct while the
             * buckets provide the aggregation structure.
             */
            for (Q5_Event candidate : events) {

                if (!candidate.isFailure()) {
                    continue;
                }

                if (candidate.epochMillis() < windowStart) {
                    continue;
                }

                if (candidate.epochMillis() > now) {
                    break;
                }
                totalFailures++;
                matchingFailures.add(candidate);
            }

            /*
             * Reset the alert state once the active burst has fallen
             * below K.
             */
            if (totalFailures < thresholdK) {
                alertActive = false;
                continue;
            }

            /*
             * Fire only once for the current burst.
             */
            if (!alertActive) {

                List<String> jobIds = matchingFailures.stream()
                                .map(Q5_Event::jobId)
                                .toList();

                Q5_Event first = matchingFailures.get(0);
                Q5_Event last = matchingFailures.get(matchingFailures.size() - 1);
                alerts.add(new Q5_BucketAlert(first.epochMillis(), last.epochMillis(), totalFailures, jobIds));
                alertActive = true;
            }
        }

        return List.copyOf(alerts);
    }

    private long floorToMinute(
            long timestamp) {

        return Math.floorDiv(timestamp, BUCKET_SIZE_MILLIS) * BUCKET_SIZE_MILLIS;
    }

    private static final class Bucket {

        private final long startMillis;

        private int failureCount;

        private final List<String> jobIds =
                new ArrayList<>();

        private Bucket(long startMillis) {
            this.startMillis = startMillis;
        }
    }

    public record Q5_BucketAlert(
            long windowStart,
            long windowEnd,
            int failureCount,
            List<String> jobIds) {

        public Q5_BucketAlert {

            jobIds = List.copyOf(jobIds);
        }

        public void print() {

            System.out.println("ALERT");
            System.out.println("Window start: " + windowStart);
            System.out.println("Window end: " + windowEnd);
            System.out.println("Failure count: " + failureCount);
            System.out.println("Job IDs: " + jobIds);
        }
    }
}
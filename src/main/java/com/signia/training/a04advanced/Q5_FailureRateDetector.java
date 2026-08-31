package com.signia.training.a04advanced;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Stretch implementation for Q5.
 *
 * Alerts when:
 *
 * failure rate > 30%
 *
 * and:
 *
 * total events in the window >= minimum sample size.
 */
public final class Q5_FailureRateDetector {
    private static final double FAILURE_RATE_THRESHOLD = 0.30;
    private final long windowMillis;
    private final int minimumSampleSize;
    public Q5_FailureRateDetector(
            long windowMillis,
            int minimumSampleSize
    ) {

        if (windowMillis <= 0) {
            throw new IllegalArgumentException("windowMillis must be positive");
        }

        if (minimumSampleSize <= 0) {
            throw new IllegalArgumentException("minimumSampleSize must be positive");
        }

        this.windowMillis = windowMillis;

        this.minimumSampleSize = minimumSampleSize;
    }

    public List<Q5_RateAlert> detect(
            List<Q5_Event> events
    ) {

        Deque<Q5_Event> window = new ArrayDeque<>();

        List<Q5_RateAlert> alerts = new ArrayList<>();

        long previousTimestamp = Long.MIN_VALUE;

        boolean alertActive = false;

        for (Q5_Event event : events) {

            long now =
                    event.epochMillis();

            if (now < previousTimestamp) {

                throw new IllegalArgumentException(
                        "Events are out of order"
                );
            }

            previousTimestamp =
                    now;

            window.addLast(event);

            /*
             * Remove events older than the window.
             */
            while (!window.isEmpty()
                    && window.peekFirst()
                    .epochMillis()
                    < now - windowMillis) {

                window.removeFirst();
            }

            int total = window.size();

            if (total < minimumSampleSize) {

                alertActive =
                        false;

                continue;
            }

            long failures =
                    window.stream()
                            .filter(Q5_Event::isFailure)
                            .count();

            double failureRate =
                    (double) failures
                            / total;

            if (failureRate <= FAILURE_RATE_THRESHOLD) {

                alertActive =
                        false;

                continue;
            }

            if (!alertActive) {

                List<String> failedJobs =
                        window.stream()
                                .filter(Q5_Event::isFailure)
                                .map(Q5_Event::jobId)
                                .toList();

                alerts.add(
                        new Q5_RateAlert(
                                window.peekFirst()
                                        .epochMillis(),
                                now,
                                total,
                                failures,
                                failureRate,
                                failedJobs
                        )
                );

                alertActive =
                        true;
            }
        }

        return List.copyOf(alerts);
    }

    public record Q5_RateAlert(
            long windowStart,
            long windowEnd,
            int totalEvents,
            long failureCount,
            double failureRate,
            List<String> failedJobIds
    ) {

        public Q5_RateAlert {

            failedJobIds = List.copyOf(failedJobIds);
        }

        public void print() {
            System.out.println("RATE ALERT");
            System.out.println("Window start: " + windowStart);
            System.out.println("Window end: " + windowEnd);
            System.out.println("Total events: " + totalEvents);
            System.out.println("Failures: " + failureCount);
            System.out.printf("Failure rate: %.2f%%%n", failureRate * 100);
            System.out.println("Failed jobs: " + failedJobIds);
        }
    }
}
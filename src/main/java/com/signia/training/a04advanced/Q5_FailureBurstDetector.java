package com.signia.training.a04advanced;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Q5 failure burst detector using an ArrayDeque sliding window.
 *
 * Complexity:
 * O(n) time.
 *
 * Every failure is added to the deque once and removed at most once.
 */
public final class Q5_FailureBurstDetector {

    private final int thresholdK;
    private final long windowMillis;

    public Q5_FailureBurstDetector(
            int thresholdK,
            long windowMillis
    ) {

        if (thresholdK <= 0) {
            throw new IllegalArgumentException(
                    "K must be greater than zero"
            );
        }

        if (windowMillis <= 0) {
            throw new IllegalArgumentException(
                    "W must be greater than zero"
            );
        }

        this.thresholdK = thresholdK;
        this.windowMillis = windowMillis;
    }

    /**
     * Detects failure bursts.
     *
     * Only FAILED events enter the sliding window.
     *
     * For each event timestamp "now", failures older than
     * now - W are evicted.
     *
     * When the window reaches K failures, an alert is emitted.
     *
     * Further failures belonging to the same burst do not create
     * duplicate alerts until the burst has ended.
     */
    public List<Q5_Alert> detect(
            List<Q5_Event> events
    ) {

        Deque<Q5_Event> failureWindow =
                new ArrayDeque<>();

        List<Q5_Alert> alerts =
                new ArrayList<>();

        long previousTimestamp =
                Long.MIN_VALUE;

        boolean alertActive = false;

        for (Q5_Event event : events) {

            /*
             * The assignment states that events are timestamp ordered.
             * Reject malformed/out-of-order input rather than silently
             * producing an incorrect result.
             */
            if (event.epochMillis() < previousTimestamp) {

                throw new IllegalArgumentException(
                        "Events are out of order: "
                                + event.epochMillis()
                                + " came after "
                                + previousTimestamp
                );
            }

            previousTimestamp =
                    event.epochMillis();

            long now =
                    event.epochMillis();

            /*
             * Half-open sliding window:
             *
             * [now - W, now]
             *
             * An event exactly W milliseconds old remains in the
             * window. Events strictly older than now - W are removed.
             */
            while (!failureWindow.isEmpty()
                    && failureWindow.peekFirst()
                    .epochMillis()
                    < now - windowMillis) {

                failureWindow.removeFirst();
            }

            /*
             * If the old burst has dropped below K, the next time
             * K failures accumulate we may create a new alert.
             */
            if (failureWindow.size() < thresholdK) {
                alertActive = false;
            }

            if (!event.isFailure()) {
                continue;
            }

            failureWindow.addLast(event);

            /*
             * Alert only on the transition from below K to K.
             *
             * This prevents:
             *
             * K failures     -> alert
             * K + 1 failures -> no duplicate
             * K + 2 failures -> no duplicate
             */
            if (failureWindow.size() >= thresholdK
                    && !alertActive) {

                List<String> jobIds =
                        failureWindow.stream()
                                .map(Q5_Event::jobId)
                                .toList();

                Q5_Event first =
                        failureWindow.peekFirst();

                Q5_Event last =
                        failureWindow.peekLast();

                Q5_Alert alert = new Q5_Alert(
                                first.epochMillis(),
                                last.epochMillis(),
                                failureWindow.size(),
                                jobIds
                        );

                alerts.add(alert);

                alertActive = true;
            }
        }

        return List.copyOf(alerts);
    }

    /**
     * Immutable alert result.
     */
    public record Q5_Alert(
            long windowStart,
            long windowEnd,
            int failureCount,
            List<String> jobIds
    ) {

        public Q5_Alert {
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
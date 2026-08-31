package com.signia.training.a04advanced;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Assignment 4 Q5 driver.
 *
 * Usage:
 *
 * mvn -q compile exec:java \
 * -Dexec.mainClass=com.signia.training.a04advanced.Q5_FailureBurstDemo \
 * -Dexec.args="5 60000"
 */
public class Q5_FailureBurstDemo {

    private static final Path EVENTS_FILE = Path.of("src", "main", "resources", "a04", "events.txt");
    private static final int DEFAULT_K = 5;
    private static final long DEFAULT_WINDOW = 60_000L;
    public static void main(String[] args)
            throws Exception {

        int k = args.length >= 1 ? Integer.parseInt(args[0]) : DEFAULT_K;
        long windowMillis = args.length >= 2 ? Long.parseLong(args[1]) : DEFAULT_WINDOW;
        System.out.println("==========================================");
        System.out.println("A4 Q5 - FAILURE BURST DETECTOR");
        System.out.println("==========================================");
        System.out.println("Input: " + EVENTS_FILE);
        System.out.println("K: " + k);

        System.out.println("W: " + windowMillis + " ms");
        List<Q5_Event> events = readEvents(EVENTS_FILE);

        System.out.println("Events read: " + events.size());

        /*
         * =========================================================
         * ARRAYDEQUE SLIDING WINDOW
         * =========================================================
         */

        System.out.println();
        System.out.println("--- ARRAYDEQUE SLIDING WINDOW ---");

        Q5_FailureBurstDetector dequeDetector = new Q5_FailureBurstDetector(k, windowMillis);

        List<Q5_FailureBurstDetector.Q5_Alert>
                dequeAlerts = dequeDetector.detect(events);

        System.out.println("Alerts: " + dequeAlerts.size());

        for (int i = 0; i < dequeAlerts.size(); i++) {
            System.out.println();
            System.out.println("Alert #" + (i + 1));
            dequeAlerts
                    .get(i)
                    .print();
        }

        /*
         * =========================================================
         * BUCKET IMPLEMENTATION
         * =========================================================
         */

        System.out.println();
        System.out.println("--- ONE-MINUTE BUCKETS ---");

        Q5_BucketDetector bucketDetector = new Q5_BucketDetector(k, windowMillis);

        List<Q5_BucketDetector.Q5_BucketAlert> bucketAlerts = bucketDetector.detect(events);

        System.out.println("Alerts: " + bucketAlerts.size());

        for (int i = 0; i < bucketAlerts.size(); i++) {
            System.out.println();
            System.out.println("Alert #" + (i + 1));bucketAlerts.get(i).print();
        }

        /*
         * =========================================================
         * STRETCH
         * =========================================================
         */

        System.out.println();
        System.out.println("--- STRETCH: FAILURE RATE ---");

        Q5_FailureRateDetector rateDetector = new Q5_FailureRateDetector(windowMillis, 5);

        List<Q5_FailureRateDetector.Q5_RateAlert>
                rateAlerts = rateDetector.detect(events);

        System.out.println("Rate alerts: " + rateAlerts.size());

        for (int i = 0; i < rateAlerts.size(); i++) {
            System.out.println();
            System.out.println("Rate alert #" + (i + 1));

            rateAlerts
                    .get(i)
                    .print();
        }

        /*
         * =========================================================
         * VERIFICATION
         * =========================================================
         */

        System.out.println();
        System.out.println("--- VERIFICATION ---");
        System.out.println("Expected committed-fixture burst alerts: 2");

        System.out.println("ArrayDeque alerts: " + dequeAlerts.size());
        System.out.println("Bucket alerts: " + bucketAlerts.size());
        if (dequeAlerts.size() != 2) {
            throw new IllegalStateException("ArrayDeque detector expected 2 alerts but found " + dequeAlerts.size());
        }
        if (bucketAlerts.size() != 2) {
            throw new IllegalStateException("Bucket detector expected 2 alerts but found " + bucketAlerts.size());
        }
        System.out.println("Q5 committed-fixture verification: PASS");
    }

    /**
     * Reads the committed events.txt fixture.
     *
     * Expected format:
     *
     * epochMillis|jobId|status
     */
    private static List<Q5_Event> readEvents(
            Path path
    ) throws IOException {

        List<Q5_Event> events = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;

            boolean firstLine = true;

            while ((line = reader.readLine())
                    != null) {

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                /*
                 * Skip the comment/header:
                 *
                 * # epochMillis|jobId|status
                 */
                if (firstLine
                        && line.startsWith("#")) {

                    firstLine = false;

                    continue;
                }

                firstLine = false;

                String[] parts = line.split("\\|", -1);

                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid event line: " + line);
                }

                long timestamp = Long.parseLong(parts[0].trim());
                String jobId = parts[1].trim();
                String status = parts[2].trim();
                events.add(new Q5_Event(timestamp, jobId, status)
                );
            }
        }

        return List.copyOf(events);
    }
}
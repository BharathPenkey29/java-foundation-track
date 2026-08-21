package com.signia.training.a02collections;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class RetryQueuePriority {

    private static final int MAX_ATTEMPTS = 3;
    private static final double FAILURE_RATE = 0.30;
    public static void main(String[] args) {

        /*
         * STRETCH:
         * Replace ArrayDeque with PriorityQueue.
         *
         * Jobs with fewer attempts have higher priority.
         * Therefore first-time jobs are processed before jobs
         * that are being retried.
         *
         * PriorityQueue insertion and removal are O(log n).
         * Inspecting the head with peek() is O(1).
         */
        Queue<Job> retryQueue = new PriorityQueue<>(
                Comparator.comparingInt(Job::getAttempt)
                        .thenComparing(Job::getJobId)
        );

        /*
         * Seeded Random makes the simulation reproducible.
         */
        Random random = new Random(14);

        LocalDateTime now = LocalDateTime.now();

        retryQueue.add(
                new Job("JOB-001", now.minusDays(10))
        );

        retryQueue.add(
                new Job("JOB-002", now.minusDays(5))
        );

        retryQueue.add(
                new Job("JOB-003", now.minusDays(12))
        );

        retryQueue.add(
                new Job("JOB-004", now.minusDays(2))
        );

        retryQueue.add(
                new Job("JOB-005", now.minusDays(15))
        );

        System.out.println(
                "===== PRIORITY RETRY QUEUE STRETCH ====="
        );

        while (!retryQueue.isEmpty()) {

            Job job = retryQueue.poll();

            job.incrementAttempt();

            boolean failed =
                    random.nextDouble() < FAILURE_RATE;

            if (!failed) {

                System.out.println(
                        job.getJobId()
                                + " | attempt "
                                + job.getAttempt()
                                + " | SUCCESS"
                );

            } else {

                job.setLastError("Simulated failure");

                System.out.println(
                        job.getJobId()
                                + " | attempt "
                                + job.getAttempt()
                                + " | FAILED"
                );

                if (job.getAttempt() < MAX_ATTEMPTS) {

                    retryQueue.add(job);

                    System.out.println(
                            "  -> requeued by priority"
                    );

                } else {

                    System.out.println(
                            "  -> moved to dead letter queue"
                    );
                }
            }
        }
    }
}
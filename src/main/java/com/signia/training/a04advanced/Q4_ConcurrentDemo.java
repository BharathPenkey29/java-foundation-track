package com.signia.training.a04advanced;

import java.util.List;

/**
 * Assignment 4 Q4 driver.
 */
public class Q4_ConcurrentDemo {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "A4 Q4 - CONCURRENT JOB RUNNER"
        );

        System.out.println(
                "=========================================="
        );

        /*
         * =========================================================
         * 50 JOB DEMONSTRATION
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "--- 50 JOBS ---"
        );

        List<Q4_Job> jobs =
                Q4_JobRunner.createJobs(
                        50
                );

        long platformStart =
                System.nanoTime();

        List<Q4_JobResult> platformResults =
                Q4_JobRunner.runWithPlatformPool(
                        jobs
                );

        long platformMillis =
                elapsedMillis(platformStart);

        Q4_JobRunner.printSummary(
                "VERSION 1 - PLATFORM THREAD POOL",
                platformResults,
                platformMillis
        );

        long futureStart =
                System.nanoTime();

        List<Q4_JobResult> futureResults =
                Q4_JobRunner.runWithCompletableFuture(
                        jobs
                );

        long futureMillis =
                elapsedMillis(futureStart);

        Q4_JobRunner.printSummary(
                "VERSION 2 - COMPLETABLE FUTURE",
                futureResults,
                futureMillis
        );

        long virtualStart =
                System.nanoTime();

        List<Q4_JobResult> virtualResults =
                Q4_JobRunner.runWithVirtualThreads(
                        jobs
                );

        long virtualMillis =
                elapsedMillis(virtualStart);

        Q4_JobRunner.printSummary(
                "VERSION 3 - VIRTUAL THREADS",
                virtualResults,
                virtualMillis
        );

        /*
         * =========================================================
         * 10,000 JOB DEMONSTRATION
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "--- 10,000 JOBS ---"
        );

        /*
         * IMPORTANT:
         *
         * 10,000 jobs each sleeping up to 800 ms means a platform
         * pool of 4 can take a long time.
         *
         * This is deliberate. The assignment asks us to demonstrate
         * that a pool of four serializes the workload while virtual
         * threads allow much higher concurrency.
         */

        List<Q4_Job> largeJobs =
                Q4_JobRunner.createJobs(
                        10_000
                );

        long largePlatformStart =
                System.nanoTime();

        List<Q4_JobResult> largePlatformResults =
                Q4_JobRunner.runWithPlatformPool(
                        largeJobs
                );

        long largePlatformMillis =
                elapsedMillis(
                        largePlatformStart
                );

        Q4_JobRunner.printSummary(
                "10,000 - PLATFORM THREAD POOL",
                largePlatformResults,
                largePlatformMillis
        );

        long largeVirtualStart =
                System.nanoTime();

        List<Q4_JobResult> largeVirtualResults =
                Q4_JobRunner.runWithVirtualThreads(
                        largeJobs
                );

        long largeVirtualMillis =
                elapsedMillis(
                        largeVirtualStart
                );

        Q4_JobRunner.printSummary(
                "10,000 - VIRTUAL THREADS",
                largeVirtualResults,
                largeVirtualMillis
        );

        /*
         * =========================================================
         * SPEEDUP
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "--- SPEEDUP ---"
        );

        printSpeedup(
                "Virtual threads vs platform pool",
                largePlatformMillis,
                largeVirtualMillis
        );

        /*
         * =========================================================
         * COUNTER DEMONSTRATION
         * =========================================================
         */

        Q4_CounterDemo.run();

        /*
         * =========================================================
         * VIRTUAL THREAD LOCK DEMONSTRATION
         * =========================================================
         */

        Q4_VirtualThreadLockDemo.run();

        /*
         * =========================================================
         * FINAL NOTE
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "A4 Q4 COMPLETED"
        );

        System.out.println(
                "=========================================="
        );
    }

    private static long elapsedMillis(
            long start
    ) {

        return (
                System.nanoTime()
                        - start
        ) / 1_000_000L;
    }

    private static void printSpeedup(
            String label,
            long slowerMillis,
            long fasterMillis
    ) {

        if (fasterMillis <= 0) {

            System.out.println(
                    label + ": unavailable"
            );

            return;
        }

        double speedup =
                (double) slowerMillis
                        / fasterMillis;

        System.out.printf(
                "%s: %.2fx%n",
                label,
                speedup
        );
    }
}
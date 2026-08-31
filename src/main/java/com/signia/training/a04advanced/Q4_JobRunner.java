package com.signia.training.a04advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Assignment 4 Q4.
 *
 * Runs simulated automation jobs using:
 *
 * 1. A bounded platform-thread pool.
 * 2. CompletableFuture composition.
 * 3. Virtual threads.
 *
 * The same job list is used for each implementation so that the
 * comparison is meaningful.
 */
public final class Q4_JobRunner {

    private static final int DEFAULT_JOB_COUNT = 50;

    private static final int LARGE_JOB_COUNT = 10_000;

    private static final int PLATFORM_POOL_SIZE = 4;

    private static final long RANDOM_SEED = 42L;

    private Q4_JobRunner() {
    }

    /**
     * Creates reproducible simulated jobs.
     *
     * Each job:
     * - sleeps for 100-800 ms
     * - has approximately 20% chance of failure
     */
    public static List<Q4_Job> createJobs(int count) {

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "count must be positive"
            );
        }

        Random random =
                new Random(RANDOM_SEED);

        List<Q4_Job> jobs =
                new ArrayList<>(count);

        for (int i = 1; i <= count; i++) {

            long sleepMillis =
                    100L
                            + random.nextInt(701);

            boolean shouldFail =
                    random.nextInt(100) < 20;

            jobs.add(
                    new Q4_Job(
                            i,
                            sleepMillis,
                            shouldFail
                    )
            );
        }

        return List.copyOf(jobs);
    }

    /**
     * Version 1:
     *
     * Fixed platform-thread pool with four threads.
     *
     * Required by the assignment:
     * ExecutorService
     * Callable
     * invokeAll
     * Future
     * ExecutionException
     */
    public static List<Q4_JobResult> runWithPlatformPool(
            List<Q4_Job> jobs
    ) throws InterruptedException {

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        PLATFORM_POOL_SIZE
                );

        try {

            List<java.util.concurrent.Callable<Q4_JobResult>>
                    tasks =
                    jobs.stream()
                            .map(
                                    job ->
                                            (java.util.concurrent.Callable<Q4_JobResult>)
                                                    () -> executeJob(job)
                            )
                            .toList();

            List<Future<Q4_JobResult>> futures =
                    executor.invokeAll(tasks);

            List<Q4_JobResult> results =
                    new ArrayList<>(
                            jobs.size()
                    );

            for (Future<Q4_JobResult> future : futures) {

                try {

                    results.add(
                            future.get()
                    );

                } catch (ExecutionException exception) {

                    Throwable cause =
                            exception.getCause();

                    String message = cause == null ? "Job failed" : cause.getClass().getSimpleName();

                    int jobIndex = results.size();

                    results.add(new Q4_JobResult(
                                    jobs.get(jobIndex).jobId(), false,
                                    message, Thread.currentThread().getName())
                    );
                }
            }

            return List.copyOf(results);

        } finally {

            shutdownExecutor(executor);
        }
    }

    /**
     * Version 2:
     *
     * CompletableFuture composition.
     *
     * Required operations:
     * supplyAsync
     * thenApply
     * exceptionally
     * allOf
     */
    public static List<Q4_JobResult> runWithCompletableFuture(
            List<Q4_Job> jobs
    ) {

        List<CompletableFuture<Q4_JobResult>> futures =
                jobs.stream()
                        .map(
                                job ->
                                        CompletableFuture
                                                .supplyAsync(
                                                        () -> executeJobUnchecked(job)
                                                )
                                                .thenApply(
                                                        Q4_JobRunner::markCompleted
                                                )
                                                .exceptionally(
                                                        throwable ->
                                                                new Q4_JobResult(
                                                                        job.jobId(),
                                                                        false,
                                                                        "Job failed",
                                                                        Thread.currentThread()
                                                                                .getName()
                                                                )
                                                )
                        )
                        .toList();

        CompletableFuture<Void> all =
                CompletableFuture.allOf(
                        futures.toArray(
                                new CompletableFuture<?>[0]
                        )
                );

        all.join();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * Version 3:
     *
     * One virtual thread per job.
     *
     * No pool sizing is used.
     */
    public static List<Q4_JobResult> runWithVirtualThreads(
            List<Q4_Job> jobs
    ) throws InterruptedException {

        try (ExecutorService executor =
                     Executors.newVirtualThreadPerTaskExecutor()) {

            List<Future<Q4_JobResult>> futures =
                    new ArrayList<>(
                            jobs.size()
                    );

            for (Q4_Job job : jobs) {

                futures.add(
                        executor.submit(
                                () -> executeJob(job)
                        )
                );
            }

            List<Q4_JobResult> results =
                    new ArrayList<>(
                            jobs.size()
                    );

            for (int i = 0; i < futures.size(); i++) {

                Future<Q4_JobResult> future =
                        futures.get(i);

                try {

                    results.add(
                            future.get()
                    );

                } catch (ExecutionException exception) {

                    Throwable cause =
                            exception.getCause();

                    results.add(
                            new Q4_JobResult(
                                    jobs.get(i).jobId(),
                                    false,
                                    cause == null
                                            ? "Job failed"
                                            : cause.getClass()
                                            .getSimpleName(),
                                    Thread.currentThread()
                                            .getName()
                            )
                    );
                }
            }

            /*
             * ExecutorService.close() performs an orderly shutdown and
             * waits for submitted tasks to complete.
             *
             * This is why try-with-resources is appropriate here.
             */
            return List.copyOf(results);
        }
    }

    /**
     * Executes one simulated job.
     */
    private static Q4_JobResult executeJob(
            Q4_Job job
    ) throws InterruptedException {

        Thread.sleep(
                job.sleepMillis()
        );

        if (job.shouldFail()) {

            throw new IllegalStateException(
                    "Simulated failure"
            );
        }

        return new Q4_JobResult(
                job.jobId(),
                true,
                "SUCCESS",
                Thread.currentThread()
                        .getName()
        );
    }

    /**
     * Adapter used by CompletableFuture.supplyAsync(),
     * because a Supplier cannot declare InterruptedException.
     */
    private static Q4_JobResult executeJobUnchecked(
            Q4_Job job
    ) {

        try {

            return executeJob(job);

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Job interrupted",
                    exception
            );
        }
    }

    /**
     * Demonstrates thenApply.
     */
    private static Q4_JobResult markCompleted(
            Q4_JobResult result
    ) {

        if (!result.success()) {
            return result;
        }

        return new Q4_JobResult(
                result.jobId(),
                true,
                "COMPLETED: " + result.message(),
                result.threadName()
        );
    }

    /**
     * Safely shuts down a platform-thread executor.
     *
     * Required sequence:
     *
     * shutdown()
     * awaitTermination()
     * shutdownNow() if needed
     */
    private static void shutdownExecutor(
            ExecutorService executor
    ) {

        executor.shutdown();

        try {

            if (!executor.awaitTermination(
                    30,
                    TimeUnit.SECONDS
            )) {

                executor.shutdownNow();

                if (!executor.awaitTermination(
                        30,
                        TimeUnit.SECONDS
                )) {

                    System.out.println(
                            "Executor did not terminate cleanly."
                    );
                }
            }

        } catch (InterruptedException exception) {

            executor.shutdownNow();

            Thread.currentThread().interrupt();
        }
    }

    /**
     * Counts successful results.
     */
    public static long countSuccessful(
            List<Q4_JobResult> results
    ) {

        return results.stream()
                .filter(Q4_JobResult::success)
                .count();
    }

    /**
     * Counts failed results.
     */
    public static long countFailed(
            List<Q4_JobResult> results
    ) {

        return results.stream()
                .filter(
                        result ->
                                !result.success()
                )
                .count();
    }

    /**
     * Prints a summary.
     */
    public static void printSummary(
            String name,
            List<Q4_JobResult> results,
            long elapsedMillis
    ) {

        long successful =
                countSuccessful(results);

        long failed =
                countFailed(results);

        System.out.println();
        System.out.println(
                "--- " + name + " ---"
        );

        System.out.println(
                "Jobs: " + results.size()
        );

        System.out.println(
                "Successful: " + successful
        );

        System.out.println(
                "Failed: " + failed
        );

        System.out.println(
                "Wall time: "
                        + elapsedMillis
                        + " ms"
        );
    }

    /**
     * Runs a timed operation.
     */
    public static long measureMillis(
            ThrowingOperation operation
    ) throws Exception {

        long start =
                System.nanoTime();

        operation.run();

        long end =
                System.nanoTime();

        return TimeUnit.NANOSECONDS
                .toMillis(
                        end - start
                );
    }

    @FunctionalInterface
    public interface ThrowingOperation {

        void run() throws Exception;
    }
}
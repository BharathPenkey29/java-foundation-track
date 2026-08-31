package com.signia.training.a04advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates lost updates and four ways to fix them.
 */
public final class Q4_CounterDemo {

    private static final int THREAD_COUNT = 8;

    private static final int INCREMENTS_PER_THREAD = 1_250;

    private static final int EXPECTED =
            THREAD_COUNT
                    * INCREMENTS_PER_THREAD;

    private Q4_CounterDemo() {
    }

    /**
     * Runs all counter implementations.
     */
    public static void run() throws InterruptedException {

        System.out.println();
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "COUNTER / LOST UPDATE DEMONSTRATION"
        );

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Threads: " + THREAD_COUNT
        );

        System.out.println(
                "Increments per thread: "
                        + INCREMENTS_PER_THREAD
        );

        System.out.println(
                "Expected total: "
                        + EXPECTED
        );

        runPlainInt();

        runSynchronized();

        runAtomicInteger();

        runConcurrentHashMap();
    }

    /**
     * Deliberately broken implementation.
     *
     * count++ is not atomic.
     *
     * It is effectively:
     *
     * read
     * add
     * write
     *
     * Two threads can read the same old value and overwrite
     * each other's update.
     */
    private static void runPlainInt()
            throws InterruptedException {

        final int[] counter = {0};

        long start =
                System.nanoTime();

        runEightThreads(
                () -> {

                    for (int i = 0;
                         i < INCREMENTS_PER_THREAD;
                         i++) {

                        /*
                         * DELIBERATELY BROKEN.
                         *
                         * The small pause increases the chance that
                         * another thread interleaves between read/write.
                         */
                        int current =
                                counter[0];

                        Thread.yield();

                        counter[0] =
                                current + 1;
                    }
                }
        );

        long elapsed =
                elapsedMillis(start);

        System.out.println();
        System.out.println(
                "Plain int"
        );

        System.out.println(
                "Actual: "
                        + counter[0]
        );

        System.out.println(
                "Expected: "
                        + EXPECTED
        );

        System.out.println(
                "Lost updates: "
                        + (EXPECTED - counter[0])
        );

        System.out.println(
                "Wall time: "
                        + elapsed
                        + " ms"
        );
    }

    /**
     * synchronized protects the entire read-modify-write operation.
     */
    private static void runSynchronized()
            throws InterruptedException {

        SynchronizedCounter counter =
                new SynchronizedCounter();

        long start =
                System.nanoTime();

        runEightThreads(
                () -> {

                    for (int i = 0;
                         i < INCREMENTS_PER_THREAD;
                         i++) {

                        counter.increment();
                    }
                }
        );

        long elapsed =
                elapsedMillis(start);

        System.out.println();
        System.out.println(
                "synchronized"
        );

        System.out.println(
                "Actual: "
                        + counter.get()
        );

        System.out.println(
                "Expected: "
                        + EXPECTED
        );

        System.out.println(
                "Wall time: "
                        + elapsed
                        + " ms"
        );
    }

    /**
     * AtomicInteger provides atomic increment.
     */
    private static void runAtomicInteger()
            throws InterruptedException {

        AtomicInteger counter =
                new AtomicInteger();

        long start =
                System.nanoTime();

        runEightThreads(
                () -> {

                    for (int i = 0;
                         i < INCREMENTS_PER_THREAD;
                         i++) {

                        counter.incrementAndGet();
                    }
                }
        );

        long elapsed =
                elapsedMillis(start);

        System.out.println();
        System.out.println(
                "AtomicInteger"
        );

        System.out.println(
                "Actual: "
                        + counter.get()
        );

        System.out.println(
                "Expected: "
                        + EXPECTED
        );

        System.out.println(
                "Wall time: "
                        + elapsed
                        + " ms"
        );
    }

    /**
     * ConcurrentHashMap.merge is useful when the requirement
     * is to maintain counts per status.
     */
    private static void runConcurrentHashMap()
            throws InterruptedException {

        ConcurrentHashMap<String, Integer> counts =
                new ConcurrentHashMap<>();

        long start =
                System.nanoTime();

        runEightThreads(
                () -> {

                    for (int i = 0;
                         i < INCREMENTS_PER_THREAD;
                         i++) {

                        counts.merge(
                                "SUCCESS",
                                1,
                                Integer::sum
                        );
                    }
                }
        );

        long elapsed =
                elapsedMillis(start);

        System.out.println();
        System.out.println(
                "ConcurrentHashMap.merge"
        );

        System.out.println(
                "SUCCESS count: "
                        + counts.getOrDefault(
                        "SUCCESS",
                        0
                )
        );

        System.out.println(
                "Expected: "
                        + EXPECTED
        );

        System.out.println(
                "Wall time: "
                        + elapsed
                        + " ms"
        );
    }

    /**
     * Runs eight concurrent workers and waits for all of them.
     */
    private static void runEightThreads(
            Runnable action
    ) throws InterruptedException {

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        THREAD_COUNT
                );

        CountDownLatch ready =
                new CountDownLatch(
                        THREAD_COUNT
                );

        CountDownLatch start =
                new CountDownLatch(1);

        CountDownLatch done =
                new CountDownLatch(
                        THREAD_COUNT
                );

        try {

            for (int i = 0;
                 i < THREAD_COUNT;
                 i++) {

                executor.submit(
                        () -> {

                            ready.countDown();

                            try {

                                start.await();

                                action.run();

                            } catch (InterruptedException exception) {

                                Thread.currentThread()
                                        .interrupt();

                            } finally {

                                done.countDown();
                            }
                        }
                );
            }

            ready.await();

            start.countDown();

            done.await();

        } finally {

            executor.shutdown();
        }
    }

    private static long elapsedMillis(
            long start
    ) {

        return (
                System.nanoTime()
                        - start
        ) / 1_000_000L;
    }

    /**
     * Thread-safe counter using synchronized.
     */
    private static final class SynchronizedCounter {

        private int value;

        public synchronized void increment() {

            value++;
        }

        public synchronized int get() {

            return value;
        }
    }
}
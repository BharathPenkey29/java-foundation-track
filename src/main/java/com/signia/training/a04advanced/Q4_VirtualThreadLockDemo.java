package com.signia.training.a04advanced;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates synchronized versus ReentrantLock with virtual threads.
 *
 * Important:
 * This demonstration is specifically relevant to Java 21.
 */
public final class Q4_VirtualThreadLockDemo {
    private static final int THREAD_COUNT = 100;
    private static final long BLOCK_MILLIS = 20;
    private Q4_VirtualThreadLockDemo() {
    }

    public static void run()
            throws InterruptedException {
        System.out.println();
        System.out.println("==========================================");
        System.out.println("VIRTUAL THREAD LOCK DEMONSTRATION");
        System.out.println("==========================================");
        runSynchronized();
        runReentrantLock();
    }

    /**
     * Java 21 demonstration:
     *
     * A virtual thread blocking while holding a synchronized monitor
     * can pin its carrier thread.
     */
    private static void runSynchronized()
            throws InterruptedException {
        Object monitor = new Object();

        long start = System.nanoTime();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(
                        () -> {
                            synchronized (monitor) {
                                try {
                                    Thread.sleep(BLOCK_MILLIS);
                                } catch (InterruptedException exception) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                );
            }
        }

        long elapsed = elapsedMillis(start);
        System.out.println();
        System.out.println("synchronized monitor");
        System.out.println("Elapsed: " + elapsed + " ms");
    }

    /**
     * ReentrantLock allows the virtual thread to park without
     * pinning the carrier in the same way as a synchronized monitor.
     */
    private static void runReentrantLock()
            throws InterruptedException {

        ReentrantLock lock = new ReentrantLock();
        long start = System.nanoTime();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(
                        () -> {
                            lock.lock();
                            try {
                                Thread.sleep(BLOCK_MILLIS);
                            } catch (InterruptedException exception) {
                                Thread.currentThread().interrupt();
                            } finally {
                                lock.unlock();
                            }
                        }
                );
            }
        }

        long elapsed = elapsedMillis(start);
        System.out.println();
        System.out.println("ReentrantLock");
        System.out.println("Elapsed: " + elapsed + " ms");
    }

    private static long elapsedMillis(
            long start
    ) {
        return (System.nanoTime() - start) / 1_000_000L;
    }
}
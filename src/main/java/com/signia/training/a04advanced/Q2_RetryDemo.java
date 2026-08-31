package com.signia.training.a04advanced;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates Assignment 4 Q2.
 */
public class Q2_RetryDemo {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("A4 Q2 - GENERIC RETRY WITH BACKOFF");
        System.out.println("==========================================");
        demonstrateFlakyOperation();
        demonstratePermanentFailure();
        demonstrateVoidOperation();
        demonstrateSuppressedHistory();
        demonstrateInterruptHandling();
        demonstrateCircuitBreaker();
    }

    private static void demonstrateFlakyOperation() {

        System.out.println();
        System.out.println("--- FLAKY OPERATION ---");

        AtomicInteger attempts = new AtomicInteger();
        try {

            String result = Q2_Retry.retry(
                            () -> {
                                int attempt = attempts.incrementAndGet();
                                System.out.println("Executing flaky action: attempt " + attempt);

                                if (attempt < 3) {
                                    throw new IllegalStateException("temporary failure");
                                }
                                return "SUCCESS";
                            },
                            5,
                            50,
                            exception -> exception instanceof IllegalStateException
                    );

            System.out.println("Result: " + result);
            System.out.println("Total attempts: " + attempts.get());

        } catch (Exception exception) {

            System.out.println("Unexpected failure: " + exception.getClass().getSimpleName());
        }
    }

    private static void demonstratePermanentFailure() {

        System.out.println();
        System.out.println("--- PERMANENT FAILURE ---");

        AtomicInteger attempts = new AtomicInteger();

        try {
            Q2_Retry.retry(
                    () -> {
                        attempts.incrementAndGet();
                        throw new IllegalArgumentException("validation data must not be logged");
                    },
                    5,
                    50,
                    exception ->
                            exception instanceof IllegalStateException
            );

        } catch (Exception exception) {

            System.out.println("Failure type: " + exception.getClass().getSimpleName());
            System.out.println("Attempts: " + attempts.get());
            System.out.println("Expected attempts: 1");
        }
    }

    private static void demonstrateVoidOperation() {

        System.out.println();
        System.out.println("--- VOID RETRY ---");
        AtomicInteger attempts = new AtomicInteger();

        try {

            Q2_Retry.retry(
                    () -> {

                        int attempt = attempts.incrementAndGet();
                        System.out.println("Void action attempt " + attempt);
                        if (attempt < 2) {
                            throw new IllegalStateException("temporary failure");
                        }
                    },
                    3,
                    50,
                    exception ->
                            exception
                                    instanceof IllegalStateException
            );

            System.out.println("Void operation completed successfully.");

        } catch (Exception exception) {

            System.out.println("Unexpected failure: " + exception.getClass().getSimpleName());
        }
    }

    private static void demonstrateSuppressedHistory() {

        System.out.println();
        System.out.println("--- SUPPRESSED ATTEMPT HISTORY ---");

        AtomicInteger attempts = new AtomicInteger();

        try {

            Q2_Retry.retry(
                    () -> {
                        int attempt = attempts.incrementAndGet();

                        throw new IllegalStateException("failure-" + attempt);
                    },
                    3,
                    20,
                    exception ->
                            exception
                                    instanceof IllegalStateException
            );

        } catch (Exception exception) {

            System.out.println("Final exception: " + exception.getClass().getSimpleName());
            System.out.println("Total attempts: " + attempts.get());
            System.out.println("Suppressed failures: " + exception.getSuppressed().length);
        }
    }

    private static void demonstrateInterruptHandling() {

        System.out.println();
        System.out.println("--- INTERRUPT HANDLING ---");

        Thread worker = new Thread(
                        () -> {
                            try {
                                Q2_Retry.retry(() -> {
                                            throw new IllegalStateException("temporary");
                                        }, 5, 1_000,
                                        exception -> exception
                                                        instanceof IllegalStateException
                                );
                            } catch (InterruptedException exception) {
                                System.out.println("InterruptedException received.");
                                System.out.println("Interrupt flag restored: " + Thread.currentThread().isInterrupted());

                            } catch (Exception exception) {

                                System.out.println("Unexpected exception: " + exception.getClass().getSimpleName());
                            }
                        }
                );

        worker.start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        worker.interrupt();

        try {
            worker.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private static void demonstrateCircuitBreaker() {

        System.out.println();
        System.out.println("--- STRETCH: CIRCUIT BREAKER ---");

        Q2_CircuitBreaker breaker = new Q2_CircuitBreaker(2);

        AtomicInteger attempts = new AtomicInteger();

        for (int call = 1; call <= 3; call++) {
            try {
                Q2_Retry.retry(() -> {
                            attempts.incrementAndGet();
                            throw new IllegalStateException("temporary");
                        }, 1, 0, exception ->
                                exception instanceof IllegalStateException,
                        breaker
                );
            } catch (Exception exception) {
                System.out.println("Logical call " + call + " failed: " + exception.getClass().getSimpleName());
            }
        }

        System.out.println("Underlying attempts made: " + attempts.get());
        System.out.println("Circuit open: " + breaker.isOpen());
        System.out.println("The third logical call should be rejected " + "without executing the action.");
    }
}
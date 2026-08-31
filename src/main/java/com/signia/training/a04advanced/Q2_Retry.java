package com.signia.training.a04advanced;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * Generic retry utility.
 *
 * Q2 requirements:
 * - generic operation
 * - exponential backoff
 * - jitter
 * - retry predicate
 * - checked exceptions
 * - suppressed attempt history
 * - interrupt restoration
 * - safe logging
 *
 * Stretch:
 * - circuit breaker
 */
public final class Q2_Retry {

    private Q2_Retry() {
        /*
         * Utility class.
         */
    }

    /**
     * Required Q2 method.
     *
     * @param action operation to execute
     * @param maxAttempts maximum number of attempts
     * @param baseDelayMs base delay used for exponential backoff
     * @param isRetryable decides whether an exception may be retried
     * @param <T> result type
     * @return successful operation result
     * @throws Exception final failure
     */
    public static <T> T retry(
            Q2_ThrowingSupplier<T> action,
            int maxAttempts,
            long baseDelayMs,
            Predicate<Exception> isRetryable
    ) throws Exception {

        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(isRetryable, "isRetryable must not be null");
        validateArguments(maxAttempts, baseDelayMs);

        java.util.List<Exception> previousFailures = new java.util.ArrayList<>();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            long delayMs = calculateDelay(baseDelayMs, attempt);
            if (attempt > 1) {
                logAttempt(attempt, delayMs, previousFailures.getLast());
                sleepWithInterruptHandling(delayMs);
            }
            try {
                return action.get();
            } catch (Exception exception) {

                /*
                 * A non-retryable exception is immediately rethrown.
                 */
                if (!isRetryable.test(exception)) {
                    logNonRetryable(attempt, exception);
                    throw exception;
                }

                /*
                 * If this is the final attempt, attach all previous
                 * retry failures to the final exception.
                 */
                if (attempt == maxAttempts) {

                    for (Exception previousFailure : previousFailures) {
                        exception.addSuppressed(previousFailure);
                    }
                    throw exception;
                }
                previousFailures.add(
                        exception
                );
            }
        }

        throw new IllegalStateException("Retry loop terminated unexpectedly");
    }

    /**
     * Overload using Assignment 3 AppException retryability.
     *
     * Q2 explicitly asks us to reuse AppException.isRetryable()
     * as the default retry predicate. :contentReference[oaicite:3]{index=3}
     *
     * Because your A3 implementation uses Q4AppException,
     * this overload accepts that type.
     */
    public static <T> T retryAppExceptionAware(
            Q2_ThrowingSupplier<T> action,
            int maxAttempts,
            long baseDelayMs
    ) throws Exception {

        return retry(action, maxAttempts, baseDelayMs, Q2_Retry::isRetryableFromA3);
    }

    /**
     * Converts the A3 retryability rule into the Predicate
     * required by the generic retry method.
     *
     * This is isolated here so the retry algorithm itself
     * remains independent of the concrete A3 exception hierarchy.
     */
    private static boolean isRetryableFromA3(
            Exception exception) {

        /*
         * Assignment 3's Q4 hierarchy uses Q4AppException.
         *
         * If an A3 AppException is supplied, use its own
         * isRetryable() decision.
         */
        if (exception instanceof com.signia.training.a03oop.Q4AppException appException) {
            return appException.isRetryable();
        }

        /*
         * Unknown exceptions are not automatically considered
         * retryable.
         *
         * This prevents accidentally retrying validation/programming
         * errors.
         */
        return false;
    }

    /**
     * Void overload.
     *
     * It delegates to the generic T-returning implementation
     * rather than duplicating retry logic.
     */
    public static void retry(
            Q2_ThrowingRunnable action,
            int maxAttempts,
            long baseDelayMs,
            Predicate<Exception> isRetryable
    ) throws Exception {

        Objects.requireNonNull(action, "action must not be null");

        retry(() -> {
                    action.run();
                    return null;
                }, maxAttempts, baseDelayMs, isRetryable
        );
    }

    /**
     * Stretch overload with a circuit breaker.
     */
    public static <T> T retry(
            Q2_ThrowingSupplier<T> action,
            int maxAttempts,
            long baseDelayMs,
            Predicate<Exception> isRetryable,
            Q2_CircuitBreaker circuitBreaker
    ) throws Exception {

        Objects.requireNonNull(action, "action must not be null");

        Objects.requireNonNull(isRetryable, "isRetryable must not be null");

        Objects.requireNonNull(circuitBreaker, "circuitBreaker must not be null");

        /*
         * Circuit breaker is checked BEFORE attempting
         * the operation.
         */
        if (!circuitBreaker.allowRequest()) {

            throw new IllegalStateException("Circuit breaker is OPEN; " + "request rejected without execution");
        }

        try {
            T result = retry(action, maxAttempts, baseDelayMs, isRetryable);

            circuitBreaker.recordSuccess();

            return result;

        } catch (Exception exception) {

            /*
             * One retry() call represents one logical operation.
             *
             * If that logical operation ultimately fails,
             * count it as one circuit-breaker failure.
             */
            circuitBreaker.recordFailure();

            throw exception;
        }
    }

    /**
     * Calculate exponential backoff plus jitter.
     *
     * Attempt 2:
     *     base * 2^0
     *
     * Attempt 3:
     *     base * 2^1
     *
     * Attempt 4:
     *     base * 2^2
     *
     * etc.
     */
    static long calculateDelay(
            long baseDelayMs,
            int attempt) {

        if (attempt <= 1) {
            return 0;
        }

        int exponent = attempt - 2;

        /*
         * Protect against long overflow.
         */
        long exponentialDelay;

        if (exponent >= 62) {
            exponentialDelay = Long.MAX_VALUE;
        } else {
            long multiplier = 1L << exponent;
            if (baseDelayMs > Long.MAX_VALUE / multiplier) {
                exponentialDelay = Long.MAX_VALUE;

            } else {
                exponentialDelay = baseDelayMs * multiplier;
            }
        }

        /*
         * Jitter is a random fraction of the exponential delay.
         *
         * For example, a 100 ms delay might become
         * 100-199 ms.
         */
        long jitter;

        if (exponentialDelay == 0) {
            jitter = 0;
        } else if (exponentialDelay == Long.MAX_VALUE) {

            jitter = ThreadLocalRandom.current().nextLong(0, 1_000);

        } else {

            jitter = ThreadLocalRandom.current().nextLong(exponentialDelay);
        }

        if (Long.MAX_VALUE - exponentialDelay < jitter) {
            return Long.MAX_VALUE;
        }

        return exponentialDelay + jitter;
    }

    /**
     * Sleep while preserving the interrupt status.
     *
     * Thread.sleep() clears the interrupted flag when it throws
     * InterruptedException.
     *
     * We restore the flag before propagating the interruption so
     * higher-level code can still see that cancellation was requested.
     */
    private static void sleepWithInterruptHandling(
            long delayMs) throws InterruptedException {

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException exception) {

            /*
             * Restore the interrupt flag because sleep clears it
             * when InterruptedException is thrown.
             */
            Thread.currentThread().interrupt();
            throw exception;
        }
    }

    /**
     * Log retry information without logging exception messages.
     *
     * The exception message might contain PHI, credentials,
     * tokens, request payloads, or other sensitive information.
     */
    private static void logAttempt(
            int attempt, long delayMs, Exception exception) {

        System.out.println("Retry attempt " + attempt + " | delay=" + delayMs + " ms" + " | exception=" + exception.getClass().getSimpleName());
    }

    private static void logNonRetryable(
            int attempt, Exception exception) {

        System.out.println("Non-retryable failure" + " | attempt=" + attempt + " | exception=" + exception.getClass().getSimpleName());
    }

    /**
     * Adds earlier exceptions as suppressed exceptions to
     * the final exception.
     */


    private static void validateArguments(
            int maxAttempts,
            long baseDelayMs) {

        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be greater than zero");
        }

        if (baseDelayMs < 0) {
            throw new IllegalArgumentException("baseDelayMs must not be negative");
        }
    }
}
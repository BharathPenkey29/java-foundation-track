package com.signia.training.a04advanced;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Simple thread-safe circuit breaker.
 *
 * Stretch requirement:
 * after N consecutive failures across calls, fail fast
 * for the next 60 seconds without attempting the operation.
 *
 * After the open period expires, one trial call is permitted.
 */
public class Q2_CircuitBreaker {

    private static final Duration OPEN_DURATION =
            Duration.ofSeconds(60);

    private final int failureThreshold;

    private int consecutiveFailures;

    private Instant openedAt;

    private boolean halfOpenTrialInProgress;

    public Q2_CircuitBreaker(int failureThreshold) {

        if (failureThreshold <= 0) {
            throw new IllegalArgumentException("failureThreshold must be greater than zero");
        }

        this.failureThreshold = failureThreshold;
        this.consecutiveFailures = 0;
        this.openedAt = null;
        this.halfOpenTrialInProgress = false;
    }

    /**
     * Checks whether a call may proceed.
     *
     * This method is synchronized because the circuit breaker
     * is shared across calls.
     */
    public synchronized boolean allowRequest() {

        /*
         * CLOSED:
         * requests are allowed.
         */
        if (openedAt == null) {
            return true;
        }

        /*
         * OPEN:
         *
         * Keep failing fast until the 60-second window expires.
         */
        Duration elapsed = Duration.between(openedAt, Instant.now());

        if (elapsed.compareTo(OPEN_DURATION) < 0) {
            return false;
        }

        /*
         * HALF-OPEN:
         *
         * Permit exactly one trial request.
         */
        if (!halfOpenTrialInProgress) {
            halfOpenTrialInProgress = true;
            return true;
        }
        return false;
    }

    /**
     * Records a successful operation.
     *
     * Success closes the breaker and resets the failure count.
     */
    public synchronized void recordSuccess() {
        consecutiveFailures = 0;
        openedAt = null;
        halfOpenTrialInProgress = false;
    }

    /**
     * Records a failed operation.
     */
    public synchronized void recordFailure() {
        consecutiveFailures++;
        /*
         * If the threshold is reached, open the circuit.
         */
        if (consecutiveFailures >= failureThreshold) {
            openedAt = Instant.now();
            halfOpenTrialInProgress = false;
        }
    }

    /**
     * Returns whether the circuit is currently open.
     */
    public synchronized boolean isOpen() {
        if (openedAt == null) {
            return false;
        }
        Duration elapsed = Duration.between(openedAt, Instant.now());
        return elapsed.compareTo(OPEN_DURATION) < 0;
    }

    /**
     * Current consecutive failure count.
     */
    public synchronized int consecutiveFailures() {
        return consecutiveFailures;
    }

    /**
     * Returns the threshold configured for this breaker.
     */
    public int failureThreshold() {

        return failureThreshold;
    }
}
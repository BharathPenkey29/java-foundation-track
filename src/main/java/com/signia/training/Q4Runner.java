package com.signia.training;

public class Q4Runner {

    private final int maxAttempts;

    public Q4Runner(int maxAttempts) {

        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "maxAttempts must be >= 1"
            );
        }

        this.maxAttempts = maxAttempts;
    }

    /*
     * Q4 COMPILER-ERROR DEMONSTRATION
     *
     * Before Q4ThrottleException was handled below, the switch
     * contained only:
     *
     *     case Q4TransientException t
     *             when t.attempt() >= maxAttempts ->
     *             Q4Decision.DEAD_LETTER;
     *
     *     case Q4TransientException t ->
     *             Q4Decision.RETRY;
     *
     *     case Q4PermanentException p ->
     *             Q4Decision.DEAD_LETTER;
     *
     * After Q4ThrottleException was added to the sealed hierarchy,
     * the incomplete switch failed to compile because it was not
     * exhaustive.
     *
     * Representative compiler error:
     *
     *     "the switch expression does not cover all possible
     *      input values"
     *
     * The Q4ThrottleException case is now handled explicitly.
     */

    public Q4Decision route(Q4AppException failure) {

        if (failure == null) {
            throw new IllegalArgumentException(
                    "failure cannot be null"
            );
        }

        return switch (failure) {

            case Q4TransientException t
                    when t.attempt() >= maxAttempts ->
                    Q4Decision.DEAD_LETTER;

            case Q4TransientException t ->
                    Q4Decision.RETRY;

            case Q4PermanentException p ->
                    Q4Decision.DEAD_LETTER;

            case Q4ThrottleException t ->
                    Q4Decision.RETRY;
        };
    }

    public void handle(Q4AppException failure) {

        Q4Decision decision = route(failure);

        System.out.println(
                "Error code : " + failure.errorCode()
        );

        System.out.println(
                "Message    : " + failure.getMessage()
        );

        System.out.println(
                "Retryable  : " + failure.isRetryable()
        );

        System.out.println(
                "Decision   : " + decision
        );

        printCauseChain(failure);
    }

    private void printCauseChain(Throwable throwable) {

        System.out.println("Cause chain:");

        Throwable current = throwable;

        while (current != null) {

            System.out.println(
                    "  -> "
                            + current.getClass().getSimpleName()
                            + ": "
                            + current.getMessage()
            );

            current = current.getCause();
        }
    }
}
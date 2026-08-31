package com.signia.training.a04advanced;

/**
 * Immutable result of one simulated job.
 */
public record Q4_JobResult(
        int jobId,
        boolean success,
        String message,
        String threadName
) {

    public Q4_JobResult {
        if (jobId < 1) {
            throw new IllegalArgumentException(
                    "jobId must be positive"
            );
        }

        if (message == null) {
            throw new IllegalArgumentException(
                    "message must not be null"
            );
        }

        if (threadName == null) {
            throw new IllegalArgumentException(
                    "threadName must not be null"
            );
        }
    }
}
package com.signia.training.a04advanced;

import java.util.Objects;

/**
 * Represents one simulated automation job.
 */
public record Q4_Job(
        int jobId,
        long sleepMillis,
        boolean shouldFail
) {

    public Q4_Job {
        if (jobId < 1) {
            throw new IllegalArgumentException(
                    "jobId must be positive"
            );
        }

        if (sleepMillis < 100 || sleepMillis > 800) {
            throw new IllegalArgumentException(
                    "sleepMillis must be between 100 and 800"
            );
        }
    }
}
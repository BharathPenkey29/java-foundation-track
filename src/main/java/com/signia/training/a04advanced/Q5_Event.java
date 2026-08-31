package com.signia.training.a04advanced;

/**
 * One event from events.txt.
 *
 * Format:
 * epochMillis|jobId|status
 */
public record Q5_Event(
        long epochMillis,
        String jobId,
        String status
) {

    public Q5_Event {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("jobId must not be blank");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status must not be blank");
        }
    }

    public boolean isFailure() {
        return "FAILED".equals(status);
    }
}
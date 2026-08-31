package com.signia.training.a04advanced;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Represents a merged busy block for one provider.
 */
public record Q6_MergedInterval(
        LocalTime start,
        LocalTime end) {

    public Q6_MergedInterval {
        if (start == null || end == null) {
            throw new NullPointerException(
                    "Start and end cannot be null.");
        }

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Start must be before end.");
        }
    }

    public long durationMinutes() {
        return Duration.between(start, end).toMinutes();
    }

    /**
     * Half-open containment:
     *
     * start <= time < end
     */
    public boolean contains(LocalTime time) {
        return !time.isBefore(start)
                && time.isBefore(end);
    }

    @Override
    public String toString() {
        return start + "-" + end;
    }
}
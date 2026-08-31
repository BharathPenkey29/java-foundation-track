package com.signia.training.a04advanced;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Working hours used for free-time calculations.
 */
public record Q6_WorkingHours(
        LocalTime start,
        LocalTime end) {

    public Q6_WorkingHours {
        if (start == null || end == null) {
            throw new NullPointerException("Working hours cannot be null.");
        }

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Working-hours start must be before end.");
        }
    }

    public long totalMinutes() {
        return Duration.between(start, end).toMinutes();
    }
}
package com.signia.training.a04advanced;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents one provider visit interval.
 *
 * Intervals are half-open:
 *
 * [start, end)
 *
 * Therefore an interval ending at 10:15 does not overlap
 * another interval starting at 10:15.
 */
public record Q6_VisitInterval(
        String provider,
        LocalTime start,
        LocalTime end) {

    public Q6_VisitInterval {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");

        if (provider.isBlank()) {
            throw new IllegalArgumentException(
                    "Provider cannot be blank.");
        }

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Start must be before end.");
        }
    }

    /**
     * Returns the duration of this interval in minutes.
     */
    public long durationMinutes() {
        return Duration.between(start, end).toMinutes();
    }

    /**
     * Half-open interval containment.
     *
     * start <= time < end
     */
    public boolean contains(LocalTime time) {
        return !time.isBefore(start)
                && time.isBefore(end);
    }

    /**
     * Returns true only when two intervals actually overlap.
     *
     * Touching intervals are NOT overlapping.
     *
     * Example:
     *
     * [10:00, 10:15)
     * [10:15, 11:00)
     *
     * do not overlap.
     */
    public boolean overlaps(Q6_VisitInterval other) {

        return this.start.isBefore(other.end)
                && other.start.isBefore(this.end);
    }

    @Override
    public String toString() {
        return provider + " " + start + "-" + end;
    }
}
package com.signia.training.a04advanced;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable visit model used by Assignment 4 Q3.
 *
 * Q3 rebuilds the Assignment 2 Q1 report using Stream API.
 */
public record Q3_Visit(
        String visitId,
        String mrn,
        String provider,
        LocalDate date,
        int durationMins,
        String status
) {

    public Q3_Visit {

        Objects.requireNonNull(
                visitId,
                "visitId must not be null"
        );

        Objects.requireNonNull(
                mrn,
                "mrn must not be null"
        );

        Objects.requireNonNull(
                provider,
                "provider must not be null"
        );

        Objects.requireNonNull(
                date,
                "date must not be null"
        );

        Objects.requireNonNull(
                status,
                "status must not be null"
        );

        if (durationMins < 0) {

            throw new IllegalArgumentException(
                    "durationMins must not be negative"
            );
        }
    }
}
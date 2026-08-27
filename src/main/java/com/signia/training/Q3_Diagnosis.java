package com.signia.training;

/**
 * Small helper record used to demonstrate record-pattern
 * deconstruction.
 */
public record Q3_Diagnosis(
        String code,
        String description
) {

    public Q3_Diagnosis {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Diagnosis code must not be blank."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Diagnosis description must not be blank."
            );
        }
    }
}
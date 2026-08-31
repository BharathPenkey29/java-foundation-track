package com.signia.training.a04advanced;

/**
 * Simple patient entity used by the generic repository demo.
 *
 * This is deliberately a record because Q1 specifically asks
 * us to contrast records with Assignment 3 Q3.
 */
public record Q1_Patient(
        String mrn,
        String name
) implements Q1_Identifiable<String> {

    public Q1_Patient {
        if (mrn == null || mrn.isBlank()) {
            throw new IllegalArgumentException("mrn must not be null or blank");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
    }

    @Override
    public String id() {
        return mrn;
    }
}
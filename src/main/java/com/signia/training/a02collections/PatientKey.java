package com.signia.training.a02collections;

import java.time.LocalDate;

public record PatientKey(
        String mrn,
        LocalDate dateOfBirth
) {
}
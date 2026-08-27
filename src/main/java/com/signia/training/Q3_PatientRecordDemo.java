package com.signia.training;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Demonstrates the Q3 PatientRecord implementation.
 */
public class Q3_PatientRecordDemo {

    public static void main(String[] args) {

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Q3 - IMMUTABLE PATIENT RECORD"
        );

        System.out.println(
                "=========================================="
        );

        List<String> diagnoses =
                new ArrayList<>();

        diagnoses.add("I10");

        Q3_PatientRecord patient =
                new Q3_PatientRecord.Builder()
                        .mrn("MRN001")
                        .name("John Smith")
                        .dob(LocalDate.of(1990, 1, 15))
                        .diagnoses(diagnoses)
                        .build();

        System.out.println();
        System.out.println("Patient:");
        System.out.println(patient);

        System.out.println();
        System.out.println("Original diagnoses:");
        System.out.println(
                patient.getDiagnoses()
        );

        /*
         * Mutate the original list.
         */
        diagnoses.add("E11");

        System.out.println();
        System.out.println(
                "After mutating original list:"
        );

        System.out.println(
                patient.getDiagnoses()
        );

        /*
         * Equality demonstration.
         */
        Q3_PatientRecord samePatient =
                new Q3_PatientRecord.Builder()
                        .mrn("MRN001")
                        .name("Different Name")
                        .dob(LocalDate.of(2000, 1, 1))
                        .build();

        System.out.println();
        System.out.println(
                "Same MRN records equal: "
                        + patient.equals(samePatient)
        );

        /*
         * HashSet demonstration.
         */
        Set<Q3_PatientRecord> records =
                new HashSet<>();

        records.add(patient);
        records.add(samePatient);

        System.out.println(
                "HashSet size: "
                        + records.size()
        );

        /*
         * Record pattern demonstration.
         */
        Q3_Diagnosis diagnosis =
                new Q3_Diagnosis(
                        "I10",
                        "Essential hypertension"
                );

        System.out.println();
        System.out.println(
                "Record pattern:"
        );

        System.out.println(
                Q3_PatientRecord.describeDiagnosis(
                        diagnosis
                )
        );

        /*
         * Builder validation demonstration.
         */
        System.out.println();
        System.out.println(
                "Builder validation:"
        );

        try {

            new Q3_PatientRecord.Builder()
                    .build();

        } catch (IllegalStateException exception) {

            System.out.println(
                    exception.getMessage()
            );
        }
    }
}
package com.signia.training.a02collections;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

public class CompositeKeyReconciliation {

    public static void main(String[] args) {

        SequencedSet<PatientKey> yesterday =
                new LinkedHashSet<>();

        yesterday.add(
                new PatientKey(
                        "MRN00042",
                        LocalDate.of(1995, 4, 10)
                )
        );

        yesterday.add(
                new PatientKey(
                        "MRN00043",
                        LocalDate.of(1990, 8, 20)
                )
        );

        yesterday.add(
                new PatientKey(
                        "MRN00044",
                        LocalDate.of(1988, 12, 5)
                )
        );

        SequencedSet<PatientKey> today =
                new LinkedHashSet<>();

        today.add(
                new PatientKey(
                        "MRN00042",
                        LocalDate.of(1995, 4, 10)
                )
        );

        today.add(
                new PatientKey(
                        "MRN00043",
                        LocalDate.of(1991, 8, 20)
                )
        );

        today.add(
                new PatientKey(
                        "MRN00045",
                        LocalDate.of(2000, 2, 15)
                )
        );

        /*
         * Defensive copies are required because retainAll()
         * and removeAll() modify the collection.
         */

        SequencedSet<PatientKey> unchanged =
                new LinkedHashSet<>(yesterday);

        unchanged.retainAll(today);

        SequencedSet<PatientKey> removed =
                new LinkedHashSet<>(yesterday);

        removed.removeAll(today);

        SequencedSet<PatientKey> added =
                new LinkedHashSet<>(today);

        added.removeAll(yesterday);

        System.out.println("===== COMPOSITE KEY RECONCILIATION =====");

        System.out.println();
        System.out.println("ADDED: " + added.size());

        for (PatientKey key : added) {
            System.out.println(
                    "  " + key.mrn() +
                            " | DOB: " +
                            key.dateOfBirth()
            );
        }

        System.out.println();
        System.out.println("REMOVED: " + removed.size());

        for (PatientKey key : removed) {
            System.out.println(
                    "  " + key.mrn() +
                            " | DOB: " +
                            key.dateOfBirth()
            );
        }

        System.out.println();
        System.out.println("UNCHANGED: " + unchanged.size());

        for (PatientKey key : unchanged) {
            System.out.println(
                    "  " + key.mrn() +
                            " | DOB: " +
                            key.dateOfBirth()
            );
        }
    }
}
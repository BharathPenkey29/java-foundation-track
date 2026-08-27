package com.signia.training.a03oop;

import com.signia.training.Q3_Diagnosis;
import com.signia.training.Q3_PatientRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class Q3_PatientRecordTest {

    @Test
    void equalRecordsCollapseToOneHashSetEntry() {

        Q3_PatientRecord first =
                new Q3_PatientRecord.Builder()
                        .mrn("MRN001")
                        .name("John Smith")
                        .dob(LocalDate.of(1990, 1, 1))
                        .diagnoses(List.of("I10"))
                        .build();

        Q3_PatientRecord second =
                new Q3_PatientRecord.Builder()
                        .mrn("MRN001")
                        .name("Jane Doe")
                        .dob(LocalDate.of(2000, 5, 10))
                        .diagnoses(List.of("E11"))
                        .build();

        assertEquals(first, second);

        assertEquals(
                first.hashCode(),
                second.hashCode()
        );

        Set<Q3_PatientRecord> records =
                new HashSet<>();

        records.add(first);
        records.add(second);

        assertEquals(1, records.size());
    }

    @Test
    void diagnosesCannotMutateRecordFromOriginalList() {

        List<String> diagnoses =
                new ArrayList<>();

        diagnoses.add("I10");

        Q3_PatientRecord patient =
                new Q3_PatientRecord.Builder()
                        .mrn("MRN001")
                        .name("John Smith")
                        .dob(LocalDate.of(1990, 1, 1))
                        .diagnoses(diagnoses)
                        .build();

        /*
         * Mutate the original list after construction.
         */
        diagnoses.add("E11");

        /*
         * PatientRecord must remain unchanged.
         */
        assertEquals(
                List.of("I10"),
                patient.getDiagnoses()
        );
    }

    @Test
    void returnedDiagnosesListCannotMutateRecord() {

        Q3_PatientRecord patient =
                new Q3_PatientRecord.Builder()
                        .mrn("MRN001")
                        .diagnoses(
                                List.of("I10")
                        )
                        .build();

        List<String> returnedDiagnoses =
                patient.getDiagnoses();

        assertThrows(
                UnsupportedOperationException.class,
                () -> returnedDiagnoses.add("E11")
        );

        assertEquals(
                List.of("I10"),
                patient.getDiagnoses()
        );
    }

    @Test
    void builderRejectsMissingMrn() {

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> new Q3_PatientRecord.Builder()
                                .build()
                );

        assertTrue(
                exception.getMessage()
                        .contains("mrn")
        );
    }

    @Test
    void builderAcceptsOnlyRequiredMrn() {

        Q3_PatientRecord patient =
                new Q3_PatientRecord.Builder()
                        .mrn("MRN001")
                        .build();

        assertEquals(
                "MRN001",
                patient.getMrn()
        );

        assertNull(patient.getName());
        assertNull(patient.getDob());

        assertEquals(
                List.of(),
                patient.getDiagnoses()
        );
    }

    @Test
    void toStringMasksNameAndDob() {

        Q3_PatientRecord patient =
                new Q3_PatientRecord.Builder()
                        .mrn("MRN001")
                        .name("John Smith")
                        .dob(LocalDate.of(1990, 1, 15))
                        .build();

        String output =
                patient.toString();

        /*
         * Name and DOB should not appear in clear text.
         */
        assertFalse(
                output.contains("John Smith")
        );

        assertFalse(
                output.contains("1990-01-15")
        );

        /*
         * MRN is not PHI-masked by this requirement.
         */
        assertTrue(
                output.contains("MRN001")
        );
    }

    @Test
    void recordPatternDeconstructsDiagnosis() {

        Q3_Diagnosis diagnosis =
                new Q3_Diagnosis(
                        "I10",
                        "Essential hypertension"
                );

        String result =
                Q3_PatientRecord.describeDiagnosis(
                        diagnosis
                );

        assertEquals(
                "I10: Essential hypertension",
                result
        );
    }

    /**
     * Deliberately broken example.
     *
     * The mutable field participates in equals() and hashCode().
     * After inserting the object into a HashSet, changing the field
     * changes the hash code. The HashSet searches in the new bucket
     * and therefore cannot find the object in the old bucket.
     */
    @Test
    void brokenHashCodeDemonstration() {

        BrokenPatient patient =
                new BrokenPatient(
                        "MRN001",
                        "ACTIVE"
                );

        Set<BrokenPatient> patients =
                new HashSet<>();

        patients.add(patient);

        assertTrue(
                patients.contains(patient)
        );

        patient.setStatus("INACTIVE");

        /*
         * Deliberately broken behaviour.
         */
        assertFalse(
                patients.contains(patient)
        );
    }

    /**
     * Deliberately broken class used only for the HashSet
     * demonstration required by the assignment.
     */
    private static final class BrokenPatient {

        private final String mrn;
        private String status;

        private BrokenPatient(
                String mrn,
                String status) {

            this.mrn = mrn;
            this.status = status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public boolean equals(Object other) {

            if (this == other) {
                return true;
            }

            if (!(other instanceof BrokenPatient that)) {
                return false;
            }

            return mrn.equals(that.mrn)
                    && status.equals(that.status);
        }

        @Override
        public int hashCode() {

            /*
             * DELIBERATELY BROKEN:
             *
             * status participates in equals()
             * and hashCode().
             *
             * Mutating status after insertion into HashSet
             * changes the object's hash bucket.
             */
            return 31 * mrn.hashCode()
                    + status.hashCode();
        }
    }
}
package com.signia.training.a03oop;

import com.signia.training.a01basics.Identifiers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Immutable patient value object.
 *
 * All fields are final and there are no setters.
 *
 * Defensive copies are used for the mutable diagnoses list
 * both when the object is created and when the list is returned.
 */
public final class Q3_PatientRecord {

    private final String mrn;
    private final String name;
    private final LocalDate dob;
    private final List<String> diagnoses;

    /**
     * Private constructor.
     *
     * Objects must be created through the Builder.
     */
    private Q3_PatientRecord(
            String mrn,
            String name,
            LocalDate dob,
            List<String> diagnoses) {

        /*
         * MRN is the only required field.
         */
        if (mrn == null || mrn.isBlank()) {
            throw new IllegalArgumentException(
                    "mrn must not be null or blank."
            );
        }

        /*
         * Optional fields are allowed to be null.
         */
        this.mrn = mrn;
        this.name = name;
        this.dob = dob;

        /*
         * Defensive copy.
         *
         * List.copyOf() creates an immutable copy, so changes
         * to the Builder's list cannot corrupt this object.
         */
        this.diagnoses = diagnoses == null
                ? List.of()
                : List.copyOf(diagnoses);
    }

    public String getMrn() {
        return mrn;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDob() {
        return dob;
    }

    /**
     * Returns an immutable copy of the diagnoses list.
     *
     * The caller cannot mutate the internal state of this object.
     */
    public List<String> getDiagnoses() {
        return List.copyOf(diagnoses);
    }

    /**
     * Equality is based only on MRN.
     *
     * MRN represents the stable identity of the patient.
     * Name, DOB and diagnoses are descriptive information and
     * therefore should not change the identity of the patient.
     */
    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (!(other instanceof Q3_PatientRecord that)) {
            return false;
        }

        return mrn.equals(that.mrn);
    }

    /**
     * Must use exactly the same identity fields as equals().
     */
    @Override
    public int hashCode() {
        return mrn.hashCode();
    }

    /**
     * Masks PHI using the maskPhi() implementation from A1 Q3.
     *
     * Name and DOB are not displayed in clear text.
     */
    @Override
    public String toString() {

        String maskedName =
                name == null
                        ? "***"
                        : Identifiers.maskPhi(name);

        String maskedDob =
                dob == null
                        ? "***"
                        : Identifiers.maskPhi(
                        dob.toString()
                );

        return "Q3_PatientRecord{" +
                "mrn='" + mrn + '\'' +
                ", name='" + maskedName + '\'' +
                ", dob='" + maskedDob + '\'' +
                ", diagnoses=" + diagnoses +
                '}';
    }

    /**
     * Demonstrates Java record-pattern deconstruction.
     *
     * The Diagnosis record is deconstructed directly into
     * its code and description components.
     */
    public static String describeDiagnosis(Object value) {

        if (value instanceof Q3_Diagnosis(
                String code,
                String description)) {

            return code + ": " + description;
        }

        return "Not a diagnosis";
    }

    // ============================================================
    // Builder
    // ============================================================

    /**
     * Static nested Builder.
     *
     * Only MRN is required.
     * Name, DOB and diagnoses are optional.
     */
    public static final class Builder {

        private String mrn;
        private String name;
        private LocalDate dob;
        private List<String> diagnoses;

        public Builder mrn(String mrn) {

            this.mrn = mrn;

            return this;
        }

        public Builder name(String name) {

            this.name = name;

            return this;
        }

        public Builder dob(LocalDate dob) {

            this.dob = dob;

            return this;
        }

        /**
         * Makes a defensive copy immediately.
         */
        public Builder diagnoses(List<String> diagnoses) {

            this.diagnoses =
                    diagnoses == null
                            ? null
                            : new ArrayList<>(diagnoses);

            return this;
        }

        /**
         * Adds one diagnosis.
         */
        public Builder addDiagnosis(String diagnosis) {

            if (diagnosis == null || diagnosis.isBlank()) {
                throw new IllegalArgumentException(
                        "diagnosis must not be null or blank."
                );
            }

            if (diagnoses == null) {
                diagnoses = new ArrayList<>();
            }

            diagnoses.add(diagnosis);

            return this;
        }

        /**
         * Builds an immutable PatientRecord.
         *
         * Only MRN is required.
         *
         * The builder reports all missing required fields at once.
         */
        public Q3_PatientRecord build() {

            List<String> missingFields =
                    new ArrayList<>();

            if (mrn == null || mrn.isBlank()) {
                missingFields.add("mrn");
            }

            if (!missingFields.isEmpty()) {

                throw new IllegalStateException(
                        "Missing required fields: "
                                + String.join(
                                ", ",
                                missingFields
                        )
                );
            }

            /*
             * Validate constructor arguments before construction.
             */
            if (name != null && name.isBlank()) {
                throw new IllegalArgumentException(
                        "name must not be blank."
                );
            }

            if (diagnoses != null) {

                for (String diagnosis : diagnoses) {

                    if (diagnosis == null
                            || diagnosis.isBlank()) {

                        throw new IllegalArgumentException(
                                "diagnoses cannot contain "
                                        + "null or blank values."
                        );
                    }
                }
            }

            return new Q3_PatientRecord(
                    mrn,
                    name,
                    dob,
                    diagnoses
            );
        }
    }

    /*
     * ============================================================
     * Java record equivalent
     * ============================================================
     *
     * record Q3_PatientRecord(
     *         String mrn,
     *         String name,
     *         LocalDate dob,
     *         List<String> diagnoses
     * ) {}
     *
     * A Java record would give us several things automatically:
     *
     * 1. Constructor
     * 2. Accessors
     * 3. equals()
     * 4. hashCode()
     * 5. toString()
     *
     * However, it would NOT automatically give us the behaviour
     * required by this assignment.
     *
     * First:
     * A record would not automatically create a defensive copy
     * of the mutable diagnoses List. The List reference would be
     * final, but the List itself could still be mutable.
     *
     * Second:
     * A record's generated equals() and hashCode() use every
     * record component. This assignment requires identity based
     * on MRN alone.
     *
     * Therefore this class is deliberately written by hand.
     */
}
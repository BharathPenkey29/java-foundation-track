package com.signia.training.a01basics;

public class Patient {

    private final String mrn;
    private final String firstName;
    private final String lastName;
    private final String dob;
    private final String phone;
    private final String visitType;
    private final String provider;

    public Patient(
            String mrn,
            String firstName,
            String lastName,
            String dob,
            String phone,
            String visitType) {

        this(
                mrn,
                firstName,
                lastName,
                dob,
                phone,
                visitType,
                ""
        );
    }

    public Patient(
            String mrn,
            String firstName,
            String lastName,
            String dob,
            String phone,
            String visitType,
            String provider) {

        this.mrn = mrn;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.phone = phone;
        this.visitType = visitType;
        this.provider = provider;
    }

    public String getMrn() {
        return mrn;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDob() {
        return dob;
    }

    public String getPhone() {
        return phone;
    }

    public String getVisitType() {
        return visitType;
    }

    public String getProvider() {
        return provider;
    }
}
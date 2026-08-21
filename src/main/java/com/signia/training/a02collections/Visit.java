package com.signia.training.a02collections;

import java.time.LocalDate;

public class Visit implements Comparable<Visit> {

    private final String visitId;
    private final String mrn;
    private final String provider;
    private final LocalDate date;
    private final int durationMins;
    private final String status;

    public Visit(
            String visitId,
            String mrn,
            String provider,
            LocalDate date,
            int durationMins,
            String status) {

        this.visitId = visitId;
        this.mrn = mrn;
        this.provider = provider;
        this.date = date;
        this.durationMins = durationMins;
        this.status = status;
    }

    public String getVisitId() {
        return visitId;
    }

    public String getMrn() {
        return mrn;
    }

    public String getProvider() {
        return provider;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getDurationMins() {
        return durationMins;
    }

    public String getStatus() {
        return status;
    }

    /*
     * Natural ordering for Visit.
     *
     * Q3 will use this ordering with Collections.sort().
     */
    @Override
    public int compareTo(Visit other) {
        return this.visitId.compareTo(other.visitId);
    }

    @Override
    public String toString() {
        return "Visit{" +
                "visitId='" + visitId + '\'' +
                ", mrn='" + mrn + '\'' +
                ", provider='" + provider + '\'' +
                ", date=" + date +
                ", durationMins=" + durationMins +
                ", status='" + status + '\'' +
                '}';
    }
}
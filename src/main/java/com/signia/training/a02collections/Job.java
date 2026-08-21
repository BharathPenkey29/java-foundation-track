package com.signia.training.a02collections;

import java.time.LocalDateTime;

public class Job {

    private final String jobId;
    private int attempt;
    private final LocalDateTime submittedAt;
    private String lastError;

    public Job(
            String jobId,
            LocalDateTime submittedAt) {

        this.jobId = jobId;
        this.attempt = 0;
        this.submittedAt = submittedAt;
        this.lastError = null;
    }

    public String getJobId() {
        return jobId;
    }

    public int getAttempt() {
        return attempt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void incrementAttempt() {
        attempt++;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobId='" + jobId + '\'' +
                ", attempt=" + attempt +
                ", submittedAt=" + submittedAt +
                ", lastError='" + lastError + '\'' +
                '}';
    }
}
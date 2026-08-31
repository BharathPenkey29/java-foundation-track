package com.signia.training.a03oop;

import java.io.IOException;

public class Q4Session implements AutoCloseable {

    private final String sessionName;

    public Q4Session(String sessionName) {

        if (sessionName == null || sessionName.isBlank()) {
            throw new IllegalArgumentException(
                    "sessionName cannot be blank"
            );
        }

        this.sessionName = sessionName;
    }

    public void performOperation()
            throws IOException {

        throw new IOException(
                "Primary operation failed"
        );
    }

    @Override
    public void close()
            throws IOException {

        throw new IOException(
                "Session cleanup failed"
        );
    }

    public String getSessionName() {

        return sessionName;
    }
}
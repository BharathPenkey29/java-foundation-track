package com.signia.training.a03oop;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class Q4Step {

    public void timeout() throws IOException {

        throw new SocketTimeoutException(
                "Connection timed out while contacting remote service"
        );
    }

    public void connectionReset() throws IOException {

        throw new IOException(
                "Connection reset by remote service"
        );
    }

    public void validationFailure() throws IOException {

        throw new IOException(
                "Patient record validation failed"
        );
    }

    public void throttle() throws IOException {

        throw new IOException(
                "Remote service throttled the request"
        );
    }

    public void successfulOperation() {

        System.out.println(
                "Step completed successfully"
        );
    }
}
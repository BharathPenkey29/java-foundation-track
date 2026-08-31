package com.signia.training.a03oop;

import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class Q4_ExceptionRoutingTest {

    @Test
    void timeoutRoutesToRetry()
            throws Q4AppException {

        Q4Step step =
                new Q4Step();

        Q4Workflow workflow =
                new Q4Workflow(step);

        Q4Runner runner =
                new Q4Runner(3);

        try {

            workflow.executeTimeout(1);

            fail("Expected Q4AppException");

        } catch (Q4AppException e) {

            assertEquals(
                    Q4ErrorCode.TIMEOUT,
                    e.errorCode()
            );

            assertTrue(
                    e.isRetryable()
            );

            assertEquals(
                    Q4Decision.RETRY,
                    runner.route(e)
            );
        }
    }

    @Test
    void validationFailureRoutesToDeadLetter()
            throws Q4AppException {

        Q4Step step =
                new Q4Step();

        Q4Workflow workflow =
                new Q4Workflow(step);

        Q4Runner runner =
                new Q4Runner(3);

        try {

            workflow.executeValidationFailure();

            fail("Expected Q4AppException");

        } catch (Q4AppException e) {

            assertEquals(
                    Q4ErrorCode.VALIDATION_FAILED,
                    e.errorCode()
            );

            assertFalse(
                    e.isRetryable()
            );

            assertEquals(
                    Q4Decision.DEAD_LETTER,
                    runner.route(e)
            );
        }
    }

    @Test
    void causeChainSurvivesTranslation()
            throws Q4AppException {

        Q4Step step =
                new Q4Step();

        Q4Workflow workflow =
                new Q4Workflow(step);

        try {

            workflow.executeTimeout(1);

            fail("Expected Q4AppException");

        } catch (Q4AppException e) {

            assertNotNull(
                    e.getCause()
            );

            assertInstanceOf(
                    SocketTimeoutException.class,
                    e.getCause()
            );

            assertEquals(
                    "Connection timed out while contacting remote service",
                    e.getCause().getMessage()
            );
        }
    }

    @Test
    void timeoutAtMaximumAttemptsRoutesToDeadLetter()
            throws Q4AppException {

        Q4Step step =
                new Q4Step();

        Q4Workflow workflow =
                new Q4Workflow(step);

        Q4Runner runner =
                new Q4Runner(3);

        try {

            workflow.executeTimeout(3);

            fail("Expected Q4AppException");

        } catch (Q4AppException e) {

            assertEquals(
                    Q4Decision.DEAD_LETTER,
                    runner.route(e)
            );
        }
    }
}
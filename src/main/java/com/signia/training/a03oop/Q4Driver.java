package com.signia.training.a03oop;

import java.io.IOException;

public class Q4Driver {

    public static void main(String[] args) {

        Q4Step step = new Q4Step();

        Q4Workflow workflow =
                new Q4Workflow(step);

        Q4Runner runner =
                new Q4Runner(3);

        System.out.println(
                "===== Q4 EXCEPTION ROUTING ====="
        );

        runTimeout(workflow, runner);

        System.out.println();

        runValidationFailure(workflow, runner);

        System.out.println();

        runThrottle(workflow, runner);

        System.out.println();

        runTimeoutAfterMaximumAttempts(
                workflow,
                runner
        );

        System.out.println();

        demonstrateSuppressedException();
    }

    private static void runTimeout(
            Q4Workflow workflow,
            Q4Runner runner) {

        try {

            workflow.executeTimeout(1);

        } catch (Q4AppException e) {

            System.out.println(
                    "=== TIMEOUT ==="
            );

            runner.handle(e);
        }
    }

    private static void runValidationFailure(
            Q4Workflow workflow,
            Q4Runner runner) {

        try {

            workflow.executeValidationFailure();

        } catch (Q4AppException e) {

            System.out.println(
                    "=== VALIDATION FAILURE ==="
            );

            runner.handle(e);
        }
    }

    private static void runThrottle(
            Q4Workflow workflow,
            Q4Runner runner) {

        try {

            workflow.executeThrottle();

        } catch (Q4AppException e) {

            System.out.println(
                    "=== THROTTLE ==="
            );

            runner.handle(e);
        }
    }

    private static void runTimeoutAfterMaximumAttempts(
            Q4Workflow workflow,
            Q4Runner runner) {

        try {

            workflow.executeTimeout(3);

        } catch (Q4AppException e) {

            System.out.println(
                    "=== TIMEOUT AT MAX ATTEMPTS ==="
            );

            runner.handle(e);
        }
    }

    private static void demonstrateSuppressedException() {

        System.out.println(
                "=== SUPPRESSED EXCEPTION ==="
        );

        try (Q4Session session =
                     new Q4Session("patient-session")) {

            session.performOperation();

        } catch (IOException e) {

            System.out.println(
                    "Primary exception: "
                            + e.getMessage()
            );

            System.out.println(
                    "Suppressed exceptions:"
            );

            for (Throwable suppressed :
                    e.getSuppressed()) {

                System.out.println(
                        "  -> "
                                + suppressed
                                .getClass()
                                .getSimpleName()
                                + ": "
                                + suppressed.getMessage()
                );
            }
        }
    }
}
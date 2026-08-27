package com.signia.training;

import java.time.Duration;
import java.time.Instant;

/**
 * Base class for all portal workflows.
 *
 * Template Method Pattern:
 * The run() method owns the workflow sequence while subclasses
 * provide the implementation of individual steps.
 */
public abstract class Workflow {
    /*
     * Inheritance vs composition:
     *
     * Template Method uses inheritance to keep the workflow sequence fixed
     * while allowing subclasses to customize individual steps.
     *
     * Composition could instead inject separate Login, Navigation, Extraction,
     * and Logout strategies. Composition is generally more flexible when
     * behaviours need to be mixed and changed independently at runtime.
     *
     * For this problem, I would choose Template Method because the workflow
     * sequence itself is a fixed business rule and the subclasses only need
     * to customize the individual steps.
     */


    /**
     * Template method.
     *
     * The sequence of workflow steps is fixed here.
     * Subclasses cannot change the sequence because this method is final.
     */
    public final void run() {

        Throwable primaryFailure = null;

        try {
            executeStep("login", this::login);
            executeStep("navigate", this::navigate);
            executeStep("extract", this::extract);

        } catch (Throwable failure) {
            primaryFailure = failure;
            throw failure;

        } finally {
            /*
             * Cleanup must always happen, even when extract() fails.
             *
             * We do NOT simply call logout() here because logout itself
             * could fail and accidentally replace the original exception.
             */
            try {
                executeStep("logout", this::logout);

            } catch (Throwable cleanupFailure) {

                if (primaryFailure != null) {
                    /*
                     * The original failure is more important.
                     *
                     * Instead of replacing it with the cleanup failure,
                     * attach the cleanup failure as suppressed.
                     */
                    primaryFailure.addSuppressed(cleanupFailure);

                } else {
                    /*
                     * There was no earlier failure, so the logout failure
                     * becomes the failure of the workflow.
                     */
                    throw cleanupFailure;
                }
            }
        }
    }

    /**
     * Executes one workflow step and surrounds it with hooks.
     */
    private void executeStep(String stepName, Step step) {

        beforeStep(stepName);

        Instant start = Instant.now();

        try {
            step.execute();

        } finally {
            long durationMs =
                    Duration.between(start, Instant.now()).toMillis();

            afterStep(stepName, durationMs);
        }
    }

    /**
     * Login is different for every portal.
     */
    protected abstract void login();

    /**
     * Extraction is different for every portal.
     */
    protected abstract void extract();

    /**
     * Default navigation behaviour.
     *
     * Subclasses may override this when required.
     */
    protected void navigate() {
        System.out.println("Navigating to default page.");
    }

    /**
     * Default logout behaviour.
     *
     * One subclass overrides this as required by the assignment.
     */
    protected void logout() {
        System.out.println("Logging out from default portal.");
    }

    /**
     * Hook executed before every step.
     *
     * Subclasses can override this for logging.
     */
    protected void beforeStep(String stepName) {
        System.out.println("Starting step: " + stepName);
    }

    /**
     * Hook executed after every step.
     *
     * @param stepName  name of the completed step
     * @param durationMs duration of the step in milliseconds
     */
    protected void afterStep(String stepName, long durationMs) {
        System.out.println(
                "Finished step: " + stepName
                        + " (" + durationMs + " ms)"
        );
    }

    /**
     * Small functional interface used internally by executeStep().
     */
    @FunctionalInterface
    private interface Step {
        void execute();
    }
}
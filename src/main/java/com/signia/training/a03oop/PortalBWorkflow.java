package com.signia.training.a03oop;

/**
 * Workflow implementation for Portal B.
 */
public class PortalBWorkflow extends Workflow {

    @Override
    protected void login() {
        System.out.println("Portal B: Logging in with security token.");
    }

    @Override
    protected void navigate() {
        System.out.println("Portal B: Navigating to claims dashboard.");
    }

    @Override
    protected void extract() {
        System.out.println("Portal B: Starting extraction...");

        /*
         * Deliberately fail extraction.
         *
         * The assignment requires proving that logout()
         * still runs when extract() throws.
         */
        throw new IllegalStateException(
                "Portal B extraction failed."
        );
    }

    /**
     * Portal B has its own logout behaviour.
     *
     * This is the one subclass that overrides logout(),
     * as required by the assignment.
     */
    @Override
    protected void logout() {
        System.out.println("Portal B: Closing secure session.");
    }

    @Override
    protected void beforeStep(String stepName) {
        System.out.println("Portal B LOG: Starting " + stepName);
    }

    @Override
    protected void afterStep(String stepName, long durationMs) {
        System.out.println(
                "Portal B LOG: Completed "
                        + stepName
                        + " in "
                        + durationMs
                        + " ms"
        );
    }
}
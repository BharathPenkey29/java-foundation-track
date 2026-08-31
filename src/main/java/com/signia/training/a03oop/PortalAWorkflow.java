package com.signia.training.a03oop;

/**
 * Workflow implementation for Portal A.
 */
public class PortalAWorkflow extends Workflow {

    @Override
    protected void login() {
        System.out.println("Portal A: Logging in with username and password.");
    }

    @Override
    protected void navigate() {
        System.out.println("Portal A: Navigating to patient records.");
    }

    @Override
    protected void extract() {
        System.out.println("Portal A: Extracting patient records.");
    }

    @Override
    protected void beforeStep(String stepName) {
        System.out.println("Portal A LOG: Before " + stepName);
    }

    @Override
    protected void afterStep(String stepName, long durationMs) {
        System.out.println(
                "Portal A LOG: After "
                        + stepName
                        + " - "
                        + durationMs
                        + " ms"
        );
    }
}
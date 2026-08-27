package com.signia.training;

/**
 * Demonstrates the Workflow Template Method implementation.
 */

/**
 * C:\Users\penky\.jdks\openjdk-26.0.2\bin\java.exe "-javaagent:C:\Users\penky\AppData\Local\Programs\IntelliJ IDEA 2026.2.1\lib\idea_rt.jar=61067" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath C:\Users\penky\IdeaProjects\java-foundation-track\target\classes com.signia.training.a03oop.WorkflowDemo
 * =================================
 * Running Portal A
 * =================================
 * Portal A LOG: Before login
 * Portal A: Logging in with username and password.
 * Portal A LOG: After login - 0 ms
 * Portal A LOG: Before navigate
 * Portal A: Navigating to patient records.
 * Portal A LOG: After navigate - 0 ms
 * Portal A LOG: Before extract
 * Portal A: Extracting patient records.
 * Portal A LOG: After extract - 0 ms
 * Portal A LOG: Before logout
 * Logging out from default portal.
 * Portal A LOG: After logout - 0 ms
 *
 * =================================
 * Running Portal B
 * =================================
 * Portal B LOG: Starting login
 * Portal B: Logging in with security token.
 * Portal B LOG: Completed login in 0 ms
 * Portal B LOG: Starting navigate
 * Portal B: Navigating to claims dashboard.
 * Portal B LOG: Completed navigate in 0 ms
 * Portal B LOG: Starting extract
 * Portal B: Starting extraction...
 * Portal B LOG: Completed extract in 0 ms
 * Portal B LOG: Starting logout
 * Portal B: Closing secure session.
 * Portal B LOG: Completed logout in 0 ms
 * Workflow failed: Portal B extraction failed.
 * Suppressed exceptions: 0
 *
 * Process finished with exit code 0
 */
public class WorkflowDemo {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println("Running Portal A");
        System.out.println("=================================");

        Workflow portalA = new PortalAWorkflow();

        portalA.run();

        System.out.println();

        System.out.println("=================================");
        System.out.println("Running Portal B");
        System.out.println("=================================");

        Workflow portalB = new PortalBWorkflow();

        try {
            portalB.run();

        } catch (RuntimeException exception) {
            System.out.println(
                    "Workflow failed: " + exception.getMessage()
            );

            System.out.println(
                    "Suppressed exceptions: "
                            + exception.getSuppressed().length
            );
        }
    }
}
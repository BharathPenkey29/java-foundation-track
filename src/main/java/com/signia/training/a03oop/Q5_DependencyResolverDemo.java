package com.signia.training.a03oop;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Q5_DependencyResolverDemo {

    private static final String RESOURCE_DIR =
            "src/main/resources/a03/";

    public static void main(String[] args) {

        Q5_DependencyResolver resolver =
                new Q5_DependencyResolver();

        System.out.println("==========================================");
        System.out.println("Q5 - STEP DEPENDENCY RESOLVER");
        System.out.println("==========================================");

        runHappyPath(
                resolver,
                Path.of(
                        RESOURCE_DIR
                                + "workflow-steps.txt"
                )
        );

        runCycleTest(
                resolver,
                Path.of(
                        RESOURCE_DIR
                                + "workflow-steps-cyclic.txt"
                )
        );

        runMissingDependencyTest(
                resolver,
                Path.of(
                        RESOURCE_DIR
                                + "workflow-steps-missing-dep.txt"
                )
        );
    }

    private static void runHappyPath(
            Q5_DependencyResolver resolver,
            Path path) {

        System.out.println();
        System.out.println("File: " + path);

        Map<String, Set<String>> dependencies =
                resolver.readSteps(path);

        System.out.println();
        System.out.println("Topological order:");

        List<String> order =
                resolver.topologicalOrder(
                        dependencies
                );

        System.out.println(
                String.join(
                        " -> ",
                        order
                )
        );

        System.out.println();
        System.out.println("Parallel waves:");

        List<List<String>> waves =
                resolver.parallelWaves(
                        dependencies
                );

        for (int i = 0; i < waves.size(); i++) {

            System.out.println(
                    "Wave " + i + ": "
                            + waves.get(i)
            );
        }

        System.out.println();
        System.out.println(
                "Critical path length: "
                        + resolver.criticalPathLength(
                        dependencies
                )
        );
    }

    private static void runCycleTest(
            Q5_DependencyResolver resolver,
            Path path) {

        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println("Cycle test");
        System.out.println("------------------------------------------");

        try {

            Map<String, Set<String>> dependencies =
                    resolver.readSteps(path);

            resolver.topologicalOrder(
                    dependencies
            );

            System.out.println(
                    "ERROR: Expected a cycle."
            );

        } catch (IllegalStateException exception) {

            System.out.println(
                    exception.getMessage()
            );
        }
    }

    private static void runMissingDependencyTest(
            Q5_DependencyResolver resolver,
            Path path) {

        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println("Missing dependency test");
        System.out.println("------------------------------------------");

        try {

            resolver.readSteps(path);

            System.out.println(
                    "ERROR: Expected missing dependency."
            );

        } catch (IllegalArgumentException exception) {

            System.out.println(
                    exception.getMessage()
            );
        }
    }
}
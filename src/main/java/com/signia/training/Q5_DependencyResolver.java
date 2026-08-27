package com.signia.training;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;

public class Q5_DependencyResolver {

    /**
     * Reads a workflow definition from a file.
     *
     * Format:
     *
     * login ->
     * fetchRoster -> login
     * fetchVisits -> login
     * mergeReport -> fetchRoster, fetchVisits
     *
     * Blank lines and lines beginning with # are ignored.
     */
    public Map<String, Set<String>> readSteps(Path path) {

        Map<String, Set<String>> dependencies =
                new TreeMap<>();

        try {
            List<String> lines =
                    Files.readAllLines(
                            path,
                            StandardCharsets.UTF_8
                    );

            for (String line : lines) {

                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                parseLine(line, dependencies);
            }

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Unable to read workflow file: " + path,
                    exception
            );
        }

        validateDependencies(dependencies);

        return dependencies;
    }

    /**
     * Parses one step declaration.
     */
    private void parseLine(
            String line,
            Map<String, Set<String>> dependencies) {

        String[] parts = line.split("->", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid workflow line: " + line
            );
        }

        String step = parts[0].trim();

        if (step.isEmpty()) {
            throw new IllegalArgumentException(
                    "Step name cannot be empty: " + line
            );
        }

        Set<String> stepDependencies =
                dependencies.computeIfAbsent(
                        step,
                        ignored -> new TreeSet<>()
                );

        String dependencyPart = parts[1].trim();

        if (!dependencyPart.isEmpty()) {

            String[] dependencyNames =
                    dependencyPart.split(",");

            for (String dependency : dependencyNames) {

                dependency = dependency.trim();

                if (!dependency.isEmpty()) {
                    stepDependencies.add(dependency);
                }
            }
        }
    }

    /**
     * Makes sure every dependency was declared as a step.
     */
    private void validateDependencies(
            Map<String, Set<String>> dependencies) {

        for (Map.Entry<String, Set<String>> entry
                : dependencies.entrySet()) {

            String step = entry.getKey();

            for (String dependency : entry.getValue()) {

                if (!dependencies.containsKey(dependency)) {

                    throw new IllegalArgumentException(
                            "Step '" + step
                                    + "' depends on undeclared step '"
                                    + dependency
                                    + "'"
                    );
                }
            }
        }
    }

    /**
     * Kahn's algorithm.
     *
     * Returns a deterministic topological order.
     */
    public List<String> topologicalOrder(
            Map<String, Set<String>> dependencies) {

        Map<String, Integer> inDegree =
                new HashMap<>();

        Map<String, Set<String>> dependents =
                buildDependents(dependencies);

        for (String step : dependencies.keySet()) {

            inDegree.put(
                    step,
                    dependencies.get(step).size()
            );
        }

        /*
         * TreeSet gives alphabetical ordering.
         */
        Deque<String> ready =
                new ArrayDeque<>(
                        new TreeSet<>()
                );

        for (String step : dependencies.keySet()) {

            if (inDegree.get(step) == 0) {
                ready.addLast(step);
            }
        }

        List<String> order =
                new ArrayList<>();

        while (!ready.isEmpty()) {

            String step = ready.removeFirst();

            order.add(step);

            for (String dependent :
                    dependents.get(step)) {

                int newDegree =
                        inDegree.get(dependent) - 1;

                inDegree.put(
                        dependent,
                        newDegree
                );

                if (newDegree == 0) {

                    /*
                     * Rebuild ready in sorted order so ties
                     * remain deterministic.
                     */
                    ready.addLast(dependent);

                    List<String> sorted =
                            new ArrayList<>(ready);

                    Collections.sort(sorted);

                    ready.clear();
                    ready.addAll(sorted);
                }
            }
        }

        if (order.size() != dependencies.size()) {

            List<String> cycle =
                    findCycle(dependencies);

            throw new IllegalStateException(
                    "Cycle detected: "
                            + String.join(
                            " -> ",
                            cycle
                    )
            );
        }

        return order;
    }

    /**
     * Builds:
     *
     * dependency -> steps that depend on it
     */
    private Map<String, Set<String>> buildDependents(
            Map<String, Set<String>> dependencies) {

        Map<String, Set<String>> dependents =
                new HashMap<>();

        for (String step : dependencies.keySet()) {

            dependents.put(
                    step,
                    new TreeSet<>()
            );
        }

        for (Map.Entry<String, Set<String>> entry
                : dependencies.entrySet()) {

            String step = entry.getKey();

            for (String dependency :
                    entry.getValue()) {

                dependents
                        .get(dependency)
                        .add(step);
            }
        }

        return dependents;
    }

    /**
     * Produces parallel execution waves.
     *
     * Wave 0 = steps with no dependencies.
     * Wave 1 = steps whose dependencies are complete in wave 0.
     * etc.
     */
    public List<List<String>> parallelWaves(
            Map<String, Set<String>> dependencies) {

        Map<String, Integer> inDegree =
                new HashMap<>();

        Map<String, Set<String>> dependents =
                buildDependents(dependencies);

        for (String step : dependencies.keySet()) {

            inDegree.put(
                    step,
                    dependencies.get(step).size()
            );
        }

        List<List<String>> waves =
                new ArrayList<>();

        Set<String> currentWave =
                new TreeSet<>();

        for (String step : dependencies.keySet()) {

            if (inDegree.get(step) == 0) {
                currentWave.add(step);
            }
        }

        int processed = 0;

        while (!currentWave.isEmpty()) {

            List<String> wave =
                    new ArrayList<>(currentWave);

            waves.add(wave);

            processed += wave.size();

            Set<String> nextWave =
                    new TreeSet<>();

            for (String step : wave) {

                for (String dependent :
                        dependents.get(step)) {

                    int newDegree =
                            inDegree.get(dependent) - 1;

                    inDegree.put(
                            dependent,
                            newDegree
                    );

                    if (newDegree == 0) {
                        nextWave.add(dependent);
                    }
                }
            }

            currentWave = nextWave;
        }

        if (processed != dependencies.size()) {

            List<String> cycle =
                    findCycle(dependencies);

            throw new IllegalStateException(
                    "Cycle detected: "
                            + String.join(
                            " -> ",
                            cycle
                    )
            );
        }

        return waves;
    }

    /**
     * Critical path length in this assignment is
     * the number of parallel waves.
     */
    public int criticalPathLength(
            Map<String, Set<String>> dependencies) {

        return parallelWaves(dependencies).size();
    }

    /**
     * Finds actual cycle names using DFS.
     */
    private List<String> findCycle(
            Map<String, Set<String>> dependencies) {

        Set<String> visiting =
                new HashSet<>();

        Set<String> visited =
                new HashSet<>();

        List<String> path =
                new ArrayList<>();

        for (String step : dependencies.keySet()) {

            List<String> cycle =
                    findCycleDfs(
                            step,
                            dependencies,
                            visiting,
                            visited,
                            path
                    );

            if (cycle != null) {
                return cycle;
            }
        }

        return List.of("unknown");
    }

    private List<String> findCycleDfs(
            String step,
            Map<String, Set<String>> dependencies,
            Set<String> visiting,
            Set<String> visited,
            List<String> path) {

        if (visiting.contains(step)) {

            int start =
                    path.indexOf(step);

            List<String> cycle =
                    new ArrayList<>(
                            path.subList(
                                    start,
                                    path.size()
                            )
                    );

            cycle.add(step);

            return cycle;
        }

        if (visited.contains(step)) {
            return null;
        }

        visiting.add(step);
        path.add(step);

        for (String dependency :
                dependencies.get(step)) {

            List<String> cycle =
                    findCycleDfs(
                            dependency,
                            dependencies,
                            visiting,
                            visited,
                            path
                    );

            if (cycle != null) {
                return cycle;
            }
        }

        path.remove(path.size() - 1);
        visiting.remove(step);
        visited.add(step);

        return null;
    }
}
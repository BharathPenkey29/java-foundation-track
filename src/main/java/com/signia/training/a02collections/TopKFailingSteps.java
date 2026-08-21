package com.signia.training.a02collections;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class TopKFailingSteps {

    private static final Path LOG_FILE =
            Path.of("src/main/resources/generated/run-log-1m.txt");

    private static final int K = 3;

    public static void main(String[] args) throws IOException {

        System.out.println("==========================================");
        System.out.println("        Q6 - TOP-K FAILING STEPS");
        System.out.println("==========================================");

        /*
         * ========================================================
         * HEAP APPROACH
         * ========================================================
         *
         * The complete heap approach is timed:
         *
         * 1. Stream the 1M-line file.
         * 2. Count FAILED records.
         * 3. Select top K using a min-heap.
         *
         * Counting is O(n).
         * Heap selection is O(s log k),
         * where s = number of distinct failing steps.
         *
         * Overall: O(n + s log k).
         */
        long heapStart = System.nanoTime();

        Map<String, Integer> heapCounts =
                countFailures(LOG_FILE);

        List<Map.Entry<String, Integer>> heapResult =
                topKUsingHeap(heapCounts, K);

        long heapEnd = System.nanoTime();

        /*
         * ========================================================
         * SORTING APPROACH
         * ========================================================
         *
         * This independently streams the same 1M-line file.
         *
         * 1. Stream the file.
         * 2. Count FAILED records.
         * 3. Sort every distinct failing step.
         *
         * Counting is O(n).
         * Sorting is O(s log s).
         *
         * Overall: O(n + s log s).
         */
        long sortStart = System.nanoTime();

        Map<String, Integer> sortCounts =
                countFailures(LOG_FILE);

        List<Map.Entry<String, Integer>> sortResult =
                topKUsingSorting(sortCounts, K);

        long sortEnd = System.nanoTime();

        /*
         * ========================================================
         * OVERALL RESULT
         * ========================================================
         */
        System.out.println();
        System.out.println(
                "===== TOP " + K + " FAILING STEPS ====="
        );

        printResults(heapResult);

        /*
         * ========================================================
         * SORTING VERIFICATION
         * ========================================================
         */
        System.out.println();
        System.out.println(
                "===== SORTING VERIFICATION ====="
        );

        boolean identical =
                heapResult.equals(sortResult);

        System.out.println(
                "Heap and sorting outputs identical: "
                        + identical
        );

        /*
         * ========================================================
         * TIMING
         * ========================================================
         */
        System.out.println();
        System.out.println("===== TIMING =====");

        System.out.printf(
                "Heap approach: %.3f ms%n",
                (heapEnd - heapStart) / 1_000_000.0
        );

        System.out.printf(
                "Sorting approach: %.3f ms%n",
                (sortEnd - sortStart) / 1_000_000.0
        );

        /*
         * ========================================================
         * K LARGER THAN DISTINCT STEPS
         * ========================================================
         */
        System.out.println();
        System.out.println(
                "===== K LARGER THAN DISTINCT STEPS ====="
        );

        List<Map.Entry<String, Integer>> largeK =
                topKUsingHeap(heapCounts, 100);

        System.out.println(
                "Requested K: 100"
        );

        System.out.println(
                "Returned: "
                        + largeK.size()
                        + " distinct failing steps"
        );

        /*
         * ========================================================
         * STRETCH
         * TOP K PER DAY
         * ========================================================
         */
        System.out.println();
        System.out.println(
                "===== STRETCH - TOP K PER DAY ====="
        );

        Map<String, Map<String, Integer>> dailyFailures =
                countFailuresByDay(LOG_FILE);

        /*
         * Sort dates so the report is deterministic.
         */
        List<String> dates =
                new ArrayList<>(dailyFailures.keySet());

        dates.sort(String::compareTo);

        for (String date : dates) {

            System.out.println();
            System.out.println(
                    "Date: " + date
            );

            List<Map.Entry<String, Integer>> dailyTopK =
                    topKUsingHeap(
                            dailyFailures.get(date),
                            K
                    );

            printResults(dailyTopK);
        }

        /*
         * ========================================================
         * STRETCH
         * MORE THAN DOUBLED
         * ========================================================
         */
        System.out.println();
        System.out.println(
                "===== STRETCH - MORE THAN DOUBLED ====="
        );

        printMoreThanDoubled(
                dailyFailures,
                dates
        );
    }

    /*
     * ============================================================
     * COUNT FAILURES
     * ============================================================
     *
     * The file is streamed line by line.
     *
     * We do NOT load one million lines into a List.
     *
     * Time: O(n)
     * Space: O(s)
     *
     * s = number of distinct failing steps.
     */
    private static Map<String, Integer> countFailures(
            Path file) throws IOException {

        Map<String, Integer> counts =
                new HashMap<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(file)) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] parts =
                        line.split("\\|");

                if (parts.length != 4) {
                    continue;
                }

                String stepName = parts[2];
                String status = parts[3];

                if ("FAILED".equals(status)) {

                    counts.merge(
                            stepName,
                            1,
                            Integer::sum
                    );
                }
            }
        }

        return counts;
    }

    /*
     * ============================================================
     * TOP K USING MIN-HEAP
     * ============================================================
     *
     * The heap contains at most K candidates.
     *
     * The weakest candidate is kept at the root.
     *
     * Therefore, when a better candidate appears, we remove
     * the root and insert the better candidate.
     *
     * Selection complexity:
     * O(s log k)
     *
     * With the O(n) counting pass:
     * O(n + s log k)
     *
     * This satisfies the required O(n log k) upper-bound style
     * for the top-K operation.
     */
    private static List<Map.Entry<String, Integer>> topKUsingHeap(
            Map<String, Integer> counts,
            int k) {

        if (k <= 0 || counts.isEmpty()) {
            return new ArrayList<>();
        }

        /*
         * MIN-HEAP:
         *
         * Smaller count = weaker candidate.
         *
         * For equal counts, alphabetically later names are
         * considered weaker so that the alphabetically earlier
         * name wins the final top-K tie-break.
         */
        Comparator<Map.Entry<String, Integer>> weakestFirst =
                (a, b) -> {

                    int countComparison =
                            Integer.compare(
                                    a.getValue(),
                                    b.getValue()
                            );

                    if (countComparison != 0) {
                        return countComparison;
                    }

                    return b.getKey()
                            .compareTo(a.getKey());
                };

        PriorityQueue<Map.Entry<String, Integer>> heap =
                new PriorityQueue<>(weakestFirst);

        for (Map.Entry<String, Integer> entry
                : counts.entrySet()) {

            if (heap.size() < k) {

                heap.offer(entry);

            } else if (weakestFirst.compare(
                    entry,
                    heap.peek()) > 0) {

                heap.poll();
                heap.offer(entry);
            }
        }

        List<Map.Entry<String, Integer>> result =
                new ArrayList<>(heap);

        /*
         * Heap order is not the final report order.
         *
         * Sort only the K retained entries.
         *
         * K is small, so this is O(k log k).
         */
        result.sort(bestFirst());

        return result;
    }

    /*
     * ============================================================
     * SORT EVERYTHING
     * ============================================================
     *
     * Time:
     * O(s log s)
     *
     * Space:
     * O(s)
     */
    private static List<Map.Entry<String, Integer>> topKUsingSorting(
            Map<String, Integer> counts,
            int k) {

        List<Map.Entry<String, Integer>> all =
                new ArrayList<>(counts.entrySet());

        all.sort(bestFirst());

        if (k >= all.size()) {
            return all;
        }

        return new ArrayList<>(
                all.subList(0, k)
        );
    }

    /*
     * Final ordering:
     *
     * 1. Failure count descending.
     * 2. Step name alphabetically.
     */
    private static Comparator<Map.Entry<String, Integer>>
    bestFirst() {

        return (a, b) -> {

            int countComparison =
                    Integer.compare(
                            b.getValue(),
                            a.getValue()
                    );

            if (countComparison != 0) {
                return countComparison;
            }

            return a.getKey()
                    .compareTo(b.getKey());
        };
    }

    /*
     * ============================================================
     * DAILY FAILURE COUNTS
     * ============================================================
     *
     * Outer map:
     * date -> step counts
     *
     * Time: O(n)
     */
    private static Map<String, Map<String, Integer>>
    countFailuresByDay(Path file) throws IOException {

        Map<String, Map<String, Integer>> daily =
                new HashMap<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(file)) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] parts =
                        line.split("\\|");

                if (parts.length != 4) {
                    continue;
                }

                String timestamp = parts[0];
                String stepName = parts[2];
                String status = parts[3];

                if (!"FAILED".equals(status)) {
                    continue;
                }

                String date =
                        timestamp.substring(0, 10);

                Map<String, Integer> stepCounts =
                        daily.computeIfAbsent(
                                date,
                                ignored -> new HashMap<>()
                        );

                stepCounts.merge(
                        stepName,
                        1,
                        Integer::sum
                );
            }
        }

        return daily;
    }

    /*
     * ============================================================
     * MORE THAN DOUBLED
     * ============================================================
     *
     * A step is flagged when:
     *
     * today's count > yesterday's count * 2
     *
     * If the step did not fail yesterday, its previous count
     * is treated as zero.
     */
    private static void printMoreThanDoubled(
            Map<String, Map<String, Integer>> dailyFailures,
            List<String> dates) {

        boolean found = false;

        for (int i = 1; i < dates.size(); i++) {

            String previousDate =
                    dates.get(i - 1);

            String currentDate =
                    dates.get(i);

            Map<String, Integer> previous =
                    dailyFailures.get(previousDate);

            Map<String, Integer> current =
                    dailyFailures.get(currentDate);

            for (Map.Entry<String, Integer> entry
                    : current.entrySet()) {

                String step =
                        entry.getKey();

                int currentCount =
                        entry.getValue();

                int previousCount =
                        previous.getOrDefault(
                                step,
                                0
                        );

                if (currentCount >
                        previousCount * 2) {

                    found = true;

                    System.out.println(
                            currentDate
                                    + " | "
                                    + step
                                    + " | previous="
                                    + previousCount
                                    + " | current="
                                    + currentCount
                    );
                }
            }
        }

        if (!found) {

            System.out.println(
                    "No steps more than doubled."
            );
        }
    }

    private static void printResults(
            List<Map.Entry<String, Integer>> results) {

        int rank = 1;

        for (Map.Entry<String, Integer> entry
                : results) {

            System.out.println(
                    rank
                            + ". "
                            + entry.getKey()
                            + " -> "
                            + entry.getValue()
            );

            rank++;
        }
    }
}
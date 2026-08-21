package com.signia.training.a02collections;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupAndSummarizeVisits {

    private static final Path VISITS_FILE =
            Path.of("src/main/resources/a02/visits.csv");

    private static final Path PROVIDERS_FILE =
            Path.of("src/main/resources/a02/providers.txt");

    public static void main(String[] args) throws IOException {

        List<Visit> visits = loadVisits();

        /*
         * Map choice:
         *
         * LinkedHashMap preserves provider insertion order.
         * This makes the grouped data deterministic and easier
         * to inspect while debugging.
         */
        Map<String, List<Visit>> visitsByProvider =
                new LinkedHashMap<>();

        /*
         * Map choice:
         *
         * HashMap is appropriate because we only need fast
         * lookup and update operations for total minutes.
         * No ordering is required here because the entries
         * are sorted later for the final report.
         */
        Map<String, Integer> totalMinutesByProvider =
                new HashMap<>();

        /*
         * Map choice:
         *
         * LinkedHashMap preserves provider insertion order.
         * The inner LinkedHashMap also preserves the order in
         * which statuses first appear for each provider.
         */
        Map<String, Map<String, Integer>> statusCountsByProvider =
                new LinkedHashMap<>();

        /*
         * Build all three summaries in one pass over the visits.
         */
        for (Visit visit : visits) {

            String provider = visit.getProvider();

            /*
             * computeIfAbsent:
             *
             * Creates a new list only when this provider has
             * not been seen before.
             */
            visitsByProvider
                    .computeIfAbsent(
                            provider,
                            key -> new ArrayList<>()
                    )
                    .add(visit);

            /*
             * merge:
             *
             * First visit for a provider -> duration becomes
             * the initial value.
             *
             * Later visits -> durations are added together.
             */
            totalMinutesByProvider.merge(
                    provider,
                    visit.getDurationMins(),
                    Integer::sum
            );

            /*
             * Get or create the status-count map for this provider.
             */
            Map<String, Integer> statusCounts =
                    statusCountsByProvider
                            .computeIfAbsent(
                                    provider,
                                    key -> new LinkedHashMap<>()
                            );

            /*
             * getOrDefault:
             *
             * If this status has not appeared before, use 0.
             * Otherwise use the current count.
             */
            int currentCount =
                    statusCounts.getOrDefault(
                            visit.getStatus(),
                            0
                    );

            statusCounts.put(
                    visit.getStatus(),
                    currentCount + 1
            );
        }

        /*
         * Add providers from the complete roster.
         *
         * A group-by operation can only discover providers that
         * actually appear in visits.csv.
         *
         * providers.txt contains the complete roster, so it allows
         * us to represent providers with zero visits.
         */
        addZeroVisitProviders(
                visitsByProvider,
                totalMinutesByProvider,
                statusCountsByProvider
        );

        printReport(
                visitsByProvider,
                totalMinutesByProvider,
                statusCountsByProvider
        );
    }

    /**
     * Loads visits.csv into Visit objects.
     */
    private static List<Visit> loadVisits() throws IOException {

        List<Visit> visits = new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(VISITS_FILE)) {

            // Skip CSV header.
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(",");

                String visitId = parts[0].trim();
                String mrn = parts[1].trim();
                String provider = parts[2].trim();

                LocalDate date =
                        LocalDate.parse(parts[3].trim());

                int durationMins =
                        Integer.parseInt(parts[4].trim());

                String status = parts[5].trim();

                visits.add(
                        new Visit(
                                visitId,
                                mrn,
                                provider,
                                date,
                                durationMins,
                                status
                        )
                );
            }
        }

        return visits;
    }

    /**
     * Adds providers that exist in the complete provider roster
     * but have no visits.
     */
    private static void addZeroVisitProviders(
            Map<String, List<Visit>> visitsByProvider,
            Map<String, Integer> totalMinutesByProvider,
            Map<String, Map<String, Integer>> statusCountsByProvider)
            throws IOException {

        try (BufferedReader reader =
                     Files.newBufferedReader(PROVIDERS_FILE)) {

            String line;

            while ((line = reader.readLine()) != null) {

                /*
                 * Ignore blank lines and comment lines.
                 *
                 * providers.txt contains documentation comments
                 * beginning with '#', not provider names.
                 */
                if (line.isBlank() ||
                        line.trim().startsWith("#")) {
                    continue;
                }

                String provider = line.trim();

                /*
                 * If the provider already has visits, keep the
                 * existing list.
                 *
                 * Otherwise create an empty list.
                 */
                visitsByProvider.computeIfAbsent(
                        provider,
                        key -> new ArrayList<>()
                );

                /*
                 * A provider with no visits has zero total minutes.
                 *
                 * putIfAbsent prevents us from replacing an
                 * existing calculated total.
                 */
                totalMinutesByProvider.putIfAbsent(
                        provider,
                        0
                );

                /*
                 * A provider with no visits has no status counts.
                 */
                statusCountsByProvider.computeIfAbsent(
                        provider,
                        key -> new LinkedHashMap<>()
                );
            }
        }
    }

    /**
     * Prints providers ordered by:
     *
     * 1. Total minutes descending
     * 2. Provider name ascending when totals are equal
     */
    private static void printReport(
            Map<String, List<Visit>> visitsByProvider,
            Map<String, Integer> totalMinutesByProvider,
            Map<String, Map<String, Integer>> statusCountsByProvider) {

        System.out.println("==========================================");
        System.out.println("       CLINICAL VISIT SUMMARY");
        System.out.println("==========================================");

        /*
         * Create a separate list of map entries before sorting.
         *
         * We do not want to modify the HashMap itself.
         */
        List<Map.Entry<String, Integer>> sortedProviders =
                new ArrayList<>(
                        totalMinutesByProvider.entrySet()
                );

        /*
         * Sort:
         *
         * 1. Total minutes descending
         * 2. Provider name ascending for ties
         */
        sortedProviders.sort(
                Comparator
                        .comparing(
                                Map.Entry<String, Integer>::getValue,
                                Comparator.reverseOrder()
                        )
                        .thenComparing(
                                Map.Entry::getKey
                        )
        );

        /*
         * entrySet() gives us both key and value directly.
         *
         * This avoids:
         *
         * for (String provider : map.keySet()) {
         *     map.get(provider);
         * }
         *
         * keySet() gives only the key, so another map lookup
         * would be required to retrieve its value.
         *
         * entrySet() gives the key-value pair directly.
         */
        for (Map.Entry<String, Integer> entry :
                sortedProviders) {

            String provider = entry.getKey();
            int totalMinutes = entry.getValue();

            List<Visit> providerVisits =
                    visitsByProvider.get(provider);

            Map<String, Integer> statusCounts =
                    statusCountsByProvider.get(provider);

            System.out.println();
            System.out.println("Provider: " + provider);

            System.out.println(
                    "Total visits: " +
                            providerVisits.size()
            );

            System.out.println(
                    "Total minutes: " +
                            totalMinutes
            );

            System.out.println("Status counts:");

            if (statusCounts.isEmpty()) {

                System.out.println("  No visits");

            } else {

                /*
                 * Iterate with entrySet() instead of keySet().
                 */
                for (Map.Entry<String, Integer> statusEntry :
                        statusCounts.entrySet()) {

                    System.out.println(
                            "  " +
                                    statusEntry.getKey() +
                                    ": " +
                                    statusEntry.getValue()
                    );
                }
            }
        }
    }
}
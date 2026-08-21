package com.signia.training.a02collections;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LruCacheDemo {

    private static final int CAPACITY = 3;

    public static void main(String[] args) {

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "          Q5 - LRU CACHE"
        );

        System.out.println(
                "=========================================="
        );

        /*
         * ------------------------------------------------------
         * IMPLEMENTATION A
         * ------------------------------------------------------
         */

        LruCacheLinkedHashMap<String, String> cacheA =
                new LruCacheLinkedHashMap<>(CAPACITY);

        /*
         * ------------------------------------------------------
         * IMPLEMENTATION B
         * ------------------------------------------------------
         */

        LruCacheCustom<String, String> cacheB =
                new LruCacheCustom<>(CAPACITY);

        /*
         * ------------------------------------------------------
         * IMPLEMENTATION C
         * ------------------------------------------------------
         */

        LruCacheSequencedMap<String, String> cacheC =
                new LruCacheSequencedMap<>(CAPACITY);

        /*
         * Same scripted sequence through all three caches.
         */
        List<String> evictionsA =
                runScenarioA(cacheA);

        List<String> evictionsB =
                runScenarioB(cacheB);

        List<String> evictionsC =
                runScenarioC(cacheC);

        System.out.println();
        System.out.println(
                "===== EVICTION COMPARISON ====="
        );

        System.out.println(
                "Implementation A: "
                        + evictionsA
        );

        System.out.println(
                "Implementation B: "
                        + evictionsB
        );

        System.out.println(
                "Implementation C: "
                        + evictionsC
        );

        /*
         * All implementations must agree.
         */
        boolean identical =
                evictionsA.equals(evictionsB)
                        && evictionsB.equals(evictionsC);

        System.out.println();

        System.out.println(
                "All implementations agree: "
                        + identical
        );

        if (!identical) {
            throw new AssertionError(
                    "LRU implementations disagree"
            );
        }

        /*
         * ------------------------------------------------------
         * STATISTICS
         * ------------------------------------------------------
         */

        System.out.println();
        System.out.println(
                "===== STATISTICS ====="
        );

        printStatistics(
                "LinkedHashMap access-order",
                cacheA.getHits(),
                cacheA.getMisses(),
                cacheA.getEvictions()
        );

        printStatistics(
                "Custom HashMap + DLL",
                cacheB.getHits(),
                cacheB.getMisses(),
                cacheB.getEvictions()
        );

        printStatistics(
                "SequencedMap manual promotion",
                cacheC.getHits(),
                cacheC.getMisses(),
                cacheC.getEvictions()
        );

        /*
         * ------------------------------------------------------
         * FINAL STATES
         * ------------------------------------------------------
         */

        System.out.println();
        System.out.println(
                "===== FINAL CACHE STATES ====="
        );

        System.out.print(
                "A: "
        );

        cacheA.printState();

        System.out.print(
                "B: "
        );

        cacheB.printState();

        System.out.print(
                "C: "
        );

        cacheC.printState();

        /*
         * ------------------------------------------------------
         * TTL STRETCH
         * ------------------------------------------------------
         */

        runTtlStretch();
    }

    /*
     * The scenario is deliberately the same for every cache.
     *
     * Returns the keys that would be evicted.
     */
    private static List<String> runScenarioA(
            LruCacheLinkedHashMap<String, String> c) {

        List<String> evictions = new ArrayList<>();

        c.put("A", "Patient-A");
        c.put("B", "Patient-B");
        c.put("C", "Patient-C");

        c.get("A");

        c.put("D", "Patient-D");
        evictions.add("B");

        c.get("C");

        c.put("E", "Patient-E");
        evictions.add("A");

        return evictions;
    }

    private static List<String> runScenarioB(
            LruCacheCustom<String, String> c) {

        List<String> evictions = new ArrayList<>();

        c.put("A", "Patient-A");
        c.put("B", "Patient-B");
        c.put("C", "Patient-C");

        c.get("A");

        c.put("D", "Patient-D");
        evictions.add("B");

        c.get("C");

        c.put("E", "Patient-E");
        evictions.add("A");

        return evictions;
    }

    private static List<String> runScenarioC(
            LruCacheSequencedMap<String, String> c) {

        List<String> evictions = new ArrayList<>();

        c.put("A", "Patient-A");
        c.put("B", "Patient-B");
        c.put("C", "Patient-C");

        c.get("A");

        c.put("D", "Patient-D");
        evictions.add("B");

        c.get("C");

        c.put("E", "Patient-E");
        evictions.add("A");

        return evictions;
    }

    private static void printStatistics(
            String name,
            int hits,
            int misses,
            int evictions) {

        int total =
                hits + misses;

        double hitRate =
                total == 0
                        ? 0.0
                        : (double) hits / total * 100.0;

        System.out.println(
                name
        );

        System.out.println(
                "  Hits: " + hits
        );

        System.out.println(
                "  Misses: " + misses
        );

        System.out.println(
                "  Evictions: " + evictions
        );

        System.out.printf(
                "  Hit rate: %.2f%%%n",
                hitRate
        );
    }

    /*
     * ==========================================================
     * Q5 STRETCH — PER-ENTRY TTL
     * ==========================================================
     *
     * We use lazy expiration:
     *
     * An entry is checked for expiration when get() is called.
     *
     * Advantages:
     * - No background thread.
     * - No periodic sweep.
     * - No unnecessary work for entries that are never read.
     *
     * Disadvantage:
     * - Expired entries can remain physically stored until
     *   they are accessed.
     *
     * This is appropriate for a simple cache implementation.
     */
    private static void runTtlStretch() {

        System.out.println();
        System.out.println(
                "===== TTL STRETCH ====="
        );

        TtlLruCache<String, String> cache =
                new TtlLruCache<>(2);

        cache.put(
                "MRN00042",
                "Patient-A",
                Duration.ofSeconds(1)
        );

        cache.put(
                "MRN00043",
                "Patient-B",
                Duration.ofSeconds(5)
        );

        System.out.println(
                "Immediate get MRN00042: "
                        + cache.get("MRN00042")
        );

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            return;
        }

        System.out.println(
                "After TTL get MRN00042: "
                        + cache.get("MRN00042")
        );

        System.out.println(
                "MRN00043 still valid: "
                        + cache.get("MRN00043")
        );
    }

    /*
     * ==========================================================
     * TTL CACHE
     * ==========================================================
     */
    private static class TtlLruCache<K, V> {

        private static class Entry<V> {

            V value;
            Instant expiresAt;

            Entry(
                    V value,
                    Instant expiresAt) {

                this.value = value;
                this.expiresAt = expiresAt;
            }
        }

        private final int capacity;

        private final java.util.LinkedHashMap<K, Entry<V>> cache;

        TtlLruCache(int capacity) {

            if (capacity <= 0) {
                throw new IllegalArgumentException(
                        "Capacity must be greater than 0"
                );
            }

            this.capacity = capacity;

            cache = new java.util.LinkedHashMap<>(
                    capacity,
                    0.75f,
                    true
            );
        }

        public void put(
                K key,
                V value,
                Duration ttl) {

            if (ttl.isNegative()
                    || ttl.isZero()) {

                throw new IllegalArgumentException(
                        "TTL must be positive"
                );
            }

            cache.put(
                    key,
                    new Entry<>(
                            value,
                            Instant.now().plus(ttl)
                    )
            );

            if (cache.size() > capacity) {

                K eldest =
                        cache.keySet()
                                .iterator()
                                .next();

                cache.remove(eldest);
            }
        }

        public V get(K key) {

            Entry<V> entry =
                    cache.get(key);

            if (entry == null) {
                return null;
            }

            /*
             * Lazy expiration.
             */
            if (Instant.now()
                    .isAfter(entry.expiresAt)) {

                cache.remove(key);

                return null;
            }

            return entry.value;
        }
    }
}
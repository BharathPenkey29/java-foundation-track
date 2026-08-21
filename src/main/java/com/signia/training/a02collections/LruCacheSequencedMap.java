package com.signia.training.a02collections;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCacheSequencedMap<K, V> {

    private final int capacity;

    private int hits;
    private int misses;
    private int evictions;

    /*
     * insertion-order LinkedHashMap.
     *
     * We manually control recency using Java 21
     * SequencedMap methods.
     */
    private final LinkedHashMap<K, V> cache;

    public LruCacheSequencedMap(int capacity) {

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than 0"
            );
        }

        this.capacity = capacity;

        cache = new LinkedHashMap<>();
    }

    /*
     * Average O(1) for HashMap lookup.
     *
     * putLast() moves an existing key to the end,
     * representing most-recent use.
     */
    public V get(K key) {

        V value = cache.get(key);

        if (value == null) {

            misses++;

            return null;
        }

        hits++;

        /*
         * Manual promotion:
         *
         * remove old position
         * then put at the end
         */
        cache.remove(key);
        cache.putLast(key, value);

        return value;
    }

    /*
     * Average O(1).
     */
    public void put(K key, V value) {

        /*
         * If key already exists, remove it first so that
         * putLast() makes it most-recent.
         */
        cache.remove(key);

        cache.putLast(key, value);

        if (cache.size() > capacity) {

            /*
             * First entry is the least recently used.
             */
            Map.Entry<K, V> removed =
                    cache.pollFirstEntry();

            if (removed != null) {
                evictions++;
            }
        }
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }

    public int getEvictions() {
        return evictions;
    }

    public int size() {
        return cache.size();
    }

    public void printState() {
        System.out.println(cache);
    }
}
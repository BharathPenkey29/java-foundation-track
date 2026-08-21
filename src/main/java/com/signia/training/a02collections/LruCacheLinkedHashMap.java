package com.signia.training.a02collections;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCacheLinkedHashMap<K, V> {

    private final int capacity;

    private int hits;
    private int misses;
    private int evictions;

    /*
     * accessOrder = true means that successful get()
     * operations move entries to the most-recent position.
     *
     * get()  -> O(1) average
     * put()  -> O(1) average
     */
    private final LinkedHashMap<K, V> cache;

    public LruCacheLinkedHashMap(int capacity) {

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than 0"
            );
        }

        this.capacity = capacity;

        cache = new LinkedHashMap<>(
                capacity,
                0.75f,
                true
        ) {
            @Override
            protected boolean removeEldestEntry(
                    Map.Entry<K, V> eldest) {

                boolean remove =
                        size() > LruCacheLinkedHashMap.this.capacity;

                if (remove) {
                    evictions++;
                }

                return remove;
            }
        };
    }

    public V get(K key) {

        V value = cache.get(key);

        if (value != null) {
            hits++;
        } else {
            misses++;
        }

        return value;
    }

    public void put(K key, V value) {
        cache.put(key, value);
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
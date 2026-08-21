package com.signia.training.a02collections;

import java.util.HashMap;
import java.util.Map;

public class LruCacheCustom<K, V> {

    /*
     * Node represents one cache entry.
     *
     * prev and next allow O(1) unlinking and insertion.
     */
    private static class Node<K, V> {

        K key;
        V value;

        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;

    private int hits;
    private int misses;
    private int evictions;

    /*
     * HashMap provides O(1) average lookup.
     */
    private final Map<K, Node<K, V>> map;

    /*
     * Sentinel nodes eliminate null checks during
     * insertion and removal.
     */
    private final Node<K, V> head;
    private final Node<K, V> tail;

    public LruCacheCustom(int capacity) {

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than 0"
            );
        }

        this.capacity = capacity;

        map = new HashMap<>();

        head = new Node<>(null, null);
        tail = new Node<>(null, null);

        head.next = tail;
        tail.prev = head;
    }

    /*
     * O(1) average time.
     */
    public V get(K key) {

        Node<K, V> node = map.get(key);

        if (node == null) {
            misses++;
            return null;
        }

        hits++;

        /*
         * Existing key was used, therefore it becomes
         * the most-recent entry.
         */
        remove(node);
        addToTail(node);

        return node.value;
    }

    /*
     * O(1) average time.
     */
    public void put(K key, V value) {

        Node<K, V> existing = map.get(key);

        if (existing != null) {

            existing.value = value;

            /*
             * Updating an existing entry is also a use.
             */
            remove(existing);
            addToTail(existing);

            return;
        }

        Node<K, V> node =
                new Node<>(key, value);

        map.put(key, node);

        addToTail(node);

        if (map.size() > capacity) {

            /*
             * First real node is least recently used.
             */
            Node<K, V> leastRecent =
                    head.next;

            remove(leastRecent);

            map.remove(leastRecent.key);

            evictions++;
        }
    }

    /*
     * Remove node from its current position.
     *
     * O(1)
     */
    private void remove(Node<K, V> node) {

        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /*
     * Add node immediately before tail.
     *
     * O(1)
     */
    private void addToTail(Node<K, V> node) {

        node.prev = tail.prev;
        node.next = tail;

        tail.prev.next = node;
        tail.prev = node;
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
        return map.size();
    }

    public void printState() {

        Node<K, V> current =
                head.next;

        System.out.print("[ ");

        while (current != tail) {

            System.out.print(
                    current.key
                            + "="
                            + current.value
            );

            current = current.next;

            if (current != tail) {
                System.out.print(", ");
            }
        }

        System.out.println(" ]");
    }
}
package com.signia.training.a04advanced;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic repository implementation backed by ConcurrentHashMap.
 *
 * @param <T> entity type
 * @param <ID> identifier type
 */
public class Q1_MapRepository<
        T extends Q1_Identifiable<ID>,
        ID>
        implements Q1_Repository<T, ID> {

    private final ConcurrentHashMap<ID, T> storage =
            new ConcurrentHashMap<>();

    @Override
    public Optional<T> findById(ID id) {

        Objects.requireNonNull(id, "id must not be null");

        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {

        /*
         * Return an immutable snapshot.
         *
         * The caller does not receive direct access to the
         * repository's internal map.
         */
        return List.copyOf(storage.values());
    }

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        ID id = Objects.requireNonNull(entity.id(), "entity id must not be null");
        storage.put(id, entity);
        return entity;
    }

    @Override
    public boolean deleteById(ID id) {
        Objects.requireNonNull(id, "id must not be null");
        return storage.remove(id) != null;
    }

    /**
     * PECS — Producer Extends.
     *
     * List<? extends Number> is a producer of Number values.
     *
     * We can safely READ Number values from the list.
     *
     * We cannot safely add a Number because the actual list
     * might be List<Integer>, List<Double>, etc.
     */
    public static double sumNumbers(
            List<? extends Number> numbers) {
        Objects.requireNonNull(numbers, "numbers must not be null");
        double total = 0.0;
        for (Number number : numbers) {

            total += number.doubleValue();
        }
        return total;
    }

    /**
     * PECS — Consumer Super.
     *
     * List<? super Integer> is a consumer of Integer values.
     *
     * It is safe to ADD Integer values.
     *
     * We cannot safely read a specific Integer because the
     * actual list might be List<Number> or List<Object>.
     */
    public static void addIntegers(
            List<? super Integer> numbers) {
        Objects.requireNonNull(numbers, "numbers must not be null");
        numbers.add(10);
        numbers.add(20);
        numbers.add(30);
    }

    /**
     * Returns the current number of stored entities.
     */
    public int size() {
        return storage.size();
    }
}
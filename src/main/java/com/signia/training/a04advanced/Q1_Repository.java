package com.signia.training.a04advanced;
import java.util.List;
import java.util.Optional;

/**
 * Generic repository abstraction.
 *
 * @param <T> entity type
 * @param <ID> identifier type
 */
public interface Q1_Repository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    boolean deleteById(ID id);
}
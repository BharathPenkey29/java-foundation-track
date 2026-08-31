package com.signia.training.a04advanced;

/**
 * Functional interface for operations that return a value
 * and may throw a checked exception.
 *
 * java.util.function.Supplier cannot be used here because
 * Supplier.get() does not declare checked exceptions.
 */
@FunctionalInterface
public interface Q2_ThrowingSupplier<T> {

    T get() throws Exception;
}
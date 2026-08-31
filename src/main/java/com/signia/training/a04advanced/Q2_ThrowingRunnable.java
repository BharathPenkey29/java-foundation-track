package com.signia.training.a04advanced;

/**
 * Functional interface for operations that return no value
 * and may throw a checked exception.
 */
@FunctionalInterface
public interface Q2_ThrowingRunnable {

    void run() throws Exception;
}
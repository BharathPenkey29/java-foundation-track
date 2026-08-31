package com.signia.training.a04advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Generic Result type for representing either success or failure.
 *
 * The sealed interface has exactly two implementations:
 * Ok and Fail.
 *
 * This makes it structurally impossible for one Result instance
 * to contain both a value and an error.
 */
public sealed interface Q1_Result<T>
        permits Q1_Result.Ok, Q1_Result.Fail {

    /**
     * Successful result.
     *
     * Records provide the constructor, accessor, equals(),
     * hashCode(), and toString() automatically.
     */
    record Ok<T>(T value) implements Q1_Result<T> {

        public Ok {
            Objects.requireNonNull(value, "value must not be null");
        }
    }

    /**
     * Failed result.
     *
     * A failure contains an error code and message.
     */
    record Fail<T>(
            String code,
            String message
    ) implements Q1_Result<T> {

        public Fail {

            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("code must not be null or blank");
            }

            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("message must not be null or blank");
            }
        }
    }

    /**
     * Creates a successful Result.
     */
    static <T> Q1_Result<T> ok(T value) {
        return new Ok<>(value);
    }

    /**
     * Creates a failed Result.
     */
    static <T> Q1_Result<T> fail(
            String code,
            String message) {
        return new Fail<>(code, message);
    }

    /**
     * Transforms the successful value.
     *
     * The switch is exhaustive because Q1_Result is sealed.
     * There is deliberately no default branch.
     *
     * The failure must be rebuilt as Fail<R>.
     *
     * We cannot cast Fail<T> to Fail<R> because generic type
     * parameters are invariant and the cast would be unsafe.
     * Rebuilding preserves the error information while safely
     * changing the generic result type.
     */
    default <R> Q1_Result<R> map(
            Function<T, R> fn) {
        Objects.requireNonNull(fn, "fn must not be null");

        return switch (this) {
            case Ok<T>(T value) -> Q1_Result.ok(fn.apply(value));

            case Fail<T> failure ->
                    new Q1_Result.Fail<>(failure.code(), failure.message());
        };
    }

    /**
     * Transforms a successful value into another Result.
     *
     * flatMap is used when the next operation can itself fail.
     */
    default <R> Q1_Result<R> flatMap(
            Function<T, Q1_Result<R>> fn) {

        Objects.requireNonNull(fn, "fn must not be null");

        return switch (this) {
            case Ok<T>(T value) ->
                    Objects.requireNonNull(fn.apply(value), "flatMap function returned null");
            case Fail<T> failure ->
                    new Q1_Result.Fail<>(failure.code(), failure.message());
        };
    }

    /**
     * Returns the contained value if successful.
     *
     * Otherwise returns the supplied fallback.
     *
     * Again, the switch is exhaustive and contains no default.
     */
    default T orElse(T fallback) {

        return switch (this) {
            case Ok<T>(T value) -> value;
            case Fail<T> ignored -> fallback;
        };
    }

    /*
     * Executes the consumer only for successful results.
     */
    default void ifOk(
            Consumer<T> consumer) {

        Objects.requireNonNull(consumer, "consumer must not be null");

        if (this instanceof Ok<T>(T value)) {
            consumer.accept(value);
        }
    }

    /**
     * Returns true when this is an Ok result.
     */
    default boolean isOk() {
        return this instanceof Ok<T>;
    }

    /**
     * Stretch goal:
     *
     * Combines multiple Results.
     *
     * Returns Ok only when every result is Ok.
     *
     * If one or more results are failures, all error codes
     * are collected into a single failure.
     *
     * The order of the codes follows the input list.
     */
    static <T> Q1_Result<List<T>> combine(
            List<Q1_Result<T>> results) {

        Objects.requireNonNull(results, "results must not be null");

        List<T> values = new ArrayList<>(results.size());

        List<String> errorCodes = new ArrayList<>();

        for (Q1_Result<T> result : results) {

            Objects.requireNonNull(result, "result list cannot contain null");

            switch (result) {

                case Ok<T>(T value) -> values.add(value);

                case Fail<T> failure -> errorCodes.add(failure.code());
            }
        }

        if (!errorCodes.isEmpty()) {
            return Q1_Result.fail(String.join(",", errorCodes), "One or more operations failed");
        }

        return Q1_Result.ok(List.copyOf(values));
    }

    /*
     * IMPORTANT:
     *
     * Result is immutable and cannot contain both a value and an error.
     *
     * The sealed-interface + record design makes that structurally
     * impossible because the only permitted states are Ok or Fail.
     */
}
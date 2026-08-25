/**
 * Assignment 4 - Generics, Streams and Concurrency.
 * <p>
 * Suggested classes, one per question:
 * <ul>
 *   <li>Q1 {@code Result}, {@code Repository}, {@code MapRepository} - generic types and wildcards</li>
 *   <li>Q2 {@code Retry}, {@code ThrowingSupplier} - reusable backoff, any operation</li>
 *   <li>Q3 {@code StreamVisitSummary}   - Assignment 2 Q1 again, in streams, byte-identical output</li>
 *   <li>Q4 {@code ConcurrentJobRunner}  - executors, futures, and a deliberate lost-update race</li>
 *   <li>Q5 {@code BurstDetector}        - sliding time window over an event stream</li>
 *   <li>Q6 {@code IntervalMerger}       - merge overlapping intervals, then binary search them</li>
 * </ul>
 * Q1 builds {@code Result} as a {@code sealed interface} over records; Q4 adds virtual threads
 * ({@code Executors.newVirtualThreadPerTaskExecutor}) alongside the platform-thread pool. Preview
 * APIs - String Templates, Structured Concurrency, Scoped Values - are out of scope.
 * <p>
 * JUnit 5 tests are required for Q6. They belong in
 * {@code src/test/java/com/signia/training/a04advanced}.
 *
 * @see <a href="../../../../../../assignments/04-advanced/README.md">Assignment 4 brief</a>
 */
package com.signia.training.a04advanced;

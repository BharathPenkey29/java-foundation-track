/**
 * Assignment 3 - Objects, Abstraction and Failure.
 * <p>
 * Suggested classes, one per question:
 * <ul>
 *   <li>Q1 {@code Workflow} and subclasses - template method with guaranteed cleanup</li>
 *   <li>Q2 {@code DataExtractor} and friends - interface, implementations, factory</li>
 *   <li>Q3 {@code PatientRecord}         - immutable value object with a builder</li>
 *   <li>Q4 {@code AppException} hierarchy - retryable versus permanent, three layers</li>
 *   <li>Q5 {@code DependencyResolver}    - topological order and parallel waves</li>
 *   <li>Q6 {@code ConfigValidator}, {@code EditSession} - stacks, and the command pattern</li>
 * </ul>
 * Q4 is where Java 21 earns its place: a {@code sealed} exception hierarchy routed by an
 * exhaustive {@code switch} pattern with no {@code default} branch, so the compiler reports any
 * error type nobody handled.
 * <p>
 * JUnit 5 tests are required for Q3 and Q4 at minimum. They belong in
 * {@code src/test/java/com/signia/training/a03oop}.
 *
 * @see <a href="../../../../../../assignments/03-oop/README.md">Assignment 3 brief</a>
 */
package com.signia.training.a03oop;

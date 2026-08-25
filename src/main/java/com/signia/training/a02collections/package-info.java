/**
 * Assignment 2 - Collections.
 * <p>
 * Suggested classes, one per question:
 * <ul>
 *   <li>Q1 {@code VisitSummary}       - group and aggregate with maps and loops</li>
 *   <li>Q2 {@code DropReconciler}     - set algebra across two daily roster files</li>
 *   <li>Q3 {@code VisitSorting}       - multi-key comparators and TreeMap range queries</li>
 *   <li>Q4 {@code RetryQueue}         - ArrayDeque work queue, dead-letter list, safe removal</li>
 *   <li>Q5 {@code LruCache}           - bounded O(1) cache, built two different ways</li>
 *   <li>Q6 {@code TopFailingSteps}    - top-K by count using a bounded min-heap</li>
 * </ul>
 * <strong>The Stream API is banned in this package.</strong> Loops and collection methods only.
 * <p>
 * Java 21's sequenced collections are in scope here: {@code getFirst}, {@code getLast},
 * {@code reversed} on any {@code SequencedCollection}, and {@code putFirst} / {@code pollFirstEntry}
 * on a {@code SequencedMap}. Q5 wants an LRU built three ways, one of them driven by those methods.
 * You will rebuild Q1 with streams in Assignment 4 and compare.
 *
 * @see <a href="../../../../../../assignments/02-collections/README.md">Assignment 2 brief</a>
 */
package com.signia.training.a02collections;

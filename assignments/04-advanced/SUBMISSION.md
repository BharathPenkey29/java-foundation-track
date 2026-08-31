Yes. With the Q4 execution you provided, **Q4 can now be marked complete**. The output demonstrates all required execution modes, the 10,000-job comparison, lost updates, atomic/synchronized fixes, and the `synchronized` vs `ReentrantLock` comparison.

Here is the **updated complete `assignments/04-advanced/SUBMISSION.md`**:

# Submission — Assignment 4

**Name:** Bharath Penkey
**Branch:** `training/BharathPenkey/assignment-4`
**Date submitted:** 2026-08-31
**JDK version:** JDK 26.0.2

---

## How to run it

| Q  | Command                                                                                                    |
| -- | ---------------------------------------------------------------------------------------------------------- |
| Q1 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a04advanced.Q1_ResultDemo -f pom.xml`       |
| Q2 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a04advanced.Q2_RetryDemo -f pom.xml`        |
| Q3 | Not completed / command to be added after verification                                                     |
| Q4 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a04advanced.Q4_ConcurrentDemo -f pom.xml`   |
| Q5 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a04advanced.Q5_FailureBurstDemo -f pom.xml` |
| Q6 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a04advanced.Q6_IntervalDemo -f pom.xml`     |

---

## Status

| Q  | Done | Notes                                                                                                                                                                                                            |
| -- | ---- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Q1 | ☑    | Generic `Result`, generic repository, PECS, `Result.combine` stretch, and type-erasure demonstration completed.                                                                                                  |
| Q2 | ☑    | Generic retry with backoff, permanent-failure handling, void retry, suppressed attempt history, interrupt handling, and circuit-breaker stretch completed.                                                       |
| Q3 | ☐    | To be verified/completed.                                                                                                                                                                                        |
| Q4 | ☑    | Platform thread pool, `CompletableFuture`, virtual threads, 10,000-job comparison, lost-update demonstration, `synchronized`, `AtomicInteger`, `ConcurrentHashMap.merge`, and `ReentrantLock` stretch completed. |
| Q5 | ☑    | ArrayDeque sliding window, one-minute bucket detector, failure-rate stretch, and committed-fixture verification completed.                                                                                       |
| Q6 | ☑    | Interval merging, double-booking detection, time summaries, hand-written binary search, `Collections.binarySearch` comparison, and multi-provider stretch completed.                                             |

Stretch goals attempted: **Q1, Q2, Q4, Q5, Q6**

---

## Assumptions I made

* Used half-open intervals `[start, end)` throughout Q6.
* Intervals that only touch are not considered double-bookings, but are merged into one contiguous busy block.
* Double-bookings are detected before merging so the original overlapping pairs can be reported.
* Q5 uses the committed `events.txt` fixture with `K=5` and `W=60000 ms`.
* Q6 calculates free time only inside the configured working hours of `08:00–17:00`.
* Q6's multi-provider stretch requires the provider to have at least the requested continuous free duration.

---

## What I could not finish, and why

* Q3 has not been finalized yet.
* All other completed questions listed above have been implemented and executed successfully.

---

## Questions that asked for written answers

### A4 Q1 — Type erasure

Java generics use type erasure. `List<String>` and `List<Integer>` both erase to `List` at runtime. Therefore, methods that differ only by these generic parameter types cannot be overloaded.

The Q1 demonstration shows:

`List<String>` and `List<Integer>` cannot overload the same method because both erase to `List` at runtime.

### A4 Q2 — Retry and interrupt handling

Permanent failures are not retried.

Earlier failed attempts are attached to the final exception as suppressed exceptions so the attempt history is retained.

When an `InterruptedException` occurs during retry backoff, the interrupt flag is restored using `Thread.currentThread().interrupt()`.

### A4 Q4 — Concurrency

Q4 implements three execution modes:

1. Platform thread pool
2. `CompletableFuture`
3. Virtual threads

For 50 jobs, all three modes produced 40 successful and 10 failed jobs.

For 10,000 jobs:

* Platform thread pool: 1,147,285 ms
* Virtual threads: 932 ms
* Reported virtual-thread speedup: 1230.99x

The counter demonstration intentionally shows lost updates with an unsynchronised `int`.

The observed result was:

* Expected: `10000`
* Plain `int` actual: `1273`
* Lost updates: `8727`

The corrected implementations produced the expected `10000` using:

* `synchronized`
* `AtomicInteger`
* `ConcurrentHashMap.merge`

The stretch also compares a `synchronized` monitor with `ReentrantLock` under virtual threads.

### A4 Q5 — Failure burst detection

The committed fixture was tested using `K=5` and `W=60000 ms`.

The detector produced exactly two committed-fixture burst alerts:

* `JOB-5200` through `JOB-5204`
* `JOB-5600` through `JOB-5604`

Final verification:

* Expected alerts: `2`
* ArrayDeque alerts: `2`
* Bucket alerts: `2`
* Verification: `PASS`

The failure-rate stretch produced three rate alerts.

### A4 Q6 — Half-open intervals and binary search

Q6 uses half-open intervals `[start, end)`.

Therefore, an interval ending at exactly the same time another starts does not constitute a double-booking.

For example:

`09:30–10:15`
`10:15–11:00`

These intervals do not overlap, but they are contiguous and therefore merge into:

`09:30–11:00`

Double-bookings are detected before merging.

The hand-written binary search operates in `O(log n)` over the merged busy blocks.

For `Collections.binarySearch`, a negative return value means that the searched value was not found. The insertion point is calculated as:

`-(result + 1)`

This gives the index where the value would be inserted while maintaining sorted order.

---

## Deliberate demonstrations

| What                                       | Where                                            |
| ------------------------------------------ | ------------------------------------------------ |
| Type erasure                               | `Q1_ResultDemo`                                  |
| Retry attempt history                      | `Q2_RetryDemo`                                   |
| Interrupt handling                         | `Q2_RetryDemo`                                   |
| Circuit breaker                            | `Q2_RetryDemo`                                   |
| Lost updates                               | `Q4_ConcurrentDemo` — plain unsynchronised `int` |
| Synchronised counter                       | `Q4_ConcurrentDemo`                              |
| Atomic counter                             | `Q4_ConcurrentDemo`                              |
| `ConcurrentHashMap.merge` counter          | `Q4_ConcurrentDemo`                              |
| Virtual-thread carrier-thread pinning      | `Q4_ConcurrentDemo`                              |
| `synchronized` vs `ReentrantLock`          | `Q4_ConcurrentDemo`                              |
| Failure burst detection                    | `Q5_FailureBurstDemo`                            |
| Failure-rate detection                     | `Q5_FailureBurstDemo`                            |
| Double-booking detection                   | `Q6_IntervalDemo`                                |
| Half-open touching intervals               | `Q6_IntervalDemo`                                |
| Hand-written binary search                 | `Q6_IntervalDemo`                                |
| `Collections.binarySearch` insertion point | `Q6_IntervalDemo`                                |
| Multiple-provider free-time stretch        | `Q6_IntervalDemo`                                |

---

## What I tried that did not work

* The first Q5 implementation produced a bucket-detector mismatch: the ArrayDeque detector found 2 alerts while the bucket detector found 1. The bucket implementation was corrected, after which both detectors found 2 alerts and the committed-fixture verification passed.
* Passing explicit Q5 arguments through the IntelliJ Maven command initially caused a Windows command-line syntax error. Running the Maven execution with the required default values worked successfully.

---

## Time spent

| Q         | Hours |
| --------- |------:|
| Q1        |     2 |
| Q2        |   1.5 |
| Q3        |     2 |
| Q4        |   1.5 |
| Q5        |     1 |
| Q6        |   1.5 |
| **Total** |   9.5 |

---

## Anything else

* Q1, Q2, Q4, Q5, and Q6 have been implemented and executed successfully.
* Q4 now has a completed execution result and is marked complete.
* Q4 demonstrated the required lost-update problem with an incorrect unsynchronised result of `1273` versus the expected `10000`.
* Q4 demonstrated all three execution approaches and the 10,000-job platform-thread-pool versus virtual-thread comparison.
* Q4 also demonstrated `synchronized` versus `ReentrantLock` with virtual threads.
* Q5 committed-fixture verification passed with exactly 2 burst alerts.
* Q6 completed successfully, including the multiple-provider stretch goal.
* Q3 remains to be finalized and should be updated before the final Assignment 4 submission.
* Before final submission, run `mvn -q test` from a clean clone and confirm all tests pass.

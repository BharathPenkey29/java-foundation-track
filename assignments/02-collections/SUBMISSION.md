# Submission — Assignment 2

**Name:** Bharath Penkey
**Branch:** `training/BharathPenkey/assignment-2`
**Date submitted:** 2026-08-21
**JDK version:** ` openjdk version "26.0.2" 2026-07-21`

---

## How to run it

| Q | Command |
|---|---|
| Q1 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a02collections.GroupAndSummarizeVisits` |
| Q2 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a02collections.ReconcileRosters` |
| Q3 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a02collections.SortAndRangeQueries` |
| Q4 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a02collections.RetryQueue` |
| Q5 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a02collections.LruCacheDemo` |
| Q6 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a02collections.TopKFailingSteps` |

---

## Status

| Q | Done | Notes |
|---|---|---|
| Q1 | ☑ | Provider grouping, totals, status counts, zero-visit providers, map choices |
| Q2 | ☑ | Set reconciliation, mutation demonstration/fix, duplicates, large 500k fixture, reconciliation summary, composite-key stretch |
| Q3 | ☑ | Multi-key sorting, natural ordering, TreeMap range queries, comparator verification, configurable comparator stretch |
| Q4 | ☑ | ArrayDeque retry queue, dead-letter handling, deliberate ConcurrentModificationException, Iterator.remove(), removeIf(), PriorityQueue stretch  |
| Q5 | ☑ | Three LRU implementations, eviction agreement, statistics, sentinel-node implementation, TTL stretch |
| Q6 | ☑ | Min-heap Top-K, sorting comparison, large log, per-day Top-K stretch, more-than-doubled stretch |

Stretch goals attempted: Q1, Q2, Q3,Q4, Q5, Q6

All Q4 requirements and the PriorityQueue retry-ordering stretch were completed.

---

## Assumptions I made

- For Q1, providers listed in `providers.txt` but absent from the visits file are still included with zero visits.
- For Q2, source order is preserved using an insertion-ordered set so the first 10 members can be reported in source order.
- For Q2, duplicate reporting identifies an MRN as duplicated when it occurs more than once in the same source file.
- For Q3, the date range `2026-08-11` through `2026-08-13` is inclusive.
- For Q4, a retry is placed at the tail of the retry queue after a failed attempt.
- For Q6, a step is considered more than doubled when today's failure count is strictly greater than twice the previous day's count.
- For Q6, a step with no failures on the previous day has a previous count of zero.

---

## What I could not finish, and why

-
---

## Questions that asked for written answers

### Q1 — Map implementation choices

`HashMap` is appropriate for counting because fast key-based updates are needed and ordering is not required for the internal counts. `LinkedHashMap`/insertion-ordered collections are appropriate where source order must be preserved for reporting. A `TreeMap` would be appropriate when sorted key order is required, but it is not necessary for the basic counting operation.

### Q2 — Why use defensive copies with retainAll/removeAll?

`retainAll()` and `removeAll()` mutate the collection they are called on. Therefore, using them directly on the original roster can destroy information needed for another reconciliation calculation. Defensive copies allow each bucket to be calculated independently while preserving the original sets.

### Q3 — Comparable vs Comparator

`Comparable` is appropriate when a class has one natural ordering that belongs to the type itself. In Q3, `Visit` uses `visitId` as its natural ordering.

`Comparator` is appropriate when different orderings are required depending on the report. The visit report needs date descending, provider ascending, and duration descending, so a separate comparator is appropriate.

### Q4 — ConcurrentModificationException and PriorityQueue stretch

The broken version uses a for-each loop while directly calling `deadLetterQueue.remove(job)`. The enhanced for-loop uses an iterator internally, so structurally modifying the `ArrayList` directly during iteration causes `ConcurrentModificationException`.

`Iterator.remove()` is safe because the iterator performs the removal itself. `removeIf()` is also safe and is the concise option for removing elements matching a condition.

The main implementation uses `ArrayDeque` because the retry queue is FIFO: jobs are processed from the head and failed jobs are added to the tail. `ArrayDeque` provides explicit head/tail operations and implements `SequencedCollection`.

The stretch replaces `ArrayDeque` with `PriorityQueue`, ordering jobs by attempt count ascending and then by job ID. This means first-time jobs are prioritized before jobs that have already been retried.
### Q5 — Which LRU implementation would I ship?

I would normally ship the `LinkedHashMap` access-order implementation because it is concise, well-tested by the JDK, and directly expresses LRU behavior.

The custom `HashMap + doubly linked list` implementation is useful when full control over the data structure is required and demonstrates why O(1) LRU operations are possible.

### Q6 — Why a min-heap for Top-K?

A min-heap keeps the weakest member of the current Top-K at the root. When a better candidate appears, the root can be removed and replaced.

The heap therefore stores only K candidates instead of sorting every distinct step. The top-K selection is O(s log k), with the streaming counting pass taking O(n), giving O(n + s log k).

---

## Deliberate demonstrations

| What | Where |
|---|---|
| `retainAll()` mutation bug | `ReconcileRosters.java` — deliberate mutation demonstration followed by the defensive-copy fix |
| `ConcurrentModificationException` | `RetryQueue.java` — broken `for-each + list.remove()` version is commented out with the real exception trace |
| `List.reversed()` view behavior | `RetryQueue.java` — dead-letter list is printed using `reversed()` and the code explains that it is a view |
| Comparator direction / reversed-order trap | `SortAndRangeQueries.java` — wrong and corrected comparator behavior is demonstrated |
| Q4 PriorityQueue stretch | `RetryQueuePriority.java` — retries are ordered by attempt count ascending, with job ID as a deterministic tie-breaker |
| Q6 min-heap Top-K | `TopKFailingSteps.java` — weakest Top-K candidate is kept at the heap root |

---

## What I tried that did not work

- In Q2, mutating the original roster set with `retainAll()` caused the later removed calculation to become incorrect. The mutation was demonstrated and then corrected by using defensive copies.
- In Q4, directly removing an element from the `ArrayList` inside a for-each loop produced the required `ConcurrentModificationException`. The solution uses `Iterator.remove()` and `removeIf()` instead.

---

## Time spent

| Q |   Hours |
|---|--------:|
| Q1 |     1.5 |
| Q2 |       1 |
| Q3 |     1.5 |
| Q4 |     1.5 |
| Q5 |     1.5 |
| Q6 |       1 |
| **Total** |  **8.** |

---

## Anything else

- The large Q2 and Q6 fixtures are generated files and should not be committed because they are git-ignored.
- Q2 produces `reconciliation-summary.txt` as required.
- Q6 processes the large log line by line rather than loading one million lines into a list.
- No Stream API is used in the assignment package.
-  Q4 includes both the required ArrayDeque implementation and the PriorityQueue retry-ordering stretch.
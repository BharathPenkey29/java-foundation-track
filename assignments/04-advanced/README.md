# Assignment 4 — Generics, Streams & Concurrency

> **Write it once, then make it run at the same time.**

| | |
|---|---|
| **Focus** | Generics, wildcards, functional interfaces, Stream API, `Optional`, executors, virtual threads, atomics |
| **Prerequisite** | Assignments 1–3 signed off |
| **Package** | `com.signia.training.a04advanced` |
| **Fixtures** | `src/main/resources/a04/` (and `a02/visits.csv` again, for Q3) |
| **Tests** | JUnit 5 required for Q6 |
| **Suggested time** | 1–2 weeks. This is the hardest of the four; budget accordingly. |

---

## Q1 · Generic Result Type and Repository

**Advanced Java**

Two generic types you will use for the rest of your career: a result wrapper that carries either a
value or a coded failure, and a typed repository interface.

### Build

- `Result<T>` as a **sealed interface with two record implementations**:

  ```java
  sealed interface Result<T> permits Result.Ok, Result.Fail {
      record Ok<T>(T value) implements Result<T> { }
      record Fail<T>(String code, String message) implements Result<T> { }
  }
  ```

- Static factories `ok(T)` and `fail(String code, String message)`, plus
  `<R> Result<R> map(Function<T,R>)`, `flatMap`, `orElse(T)`, `ifOk(Consumer<T>)`, `isOk()`.
- Implement `map` and `orElse` with an **exhaustive switch pattern and no `default`**, using record
  deconstruction to pull the value straight out:

  ```java
  return switch (this) {
      case Ok<T>(T value)     -> Result.ok(fn.apply(value));
      case Fail<T> f          -> new Fail<>(f.code(), f.message());
  };
  ```

  Note why the failure case has to be rebuilt rather than cast, and write that reason down.
- `interface Repository<T, ID>`: `Optional<T> findById(ID)`, `List<T> findAll()`, `T save(T)`,
  `boolean deleteById(ID)`.
- `class MapRepository<T extends Identifiable<ID>, ID>` implementing it over a `ConcurrentHashMap`.
- One method taking `List<? extends Number>` and one taking `List<? super Integer>`, with a comment
  explaining PECS on each.
- A short demo chaining three `map` calls where the middle one fails, showing the failure
  short-circuits with its code intact.

### Constraints

- **Demonstrate type erasure:** try to overload one method for `List<String>` and `List<Integer>`,
  paste the compiler error as a comment, and explain it.
- `Result` is immutable and never holds both a value and an error. Note that the sealed-plus-record
  version makes that *structurally impossible* rather than merely enforced by a constructor check —
  which is the argument for building it this way. Say so in one line.
- Records are the one place in this track where you may skip hand-written `equals`, `hashCode` and
  `toString`. Contrast this deliberately with Assignment 3 Q3, where you were told to write them by
  hand: explain in `SUBMISSION.md` what makes a record right here and wrong there.
- Zero unchecked-cast warnings. If you need `@SuppressWarnings`, justify it in a comment on the
  annotation.
- No raw types anywhere. A bare `List` without a type argument is a finding.

> **Why this one is here.** A coded `Result` is how a step reports failure without throwing across
> a boundary where an exception would be the wrong control flow. You will meet this shape in every
> service we run.

**Stretch.** Add `Result.combine(List<Result<T>>)` that returns ok only if all are ok, and otherwise
aggregates every error code.

---

## Q2 · Generic Retry with Backoff

**Advanced Java**

One reusable method that can retry *any* operation. This is the single most-copied utility in our
codebase, so write it properly.

### Build

```java
static <T> T retry(ThrowingSupplier<T> action,
                   int maxAttempts,
                   long baseDelayMs,
                   Predicate<Exception> isRetryable) throws Exception
```

- Exponential backoff with jitter: attempt *n* waits roughly `base * 2^(n-1)`, plus a random
  fraction.
- Your own `@FunctionalInterface ThrowingSupplier<T>`, because `java.util.function.Supplier` cannot
  throw checked exceptions. Work out why that is before you write it.
- An overload for a void `ThrowingRunnable`, implemented by delegating to the `T` version rather
  than duplicating the logic.
- Test with a flaky action that fails twice and succeeds on the third attempt, and with one that
  always fails permanently — the second must not retry at all.

### Constraints

- A non-retryable exception must be rethrown immediately, on attempt one. Retrying a validation
  failure is a bug, not caution.
- On final failure, throw the *last* exception with the earlier ones attached as suppressed. Do not
  lose attempt history.
- Restore the interrupt flag if `Thread.sleep` throws `InterruptedException`. Explain why in a
  comment.
- Log each attempt: attempt number, delay, exception type. Never log the exception's message
  blindly — it might carry data you should not write to disk.
- Reuse `AppException.isRetryable()` from Assignment 3 Q4 as the default predicate.

> **Why this one is here.** Everything that touches a network fails intermittently.
> Retry-with-backoff is the difference between a platform that works and one that pages someone at
> 3am.

**Stretch.** Add a circuit breaker: after N consecutive failures across all calls, fail fast for
the next 60 seconds without even attempting.

---

## Q3 · The Same Report, in Streams

**Advanced Java**

Rebuild Assignment 2 Q1 with the Stream API, over the same `a02/visits.csv`, then decide honestly
which version you would rather maintain.

### Build

- Visits per provider (`groupingBy` + `counting`), total and average duration (`summingInt`,
  `averagingInt`).
- Top 3 providers by minutes (`sorted` + `limit`), and a completed/cancelled split with
  `partitioningBy`.
- Every distinct diagnosis across all patients using `flatMap` + `distinct` + `sorted`.
- A `Map<String, String>` of provider to busiest date via `Collectors.toMap` with a merge function
  — and then deliberately remove the merge function to see the `IllegalStateException` it prevents.
- Proper `Optional` use throughout: `map`, `filter`, `orElseGet`. No `.get()` without a guard.

### Constraints

- **Output must be identical to your Assignment 2 Q1 output.** Diff the two files — that is the
  test, and it is the whole point of the question.
- No side effects inside a stream. Never mutate an external collection from inside `forEach`;
  collect instead.
- Do not use `parallelStream()` here. Note in a comment when it would actually help and when it
  makes things slower.
- In `SUBMISSION.md`: name two of these that were clearer as loops, and two that were clearly better
  as streams. Preferring loops is an acceptable answer if you can argue it.

> **Why this one is here.** Streams are everywhere in our services. Writing the same logic both
> ways is the fastest route to judgement about when they help and when they are just shorter.

**Stretch.** Write a custom `Collector` that computes count, sum, min, max and average in a single
pass, and use it instead of four separate collectors.

---

## Q4 · Concurrent Job Runner

**Advanced Java**

Run many simulated automation jobs three ways — a bounded platform-thread pool, composed futures,
and virtual threads — then reproduce and fix a lost-update race with your own eyes.

### Build

- 50 simulated jobs, each sleeping a random 100–800 ms and failing about 20% of the time.
- **Version one — platform threads.** `ExecutorService` with a fixed pool of 4, `Callable` and
  `invokeAll`, collecting `Future` results and handling `ExecutionException`.
- **Version two — composed futures.** `CompletableFuture`: `supplyAsync`, `thenApply`,
  `exceptionally`, joined by `allOf`.
- **Version three — virtual threads.** `Executors.newVirtualThreadPerTaskExecutor()`, one virtual
  thread per job, no pool sizing at all.
- Then re-run versions one and three with **10,000 jobs** instead of 50. This is where the
  difference stops being academic — a fixed pool of 4 serialises them; virtual threads do not.
- A shared success counter, four ways:
  1. a plain `int` — run 10,000 increments across 8 threads and show the total is wrong,
  2. `synchronized`,
  3. `AtomicInteger`,
  4. `ConcurrentHashMap.merge` for per-status counts.
- Print sequential wall time versus each concurrent version, and the speedup ratio for each.

### Constraints

- Always shut down: `shutdown()`, then `awaitTermination`, then `shutdownNow()` if it times out. A
  leaked non-daemon pool keeps the JVM alive — demonstrate that once, then fix it. The
  virtual-thread executor is `AutoCloseable`, so try-with-resources handles this for you; say what
  its `close()` actually waits for.
- **Do not pool virtual threads.** A `newFixedThreadPool` of virtual threads is a mistake, and
  understanding why is the point: they are cheap to create and designed to be blocked, so pooling
  them re-imposes the exact scarcity they exist to remove. One paragraph in `SUBMISSION.md`.
- **On Java 21, a virtual thread that blocks inside a `synchronized` block pins its carrier thread**
  and cannot unmount. Your `synchronized` counter is a live example. Reproduce it, then show
  `ReentrantLock` behaving differently. Note that this specific limitation was addressed in a later
  Java release — so it is a Java 21 fact, not a permanent law.
- The lost-update demo must actually lose updates. If your run happens to come out correct, raise
  the iteration count until it does not.
- No `Thread` created with `new`. Executors only.
- Never `catch (InterruptedException e) {}`. Restore the flag.
- Speedup for the fixed pool will not be 4×. Explain what accounts for the gap — then explain
  separately why the virtual-thread version scales differently as the job count grows, and what
  would have to be true about the workload for it to be *slower*.

> **Why this one is here.** Our runners execute many jobs concurrently, and nearly all of that time
> is spent blocked on somebody else's slow portal rather than on CPU — which is exactly the shape
> virtual threads exist for. Meanwhile, shared mutable state under concurrency is where the hardest
> bugs of your career will come from, and the only cure is having caused one deliberately.

**Stretch.** Add a per-host rate limit with `Semaphore`, so at most 2 jobs targeting the same host
run at once while everything else stays busy. Then note what a `Semaphore` is really for once
threads are nearly free: the bound is about protecting the *remote* system, not about rationing
your own.

> Java 21 also previews **structured concurrency** (`StructuredTaskScope`), which is the natural
> next step from here. It is a preview API, it changed after 21, and `--enable-preview` is not set
> on this project — so read about it, do not submit it.

---

## Q5 · Failure-Burst Detector

**DSA**

Given a time-ordered event stream, raise an alert whenever K failures land inside any
W-millisecond window. A sliding window over *time* rather than over indices.

### Input

`src/main/resources/a04/events.txt` — 97 events, `epochMillis|jobId|status`, ascending. It contains
scattered harmless failures plus **two engineered bursts**: six failures inside 38 seconds, and
five inside 20 seconds. With `K=5, W=60000` both must fire, and nothing else may.

Lines starting with `#` are comments.

### Build

- Read events in timestamp order.
- Maintain an `ArrayDeque` of failure timestamps; evict from the head anything older than
  `now - W`; alert when the deque size reaches K.
- Each alert reports window start, window end, the count, and the offending job IDs.
- Suppress duplicate alerts — one alert per burst, not one per event once the threshold is crossed.
- Second implementation: fixed one-minute buckets counting failures per minute. Compare the two.

### Constraints

- O(n) overall. Each event is pushed and popped at most once — state that clearly in a comment.
- Single pass, no re-scanning the event list per window.
- Handle out-of-order timestamps by rejecting them with a clear message, and say what you would do
  instead in a real system.
- K and W come from the command line, not constants.
- Three lines in `SUBMISSION.md` on the trade-off: exact sliding window versus bucketed counters, in
  memory and in accuracy.

> **Why this one is here.** This is the alerting rule we want on our own job runs: not "a job
> failed" but "an unusual number failed in a short time", which is the one worth waking up for.

**Stretch.** Alert on failure *rate* instead of count — more than 30% of jobs in the window failed
— with a minimum sample size so that 1-of-2 does not trigger it.

---

## Q6 · Merge Visit Intervals and Search Them

**DSA**

Collapse a provider's overlapping appointments into contiguous busy blocks, find the largest gap,
then answer "were they busy at 14:20?" in logarithmic time.

### Input

`src/main/resources/a04/visit-intervals.csv` — four providers, chosen to cover every case:

| Provider | What it exercises |
|---|---|
| `K. Rao` | Overlaps, a **touching** pair (`10:15` end, `10:15` start), and a genuine double-booking. |
| `L. Byrne` | Touching intervals and one overlap, with real gaps between. |
| `P. Adeyemi` | One interval fully contained inside another. |
| `D. Marchetti` | A single all-day interval — no gaps at all. |

Working hours come from `src/main/resources/a04/working-hours.txt`.

### Build

- Sort intervals `[start, end)` by start, merge overlapping and touching ones in a single linear
  pass.
- Report total booked minutes, total free minutes inside working hours, and the largest free gap
  with its start and end times.
- Detect double-bookings — the specific pairs that overlap — *before* merging.
- A hand-written binary search over the merged list answering `isBusyAt(LocalTime)`.
- Verify against `Collections.binarySearch` with a custom `Comparator`, and explain the negative
  return value's meaning as an insertion point.

### Required tests

In `src/test/java/com/signia/training/a04advanced/IntervalMergerTest.java`, covering: the empty
list, a single interval, all intervals overlapping, the fully-contained case, and touching
intervals — which must collapse into **one** busy block with no gap between them, while **not**
being reported as a double-booking.

### Constraints

- Half-open intervals throughout. `K. Rao` is booked `09:30–10:15` and `10:15–11:00`: those two do
  **not** overlap, so they are not a double-booking — but they are contiguous, so they merge into a
  single busy block `09:30–11:00` with no free gap at 10:15. Getting one of those two facts right
  and the other wrong is the classic failure here. Say in a comment which rule you applied where.
- Sorting is O(n log n); the merge itself must be a single O(n) pass.
- Binary search is O(log n) — a linear scan does not count, even though it would pass the tests.
- Handle the empty list, one interval, and every interval overlapping every other.

> **Why this one is here.** Interval merging is how scheduling conflicts and coverage gaps get
> computed, and half-open interval discipline is the reason our reports agree with the client's.

**Stretch.** Handle intervals for multiple providers at once and answer "who is free at 14:20 for
at least 45 minutes?" — a sweep line over all providers.

---

## Before you submit

- [ ] `mvn -q test` passes.
- [ ] Q1's `Result` is a sealed interface over records, and `map` switches exhaustively with no
      `default`.
- [ ] Q1 has zero raw types and zero unexplained `@SuppressWarnings`, and the erasure error is
      pasted in.
- [ ] Q2 does not retry a permanent failure even once, and attaches earlier attempts as suppressed.
- [ ] Q2 restores the interrupt flag.
- [ ] **Q3's output diffs clean against your Assignment 2 Q1 output.** Paste the `diff` command and
      its empty result.
- [ ] Q4 genuinely loses updates in the unsynchronised version — show the wrong number.
- [ ] Q4 has all three execution modes, and the 10,000-job comparison between the fixed pool and
      virtual threads.
- [ ] Q4 demonstrates carrier-thread pinning under `synchronized` and contrasts it with
      `ReentrantLock`.
- [ ] Q4 shuts every executor down and the JVM exits on its own.
- [ ] Q5 fires exactly twice on the committed `events.txt` at `K=5, W=60000`.
- [ ] Q6 treats `[start, end)` as half-open and does not merge merely-touching intervals into a
      claim of no gap.
- [ ] `SUBMISSION.md` filled in — copy [the template](../SUBMISSION-TEMPLATE.md) into this folder.

---

## After this

You have written, in miniature, a file ingestion pipeline, a reconciliation report, a workflow
engine with retries and error routing, a scheduler, and an alerting rule. That is not a coincidence
and it is not a metaphor — it is the shape of what you will be handed next.

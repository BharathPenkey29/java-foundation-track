# Assignment 3 — Objects, Abstraction & Failure

> **Model the domain, then model how it goes wrong.**

| | |
|---|---|
| **Focus** | Inheritance, interfaces, polymorphism, immutability, `equals`/`hashCode`, sealed types, pattern matching, exception design |
| **Prerequisite** | Assignments 1–2 signed off |
| **Package** | `com.signia.training.a03oop` |
| **Fixtures** | `src/main/resources/a03/` (and `a01/` again, for Q2) |
| **New requirement** | JUnit 5 tests for Q3 and Q4 at minimum, in `src/test/java/com/signia/training/a03oop` |
| **Suggested time** | 1 week |

Streams are allowed from here on, but they are not the subject. Do not reach for them to avoid a
design decision.

---

## Q1 · Workflow Template Method

**Core Java**

Every automation we run has the same skeleton and different innards. Express that with an abstract
base class that owns the sequence and subclasses that own the steps.

### Build

- `abstract class Workflow` with a `final void run()` that calls, in order: `login()`,
  `navigate()`, `extract()`, `logout()`.
- `login()` and `extract()` abstract; `navigate()` and `logout()` with sensible defaults.
- Hooks `beforeStep(String)` and `afterStep(String, long durationMs)` that the base class calls
  around each step and that subclasses may override for logging.
- Two subclasses — `PortalAWorkflow` and `PortalBWorkflow` — that print distinct behaviour and
  where exactly one overrides `logout()`.
- Make `extract()` throw in one subclass, and prove that `logout()` still runs.

### Constraints

- `run()` is `final`. Subclasses control the steps, never the sequence. Write one line on why that
  matters.
- Cleanup must be guaranteed with `finally` — and it must not mask the original exception when
  cleanup itself fails. Work out what happens if you `return` from a `finally` block, then never
  do it again.
- Protected, not public, for anything only subclasses need.
- Include a comment contrasting this with achieving the same thing by composition. Which would you
  choose, and when?

> **Why this one is here.** This is verbatim the base class our automations extend. Getting logout
> to run even when extraction explodes is the difference between a failed job and a failed job that
> also leaves a session locked.

**Stretch.** Add a `Duration` timing report per step, and a `dryRun` flag that runs the sequence
and logs it without performing any step.

---

## Q2 · Interfaces, Polymorphism and a Factory

**Core Java**

One calling loop, three completely different file formats, zero `instanceof` checks.

### Input

Three formats, all already in the repo:

| Format | File |
|---|---|
| CSV | `src/main/resources/a01/patients.csv` |
| Fixed width | `src/main/resources/a01/fixedwidth/roster.txt` |
| Pipe delimited | `src/main/resources/a03/roster.psv` |

### Build

- `interface DataExtractor` with `List<String[]> extract(Path)`, `String name()`, a
  `default boolean supports(Path)` and a `static DataExtractor noop()`.
- Three implementations: `CsvExtractor`, `FixedWidthExtractor` (reuse your A1 Q6 logic),
  `PipeDelimitedExtractor`.
- `ExtractorFactory.forPath(Path)` selecting by extension, throwing `UnsupportedSourceException`
  when nothing matches.
- A driver that iterates `List<DataExtractor>` and runs each over a folder, printing rows extracted
  per format.

### Constraints

- No `instanceof` and no casting in the calling loop. If you need one, the interface is wrong.
- You must use both a `default` and a `static` interface method, and write one line each on when
  each is the right tool.
- Adding a fourth format must require zero edits to the driver. Demonstrate this by actually adding
  a fourth one at the end — tab-separated will do.
- The factory returns the interface type, never a concrete class.

> **Why this one is here.** We integrate with a growing list of source systems. The value of this
> pattern is measured in how little changes when the eleventh one arrives.

**Stretch.** Discover implementations at runtime with `ServiceLoader` instead of a hardcoded list,
so a new extractor registers itself.

---

## Q3 · Immutable Record with a Builder

**Core Java**

Build a value object that cannot be corrupted after construction, then deliberately break its
`hashCode` and watch a `HashSet` lose track of it.

### Build

- `final class PatientRecord`: all fields `final`, no setters, defensive copies in *and* out for
  the mutable `List<String> diagnoses`.
- A static nested `Builder` with a required `mrn` and optional everything else; `build()` validates
  and throws `IllegalStateException` listing **every** missing field at once, not just the first.
- `equals` and `hashCode` based on `mrn` alone, with a comment justifying that identity choice.
- `toString()` that masks name and DOB — reuse `maskPhi` from A1 Q3.

### Required tests

In `src/test/java/com/signia/training/a03oop/PatientRecordTest.java`:

- Equal records collapse to one entry in a `HashSet`.
- The `List` returned by `getDiagnoses()` cannot be used to mutate the record.
- The builder rejects an incomplete record, and the message names every missing field.

### Constraints

- **Then break it:** add a mutable field to `equals` but not `hashCode`, put the object in a
  `HashSet`, mutate it, and show that `contains` now returns false. Keep this as a test that
  documents the broken behaviour, clearly named.
- Do *not* use a Java `record` here — write it by hand. Then, at the bottom of the file, add the
  `record` equivalent as a comment and note what you got for free and what you lost. Be specific
  about two things a record would *not* have given you: the defensive copy of `diagnoses`, and
  `equals`/`hashCode` on `mrn` alone rather than on every component.
- Add one method that consumes a record with a **record pattern** —
  `if (o instanceof Diagnosis(String code, String description))` — so you have used deconstruction
  at least once. A small helper record is fine.
- No Lombok.
- Every constructor argument validated; fail at construction, never later.

> **Why this one is here.** A broken `hashCode` produces bugs that are invisible in testing and
> catastrophic in a caching layer. Seeing a `HashSet` fail to find an object it definitely contains
> is a formative experience.

**Stretch.** Add a `withDiagnosis(String)` method that returns a new instance rather than mutating
— the copy-on-write style that makes immutability practical.

---

## Q4 · Layered Exception Hierarchy

**Core Java**

Design failure as carefully as you design success: an exception hierarchy that tells the caller
whether to retry, and three layers that each handle what they are actually qualified to handle.

### Build

- `sealed abstract class AppException permits TransientException, PermanentException`, carrying an
  error code and `boolean isRetryable()`. `TransientException` covers timeout, connection reset and
  rate limited; `PermanentException` covers validation failed, not authorised and record not found.
  Make each branch `final` or `sealed` in turn — the compiler will insist, and understanding why is
  part of the question.
- The Runner layer routes with an **exhaustive `switch` pattern over the sealed hierarchy, with no
  `default` branch**:

  ```java
  Decision decision = switch (failure) {
      case TransientException t when t.attempt() >= maxAttempts -> Decision.DEAD_LETTER;
      case TransientException t                                 -> Decision.RETRY;
      case PermanentException p                                 -> Decision.DEAD_LETTER;
  };
  ```

- Then **add a third branch** to the hierarchy — say `ThrottleException` — and observe that the
  switch above stops compiling until you handle it. Paste that compiler error into your code as a
  comment. That error is the entire reason we sealed the type.
- Three layers:
  - **Step** throws raw low-level exceptions (`IOException`, `SocketTimeoutException`, and so on).
  - **Workflow** catches and translates them into `AppException` subtypes with codes, always
    preserving the cause.
  - **Runner** catches `AppException` and decides: retry, dead-letter, or abort the batch.
- A custom `AutoCloseable` session object used in try-with-resources whose `close()` *also* throws
  — then print the **suppressed** exceptions from the primary one.
- A driver that triggers one of each failure class and prints the routing decision for each.

### Required tests

In `src/test/java/com/signia/training/a03oop/ExceptionRoutingTest.java`: a timeout routes to retry,
a validation failure routes to dead-letter, and the cause chain survives translation intact.

### Constraints

- Never lose the cause. Every translation passes the original into the constructor, and your output
  must show the full chain.
- No layer catches an exception it cannot act on. Passing it up untouched is a valid and often
  correct decision.
- **No `default` branch in the routing switch.** A `default` re-introduces exactly the silent
  fall-through that sealing the hierarchy was meant to eliminate: it compiles happily forever while
  quietly mishandling every error type added after today. Write one line on why `default` and
  exhaustiveness are in tension.
- Use a `when` guard for at least one case, so that routing depends on state (attempt count) and not
  only on type.
- Include one example of the wrong pattern — `catch (Exception e) { e.printStackTrace(); }` —
  commented out, with two lines on exactly what it costs you.
- Write a short paragraph in `SUBMISSION.md`: checked or unchecked for `AppException`, and why you
  chose it. Both answers are defensible; an undefended one is not.

> **Why this one is here.** Retryable-versus-permanent is the single most important distinction in
> an automation platform. Retrying a validation failure 30 times wastes an hour; not retrying a
> timeout fails a job that would have worked.

**Stretch.** Add a `Map<Class<? extends Exception>, ErrorCode>` translation registry so the
workflow layer maps by lookup instead of a `catch` ladder.

---

## Q5 · Step Dependency Resolver

**DSA**

Steps declare what they depend on. Work out a legal execution order, detect impossible ones, then
work out what could run in parallel.

### Input

Three files in `src/main/resources/a03/`:

| File | What it exercises |
|---|---|
| `workflow-steps.txt` | The happy path — 9 steps, plus a disconnected 2-step component. |
| `workflow-steps-cyclic.txt` | A real cycle. Name the steps in it. |
| `workflow-steps-missing-dep.txt` | A dependency on a step that was never declared. |

```
login        ->
fetchRoster  -> login
fetchVisits  -> login
mergeReport  -> fetchRoster, fetchVisits
upload       -> validate
```

### Build

- Read steps and their dependencies into an adjacency structure. Ignore blank lines and `#`
  comments.
- Produce a valid linear execution order with Kahn's algorithm (in-degree + queue).
- Detect cycles and report the actual step names in the cycle — not just "a cycle exists".
- Produce **parallel waves**: wave 0 is everything with no dependencies, wave 1 is everything
  depending only on wave 0, and so on. Print each wave as a batch.
- Report the critical path length — the number of waves — as the theoretical minimum runtime.

### Constraints

- O(V + E). No repeated scanning of the full graph per step.
- A dependency on an undeclared step is an error with a clear message, not a silent skip.
- Output must be deterministic — break ties alphabetically inside each wave.
- Handle the disconnected component in `workflow-steps.txt`, and a single step with no
  dependencies at all.

> **Why this one is here.** Our longer workflows have real step dependencies, and the wave
> computation is exactly how you would decide what to parallelise in Assignment 4.

**Stretch.** Give each step a duration and compute the true critical path by longest weighted path,
rather than by wave count.

---

## Q6 · Config Validator and Undo Stack

**DSA**

Two stack problems in one: validate a config file's nesting, then build an undo/redo history over
typed commands.

### Input

`src/main/resources/a03/sample-config.txt` is valid — and contains brackets inside quoted strings
specifically to break a naive validator. `sample-config-broken.txt` has one genuine mismatch.

### Build

- **(a)** A validator for a config string containing `{}`, `[]`, `()` and paired quotes. On
  mismatch, report the character index, what was found, and what was expected.
- Brackets inside a quoted string are literal text and must be ignored. Both `"` and `'` pair, and
  each may contain the other as an ordinary character.
- **(b)** An `interface Command` with `apply()` and `revert()`. Implement three concrete commands
  over an in-memory record store: `SetField`, `AddDiagnosis`, `DeleteRecord`.
- An `EditSession` with undo and redo `Deque`s. Applying a new command after an undo clears the
  redo stack — as every real editor does.

### Constraints

- Part (a) must be a single pass, O(n) time, O(depth) space.
- `DeleteRecord.revert()` has to restore the deleted record — so the command must capture enough
  state at `apply()` time. Think about this before you write it.
- Undo on an empty stack is a no-op with a message, never an exception.
- Use `ArrayDeque`, not `Stack`.
- Print the store's state after every operation so the run log reads as a proof.

> **Why this one is here.** Part (b) is the command pattern — the cleanest place where OOP and a
> data structure meet, and the model behind every reversible operation you will ever build.

**Stretch.** Add `CompositeCommand` that groups several commands into one undoable transaction,
reverting them in reverse order.

---

## Before you submit

- [ ] `mvn -q test` passes, and the tests actually assert something.
- [ ] Q1's `run()` is `final`, and logout provably runs when extract throws.
- [ ] Q2's driver contains no `instanceof` and no casts, and you added a fourth format without
      touching it.
- [ ] Q3 has the deliberate broken-`hashCode` demonstration, clearly labelled.
- [ ] Q3's builder reports *all* missing fields at once.
- [ ] Q4's `AppException` is `sealed`, and the routing switch has **no** `default` branch.
- [ ] Q4 has the compiler error from adding a third exception type pasted in as a comment.
- [ ] Q4 preserves the cause chain end to end, and prints suppressed exceptions.
- [ ] Q4's checked-vs-unchecked paragraph is written and defensible.
- [ ] Q5 names the steps in the cycle and handles the missing-dependency file.
- [ ] Q6's validator ignores brackets inside quotes — test it against `sample-config.txt`.
- [ ] `SUBMISSION.md` filled in — copy [the template](../SUBMISSION-TEMPLATE.md) into this folder.

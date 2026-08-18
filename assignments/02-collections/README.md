# Assignment 2 — Collections

> **Choosing the right container is most of the design.**

| | |
|---|---|
| **Focus** | `List`, `Set`, `Map`, `Deque`, `Comparator`, iteration, hashing, sequenced collections |
| **Prerequisite** | Assignment 1 reviewed and signed off |
| **Package** | `com.signia.training.a02collections` |
| **Fixtures** | `src/main/resources/a02/` |
| **Hard rule** | **No Stream API anywhere in this assignment.** Loops and collection methods only. |
| **Suggested time** | 1 week |

That hard rule is not arbitrary. In Assignment 4 you will rebuild Q1 with streams and diff the two
outputs. Doing it the long way first is what makes the short way meaningful.

### Sequenced collections

Java 21 added the `SequencedCollection` / `SequencedSet` / `SequencedMap` interfaces, and this
assignment is where they earn their keep. Before Q2, read the javadoc for `SequencedCollection` and
note which of the types you already use implement it — `ArrayList`, `ArrayDeque`, `LinkedHashSet`
and `LinkedHashMap` all do.

The methods you now have everywhere: `getFirst()`, `getLast()`, `addFirst()`, `addLast()`,
`removeFirst()`, `removeLast()`, `reversed()` — and on a `SequencedMap`, `putFirst()`, `putLast()`,
`firstEntry()`, `lastEntry()`, `pollFirstEntry()`, `pollLastEntry()`.

Use them where they say what you mean. `list.getFirst()` beats `list.get(0)`; `list.getLast()`
beats `list.get(list.size() - 1)` and cannot be off by one. But `reversed()` returns a *view*, not
a copy — mutating it mutates the original. Know that before you use it.

---

## Q1 · Group and Summarize Visits

**Core Java**

Take a flat list of clinical visits and turn it into the three summaries a coordinator actually
asks for — using maps and loops, so that when you meet `Collectors.groupingBy` in Assignment 4 you
know precisely what it replaced.

### Input

`src/main/resources/a02/visits.csv` — 40 visits across 5 dates and 4 providers.
`src/main/resources/a02/providers.txt` — the full provider roster, which includes **two providers
with no visits at all**. They exist for the stretch goal, and for the trap in it.

```
visitId,mrn,provider,date,durationMins,status
V1001,MRN00042,K. Rao,2026-08-10,45,COMPLETED
V1004,MRN00045,L. Byrne,2026-08-10,25,CANCELLED
```

### Build

- A `Visit` class: `visitId`, `mrn`, `provider`, `LocalDate date`, `int durationMins`,
  `String status`.
- `Map<String, List<Visit>>` grouped by provider.
- `Map<String, Integer>` of total minutes per provider.
- `Map<String, Map<String, Integer>>` — per provider, a count of each status.
- Print providers sorted by total minutes descending, then by name for ties.

### Constraints

- Use `computeIfAbsent`, `merge` and `getOrDefault` — at least one of each. No
  `if (map.containsKey(k))` followed by `map.get(k)`.
- Iterate with `entrySet()`, never `keySet()` plus a lookup per key. Explain the difference in a
  comment.
- Choose `HashMap` vs `LinkedHashMap` vs `TreeMap` deliberately for each of the three maps, and
  justify each choice in one line.
- No streams. None.

> **Why this one is here.** Every operational report we produce is a group-by over a flat extract.
> Doing it by hand once means you will never be confused by a collector again.

**Stretch.** Read `providers.txt` and make sure `S. Okafor` and `R. Villanueva` appear in the
report with a count of 0. This is harder than it sounds and it is the bug we hit most often — a
group-by can only ever tell you about keys that are present in the data.

---

## Q2 · Two-Drop Reconciliation

**Core Java**

Yesterday's roster and today's roster. Work out what is new, what disappeared, what stayed, and
what is duplicated inside each file — using set algebra rather than nested loops.

### Input

Small, committed, hand-checkable:
`src/main/resources/a02/yesterday.csv` (16 rows) and `today.csv` (17 rows). Both contain in-file
duplicates. Work these out on paper first, then make your program agree with you.

Large, generated — for the memory constraint below:

```bash
mvn -q compile exec:java -Dexec.mainClass=com.signia.training.tools.FixtureGenerator
# writes roster-500k-yesterday.csv and roster-500k-today.csv (2,000 removed, 3,000 added)
```

### Build

- Load both files into sets of MRNs.
- Report four buckets: *added*, *removed*, *unchanged*, and *duplicated within a single file*.
- Print counts, then the first 10 members of each bucket in the order they appeared in the source
  file.
- Write a `reconciliation-summary.txt` that a non-engineer could read.

### Constraints

- Use `retainAll` and `removeAll` — but on defensive copies. **Demonstrate the bug first:** mutate
  the original set, show the wrong second answer, then fix it. Keep both in the code, the broken
  one commented out.
- Preserving source order is a requirement, so pick your `Set` implementation accordingly and say
  why in a comment. Report the earliest and latest MRN in each bucket using `getFirst()` and
  `getLast()` on a `SequencedSet` rather than by iterating and tracking.
- Detecting in-file duplicates requires something a `Set` alone cannot tell you. Solve it without
  loading the file twice.
- Must handle the 500,000-row pair without running out of memory on default heap settings.

> **Why this one is here.** Day-over-day reconciliation is how we detect that an upstream feed
> silently truncated. The set operations are trivial; the mutation trap in the middle of them is
> not.

**Stretch.** Reconcile on a composite key (MRN + date of birth) instead of MRN alone. You will need
a proper key type — which is a preview of Assignment 3.

---

## Q3 · Multi-Key Sorting and Range Queries

**Core Java**

Sort the same visit list four different ways, then make date-range lookups fast with a sorted map.

### Build

- Sort by date descending, then provider ascending, then duration descending — written twice: once
  with `Comparator.comparing().thenComparing().reversed()`, once as a hand-written `compare()`
  method.
- Implement `Comparable<Visit>` on `Visit` for its natural order (by `visitId`), and use
  `Collections.sort` with no comparator.
- Build a `TreeMap<LocalDate, List<Visit>>` and implement
  `visitsBetween(LocalDate from, LocalDate to)` using `subMap`, `headMap` and `tailMap`.
- Handle null providers with `Comparator.nullsLast`.

### Constraints

- Both sort implementations must produce byte-identical output on the same input. Prove it in code.
- Watch where `.reversed()` attaches — it reverses the whole chain, not the last key. Demonstrate
  the wrong version and the right one.
- Be explicit about whether `subMap` bounds are inclusive, and get the end date right. Query
  `2026-08-11` to `2026-08-13` and check by hand that you get the visits you expect.
- Write one paragraph on when `Comparable` is appropriate and when `Comparator` is. There is a
  right answer.

> **Why this one is here.** "Show me everything for this provider between these dates, most recent
> first" is a request we serve constantly. Also, a misplaced `.reversed()` is a genuinely common
> production bug.

**Stretch.** Make the sort order configurable from a string like `"date:desc,provider:asc"` parsed
at runtime into a composed `Comparator`.

---

## Q4 · Retry Queue and Safe Removal

**Core Java**

Model a queue of automation jobs that can fail and be re-attempted, then learn the three ways to
remove elements from a list you are iterating — one of which throws.

### Build

- A `Job` class with `jobId`, `attempt`, `submittedAt`, `lastError`.
- An `ArrayDeque<Job>` retry queue. Process jobs from the head; simulate ~30% failure; on failure
  increment `attempt` and push to the tail; after 3 attempts move to a dead-letter `List`.
- Print a run log and a final tally: succeeded, dead-lettered, total attempts consumed.
- Then: purge all dead-lettered jobs older than 7 days from the list, three ways — the broken
  `for-each` with `list.remove()`, then `Iterator.remove()`, then `removeIf`.

### Constraints

- Keep the broken version, commented out, with the real `ConcurrentModificationException` stack
  trace pasted beneath it. We want to see that you caused it on purpose.
- Use `ArrayDeque`, not `Stack` or `LinkedList`. Note in a comment why `Stack` is effectively
  deprecated — and why `ArrayDeque` being a `SequencedCollection` makes head-and-tail access
  explicit in a way `Stack`'s inherited `Vector` methods never were.
- Print the dead-letter list most-recent-first using `List.reversed()`. Then check whether your
  print loop can accidentally mutate the original through that view, and say what you found.
- Seed your `Random` so the run is reproducible. An unseeded run tells your reviewer nothing.
- Backoff is not required yet — that is Assignment 4. Just count attempts.

> **Why this one is here.** This is the actual shape of our job runner: a work queue, bounded
> retries, and a dead-letter list somebody has to look at on Monday.

**Stretch.** Swap the `ArrayDeque` for a `PriorityQueue` ordered by attempt count ascending, so
first-time jobs are always tried before retries.

---

## Q5 · LRU Cache, Twice

**DSA**

A bounded cache for recently-fetched patient records, with O(1) get and put. Build it the easy way,
then build it properly.

### Build

- **(a)** `LinkedHashMap` in access-order mode with `removeEldestEntry` overridden. Roughly 15
  lines.
- **(b)** From scratch: `HashMap<K, Node>` plus a doubly linked list you write yourself, with head
  and tail sentinel nodes.
- **(c)** `LinkedHashMap` again, but in *insertion* order, driving recency by hand with the Java 21
  `SequencedMap` methods: `putLast()` to promote on access, `pollFirstEntry()` to evict. No
  `removeEldestEntry`, no access-order flag.
- Generic in both key and value: `LruCache<K, V>`.
- Track hits, misses and evictions. Run one scripted access sequence through all three, assert they
  evict identically, then print the hit rate.

### Constraints

- All three must agree, entry for entry, on every eviction. If (c) disagrees with (a), you have
  found the difference between access-order mode and manual promotion — work out which one is
  right before you "fix" it.
- Version (c) exists to make the point that `SequencedMap` gave `LinkedHashMap` a public vocabulary
  for something it could always do internally. Two or three lines in `SUBMISSION.md` on which of
  the three you would actually ship, and why.
- Both `get` and `put` must be O(1). If you are scanning a list to find a node, you have the wrong
  data structure — and check the complexity of `pollFirstEntry()` before assuming (c) qualifies.
- Sentinel head and tail nodes are required in (b) — they remove every null check from the unlink
  logic. Note how much shorter the code gets.
- A `get` on an existing key counts as a use and must move that entry to most-recent.
- Capacity of 0 or a negative capacity must be rejected at construction.
- State the complexity of each operation in a comment.

> **Why this one is here.** It is the most-asked data structure question in the industry, and
> separately, it is what you would build to stop re-fetching the same record 40 times in one run.

**Stretch.** Add a per-entry TTL so entries expire on time as well as on capacity. Decide whether
you expire lazily on read or eagerly on a sweep, and defend it.

---

## Q6 · Top-K Failing Steps

**DSA**

Given a large run log, find the K workflow steps that fail most often — without sorting the whole
thing.

### Input

`src/main/resources/a02/run-log.txt` — 176 lines, small enough to verify by hand with
`grep FAILED run-log.txt | cut -d'|' -f3 | sort | uniq -c`.

The committed log contains a **deliberate tie**: two steps have the same failure count. Your
alphabetical tie-break decides which one makes the top 3, and getting that wrong is the whole
point of the constraint.

For the timing comparison, generate the large one:

```bash
mvn -q compile exec:java -Dexec.mainClass=com.signia.training.tools.FixtureGenerator
# writes run-log-1m.txt, one million lines
```

### Build

- Parse a log of lines shaped `timestamp|jobId|stepName|status`; count only `FAILED`.
- Count occurrences with a `HashMap<String, Integer>`.
- Select the top K with a **min-heap of size K** (`PriorityQueue`), so you never hold more than K
  candidates.
- Tie-break alphabetically by step name so output is deterministic.
- Also implement the sort-everything approach, and time both on the 1,000,000-line log.

### Constraints

- The heap solution must be O(n log k), not O(n log n). Write the complexity in a comment and be
  ready to justify it out loud.
- Stream the log line by line. Do not read a million lines into a `List` first.
- Get the comparator direction right — a *min*-heap for a *top*-K query is counter-intuitive and it
  is the whole trick. Explain it in two lines.
- Handle K larger than the number of distinct steps.

> **Why this one is here.** "Which step is breaking most this week" is the first question asked in
> every triage. Our real logs are large enough that the naive answer is slow enough to notice.

**Stretch.** Report the top K per day as well as overall, and flag any step whose failure count
more than doubled versus the previous day.

---

## Before you submit

- [ ] `grep -rn "\.stream()\|Collectors\." src/main/java/com/signia/training/a02collections` returns nothing.
- [ ] Q1 uses `computeIfAbsent`, `merge` and `getOrDefault`, and iterates with `entrySet()`.
- [ ] Q1 justifies each `Map` implementation choice in a comment.
- [ ] Q2 shows the mutation bug *and* the fix, and survives the 500k pair.
- [ ] Q3's two sort implementations produce identical output, proven in code.
- [ ] Q4 has the real `ConcurrentModificationException` trace pasted in.
- [ ] Q5's from-scratch cache uses sentinel nodes and is genuinely O(1).
- [ ] Q5 has all three implementations and they agree on every eviction.
- [ ] Sequenced-collection methods used where they say what you mean — no `get(size() - 1)` left in
      the code.
- [ ] Q6 breaks the tie alphabetically and states its complexity.
- [ ] Every `Random` is seeded.
- [ ] `SUBMISSION.md` filled in — copy [the template](../SUBMISSION-TEMPLATE.md) into this folder.

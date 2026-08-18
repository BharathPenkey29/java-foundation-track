# Java Foundations Track

**Fresher onboarding · Engineering**

Four assignments, twenty-four problems. Each one is a small, self-contained program — and each one
is a miniature of something we genuinely run in production: parsing a vendor file drop, deduping a
patient roster, retrying a flaky bot step, merging a provider's visit intervals. By the end you
will have written, in Java, a rough sketch of our stack.

| | |
|---|---|
| **Assignments** | 4 |
| **Questions each** | 4 Java + 2 DSA |
| **Pace** | 1 week each (Assignment 4 may take two) |
| **Language level** | Java 21 (LTS) |
| **Review** | Live walkthrough at the end of each assignment |

---

## The assignments

| # | Assignment | Theme | Brief |
|---|---|---|---|
| 01 | Syntax, Files & the Console | *Read it, validate it, write it back out.* | [→](assignments/01-fundamentals/README.md) |
| 02 | Collections | *Choosing the right container is most of the design.* | [→](assignments/02-collections/README.md) |
| 03 | Objects, Abstraction & Failure | *Model the domain, then model how it goes wrong.* | [→](assignments/03-oop/README.md) |
| 04 | Generics, Streams & Concurrency | *Write it once, then make it run at the same time.* | [→](assignments/04-advanced/README.md) |

They are **sequential**. Assignment 3 reuses code you wrote in Assignment 1; Assignment 4 reuses
Assignment 2 and 3. Do them in order, and do not start the next one before the previous one is
signed off — gaps compound.

Also read: [SETUP.md](SETUP.md) · [RUBRIC.md](RUBRIC.md) · [SUBMISSION template](assignments/SUBMISSION-TEMPLATE.md)

---

## What Java 21 changes

We build on 21 because it is the current LTS, and because three of its features change what the
*right* answer looks like rather than merely offering a shorter way to write the old one. Those
three are required, in these questions specifically:

| Feature | Where it is required | Why it is not just sugar |
|---|---|---|
| **Sealed types + pattern matching for `switch`** | A3 Q4, A4 Q1 | An exhaustive switch over a sealed hierarchy means the *compiler* tells you when a new error type is unhandled. A `default` branch silently swallows that. |
| **Sequenced collections** | A2 Q2, Q4, Q5 | `getFirst()`, `getLast()`, `reversed()`, `putFirst()` on a `LinkedHashMap` — first-and-last access finally has a name, instead of five different idioms per collection type. |
| **Virtual threads** | A4 Q4 | Thousands of concurrent blocking tasks stop needing a pool at all. This inverts the standard advice you will read in older tutorials. |

Everything else in the track is ordinary Java that has worked for a decade. Do not reach for a
language feature to avoid a design decision — a `switch` pattern in the wrong place still scores as
the wrong design.

---

## Getting started

```bash
git clone <this-repo>
cd java-foundations-track
git checkout -b training/<yourname>/assignment-1

mvn -q compile                      # should succeed on a clean checkout
mvn -q compile exec:java -Dexec.mainClass=com.signia.training.tools.FixtureGenerator
```

That last command writes the large input files that are deliberately kept out of git. You need it
before Assignment 1 Q5. Full details in [SETUP.md](SETUP.md).

---

## Ground rules

These apply to every question in every assignment. Most of the marks that get lost in review are
lost here, not in the algorithms.

### Environment

- **JDK 21 (LTS).** The project compiles at language level 21. A newer JDK is fine to *run*, but
  anything newer than 21 syntactically will be rejected by the build, and that is deliberate.
- **Preview features are off.** Java 21 ships String Templates, Structured Concurrency and Scoped
  Values as *previews*; they are not on the syllabus, `--enable-preview` is not set, and a couple
  of them were changed or withdrawn in later releases. Where a stretch goal touches one, it says so
  explicitly.
- **Maven** project, one module, package root `com.signia.training`, one sub-package per
  assignment.
- **IntelliJ IDEA Community** is the house default, and it bundles Maven, so you do not need a
  separate install. Learn the debugger in week one — breakpoints, step into/over, watch
  expressions, evaluate expression. You will use it more than you use `System.out.println`.
- **Git.** One branch per assignment: `training/<yourname>/assignment-1`. Commit as you go, not
  once at the end. We read the commit history.
- **No third-party libraries.** JDK standard library only. The single exception is JUnit 5, from
  Assignment 3 onward, for tests. No Apache Commons, no Guava, no OpenCSV, no Lombok — you are here
  to learn what those libraries do for you. `pom.xml` is not to be edited.

### What you hand in

- Working code that compiles clean — zero warnings you cannot explain.
- A **`SUBMISSION.md`** inside the assignment folder: how to run each question, what you assumed,
  what you could not finish and why. Copy [the template](assignments/SUBMISSION-TEMPLATE.md). A
  short honest one beats a long vague one.
- Console output of a successful run, committed as `run-output.txt` in the assignment folder. We
  should not have to run it to see that it worked — but we will run it anyway.
- Any *new* input files you invented, committed alongside. If your program reads a file, that file
  is part of the submission.

### Standing rules for every question

- **No hardcoded absolute paths.** Read from `args[]` or the classpath.
  `C:\Users\you\Desktop\file.csv` fails on the reviewer's machine, which is the whole point.
- **Every stream, reader and writer is closed** — use try-with-resources. This is not style advice;
  we have shipped a production memory leak that was exactly this.
- **Never swallow an exception.** An empty `catch` block, or one containing only
  `e.printStackTrace()`, is an automatic finding in review.
- **Never print real-looking patient data** beyond the synthetic fixtures in this repo. Get in the
  habit now: when you log an identifier, mask it.
- **Name things fully.** `patientCount`, not `pc`. `i` and `j` are fine as loop indices and nowhere
  else.
- **Seed every `Random`.** An unseeded run cannot be reproduced, and a number your reviewer cannot
  reproduce is not evidence.

### On using AI

You may use it. You may not hide behind it.

The review is a live walkthrough where you will be asked to explain any line, change a requirement
on the spot, and predict what the program does before running it. Code you cannot defend counts as
code you did not write, whatever generated it. The reviewer's actual question is *"why this and not
that?"* — and there is no tool that answers that for you.

---

## The fixtures

Everything your programs read lives in `src/main/resources/`, one folder per assignment. **The
fixture files contain deliberate defects.** Malformed rows, duplicate keys, bad checksums, a
dependency cycle, a truncated line, brackets inside quoted strings. They are there because your
program is supposed to survive them.

Do not edit a fixture to make your program pass. If a fixture looks wrong, it probably is wrong on
purpose — and if you genuinely think it is a mistake, say so in `SUBMISSION.md` and you will get
credit for spotting it.

```
src/main/resources/
├── a01/
│   ├── patients.csv                  28 lines, 3 deliberately malformed, 1 non-ASCII name
│   ├── drops/                        5 files that between them trip every validator check
│   └── fixedwidth/roster.txt         14 lines: 2 bad checksums, 1 truncated, 1 over-long
├── a02/
│   ├── visits.csv                    40 visits, 4 providers, 5 dates
│   ├── providers.txt                 6 providers — 2 of them have no visits at all
│   ├── yesterday.csv / today.csv     roster pair with adds, removes and in-file duplicates
│   └── run-log.txt                   176 lines, with a deliberate tie in the failure counts
├── a03/
│   ├── workflow-steps*.txt           happy path, a cycle, and a missing dependency
│   ├── sample-config*.txt            brackets inside quoted strings, and one real mismatch
│   └── roster.psv                    pipe-delimited, for the extractor factory
└── a04/
    ├── events.txt                    97 events with two engineered failure bursts
    ├── visit-intervals.csv           overlaps, touching pairs, containment, all-day
    └── working-hours.txt
```

Large inputs (100k MRNs, a 1M-line log, a 500k roster pair) are **generated, not committed**. They
are seeded, so every machine produces byte-identical files:

```bash
mvn -q compile exec:java -Dexec.mainClass=com.signia.training.tools.FixtureGenerator
```

---

## How you are marked

Scored out of 100 in a live review. Below 60 means a rework of the weak questions before moving on.
Full criteria in [RUBRIC.md](RUBRIC.md).

| Weight | Criterion |
|---:|---|
| 35 | Correctness — right answer, edge cases handled, stated constraints met |
| 20 | Code quality — honest names, small methods, no duplication, no dead code |
| 15 | Failure handling — resources closed, exceptions neither swallowed nor blanket-caught |
| 15 | Proof of work — tests, committed run output, complexity stated where required |
| 15 | Explanation — you can walk any line and justify any choice |

---

## Getting unstuck

Time-box it to 45 minutes. Then ask — in the team channel, not privately, so the answer is useful
to everyone. Bring what you tried, the exact error, and what you think it means.

"It doesn't work" starts a twenty-minute conversation. "I expect X here, I get Y, and I think it is
because Z" starts a two-minute one.

Being stuck is not a mark against you. Being stuck quietly for three days is.

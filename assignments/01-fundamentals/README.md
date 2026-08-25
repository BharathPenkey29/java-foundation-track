# Assignment 1 — Syntax, Files & the Console

> **Read it, validate it, write it back out.**

| | |
|---|---|
| **Focus** | Types, control flow, arrays, `String`, `Scanner`, file I/O, exceptions |
| **Prerequisite** | None. Start here even if you have written Java before. |
| **Package** | `com.signia.training.a01basics` |
| **Fixtures** | `src/main/resources/a01/` |
| **Deliverable** | 6 runnable classes + `SUBMISSION.md` in this folder |
| **Suggested time** | 1 week |

Before you start, read the [root README](../../README.md) — the standing rules apply to every
question here, and losing marks to them is the most common way to lose marks.

---

## Q1 · Patient Intake Console

**Core Java**

A menu-driven console app that captures a patient intake record field by field, refusing to move
on until each field is valid. This is the "hello world" of every line-of-business system ever
written.

### Build

- A loop-driven menu: *1. New intake · 2. List captured · 3. Exit*.
- Capture MRN, first name, last name, date of birth (`dd-MM-yyyy`), phone, and visit type chosen
  from a numbered list.
- Validate every field and re-prompt on failure with a message that says what was wrong —
  *"DOB must be dd-MM-yyyy, you entered 1990/05/12"*, not *"Invalid input"*.
- Store records in a fixed-size array of a simple `Patient` class; refuse gracefully when full.
- Print the captured list with `System.out.printf` in aligned columns.

### Constraints

- Exactly one `Scanner` instance for the whole program. Do not open a new one per read, and never
  close `System.in` mid-run.
- Handle the `nextInt()` / `nextLine()` newline trap deliberately — write a comment explaining
  what it is.
- Catch `InputMismatchException` and recover without crashing.
- Use `switch` (the arrow form), `while`, `do-while`, and at least one ternary.
- No collections. Arrays only — that is the point of week one.

> **Why this one is here.** Every bot we run starts by accepting a job configuration from outside
> and refusing to proceed on bad input. Failing loudly and early at the boundary is the habit
> being trained.

**Stretch.** Add option 4: save all captured records to a CSV, then reload them on next launch if
the file exists.

---

## Q2 · CSV Reader to Filtered Report

**Core Java**

Read a patient roster CSV, parse it into objects, filter it, and write a human-readable report
file. Roughly 40% of the code we maintain is some version of this sentence.

### Input

`src/main/resources/a01/patients.csv` — 28 data lines, of which **three are deliberately
malformed**: one has too few columns, one has an unparseable date, and one is a blank line. Find
them by handling them, not by editing the file. That leaves 25 rows you can actually parse.

One row contains non-ASCII characters in the name. Read and write with an explicit
`StandardCharsets.UTF_8` — if you rely on the platform default charset, that row will be mangled on
Windows and fine on Linux, which is exactly the class of bug that only appears in production.

```
mrn,lastName,firstName,dob,visitType,provider
MRN00042,Alvarez,Rosa,1948-03-11,HOSPICE,K. Rao
MRN00043,Bell,Thomas,1979-11-02,ROUTINE,K. Rao
```

### Build

- Read the CSV (header row + data) with `BufferedReader`.
- Map each row to a `Patient` object; skip and *report* malformed rows rather than dying on them.
- Filter: age >= 65 **or** `visitType` equals `HOSPICE`.
- Write `eligible-patients.txt` with aligned columns, a title line, and a footer in this shape —
  the numbers below are illustrative, work out the real ones:
  *"N of 25 rows eligible, 3 rows skipped."*

Note that the eligible count is **not** a fixed number: it depends on the date you run the program,
because two patients in this file sit within two years of the age-65 boundary. Print the reference
date alongside the count so the result can be checked later.

### Constraints

- try-with-resources for both the reader and the writer. No `finally { close(); }`.
- Use `String.split(",", -1)` and explain in a comment why the `-1` limit matters for trailing
  empty fields.
- Compute age from `LocalDate` and `Period`, not by subtracting years. Age is relative to the day
  the program runs — print the reference date in your output so the numbers can be checked later.
- A missing input file must produce one clear line of output and exit code 1 — not a stack trace.
- Read the file exactly once. No loading it twice to count rows.

> **Why this one is here.** Vendors send us flat files daily. They arrive malformed more often
> than you would believe, and a job that dies on row 4,000 of 10,000 is worse than one that
> reports and continues.

**Stretch.** Handle quoted fields containing commas — `"Alvarez, Jr.",Rosa`. Write the parser
character by character. You will now understand why CSV libraries exist.

---

## Q3 · Identifier & Date Normalizer

**Core Java**

A static utility class of pure functions for cleaning up the messy identifiers that arrive from
upstream systems. No state, no I/O, entirely testable.

### Build

- `normalizeMrn(String)` — trim, uppercase, drop every non-alphanumeric character, left-pad with
  zeros to 8 characters. Reject anything longer than 8 after cleaning.
- `toIsoDate(String)` — accept `dd/MM/yyyy`, `dd-MM-yyyy`, `MM/dd/yyyy` and `yyyyMMdd`; return
  `yyyy-MM-dd`. Ambiguous inputs must throw, not guess.
- `maskPhi(String fullName)` — `"Rosa Alvarez"` becomes `"R*** A*****"`, preserving length.
- A `main` that runs at least five cases per method, **including the failure cases**, and prints a
  pass/fail line for each.

### Constraints

- Write `normalizeMrn` twice: once with a `char` loop and `StringBuilder`, once with a regex. Keep
  both. Comment on which you would ship and why.
- Use `DateTimeFormatter` — not `SimpleDateFormat`, which is not thread-safe. Say so in a comment.
- Never use `+=` to build a string inside a loop. Know why.
- Private constructor so the utility class cannot be instantiated.
- Null and empty input must be handled explicitly on every method.

> **Why this one is here.** Identifier normalization is where our matching logic lives or dies.
> `mrn00042`, `MRN-00042` and `42` are the same patient, and something has to decide that.

**Stretch.** Add `levenshtein(String, String)` and use it to flag near-duplicate surnames within
one file.

---

## Q4 · File Drop Validator with Error Codes

**Core Java**

Given a folder of incoming CSV files, validate each one against an expected shape and emit coded,
machine-greppable findings. This is a stripped-down version of a tool we actually run.

### Input

`src/main/resources/a01/drops/` — five files, engineered so that between them they trip every
check:

| File | What is wrong with it |
|---|---|
| `drop-clean.csv` | Nothing. It must pass. |
| `drop-empty.csv` | Zero bytes. |
| `drop-header-only.csv` | Valid header, no data rows. Decide whether that is an error and defend it. |
| `drop-bad-header.csv` | Two columns renamed. |
| `drop-broken-rows.csv` | Too few columns, too many columns, blank `mrn`, blank `dob`, and a duplicate key. |

Expected header: `mrn,lastName,firstName,dob,visitType,provider`

### Build

- Walk a directory given on the command line; process every `*.csv` in it.
- Checks, in order: file exists and is readable; file is non-empty; header matches the expected
  column list exactly; every row's column count matches the header; mandatory columns (`mrn`,
  `dob`) are non-blank; no duplicate `mrn` within a file.
- A custom **checked** `ValidationException` carrying an error code, the file name and the row
  number.
- Print a summary table at the end: file, rows read, findings by code. Exit `0` if clean, `1` if
  any file failed.

```
TRN.1.1  FILE_EMPTY
TRN.1.2  HEADER_MISMATCH
TRN.2.1  COLUMN_COUNT_MISMATCH
TRN.2.2  MANDATORY_FIELD_BLANK
TRN.2.3  DUPLICATE_KEY
```

### Constraints

- Codes live in one `enum` with a code string and a message template — not scattered string
  literals.
- One bad file must not stop the other files from being validated. Collect findings, do not fail
  fast at the top level.
- Use `java.nio.file.Files` and `Path`, not the legacy `File` API.
- Expected headers come from a config file or a constant — not inlined in three different methods.
- Findings print in stable, sorted order so two runs on the same input produce identical output.

> **Why this one is here.** Our alerting keys off structured error codes. A log line that says
> "something went wrong" cannot be routed, counted, or dashboarded; `TRN.2.2` can.

**Stretch.** Emit the findings as a JSON file as well as a table — hand-written, no library. It
will be uglier than you expect, and that is the lesson.

---

## Q5 · Duplicate Detection, Three Ways

**DSA**

Find duplicate MRNs in a large array using three different strategies, then measure them. The
answer matters less than the measurement.

### Input

Run the fixture generator first — the file is too large to keep in git:

```bash
mvn -q compile exec:java -Dexec.mainClass=com.signia.training.tools.FixtureGenerator
```

That writes `src/main/resources/generated/mrns-100k.txt`: 100,000 MRNs containing exactly **250
planted duplicates**, scattered by a seeded shuffle. The seed is fixed, so your file and your
reviewer's file are identical.

### Build

- **(a)** Nested-loop comparison, O(n²). Count the comparisons performed.
- **(b)** Sort, then scan adjacent pairs, O(n log n).
- **(c)** Single pass with a `HashSet`, O(n).
- Each returns the first duplicate found and the full set of duplicated values. Time each with
  `System.nanoTime()`.

### Constraints

- All three must agree on the set of duplicates. Assert it in code — if they disagree, one of them
  is wrong. You know the expected count, so check against it.
- Run (a) on a reduced input if it takes over 30 seconds, and say so in your output.
- Write three to four sentences in `SUBMISSION.md`: which won, by what factor, and what each one
  costs in *memory* as well as time.
- State the time and space complexity of each in a comment above the method.

> **Why this one is here.** Duplicate patient keys inside a single vendor drop are a real and
> recurring defect. Once the file is large enough, the naive answer stops being an answer.

**Stretch.** Return the *indices* of every occurrence, not just the values, without changing the
O(n) complexity.

---

## Q6 · Fixed-Width Record Parser with Checksum

**DSA**

Older clinical systems still emit fixed-width flat files. Parse one by column position and verify
each line's trailing checksum.

### Input

`src/main/resources/a01/fixedwidth/roster.txt` — 14 lines. Most are well-formed. Some are not, and
working out which is the exercise.

```
cols  0- 7  MRN        (8)
cols  8-27  LAST NAME  (20)
cols 28-42  FIRST NAME (15)
cols 43-50  DOB        (8, yyyyMMdd)
cols 51-52  CHECKSUM   (2)
            total line length 53
```

Checksum rule: sum the ASCII values of characters 0–50 inclusive, take `% 97`, zero-pad to two
digits.

### Build

- Parse each line by position, using `substring` only — no `split`, no regex.
- Trim the space padding from each extracted field.
- Verify the checksum and report every line number that fails it.
- Report every line whose length is wrong, then print totals.

### Constraints

- Never let `substring` throw. Short and over-long lines are data errors to be reported, not
  crashes.
- Column boundaries are declared once as constants — changing a width must mean editing one line.
- Process the file line by line. Do not read it fully into memory; assume it could be 2 GB.
- Report the first 10 failures in detail, then just a count. Nobody reads 40,000 error lines.

> **Why this one is here.** Positional parsing and integrity checks are exactly what an interface
> with a legacy EHR looks like. Also: this is where you learn that off-by-one is not a joke.

**Stretch.** Make the layout data-driven — read the field name, offset and length from a small
schema file, so the same parser handles a second layout with no code change.

---

## Before you submit

- [ ] All six questions run from a clean checkout with no absolute paths.
- [ ] `mvn -q compile` produces no warnings you cannot explain.
- [ ] Every reader and writer is inside a try-with-resources.
- [ ] No empty `catch` blocks, and none containing only `e.printStackTrace()`.
- [ ] Q2 reports the three malformed rows instead of crashing on them.
- [ ] Q4 finds every defect in every drop file and still exits cleanly.
- [ ] Q5 asserts that all three implementations agree.
- [ ] Q6 survives both the truncated line and the over-long line.
- [ ] `SUBMISSION.md` filled in — copy [the template](../SUBMISSION-TEMPLATE.md) into this folder.
- [ ] Committed to `training/<yourname>/assignment-1`, with more than one commit.

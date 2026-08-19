# Submission — Assignment 1

**Name:** Bharath Penkey 
**Branch:** `training/BharathPenky/assignment-1`  
**Date submitted:** 19-Aug-2026  
**JDK version:** ` openjdk version "26.0.2" 2026-07-21`

---

## How to run it

The six runnable classes are in package `com.signia.training.a01basics`.

| Q | Command |
|---|---|
| Q1 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a01basics.PatientIntakeConsole` |
| Q2 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a01basics.EligibilityReport` |
| Q3 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a01basics.Identifiers` |
| Q4 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a01basics.FileDropValidator -Dexec.args="src/main/resources/a01/drops"` |
| Q5 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.tools.FixtureGenerator` then `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a01basics.DuplicateDetection` |
| Q6 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.a01basics.FixedWidthParser` |

Q5 requires the fixture generator first because the 100,000-MRN input is generated rather than kept in git.

---

## Status

| Q | Done | Notes |
|---|---|---|
| Q1 | ☑ | Patient intake console implemented with the required validation/control-flow constraints. |
| Q2 | ☑ | CSV is read once, malformed rows are reported/skipped, eligible patients are written to the report. |
| Q3 | ☑ | Identifier/date utility methods and failure cases are included. |
| Q4 | ☑ | All CSV drop files are validated with coded findings and a checked `ValidationException`. |
| Q5 | ☑ | Three duplicate-detection approaches are implemented and their results are asserted against each other. |
| Q6 | ☑ | Fixed-width records are processed line-by-line with length and checksum validation. |

**Stretch goals attempted:** Q4 JSON report.

---

## Assumptions I made

- **Q4:** A header-only file is treated as valid/clean because the brief requires the file to be non-empty and the header to match, but does not require at least one data row.
- **Q4:** Operational file-access problems are reported separately from the assignment validation error codes.
- **Q5:** The nested-loop implementation is reduced to 10,000 records only if the full-input run exceeds the specified 30-second limit.
- **Q6:** The checksum is calculated from characters 0–50 inclusive, using the ASCII/numeric character values and `% 97`, then zero-padded to two digits.

---

## What I could not finish, and why

- Nothing intentionally left unfinished. Before submission, verify the commands above from a clean checkout and replace the JDK/time placeholders with the actual values from the submission run.

---

## Questions that asked for written answers

### A1 Q5 — Duplicate-detection comparison

The **HashSet** approach is expected to be the fastest overall 
because it performs a single pass with average **O(n)** time,
compared with **O(n log n)** for sort-and-scan and **O(n²)** 
for the nested-loop approach. 

Nested loop: 451.025 ms — but this was on 10,000 records
Sort + scan: 201.165 ms — 100,000 records
HashSet: 34.923 ms — 100,000 records

Because the nested-loop was run on a reduced input, we should NOT claim that HashSet is X times faster than nested loop. That would be an invalid comparison.
**Measured winner/factor from final run:** HashSet was the fastest measured approach at 34.923 ms. The nested-loop timing is not directly comparable because the full 100,000-record run exceeded 30 seconds and was therefore rerun on 10,000 records.
---

### A1 Q4 — Header-only file

I treated the header-only file as a clean file because the assignment explicitly requires the file to be non-empty and the header to match, but does not state that at least one data row is mandatory. This assumption is documented in the code and here so the reviewer can see the decision.

---

## Deliberate demonstrations

| What | Where |
|---|---|
| Q4 checked `ValidationException` with validation codes | `FileDropValidator.java` — `ValidationException` extends `Exception` |
| Q4 malformed CSV rows | `FileDropValidator.java` — column count, blank MRN/DOB and duplicate MRN checks |
| Q5 three different duplicate-detection strategies | `DuplicateDetection.java` — nested loop, sort + scan, HashSet |
| Q6 truncated/over-long line handling | `FixedWidthParser.java` — length validation occurs before `substring()` |
| Q6 checksum failure handling | `FixedWidthParser.java` — checksum validation and failure reporting |

---

## What I tried that did not work

- No unresolved implementation dead end remains. The final implementation uses the approaches required by the assignment brief.
- Q5's nested-loop implementation includes the required 30-second safeguard so the full 100,000-record O(n²) run does not prevent the assignment from completing.

---

## Time spent

| Q |     Hours |
|---|----------:|
| Q1 | `1.5hour` |
| Q2 |   `1 min` |
| Q3 |   `45min` |
| Q4 |   `1hour` |
| Q5 |   `1hour` |
| Q6 |   `1hour` |
| **Total** |    `6.45` |

---

## Anything else

- The implementation follows the required package `com.signia.training.a01basics`.
- Q4 additionally generates a hand-written `file-drop-findings.json` report as the stretch goal.
- Q5's generated fixture should be created before running `DuplicateDetection`.
- The final submission should be verified from a clean checkout with no absolute paths, as required by the assignment.

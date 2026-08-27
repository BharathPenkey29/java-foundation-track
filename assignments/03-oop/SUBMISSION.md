
# Submission — Assignment 3

**Name:** Bharath Penkey  
**Branch:** `training/BharathPenkey/assignment-3`  
**Date submitted:** 26-Aug-2026  
**JDK version:**

```text
openjdk version "26.0.2"
OpenJDK Runtime Environment
OpenJDK 64-Bit Server VM
```

---

## How to run it

| Q | Command |
|---|---|
| Q1 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.WorkflowDemo` |
| Q2 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.Q2_ExtractorDemo` |
| Q3 | `mvn -q test -Dtest=Q3_PatientRecordTest` |
| Q4 | `mvn -q test -Dtest=Q4_ExceptionRoutingTest` |
| Q5 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.Q5_DependencyResolverDemo` |
| Q6 | `mvn -q compile exec:java -Dexec.mainClass=com.signia.training.Q6_EditSessionDemo` |

---

## Status

| Q | Done | Notes                                                                                                                             |
|---|---|-----------------------------------------------------------------------------------------------------------------------------------|
| Q1 | ☑ | Template Method pattern with hooks, timing, cleanup in finally, exception handling                                                |
| Q2 | ☑ | Extractor interface, factory, CSV/Fixed Width/PSV/TSV support, no-op implementation                                               |
| Q3 | ☑ | Immutable PatientRecord, Builder, defensive copies, HashSet equality, record pattern                                              |
| Q4 | ☑ | Exception routing with retry/permanent/throttle handling and tests                                                                |
| Q5 | ☐ | Topological ordering, parallel waves, critical path length, cycle detection, and missing-dependency detection implemented.        |
| Q6 | ☐ | Config validator, Command pattern, undo/redo stacks, delete restoration, and actual A3 config fixtures implemented and verified . |


---

## Assumptions I made

- For Q3, only `mrn` is mandatory; `name`, `dob`, and `diagnoses` are optional.
- Equality and hash code for `PatientRecord` are based only on `mrn`, as it uniquely identifies a patient.
- `diagnoses` is represented as an immutable list using defensive copies.
- `maskPhi()` from Assignment 1 Q3 is reused instead of reimplementing masking logic.
- Unsupported file types in Q2 throw `Q2_UnsupportedSourceException`.

---

## What I could not finish, and why

- All implemented questions compile and pass tests successfully.

---

## Questions that asked for written answers

### A3 Q1 — Inheritance vs Composition

Template Method uses inheritance to keep the workflow sequence fixed while allowing subclasses to customize steps. Composition would provide more flexibility by injecting strategies for login, extraction, and navigation. For this assignment, Template Method is more appropriate because the workflow order is a fixed business rule.

---

### A3 Q3 — Why equals() and hashCode() use only MRN

MRN uniquely identifies a patient. Name, DOB, and diagnoses may change over time, but the patient identity remains the same. Therefore, equality and hashing are based only on MRN.

---

### A3 Q4 — Checked or Unchecked Exceptions

`AppException` and its subclasses were implemented as unchecked exceptions (`RuntimeException`) because:

- Callers are not forced to catch every exception.
- Centralized routing logic decides retry/permanent/throttle handling.
- This matches common application-service exception handling patterns.

---
### A3 Q5 — dependency resolution

The dependency resolver uses topological ordering so that a step is processed only after its dependencies have been processed.

Parallel waves group steps that can execute independently at the same stage.

Cycle detection prevents a cyclic workflow from being treated as executable.

The implementation also reports dependencies that refer to undeclared steps.


## Deliberate demonstrations

| What | Where |
|---|---|
| Extraction failure with guaranteed cleanup | `PortalBWorkflow.extract()` and `Workflow.run()` |
| Logout runs even after exception | `Workflow.run()` finally block |
| HashSet issue caused by mutable hash fields | `Q3_PatientRecordTest.brokenHashCodeDemonstration()` |
| Unsupported source exception | `Q2_ExtractorFactory` |
| Q5 cycle detection | `Q5_DependencyResolverDemo` using `workflow-steps-cyclic.txt` |
| Q5 missing dependency detection | `Q5_DependencyResolverDemo` using `workflow-steps-missing-dep.txt` |
| Q6 invalid bracket configuration | `Q6_EditSessionDemo` using `sample-config-broken.txt` |
| Q6 undo deleted record | `Q6_EditSessionDemo` |
| Q6 new edit clears redo history | `Q6_EditSessionDemo` |


---

## What I tried that did not work

- IntelliJ showed `Cannot resolve symbol 'Test'` even though Maven dependencies were correct. The issue was IDE indexing; Maven tests passed successfully.
- Initially treated all `PatientRecord` fields as mandatory, but the assignment required only `mrn` to be mandatory.
- During Q5 implementation, `TreeMap` was used without importing `java.util.TreeMap`. Adding the missing import fixed the compilation error.
- During Q6 validation, the first demo used hardcoded configuration strings. It was changed to read the required `sample-config.txt` and `sample-config-broken.txt` fixtures from `src/main/resources/a03/`.

---

## Time spent

| Q | Hours    |
|---|----------|
| Q1 | 3        |
| Q2 | 5        |
| Q3 | 4        |
| Q4 | 2        |
| Q5 | 1.5      |
| Q6 | 2        |
| **Total** | **17.5** |

---

## Anything else

- Maven build and tests pass successfully:

```text
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- IntelliJ sometimes failed to recognize JUnit until Maven reindexing was performed.
- The assignment structure and Maven setup were clear and easy to follow.

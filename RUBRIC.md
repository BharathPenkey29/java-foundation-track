# How each assignment is marked

Scored out of 100 in a live review. Below 60 means a rework of the weak questions before moving on;
the track is sequential and gaps compound.

The score is not the point. The review conversation is the point — the score just makes it concrete.

---

## The criteria

| Weight | Criterion | What full marks looks like |
|---:|---|---|
| **35** | Correctness | Produces the right answer on the given input and on the edge cases the question names. Meets every stated constraint, including the complexity ones. |
| **20** | Code quality | Honest names, small methods, no duplication, no dead code, no commented-out experiments left behind — except the ones a question explicitly asks you to keep. |
| **15** | Failure handling | Resources closed, exceptions neither swallowed nor blanket-caught, bad input reported clearly, nothing crashes on a malformed row. |
| **15** | Proof of work | Tests where required, run output committed, complexity stated where required, and the deliberate-bug demonstrations actually demonstrated. |
| **15** | Explanation | You can walk through any line, justify each design choice, and correctly predict the program's behaviour under a change the reviewer invents on the spot. |

---

## What the reviewer will actually do

1. **Clone your branch fresh** and run `mvn -q compile`. Warnings get asked about.
2. **Run your code on an input file you have not seen.** It will contain a blank line, a duplicate
   key, and a row with the wrong column count. This is not a trick — the committed fixtures warned
   you.
3. **Pick one method and ask you to change its behaviour live.** Not to catch you out; to see how
   you navigate your own code.
4. **Ask "what happens if this is null?"** at least once per assignment.
5. **Ask you to name something you tried that did not work.** There should be one. "It all went
   fine" is a worse answer than a specific dead end.
6. **Read your commit history.** One commit titled `final` at 11pm the night before tells its own
   story.

---

## Common ways marks are lost

Ranked by how often they actually happen.

| | Costs |
|---|---|
| Absolute paths — code that only runs on your machine | Correctness |
| `catch (Exception e) { e.printStackTrace(); }` | Failure handling |
| Readers and writers not closed, or closed in `finally` instead of try-with-resources | Failure handling |
| Unseeded `Random`, so no number in the submission can be reproduced | Proof of work |
| Editing a fixture file to make the program pass | Correctness, and it is noticed immediately |
| Complexity claimed in a comment that the code does not actually achieve | Correctness + Explanation |
| A deliberate-bug demonstration described in `SUBMISSION.md` but not present in the code | Proof of work |
| `SUBMISSION.md` missing or empty | Proof of work, and it makes the review slower for everyone |
| Streams used in Assignment 2 | Correctness — it is an explicit hard rule |
| Cannot explain a line, because it was generated and not read | Explanation, heavily |

---

## Scoring bands

| Score | Means |
|---|---|
| **85–100** | Ship-quality for a fresher. Move on immediately; consider the stretch goals. |
| **70–84** | Solid. Specific feedback given, no rework needed, carry the notes into the next assignment. |
| **60–69** | Passing. One or two questions get reworked in parallel with starting the next assignment. |
| **Below 60** | Rework the weak questions before starting the next assignment. This is a scheduling decision, not a judgement — the next assignment builds directly on this one. |

---

## For reviewers

Keep it to 45–60 minutes per assignment. Structure that works:

- **10 min** — candidate demos it running. They drive; you watch.
- **20 min** — walk two questions, one Java and one DSA. Pick the ones where the code looks most
  and least confident.
- **10 min** — you run it on the unseen input. Watch how they react to a failure in front of you,
  which is more informative than whether it fails.
- **10 min** — the live change. Small and concrete: "make this sort ascending", "what if the file
  has no header row?"
- **5 min** — score, two things done well, two to carry forward.

Give the two things done well first, and be specific. "Good job" is worth nothing; "your error
codes made the validator output greppable, which is exactly why we do that" teaches.

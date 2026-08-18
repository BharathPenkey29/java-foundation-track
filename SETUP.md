# Setup

Everything here should take under an hour. If it takes longer, stop and ask — a broken environment
is not a productive first week.

---

## 1. Install the JDK

You need **JDK 21**. It is the current LTS. Temurin is the easiest:

- Windows: [adoptium.net](https://adoptium.net) → Temurin **21** LTS, `.msi` installer, tick
  *Set JAVA_HOME*.
- macOS: `brew install --cask temurin@21`
- Linux: `sudo apt install openjdk-21-jdk`

Verify — **both** commands must work:

```bash
java -version
javac -version
```

If `java` works and `javac` does not, you installed a JRE rather than a JDK. Install the JDK.

> The project compiles at **language level 21**. A newer JDK will run it, but syntax newer than 21
> is rejected by the build, and that is deliberate.
>
> **Preview features are off.** Java 21's String Templates, Structured Concurrency and Scoped
> Values are previews — `--enable-preview` is not set and will not be. String Templates in
> particular were withdrawn in a later release, so anything you learn about them now is a liability.
> If a stretch goal touches a preview feature, it says so.

---

## 2. Install IntelliJ IDEA Community

Download from [jetbrains.com/idea](https://www.jetbrains.com/idea/download) — the **Community**
edition is free and sufficient.

IntelliJ **bundles Maven**, so you do not need a separate Maven install. Opening the project's
`pom.xml` is enough.

### Open the project

`File → Open` → select the `java-foundations-track` folder (not a file inside it). IntelliJ detects
the `pom.xml` and imports it. Wait for indexing to finish.

Then check `File → Project Structure → Project`: SDK should be 21, language level 21. IntelliJ
sometimes defaults a new import to an older level — if `switch` patterns show red, this is why.

### Learn the debugger this week

Seriously — this is the highest-return hour of your onboarding.

| What | How |
|---|---|
| Breakpoint | Click the gutter next to a line number |
| Debug | `Shift+F9` |
| Step over | `F8` — run this line, stay in this method |
| Step into | `F7` — go into the method being called |
| Evaluate expression | `Alt+F8` — run any expression in the current scope |
| Watch | Add a variable to the Watches panel to track it as you step |

A conditional breakpoint (right-click the breakpoint → Condition) that fires only when
`row == 4000` will save you more time across these four assignments than any other single trick.

---

## 3. Command-line Maven (optional)

Only needed if you want to build outside the IDE.

- Windows: `winget install Apache.Maven`, or download from
  [maven.apache.org](https://maven.apache.org/download.cgi) and add `bin/` to `PATH`.
- macOS: `brew install maven`
- Linux: `sudo apt install maven`

```bash
mvn -v
```

---

## 4. Build and verify the checkout

```bash
git clone <this-repo>
cd java-foundations-track

mvn -q compile          # must succeed with no output
mvn -q test             # passes trivially until you write tests in Assignment 3
```

If `mvn -q compile` prints nothing, you are set up correctly.

---

## 5. Generate the large fixtures

Three questions need input files too big to sensibly keep in git. Generate them once:

```bash
mvn -q compile exec:java -Dexec.mainClass=com.signia.training.tools.FixtureGenerator
```

Expected output:

```
mrns-100k.txt                   100,000 lines (250 planted duplicates)
run-log-1m.txt                1,000,000 lines
roster-500k-yesterday.csv       500,000 + 1,000 duplicate lines
roster-500k-today.csv           501,000 lines (2,000 removed, 3,000 added)
```

They land in `src/main/resources/generated/`, which is git-ignored. **Do not commit them.** They
are seeded, so your files are byte-identical to your reviewer's — which is what makes your timing
numbers mean anything.

Total size is about 55 MB. Regenerate rather than back up.

---

## 6. Start Assignment 1

```bash
git checkout -b training/<yourname>/assignment-1
```

Open [assignments/01-fundamentals/README.md](assignments/01-fundamentals/README.md) and begin.

---

## Running a single question

From the IDE: click the green arrow next to any `main` method.

From the command line:

```bash
mvn -q compile exec:java \
  -Dexec.mainClass=com.signia.training.a01basics.PatientIntakeConsole

# with arguments
mvn -q compile exec:java \
  -Dexec.mainClass=com.signia.training.a01basics.FileDropValidator \
  -Dexec.args="src/main/resources/a01/drops"
```

Q1 of Assignment 1 reads from the console. `mvn exec:java` handles stdin poorly on some setups — if
prompts do not appear, run it from the IDE instead. That is a known quirk of the tool, not a bug in
your code.

---

## Troubleshooting

**`mvn` is not recognised.** You did not install standalone Maven. Either install it (step 3) or
just use IntelliJ's bundled one.

**`release version 21 not supported`.** Your JDK is older than 21. Check `javac -version` — and
check it is the one IntelliJ is using, which is not always the one on your `PATH`.

**IntelliJ shows red everywhere but `mvn compile` works.** Right-click `pom.xml` →
*Maven → Reload project*.

**Tests are not discovered.** They must live under `src/test/java`, and the class name must end in
`Test`.

**Fixture files look corrupted — every line ends in `^M`, or the fixed-width columns are off by
one.** Your git checkout converted line endings. `.gitattributes` pins these files to LF; if you
cloned before that took effect, re-clone. The fixed-width parser in Assignment 1 Q6 depends on
exact line length, so this matters.

**Out of memory on the 500k roster pair.** That is the constraint working as intended. Do not raise
the heap — hold less.

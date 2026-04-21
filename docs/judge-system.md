# Judge System — CodeArena

> Owner: Zeeshan (Tech Lead)
> Files: `judge/JudgeEngine.java`, `judge/Sandbox.java`, `judge/TestCaseRunner.java`, `judge/Verdict.java`

---

## Overview

The CodeArena judge is a local code execution engine. It compiles and runs user-submitted Java code against a set of test cases, comparing actual output to expected output, and assigns a verdict to the submission. It runs entirely on the local machine using `java.lang.ProcessBuilder` — no external judge service is used.

---

## Verdict Types

| Verdict | Code | Meaning |
|---|---|---|
| Accepted | AC | All test cases passed |
| Wrong Answer | WA | Output did not match expected |
| Time Limit Exceeded | TLE | Execution took more than 5 seconds |
| Compilation Error | CE | `javac` failed to compile the code |
| Runtime Error | RE | Process exited with non-zero code |
| Memory Limit Exceeded | MLE | Reserved for future implementation |

---

## Execution Flow

```
JudgeEngine.runSubmission(Submission submission)
│
├── 1. Sandbox.createTempDir(submission.getId())
│       Creates: /tmp/codearena/{submissionId}/
│
├── 2. Write source code to file
│       Path: /tmp/codearena/{submissionId}/Solution.java
│
├── 3. COMPILE PHASE
│       Command: ["javac", "Solution.java"]
│       Working dir: /tmp/codearena/{submissionId}/
│       ├── Exit code != 0 → CE verdict, goto cleanup
│       └── Exit code == 0 → continue
│
├── 4. For each TestCase (from TestCaseDAO.getByProblemId):
│       TestCaseRunner.run(testCase, workingDir)
│       │
│       ├── Command: ["java", "-cp", ".", "Solution"]
│       ├── Pipe testCase.input to process stdin
│       ├── Capture stdout into StringBuilder
│       ├── waitFor(5, TimeUnit.SECONDS)
│       │   ├── Timeout → process.destroyForcibly() → TLE
│       │   └── No timeout → continue
│       ├── process.exitValue() != 0 → RE
│       └── stdout.trim().equals(testCase.expected.trim())
│           ├── true  → AC
│           └── false → WA
│
├── 5. Determine overall verdict
│       Priority: CE > RE > TLE > WA > AC
│       (worst single test case verdict wins)
│
├── 6. Update submission object with verdict + runtime
│
├── 7. SubmissionDAO.save(submission)
│
└── 8. Sandbox.cleanup(submissionId)  ← ALWAYS runs (finally block)
```

---

## Class Responsibilities

### Verdict.java
```java
public enum Verdict {
    AC, WA, TLE, CE, RE, MLE;

    public int priority() {
        // Higher = worse, used to determine overall verdict
        return switch (this) {
            case CE  -> 5;
            case RE  -> 4;
            case TLE -> 3;
            case WA  -> 2;
            case AC  -> 0;
            case MLE -> 1;
        };
    }
}
```

### Sandbox.java
```java
public class Sandbox {
    private static final String BASE_DIR = "/tmp/codearena/";

    public static Path createTempDir(int submissionId) throws IOException {
        Path dir = Paths.get(BASE_DIR + submissionId);
        Files.createDirectories(dir);
        return dir;
    }

    public static void writeSourceFile(Path dir, String code) throws IOException {
        Files.writeString(dir.resolve("Solution.java"), code);
    }

    public static void cleanup(int submissionId) {
        // Recursively delete /tmp/codearena/{submissionId}/
        // Use Files.walk() with reversed order delete
        // Must be in a finally block — never skip cleanup
    }
}
```

### TestCaseRunner.java
```java
public class TestCaseRunner {
    public static Verdict run(TestCase tc, Path workingDir) {
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", ".", "Solution");
        pb.directory(workingDir.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        // Write tc.getInput() to process.getOutputStream()
        // Read stdout from process.getInputStream()
        // waitFor(5, TimeUnit.SECONDS)
        // Compare output to tc.getExpected()
        // Return appropriate Verdict
    }
}
```

### JudgeEngine.java
```java
public class JudgeEngine {
    private final TestCaseDAO testCaseDAO;
    private final SubmissionDAO submissionDAO;

    public Verdict runSubmission(Submission submission) {
        int submissionId = submission.getId();
        try {
            Path dir = Sandbox.createTempDir(submissionId);
            Sandbox.writeSourceFile(dir, submission.getCode());

            // Compile
            if (!compile(dir)) {
                submission.setVerdict(Verdict.CE);
                submissionDAO.save(submission);
                return Verdict.CE;
            }

            // Run against all test cases
            List<TestCase> testCases = testCaseDAO.getByProblemId(submission.getProblemId());
            Verdict overall = Verdict.AC;
            for (TestCase tc : testCases) {
                Verdict v = TestCaseRunner.run(tc, dir);
                if (v.priority() > overall.priority()) overall = v;
                if (overall == Verdict.CE) break; // can't get worse
            }

            submission.setVerdict(overall);
            submissionDAO.save(submission);
            return overall;

        } catch (Exception e) {
            submission.setVerdict(Verdict.RE);
            submissionDAO.save(submission);
            return Verdict.RE;
        } finally {
            Sandbox.cleanup(submissionId);
        }
    }
}
```

---

## Threading — How to Call from UI

```java
// In CodeEditorController.java
private void handleSubmit() {
    Submission submission = buildSubmission();

    Task<Verdict> judgeTask = new Task<>() {
        @Override
        protected Verdict call() {
            return judgeService.evaluate(submission);
        }
    };

    judgeTask.setOnSucceeded(e -> {
        Platform.runLater(() -> {
            Verdict v = judgeTask.getValue();
            verdictLabel.setText(v.name());
            // update UI with colors, messages, XP gain, etc.
        });
    });

    judgeTask.setOnFailed(e -> {
        Platform.runLater(() -> verdictLabel.setText("Judge Error"));
    });

    new Thread(judgeTask).start();
    // Or use an ExecutorService for better thread management
}
```

---

## Security Notes

- The judge does NOT sandbox the executed code from the OS (no container/chroot). This is acceptable for a local desktop app used by trusted users in an academic setting.
- Future improvement: wrap execution in a SecurityManager or Docker container for public deployment.
- Temp files are always cleaned up via the `finally` block to prevent disk accumulation.
- Never pass user input directly to shell commands — always use `ProcessBuilder` with argument arrays, not `Runtime.exec(String command)`.

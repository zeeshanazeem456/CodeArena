package com.codearena.judge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.codearena.TestDatabase;
import com.codearena.dao.ProblemDAO;
import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.TestCaseDAO;
import com.codearena.model.Difficulty;
import com.codearena.model.Problem;
import com.codearena.model.Submission;
import com.codearena.model.TestCase;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JudgeEngineIntegrationTest {

    private int problemId;

    @BeforeEach
    void resetDatabase() throws Exception {
        TestDatabase.reset();
        problemId = createEchoProblem();
    }

    @Test
    void javaAcceptedWrongAnswerCompilationErrorAndCleanupAreHandled() {
        JudgeEngine judge = new JudgeEngine(new TestCaseDAO(), new SubmissionDAO());

        Submission accepted = submission("""
                import java.util.*;
                public class Solution {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        System.out.print(sc.nextInt() * 2);
                    }
                }
                """, "Java");
        assertEquals(Verdict.AC, judge.runSubmission(accepted));
        assertFalse(Sandbox.exists(accepted.getId()));

        Submission wrongAnswer = submission("""
                public class Solution {
                    public static void main(String[] args) {
                        System.out.print(999);
                    }
                }
                """, "Java");
        assertEquals(Verdict.WA, judge.runSubmission(wrongAnswer));
        assertFalse(Sandbox.exists(wrongAnswer.getId()));

        Submission compileError = submission("""
                public class Solution {
                    public static void main(String[] args) {
                        System.out.print("missing close");
                    }
                """, "Java");
        assertEquals(Verdict.CE, judge.runSubmission(compileError));
        assertFalse(Sandbox.exists(compileError.getId()));
    }

    @Test
    void javaRuntimeErrorAndTimeoutAreHandled() {
        JudgeEngine judge = new JudgeEngine(new TestCaseDAO(), new SubmissionDAO());

        Submission runtimeError = submission("""
                public class Solution {
                    public static void main(String[] args) {
                        throw new RuntimeException("boom");
                    }
                }
                """, "Java");
        assertEquals(Verdict.RE, judge.runSubmission(runtimeError));

        Submission timeout = submission("""
                public class Solution {
                    public static void main(String[] args) {
                        while (true) {
                        }
                    }
                }
                """, "Java");
        assertEquals(Verdict.TLE, judge.runSubmission(timeout));
        assertFalse(Sandbox.exists(timeout.getId()));
    }

    @Test
    void pythonAcceptedAndSyntaxErrorsAreHandledWhenPythonIsAvailable() {
        assumeTrue(isPythonAvailable(), "Python runtime not available on PATH");
        JudgeEngine judge = new JudgeEngine(new TestCaseDAO(), new SubmissionDAO());

        Submission accepted = submission("""
                import sys
                value = int(sys.stdin.read().strip())
                print(value * 2)
                """, "Python");
        assertEquals(Verdict.AC, judge.runSubmission(accepted));

        Submission syntaxError = submission("if True print('bad')", "Python");
        assertEquals(Verdict.CE, judge.runSubmission(syntaxError));
        assertFalse(Sandbox.exists(syntaxError.getId()));
    }

    private int createEchoProblem() {
        Problem problem = new Problem();
        problem.setTitle("Double It");
        problem.setDescription("Double the input.");
        problem.setDifficulty(Difficulty.EASY);
        problem.setCategory("Tests");
        problem.setTags("judge");
        problem.setTimeLimit(5);
        problem.setMemoryLimit(256);
        problem.setPublished(true);
        new ProblemDAO().save(problem);

        TestCase testCase = new TestCase();
        testCase.setProblemId(problem.getId());
        testCase.setInput("21");
        testCase.setExpected("42");
        testCase.setSample(true);
        testCase.setSequenceOrder(1);
        new TestCaseDAO().save(testCase);
        return problem.getId();
    }

    private Submission submission(String code, String language) {
        Submission submission = new Submission();
        submission.setUserId(1);
        submission.setProblemId(problemId);
        submission.setCode(code);
        submission.setLanguage(language);
        return submission;
    }

    private boolean isPythonAvailable() {
        String[][] commands = {{"python", "--version"}, {"python3", "--version"}, {"py", "-3", "--version"}};
        for (String[] command : commands) {
            try {
                Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
                if (process.waitFor(3, TimeUnit.SECONDS) && process.exitValue() == 0) {
                    return true;
                }
            } catch (IOException exception) {
                // Try the next candidate.
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
}

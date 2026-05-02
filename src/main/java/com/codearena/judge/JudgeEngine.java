package com.codearena.judge;

import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.TestCaseDAO;
import com.codearena.model.Submission;
import com.codearena.model.TestCase;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JudgeEngine {

    private final TestCaseDAO testCaseDAO;
    private final SubmissionDAO submissionDAO;
    private List<TestCaseRunner.TestCaseResult> lastResults = List.of();

    public JudgeEngine(TestCaseDAO testCaseDAO, SubmissionDAO submissionDAO) {
        this.testCaseDAO = testCaseDAO;
        this.submissionDAO = submissionDAO;
    }

    public Verdict runSubmission(Submission submission) {
        return runSubmission(submission, false);
    }

    public Verdict runSubmission(Submission submission, boolean sampleOnly) {
        long totalRuntimeMs = 0L;

        try {
            if (!sampleOnly && submission.getId() == 0) {
                submissionDAO.save(submission);
            } else if (sampleOnly && submission.getId() == 0) {
                submission.setId((int) (System.nanoTime() & 0x7fffffff));
            }

            int submissionId = submission.getId();
            Path directory = Sandbox.createTempDir(submissionId);
            Sandbox.writeSourceFile(directory, submission.getCode());

            Verdict compileVerdict = compile(directory);
            if (compileVerdict == Verdict.CE) {
                lastResults = List.of(new TestCaseRunner.TestCaseResult(Verdict.CE, "", "Compilation failed.", "", 0));
                submission.setVerdict(Verdict.CE);
                submission.setRuntimeMs(0);
                if (!sampleOnly) {
                    submissionDAO.save(submission);
                }
                return Verdict.CE;
            }

            List<TestCase> testCases = sampleOnly
                    ? testCaseDAO.getSampleByProblemId(submission.getProblemId())
                    : testCaseDAO.getByProblemId(submission.getProblemId());
            Verdict overallVerdict = Verdict.AC;
            List<TestCaseRunner.TestCaseResult> currentResults = new ArrayList<>();

            for (TestCase testCase : testCases) {
                TestCaseRunner.TestCaseResult result = TestCaseRunner.run(testCase, directory);
                currentResults.add(result);
                totalRuntimeMs += result.getRuntimeMs();

                if (result.getVerdict().priority() > overallVerdict.priority()) {
                    overallVerdict = result.getVerdict();
                }

                if (overallVerdict.isTerminal()) {
                    break;
                }
            }

            lastResults = List.copyOf(currentResults);
            submission.setVerdict(overallVerdict);
            submission.setRuntimeMs((int) totalRuntimeMs);
            if (!sampleOnly) {
                submissionDAO.save(submission);
            }
            return overallVerdict;
        } catch (Exception exception) {
            lastResults = List.of(new TestCaseRunner.TestCaseResult(Verdict.RE, "", exception.getMessage(), "", totalRuntimeMs));
            submission.setVerdict(Verdict.RE);
            submission.setRuntimeMs((int) totalRuntimeMs);
            if (!sampleOnly) {
                submissionDAO.save(submission);
            }
            return Verdict.RE;
        } finally {
            Sandbox.cleanup(submission.getId());
        }
    }

    public List<TestCaseRunner.TestCaseResult> getLastResults() {
        return List.copyOf(lastResults);
    }

    private Verdict compile(Path directory) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("javac", "Solution.java");
        processBuilder.directory(directory.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        Thread outputDrainer = new Thread(() -> {
            try (var stream = process.getInputStream()) {
                stream.transferTo(OutputStream.nullOutputStream());
            } catch (IOException ignored) {
            }
        });
        outputDrainer.start();

        boolean finished = process.waitFor(15, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            join(outputDrainer);
            return Verdict.RE;
        }

        join(outputDrainer);
        return process.exitValue() == 0 ? Verdict.AC : Verdict.CE;
    }

    private void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}

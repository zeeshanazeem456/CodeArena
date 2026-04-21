package com.codearena.judge;

import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.TestCaseDAO;
import com.codearena.model.Submission;
import com.codearena.model.TestCase;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JudgeEngine {

    private final TestCaseDAO testCaseDAO;
    private final SubmissionDAO submissionDAO;

    public JudgeEngine(TestCaseDAO testCaseDAO, SubmissionDAO submissionDAO) {
        this.testCaseDAO = testCaseDAO;
        this.submissionDAO = submissionDAO;
    }

    public Verdict runSubmission(Submission submission) {
        return runSubmission(submission, false);
    }

    public Verdict runSubmission(Submission submission, boolean sampleOnly) {
        int submissionId = submission.getId();
        long totalRuntimeMs = 0L;

        try {
            Path directory = Sandbox.createTempDir(submissionId);
            Sandbox.writeSourceFile(directory, submission.getCode());

            Verdict compileVerdict = compile(directory);
            if (compileVerdict == Verdict.CE) {
                submission.setVerdict(Verdict.CE);
                submission.setRuntimeMs(0);
                submissionDAO.save(submission);
                return Verdict.CE;
            }

            List<TestCase> testCases = sampleOnly
                    ? testCaseDAO.getSampleByProblemId(submission.getProblemId())
                    : testCaseDAO.getByProblemId(submission.getProblemId());
            Verdict overallVerdict = Verdict.AC;

            for (TestCase testCase : testCases) {
                TestCaseRunner.TestCaseResult result = TestCaseRunner.run(testCase, directory);
                totalRuntimeMs += result.getRuntimeMs();

                if (result.getVerdict().priority() > overallVerdict.priority()) {
                    overallVerdict = result.getVerdict();
                }

                if (overallVerdict.isTerminal()) {
                    break;
                }
            }

            submission.setVerdict(overallVerdict);
            submission.setRuntimeMs((int) totalRuntimeMs);
            submissionDAO.save(submission);
            return overallVerdict;
        } catch (Exception exception) {
            submission.setVerdict(Verdict.RE);
            submission.setRuntimeMs((int) totalRuntimeMs);
            submissionDAO.save(submission);
            return Verdict.RE;
        } finally {
            Sandbox.cleanup(submissionId);
        }
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

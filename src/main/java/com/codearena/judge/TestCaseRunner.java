package com.codearena.judge;

import com.codearena.model.TestCase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public final class TestCaseRunner {

    private TestCaseRunner() {
    }

    public static TestCaseResult run(TestCase tc, Path workingDir) {
        long startTime = System.currentTimeMillis();
        String expectedOutput = tc.getExpected();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", ".", "Solution");
            processBuilder.directory(workingDir.toFile());

            Process process = processBuilder.start();

            try (Writer writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(tc.getInput() == null ? "" : tc.getInput());
            }

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            Thread stdoutReader = createReaderThread(process.getInputStream(), stdout);
            Thread stderrReader = createReaderThread(process.getErrorStream(), stderr);
            stdoutReader.start();
            stderrReader.start();

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                joinReader(stdoutReader);
                joinReader(stderrReader);
                long runtimeMs = System.currentTimeMillis() - startTime;
                return new TestCaseResult(Verdict.TLE, stdout.toString(), expectedOutput, runtimeMs);
            }

            joinReader(stdoutReader);
            joinReader(stderrReader);

            long runtimeMs = System.currentTimeMillis() - startTime;
            if (process.exitValue() != 0) {
                String actualOutput = stderr.length() > 0 ? stderr.toString() : stdout.toString();
                return new TestCaseResult(Verdict.RE, actualOutput, expectedOutput, runtimeMs);
            }

            String actualOutput = stdout.toString();
            Verdict verdict = actualOutput.trim().equals(expectedOutput == null ? "" : expectedOutput.trim())
                    ? Verdict.AC
                    : Verdict.WA;
            return new TestCaseResult(verdict, actualOutput, expectedOutput, runtimeMs);
        } catch (Exception exception) {
            long runtimeMs = System.currentTimeMillis() - startTime;
            return new TestCaseResult(Verdict.RE, "", expectedOutput, runtimeMs);
        }
    }

    private static Thread createReaderThread(InputStream inputStream, StringBuilder target) {
        return new Thread(() -> readStream(inputStream, target));
    }

    private static void readStream(InputStream inputStream, StringBuilder target) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (!firstLine) {
                    target.append(System.lineSeparator());
                }
                target.append(line);
                firstLine = false;
            }
        } catch (IOException ignored) {
        }
    }

    private static void joinReader(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    public static final class TestCaseResult {

        private final Verdict verdict;
        private final String actualOutput;
        private final String expectedOutput;
        private final long runtimeMs;

        public TestCaseResult(Verdict verdict, String actualOutput, String expectedOutput, long runtimeMs) {
            this.verdict = verdict;
            this.actualOutput = actualOutput;
            this.expectedOutput = expectedOutput;
            this.runtimeMs = runtimeMs;
        }

        public Verdict getVerdict() {
            return verdict;
        }

        public String getActualOutput() {
            return actualOutput;
        }

        public String getExpectedOutput() {
            return expectedOutput;
        }

        public long getRuntimeMs() {
            return runtimeMs;
        }
    }
}

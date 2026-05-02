package com.codearena.service;

import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.TestCaseDAO;
import com.codearena.judge.JudgeEngine;
import com.codearena.judge.TestCaseRunner;
import com.codearena.judge.Verdict;
import com.codearena.model.Submission;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class JudgeService {

    private final JudgeEngine judgeEngine;

    public JudgeService() {
        this(new JudgeEngine(new TestCaseDAO(), new SubmissionDAO()));
    }

    public JudgeService(JudgeEngine judgeEngine) {
        this.judgeEngine = judgeEngine;
    }

    public void evaluateAsync(Submission submission, Consumer<Verdict> onComplete, Consumer<String> onError) {
        evaluateAsync(submission, false, onComplete, onError);
    }

    public void evaluateAsync(Submission submission, boolean sampleOnly,
                              Consumer<Verdict> onComplete, Consumer<String> onError) {
        evaluateReportAsync(submission, sampleOnly,
                report -> {
                    if (onComplete != null) {
                        onComplete.accept(report.getVerdict());
                    }
                },
                onError);
    }

    public void evaluateReportAsync(Submission submission, boolean sampleOnly,
                                    Consumer<JudgeReport> onComplete, Consumer<String> onError) {
        Task<JudgeReport> judgeTask = new Task<>() {
            @Override
            protected JudgeReport call() {
                Verdict verdict = judgeEngine.runSubmission(submission, sampleOnly);
                return new JudgeReport(verdict, judgeEngine.getLastResults());
            }
        };

        judgeTask.setOnSucceeded(event -> Platform.runLater(() -> {
            if (onComplete != null) {
                onComplete.accept(judgeTask.getValue());
            }
        }));

        judgeTask.setOnFailed(event -> Platform.runLater(() -> {
            Throwable exception = judgeTask.getException();
            String message = exception == null ? "Judge execution failed." : exception.getMessage();
            if (onError != null) {
                onError.accept(message);
            }
        }));

        Thread worker = new Thread(judgeTask, "judge-service-worker");
        worker.setDaemon(true);
        worker.start();
    }

    public static final class JudgeReport {
        private final Verdict verdict;
        private final List<TestCaseRunner.TestCaseResult> results;

        public JudgeReport(Verdict verdict, List<TestCaseRunner.TestCaseResult> results) {
            this.verdict = verdict;
            this.results = results == null ? List.of() : List.copyOf(results);
        }

        public Verdict getVerdict() {
            return verdict;
        }

        public List<TestCaseRunner.TestCaseResult> getResults() {
            return results;
        }
    }
}

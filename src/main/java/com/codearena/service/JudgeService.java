package com.codearena.service;

import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.TestCaseDAO;
import com.codearena.judge.JudgeEngine;
import com.codearena.judge.Verdict;
import com.codearena.model.Submission;
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
        Task<Verdict> judgeTask = new Task<>() {
            @Override
            protected Verdict call() {
                return judgeEngine.runSubmission(submission, sampleOnly);
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
}

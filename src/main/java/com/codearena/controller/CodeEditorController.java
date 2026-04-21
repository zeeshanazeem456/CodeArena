package com.codearena.controller;

import com.codearena.judge.Verdict;
import com.codearena.model.Problem;
import com.codearena.model.Submission;
import com.codearena.model.User;
import com.codearena.service.JudgeService;
import com.codearena.util.NavigationUtil;
import com.codearena.util.SessionManager;
import com.codearena.util.XPCalculator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class CodeEditorController {

    private static final String JAVA_TEMPLATE = """
            import java.util.Scanner;
            public class Solution {
                public static void main(String[] args) {
                    Scanner sc = new Scanner(System.in);
                    // your code here
                }
            }
            """;

    private static Problem selectedProblem;

    private final JudgeService judgeService;

    @FXML
    private Label problemTitleLabel;

    @FXML
    private Label difficultyLabel;

    @FXML
    private TextArea codeArea;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox resultsBox;

    @FXML
    private Label verdictLabel;

    @FXML
    private Button runButton;

    @FXML
    private Button submitButton;

    public CodeEditorController() {
        this(new JudgeService());
    }

    public CodeEditorController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    public static void setSelectedProblem(Problem problem) {
        selectedProblem = problem;
    }

    @FXML
    private void initialize() {
        verdictLabel.setVisible(false);
        verdictLabel.setManaged(false);
        statusLabel.setText("");
        resultsBox.getChildren().clear();

        if (selectedProblem != null) {
            problemTitleLabel.setText(selectedProblem.getTitle());
            if (selectedProblem.getDifficulty() != null) {
                difficultyLabel.setText(selectedProblem.getDifficulty().getLabel());
                applyDifficultyStyle();
            } else {
                difficultyLabel.setText("");
            }
        }

        if (codeArea.getText() == null || codeArea.getText().isBlank()) {
            codeArea.setText(JAVA_TEMPLATE);
        }
    }

    @FXML
    private void handleRun() {
        runEvaluation(true);
    }

    @FXML
    private void handleSubmit() {
        runEvaluation(false);
    }

    @FXML
    private void goToProblemDetail(ActionEvent event) {
        try {
            NavigationUtil.navigateTo("problem-detail.fxml", event);
        } catch (Exception exception) {
            statusLabel.setText("Unable to return to problem details.");
        }
    }

    private void runEvaluation(boolean sampleOnly) {
        try {
            if (codeArea.getText() == null || codeArea.getText().trim().isEmpty()) {
                statusLabel.setText("Code cannot be empty.");
                return;
            }

            if (selectedProblem == null || SessionManager.getCurrentUser() == null) {
                statusLabel.setText("Problem or user session is missing.");
                return;
            }

            Submission submission = buildSubmission();
            statusLabel.setText(sampleOnly ? "Running..." : "Judging...");
            verdictLabel.setVisible(false);
            verdictLabel.setManaged(false);
            resultsBox.getChildren().clear();
            setButtonsDisabled(true);

            judgeService.evaluateAsync(
                    submission,
                    sampleOnly,
                    verdict -> {
                        displayResults(verdict);
                        if (!sampleOnly && verdict == Verdict.AC) {
                            awardXp();
                        }
                    },
                    error -> {
                        statusLabel.setText(error == null || error.isBlank() ? "Judge execution failed." : error);
                        setButtonsDisabled(false);
                    }
            );
        } catch (Exception exception) {
            statusLabel.setText(exception.getMessage() == null ? "Unable to start judging." : exception.getMessage());
            setButtonsDisabled(false);
        }
    }

    private Submission buildSubmission() {
        User currentUser = SessionManager.getCurrentUser();

        Submission submission = new Submission();
        submission.setUserId(currentUser.getId());
        submission.setProblemId(selectedProblem.getId());
        submission.setCode(codeArea.getText());
        submission.setLanguage("Java");
        submission.setVerdict(null);
        return submission;
    }

    private void displayResults(Verdict verdict) {
        verdictLabel.setText(verdict.getDisplayName());
        verdictLabel.setStyle("-fx-text-fill: " + verdict.getColor() + "; -fx-font-weight: bold;");
        verdictLabel.setVisible(true);
        verdictLabel.setManaged(true);
        statusLabel.setText("");
        setButtonsDisabled(false);

        Label resultCard = new Label("Result: " + verdict.getDisplayName());
        resultCard.setStyle("-fx-border-color: " + verdict.getColor() + "; -fx-padding: 10; -fx-background-radius: 6;");
        resultsBox.getChildren().setAll(resultCard);
    }

    private void setButtonsDisabled(boolean disabled) {
        runButton.setDisable(disabled);
        submitButton.setDisable(disabled);
    }

    private void awardXp() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || selectedProblem == null || selectedProblem.getDifficulty() == null) {
            return;
        }

        int gainedXp = XPCalculator.forSolvingProblem(selectedProblem.getDifficulty().name());
        int updatedXp = currentUser.getXp() + gainedXp;
        currentUser.setXp(updatedXp);
        currentUser.setRankTitle(XPCalculator.getRankTitle(updatedXp));
        SessionManager.setCurrentUser(currentUser);
    }

    private void applyDifficultyStyle() {
        switch (selectedProblem.getDifficulty()) {
            case EASY -> difficultyLabel.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12;");
            case MEDIUM -> difficultyLabel.setStyle("-fx-background-color: #F9A825; -fx-text-fill: black; -fx-padding: 4 10; -fx-background-radius: 12;");
            case HARD -> difficultyLabel.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12;");
        }
    }
}

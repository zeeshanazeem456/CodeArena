package com.codearena.controller;

import com.codearena.model.Problem;
import com.codearena.model.TestCase;
import com.codearena.service.ProblemService;
import com.codearena.util.NavigationUtil;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class ProblemDetailController {

    private static Problem selectedProblem;

    private final ProblemService problemService;

    @FXML
    private Label titleLabel;

    @FXML
    private Label difficultyLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label tagsLabel;

    @FXML
    private ScrollPane descriptionArea;

    @FXML
    private VBox sampleTestCasesBox;

    public ProblemDetailController() {
        this(new ProblemService());
    }

    public ProblemDetailController(ProblemService problemService) {
        this.problemService = problemService;
    }

    public static void setSelectedProblem(Problem problem) {
        selectedProblem = problem;
    }

    @FXML
    private void initialize() {
        try {
            if (selectedProblem == null) {
                return;
            }

            titleLabel.setText(selectedProblem.getTitle());
            difficultyLabel.setText(selectedProblem.getDifficulty() == null ? "" : selectedProblem.getDifficulty().getLabel());
            applyDifficultyStyle();
            categoryLabel.setText(selectedProblem.getCategory() == null ? "Category: -" : "Category: " + selectedProblem.getCategory());
            tagsLabel.setText(selectedProblem.getTags() == null || selectedProblem.getTags().isBlank()
                    ? "Tags: -"
                    : "Tags: " + selectedProblem.getTags());

            Label descriptionLabel = new Label(selectedProblem.getDescription());
            descriptionLabel.setWrapText(true);
            descriptionLabel.setPadding(new Insets(8));
            descriptionArea.setContent(descriptionLabel);

            renderSampleTestCases(problemService.getSampleTestCases(selectedProblem.getId()));
        } catch (Exception exception) {
            sampleTestCasesBox.getChildren().setAll(new Label("Unable to load problem details."));
        }
    }

    @FXML
    private void goToEditor(ActionEvent event) {
        try {
            CodeEditorController.setSelectedProblem(selectedProblem);
            NavigationUtil.navigateTo("code-editor.fxml", event);
        } catch (Exception exception) {
            sampleTestCasesBox.getChildren().setAll(new Label("Unable to open the editor."));
        }
    }

    @FXML
    private void goToProblemList(ActionEvent event) {
        try {
            NavigationUtil.navigateTo("problem-list.fxml", event);
        } catch (Exception exception) {
            sampleTestCasesBox.getChildren().setAll(new Label("Unable to return to the problem list."));
        }
    }

    private void renderSampleTestCases(List<TestCase> testCases) {
        sampleTestCasesBox.getChildren().clear();

        if (testCases.isEmpty()) {
            sampleTestCasesBox.getChildren().add(new Label("No sample test cases available."));
            return;
        }

        int index = 1;
        for (TestCase testCase : testCases) {
            VBox card = new VBox(6);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-border-color: #D0D0D0; -fx-border-radius: 6; -fx-background-radius: 6;");

            Label header = new Label("Sample " + index++);
            header.setStyle("-fx-font-weight: bold;");

            Label inputLabel = new Label("Input:\n" + testCase.getInput());
            inputLabel.setWrapText(true);

            Label outputLabel = new Label("Output:\n" + testCase.getExpected());
            outputLabel.setWrapText(true);

            card.getChildren().addAll(header, inputLabel, outputLabel);
            sampleTestCasesBox.getChildren().add(card);
        }
    }

    private void applyDifficultyStyle() {
        if (selectedProblem.getDifficulty() == null) {
            return;
        }

        switch (selectedProblem.getDifficulty()) {
            case EASY -> difficultyLabel.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12;");
            case MEDIUM -> difficultyLabel.setStyle("-fx-background-color: #F9A825; -fx-text-fill: black; -fx-padding: 4 10; -fx-background-radius: 12;");
            case HARD -> difficultyLabel.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12;");
        }
    }
}

package com.codearena.controller;

import com.codearena.model.Problem;
import com.codearena.model.User;
import com.codearena.service.ProblemService;
import com.codearena.util.NavigationUtil;
import com.codearena.util.SessionManager;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;

public class ProblemListController implements Initializable {

    private final ProblemService problemService;
    private final Map<Integer, Boolean> solvedStatusByProblemId = new HashMap<>();
    private final AtomicReference<Map<Integer, Boolean>> pendingSolvedStatuses =
            new AtomicReference<>(Collections.emptyMap());

    @FXML
    private TableView<Problem> problemTable;

    @FXML
    private TableColumn<Problem, Number> numberColumn;

    @FXML
    private TableColumn<Problem, String> titleColumn;

    @FXML
    private TableColumn<Problem, String> difficultyColumn;

    @FXML
    private TableColumn<Problem, String> statusColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ChoiceBox<String> difficultyFilter;

    @FXML
    private ProgressIndicator loadingSpinner;

    @FXML
    private Label errorLabel;

    public ProblemListController() {
        this(new ProblemService());
    }

    public ProblemListController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTable();
        configureFilters();
        loadProblemsAsync();
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            NavigationUtil.navigateTo("dashboard.fxml", event);
        } catch (Exception exception) {
            showError("Unable to return to dashboard.");
        }
    }

    private void configureTable() {
        numberColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        titleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitle()));
        difficultyColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
                cellData.getValue().getDifficulty() == null ? "" : cellData.getValue().getDifficulty().getLabel()
        ));
        statusColumn.setCellValueFactory(cellData -> {
            boolean solved = solvedStatusByProblemId.getOrDefault(cellData.getValue().getId(), false);
            return new ReadOnlyStringWrapper(solved ? "Solved \u2713" : "\u2014");
        });

        problemTable.setRowFactory(tableView -> {
            TableRow<Problem> row = new TableRow<>() {
                @Override
                protected void updateItem(Problem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    } else if (solvedStatusByProblemId.getOrDefault(item.getId(), false)) {
                        setStyle("-fx-background-color: #E8F5E9;");
                    } else {
                        setStyle("");
                    }
                }
            };

            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    try {
                        ProblemDetailController.setSelectedProblem(row.getItem());
                        NavigationUtil.navigateTo("problem-detail.fxml", problemTable);
                    } catch (Exception exception) {
                        showError("Unable to open the selected problem.");
                    }
                }
            });

            return row;
        });
    }

    private void configureFilters() {
        difficultyFilter.setItems(FXCollections.observableArrayList("All", "Easy", "Medium", "Hard"));
        difficultyFilter.setValue("All");

        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                loadProblemsAsync(newValue, difficultyFilter.getValue()));

        difficultyFilter.valueProperty().addListener((observable, oldValue, newValue) ->
                loadProblemsAsync(searchField.getText(), newValue));
    }

    private void loadProblemsAsync() {
        loadProblemsAsync(searchField.getText(), difficultyFilter.getValue());
    }

    private void loadProblemsAsync(String keyword, String difficulty) {
        hideError();
        String currentKeyword = keyword == null ? "" : keyword;
        String currentDifficulty = difficulty == null ? "All" : difficulty;

        Task<List<Problem>> loadTask = new Task<>() {
            @Override
            protected List<Problem> call() {
                List<Problem> problems = problemService.getFilteredProblems(currentKeyword, currentDifficulty);

                Map<Integer, Boolean> solvedStatuses = new HashMap<>();
                User currentUser = SessionManager.getCurrentUser();
                int userId = currentUser == null ? 0 : currentUser.getId();

                for (Problem problem : problems) {
                    boolean solved = userId > 0 && problemService.isSolvedByUser(problem.getId(), userId);
                    solvedStatuses.put(problem.getId(), solved);
                }

                pendingSolvedStatuses.set(solvedStatuses);
                return problems;
            }
        };

        loadTask.setOnRunning(event -> loadingSpinner.setVisible(true));
        loadTask.setOnSucceeded(event -> {
            solvedStatusByProblemId.clear();
            solvedStatusByProblemId.putAll(pendingSolvedStatuses.get());
            problemTable.setItems(FXCollections.observableArrayList(loadTask.getValue()));
            problemTable.refresh();
            loadingSpinner.setVisible(false);
        });
        loadTask.setOnFailed(event -> {
            loadingSpinner.setVisible(false);
            Throwable exception = loadTask.getException();
            showError(exception == null ? "Unable to load problems." : exception.getMessage());
        });

        Thread worker = new Thread(loadTask, "problem-list-loader");
        worker.setDaemon(true);
        worker.start();
    }

    private void showError(String message) {
        Runnable uiUpdate = () -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        };

        if (Platform.isFxApplicationThread()) {
            uiUpdate.run();
        } else {
            Platform.runLater(uiUpdate);
        }
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}

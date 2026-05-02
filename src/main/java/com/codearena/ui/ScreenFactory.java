package com.codearena.ui;

import com.codearena.judge.Verdict;
import com.codearena.model.Battle;
import com.codearena.model.Difficulty;
import com.codearena.model.Problem;
import com.codearena.model.Submission;
import com.codearena.model.TestCase;
import com.codearena.model.User;
import com.codearena.service.AdminService;
import com.codearena.service.AnalyticsService;
import com.codearena.service.AuthService;
import com.codearena.service.BattleService;
import com.codearena.service.JudgeService;
import com.codearena.service.LeaderboardService;
import com.codearena.service.ProblemService;
import com.codearena.service.ProfileService;
import com.codearena.service.SquadService;
import com.codearena.service.UserProgressService;
import com.codearena.util.AuthException;
import com.codearena.util.NavigationUtil;
import com.codearena.util.SessionManager;
import com.codearena.util.XPCalculator;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Duration;

public final class ScreenFactory {

    private static final String TEMPLATE = """
            import java.util.Scanner;
            public class Solution {
                public static void main(String[] args) {
                    Scanner sc = new Scanner(System.in);
                    // your code here
                }
            }
            """;

    private static Problem selectedProblem;
    private static Battle selectedBattle;

    private ScreenFactory() {
    }

    public static Parent create(String screenName) {
        return switch (normalize(screenName)) {
            case "login" -> login();
            case "register" -> register();
            case "dashboard" -> dashboard();
            case "problem-list" -> problemList();
            case "problem-detail" -> problemDetail();
            case "code-editor" -> codeEditor();
            case "leaderboard" -> leaderboard();
            case "profile" -> profile();
            case "squad" -> squad();
            case "battle-lobby" -> battleLobby();
            case "battle-1v1" -> battleOneVsOne();
            case "battle-ffa" -> battleFreeForAll();
            case "battle-random" -> battleRandomOneVsOne();
            case "battle-arena" -> battleArena();
            case "admin-panel" -> adminPanel();
            default -> missing(screenName);
        };
    }

    private static String normalize(String screenName) {
        return screenName == null ? "" : screenName.trim();
    }

    private static Parent login() {
        AuthService authService = new AuthService();
        VBox root = page();
        root.setAlignment(Pos.CENTER);

        Label title = h1("CodeArena");
        TextField username = input("Username");
        PasswordField password = passwordInput("Password");
        Label message = errorLabel();
        message.setText(NavigationUtil.consumeFlashMessage() == null ? "" : "Registration successful. Please log in.");

        Button login = primaryButton("Login");
        login.setOnAction(event -> {
            try {
                User user = authService.login(username.getText(), password.getText());
                SessionManager.setCurrentUser(user);
                NavigationUtil.navigateTo("ADMIN".equalsIgnoreCase(user.getRole()) ? "admin-panel" : "dashboard", login);
            } catch (AuthException exception) {
                message.setText(exception.getMessage());
            }
        });

        Hyperlink register = new Hyperlink("Create an account");
        register.setOnAction(event -> NavigationUtil.navigateTo("register", register));
        Button browseProblems = secondaryButton("Browse Problems as Guest");
        browseProblems.setOnAction(event -> NavigationUtil.navigateTo("problem-list", browseProblems));
        Button publicLeaderboard = secondaryButton("View Public Leaderboard");
        publicLeaderboard.setOnAction(event -> NavigationUtil.navigateTo("leaderboard", publicLeaderboard));
        HBox guestLinks = new HBox(10, browseProblems, publicLeaderboard);
        guestLinks.setAlignment(Pos.CENTER);
        root.getChildren().addAll(title, label("Login"), username, password, login, register, guestLinks, message);
        return root;
    }

    private static Parent register() {
        AuthService authService = new AuthService();
        VBox root = page();
        root.setAlignment(Pos.CENTER);

        TextField username = input("Username");
        TextField email = input("Email");
        PasswordField password = passwordInput("Password");
        PasswordField confirm = passwordInput("Confirm password");
        Label message = errorLabel();

        Button submit = primaryButton("Register");
        submit.setOnAction(event -> {
            if (!password.getText().equals(confirm.getText())) {
                message.setText("Passwords do not match.");
                return;
            }
            try {
                authService.register(username.getText(), email.getText(), password.getText());
                NavigationUtil.setFlashMessage("Registration successful. Please log in.");
                NavigationUtil.navigateTo("login", submit);
            } catch (AuthException exception) {
                message.setText(exception.getMessage());
            }
        });

        Hyperlink back = new Hyperlink("Back to login");
        back.setOnAction(event -> NavigationUtil.navigateTo("login", back));
        root.getChildren().addAll(h1("Create Account"), username, email, password, confirm, submit, back, message);
        return root;
    }

    private static Parent dashboard() {
        if (!SessionManager.isLoggedIn()) {
            return guestAccessMessage("Please log in to open your dashboard.");
        }
        ProfileService profileService = new ProfileService();
        User current = SessionManager.getCurrentUser();
        if (current != null) {
            User refreshed = profileService.getUser(current.getId());
            if (refreshed != null) {
                SessionManager.setCurrentUser(refreshed);
                current = refreshed;
            }
        }

        VBox root = page();
        root.setAlignment(Pos.CENTER);
        Label title = h1(current == null ? "Dashboard" : "Welcome, " + current.getUsername());
        Label rank = label(current == null ? "" : current.getRankTitle() + " | " + current.getXp() + " XP");
        ProgressBar progress = new ProgressBar(0);
        progress.setPrefWidth(420);
        if (current != null) {
            int next = XPCalculator.nextRankThreshold(current.getXp());
            progress.setProgress(next == current.getXp() ? 1.0 : Math.min(1.0, current.getXp() / (double) next));
        }
        Label stats = label(current == null ? "" : "Solved: " + current.getProblemsSolved()
                + " | Battles: " + current.getBattlesWon() + "W / " + current.getBattlesLost() + "L"
                + " | Streak: " + current.getStreakDays());

        HBox nav = new HBox(10,
                navButton("Problems", "problem-list"),
                navButton("Leaderboard", "leaderboard"),
                navButton("Battle", "battle-lobby"),
                navButton("Squad", "squad"),
                navButton("Profile", "profile")
        );
        nav.setAlignment(Pos.CENTER);
        Button logout = primaryButton("Logout");
        logout.setOnAction(event -> {
            new AuthService().logout();
            NavigationUtil.navigateTo("login", logout);
        });
        root.getChildren().addAll(title, rank, progress, stats, nav, logout);
        return root;
    }

    private static Parent problemList() {
        ProblemService problemService = new ProblemService();
        BorderPane root = shell("Problem List");
        TableView<Problem> table = new TableView<>();
        table.setPlaceholder(label("No active problems yet. New challenges are coming soon."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        TableColumn<Problem, Number> id = column("#", 70);
        id.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        TableColumn<Problem, String> title = stringColumn("Title", 280, problem -> problem.getTitle());
        TableColumn<Problem, String> difficulty = stringColumn("Difficulty", 120,
                problem -> problem.getDifficulty() == null ? "" : problem.getDifficulty().getLabel());
        TableColumn<Problem, String> category = stringColumn("Category", 140, Problem::getCategory);
        TableColumn<Problem, String> acceptance = stringColumn("Acceptance", 120,
                problem -> String.format("%.1f%%", problemService.getAcceptanceRate(problem.getId())));
        TableColumn<Problem, String> status = stringColumn("Status", 120, problem -> {
            User user = SessionManager.getCurrentUser();
            return user == null ? "-" : problemService.isSolvedByUser(problem.getId(), user.getId()) ? "✓" : "—";
        });
        status.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : value);
                getStyleClass().removeAll("text-success", "text-muted");
                styled(this, "✓".equals(value) ? "text-success" : "text-muted");
            }
        });
        TableColumn<Problem, String> plainStatus = stringColumn("Status", 120, problem -> {
            User user = SessionManager.getCurrentUser();
            return user == null ? "Login to track" : problemService.isSolvedByUser(problem.getId(), user.getId()) ? "Solved" : "-";
        });
        table.getColumns().addAll(id, title, difficulty, category, acceptance, plainStatus);

        TextField search = input("Search problems...");
        search.setPromptText("Search problems...");
        ChoiceBox<String> diff = new ChoiceBox<>(FXCollections.observableArrayList("All", "Easy", "Medium", "Hard"));
        diff.setValue("All");
        TextField tag = input("Category or tag...");
        tag.setPromptText("Category or tag...");
        ProgressIndicator loading = new ProgressIndicator();
        loading.setVisible(false);
        Label error = errorLabel();

        Runnable refresh = () -> {
            try {
                table.setItems(FXCollections.observableArrayList(
                        problemService.getFilteredProblems(search.getText(), diff.getValue(), tag.getText())));
                error.setText("");
            } catch (Exception exception) {
                error.setText(exception.getMessage());
            }
        };
        search.textProperty().addListener((obs, old, value) -> refresh.run());
        diff.valueProperty().addListener((obs, old, value) -> refresh.run());
        tag.textProperty().addListener((obs, old, value) -> refresh.run());
        table.setRowFactory(view -> {
            TableRow<Problem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    selectedProblem = row.getItem();
                    NavigationUtil.navigateTo("problem-detail", table);
                }
            });
            return row;
        });
        refresh.run();

        HBox filters = new HBox(10, search, diff, tag, publicBackButton());
        filters.setPadding(new Insets(0, 20, 12, 20));
        VBox top = new VBox(10, header("Problem List"), filters, error);
        root.setTop(top);
        VBox center = new VBox(loading, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        root.setCenter(center);
        return root;
    }

    private static Parent problemDetail() {
        ProblemService problemService = new ProblemService();
        BorderPane root = shell(selectedProblem == null ? "Problem" : selectedProblem.getTitle());
        if (selectedProblem == null) {
            root.setCenter(label("No problem selected."));
            return root;
        }

        VBox content = contentPage();
        content.setAlignment(Pos.TOP_LEFT);
        Label title = h1(selectedProblem.getTitle());
        Label difficulty = badge(selectedProblem.getDifficulty() == null ? "" : selectedProblem.getDifficulty().getLabel());
        Label meta = mutedLabel("Category: " + blank(selectedProblem.getCategory()) + " | Tags: " + blank(selectedProblem.getTags()));
        Label description = label(decodeDisplayText(selectedProblem.getDescription()));
        description.setWrapText(true);
        description.setMaxWidth(Double.MAX_VALUE);
        VBox statementSections = new VBox(12,
                section("Problem Statement", description),
                detailSection("Constraints", selectedProblem.getConstraints()),
                detailSection("Input Format", selectedProblem.getInputFormat()),
                detailSection("Output Format", selectedProblem.getOutputFormat()));
        statementSections.setMaxWidth(Double.MAX_VALUE);
        VBox samples = new VBox(8);
        samples.setMaxWidth(Double.MAX_VALUE);
        for (TestCase testCase : problemService.getSampleTestCases(selectedProblem.getId())) {
            samples.getChildren().add(outputCard("Sample", testCase.getInput(), testCase.getExpected(), null));
        }
        Node actionButtons = problemDetailActions();
        content.getChildren().addAll(new HBox(10, title, difficulty), meta, statementSections, h2("Sample Test Cases"), samples,
                actionButtons);
        root.setCenter(fitScroll(content));
        return root;
    }

    private static Parent codeEditor() {
        if (!SessionManager.isLoggedIn()) {
            return guestAccessMessage("Log in or create an account to solve problems.");
        }
        JudgeService judgeService = new JudgeService();
        ProblemService problemService = new ProblemService();
        UserProgressService progressService = new UserProgressService();
        BorderPane root = shell("Code Editor");
        VBox top = new VBox(10);
        top.setPadding(new Insets(0, 0, 12, 0));

        Label title = h1(selectedProblem == null ? "Code Editor" : selectedProblem.getTitle());
        Label difficulty = badge(selectedProblem == null || selectedProblem.getDifficulty() == null ? "" : selectedProblem.getDifficulty().getLabel());
        top.getChildren().addAll(new HBox(10, title, difficulty), label("Language: Java"));

        TextArea code = new TextArea(TEMPLATE);
        code.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace;");
        code.setPrefRowCount(22);
        code.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        code.setWrapText(false);

        Label status = errorLabel();
        Label verdict = new Label();
        verdict.setMaxWidth(Double.MAX_VALUE);
        VBox results = new VBox(10);
        results.setMaxWidth(Double.MAX_VALUE);
        Button run = primaryButton("Run");
        Button submit = primaryButton("Submit");
        Button back = backButton("Back", "problem-detail");

        Runnable[] evaluate = new Runnable[2];
        evaluate[0] = () -> runJudge(true, code, status, verdict, results, run, submit, judgeService, problemService, progressService);
        evaluate[1] = () -> runJudge(false, code, status, verdict, results, run, submit, judgeService, problemService, progressService);
        run.setOnAction(event -> evaluate[0].run());
        submit.setOnAction(event -> evaluate[1].run());

        VBox bottom = new VBox(10, new HBox(10, run, submit, back), status, verdict, results);
        bottom.setPadding(new Insets(12, 0, 0, 0));
        ScrollPane resultScroll = fitScroll(bottom);
        resultScroll.setPrefViewportHeight(260);

        root.setTop(top);
        root.setCenter(code);
        root.setBottom(resultScroll);
        return root;
    }

    private static Parent leaderboard() {
        LeaderboardService service = new LeaderboardService();
        BorderPane root = shell("Leaderboard");
        TableView<User> table = new TableView<>();
        table.setPlaceholder(label("The leaderboard is not available yet."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        TableColumn<User, Number> pos = column("Position", 90);
        pos.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(table.getItems().indexOf(cell.getValue()) + 1));
        table.getColumns().addAll(pos,
                stringColumn("Username", 240, User::getUsername),
                stringColumn("Rank", 160, User::getRankTitle),
                numberColumn("XP", 110, User::getXp),
                numberColumn("Solved", 110, User::getProblemsSolved),
                numberColumn("Battles Won", 130, User::getBattlesWon));
        table.setItems(FXCollections.observableArrayList(service.getRankedUsers()));
        root.setTop(new VBox(10, header("Public Leaderboard"), publicBackButton()));
        root.setCenter(table);
        return root;
    }

    private static Parent profile() {
        if (!SessionManager.isLoggedIn()) {
            return guestAccessMessage("Please log in to manage your profile.");
        }
        ProfileService service = new ProfileService();
        User current = SessionManager.getCurrentUser();
        User user = current == null ? null : service.getUser(current.getId());
        if (user != null) {
            SessionManager.setCurrentUser(user);
        }

        BorderPane root = shell("Profile");
        VBox top = new VBox(8, h1(user == null ? "Profile" : user.getUsername() + " | " + user.getRankTitle()),
                label(user == null ? "" : user.getXp() + " XP | Solved " + user.getProblemsSolved()
                        + " | Battles " + user.getBattlesWon() + "W / " + user.getBattlesLost() + "L"),
                backButton("Back", "dashboard"));
        top.setPadding(new Insets(20));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Submissions", submissionsTable(user == null ? List.of() : service.getSubmissionHistory(user.getId()), service)));
        tabs.getTabs().add(new Tab("Battles", battlesTable(user == null ? List.of() : service.getBattleHistory(user.getId()), service)));
        tabs.getTabs().add(new Tab("Edit", profileEditor(user, service)));
        root.setTop(top);
        root.setCenter(tabs);
        return root;
    }

    private static Parent squad() {
        if (!SessionManager.isLoggedIn()) {
            return guestAccessMessage("Please log in to create or join a squad.");
        }
        SquadService service = new SquadService();
        BorderPane root = shell("Squad");
        Label title = h1("Squad");
        Label message = errorLabel();
        TextField name = input("Squad name");
        TextField description = input("Description");
        TextField join = input("Join by squad name");

        TableView<User> members = new TableView<>();
        members.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        members.getColumns().addAll(stringColumn("Username", 220, User::getUsername),
                stringColumn("Rank", 160, User::getRankTitle), numberColumn("XP", 120, User::getXp));
        members.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        TableView<com.codearena.model.Squad> squads = new TableView<>();
        squads.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        squads.getColumns().addAll(stringColumn("Squad", 240, com.codearena.model.Squad::getName),
                numberColumn("Combined XP", 140, service::getCombinedXp));
        squads.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Runnable refresh = () -> {
            var squad = service.getCurrentUserSquad(SessionManager.getCurrentUser());
            title.setText(squad == null ? "No Squad Yet" : squad.getName() + " | Combined XP: " + service.getCombinedXp(squad));
            members.setItems(FXCollections.observableArrayList(service.getMembers(squad)));
            squads.setItems(FXCollections.observableArrayList(service.getSquadLeaderboard()));
        };
        Button create = primaryButton("Create");
        create.setOnAction(event -> runUi(message, () -> {
            service.createSquad(SessionManager.getCurrentUser(), name.getText(), description.getText());
            refresh.run();
        }));
        Button joinButton = primaryButton("Join");
        joinButton.setOnAction(event -> runUi(message, () -> {
            service.joinSquad(SessionManager.getCurrentUser(), join.getText());
            refresh.run();
        }));
        Button leave = primaryButton("Leave");
        leave.setOnAction(event -> runUi(message, () -> {
            service.leaveSquad(SessionManager.getCurrentUser());
            refresh.run();
        }));
        Button remove = primaryButton("Remove Selected Member");
        remove.setOnAction(event -> runUi(message, () -> {
            User selected = members.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.removeMember(SessionManager.getCurrentUser(), selected.getId());
                refresh.run();
            }
        }));
        refresh.run();

        HBox forms = new HBox(10, name, description, create, join, joinButton, leave, backButton("Back", "dashboard"));
        forms.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(name, Priority.ALWAYS);
        HBox.setHgrow(description, Priority.ALWAYS);
        HBox.setHgrow(join, Priority.ALWAYS);
        name.setMaxWidth(Double.MAX_VALUE);
        description.setMaxWidth(Double.MAX_VALUE);
        join.setMaxWidth(Double.MAX_VALUE);

        VBox top = new VBox(14, title, forms, message);
        top.setPadding(new Insets(0, 0, 16, 0));

        VBox membersPanel = new VBox(10, h2("Members"), members, remove);
        VBox leaderboardPanel = new VBox(10, h2("Squad Leaderboard"), squads);
        membersPanel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        leaderboardPanel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(members, Priority.ALWAYS);
        VBox.setVgrow(squads, Priority.ALWAYS);

        HBox center = new HBox(16, membersPanel, leaderboardPanel);
        center.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(membersPanel, Priority.ALWAYS);
        HBox.setHgrow(leaderboardPanel, Priority.ALWAYS);

        root.setTop(top);
        root.setCenter(center);
        return root;
    }

    private static Parent battleLobby() {
        if (!SessionManager.isLoggedIn()) {
            return guestAccessMessage("Please log in to start battles.");
        }
        BorderPane root = shell("Battlefield");
        VBox modes = new VBox(16);
        modes.setPadding(new Insets(18));
        modes.setMaxWidth(560);
        modes.getChildren().addAll(
                battleModeButton("1v1", "Create or join a private code match.", "battle-1v1", false),
                battleModeButton("Free for all", "Share one code with a group. Creator starts the battle.", "battle-ffa", false),
                battleModeButton("Random Match 1v1", "Wait for another coder choosing the same difficulty.", "battle-random", false),
                backButton("Back", "dashboard")
        );
        root.setTop(header("Choose Battle Mode"));
        root.setCenter(modes);
        return root;
    }

    private static Parent battleOneVsOne() {
        if (!SessionManager.isLoggedIn()) {
            return guestAccessMessage("Please log in to start battles.");
        }
        BattleService service = new BattleService();
        BorderPane root = shell("1v1 Battle");
        int currentId = SessionManager.getCurrentUser() == null ? 0 : SessionManager.getCurrentUser().getId();
        ChoiceBox<String> difficulty = new ChoiceBox<>(FXCollections.observableArrayList("Easy", "Medium", "Hard"));
        difficulty.setValue("Easy");
        Label message = errorLabel();
        TextField joinCode = input("Enter battle code");
        Label codeValue = h1("No code yet");
        codeValue.setStyle("-fx-font-size: 34px; -fx-font-weight: bold;");
        Label createState = label("Choose difficulty, create a match, then share the code with your friend.");
        createState.setWrapText(true);
        Label activeState = label("No active match yet.");
        activeState.setWrapText(true);
        Label pollState = mutedLabel("");
        final Battle[] latestActiveBattle = new Battle[1];
        final Battle[] createdRoom = new Battle[1];
        final Timeline[] roomPoller = new Timeline[1];
        Button enterBattle = primaryButton("Enter Match");
        enterBattle.setDisable(true);

        Button createRoom = primaryButton("Create Match");
        createRoom.setOnAction(event -> runUi(message, () -> {
            if (roomPoller[0] != null) {
                roomPoller[0].stop();
            }
            Battle room = service.createBattleRoom(currentId, difficulty.getValue());
            createdRoom[0] = room;
            codeValue.setText(room.getJoinCode());
            Problem problem = service.getBattleProblem(room);
            createState.setText("Share this code. Match: " + (problem == null ? "selected problem" : problem.getTitle())
                    + " | Timer: " + formatSeconds(room.getTimeLimit()));
            activeState.setText("Waiting for your friend to join...");
            pollState.setText("Checking automatically every 5 seconds.");
            enterBattle.setDisable(true);
            roomPoller[0] = new Timeline(new KeyFrame(Duration.seconds(5), pollEvent -> {
                Battle refreshed = service.getBattle(createdRoom[0].getId());
                if (refreshed == null) {
                    activeState.setText("This match is no longer available.");
                    pollState.setText("");
                    enterBattle.setDisable(true);
                    roomPoller[0].stop();
                    return;
                }
                if (!"MATCHED".equalsIgnoreCase(refreshed.getStatus()) && !"ACTIVE".equalsIgnoreCase(refreshed.getStatus())) {
                    activeState.setText("Waiting for your friend to join...");
                    return;
                }
                latestActiveBattle[0] = refreshed;
                Problem activeProblem = service.getBattleProblem(refreshed);
                activeState.setText("Match ready vs " + service.getOpponentName(refreshed, currentId)
                        + " | " + (activeProblem == null ? "Problem selected" : activeProblem.getTitle())
                        + ("ACTIVE".equalsIgnoreCase(refreshed.getStatus())
                        ? " | Time left: " + formatSeconds(service.getRemainingSeconds(refreshed))
                        : " | Enter to start when both players are in."));
                pollState.setText("");
                enterBattle.setDisable(false);
                roomPoller[0].stop();
            }));
            roomPoller[0].setCycleCount(Animation.INDEFINITE);
            roomPoller[0].play();
        }));

        Button joinRoom = primaryButton("Join Match");
        joinRoom.setOnAction(event -> runUi(message, () -> {
            selectedBattle = service.joinBattleRoom(joinCode.getText(), currentId);
            NavigationUtil.navigateTo("battle-arena", joinRoom);
        }));

        enterBattle.setOnAction(event -> runUi(message, () -> {
            Battle battle = latestActiveBattle[0];
            if (battle == null) {
                throw new IllegalStateException("No active match is ready yet.");
            }
            if (roomPoller[0] != null) {
                roomPoller[0].stop();
            }
            selectedBattle = service.getBattle(battle.getId());
            NavigationUtil.navigateTo("battle-arena", enterBattle);
        }));

        HBox createActions = new HBox(10, label("Difficulty"), difficulty, createRoom);
        createActions.setAlignment(Pos.CENTER_LEFT);
        VBox createPanel = simplePanel("Create Match", createActions, codeValue, createState);

        HBox joinActions = new HBox(10, joinCode, joinRoom);
        joinActions.setAlignment(Pos.CENTER_LEFT);
        VBox joinPanel = simplePanel("Join Match", joinActions, label("Paste your friend's code here."));

        Button modes = secondaryButton("Modes");
        modes.setOnAction(event -> {
            if (roomPoller[0] != null) {
                roomPoller[0].stop();
            }
            NavigationUtil.navigateTo("battle-lobby", modes);
        });
        HBox readyActions = new HBox(10, enterBattle, modes);
        readyActions.setAlignment(Pos.CENTER_LEFT);
        VBox readyPanel = simplePanel("Ready Match", activeState, pollState, readyActions, message);

        HBox topPanels = new HBox(18, createPanel, joinPanel);
        topPanels.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(createPanel, Priority.ALWAYS);
        HBox.setHgrow(joinPanel, Priority.ALWAYS);

        VBox content = new VBox(20, topPanels, readyPanel);
        content.setPadding(new Insets(18));
        content.setMaxWidth(Double.MAX_VALUE);
        root.setTop(header("1v1 Battle"));
        root.setCenter(content);
        return root;
    }

    private static Parent battleFreeForAll() {
        if (!SessionManager.isLoggedIn()) {
            return guestAccessMessage("Please log in to start battles.");
        }
        BattleService service = new BattleService();
        BorderPane root = shell("Free for All");
        int currentId = SessionManager.getCurrentUser() == null ? 0 : SessionManager.getCurrentUser().getId();
        ChoiceBox<String> difficulty = new ChoiceBox<>(FXCollections.observableArrayList("Easy", "Medium", "Hard"));
        difficulty.setValue("Easy");
        TextField joinCode = input("Enter group code");
        Label message = errorLabel();
        Label codeValue = h1("No code yet");
        codeValue.setStyle("-fx-font-size: 34px; -fx-font-weight: bold;");
        Label createState = label("Create a group match, share the code, then start when players join.");
        createState.setWrapText(true);
        Label joinState = label("Paste a group code to join your friends.");
        joinState.setWrapText(true);
        Label readyState = label("No free for all room yet.");
        readyState.setWrapText(true);
        Label pollState = mutedLabel("");
        final Battle[] createdRoom = new Battle[1];
        final Battle[] latestBattle = new Battle[1];
        final Timeline[] roomPoller = new Timeline[1];

        Button startBattle = primaryButton("Start Battle");
        startBattle.setDisable(true);
        Button enterBattle = primaryButton("Enter Battle");
        enterBattle.setDisable(true);

        Runnable stopPoller = () -> {
            if (roomPoller[0] != null) {
                roomPoller[0].stop();
            }
        };

        Runnable startRoomPoller = () -> {
            stopPoller.run();
            roomPoller[0] = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
                Battle room = latestBattle[0] != null ? latestBattle[0] : createdRoom[0];
                if (room == null) {
                    return;
                }
                Battle refreshed = service.getBattle(room.getId());
                if (refreshed == null) {
                    readyState.setText("This free for all is no longer available.");
                    startBattle.setDisable(true);
                    enterBattle.setDisable(true);
                    stopPoller.run();
                    return;
                }
                latestBattle[0] = refreshed;
                int players = service.getParticipantCount(refreshed);
                if ("ACTIVE".equalsIgnoreCase(refreshed.getStatus())) {
                    Problem problem = service.getBattleProblem(refreshed);
                    readyState.setText("Battle started | " + players + " players | "
                            + (problem == null ? "Problem selected" : problem.getTitle()));
                    pollState.setText("");
                    startBattle.setDisable(true);
                    enterBattle.setDisable(false);
                    stopPoller.run();
                    return;
                }
                readyState.setText(players + " player" + (players == 1 ? "" : "s") + " joined. Creator starts the battle.");
                startBattle.setDisable(createdRoom[0] == null || createdRoom[0].getId() != refreshed.getId() || players < 2);
            }));
            roomPoller[0].setCycleCount(Animation.INDEFINITE);
            roomPoller[0].play();
        };

        Button createRoom = primaryButton("Create Group Code");
        createRoom.setOnAction(event -> runUi(message, () -> {
            Battle room = service.createFreeForAllRoom(currentId, difficulty.getValue());
            createdRoom[0] = room;
            latestBattle[0] = room;
            codeValue.setText(room.getJoinCode());
            Problem problem = service.getBattleProblem(room);
            createState.setText("Share this code. Match: " + (problem == null ? "selected problem" : problem.getTitle())
                    + " | Timer: " + formatSeconds(room.getTimeLimit()));
            readyState.setText("1 player joined. Waiting for the group...");
            pollState.setText("Checking joined players every 5 seconds.");
            startBattle.setDisable(true);
            enterBattle.setDisable(true);
            startRoomPoller.run();
        }));

        Button joinRoom = primaryButton("Join Group");
        joinRoom.setOnAction(event -> runUi(message, () -> {
            Battle room = service.joinFreeForAllRoom(joinCode.getText(), currentId);
            latestBattle[0] = room;
            joinState.setText("Joined. Waiting for the creator to start the battle.");
            readyState.setText("Waiting for battle to start...");
            pollState.setText("Checking battle status every 5 seconds.");
            enterBattle.setDisable(true);
            startRoomPoller.run();
        }));

        startBattle.setOnAction(event -> runUi(message, () -> {
            latestBattle[0] = service.startFreeForAll(createdRoom[0], currentId);
            stopPoller.run();
            selectedBattle = latestBattle[0];
            NavigationUtil.navigateTo("battle-arena", startBattle);
        }));

        enterBattle.setOnAction(event -> runUi(message, () -> {
            if (latestBattle[0] == null) {
                throw new IllegalStateException("No active free for all is ready yet.");
            }
            stopPoller.run();
            selectedBattle = service.getBattle(latestBattle[0].getId());
            NavigationUtil.navigateTo("battle-arena", enterBattle);
        }));

        HBox createActions = new HBox(10, label("Difficulty"), difficulty, createRoom);
        createActions.setAlignment(Pos.CENTER_LEFT);
        VBox createPanel = simplePanel("Create Free for All", createActions, codeValue, createState);

        HBox joinActions = new HBox(10, joinCode, joinRoom);
        joinActions.setAlignment(Pos.CENTER_LEFT);
        VBox joinPanel = simplePanel("Join Free for All", joinActions, joinState);

        Button modes = secondaryButton("Modes");
        modes.setOnAction(event -> {
            stopPoller.run();
            NavigationUtil.navigateTo("battle-lobby", modes);
        });
        HBox readyActions = new HBox(10, startBattle, enterBattle, modes);
        readyActions.setAlignment(Pos.CENTER_LEFT);
        VBox readyPanel = simplePanel("Room Status", readyState, pollState, readyActions, message);

        HBox topPanels = new HBox(18, createPanel, joinPanel);
        topPanels.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(createPanel, Priority.ALWAYS);
        HBox.setHgrow(joinPanel, Priority.ALWAYS);

        VBox content = new VBox(20, topPanels, readyPanel);
        content.setPadding(new Insets(18));
        content.setMaxWidth(Double.MAX_VALUE);
        root.setTop(header("Free for All"));
        root.setCenter(content);
        return root;
    }

    private static Parent battleRandomOneVsOne() {
        if (!SessionManager.isLoggedIn()) {
            return guestAccessMessage("Please log in to start battles.");
        }
        BattleService service = new BattleService();
        BorderPane root = shell("Random Match 1v1");
        int currentId = SessionManager.getCurrentUser() == null ? 0 : SessionManager.getCurrentUser().getId();
        ChoiceBox<String> difficulty = new ChoiceBox<>(FXCollections.observableArrayList("Easy", "Medium", "Hard"));
        difficulty.setValue("Easy");
        Label message = errorLabel();
        Label status = label("Choose a difficulty and wait for another coder.");
        status.setWrapText(true);
        Label pollState = mutedLabel("");
        final Battle[] queuedBattle = new Battle[1];
        final Timeline[] matchPoller = new Timeline[1];
        Button enterBattle = primaryButton("Enter Match");
        enterBattle.setDisable(true);

        Runnable stopPoller = () -> {
            if (matchPoller[0] != null) {
                matchPoller[0].stop();
            }
        };

        Button findMatch = primaryButton("Find Match");
        findMatch.setOnAction(event -> runUi(message, () -> {
            stopPoller.run();
            Battle battle = service.findRandomOneVsOne(currentId, difficulty.getValue());
            queuedBattle[0] = battle;
            if ("MATCHED".equalsIgnoreCase(battle.getStatus()) || "ACTIVE".equalsIgnoreCase(battle.getStatus())) {
                selectedBattle = battle;
                NavigationUtil.navigateTo("battle-arena", findMatch);
                return;
            }

            Problem problem = service.getBattleProblem(battle);
            status.setText("Waiting for another " + difficulty.getValue() + " player"
                    + " | " + (problem == null ? "Problem selected" : problem.getTitle()));
            pollState.setText("Checking automatically every 5 seconds.");
            enterBattle.setDisable(true);
            matchPoller[0] = new Timeline(new KeyFrame(Duration.seconds(5), pollEvent -> {
                Battle refreshed = service.getBattle(queuedBattle[0].getId());
                if (refreshed == null) {
                    status.setText("This random queue entry is no longer available.");
                    pollState.setText("");
                    enterBattle.setDisable(true);
                    stopPoller.run();
                    return;
                }
                if (!"MATCHED".equalsIgnoreCase(refreshed.getStatus()) && !"ACTIVE".equalsIgnoreCase(refreshed.getStatus())) {
                    status.setText("Still waiting for another " + difficulty.getValue() + " player...");
                    return;
                }
                queuedBattle[0] = refreshed;
                status.setText("Match found vs " + service.getOpponentName(refreshed, currentId));
                pollState.setText("");
                enterBattle.setDisable(false);
                stopPoller.run();
            }));
            matchPoller[0].setCycleCount(Animation.INDEFINITE);
            matchPoller[0].play();
        }));

        enterBattle.setOnAction(event -> runUi(message, () -> {
            if (queuedBattle[0] == null) {
                throw new IllegalStateException("No random match is ready yet.");
            }
            stopPoller.run();
            selectedBattle = service.getBattle(queuedBattle[0].getId());
            NavigationUtil.navigateTo("battle-arena", enterBattle);
        }));

        Button modes = secondaryButton("Modes");
        modes.setOnAction(event -> {
            stopPoller.run();
            NavigationUtil.navigateTo("battle-lobby", modes);
        });

        HBox actions = new HBox(10, label("Difficulty"), difficulty, findMatch, enterBattle, modes);
        actions.setAlignment(Pos.CENTER_LEFT);
        VBox panel = simplePanel("Random 1v1 Queue", actions, status, pollState, message);
        panel.setMaxWidth(680);
        VBox content = new VBox(panel);
        content.setPadding(new Insets(18));
        root.setTop(header("Random Match 1v1"));
        root.setCenter(content);
        return root;
    }

    private static Parent battleArena() {
        if (!SessionManager.isLoggedIn()) {
            return guestAccessMessage("Please log in to continue a battle.");
        }
        BattleService battleService = new BattleService();
        JudgeService judgeService = new JudgeService();
        selectedBattle = selectedBattle == null ? null : battleService.getBattle(selectedBattle.getId());
        User currentUser = SessionManager.getCurrentUser();
        if (selectedBattle != null && !battleService.isParticipant(selectedBattle, currentUser.getId())) {
            selectedBattle = null;
        }
        if (selectedBattle != null && "MATCHED".equalsIgnoreCase(selectedBattle.getStatus())) {
            selectedBattle = battleService.markReadyAndStartIfNeeded(selectedBattle, currentUser.getId());
        }
        Problem problem = selectedBattle == null ? null : battleService.getBattleProblem(selectedBattle);
        boolean expiredOnOpen = selectedBattle != null && battleService.expireIfNeeded(selectedBattle);
        VBox root = page();
        root.setAlignment(Pos.TOP_LEFT);
        TextArea code = new TextArea(TEMPLATE);
        code.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace;");
        code.setPrefHeight(430);
        Label status = errorLabel();
        if (selectedBattle != null && "MATCHED".equalsIgnoreCase(selectedBattle.getStatus())) {
            status.setText("Waiting for the other player to enter the battlefield...");
        }
        Label timer = label(expiredOnOpen ? "Battle expired: draw recorded."
                : "ACTIVE".equalsIgnoreCase(selectedBattle == null ? "" : selectedBattle.getStatus())
                ? "Time remaining: " + formatSeconds(battleService.getRemainingSeconds(selectedBattle))
                : "Timer starts when both players are in the battlefield.");
        Label opponent = label(selectedBattle == null ? ""
                : BattleService.MODE_FREE_FOR_ALL.equalsIgnoreCase(battleService.modeOf(selectedBattle))
                ? "Free for all | Players: " + battleService.getParticipantCount(selectedBattle)
                : "Opponent: " + battleService.getOpponentName(selectedBattle, currentUser.getId()));
        Button submit = primaryButton("Submit Battle Code");
        submit.setDisable(expiredOnOpen || selectedBattle == null || problem == null
                || !"ACTIVE".equalsIgnoreCase(selectedBattle.getStatus()));
        Timeline countdown = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (selectedBattle == null) {
                return;
            }
            selectedBattle = battleService.getBattle(selectedBattle.getId());
            if (selectedBattle == null) {
                timer.setText("Battle finished.");
                submit.setDisable(true);
                return;
            }
            if ("MATCHED".equalsIgnoreCase(selectedBattle.getStatus())) {
                timer.setText("Timer starts when both players are in the battlefield.");
                status.setText("Waiting for the other player to enter the battlefield...");
                return;
            }
            if (!"ACTIVE".equalsIgnoreCase(selectedBattle.getStatus())) {
                timer.setText("Battle finished.");
                submit.setDisable(true);
                return;
            }
            submit.setDisable(false);
            status.setText(status.getText() != null && status.getText().startsWith("Waiting for") ? "" : status.getText());
            if (battleService.expireIfNeeded(selectedBattle)) {
                timer.setText("Battle expired: draw recorded.");
                status.setText("Battle time expired. Draw recorded.");
                submit.setDisable(true);
                return;
            }
            timer.setText("Time remaining: " + formatSeconds(battleService.getRemainingSeconds(selectedBattle)));
        }));
        countdown.setCycleCount(Animation.INDEFINITE);
        if (selectedBattle != null && !expiredOnOpen) {
            countdown.play();
        }
        submit.setOnAction(event -> {
            if (selectedBattle == null || problem == null) {
                status.setText("No active battle.");
                return;
            }
            selectedBattle = battleService.getBattle(selectedBattle.getId());
            if (selectedBattle == null || !battleService.isParticipant(selectedBattle, currentUser.getId())) {
                status.setText("This battle is no longer available.");
                submit.setDisable(true);
                return;
            }
            if (!"ACTIVE".equalsIgnoreCase(selectedBattle.getStatus())) {
                status.setText("Battle already ended: " + selectedBattle.getStatus());
                timer.setText("Battle finished.");
                submit.setDisable(true);
                return;
            }
            if (battleService.expireIfNeeded(selectedBattle)) {
                status.setText("Battle time expired. Draw recorded.");
                timer.setText("Battle expired: draw recorded.");
                submit.setDisable(true);
                return;
            }
            timer.setText("Time remaining: " + formatSeconds(battleService.getRemainingSeconds(selectedBattle)));
            Submission submission = buildSubmission(problem, code.getText());
            submission.setBattleId(selectedBattle.getId());
            submit.setDisable(true);
            status.setText("Judging...");
            judgeService.evaluateReportAsync(submission, false, report -> {
                selectedBattle = battleService.getBattle(selectedBattle.getId());
                status.setText(report.getVerdict().getDisplayName());
                if (report.getVerdict() == Verdict.AC) {
                    boolean won = battleService.finishWithWinner(selectedBattle, currentUser.getId());
                    status.setText(won ? "You won this battle." : "Battle already finished.");
                    timer.setText("Battle finished.");
                    countdown.stop();
                } else {
                    submit.setDisable(false);
                }
            }, error -> {
                status.setText(error);
                submit.setDisable(false);
            });
        });
        Button back = secondaryButton("Back to Battle Lobby");
        back.setOnAction(event -> {
            countdown.stop();
            String mode = battleService.modeOf(selectedBattle);
            NavigationUtil.navigateTo(BattleService.MODE_FREE_FOR_ALL.equalsIgnoreCase(mode) ? "battle-ffa"
                    : BattleService.MODE_RANDOM_ONE_V_ONE.equalsIgnoreCase(mode) ? "battle-random"
                    : "battle-1v1", back);
        });
        root.getChildren().addAll(h1(problem == null ? "Battle Arena" : problem.getTitle()), opponent, timer, code,
                new HBox(10, submit, back), status);
        return root;
    }

    private static Parent adminPanel() {
        User current = SessionManager.getCurrentUser();
        if (current == null || !"ADMIN".equalsIgnoreCase(current.getRole())) {
            return pageWithMessage("Admin access required.");
        }
        AdminService admin = new AdminService();
        AnalyticsService analytics = new AnalyticsService();
        BorderPane root = shell("Admin Panel");
        Label message = errorLabel();
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Users", adminUsers(admin, message)));
        tabs.getTabs().add(new Tab("Problems", adminProblems(admin, message)));
        tabs.getTabs().add(new Tab("Submissions", adminSubmissions(admin)));
        tabs.getTabs().add(new Tab("Analytics", adminAnalytics(analytics)));
        root.setTop(new VBox(10, header("Admin Panel"), new HBox(10, backButton("Coder Dashboard", "dashboard")), message));
        root.setCenter(tabs);
        return root;
    }

    private static void runJudge(boolean sampleOnly, TextArea code, Label status, Label verdict, VBox results,
                                 Button run, Button submit, JudgeService judgeService, ProblemService problemService,
                                 UserProgressService progressService) {
        if (selectedProblem == null || SessionManager.getCurrentUser() == null) {
            status.setText("Problem or user session is missing.");
            return;
        }
        if (code.getText() == null || code.getText().isBlank()) {
            status.setText("Code cannot be empty.");
            return;
        }
        Submission submission = buildSubmission(selectedProblem, code.getText());
        boolean alreadySolved = problemService.isSolvedByUser(selectedProblem.getId(), SessionManager.getCurrentUser().getId());
        status.setText(sampleOnly ? "Running..." : "Judging...");
        run.setDisable(true);
        submit.setDisable(true);
        results.getChildren().clear();
        judgeService.evaluateReportAsync(submission, sampleOnly, report -> {
            verdict.setText(report.getVerdict().getDisplayName());
            verdict.setStyle("-fx-text-fill: " + report.getVerdict().getColor() + "; -fx-font-weight: bold;");
            results.getChildren().setAll(resultCards(report));
            status.setText("");
            run.setDisable(false);
            submit.setDisable(false);
            if (!sampleOnly && report.getVerdict() == Verdict.AC) {
                progressService.awardProblemSolved(SessionManager.getCurrentUser(), selectedProblem.getId(),
                        selectedProblem.getDifficulty(), alreadySolved);
            }
        }, error -> {
            status.setText(error);
            run.setDisable(false);
            submit.setDisable(false);
        });
    }

    private static Submission buildSubmission(Problem problem, String code) {
        Submission submission = new Submission();
        submission.setUserId(SessionManager.getCurrentUser().getId());
        submission.setProblemId(problem.getId());
        submission.setLanguage("Java");
        submission.setCode(code);
        return submission;
    }

    private static List<Node> resultCards(JudgeService.JudgeReport report) {
        java.util.ArrayList<Node> cards = new java.util.ArrayList<>();
        int index = 1;
        for (var result : report.getResults()) {
        VBox card = outputCard("Test " + index++ + ": " + result.getVerdict().getDisplayName()
                            + " (" + result.getRuntimeMs() + " ms)",
                    result.getInput(), result.getExpectedOutput(), result.getActualOutput());
            card.setStyle("-fx-border-color: " + result.getVerdict().getColor()
                    + "; -fx-border-radius: 6; -fx-background-color: white;");
            cards.add(card);
        }
        return cards;
    }

    private static VBox outputCard(String title, String input, String expected, String actual) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(label(title));
        HBox row = new HBox(10, outputBlock("Input", input), outputBlock("Expected Output", expected));
        if (actual != null) {
            row.getChildren().add(outputBlock("Your Output", actual));
        }
        card.getChildren().add(row);
        VBox.setVgrow(row, Priority.ALWAYS);
        return card;
    }

    private static VBox outputBlock(String title, String value) {
        TextArea area = new TextArea(value == null || value.isBlank() ? "-" : value);
        area.setEditable(false);
        area.setWrapText(false);
        area.setPrefRowCount(4);
        area.setMaxSize(Double.MAX_VALUE, Region.USE_PREF_SIZE);
        area.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace;");
        VBox box = new VBox(4, label(title), area);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private static TableView<Submission> submissionsTable(List<Submission> submissions, ProfileService service) {
        TableView<Submission> table = new TableView<>(FXCollections.observableArrayList(submissions));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(stringColumn("Problem", 220, s -> service.getProblemTitle(s.getProblemId())),
                stringColumn("Verdict", 120, s -> s.getVerdict() == null ? "PENDING" : s.getVerdict().name()),
                stringColumn("Language", 120, Submission::getLanguage),
                stringColumn("Date", 240, Submission::getSubmittedAt));
        return table;
    }

    private static TableView<Battle> battlesTable(List<Battle> battles, ProfileService service) {
        TableView<Battle> table = new TableView<>(FXCollections.observableArrayList(battles));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(stringColumn("Opponent", 180, battle -> {
            User current = SessionManager.getCurrentUser();
            int opponent = current != null && battle.getPlayer1Id() == current.getId()
                    ? battle.getPlayer2Id() : battle.getPlayer1Id();
            return service.getUsername(opponent);
        }), stringColumn("Outcome", 120, battle -> battle.getWinnerId() == null ? battle.getStatus()
                : battle.getWinnerId() == SessionManager.getCurrentUser().getId() ? "Win" : "Loss"),
                stringColumn("Problem", 220, battle -> service.getProblemTitle(battle.getProblemId())));
        return table;
    }

    private static Parent profileEditor(User user, ProfileService service) {
        VBox box = page();
        TextField username = input("Username");
        TextField email = input("Email");
        PasswordField password = new PasswordField();
        password.setPromptText("New password");
        Label message = errorLabel();
        if (user != null) {
            username.setText(user.getUsername());
            email.setText(user.getEmail());
        }
        Button save = primaryButton("Save");
        save.setOnAction(event -> runUi(message, () -> {
            service.updateProfile(SessionManager.getCurrentUser(), username.getText(), email.getText(), password.getText());
            message.setText("Profile saved.");
        }));
        box.getChildren().addAll(username, email, password, save, message);
        return box;
    }

    private static Parent adminUsers(AdminService admin, Label message) {
        TableView<User> table = new TableView<>(FXCollections.observableArrayList(admin.getUsers()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(stringColumn("Username", 220, User::getUsername),
                stringColumn("Email", 240, User::getEmail),
                stringColumn("Role", 110, User::getRole),
                stringColumn("Rank", 130, User::getRankTitle),
                numberColumn("XP", 90, User::getXp),
                numberColumn("Solved", 90, User::getProblemsSolved),
                stringColumn("Status", 120, user -> user.isActive() ? "Active" : "Banned"));
        Button toggle = dangerButton("Ban / Unban Selected");
        toggle.setOnAction(event -> {
            User user = table.getSelectionModel().getSelectedItem();
            if (user != null) {
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    message.setText("Admin accounts cannot be banned from this panel.");
                    return;
                }
                if (!confirm("Update User Status", "Change status for " + user.getUsername() + "?")) {
                    return;
                }
                admin.setUserActive(user.getId(), !user.isActive());
                table.setItems(FXCollections.observableArrayList(admin.getUsers()));
                message.setText(user.getUsername() + " is now " + (user.isActive() ? "banned." : "active."));
            }
        });
        Button refresh = secondaryButton("Refresh Users");
        refresh.setOnAction(event -> table.setItems(FXCollections.observableArrayList(admin.getUsers())));
        VBox panel = new VBox(10, new HBox(10, toggle, refresh), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return panel;
    }

    private static Parent adminProblems(AdminService admin, Label message) {
        TableView<Problem> problems = new TableView<>(FXCollections.observableArrayList(admin.getProblems()));
        problems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        problems.getColumns().addAll(stringColumn("Title", 240, Problem::getTitle),
                stringColumn("Difficulty", 120, p -> p.getDifficulty() == null ? "" : p.getDifficulty().getLabel()),
                stringColumn("Category", 140, Problem::getCategory),
                stringColumn("Tags", 180, Problem::getTags),
                stringColumn("Visibility", 110, p -> p.isPublished() ? "Active" : "Draft"));
        problems.getColumns().add(problemActionsColumn(admin, problems, message));

        Button create = primaryButton("Create New Problem");
        create.setOnAction(event -> openProblemEditor(admin, null, problems, message));

        VBox emptyState = new VBox(8, label("Select Edit to manage a problem's statement and test cases."),
                label("Create New Problem opens a full editor with metadata, sample pairs, hidden cases, and validation."));
        emptyState.setPadding(new Insets(12));

        VBox panel = new VBox(12, new HBox(10, create, backButton("Back to Dashboard", "dashboard")), problems, emptyState);
        VBox.setVgrow(problems, Priority.ALWAYS);
        panel.setPadding(new Insets(12));
        return panel;
    }

    private static TableColumn<Problem, Void> problemActionsColumn(AdminService admin, TableView<Problem> table, Label message) {
        TableColumn<Problem, Void> actions = new TableColumn<>("Actions");
        actions.setPrefWidth(180);
        actions.setCellFactory(column -> new TableCell<>() {
            private final Button edit = smallButton("Edit");
            private final Button delete = smallDangerButton("Delete");
            private final HBox buttons = new HBox(8, edit, delete);

            {
                edit.setOnAction(event -> {
                    Problem problem = getTableView().getItems().get(getIndex());
                    openProblemEditor(admin, problem, table, message);
                });
                delete.setOnAction(event -> runUi(message, () -> {
                    Problem problem = getTableView().getItems().get(getIndex());
                    if (!confirm("Delete Problem", "Delete \"" + problem.getTitle()
                            + "\" and all of its test cases/submissions?")) {
                        return;
                    }
                    admin.deleteProblem(problem.getId());
                    table.setItems(FXCollections.observableArrayList(admin.getProblems()));
                    message.setText("Problem deleted.");
                }));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        return actions;
    }

    private static void openProblemEditor(AdminService admin, Problem existing, TableView<Problem> problemTable, Label adminMessage) {
        ProblemDraft draft = ProblemDraft.from(existing);
        ArrayList<TestCase> cases = new ArrayList<>(existing == null ? List.of() : admin.getTestCases(existing.getId()));

        Dialog<ButtonType> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Create New Problem" : "Edit Problem");
        ButtonType saveType = new ButtonType("Save Problem", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Label error = errorLabel();
        error.setWrapText(true);

        TextField title = wideInput("Title");
        title.setText(draft.title);
        ChoiceBox<String> difficulty = new ChoiceBox<>(FXCollections.observableArrayList("Easy", "Medium", "Hard"));
        difficulty.setValue(draft.difficulty);
        TextField tags = wideInput("Category / tags");
        tags.setText(draft.tags);
        TextField timeLimit = wideInput("Time limit seconds");
        timeLimit.setText(String.valueOf(draft.timeLimit));
        TextField memoryLimit = wideInput("Memory limit MB");
        memoryLimit.setText(String.valueOf(draft.memoryLimit));
        CheckBox active = new CheckBox("Active / visible to coders");
        active.setSelected(draft.active);

        TextArea statement = adminTextArea("Problem statement");
        statement.setText(draft.statement);
        TextArea constraints = adminTextArea("Constraints");
        constraints.setText(draft.constraints);
        TextArea inputFormat = adminTextArea("Input format");
        inputFormat.setText(draft.inputFormat);
        TextArea outputFormat = adminTextArea("Output format");
        outputFormat.setText(draft.outputFormat);

        TableView<TestCase> caseTable = adminTestCaseTable(admin, cases);
        Button addSample = secondaryButton("Add Sample Pair");
        Button addHidden = secondaryButton("Add Hidden Case");
        addSample.setOnAction(event -> editTestCaseDialog(null, false, cases, caseTable, error));
        addHidden.setOnAction(event -> editTestCaseDialog(null, true, cases, caseTable, error));

        GridPane meta = new GridPane();
        meta.setHgap(12);
        meta.setVgap(10);
        meta.add(label("Title"), 0, 0);
        meta.add(title, 1, 0);
        meta.add(label("Difficulty"), 2, 0);
        meta.add(difficulty, 3, 0);
        meta.add(label("Category / tags"), 0, 1);
        meta.add(tags, 1, 1, 3, 1);
        meta.add(label("Time / Memory"), 0, 2);
        meta.add(timeLimit, 1, 2);
        meta.add(memoryLimit, 2, 2);
        meta.add(active, 3, 2);
        GridPane.setHgrow(title, Priority.ALWAYS);
        GridPane.setHgrow(tags, Priority.ALWAYS);

        VBox form = new VBox(12,
                error,
                meta,
                section("Problem Statement", statement),
                section("Constraints", constraints),
                new HBox(12, section("Input Format", inputFormat), section("Output Format", outputFormat)),
                new Separator(),
                new HBox(10, h2("Sample Pairs and Judge Test Cases"), addSample, addHidden),
                caseTable
        );
        form.setPadding(new Insets(14));
        VBox.setVgrow(caseTable, Priority.ALWAYS);

        ScrollPane scroll = fitScroll(form);
        scroll.setPrefViewportWidth(1000);
        scroll.setPrefViewportHeight(720);
        dialog.getDialogPane().setContent(scroll);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                Problem problem = existing == null ? new Problem() : existing;
                problem.setTitle(title.getText());
                problem.setDifficulty(Difficulty.valueOf(difficulty.getValue().toUpperCase()));
                problem.setCategory(firstTag(tags.getText()));
                problem.setTags(tags.getText());
                problem.setDescription(blankForSave(statement.getText()));
                problem.setConstraints(blankForSave(constraints.getText()));
                problem.setInputFormat(blankForSave(inputFormat.getText()));
                problem.setOutputFormat(blankForSave(outputFormat.getText()));
                problem.setTimeLimit(parsePositiveInt(timeLimit.getText(), "Time limit"));
                problem.setMemoryLimit(parsePositiveInt(memoryLimit.getText(), "Memory limit"));
                problem.setPublished(active.isSelected());

                admin.validateProblemReadyForSave(problem, cases);
                admin.saveProblem(problem);
                for (TestCase testCase : cases) {
                    testCase.setProblemId(problem.getId());
                    admin.saveTestCase(testCase);
                }
                problemTable.setItems(FXCollections.observableArrayList(admin.getProblems()));
                adminMessage.setText("Problem saved.");
            } catch (Exception exception) {
                error.setText(exception.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private static TableView<TestCase> adminTestCaseTable(AdminService admin, List<TestCase> cases) {
        TableView<TestCase> table = new TableView<>(FXCollections.observableArrayList(cases));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(240);
        table.getColumns().addAll(
                numberColumn("Order", 80, TestCase::getSequenceOrder),
                stringColumn("Type", 110, testCase -> testCase.isHidden() ? "Hidden" : "Sample"),
                stringColumn("Input Preview", 260, testCase -> preview(testCase.getInput())),
                stringColumn("Expected Preview", 260, testCase -> preview(testCase.getExpected())),
                testCaseActionsColumn(admin, cases, table)
        );
        return table;
    }

    private static TableColumn<TestCase, Void> testCaseActionsColumn(AdminService admin, List<TestCase> cases, TableView<TestCase> table) {
        TableColumn<TestCase, Void> actions = new TableColumn<>("Actions");
        actions.setPrefWidth(180);
        actions.setCellFactory(column -> new TableCell<>() {
            private final Button edit = smallButton("Edit");
            private final Button delete = smallDangerButton("Delete");
            private final HBox buttons = new HBox(8, edit, delete);

            {
                edit.setOnAction(event -> {
                    TestCase testCase = getTableView().getItems().get(getIndex());
                    editTestCaseDialog(testCase, testCase.isHidden(), cases, table, null);
                });
                delete.setOnAction(event -> {
                    TestCase testCase = getTableView().getItems().get(getIndex());
                    if (!confirm("Delete Test Case", "Delete test case #" + testCase.getSequenceOrder() + "?")) {
                        return;
                    }
                    if (testCase.getId() != 0) {
                        admin.deleteTestCase(testCase.getId());
                    }
                    cases.remove(testCase);
                    table.setItems(FXCollections.observableArrayList(cases));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        return actions;
    }

    private static void editTestCaseDialog(TestCase existing, boolean hiddenDefault, List<TestCase> cases,
                                           TableView<TestCase> table, Label parentError) {
        Dialog<ButtonType> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Add Test Case" : "Edit Test Case");
        ButtonType saveType = new ButtonType("Save Test Case", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Label error = errorLabel();
        TextArea input = adminTextArea("Input");
        input.setText(existing == null ? "" : existing.getInput());
        TextArea expected = adminTextArea("Expected output");
        expected.setText(existing == null ? "" : existing.getExpected());
        CheckBox hidden = new CheckBox("Hidden judge case");
        hidden.setSelected(existing == null ? hiddenDefault : existing.isHidden());
        TextField order = wideInput("Order");
        order.setText(String.valueOf(existing == null ? nextCaseOrder(cases) : existing.getSequenceOrder()));

        VBox content = new VBox(10, error, section("Input", input), section("Expected Output", expected),
                new HBox(10, label("Order"), order, hidden));
        content.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(content);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                TestCase testCase = existing == null ? new TestCase() : existing;
                testCase.setInput(input.getText());
                testCase.setExpected(expected.getText());
                testCase.setHidden(hidden.isSelected());
                testCase.setSequenceOrder(Integer.parseInt(order.getText().trim()));
                if (testCase.getInput() == null || testCase.getInput().isBlank()) {
                    throw new IllegalStateException("Input is required.");
                }
                if (testCase.getExpected() == null || testCase.getExpected().isBlank()) {
                    throw new IllegalStateException("Expected output is required.");
                }
                if (testCase.getSequenceOrder() < 1) {
                    throw new IllegalStateException("Order must be at least 1.");
                }
                if (existing == null) {
                    cases.add(testCase);
                }
                cases.sort(java.util.Comparator.comparingInt(TestCase::getSequenceOrder));
                table.setItems(FXCollections.observableArrayList(cases));
                if (parentError != null) {
                    parentError.setText("");
                }
            } catch (NumberFormatException exception) {
                error.setText("Order must be a number.");
                event.consume();
            } catch (Exception exception) {
                error.setText(exception.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private static VBox section(String title, Node content) {
        VBox section = new VBox(5, label(title), content);
        section.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(section, Priority.ALWAYS);
        return section;
    }

    private static VBox detailSection(String title, String value) {
        Label content = label(decodeDisplayText(value));
        content.setWrapText(true);
        content.setMaxWidth(Double.MAX_VALUE);
        VBox section = section(title, content);
        section.setVisible(value != null && !value.isBlank());
        section.setManaged(section.isVisible());
        return section;
    }

    private static TextArea adminTextArea(String prompt) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefRowCount(4);
        area.setWrapText(true);
        area.setMaxWidth(Double.MAX_VALUE);
        return area;
    }

    private static TextField wideInput(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private static int nextCaseOrder(List<TestCase> cases) {
        return cases.stream().mapToInt(TestCase::getSequenceOrder).max().orElse(0) + 1;
    }

    private static int parsePositiveInt(String value, String label) {
        try {
            int parsed = Integer.parseInt(value == null ? "" : value.trim());
            if (parsed < 1) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(label + " must be a positive number.");
        }
    }

    private static String preview(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String flattened = value.replace("\r\n", " ").replace("\n", " ");
        return flattened.length() <= 80 ? flattened : flattened.substring(0, 77) + "...";
    }

    private static String firstTag(String tags) {
        if (tags == null || tags.isBlank()) {
            return "";
        }
        String[] parts = tags.split(",");
        return parts.length == 0 ? tags.trim() : parts[0].trim();
    }

    private static String blankForSave(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class ProblemDraft {
        private String title = "";
        private String difficulty = "Easy";
        private String tags = "";
        private String statement = "";
        private String constraints = "";
        private String inputFormat = "";
        private String outputFormat = "";
        private int timeLimit = 5;
        private int memoryLimit = 256;
        private boolean active = true;

        private static ProblemDraft from(Problem problem) {
            ProblemDraft draft = new ProblemDraft();
            if (problem == null) {
                return draft;
            }
            draft.title = blankForSave(problem.getTitle());
            draft.difficulty = problem.getDifficulty() == null ? "Easy" : problem.getDifficulty().getLabel();
            draft.tags = blankForSave(problem.getTags());
            draft.timeLimit = problem.getTimeLimit() <= 0 ? 5 : problem.getTimeLimit();
            draft.memoryLimit = problem.getMemoryLimit() <= 0 ? 256 : problem.getMemoryLimit();
            draft.active = problem.isPublished();
            draft.statement = decodeDisplayText(problem.getDescription());
            draft.constraints = decodeDisplayText(problem.getConstraints());
            draft.inputFormat = decodeDisplayText(problem.getInputFormat());
            draft.outputFormat = decodeDisplayText(problem.getOutputFormat());
            if (draft.constraints.isBlank() && draft.inputFormat.isBlank() && draft.outputFormat.isBlank()) {
                applyLegacyDescription(draft.statement, draft);
            }
            return draft;
        }

        private static void applyLegacyDescription(String description, ProblemDraft draft) {
            String decoded = decodeDisplayText(description);
            if (decoded.contains("Problem Statement") && decoded.contains("Constraints")) {
                draft.statement = sectionValue(decoded, "Problem Statement", "Constraints");
                draft.constraints = sectionValue(decoded, "Constraints", "Input Format");
                draft.inputFormat = sectionValue(decoded, "Input Format", "Output Format");
                draft.outputFormat = sectionValue(decoded, "Output Format", null);
            }
        }

        private static String sectionValue(String text, String start, String end) {
            int startIndex = text.indexOf(start);
            if (startIndex < 0) {
                return "";
            }
            startIndex += start.length();
            int endIndex = end == null ? text.length() : text.indexOf(end, startIndex);
            if (endIndex < 0) {
                endIndex = text.length();
            }
            return text.substring(startIndex, endIndex).trim();
        }
    }

    private static Parent adminSubmissions(AdminService admin) {
        TableView<Submission> table = new TableView<>(FXCollections.observableArrayList(admin.getSubmissions()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(stringColumn("User", 150, s -> admin.getUsername(s.getUserId())),
                stringColumn("Problem", 220, s -> admin.getProblemTitle(s.getProblemId())),
                stringColumn("Verdict", 120, s -> s.getVerdict() == null ? "PENDING" : s.getVerdict().getDisplayName()),
                stringColumn("Runtime", 100, s -> s.getRuntimeMs() == null ? "-" : s.getRuntimeMs() + " ms"),
                stringColumn("Submitted", 180, Submission::getSubmittedAt),
                submissionActionsColumn(admin, table));
        Button refresh = secondaryButton("Refresh Submissions");
        refresh.setOnAction(event -> table.setItems(FXCollections.observableArrayList(admin.getSubmissions())));
        VBox panel = new VBox(10, refresh, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return panel;
    }

    private static Parent adminAnalytics(AnalyticsService analytics) {
        TableView<Problem> success = new TableView<>(FXCollections.observableArrayList(analytics.getProblemSuccessRates()));
        success.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        success.getColumns().addAll(stringColumn("Problem", 280, Problem::getTitle),
                stringColumn("Acceptance", 140, p -> String.format("%.1f%%", analytics.getAcceptanceRate(p.getId()))));
        VBox panel = new VBox(12, label("Total users: " + analytics.getTotalUsers()),
                label("Submissions today: " + analytics.getTotalSubmissionsToday()),
                label("Verdicts: " + analytics.getSubmissionVerdictBreakdown()), success);
        VBox.setVgrow(success, Priority.ALWAYS);
        return panel;
    }

    private static TableColumn<Submission, Void> submissionActionsColumn(AdminService admin, TableView<Submission> table) {
        TableColumn<Submission, Void> actions = new TableColumn<>("Actions");
        actions.setPrefWidth(190);
        actions.setCellFactory(column -> new TableCell<>() {
            private final Button view = smallButton("View Code");
            private final Button delete = smallDangerButton("Delete");
            private final HBox buttons = new HBox(8, view, delete);

            {
                view.setOnAction(event -> showSubmissionDialog(getTableView().getItems().get(getIndex()), admin));
                delete.setOnAction(event -> {
                    Submission submission = getTableView().getItems().get(getIndex());
                    if (!confirm("Delete Submission", "Delete this submission permanently?")) {
                        return;
                    }
                    admin.deleteSubmission(submission.getId());
                    table.setItems(FXCollections.observableArrayList(admin.getSubmissions()));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        return actions;
    }

    private static void showSubmissionDialog(Submission submission, AdminService admin) {
        Dialog<ButtonType> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Submission #" + submission.getId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        TextArea code = adminTextArea("Submitted code");
        code.setText(submission.getCode());
        code.setEditable(false);
        code.setPrefRowCount(24);
        VBox body = new VBox(10,
                label("User: " + admin.getUsername(submission.getUserId())),
                label("Problem: " + admin.getProblemTitle(submission.getProblemId())),
                label("Verdict: " + (submission.getVerdict() == null ? "PENDING" : submission.getVerdict().getDisplayName())),
                code);
        body.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(body);
        dialog.showAndWait();
    }

    private static Parent missing(String screenName) {
        return pageWithMessage("Screen not found: " + screenName);
    }

    private static Parent pageWithMessage(String message) {
        VBox root = page();
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(h1("CodeArena"), label(message), backButton("Back to Login", "login"));
        return root;
    }

    private static Parent guestAccessMessage(String message) {
        VBox root = page();
        root.setAlignment(Pos.CENTER);
        Label title = h1("Login Required");
        Button login = primaryButton("Login");
        login.setOnAction(event -> NavigationUtil.navigateTo("login", login));
        Button register = primaryButton("Create Account");
        register.setOnAction(event -> NavigationUtil.navigateTo("register", register));
        Button problems = primaryButton("Browse Problems");
        problems.setOnAction(event -> NavigationUtil.navigateTo("problem-list", problems));
        root.getChildren().addAll(title, label(message), new HBox(10, login, register, problems));
        return root;
    }

    private static BorderPane shell(String title) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #F6F8FA;");
        return root;
    }

    private static VBox page() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #F6F8FA;");
        return root;
    }

    private static VBox contentPage() {
        VBox root = page();
        root.setMaxWidth(Double.MAX_VALUE);
        return root;
    }

    private static ScrollPane fitScroll(Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    private static VBox header(String title) {
        VBox header = new VBox(8, h1(title));
        header.setPadding(new Insets(20));
        return header;
    }

    private static VBox headerWithBack(String title, String backScreen) {
        return new VBox(10, header(title), backButton("Back", backScreen));
    }

    private static Label h1(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        return label;
    }

    private static Label h2(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        return label;
    }

    private static Label label(String text) {
        return new Label(text == null ? "" : text);
    }

    private static Label mutedLabel(String text) {
        return label(text);
    }

    private static String formatSeconds(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int minutes = safeSeconds / 60;
        int seconds = safeSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private static Label badge(String text) {
        Label label = label(text);
        label.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12;");
        return label;
    }

    private static Label errorLabel() {
        Label label = new Label();
        label.setStyle("-fx-text-fill: #C62828;");
        return label;
    }

    private static TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(360);
        return field;
    }

    private static PasswordField passwordInput(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setMaxWidth(360);
        return field;
    }

    private static Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white; -fx-font-weight: bold;");
        return button;
    }

    private static Button secondaryButton(String text) {
        return primaryButton(text);
    }

    private static Button dangerButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; -fx-font-weight: bold;");
        return button;
    }

    private static Button smallButton(String text) {
        return primaryButton(text);
    }

    private static Button smallDangerButton(String text) {
        return dangerButton(text);
    }

    private static VBox simplePanel(String title, Node... children) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #D0D7DE; -fx-border-radius: 6; -fx-background-radius: 6;");
        panel.getChildren().add(h2(title));
        panel.getChildren().addAll(children);
        panel.setMaxWidth(Double.MAX_VALUE);
        return panel;
    }

    private static Button battleModeButton(String title, String description, String screen, boolean disabled) {
        Button button = secondaryButton(title + "\n" + description);
        button.setWrapText(true);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setMinHeight(72);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setDisable(disabled);
        if (!disabled) {
            button.setOnAction(event -> NavigationUtil.navigateTo(screen, button));
        }
        return button;
    }

    private static Button navButton(String text, String screen) {
        Button button = secondaryButton(text);
        button.setOnAction(event -> NavigationUtil.navigateTo(screen, button));
        return button;
    }

    private static HBox problemDetailActions() {
        HBox actions = new HBox(10);
        if (SessionManager.isLoggedIn()) {
            Button start = primaryButton("Start Coding");
            start.setOnAction(event -> NavigationUtil.navigateTo("code-editor", start));
            actions.getChildren().add(start);
        } else {
            Label prompt = label("Log in or create an account to solve this problem.");
            Button login = primaryButton("Login to Solve");
            login.setOnAction(event -> NavigationUtil.navigateTo("login", login));
            Button register = primaryButton("Register");
            register.setOnAction(event -> NavigationUtil.navigateTo("register", register));
            actions.getChildren().addAll(prompt, login, register);
        }
        actions.getChildren().add(backButton("Back", "problem-list"));
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    private static Button publicBackButton() {
        return backButton("Back", SessionManager.isLoggedIn() ? "dashboard" : "login");
    }

    private static Button backButton(String text, String screen) {
        Button button = secondaryButton(text);
        button.setOnAction(event -> NavigationUtil.navigateTo(screen, button));
        return button;
    }

    private static <T> TableColumn<T, Number> column(String title, double width) {
        TableColumn<T, Number> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        return column;
    }

    private static <T> TableColumn<T, String> stringColumn(String title, double width, java.util.function.Function<T, String> mapper) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(blank(mapper.apply(cell.getValue()))));
        return column;
    }

    private static <T> TableColumn<T, Number> numberColumn(String title, double width, java.util.function.ToIntFunction<T> mapper) {
        TableColumn<T, Number> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(mapper.applyAsInt(cell.getValue())));
        return column;
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String decodeDisplayText(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\r\\n", "\n").replace("\\n", "\n").replace("\\t", "\t");
    }

    private static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        applyDialogStyles(alert);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private static void runUi(Label message, Runnable action) {
        try {
            action.run();
            if (message != null && message.getText().isBlank()) {
                message.setText("");
            }
        } catch (Exception exception) {
            if (message != null) {
                message.setText(exception.getMessage());
            }
        }
    }

    private static void applyDialogStyles(Dialog<?> dialog) {
    }

    private static <T extends Node> T styled(T node, String... classes) {
        for (String styleClass : classes) {
            if (styleClass != null && !styleClass.isBlank() && !node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        }
        return node;
    }

}

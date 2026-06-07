package com.codearena.ui;

import com.codearena.judge.Verdict;
import com.codearena.model.Battle;
import com.codearena.model.Badge;
import com.codearena.model.Difficulty;
import com.codearena.model.Problem;
import com.codearena.model.Submission;
import com.codearena.model.TestCase;
import com.codearena.model.User;
import com.codearena.service.AdminService;
import com.codearena.service.AnalyticsService;
import com.codearena.service.AuthService;
import com.codearena.service.BadgeService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
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
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.util.Duration;

public final class ScreenFactory {

    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(ScreenFactory.class);
    private static final String DARK_MODE_KEY = "darkMode";
    private static final String LOGO_MARK_PATH = "/images/logo-mark.png";
    private static final String LOGO_FULL_PATH = "/images/logo-full.png";
    private static final String LOGO_DARK_WORDMARK_PATH = "/images/logo-dark-wordmark.png";
    private static final String DEVELOPERS = "Developed by Zeeshan Azeem, Sharjeel Ali Khan and Abdul Kabeer";

    private static final String TEMPLATE = """
            import java.util.Scanner;
            public class Solution {
                public static void main(String[] args) {
                    Scanner sc = new Scanner(System.in);
                    // your code here
                }
            }
            """;

    private static final String PYTHON_TEMPLATE = """
            import sys

            def main():
                data = sys.stdin.read().strip().split()
                # your code here

            if __name__ == "__main__":
                main()
            """;

    private static Problem selectedProblem;
    private static Battle selectedBattle;
    private static String currentScreenName = "login";

    private ScreenFactory() {
    }

    public static Parent create(String screenName) {
        currentScreenName = normalize(screenName);
        Parent screen = switch (currentScreenName) {
            case "splash" -> splash();
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
        applyTheme(screen);
        return screen;
    }

    private static String normalize(String screenName) {
        return screenName == null ? "" : screenName.trim();
    }

    private static Parent splash() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #06101A;");

        Pane background = new Pane();
        background.setStyle("-fx-background-color: linear-gradient(to bottom right, #06101A, #0A1724 55%, #0E2636);");
        background.getChildren().addAll(
                glowCircle(-60, 120, 340, "#F57C002E"),
                glowCircle(1180, 580, 460, "#1B7FA540"),
                diagonalPanel(-90, 0, 360, 720, "#F57C0033"),
                diagonalPanel(70, 0, 480, 720, "#F57C001E"),
                diagonalPanel(820, 720, 1210, 0, "#0F2B3D99"),
                diagonalPanel(960, 720, 1320, 0, "#F57C0024"),
                arenaRing(1060, 430, 300, "#1B7FA555"),
                accentLine(115, 630, 520, 630, "#F57C00"),
                accentLine(920, 690, 1210, 340, "#F57C00"),
                codeBlock(720, 36),
                dottedField(1010, 470));
        background.prefWidthProperty().bind(root.widthProperty());
        background.prefHeightProperty().bind(root.heightProperty());

        ImageView logo = logoImage(LOGO_MARK_PATH, 245);
        Label code = new Label("CODE");
        code.setStyle("-fx-font-size: 62px; -fx-font-weight: bold; -fx-text-fill: #F57C00;");
        Label arena = new Label("ARENA");
        arena.setStyle("-fx-font-size: 62px; -fx-font-weight: bold; -fx-text-fill: #F6F7FF;");
        HBox wordmark = new HBox(0, code, arena);
        wordmark.setAlignment(Pos.CENTER_LEFT);
        Label tagline = new Label("BUILD.  BATTLE.  BECOME LEGEND.");
        tagline.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-letter-spacing: 3px; -fx-text-fill: #A9BED0;");
        Label credits = label(DEVELOPERS);
        credits.setWrapText(true);
        credits.setMaxWidth(760);
        credits.setStyle("-fx-text-fill: #8098AA;");

        Button enter = primaryButton("Enter CodeArena");
        enter.setDefaultButton(true);
        enter.setOnAction(event -> NavigationUtil.navigateTo("login", enter));

        HBox mainBrand = new HBox(36, logo, new VBox(14, wordmark, tagline, credits));
        mainBrand.setAlignment(Pos.CENTER);

        Label version = new Label("2026.1");
        version.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #D7E3EC;");
        Label edition = new Label("ULTIMATE CODING ARENA");
        edition.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #7E9BB1;");
        ProgressBar loading = new ProgressBar(0.80);
        loading.setPrefWidth(420);
        loading.setStyle("-fx-accent: #F57C00;");
        Label loadingText = new Label("Press Enter to continue");
        loadingText.setStyle("-fx-font-size: 15px; -fx-text-fill: #9CB4C6;");
        VBox loadingStack = new VBox(8, version, edition, loading, loadingText);
        loadingStack.setAlignment(Pos.CENTER_LEFT);

        VBox caTile = caTile();
        HBox bottom = new HBox(24, loadingStack, spacer(), enter, caTile);
        bottom.setAlignment(Pos.BOTTOM_LEFT);

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(46, 70, 48, 70));
        layout.setCenter(mainBrand);
        layout.setBottom(bottom);

        root.getChildren().addAll(background, layout);
        root.setFocusTraversable(true);
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && enter.getScene() != null) {
                NavigationUtil.navigateTo("login", enter);
            }
        });
        root.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                root.requestFocus();
            }
        });
        return root;
    }

    private static Parent login() {
        AuthService authService = new AuthService();
        StackPane root = authRoot();

        ImageView logo = logoImage(themeLogoPath(), isDarkMode() ? 118 : 150);
        Label brandTitle = h1("CodeArena");
        Label brandCopy = label("Practice problems, win battles, unlock badges, and climb the leaderboard.");
        brandCopy.setWrapText(true);
        brandCopy.setMaxWidth(430);
        VBox brandPanel = new VBox(16, logo, brandTitle, brandCopy,
                featureCard("Problems", "Browse coding challenges and track accepted submissions."),
                featureCard("Battles", "Create rooms, join friends, or queue for random 1v1 matches."),
                featureCard("Progress", "Your profile records XP, ranks, badges, and battle history."));
        brandPanel.setPadding(new Insets(30));
        brandPanel.setMaxWidth(500);
        brandPanel.setStyle(panelStyle());

        Label title = h1("Welcome back");
        Label subtitle = mutedLabel("Log in to continue your arena run.");
        TextField username = input("Username");
        PasswordField password = passwordInput("Password");
        username.setMaxWidth(Double.MAX_VALUE);
        password.setMaxWidth(Double.MAX_VALUE);
        Label message = errorLabel();
        message.setText(NavigationUtil.consumeFlashMessage() == null ? "" : "Registration successful. Please log in.");

        Button login = primaryButton("Login");
        login.setMaxWidth(Double.MAX_VALUE);
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
        VBox loginCard = new VBox(14, title, subtitle, username, password, login, register, guestLinks,
                themeToggleButton("login"), message);
        loginCard.setPadding(new Insets(30));
        loginCard.setMaxWidth(500);
        loginCard.setStyle(panelStyle());

        HBox layout = new HBox(24, brandPanel, loginCard);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(42));
        Pane background = authBackground();
        background.prefWidthProperty().bind(root.widthProperty());
        background.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().addAll(background, layout);
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
        root.getChildren().addAll(h1("Create Account"), username, email, password, confirm, submit, back,
                themeToggleButton("register"), message);
        return root;
    }

    private static Parent dashboard() {
        if (!SessionManager.isLoggedIn()) {
            return guestAccessMessage("Please log in to open your dashboard.");
        }
        ProfileService profileService = new ProfileService();
        LeaderboardService leaderboardService = new LeaderboardService();
        User current = SessionManager.getCurrentUser();
        if (current != null) {
            User refreshed = profileService.getUser(current.getId());
            if (refreshed != null) {
                SessionManager.setCurrentUser(refreshed);
                current = refreshed;
            }
        }

        BorderPane root = shell("Dashboard");
        Label title = h1(current == null ? "Dashboard" : "Welcome back, " + current.getUsername());
        Label rank = mutedLabel(current == null ? "" : current.getRankTitle() + " | " + current.getXp() + " XP");
        ProgressBar progress = new ProgressBar(0);
        progress.setMaxWidth(Double.MAX_VALUE);
        if (current != null) {
            int next = XPCalculator.nextRankThreshold(current.getXp());
            progress.setProgress(next == current.getXp() ? 1.0 : Math.min(1.0, current.getXp() / (double) next));
        }

        HBox heroActions = new HBox(10, navButton("Problems", "problem-list"), navButton("Battle", "battle-lobby"),
                navButton("Profile", "profile"));
        if (isCurrentUserAdmin()) {
            heroActions.getChildren().add(navButton("Admin Dashboard", "admin-panel"));
        }
        heroActions.setAlignment(Pos.CENTER_RIGHT);
        HBox titleRow = new HBox(16, new VBox(4, title, rank), spacer(), heroActions);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        HBox metrics = new HBox(14,
                statCard("Solved", current == null ? "0" : String.valueOf(current.getProblemsSolved()), "Accepted problems", "#19D3F3"),
                statCard("XP", current == null ? "0" : String.valueOf(current.getXp()), "Rank progress", "#C83CFF"),
                statCard("Battles", current == null ? "0W / 0L" : current.getBattlesWon() + "W / " + current.getBattlesLost() + "L",
                        "Arena record", "#FFB020"),
                statCard("Streak", current == null ? "0" : current.getStreakDays() + " days", "Daily momentum", "#22C55E"));
        metrics.setFillHeight(true);

        VBox progressPanel = simplePanel("Rank Track", progress,
                mutedLabel("Keep solving and battling to push the next rank threshold."));
        VBox quickActions = simplePanel("Quick Launch",
                wideNavButton("Browse Problems", "problem-list"),
                wideNavButton("Battle Lobby", "battle-lobby"),
                wideNavButton("Squad", "squad"),
                wideNavButton("Leaderboard", "leaderboard"));
        TableView<User> leaders = compactLeaderboard(leaderboardService.getRankedUsers());
        VBox leaderboardPanel = simplePanel("Top Coders", leaders);
        VBox.setVgrow(leaders, Priority.ALWAYS);

        HBox lower = new HBox(14, progressPanel, quickActions, leaderboardPanel);
        HBox.setHgrow(progressPanel, Priority.ALWAYS);
        HBox.setHgrow(quickActions, Priority.ALWAYS);
        HBox.setHgrow(leaderboardPanel, Priority.ALWAYS);

        Button logout = primaryButton("Logout");
        logout.setOnAction(event -> {
            new AuthService().logout();
            NavigationUtil.navigateTo("login", logout);
        });
        HBox footer = new HBox(spacer(), logout);
        VBox content = contentPage();
        content.getChildren().addAll(titleRow, metrics, lower, footer);
        VBox.setVgrow(lower, Priority.ALWAYS);
        root.setCenter(fitScroll(content));
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
        filters.setAlignment(Pos.CENTER_LEFT);
        VBox center = new VBox(loading, table);
        center.setPadding(new Insets(18));
        center.setStyle(panelStyle());
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox content = shellContent("Problem List");
        content.getChildren().addAll(filters, error, center);
        VBox.setVgrow(center, Priority.ALWAYS);
        root.setCenter(content);
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
        BadgeService badgeService = new BadgeService();
        BorderPane root = shell("Code Editor");
        VBox top = new VBox(10);
        top.setPadding(new Insets(0, 0, 12, 0));

        Label title = h1(selectedProblem == null ? "Code Editor" : selectedProblem.getTitle());
        Label difficulty = badge(selectedProblem == null || selectedProblem.getDifficulty() == null ? "" : selectedProblem.getDifficulty().getLabel());
        ChoiceBox<String> language = languageChoice();

        TextArea code = new TextArea(TEMPLATE);
        code.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace;");
        code.setPrefRowCount(18);
        code.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        code.setWrapText(false);
        language.getSelectionModel().selectedItemProperty().addListener((observable, oldLanguage, newLanguage) -> {
            if (code.getText() == null || code.getText().isBlank()
                    || code.getText().equals(templateForLanguage(oldLanguage))) {
                code.setText(templateForLanguage(newLanguage));
            }
        });
        HBox languageRow = new HBox(10, label("Language"), language);
        languageRow.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(new HBox(10, title, difficulty), languageRow);

        Label status = errorLabel();
        Label verdict = new Label();
        verdict.setMaxWidth(Double.MAX_VALUE);
        VBox results = new VBox(10);
        results.setMaxWidth(Double.MAX_VALUE);
        Button run = primaryButton("Run");
        Button submit = primaryButton("Submit");
        Button back = backButton("Back", "problem-detail");

        Runnable[] evaluate = new Runnable[2];
        evaluate[0] = () -> runJudge(true, code, language.getValue(), status, verdict, results, run, submit,
                judgeService, problemService, progressService, badgeService);
        evaluate[1] = () -> runJudge(false, code, language.getValue(), status, verdict, results, run, submit,
                judgeService, problemService, progressService, badgeService);
        run.setOnAction(event -> evaluate[0].run());
        submit.setOnAction(event -> evaluate[1].run());

        HBox actions = new HBox(10, run, submit, back);
        actions.setAlignment(Pos.CENTER_LEFT);
        ScrollPane resultScroll = fitScroll(results);
        resultScroll.setMinHeight(86);
        resultScroll.setPrefViewportHeight(130);
        resultScroll.setMaxHeight(180);
        VBox bottom = new VBox(8, actions, status, verdict, resultScroll);
        bottom.setPadding(new Insets(12));
        bottom.setMaxWidth(Double.MAX_VALUE);
        bottom.setStyle(panelStyle());

        VBox editor = new VBox(12, top, code, bottom);
        editor.setPadding(new Insets(24));
        editor.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(code, Priority.ALWAYS);
        root.setCenter(editor);
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
        VBox panel = simplePanel("Rankings", table);
        panel.setPadding(new Insets(20));
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox content = shellContent("Public Leaderboard");
        content.getChildren().addAll(publicBackButton(), panel);
        VBox.setVgrow(panel, Priority.ALWAYS);
        root.setCenter(content);
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
        List<Submission> submissions = user == null ? List.of() : service.getSubmissionHistory(user.getId());
        List<Battle> battles = user == null ? List.of() : service.getBattleHistory(user.getId());
        VBox top = new VBox(8, h1(user == null ? "Profile" : user.getUsername() + " | " + user.getRankTitle()),
                label(user == null ? "" : user.getXp() + " XP | Solved " + user.getProblemsSolved()
                        + " | Battles " + user.getBattlesWon() + "W / " + user.getBattlesLost() + "L"),
                backButton(backToHomeText(), homeScreen()));
        top.setPadding(new Insets(20));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Performance", performanceView(submissions, battles, user, service)));
        tabs.getTabs().add(new Tab("Achievements", badgesView(user == null ? List.of() : service.getBadges(user.getId()))));
        tabs.getTabs().add(new Tab("Edit", profileEditor(user, service)));
        VBox center = new VBox(tabs);
        center.setPadding(new Insets(0, 20, 20, 20));
        center.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tabs.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tabs.setMinHeight(0);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        VBox content = new VBox(10, top, center);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(center, Priority.ALWAYS);
        root.setCenter(content);
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
            User refreshed = SessionManager.getCurrentUser();
            var squad = service.getCurrentUserSquad(refreshed);
            title.setText(squad == null ? "No Squad Yet"
                    : squad.getName() + " | Combined XP: " + service.getCombinedXp(squad));
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

        HBox forms = new HBox(10, name, description, create, join, joinButton, leave, backButton(backToHomeText(), homeScreen()));
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
        membersPanel.setPadding(new Insets(18));
        leaderboardPanel.setPadding(new Insets(18));
        membersPanel.setStyle(panelStyle());
        leaderboardPanel.setStyle(panelStyle());
        membersPanel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        leaderboardPanel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(members, Priority.ALWAYS);
        VBox.setVgrow(squads, Priority.ALWAYS);

        HBox center = new HBox(16, membersPanel, leaderboardPanel);
        center.setPadding(new Insets(0));
        center.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(membersPanel, Priority.ALWAYS);
        HBox.setHgrow(leaderboardPanel, Priority.ALWAYS);

        VBox content = shellContent("Squad");
        content.getChildren().addAll(top, center);
        VBox.setVgrow(center, Priority.ALWAYS);
        root.setCenter(content);
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
                backButton(backToHomeText(), homeScreen())
        );
        setShellContent(root, "Choose Battle Mode", modes);
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
        Button copyCode = copyCodeButton(codeValue, message);
        HBox codeRow = new HBox(12, codeValue, copyCode);
        codeRow.setAlignment(Pos.CENTER_LEFT);
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
        VBox createPanel = simplePanel("Create Match", createActions, codeRow, createState);

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
        content.setPadding(new Insets(0));
        content.setMaxWidth(Double.MAX_VALUE);
        setShellContent(root, "1v1 Battle", content);
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
        Button copyCode = copyCodeButton(codeValue, message);
        HBox codeRow = new HBox(12, codeValue, copyCode);
        codeRow.setAlignment(Pos.CENTER_LEFT);
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
        VBox createPanel = simplePanel("Create Free for All", createActions, codeRow, createState);

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
        content.setPadding(new Insets(0));
        content.setMaxWidth(Double.MAX_VALUE);
        setShellContent(root, "Free for All", content);
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
        content.setPadding(new Insets(0));
        setShellContent(root, "Random Match 1v1", content);
        return root;
    }

    private static Parent battleArena() {
        if (!SessionManager.isLoggedIn()) {
            return guestAccessMessage("Please log in to continue a battle.");
        }
        BattleService battleService = new BattleService();
        JudgeService judgeService = new JudgeService();
        BadgeService badgeService = new BadgeService();
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
        ChoiceBox<String> language = languageChoice();
        TextArea code = new TextArea(TEMPLATE);
        code.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace;");
        code.setPrefHeight(430);
        language.getSelectionModel().selectedItemProperty().addListener((observable, oldLanguage, newLanguage) -> {
            if (code.getText() == null || code.getText().isBlank()
                    || code.getText().equals(templateForLanguage(oldLanguage))) {
                code.setText(templateForLanguage(newLanguage));
            }
        });
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
            Submission submission = buildSubmission(problem, code.getText(), language.getValue());
            submission.setBattleId(selectedBattle.getId());
            submit.setDisable(true);
            status.setText("Judging...");
            judgeService.evaluateReportAsync(submission, false, report -> {
                selectedBattle = battleService.getBattle(selectedBattle.getId());
                status.setText(report.getVerdict().getDisplayName());
                if (report.getVerdict() == Verdict.AC) {
                    badgeService.checkSubmissionBadges(currentUser, submission, problem);
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
        HBox languageRow = new HBox(10, label("Language"), language);
        languageRow.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().addAll(h1(problem == null ? "Battle Arena" : problem.getTitle()), opponent, timer,
                languageRow, code,
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
        HBox metrics = new HBox(14,
                statCard("Users", String.valueOf(admin.getUsers().size()), "Registered accounts", "#C83CFF"),
                statCard("Problems", String.valueOf(admin.getProblems().size()), "Challenge library", "#19D3F3"),
                statCard("Submissions", String.valueOf(admin.getSubmissions().size()), "Judge activity", "#FFB020"));
        HBox adminActions = new HBox(10, backButton("Coder Dashboard", "dashboard"));
        adminActions.setAlignment(Pos.CENTER_LEFT);
        VBox adminTop = new VBox(14, h1("Admin Panel"), metrics, adminActions, message);
        VBox center = new VBox(14, adminTop, tabs);
        center.setPadding(new Insets(24));
        center.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        root.setCenter(center);
        return root;
    }

    private static void runJudge(boolean sampleOnly, TextArea code, String language, Label status, Label verdict, VBox results,
                                 Button run, Button submit, JudgeService judgeService, ProblemService problemService,
                                 UserProgressService progressService, BadgeService badgeService) {
        if (selectedProblem == null || SessionManager.getCurrentUser() == null) {
            status.setText("Problem or user session is missing.");
            return;
        }
        if (code.getText() == null || code.getText().isBlank()) {
            status.setText("Code cannot be empty.");
            return;
        }
        Submission submission = buildSubmission(selectedProblem, code.getText(), language);
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
                badgeService.checkSubmissionBadges(SessionManager.getCurrentUser(), submission, selectedProblem);
            }
        }, error -> {
            status.setText(error);
            run.setDisable(false);
            submit.setDisable(false);
        });
    }

    private static Submission buildSubmission(Problem problem, String code, String language) {
        Submission submission = new Submission();
        submission.setUserId(SessionManager.getCurrentUser().getId());
        submission.setProblemId(problem.getId());
        submission.setLanguage(normalizeLanguage(language));
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
                    + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: " + panelColor() + ";");
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
        boundTableViewport(table, 300);
        return table;
    }

    private static TableView<Battle> battlesTable(List<Battle> battles, ProfileService service) {
        TableView<Battle> table = new TableView<>(FXCollections.observableArrayList(battles));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(stringColumn("Opponent", 180, battle -> {
            User current = SessionManager.getCurrentUser();
            return service.getBattleOpponent(battle, current == null ? 0 : current.getId());
        }), stringColumn("Outcome", 120, battle -> {
            User current = SessionManager.getCurrentUser();
            return service.getBattleOutcome(battle, current == null ? 0 : current.getId());
        }),
                stringColumn("Problem", 220, battle -> service.getProblemTitle(battle.getProblemId())));
        boundTableViewport(table, 300);
        return table;
    }

    private static Parent performanceView(List<Submission> submissions, List<Battle> battles, User user,
                                          ProfileService service) {
        VBox content = new VBox(16);
        content.setPadding(new Insets(18));
        content.setMaxWidth(Double.MAX_VALUE);
        content.getChildren().addAll(profileCharts(submissions, battles, user), historyTabs(submissions, battles, service));
        return content;
    }

    private static TabPane historyTabs(List<Submission> submissions, List<Battle> battles, ProfileService service) {
        TabPane history = new TabPane();
        history.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        history.getTabs().add(new Tab("Submissions", tableTab(submissionsTable(submissions, service))));
        history.getTabs().add(new Tab("Battles", tableTab(battlesTable(battles, service))));
        history.setPrefHeight(340);
        history.setMinHeight(260);
        history.setMaxHeight(340);
        return history;
    }

    private static Parent tableTab(TableView<?> table) {
        VBox box = new VBox(table);
        box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private static void boundTableViewport(TableView<?> table, double height) {
        table.setFixedCellSize(30);
        table.setPrefHeight(height);
        table.setMinHeight(180);
        table.setMaxHeight(height);
    }

    private static HBox profileCharts(List<Submission> submissions, List<Battle> battles, User user) {
        HBox charts = new HBox(14,
                pieChartCard("Verdicts", verdictData(submissions)),
                barChartCard("Languages", languageData(submissions), "#19D3F3"),
                barChartCard("Battles", battleData(battles, user), "#C83CFF"));
        charts.setMaxWidth(Double.MAX_VALUE);
        for (Node child : charts.getChildren()) {
            HBox.setHgrow(child, Priority.ALWAYS);
        }
        return charts;
    }

    private static Parent pieChartCard(String title, Map<String, Integer> values) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setMinHeight(270);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(panelStyle());
        card.getChildren().add(h2(title));
        int total = values.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            Label empty = mutedLabel("No data yet");
            empty.setAlignment(Pos.CENTER);
            empty.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            card.getChildren().add(empty);
            VBox.setVgrow(empty, Priority.ALWAYS);
            return card;
        }
        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setLabelsVisible(false);
        chart.setStartAngle(90);
        chart.setData(FXCollections.observableArrayList(values.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .toList()));
        chart.setStyle(chartStyle());
        VBox.setVgrow(chart, Priority.ALWAYS);
        card.getChildren().add(chart);
        return card;
    }

    private static Parent barChartCard(String title, Map<String, Integer> values, String accent) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setMinHeight(270);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(panelStyle());
        card.getChildren().add(h2(title));
        int total = values.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            Label empty = mutedLabel("No data yet");
            empty.setAlignment(Pos.CENTER);
            empty.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            card.getChildren().add(empty);
            VBox.setVgrow(empty, Priority.ALWAYS);
            return card;
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelFill(javafx.scene.paint.Color.web(mutedTextColor()));
        yAxis.setTickLabelFill(javafx.scene.paint.Color.web(mutedTextColor()));
        xAxis.setTickLabelRotation(0);
        yAxis.setMinorTickVisible(false);
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCategoryGap(20);
        chart.setBarGap(4);
        chart.setStyle(chartStyle());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            if (entry.getValue() > 0) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }
        chart.getData().add(series);
        chart.applyCss();
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-bar-fill: " + accent + ";");
            }
        }
        VBox.setVgrow(chart, Priority.ALWAYS);
        card.getChildren().add(chart);
        return card;
    }

    private static Map<String, Integer> verdictData(List<Submission> submissions) {
        Map<String, Integer> data = new LinkedHashMap<>();
        data.put("Accepted", 0);
        data.put("Wrong Answer", 0);
        data.put("Compilation Error", 0);
        data.put("Runtime Error", 0);
        data.put("Time Limit", 0);
        data.put("Other", 0);
        for (Submission submission : submissions == null ? List.<Submission>of() : submissions) {
            Verdict verdict = submission.getVerdict();
            if (verdict == Verdict.AC) {
                data.computeIfPresent("Accepted", (key, value) -> value + 1);
            } else if (verdict == Verdict.WA) {
                data.computeIfPresent("Wrong Answer", (key, value) -> value + 1);
            } else if (verdict == Verdict.CE) {
                data.computeIfPresent("Compilation Error", (key, value) -> value + 1);
            } else if (verdict == Verdict.RE) {
                data.computeIfPresent("Runtime Error", (key, value) -> value + 1);
            } else if (verdict == Verdict.TLE) {
                data.computeIfPresent("Time Limit", (key, value) -> value + 1);
            } else {
                data.computeIfPresent("Other", (key, value) -> value + 1);
            }
        }
        return data;
    }

    private static Map<String, Integer> languageData(List<Submission> submissions) {
        Map<String, Integer> data = new LinkedHashMap<>();
        data.put("Java", 0);
        data.put("Python", 0);
        data.put("Other", 0);
        for (Submission submission : submissions == null ? List.<Submission>of() : submissions) {
            String language = submission.getLanguage() == null ? "" : submission.getLanguage().trim();
            if ("Java".equalsIgnoreCase(language)) {
                data.computeIfPresent("Java", (key, value) -> value + 1);
            } else if ("Python".equalsIgnoreCase(language)) {
                data.computeIfPresent("Python", (key, value) -> value + 1);
            } else {
                data.computeIfPresent("Other", (key, value) -> value + 1);
            }
        }
        return data;
    }

    private static Map<String, Integer> battleData(List<Battle> battles, User user) {
        Map<String, Integer> data = new LinkedHashMap<>();
        data.put("Wins", 0);
        data.put("Losses", 0);
        data.put("Other", 0);
        int userId = user == null ? 0 : user.getId();
        for (Battle battle : battles == null ? List.<Battle>of() : battles) {
            Integer winnerId = battle.getWinnerId();
            if (winnerId != null && winnerId == userId) {
                data.computeIfPresent("Wins", (key, value) -> value + 1);
            } else if (winnerId != null) {
                data.computeIfPresent("Losses", (key, value) -> value + 1);
            } else {
                data.computeIfPresent("Other", (key, value) -> value + 1);
            }
        }
        return data;
    }

    private static Parent badgesView(List<Badge> badges) {
        List<Badge> safeBadges = badges == null ? List.of() : badges;
        TabPane badgeTabs = new TabPane();
        badgeTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        badgeTabs.getTabs().add(new Tab("My Badges", badgeGallery(safeBadges.stream()
                .filter(Badge::isEarned)
                .toList(), true)));
        badgeTabs.getTabs().add(new Tab("Total Badges", badgeGallery(safeBadges, false)));
        return badgeTabs;
    }

    private static Parent badgeGallery(List<Badge> badges, boolean earnedOnly) {
        VBox content = new VBox(14);
        content.setPadding(new Insets(18));
        content.setMaxWidth(Double.MAX_VALUE);
        Label summary = h2(earnedOnly ? "My Badges" : "Total Badges");
        Label count = mutedLabel(earnedOnly
                ? badges.size() + " earned badge" + (badges.size() == 1 ? "" : "s")
                : badges.size() + " badge" + (badges.size() == 1 ? "" : "s") + " available");
        TilePane grid = new TilePane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPrefColumns(4);
        grid.setTileAlignment(Pos.TOP_LEFT);
        grid.setMaxWidth(Double.MAX_VALUE);
        if (badges.isEmpty()) {
            grid.getChildren().add(emptyBadgeCard(earnedOnly
                    ? "No badges earned yet. Solve problems or win battles to unlock your first one."
                    : "Badges will appear here after the app seeds them."));
        } else {
            for (Badge badge : badges) {
                grid.getChildren().add(badgeCard(badge));
            }
        }
        content.getChildren().addAll(summary, count, grid);
        return fitScroll(content);
    }

    private static VBox emptyBadgeCard(String message) {
        VBox card = new VBox(10, label(message));
        card.setPadding(new Insets(18));
        card.setPrefSize(260, 160);
        card.setAlignment(Pos.CENTER);
        card.setStyle(panelStyle());
        return card;
    }

    private static VBox badgeCard(Badge badge) {
        ImageView icon = badgeIcon(badge, 156);
        Label name = h2(badge.getName());
        name.setWrapText(true);
        Label status = mutedLabel(badge.isEarned() ? "Earned " + badge.getEarnedAt() : "Locked");
        Label description = label(badge.getDescription());
        description.setWrapText(true);
        description.setMaxWidth(210);
        Label category = badge(badge.getCategory());
        VBox card = new VBox(9, icon, name, category, status, description);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(14));
        card.setPrefSize(250, 330);
        card.setMaxWidth(250);
        card.setStyle(panelStyle());
        card.setOpacity(badge.isEarned() ? 1.0 : 0.55);
        return card;
    }

    private static ImageView badgeIcon(Badge badge, int size) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        String path = badge == null ? null : badge.getImagePath();
        if (path != null && !path.isBlank()) {
            try (var stream = ScreenFactory.class.getResourceAsStream(path)) {
                if (stream != null) {
                    imageView.setImage(new Image(stream));
                }
            } catch (Exception ignored) {
            }
        }
        return imageView;
    }

    private static Parent profileEditor(User user, ProfileService service) {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle(panelStyle());
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
        panel.setPadding(new Insets(18));
        panel.setStyle(panelStyle());
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

        VBox panel = new VBox(12, new HBox(10, create, backButton(backToHomeText(), homeScreen())), problems, emptyState);
        VBox.setVgrow(problems, Priority.ALWAYS);
        panel.setPadding(new Insets(18));
        panel.setStyle(panelStyle());
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
        area.setStyle(inputStyle());
        return area;
    }

    private static TextField wideInput(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle(inputStyle());
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
        panel.setPadding(new Insets(18));
        panel.setStyle(panelStyle());
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
        panel.setPadding(new Insets(18));
        panel.setStyle(panelStyle());
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
        StackPane root = authRoot();
        Pane background = authBackground();
        background.prefWidthProperty().bind(root.widthProperty());
        background.prefHeightProperty().bind(root.heightProperty());

        Label title = h1("Login Required");
        Label body = label(message);
        body.setWrapText(true);
        body.setAlignment(Pos.CENTER);
        body.setMaxWidth(520);
        Button login = primaryButton("Login");
        login.setOnAction(event -> NavigationUtil.navigateTo("login", login));
        Button register = primaryButton("Create Account");
        register.setOnAction(event -> NavigationUtil.navigateTo("register", register));
        Button problems = primaryButton("Browse Problems");
        problems.setOnAction(event -> NavigationUtil.navigateTo("problem-list", problems));
        HBox actions = new HBox(12, login, register, problems);
        actions.setAlignment(Pos.CENTER);

        VBox card = new VBox(16, logoImage(themeLogoPath(), isDarkMode() ? 92 : 125), title, body, actions);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(620);
        card.setStyle(panelStyle());
        root.getChildren().addAll(background, card);
        return root;
    }

    private static BorderPane shell(String title) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0));
        root.setStyle(pageStyle());
        root.setLeft(sidebar(title));
        return root;
    }

    private static VBox page() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(30));
        root.setStyle(pageStyle());
        return root;
    }

    private static VBox contentPage() {
        VBox root = page();
        root.setMaxWidth(Double.MAX_VALUE);
        return root;
    }

    private static VBox shellContent(String title) {
        VBox content = contentPage();
        content.getChildren().add(h1(title));
        return content;
    }

    private static void setShellContent(BorderPane root, String title, Node content) {
        VBox page = shellContent(title);
        if (content instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
        page.getChildren().add(content);
        VBox.setVgrow(content, Priority.ALWAYS);
        root.setCenter(page);
    }

    private static ScrollPane fitScroll(Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private static StackPane authRoot() {
        StackPane root = new StackPane();
        root.setStyle(pageStyle());
        return root;
    }

    private static Pane authBackground() {
        Pane background = new Pane();
        background.setStyle(splashBackgroundStyle());
        background.getChildren().addAll(
                glowCircle(120, -80, 330, isDarkMode() ? "#C83CFF22" : "#FF9F2E35"),
                glowCircle(1120, 700, 450, isDarkMode() ? "#19D3F322" : "#E86F1B22"),
                arenaRing(450, 360, 390, isDarkMode() ? "#243762AA" : "#F3B26D88"),
                arenaRing(450, 360, 250, isDarkMode() ? "#19D3F344" : "#E86F1B55"),
                accentLine(760, 170, 1120, 120, isDarkMode() ? "#19D3F377" : "#FFB347AA"));
        return background;
    }

    private static VBox featureCard(String title, String body) {
        Label heading = h2(title);
        Label copy = mutedLabel(body);
        copy.setWrapText(true);
        VBox card = new VBox(5, heading, copy);
        card.setPadding(new Insets(14));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(featureCardStyle());
        return card;
    }

    private static VBox header(String title) {
        HBox titleRow = new HBox(12, h1(title), spacer());
        titleRow.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(8, titleRow);
        header.setPadding(new Insets(26, 30, 8, 30));
        return header;
    }

    private static VBox headerWithBack(String title, String backScreen) {
        return new VBox(10, header(title), backButton("Back", backScreen));
    }

    private static Label h1(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + textColor() + ";");
        return label;
    }

    private static Label h2(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + textColor() + ";");
        return label;
    }

    private static Label label(String text) {
        Label label = new Label(text == null ? "" : text);
        label.setStyle("-fx-text-fill: " + textColor() + ";");
        return label;
    }

    private static Label mutedLabel(String text) {
        Label label = label(text);
        label.setStyle("-fx-text-fill: " + mutedTextColor() + ";");
        return label;
    }

    private static String formatSeconds(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int minutes = safeSeconds / 60;
        int seconds = safeSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private static Label badge(String text) {
        Label label = label(text);
        label.setStyle("-fx-background-color: " + successPillColor()
                + "; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
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
        field.setStyle(inputStyle());
        return field;
    }

    private static PasswordField passwordInput(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setMaxWidth(360);
        field.setStyle(inputStyle());
        return field;
    }

    private static Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(primaryButtonStyle());
        return button;
    }

    private static Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(secondaryButtonStyle());
        return button;
    }

    private static Button dangerButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + (isDarkMode() ? "#DA3633" : "#CF222E")
                + "; -fx-text-fill: white; -fx-font-weight: bold;");
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
        panel.setPadding(new Insets(20));
        panel.setStyle(panelStyle());
        panel.getChildren().add(h2(title));
        panel.getChildren().addAll(children);
        panel.setMaxWidth(Double.MAX_VALUE);
        return panel;
    }

    private static VBox statCard(String title, String value, String detail, String accent) {
        Label icon = new Label(" ");
        icon.setMinSize(42, 42);
        icon.setMaxSize(42, 42);
        icon.setStyle("-fx-background-color: " + accent
                + "33; -fx-border-color: " + accent
                + "; -fx-border-width: 1; -fx-background-radius: 21; -fx-border-radius: 21;");
        Label valueLabel = h2(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + textColor() + ";");
        VBox text = new VBox(3, mutedLabel(title), valueLabel, mutedLabel(detail));
        HBox card = new HBox(14, icon, text);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18));
        card.setMinHeight(112);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(panelStyle());
        HBox.setHgrow(card, Priority.ALWAYS);
        VBox wrapper = new VBox(card);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    private static Button wideNavButton(String text, String screen) {
        Button button = secondaryButton(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(42);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(event -> NavigationUtil.navigateTo(screen, button));
        return button;
    }

    private static TableView<User> compactLeaderboard(List<User> users) {
        TableView<User> table = new TableView<>(FXCollections.observableArrayList(
                users == null ? List.of() : users.stream().limit(6).toList()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        TableColumn<User, Number> position = column("#", 54);
        position.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(table.getItems().indexOf(cell.getValue()) + 1));
        table.getColumns().addAll(position,
                stringColumn("Coder", 180, User::getUsername),
                numberColumn("XP", 90, User::getXp));
        table.setPrefHeight(260);
        return table;
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
        return backButton("Back", SessionManager.isLoggedIn() ? homeScreen() : "login");
    }

    private static String homeScreen() {
        return isCurrentUserAdmin() ? "admin-panel" : "dashboard";
    }

    private static String backToHomeText() {
        return isCurrentUserAdmin() ? "Back to Admin Dashboard" : "Back";
    }

    private static boolean isCurrentUserAdmin() {
        User user = SessionManager.getCurrentUser();
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private static Button backButton(String text, String screen) {
        Button button = secondaryButton(text);
        button.setOnAction(event -> NavigationUtil.navigateTo(screen, button));
        return button;
    }

    private static Button copyCodeButton(Label codeValue, Label message) {
        Button button = secondaryButton("Copy");
        button.setOnAction(event -> {
            String code = codeValue == null ? "" : codeValue.getText();
            if (code == null || code.isBlank() || "No code yet".equalsIgnoreCase(code.trim())) {
                if (message != null) {
                    message.setText("Create a match first, then copy the code.");
                }
                return;
            }
            ClipboardContent content = new ClipboardContent();
            content.putString(code.trim());
            Clipboard.getSystemClipboard().setContent(content);
            if (message != null) {
                message.setText("Battle code copied.");
            }
        });
        return button;
    }

    private static ChoiceBox<String> languageChoice() {
        ChoiceBox<String> language = new ChoiceBox<>(FXCollections.observableArrayList("Java", "Python"));
        language.setValue("Java");
        language.setStyle(inputStyle());
        return language;
    }

    private static String templateForLanguage(String language) {
        return "Python".equalsIgnoreCase(language) ? PYTHON_TEMPLATE : TEMPLATE;
    }

    private static String normalizeLanguage(String language) {
        return "Python".equalsIgnoreCase(language == null ? "" : language.trim()) ? "Python" : "Java";
    }

    private static Button themeToggleButton(String screen) {
        Button button = secondaryButton(isDarkMode() ? "Light Mode" : "Dark Mode");
        button.setOnAction(event -> {
            setDarkMode(!isDarkMode());
            NavigationUtil.navigateTo(screen == null || screen.isBlank() ? currentScreenName : screen, button);
        });
        return button;
    }

    private static Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private static VBox sidebar(String title) {
        VBox sidebar = new VBox(18);
        sidebar.setPadding(new Insets(28, 18, 24, 18));
        sidebar.setPrefWidth(250);
        sidebar.setMinWidth(230);
        sidebar.setStyle(sidebarStyle());

        ImageView logoMark = logoImage(isDarkMode() ? 72 : 58);
        VBox logoText = new VBox(2, h2("CodeArena"), mutedLabel(title == null || title.isBlank() ? "Dashboard" : title));
        HBox logo = new HBox(10, logoMark, logoText);
        logo.setAlignment(Pos.CENTER_LEFT);

        VBox nav = new VBox(8,
                sidebarNavButton("Dashboard", "dashboard"),
                sidebarNavButton("Problems", "problem-list"),
                sidebarNavButton("Leaderboard", "leaderboard"),
                sidebarNavButton("Battle", "battle-lobby"),
                sidebarNavButton("Squad", "squad"),
                sidebarNavButton("Profile", "profile"));
        if (isCurrentUserAdmin()) {
            nav.getChildren().add(sidebarNavButton("Admin Panel", "admin-panel"));
        }

        Label section = mutedLabel("Navigation");
        section.setStyle("-fx-text-fill: " + mutedTextColor() + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        Button about = secondaryButton("About");
        about.setMaxWidth(Double.MAX_VALUE);
        about.setOnAction(event -> showAboutDialog());
        sidebar.getChildren().addAll(logo, new Separator(), section, nav, spacerVBox(), about,
                themeToggleButton(currentScreenName));
        return sidebar;
    }

    private static Region spacerVBox() {
        Region region = new Region();
        VBox.setVgrow(region, Priority.ALWAYS);
        return region;
    }

    private static Button sidebarNavButton(String text, String screen) {
        Button button = new Button(text);
        boolean active = currentScreenName.equals(screen);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(42);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(active ? activeSidebarButtonStyle() : sidebarButtonStyle());
        button.setOnAction(event -> NavigationUtil.navigateTo(screen, button));
        return button;
    }

    private static ImageView logoImage(double size) {
        return logoImage(themeLogoPath(), size);
    }

    private static ImageView logoImage(String resourcePath, double size) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        try (var stream = ScreenFactory.class.getResourceAsStream(resourcePath)) {
            if (stream != null) {
                imageView.setImage(new Image(stream));
            }
        } catch (Exception ignored) {
        }
        return imageView;
    }

    private static Circle glowCircle(double centerX, double centerY, double radius, String color) {
        Circle circle = new Circle(centerX, centerY, radius);
        circle.setStyle("-fx-fill: " + color + ";");
        return circle;
    }

    private static Circle arenaRing(double centerX, double centerY, double radius, String color) {
        Circle circle = new Circle(centerX, centerY, radius);
        circle.setStyle("-fx-fill: transparent; -fx-stroke: " + color + "; -fx-stroke-width: 2;");
        return circle;
    }

    private static Line accentLine(double startX, double startY, double endX, double endY, String color) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2;");
        return line;
    }

    private static Label codeAccent(String text, double x, double y) {
        Label label = new Label(text);
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setStyle("-fx-text-fill: #A8B3D633; -fx-font-size: 44px; -fx-font-weight: bold;");
        return label;
    }

    private static Polygon diagonalPanel(double startX, double startY, double endX, double endY, String color) {
        double width = 150;
        Polygon polygon = new Polygon(
                startX, startY,
                startX + width, startY,
                endX + width, endY,
                endX, endY);
        polygon.setStyle("-fx-fill: " + color + ";");
        return polygon;
    }

    private static VBox codeBlock(double x, double y) {
        VBox block = new VBox(7,
                ghostCode("01   function solve(challenge) {"),
                ghostCode("02      let skills = sharpen();"),
                ghostCode("03      let code = write();"),
                ghostCode("04      let result = test(code);"),
                ghostCode("05      return result === 'ACCEPTED';"),
                ghostCode("06   }"),
                ghostCode("07   while (true) {"),
                ghostCode("08      learn(); build(); compete();"),
                ghostCode("09   }"));
        block.setLayoutX(x);
        block.setLayoutY(y);
        return block;
    }

    private static Label ghostCode(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace;"
                + " -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #6E8AA533;");
        return label;
    }

    private static Pane dottedField(double x, double y) {
        Pane pane = new Pane();
        pane.setLayoutX(x);
        pane.setLayoutY(y);
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 12; col++) {
                Circle dot = new Circle(col * 16, row * 16, 1.3);
                dot.setStyle("-fx-fill: #3BA7C955;");
                pane.getChildren().add(dot);
            }
        }
        return pane;
    }

    private static VBox caTile() {
        Label ca = new Label("CA");
        ca.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #F6F7FF;");
        Label mark = new Label("━");
        mark.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #F57C00;");
        VBox tile = new VBox(0, ca, mark);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(18));
        tile.setMinSize(96, 96);
        tile.setMaxSize(96, 96);
        tile.setStyle("-fx-background-color: #050B12DD; -fx-border-color: #16334A; -fx-background-radius: 0;");
        return tile;
    }

    private static void showAboutDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.setTitle("About CodeArena");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        ImageView logo = logoImage(themeLogoPath(), 150);
        Label title = h1("CodeArena");
        Label description = label("CodeArena is a local JavaFX coding arena for practicing problems, running submissions through a local judge, earning badges, joining battles, forming squads, tracking leaderboards, and managing content through an admin panel.");
        description.setWrapText(true);
        description.setMaxWidth(560);
        Label developers = label("Developers: Zeeshan Azeem, Sharjeel Ali Khan, Abdul Kabeer");
        developers.setWrapText(true);
        Label stack = mutedLabel("Tech stack: Java 17, JavaFX 21, SQLite, Maven, BCrypt, and a local ProcessBuilder judge.");
        stack.setWrapText(true);
        stack.setMaxWidth(560);

        VBox content = new VBox(12, logo, title, description, developers, stack);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(22));
        dialog.getDialogPane().setContent(content);
        applyTheme(content);
        dialog.showAndWait();
    }

    private static String themeLogoPath() {
        return isDarkMode() ? LOGO_DARK_WORDMARK_PATH : "/images/logo.png";
    }

    private static boolean isDarkMode() {
        return PREFERENCES.getBoolean(DARK_MODE_KEY, true);
    }

    private static void setDarkMode(boolean darkMode) {
        PREFERENCES.putBoolean(DARK_MODE_KEY, darkMode);
    }

    private static String pageStyle() {
        return "-fx-background-color: " + backgroundColor() + ";";
    }

    private static String panelStyle() {
        return "-fx-background-color: " + panelColor()
                + "; -fx-border-color: " + borderColor()
                + "; -fx-border-radius: 10; -fx-background-radius: 10;"
                + " -fx-effect: dropshadow(gaussian, " + shadowColor() + ", 18, 0.12, 0, 8);";
    }

    private static String splashBackgroundStyle() {
        return "-fx-background-color: " + (isDarkMode()
                ? "linear-gradient(to bottom right, #050917, #0A1230 55%, #15164A)"
                : "linear-gradient(to bottom right, #FFFFFF, #FFF6EC 52%, #FFD9A8)") + ";";
    }

    private static String splashHeroStyle() {
        return "-fx-background-color: " + (isDarkMode() ? "#0B1430DD" : "#FFFFFFCC")
                + "; -fx-border-color: " + borderColor()
                + "; -fx-border-width: 1;"
                + "; -fx-border-radius: 16; -fx-background-radius: 16;"
                + " -fx-effect: dropshadow(gaussian, " + shadowColor() + ", 26, 0.18, 0, 10);";
    }

    private static String featureCardStyle() {
        return "-fx-background-color: " + (isDarkMode() ? "#121F42" : "#FFF7EE")
                + "; -fx-border-color: " + borderColor()
                + "; -fx-border-radius: 8; -fx-background-radius: 8;";
    }

    private static String inputStyle() {
        return "-fx-control-inner-background: " + inputColor()
                + "; -fx-background-color: " + inputColor()
                + "; -fx-text-fill: " + textColor()
                + "; -fx-prompt-text-fill: " + mutedTextColor()
                + "; -fx-border-color: " + borderColor()
                + "; -fx-border-radius: 6; -fx-background-radius: 6;"
                + " -fx-padding: 8 10;";
    }

    private static String tableStyle() {
        return "-fx-base: " + panelColor()
                + "; -fx-control-inner-background: " + tableRowColor()
                + "; -fx-background-color: " + panelColor()
                + "; -fx-table-cell-border-color: " + borderColor()
                + "; -fx-table-header-border-color: " + borderColor()
                + "; -fx-text-background-color: " + textColor()
                + "; -fx-selection-bar: " + accentColor()
                + "; -fx-selection-bar-text: white;"
                + " -fx-border-color: " + borderColor()
                + "; -fx-border-radius: 10; -fx-background-radius: 10;";
    }

    private static String tabStyle() {
        return "-fx-base: " + panelColor()
                + "; -fx-background-color: " + panelColor()
                + "; -fx-body-color: " + panelColor()
                + "; -fx-control-inner-background: " + inputColor()
                + "; -fx-text-base-color: " + textColor()
                + "; -fx-mark-color: " + textColor()
                + "; -fx-outer-border: " + borderColor()
                + "; -fx-inner-border: " + borderColor()
                + "; -fx-border-color: " + borderColor() + ";";
    }

    private static String chartStyle() {
        return "-fx-background-color: transparent;"
                + " -fx-text-fill: " + textColor()
                + "; -fx-legend-visible: true;";
    }

    private static String backgroundColor() {
        return isDarkMode()
                ? "linear-gradient(to bottom right, #070B18, #0A1230 55%, #111742)"
                : "linear-gradient(to bottom right, #FFFFFF, #FFF4E8 58%, #FFE0BD)";
    }

    private static String panelColor() {
        return isDarkMode() ? "#101B3A" : "#FFFFFF";
    }

    private static String inputColor() {
        return isDarkMode() ? "#0B1430" : "#FFF9F3";
    }

    private static String textColor() {
        return isDarkMode() ? "#F6F7FF" : "#2B1A10";
    }

    private static String mutedTextColor() {
        return isDarkMode() ? "#A8B3D6" : "#7A5540";
    }

    private static String borderColor() {
        return isDarkMode() ? "#243762" : "#F3B26D";
    }

    private static String accentColor() {
        return isDarkMode() ? "#C83CFF" : "#E86F1B";
    }

    private static String cyanAccentColor() {
        return isDarkMode() ? "#19D3F3" : "#FFB347";
    }

    private static String tableRowColor() {
        return isDarkMode() ? "#0D1733" : "#FFF8EF";
    }

    private static String shadowColor() {
        return isDarkMode() ? "#00000055" : "#C7661F22";
    }

    private static String successPillColor() {
        return isDarkMode() ? "#008A68" : "#E86F1B";
    }

    private static String sidebarStyle() {
        return "-fx-background-color: " + (isDarkMode() ? "#080D20" : "#FFFFFF")
                + "; -fx-border-color: " + borderColor()
                + "; -fx-border-width: 0 1 0 0;";
    }

    private static String primaryButtonStyle() {
        return "-fx-background-color: linear-gradient(to right, " + accentColor() + ", " + cyanAccentColor() + ");"
                + " -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 7;"
                + " -fx-padding: 9 16; -fx-cursor: hand;";
    }

    private static String secondaryButtonStyle() {
        return "-fx-background-color: " + (isDarkMode() ? "#121F42" : "#FFF3E4")
                + "; -fx-text-fill: " + textColor()
                + "; -fx-font-weight: bold; -fx-background-radius: 7;"
                + " -fx-border-color: " + borderColor()
                + "; -fx-border-radius: 7; -fx-padding: 8 14; -fx-cursor: hand;";
    }

    private static String sidebarButtonStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: " + mutedTextColor()
                + "; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;";
    }

    private static String activeSidebarButtonStyle() {
        return "-fx-background-color: " + (isDarkMode() ? "#121F42" : "#FFF0DF")
                + "; -fx-text-fill: " + textColor()
                + "; -fx-font-weight: bold; -fx-background-radius: 8;"
                + " -fx-border-color: " + accentColor()
                + "; -fx-border-width: 0 0 0 3; -fx-padding: 10 12; -fx-cursor: hand;";
    }

    private static void applyTheme(Node node) {
        if (node == null) {
            return;
        }

        if (node instanceof BorderPane) {
            if (!hasCustomPanelStyle(node)) {
                node.setStyle(pageStyle());
            }
        }
        if (node instanceof Label label && !(node instanceof Button)) {
            String style = label.getStyle() == null ? "" : label.getStyle();
            if (!style.contains("-fx-text-fill")) {
                label.setStyle(style + "; -fx-text-fill: " + textColor() + ";");
            }
        }
        if (node instanceof TextInputControl input) {
            String prefix = input.getStyle() == null ? "" : input.getStyle();
            input.setStyle(prefix + "; " + inputStyle());
        }
        if (node instanceof ChoiceBox<?> choiceBox) {
            choiceBox.setStyle("-fx-background-color: " + inputColor()
                    + "; -fx-mark-color: " + textColor()
                    + "; -fx-border-color: " + borderColor()
                    + "; -fx-border-radius: 4;");
        }
        if (node instanceof TableView<?> tableView) {
            tableView.setMinHeight(0);
            tableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            tableView.setStyle(tableStyle());
        }
        if (node instanceof TabPane tabPane) {
            tabPane.setStyle(tabStyle());
            for (Tab tab : tabPane.getTabs()) {
                tab.setStyle("-fx-background-color: " + panelColor()
                        + "; -fx-text-base-color: " + textColor()
                        + "; -fx-focus-color: " + accentColor() + ";");
            }
        }
        if (node instanceof ScrollPane scrollPane) {
            scrollPane.setStyle("-fx-background: " + backgroundColor() + "; -fx-background-color: transparent;");
        }

        if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                applyTheme(child);
            }
        } else if (node instanceof BorderPane borderPane) {
            applyTheme(borderPane.getTop());
            applyTheme(borderPane.getRight());
            applyTheme(borderPane.getBottom());
            applyTheme(borderPane.getLeft());
            applyTheme(borderPane.getCenter());
        } else if (node instanceof ScrollPane scrollPane) {
            applyTheme(scrollPane.getContent());
        } else if (node instanceof TabPane tabPane) {
            for (Tab tab : tabPane.getTabs()) {
                applyTheme(tab.getContent());
            }
        }
    }

    private static boolean hasCustomPanelStyle(Node node) {
        String style = node.getStyle();
        return style != null && (style.contains("-fx-border-color") || style.contains("-fx-background-radius"));
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
        if (dialog == null || dialog.getDialogPane() == null) {
            return;
        }
        dialog.getDialogPane().setStyle("-fx-background-color: " + backgroundColor()
                + "; -fx-border-color: " + borderColor()
                + "; -fx-border-width: 1;");
        dialog.getDialogPane().contentProperty().addListener((observable, oldContent, newContent) -> applyTheme(newContent));
        applyTheme(dialog.getDialogPane());
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

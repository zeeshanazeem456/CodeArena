# ARCHITECTURE.md — CodeArena

> Version: 1.0 | Pattern: MVC | Database: SQLite | Build: Maven

---

## 1. High-Level Architecture

```
┌─────────────────────────────────────────────────────┐
│                   JavaFX UI Layer                   │
│         FXML Files + XxxController.java             │
└───────────────────┬─────────────────────────────────┘
                    │ calls
┌───────────────────▼─────────────────────────────────┐
│                 Service Layer                        │
│      AuthService, JudgeService, BattleService...    │
└───────────────────┬─────────────────────────────────┘
                    │ calls
┌───────────────────▼─────────────────────────────────┐
│                   DAO Layer                         │
│       UserDAO, ProblemDAO, SubmissionDAO...         │
└───────────────────┬─────────────────────────────────┘
                    │ uses
┌───────────────────▼─────────────────────────────────┐
│              SQLite Database                        │
│         ~/.codearena/codearena.db                   │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│               Judge Engine (Parallel)               │
│   JudgeEngine → ProcessBuilder → javac / java       │
│         Runs on javafx.concurrent.Task              │
└─────────────────────────────────────────────────────┘
```

---

## 2. Maven Project Structure

```
src/
└── main/
    ├── java/
    │   └── com/codearena/
    │       ├── MainApp.java                  ← Entry point, extends Application
    │       ├── model/
    │       │   ├── User.java
    │       │   ├── Problem.java
    │       │   ├── TestCase.java
    │       │   ├── Submission.java
    │       │   ├── Battle.java
    │       │   ├── Squad.java
    │       │   └── SquadMember.java
    │       ├── dao/
    │       │   ├── UserDAO.java
    │       │   ├── ProblemDAO.java
    │       │   ├── TestCaseDAO.java
    │       │   ├── SubmissionDAO.java
    │       │   ├── BattleDAO.java
    │       │   └── SquadDAO.java
    │       ├── service/
    │       │   ├── AuthService.java
    │       │   ├── ProblemService.java
    │       │   ├── JudgeService.java
    │       │   ├── BattleService.java
    │       │   ├── LeaderboardService.java
    │       │   └── SquadService.java
    │       ├── controller/
    │       │   ├── LoginController.java
    │       │   ├── RegisterController.java
    │       │   ├── DashboardController.java
    │       │   ├── ProblemListController.java
    │       │   ├── ProblemDetailController.java
    │       │   ├── CodeEditorController.java
    │       │   ├── LeaderboardController.java
    │       │   ├── BattleLobbyController.java
    │       │   ├── BattleArenaController.java
    │       │   ├── ProfileController.java
    │       │   ├── SquadController.java
    │       │   └── AdminPanelController.java
    │       ├── judge/
    │       │   ├── JudgeEngine.java
    │       │   ├── Sandbox.java
    │       │   ├── TestCaseRunner.java
    │       │   └── Verdict.java
    │       └── util/
    │           ├── DBConnection.java
    │           ├── SchemaInitializer.java
    │           ├── NavigationUtil.java
    │           ├── SessionManager.java
    │           └── XPCalculator.java
    └── resources/
        ├── fxml/
        │   ├── login.fxml
        │   ├── register.fxml
        │   ├── dashboard.fxml
        │   ├── problem-list.fxml
        │   ├── problem-detail.fxml
        │   ├── code-editor.fxml
        │   ├── leaderboard.fxml
        │   ├── battle-lobby.fxml
        │   ├── battle-arena.fxml
        │   ├── profile.fxml
        │   ├── squad.fxml
        │   └── admin-panel.fxml
        ├── css/
        │   ├── global.css
        │   ├── editor.css
        │   └── battle.css
        └── images/
            ├── logo.png
            └── ranks/
```

---

## 3. Database Schema

### users
```sql
CREATE TABLE users (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username    TEXT    NOT NULL UNIQUE,
    email       TEXT    NOT NULL UNIQUE,
    password    TEXT    NOT NULL,              -- BCrypt hash
    role        TEXT    NOT NULL DEFAULT 'CODER', -- CODER | ADMIN
    xp          INTEGER NOT NULL DEFAULT 0,
    rank_title  TEXT    NOT NULL DEFAULT 'Novice',
    problems_solved INTEGER NOT NULL DEFAULT 0,
    battles_won INTEGER NOT NULL DEFAULT 0,
    battles_lost INTEGER NOT NULL DEFAULT 0,
    streak_days INTEGER NOT NULL DEFAULT 0,
    last_active TEXT,                         -- ISO date string
    squad_id    INTEGER REFERENCES squads(id),
    created_at  TEXT    NOT NULL DEFAULT (datetime('now')),
    is_active   INTEGER NOT NULL DEFAULT 1    -- 0 = banned
);
```

### problems
```sql
CREATE TABLE problems (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    title        TEXT    NOT NULL,
    description  TEXT    NOT NULL,
    difficulty   TEXT    NOT NULL,             -- Easy | Medium | Hard
    category     TEXT,
    tags         TEXT,                         -- comma-separated
    time_limit   INTEGER NOT NULL DEFAULT 5,  -- seconds
    memory_limit INTEGER NOT NULL DEFAULT 256, -- MB
    is_published INTEGER NOT NULL DEFAULT 0,
    created_by   INTEGER REFERENCES users(id),
    created_at   TEXT    NOT NULL DEFAULT (datetime('now'))
);
```

### test_cases
```sql
CREATE TABLE test_cases (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    problem_id  INTEGER NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    input       TEXT    NOT NULL,
    expected    TEXT    NOT NULL,
    is_sample   INTEGER NOT NULL DEFAULT 0    -- 1 = visible to user
);
```

### submissions
```sql
CREATE TABLE submissions (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL REFERENCES users(id),
    problem_id  INTEGER NOT NULL REFERENCES problems(id),
    code        TEXT    NOT NULL,
    language    TEXT    NOT NULL DEFAULT 'Java',
    verdict     TEXT    NOT NULL,             -- AC | WA | TLE | CE | RE | MLE
    runtime_ms  INTEGER,
    battle_id   INTEGER REFERENCES battles(id),
    submitted_at TEXT   NOT NULL DEFAULT (datetime('now'))
);
```

### battles
```sql
CREATE TABLE battles (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    player1_id   INTEGER NOT NULL REFERENCES users(id),
    player2_id   INTEGER NOT NULL REFERENCES users(id),
    problem_id   INTEGER NOT NULL REFERENCES problems(id),
    winner_id    INTEGER REFERENCES users(id),  -- NULL = draw/in-progress
    status       TEXT    NOT NULL DEFAULT 'PENDING', -- PENDING | ACTIVE | FINISHED
    started_at   TEXT,
    finished_at  TEXT,
    time_limit   INTEGER NOT NULL DEFAULT 1800  -- seconds (30 min)
);
```

### squads
```sql
CREATE TABLE squads (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    description TEXT,
    leader_id   INTEGER NOT NULL REFERENCES users(id),
    created_at  TEXT    NOT NULL DEFAULT (datetime('now'))
);
```

### XP Rank Thresholds
```
Novice      →    0 – 499 XP
Apprentice  →  500 – 1499 XP
Warrior     → 1500 – 3499 XP
Elite       → 3500 – 6999 XP
Champion    → 7000 – 11999 XP
Legend      → 12000+ XP
```

---

## 4. Judge Engine Flow

```
User clicks "Submit"
        │
        ▼
CodeEditorController.handleSubmit()
        │  creates Submission object
        ▼
JudgeService.evaluate(submission)
        │  wraps in javafx.concurrent.Task
        ▼
JudgeEngine.runSubmission(submission)
        │
        ├──► Sandbox.createTempDir(submissionId)
        │         /tmp/codearena/{id}/Solution.java
        │
        ├──► [COMPILE] ProcessBuilder("javac", "Solution.java")
        │         ├── CE → return Verdict.CE, stop
        │         └── Success → continue
        │
        ├──► For each TestCase:
        │         ProcessBuilder("java", "Solution")
        │         ├── stdin  ← testCase.input
        │         ├── stdout → captured
        │         ├── timeout 5s → TLE
        │         ├── exit code != 0 → RE
        │         ├── stdout.trim() != expected.trim() → WA
        │         └── match → AC
        │
        ├──► Overall verdict = worst case across all test cases
        │
        ├──► Sandbox.cleanup(submissionId)   ← always runs (finally block)
        │
        └──► SubmissionDAO.save(submission)
                  │
                  └──► Platform.runLater() → update UI with result
```

---

## 5. Navigation Flow

```
[Login Screen]
    ├── Login Success (CODER)  → [Dashboard]
    ├── Login Success (ADMIN)  → [Admin Panel]
    └── Register Link          → [Register Screen]

[Dashboard]
    ├── Problems Button        → [Problem List]
    ├── Battle Button          → [Battle Lobby]
    ├── Leaderboard Button     → [Leaderboard]
    ├── Squad Button           → [Squad Screen]
    └── Profile Button         → [Profile Screen]

[Problem List]
    └── Click Problem          → [Problem Detail]
                                      └── Start Coding → [Code Editor]

[Battle Lobby]
    └── Match Found / Challenge Accepted → [Battle Arena]

[All Screens]
    └── Logout Button          → [Login Screen]
```

---

## 6. Key Utility Classes

### DBConnection.java
```java
// Singleton pattern — one connection for the app lifetime
public class DBConnection {
    private static Connection connection;
    public static Connection getConnection() throws SQLException { ... }
}
```

### SessionManager.java
```java
// Holds the logged-in user for the session
public class SessionManager {
    private static User currentUser;
    public static void setCurrentUser(User u) { ... }
    public static User getCurrentUser() { ... }
    public static void clearSession() { ... }
}
```

### NavigationUtil.java
```java
// Switches scenes from any controller
public class NavigationUtil {
    public static void navigateTo(String fxmlPath, Stage stage) { ... }
    public static void navigateTo(String fxmlPath, ActionEvent event) { ... }
}
```

### XPCalculator.java
```java
// Centralizes all XP award logic
public class XPCalculator {
    public static int forSolvingProblem(String difficulty) { ... } // Easy=10, Med=25, Hard=50
    public static int forWinningBattle() { return 75; }
    public static int forDailyStreak() { return 20; }
    public static String getRankTitle(int xp) { ... }
}
```

---

## 7. Threading Model

| Operation | Thread |
|---|---|
| DB reads for UI population | `Task<List<T>>` → `setOnSucceeded` → `Platform.runLater()` |
| Judge execution | `Task<Verdict>` with progress updates |
| Battle timer countdown | `Timeline` (JavaFX animation timer) |
| App startup / schema init | Main JavaFX Application thread (before first scene) |

**Rule:** If it touches the DB or runs a process, it does NOT run on the FX Application Thread.

---

## 8. Development Phases

| Phase | Scope |
|---|---|
| 1 | Project setup, Maven config, DBConnection, SchemaInitializer |
| 2 | User model, UserDAO, AuthService, Login + Register screens |
| 3 | Problem model, ProblemDAO, ProblemService, Problem List + Detail screens |
| 4 | Judge engine (JudgeEngine, Sandbox, TestCaseRunner), Code Editor screen |
| 5 | Submission model, SubmissionDAO, submission history |
| 6 | RPG system: XPCalculator, rank logic, profile screen |
| 7 | Leaderboard screen and LeaderboardService |
| 8 | Battle system: BattleDAO, BattleService, Battle Lobby + Arena screens |
| 9 | Squad system + Admin Panel |

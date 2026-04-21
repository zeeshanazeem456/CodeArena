# AGENTS.md — CodeArena Codex Instructions

> This file is for AI coding agents (OpenAI Codex, GitHub Copilot Workspace, etc.).
> Read this entire file before generating or modifying any code.

---

## Project Identity

**CodeArena** is a competitive coding desktop application — NOT a web app, NOT a mobile app.
It is a local Java desktop application built with JavaFX and backed by an embedded SQLite database.

Do NOT generate:
- HTML / CSS / JavaScript
- Spring Boot, REST APIs, or servlets
- Android or iOS code
- Any networked database (MySQL, PostgreSQL, SQL Server)

---

## Tech Stack (Non-Negotiable)

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | JavaFX 21 with FXML |
| Build System | Maven (`pom.xml` is source of truth) |
| Database | SQLite via `org.xerial:sqlite-jdbc` |
| Judge Engine | `java.lang.ProcessBuilder` |
| Threading | `javafx.concurrent.Task` |
| Password Hashing | BCrypt (`org.mindrot:jbcrypt`) |

---

## Repository Structure

```
CodeArena/
├── AGENTS.md
├── REQUIREMENTS.md
├── ARCHITECTURE.md
├── pom.xml
├── data/
│   ├── schema.sql          ← Full DB schema (run on first launch)
│   └── seed.sql            ← Sample problems, test cases, users
├── docs/
│   ├── judge-system.md
│   ├── database-schema.md
│   └── ui-screens.md
└── src/
    └── main/
        ├── java/com/codearena/
        │   ├── MainApp.java
        │   ├── model/          ← POJOs only
        │   ├── dao/            ← SQL queries only
        │   ├── service/        ← Business logic only
        │   ├── controller/     ← JavaFX controllers only
        │   ├── judge/          ← Judge engine only
        │   └── util/           ← Helpers and utilities
        └── resources/
            ├── fxml/           ← All .fxml screen files
            ├── css/            ← All .css stylesheets
            └── images/         ← Icons and assets
```

---

## Strict MVC Architecture Rules

### model/
- Plain Java objects (POJOs) only
- Fields, constructors, getters, setters, `toString()`
- **NO** JavaFX imports, **NO** SQL, **NO** business logic
- Example: `User.java`, `Problem.java`, `Submission.java`, `Battle.java`

### dao/
- One DAO class per model entity
- All SQL lives here — nowhere else
- Use `PreparedStatement` only — never string-concatenated SQL
- Always call `DBConnection.getConnection()` to get the connection
- Return model objects or `List<ModelObject>` — never raw ResultSets
- Example: `UserDAO.java`, `ProblemDAO.java`, `SubmissionDAO.java`

### service/
- All business logic and validation lives here
- Controllers call services. Services call DAOs. Never skip a layer.
- Example: `AuthService.java`, `JudgeService.java`, `BattleService.java`

### controller/
- JavaFX controllers wired to FXML files only
- No SQL, no business logic
- Inject services via constructor or field injection
- Any UI update from a background thread **MUST** use `Platform.runLater()`
- Example: `LoginController.java`, `ProblemListController.java`

### judge/
- `JudgeEngine.java` — core execution using ProcessBuilder
- `Sandbox.java` — temp file/directory management
- `TestCaseRunner.java` — runs code against one test case, returns verdict
- `Verdict.java` — enum: `AC, WA, TLE, CE, RE, MLE`

### util/
- `DBConnection.java` — singleton SQLite connection
- `NavigationUtil.java` — scene/screen switching helper
- `SessionManager.java` — holds the currently logged-in User object

---

## Coding Rules Codex Must Follow

1. **Never put SQL in a Controller or Service.** SQL belongs in DAO only.
2. **Never update JavaFX UI directly from a non-FX thread.** Always wrap in `Platform.runLater()`.
3. **All long-running operations** (judge execution, DB queries in loops) must use `javafx.concurrent.Task`.
4. **Every new screen** requires three things: an `.fxml` file in `resources/fxml/`, a `XxxController.java`, and a navigation entry in `NavigationUtil.java`.
5. **Naming conventions:**
   - Models: `User.java`, `Problem.java`
   - DAOs: `UserDAO.java`, `ProblemDAO.java`
   - Services: `AuthService.java`, `JudgeService.java`
   - Controllers: `LoginController.java`, `DashboardController.java`
   - FXML: `login.fxml`, `dashboard.fxml`
6. **Use lambda expressions** for JavaFX event handlers — never anonymous inner classes.
7. **Do not add Maven dependencies** without a comment in `pom.xml` explaining why.
8. **All user passwords** must be hashed with BCrypt before storing. Never store plaintext.
9. **Temp files for judge** go in `/tmp/codearena/{submissionId}/`. Always clean up after execution.
10. **Judge timeout** is 5 seconds per test case. Use `process.waitFor(5, TimeUnit.SECONDS)`.

---

## Team & Ownership

| Member | Role | Owns |
|---|---|---|
| Zeeshan | Tech Lead & Judge System | `judge/`, `service/`, `dao/`, `util/`, `model/` |
| Sharjeel | Frontend Engineer | `controller/`, `resources/fxml/`, `resources/css/` |
| Kabeer | Data & Content Lead | `data/seed.sql`, problem content, test cases |

When generating code, respect ownership boundaries. Judge/backend changes belong in Zeeshan's domain. FXML/CSS changes belong in Sharjeel's domain.

---

## How to Prompt Codex for This Project

Use this pattern for every task:

> "In `[file path]`, implement `[method/class name]`. It should: [what it does]. Inputs: [params]. Output: [return type / side effects]. Do not modify any other files."

**Example:**
> "In `src/main/java/com/codearena/judge/JudgeEngine.java`, implement `runSubmission(Submission s)`. It should compile the user's Java source using ProcessBuilder, run it against each TestCase from `TestCaseDAO.getByProblemId()`, compare stdout to expected output, assign a Verdict (AC/WA/TLE/CE), update the Submission object, and save via `SubmissionDAO.save()`. Use a 5-second timeout. Do not modify any other files."

---

## What Success Looks Like

- `mvn clean install` passes with no errors
- App launches to the Login screen
- All navigation works without null pointer exceptions
- Judge correctly assigns AC to valid solutions and WA/TLE to bad ones
- No SQL injection vulnerabilities (PreparedStatement everywhere)
- No UI freezes (all heavy work on background threads)

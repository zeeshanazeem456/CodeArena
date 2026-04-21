# 🏟️ CodeArena

> A LeetCode-inspired competitive coding platform — built as a Java desktop application with JavaFX and Maven.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-purple?style=flat-square)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=flat-square&logo=apachemaven)
![SQLite](https://img.shields.io/badge/Database-SQLite-blue?style=flat-square&logo=sqlite)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow?style=flat-square)

---

## 📖 Table of Contents

- [About](#about)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Team](#team)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [Running the App](#running-the-app)
- [Judge System](#judge-system)
- [Screens](#screens)
- [Roadmap](#roadmap)
- [Git Workflow](#git-workflow)

---

## About

CodeArena is a desktop coding platform where users can browse problems, write Java solutions in a built-in code editor, and receive instant feedback from a local judge engine. It is built entirely in Java — frontend in JavaFX, data in SQLite, and the judge using Java's own `ProcessBuilder` to compile and run submitted code.

This is an academic team project. The goal is a fully functional, self-contained desktop app that requires no internet connection, no external server, and no installation beyond the app itself.

---

## Features

- 🔐 User authentication (register / login) with BCrypt password hashing
- 📋 Problem browser with difficulty filters (Easy / Medium / Hard) and search
- ✍️ Built-in code editor with Java syntax highlighting via RichTextFX
- ⚡ Local judge engine — compiles and runs code, validates against test cases
- 🏆 Verdict feedback: Accepted, Wrong Answer, Time Limit Exceeded, Compilation Error
- 📊 Profile screen with XP progression, PieChart, and submission history BarChart
- 🌑 Dark arena-themed UI throughout

---

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17+ |
| Frontend | JavaFX | 21 |
| Build | Maven | 3.8+ |
| Database | SQLite (via JDBC) | 3.45.1.0 |
| Code Editor | RichTextFX | 0.11.2 |
| JSON parsing | Gson | 2.10.1 |
| Password hashing | BCrypt | 0.10.2 |
| Testing | JUnit 5 | 5.10.0 |

> **Why SQLite?** This is a local desktop application. SQLite requires zero server setup, runs entirely from a single `.db` file, and integrates seamlessly with Java via JDBC. SQL Server and PostgreSQL are designed for multi-user networked systems — overkill here. If CodeArena ever moves to a networked/multiplayer model, PostgreSQL would be the migration target.

---

## Team

| Member | Role | Responsibilities |
|---|---|---|
| **Zeeshan** | Tech Lead & Judge System | Maven setup, database layer (DAOs), service layer, judge engine, threading, integration, packaging |
| **Sharjeel** | Frontend Engineer | All FXML screens, CSS dark theme, SceneBuilder layouts, screen navigation, RichTextFX editor integration |
| **Kabeer** | Data & Content Lead | Problem content (20+ problems), test case design, JSON formatting, Profile/Stats charts, XP system, DB seeding script |

---

## Project Structure

```
codearena/
├── pom.xml
├── codearena.db                          ← auto-created on first run (gitignored)
└── src/
    ├── main/
    │   ├── java/com/codearena/
    │   │   ├── MainApp.java              ← entry point, screen navigation
    │   │   ├── controller/
    │   │   │   ├── LoginController.java
    │   │   │   ├── ProblemListController.java
    │   │   │   ├── EditorController.java
    │   │   │   ├── ResultsController.java
    │   │   │   └── ProfileController.java
    │   │   ├── model/
    │   │   │   ├── User.java
    │   │   │   ├── Problem.java
    │   │   │   └── Submission.java
    │   │   ├── service/
    │   │   │   ├── UserService.java
    │   │   │   ├── ProblemService.java
    │   │   │   └── SubmissionService.java
    │   │   ├── dao/
    │   │   │   ├── DatabaseManager.java
    │   │   │   ├── UserDao.java
    │   │   │   ├── ProblemDao.java
    │   │   │   └── SubmissionDao.java
    │   │   └── judge/
    │   │       ├── JudgeEngine.java
    │   │       ├── JudgeResult.java
    │   │       └── TestCase.java
    │   └── resources/
    │       ├── fxml/
    │       │   ├── login.fxml
    │       │   ├── problem-list.fxml
    │       │   ├── editor.fxml
    │       │   ├── results.fxml
    │       │   ├── profile.fxml
    │       │   └── navbar.fxml
    │       ├── css/
    │       │   └── arena-dark.css
    │       └── db/
    │           └── schema.sql
    └── test/java/com/codearena/
```

**Layer rules — strictly enforced:**

- `Controllers` call `Services`. Services call `DAOs`. DAOs touch the database. Never skip a layer.
- Services must have zero JavaFX imports — they are plain Java business logic.
- Controllers must never query the database directly.

---

## Database Schema

```sql
CREATE TABLE users (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  username        TEXT UNIQUE NOT NULL,
  password_hash   TEXT NOT NULL,
  xp              INTEGER DEFAULT 0,
  created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE problems (
  id                  INTEGER PRIMARY KEY AUTOINCREMENT,
  title               TEXT NOT NULL,
  difficulty          TEXT CHECK(difficulty IN ('Easy', 'Medium', 'Hard')),
  description         TEXT,
  function_signature  TEXT,
  test_cases          TEXT   -- JSON: [{"input":"5","expected_output":"25"}]
);

CREATE TABLE submissions (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id       INTEGER REFERENCES users(id),
  problem_id    INTEGER REFERENCES problems(id),
  code          TEXT,
  language      TEXT DEFAULT 'Java',
  status        TEXT,   -- Accepted | Wrong Answer | TLE | Compilation Error
  runtime_ms    INTEGER,
  submitted_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

The `.db` file is auto-created on first run by `DatabaseManager.java` using `schema.sql`. It is **gitignored** — never commit the database file. Commit only `schema.sql`.

---

## Getting Started

### Prerequisites

Make sure every team member has these installed before starting:

- [JDK 17+](https://adoptium.net/) — verify with `java -version`
- [Maven 3.8+](https://maven.apache.org/) — verify with `mvn -version`
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Community or Ultimate)
- [SceneBuilder](https://gluonhq.com/products/scene-builder/) — for Sharjeel's FXML work
- [DB Browser for SQLite](https://sqlitebrowser.org/) — for inspecting the database

### Clone and setup

```bash
git clone https://github.com/your-org/codearena.git
cd codearena
mvn clean install
```

---

## Running the App

```bash
mvn javafx:run
```

> ⚠️ Do **not** run `MainApp.java` directly from IntelliJ without the Maven plugin. JavaFX modules are not on the classpath without `javafx-maven-plugin` — it will crash with a module error.

---

## Judge System

The judge engine is the core of CodeArena. It lives in `com.codearena.judge`.

### Flow

```
User clicks Submit
      │
      ▼
EditorController creates a JavaFX Task<JudgeResult>
      │
      ▼  (background thread)
JudgeEngine.judge(code, testCases)
      │
      ├── Write code to temp .java file
      │
      ├── Invoke javac via ProcessBuilder
      │       └── Compilation Error? → return CE result immediately
      │
      └── For each test case:
              ├── Invoke java via ProcessBuilder
              ├── Pipe input via stdin
              ├── Enforce 2-second timeout (kill if exceeded → TLE)
              └── Compare stdout to expected output → WA or continue
      │
      ▼  (back on UI thread via setOnSucceeded)
Display JudgeResult in Results panel
```

### Verdict types

| Verdict | Meaning | UI Color |
|---|---|---|
| ✅ Accepted | All test cases passed | `#1D9E75` (green) |
| ❌ Wrong Answer | Output did not match expected | `#E24B4A` (red) |
| ⏱️ Time Limit Exceeded | Process ran longer than 2 seconds | `#EF9F27` (amber) |
| 🔴 Compilation Error | `javac` returned non-zero exit code | `#E24B4A` (red) |

---

## Screens

| Screen | FXML | Owner | Description |
|---|---|---|---|
| Login / Register | `login.fxml` | Sharjeel | Username + password fields, toggle between login and register |
| Problem List | `problem-list.fxml` | Sharjeel | TableView with ID, title, difficulty, status — filterable and searchable |
| Code Editor | `editor.fxml` | Sharjeel | SplitPane: problem description (left) + CodeArea with syntax highlighting (right), Run + Submit buttons |
| Results Panel | `results.fxml` | Sharjeel | Verdict badge, runtime, test case breakdown (input / expected / got) |
| Profile & Stats | `profile.fxml` | Sharjeel + Kabeer | XP bar, PieChart (difficulty breakdown), BarChart (submissions over time) |
| Top Navbar | `navbar.fxml` | Sharjeel | Persistent navigation bar shown on all screens except Login |

### Navigation flow

```
App launch → Login screen
Login success → Problem List
Problem row click → Editor screen (receives problem ID)
Run / Submit → Results panel
Results → Back → Problem List
Navbar → Profile → Profile screen
Navbar → Logout → Login screen
```

### UI color palette

| Token | Hex | Used for |
|---|---|---|
| Background | `#1A1A2E` | All root panes |
| Surface | `#16213E` | Cards, panels, sidebars |
| Accent | `#5B4FCC` | Buttons, highlights, active states |
| Success | `#1D9E75` | Accepted verdict |
| Error | `#E24B4A` | Wrong Answer, Compilation Error |
| Warning | `#EF9F27` | Time Limit Exceeded |
| Text primary | `#E8E8E8` | All body text |
| Text muted | `#888780` | Labels, placeholders, timestamps |

---

## Roadmap

| Phase | Goal | Owner |
|---|---|---|
| 0 | Environment setup on all machines | All |
| 1 | Maven skeleton + Hello World JavaFX window | Zeeshan |
| 2 | SQLite schema + DAO layer | Zeeshan |
| 3 | Judge engine (compile, run, validate) | Zeeshan |
| 4 | All FXML screens + dark CSS theme | Sharjeel |
| 5 | 20+ problems with JSON test cases in DB | Kabeer |
| 6 | Service layer + full integration (real data flowing) | Zeeshan + Sharjeel |
| 7 | Profile screen — charts, XP, submission history | Sharjeel + Kabeer |
| 8 | Polish — error handling, edge cases, UI refinements | All |
| 9 | Packaging — fat JAR + jpackage installer | Zeeshan |

---

## Git Workflow

### Branch naming

```
feature/judge-engine        ← Zeeshan
feature/login-screen        ← Sharjeel
feature/problem-data        ← Kabeer
fix/description-of-fix
release/v1.0
```

### Rules

- Never commit directly to `main` — always open a pull request
- `codearena.db` is in `.gitignore` — never commit the database file
- `schema.sql` is committed — any member can recreate the DB from scratch
- Write meaningful commit messages: *what* you did, not *how* (e.g. `Add TLE handling to JudgeEngine`)

### .gitignore entries

```
codearena.db
target/
*.class
*.iml
.idea/
```

---

<div align="center">
  <sub>Built with Java, JavaFX, and Maven · Academic Project</sub>
</div>

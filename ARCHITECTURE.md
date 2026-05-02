# ARCHITECTURE.md - CodeArena

CodeArena is a local JavaFX desktop application using SQLite for persistence and a local Java judge powered by `ProcessBuilder`.

## Layers

```text
JavaFX UI in Java
        ↓
Service layer
        ↓
DAO layer
        ↓
SQLite database

JudgeService
        ↓
JudgeEngine / Sandbox / TestCaseRunner
        ↓
javac and java processes
```

## Project Structure

```text
src/main/java/com/codearena/
├── MainApp.java
├── model/
├── dao/
├── service/
├── ui/
│   └── ScreenFactory.java
├── judge/
└── util/

src/main/resources/
├── data/
│   ├── schema.sql
│   └── seed.sql
└── images/
    └── logo.png
```

## UI Design

The UI is pure JavaFX Java code. Screens are created in `ScreenFactory` and selected by navigation keys such as `login`, `dashboard`, `problem-list`, and `admin-panel`.

`NavigationUtil` switches scenes by asking `ScreenFactory` for the next JavaFX `Parent`. No XML layout loader is used.

## Persistence

The app stores its SQLite database at:

```text
{user.home}/.codearena/codearena.db
```

Startup calls `PersistenceHandler.initialize()`, which opens the database, runs schema creation, and applies seed/repair data. Shutdown calls `PersistenceHandler.shutdown()`.

## Core Tables

- `users`
- `problems`
- `test_cases`
- `submissions`
- `battles`
- `squads`
- `app_metadata`

## Judge Flow

1. The UI builds a `Submission`.
2. `JudgeService` runs the judge on a background JavaFX `Task`.
3. `JudgeEngine` writes `Solution.java` into a sandbox directory.
4. `javac Solution.java` compiles the code.
5. `java -cp . Solution` runs the code for each test case.
6. `TestCaseRunner` compares normalized output to expected output.
7. The worst verdict is saved on the submission.
8. `Sandbox.cleanup()` always removes temporary files.

## Rank Thresholds

```text
Novice        0+
Apprentice    500+
Warrior       1500+
Elite         3500+
Champion      7000+
Legend        12000+
```

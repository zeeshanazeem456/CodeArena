# CodeArena

CodeArena is a local competitive-coding desktop application built with Java 17, JavaFX 21, Maven, and SQLite. It is a desktop app: there is no web frontend, REST API, Spring Boot layer, HTML, CSS, or JavaScript UI.

## Features

- Registration and login with BCrypt password hashing
- Guest problem browsing and public leaderboard
- SQLite schema and seed initialization on startup
- Problem list, search, difficulty/category filters, details, samples, and acceptance rate
- Java and Python submissions judged locally through `ProcessBuilder`
- Run against samples, submit against all test cases, and view per-case verdict output
- XP, ranks, streaks, badges, profile editing, and submission/battle history
- 1v1, free-for-all, and random 1v1 battle flows
- Squad creation, joining, member list, and squad leaderboard
- Admin panel for users, problems, test cases, submissions, and analytics

## Requirements

- Java 17+
- Maven 3.8+
- `javac` and `java` available on `PATH`
- Optional for Python submissions: `python`, `python3`, or Windows `py -3` available on `PATH`

Maven downloads JavaFX 21 and the other project dependencies declared in `pom.xml`.

## Run

```powershell
mvn javafx:run
```

The main class is `com.codearena.MainApp`.

## Tests

```powershell
mvn test
```

The tests use a separate SQLite database at `target/test-data/codearena-test.db`, so they do not touch the local app database.

## Database

By default, the app stores SQLite data here:

```text
data/codearena.db
```

To run with a different database file:

```powershell
mvn javafx:run -Dcodearena.db.path="C:\path\to\codearena.db"
```

You can also set:

```powershell
$env:CODEARENA_DB_PATH = "C:\path\to\codearena.db"
mvn javafx:run
```

Startup calls `PersistenceHandler.initialize()`, which opens the database, creates missing schema objects from `src/main/resources/data/schema.sql`, and applies seed/demo content from `src/main/resources/data/seed.sql` plus `SeedInitializer`.

## Reset Local Data

Close the app, then delete the local database:

```powershell
Remove-Item -LiteralPath data/codearena.db
mvn javafx:run
```

On the next launch, CodeArena recreates the schema and seed/demo data from resources.

## Demo Logins

```text
Admin:
username: admin
password: admin123

Demo coders:
username: byteknight
username: loopwizard
username: stackrider
password: admin123
```

## Source Layout

```text
src/main/java/com/codearena/
├── MainApp.java
├── model/
├── dao/
├── service/
├── ui/
├── judge/
└── util/

src/main/resources/
├── data/
└── images/
```

## Notes

- UI screens are built directly in JavaFX Java code in `ScreenFactory`.
- SQL belongs in DAO classes or persistence initializers.
- Long-running judge work runs off the JavaFX application thread.
- Judge temp files are created under the OS temp directory and cleaned after execution.

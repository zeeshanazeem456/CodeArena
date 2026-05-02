# CodeArena

CodeArena is a local competitive coding desktop application built with Java, JavaFX, Maven, and SQLite.

It is not a web app and does not use HTML, JavaScript, Spring Boot, or server APIs. The current UI is built directly with JavaFX classes in Java. A richer GUI polish pass can be added after the core project is complete.

## Features

- User registration and login with BCrypt password hashing
- Guest browsing for public problems and public leaderboard
- SQLite persistence with startup schema and seed initialization
- Seeded users, coding problems, and test cases
- Problem list, search/filter, details, and sample test cases
- Java code editor with local `javac`/`java` judge execution
- Per-test-case verdict output and saved submission history
- XP, rank, solved count, streak, and leaderboard progression
- Profile, squads, and local simulated 1v1 battles
- Admin panel for users, problems, test cases, submissions, and analytics

## Tech Stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| UI | JavaFX 21, written directly in Java |
| Database | SQLite |
| Build | Maven |
| Password Hashing | BCrypt |
| Judge | `ProcessBuilder` |

## Structure

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

## Run

```powershell
mvn javafx:run
```

## Database

By default, CodeArena uses the SQLite file in the project `data` folder:

```text
data/codearena.db
```

To use a different database file on another machine, launch with either:

```powershell
mvn javafx:run -Dcodearena.db.path="C:\path\to\codearena.db"
```

or set the `CODEARENA_DB_PATH` environment variable.

## Demo Logins

```text
Admin:
username: admin
password: admin123

Demo coders:
username: byteknight
username: loopwizard
username: stackrider
password for each: admin123
```

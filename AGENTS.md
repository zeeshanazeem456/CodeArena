# AGENTS.md - CodeArena Codex Instructions

CodeArena is a local Java desktop application. It is not a web app, mobile app, Spring Boot app, or REST API project.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI | JavaFX 21, built directly in Java |
| Build | Maven |
| Database | SQLite via `org.xerial:sqlite-jdbc` |
| Judge | `java.lang.ProcessBuilder` |
| Passwords | BCrypt |

## Source Layout

```text
src/main/java/com/codearena/
├── MainApp.java
├── model/      POJOs and enums
├── dao/        SQL and persistence access only
├── service/    Business logic
├── ui/         JavaFX screen builders
├── judge/      Local compile/run judge
└── util/       App utilities

src/main/resources/
├── data/       schema.sql and seed.sql
└── images/     app assets
```

## Architecture Rules

- UI screens are written directly in JavaFX Java code under `ui/`.
- Do not add separate layout files, visual layout-builder files, HTML, CSS, or JavaScript.
- Controllers are not used as a separate layout layer in this version.
- SQL belongs only in DAO or persistence initializer classes.
- Services own business logic and call DAOs.
- UI code delegates to services and must not contain SQL or BCrypt logic.
- Long-running work, especially judging, must run off the JavaFX application thread.
- Navigation goes through `NavigationUtil`, which uses `ScreenFactory`.

## Screen Rule

Every new screen must be added as JavaFX code in:

```text
src/main/java/com/codearena/ui/ScreenFactory.java
```

Use a screen key such as `dashboard`, `problem-list`, or `admin-panel`, then navigate with:

```java
NavigationUtil.navigateTo("dashboard", sourceNode);
```

## Judge Rule

The judge compiles and runs Java submissions locally using `ProcessBuilder`. Temporary files must be created under the OS temp directory and cleaned up after execution.

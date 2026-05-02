# REQUIREMENTS.md — CodeArena

> Version: 1.0 | Status: Active Development
> Platform: Java Desktop Application (JavaFX + SQLite)

---

## 1. Project Overview

CodeArena is a gamified competitive coding desktop application. Users solve algorithmic problems, compete in real-time 1v1 battles, progress through RPG-style ranks, and collaborate in squads. It targets students and developers who want competitive programming with a social and gaming twist — filling the gap between LeetCode (job prep) and Codeforces (elite competition).

**Positioning:** Social + Real-time + Gaming = CodeArena

---

## 2. User Roles

| Role | Description |
|---|---|
| **Guest** | Can register/login, browse the public problem catalog, view public problem details, and view the public leaderboard. Cannot solve, submit, compete, join squads, or access profile/admin screens. |
| **Coder** | Registered user. Can solve problems, compete in battles, join squads, and track progress. |
| **Admin** | Can manage problems, view all users, manage reports, seed new content. |
| **Judge System** | Internal automated actor. Compiles and evaluates code submissions. |

---

## 3. Functional Requirements

### 3.1 Authentication

- FR-AUTH-01: Guest can register a new account with username, email, and password.
- FR-AUTH-01A: Guest can browse published problems and view the public leaderboard without logging in.
- FR-AUTH-02: Passwords must be at least 8 characters and stored as BCrypt hashes.
- FR-AUTH-03: Registered user can log in with username + password.
- FR-AUTH-04: Session persists for the duration of the application run (no re-login required).
- FR-AUTH-05: User can log out, which clears the session and returns to the login screen.
- FR-AUTH-06: Admin account is seeded during DB initialization (not self-registerable).

### 3.2 Problem Management

- FR-PROB-01: Coder can browse a list of all available problems.
- FR-PROB-02: Problems can be filtered by difficulty (Easy, Medium, Hard) and category/tag.
- FR-PROB-03: Each problem shows: title, difficulty, acceptance rate, and solved/unsolved status.
- FR-PROB-04: Coder can open a problem to view the full description, constraints, and examples.
- FR-PROB-05: Admin can create a new problem with title, description, difficulty, tags, time limit, and memory limit.
- FR-PROB-06: Admin can add, edit, and delete test cases for any problem.
- FR-PROB-07: Admin can toggle a problem between draft and published state.

### 3.3 Code Editor & Submission

- FR-EDITOR-01: The code editor supports Java as the primary language.
- FR-EDITOR-02: The editor provides a text area with monospace font and basic syntax display.
- FR-EDITOR-03: User can click "Run" to test code against sample/visible test cases only.
- FR-EDITOR-04: User can click "Submit" to run code against all hidden test cases.
- FR-EDITOR-05: Submission results show verdict per test case: AC, WA, TLE, CE, or RE.
- FR-EDITOR-06: Overall submission verdict is the worst-case verdict across all test cases.
- FR-EDITOR-07: Submission history is saved and viewable per problem per user.

### 3.4 Judge System

- FR-JUDGE-01: Judge compiles user-submitted Java code using `javac` via ProcessBuilder.
- FR-JUDGE-02: Compilation errors are caught and returned as CE (Compilation Error) verdict.
- FR-JUDGE-03: Judge runs the compiled code against each test case using `java` via ProcessBuilder.
- FR-JUDGE-04: Standard input is piped to the process; standard output is captured for comparison.
- FR-JUDGE-05: If execution exceeds 5 seconds, the process is killed and verdict is TLE (Time Limit Exceeded).
- FR-JUDGE-06: If the process exits with a non-zero code, verdict is RE (Runtime Error).
- FR-JUDGE-07: If output matches expected output (trimmed), verdict is AC (Accepted).
- FR-JUDGE-08: If output does not match, verdict is WA (Wrong Answer).
- FR-JUDGE-09: Temp files are written to `/tmp/codearena/{submissionId}/` and deleted after execution.
- FR-JUDGE-10: Judge execution runs on a background thread — UI must not freeze during judging.

### 3.5 1v1 Battle System

- FR-BATTLE-01: Coder can challenge another registered user to a 1v1 battle.
- FR-BATTLE-02: Both coders are assigned the same randomly selected problem of agreed difficulty.
- FR-BATTLE-03: A countdown timer (default: 30 minutes) is displayed to both players.
- FR-BATTLE-04: The first player to achieve an AC verdict wins the battle.
- FR-BATTLE-05: If time expires with no AC, the battle is a draw or loss depending on partial progress.
- FR-BATTLE-06: Battle outcome (win/loss/draw) updates both players' stats and XP.
- FR-BATTLE-07: Battle history is saved and viewable in the user profile.

### 3.6 RPG Progression System

- FR-RPG-01: Each user has an XP (experience points) counter starting at 0.
- FR-RPG-02: XP is earned by: solving a problem (10–50 XP by difficulty), winning a battle (+75 XP), daily streak (+20 XP).
- FR-RPG-03: XP thresholds map to ranks: Novice → Apprentice → Warrior → Elite → Champion → Legend.
- FR-RPG-04: Rank is displayed on the user profile and leaderboard.
- FR-RPG-05: A progress bar shows XP progress toward the next rank.

### 3.7 Leaderboard

- FR-LEAD-01: Global leaderboard shows all users ranked by XP descending.
- FR-LEAD-02: Leaderboard displays: rank position, username, rank title, XP, problems solved, battles won.
- FR-LEAD-03: Leaderboard updates in real time when the screen is open.
- FR-LEAD-04: Current logged-in user's row is highlighted.

### 3.8 Squad System

- FR-SQUAD-01: Coder can create a squad with a name and optional description.
- FR-SQUAD-02: Coder can invite other users to join their squad.
- FR-SQUAD-03: Squad page shows all members, combined XP, and a squad leaderboard.
- FR-SQUAD-04: Squad creator is the squad leader and can remove members.
- FR-SQUAD-05: A user can only belong to one squad at a time.

### 3.9 User Profile

- FR-PROF-01: Profile page shows: username, rank, XP, problems solved, battles won/lost, join date.
- FR-PROF-02: Submission history is listed with problem name, verdict, language, and timestamp.
- FR-PROF-03: Battle history is listed with opponent, outcome, problem, and duration.
- FR-PROF-04: User can update their display name and password from the profile page.

### 3.10 Admin Panel

- FR-ADMIN-01: Admin can view all registered users with their stats.
- FR-ADMIN-02: Admin can deactivate (ban) a user account.
- FR-ADMIN-03: Admin can create, edit, and delete problems and their test cases.
- FR-ADMIN-04: Admin can view all submissions across all users.
- FR-ADMIN-05: Admin panel is only accessible to accounts with `role = ADMIN` in the DB.

---

## 4. Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR-01 | App must launch in under 3 seconds on standard student hardware. |
| NFR-02 | Judge must return a verdict within 10 seconds for a typical problem. |
| NFR-03 | UI must never freeze — all I/O and judge work runs on background threads. |
| NFR-04 | SQLite DB file is stored at `{user.home}/.codearena/codearena.db`. |
| NFR-05 | DB schema is auto-initialized on first launch if the file does not exist. |
| NFR-06 | App must run on Windows 10/11 and Linux without configuration changes. |
| NFR-07 | All SQL queries use PreparedStatement to prevent injection. |
| NFR-08 | Passwords are never stored in plaintext — BCrypt hashing is mandatory. |
| NFR-09 | Temp judge files are always cleaned up, even on exception. |
| NFR-10 | The app requires Java 17+ and JavaFX 21 to be installed on the host machine. |

---

## 5. Out of Scope

The following are explicitly NOT part of this version:

- Network multiplayer or server-client architecture
- Support for C++, Python, or languages other than Java (future phase)
- Cloud sync or remote database
- Web or mobile frontend
- Real-time chat or messaging system
- Payment or premium tier system

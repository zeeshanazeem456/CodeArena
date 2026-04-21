# UI Screens — CodeArena

> Owner: Sharjeel (Frontend Engineer)
> All screens are JavaFX FXML files in `src/main/resources/fxml/`

---

## Screen Inventory

| # | Screen Name | FXML File | Controller | Access |
|---|---|---|---|---|
| 1 | Login | `login.fxml` | `LoginController` | Guest |
| 2 | Register | `register.fxml` | `RegisterController` | Guest |
| 3 | Dashboard | `dashboard.fxml` | `DashboardController` | Coder |
| 4 | Problem List | `problem-list.fxml` | `ProblemListController` | Coder |
| 5 | Problem Detail | `problem-detail.fxml` | `ProblemDetailController` | Coder |
| 6 | Code Editor | `code-editor.fxml` | `CodeEditorController` | Coder |
| 7 | Leaderboard | `leaderboard.fxml` | `LeaderboardController` | Coder |
| 8 | Battle Lobby | `battle-lobby.fxml` | `BattleLobbyController` | Coder |
| 9 | Battle Arena | `battle-arena.fxml` | `BattleArenaController` | Coder |
| 10 | Profile | `profile.fxml` | `ProfileController` | Coder |
| 11 | Squad | `squad.fxml` | `SquadController` | Coder |
| 12 | Admin Panel | `admin-panel.fxml` | `AdminPanelController` | Admin |

---

## Screen Descriptions

### 1. Login Screen (`login.fxml`)
**Purpose:** Entry point for all users.
**Elements:**
- CodeArena logo + tagline
- Username field
- Password field
- Login button → triggers `AuthService.login()`
- Register link → navigate to Register screen
- Error label for invalid credentials

---

### 2. Register Screen (`register.fxml`)
**Purpose:** New user account creation.
**Elements:**
- Username field
- Email field
- Password field
- Confirm password field
- Register button → triggers `AuthService.register()`
- Back to Login link
- Validation error labels per field

---

### 3. Dashboard (`dashboard.fxml`)
**Purpose:** Home screen after login. Shows summary stats and navigation.
**Elements:**
- Welcome message with username and rank badge
- XP progress bar (current XP / next rank threshold)
- Stats cards: Problems Solved, Battles Won, Current Streak
- Navigation buttons: Problems, Battle, Leaderboard, Squad, Profile
- Recent activity list (last 5 submissions)
- Logout button

---

### 4. Problem List (`problem-list.fxml`)
**Purpose:** Browse and filter all published problems.
**Elements:**
- Search bar (filter by title)
- Difficulty filter: All / Easy / Medium / Hard (ToggleGroup)
- TableView columns: #, Title, Difficulty, Acceptance Rate, Status (✓ or -)
- Click row → navigate to Problem Detail
- Back button → Dashboard

---

### 5. Problem Detail (`problem-detail.fxml`)
**Purpose:** Read the full problem statement before coding.
**Elements:**
- Problem title and difficulty badge
- Problem description (ScrollPane with formatted text)
- Input/Output format section
- Sample test cases (shown as read-only input/output pairs)
- Constraints section
- "Start Coding" button → navigate to Code Editor (passes problem ID)
- Back button → Problem List

---

### 6. Code Editor (`code-editor.fxml`)
**Purpose:** Write, run, and submit code for a problem.
**Elements:**
- Problem title (read-only header)
- Split pane: left = problem description (collapsed), right = editor
- TextArea for code (monospace font)
- Language label: "Java" (fixed for now)
- Run button → judge against sample test cases only
- Submit button → judge against all test cases
- Results panel: verdict badge, test case breakdown (AC/WA/TLE per case)
- Status label: "Judging..." during execution
- Back button → Problem Detail

---

### 7. Leaderboard (`leaderboard.fxml`)
**Purpose:** Global ranking of all users by XP.
**Elements:**
- TableView columns: Position, Username, Rank Title, XP, Problems Solved, Battles Won
- Current user's row highlighted in accent color
- Refresh button
- Back button → Dashboard

---

### 8. Battle Lobby (`battle-lobby.fxml`)
**Purpose:** Find an opponent and start a 1v1 battle.
**Elements:**
- Username search field to find a specific user
- Difficulty selector for the battle problem
- Challenge button → sends battle request
- Incoming challenge notification area
- Accept / Decline buttons for incoming requests
- Back button → Dashboard

---

### 9. Battle Arena (`battle-arena.fxml`)
**Purpose:** Real-time 1v1 coding battle screen.
**Elements:**
- Split layout: left = problem, right = code editor
- Opponent username + status indicator
- Countdown timer (large, prominent)
- Your submission status vs opponent's status
- Run and Submit buttons (same as Code Editor)
- Winner banner overlay (appears when battle ends)
- Back to Dashboard button (only after battle concludes)

---

### 10. Profile (`profile.fxml`)
**Purpose:** View and edit user stats and history.
**Elements:**
- Avatar placeholder + username + rank badge
- XP progress bar
- Stats: Problems Solved, Battles Won, Battles Lost, Streak Days, Join Date
- Submission history tab: TableView (Problem, Verdict, Language, Date)
- Battle history tab: TableView (Opponent, Outcome, Problem, Duration)
- Edit Profile section: change display name, change password
- Save button → triggers UserDAO.updateProfile()
- Back button → Dashboard

---

### 11. Squad (`squad.fxml`)
**Purpose:** View, create, or manage a squad.
**Elements:**
- If user has no squad:
  - Create Squad form (name, description)
  - Join Squad search (by squad name)
- If user is in a squad:
  - Squad name + description
  - Leader name
  - Members TableView (Username, Rank, XP)
  - Combined squad XP
  - Leave Squad button (if not leader)
  - Remove Member buttons (if leader)
- Back button → Dashboard

---

### 12. Admin Panel (`admin-panel.fxml`)
**Purpose:** Administrative control panel (Admin role only).
**Elements:**
- Tab 1 — Users: TableView of all users, ban/unban toggle
- Tab 2 — Problems: TableView of all problems, Create/Edit/Delete buttons
- Tab 3 — Submissions: TableView of all submissions across users
- Problem Editor form (appears on create/edit): title, description, difficulty, tags, time limit, test cases manager
- Back button → Dashboard

---

## Color Palette

| Token | Hex | Usage |
|---|---|---|
| Background Dark | `#0D1117` | Main app background |
| Surface | `#161B22` | Cards, panels |
| Accent Orange | `#E25822` | Buttons, highlights, rank badges |
| Accent Blue | `#1F3A5F` | Secondary elements |
| Text Primary | `#E6EDF3` | Main body text |
| Text Muted | `#8B949E` | Labels, metadata |
| AC Green | `#3FB950` | Accepted verdict |
| WA Red | `#F85149` | Wrong answer verdict |
| TLE Yellow | `#D29922` | TLE verdict |
| CE Purple | `#BC8CFF` | Compile error verdict |

---

## Global CSS Variables (`global.css`)

```css
.root {
    -fx-background-color: #0D1117;
    -fx-text-fill: #E6EDF3;
    -fx-font-family: 'JetBrains Mono', 'Courier New', monospace;
    -fx-accent: #E25822;
}

.card {
    -fx-background-color: #161B22;
    -fx-background-radius: 8;
    -fx-padding: 16;
}

.btn-primary {
    -fx-background-color: #E25822;
    -fx-text-fill: white;
    -fx-background-radius: 6;
    -fx-font-weight: bold;
}

.verdict-ac  { -fx-text-fill: #3FB950; }
.verdict-wa  { -fx-text-fill: #F85149; }
.verdict-tle { -fx-text-fill: #D29922; }
.verdict-ce  { -fx-text-fill: #BC8CFF; }
```

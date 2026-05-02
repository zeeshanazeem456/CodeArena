# UI Screens - CodeArena

All screens are built with JavaFX directly in Java. There are no separate XML layout files in this project.

## Screen Inventory

| # | Screen | Navigation Key | JavaFX Builder | Access |
|---|---|---|---|---|
| 1 | Login | `login` | `ScreenFactory.login()` | Guest |
| 2 | Register | `register` | `ScreenFactory.register()` | Guest |
| 3 | Dashboard | `dashboard` | `ScreenFactory.dashboard()` | Coder |
| 4 | Problem List | `problem-list` | `ScreenFactory.problemList()` | Guest/Coder |
| 5 | Problem Detail | `problem-detail` | `ScreenFactory.problemDetail()` | Guest/Coder |
| 6 | Code Editor | `code-editor` | `ScreenFactory.codeEditor()` | Coder |
| 7 | Leaderboard | `leaderboard` | `ScreenFactory.leaderboard()` | Guest/Coder |
| 8 | Battle Lobby | `battle-lobby` | `ScreenFactory.battleLobby()` | Coder |
| 9 | Battle Arena | `battle-arena` | `ScreenFactory.battleArena()` | Coder |
| 10 | Profile | `profile` | `ScreenFactory.profile()` | Coder |
| 11 | Squad | `squad` | `ScreenFactory.squad()` | Coder |
| 12 | Admin Panel | `admin-panel` | `ScreenFactory.adminPanel()` | Admin |

## UI Rules

- Screens are JavaFX `Parent` trees created in Java.
- Navigation is handled by `NavigationUtil`.
- Screen state shared during navigation, such as selected problem or battle, is owned by `ScreenFactory`.
- UI code delegates business behavior to services.
- Database work remains in DAO classes.

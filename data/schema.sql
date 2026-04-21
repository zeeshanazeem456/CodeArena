-- CodeArena Database Schema
-- SQLite | Run via SchemaInitializer.java on first launch

PRAGMA foreign_keys = ON;

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    username        TEXT    NOT NULL UNIQUE,
    email           TEXT    NOT NULL UNIQUE,
    password        TEXT    NOT NULL,
    role            TEXT    NOT NULL DEFAULT 'CODER',
    xp              INTEGER NOT NULL DEFAULT 0,
    rank_title      TEXT    NOT NULL DEFAULT 'Novice',
    problems_solved INTEGER NOT NULL DEFAULT 0,
    battles_won     INTEGER NOT NULL DEFAULT 0,
    battles_lost    INTEGER NOT NULL DEFAULT 0,
    streak_days     INTEGER NOT NULL DEFAULT 0,
    last_active     TEXT,
    squad_id        INTEGER REFERENCES squads(id),
    created_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    is_active       INTEGER NOT NULL DEFAULT 1
);

-- PROBLEMS
CREATE TABLE IF NOT EXISTS problems (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    title        TEXT    NOT NULL,
    description  TEXT    NOT NULL,
    difficulty   TEXT    NOT NULL CHECK(difficulty IN ('Easy','Medium','Hard')),
    category     TEXT,
    tags         TEXT,
    time_limit   INTEGER NOT NULL DEFAULT 5,
    memory_limit INTEGER NOT NULL DEFAULT 256,
    is_published INTEGER NOT NULL DEFAULT 0,
    created_by   INTEGER REFERENCES users(id),
    created_at   TEXT    NOT NULL DEFAULT (datetime('now'))
);

-- TEST CASES
CREATE TABLE IF NOT EXISTS test_cases (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    problem_id INTEGER NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    input      TEXT    NOT NULL,
    expected   TEXT    NOT NULL,
    is_sample  INTEGER NOT NULL DEFAULT 0
);

-- SUBMISSIONS
CREATE TABLE IF NOT EXISTS submissions (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER NOT NULL REFERENCES users(id),
    problem_id   INTEGER NOT NULL REFERENCES problems(id),
    code         TEXT    NOT NULL,
    language     TEXT    NOT NULL DEFAULT 'Java',
    verdict      TEXT    NOT NULL,
    runtime_ms   INTEGER,
    battle_id    INTEGER REFERENCES battles(id),
    submitted_at TEXT    NOT NULL DEFAULT (datetime('now'))
);

-- BATTLES
CREATE TABLE IF NOT EXISTS battles (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    player1_id  INTEGER NOT NULL REFERENCES users(id),
    player2_id  INTEGER NOT NULL REFERENCES users(id),
    problem_id  INTEGER NOT NULL REFERENCES problems(id),
    winner_id   INTEGER REFERENCES users(id),
    status      TEXT    NOT NULL DEFAULT 'PENDING',
    started_at  TEXT,
    finished_at TEXT,
    time_limit  INTEGER NOT NULL DEFAULT 1800
);

-- SQUADS
CREATE TABLE IF NOT EXISTS squads (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    description TEXT,
    leader_id   INTEGER NOT NULL REFERENCES users(id),
    created_at  TEXT    NOT NULL DEFAULT (datetime('now'))
);

-- ADMIN SEED (default admin account)
INSERT OR IGNORE INTO users (username, email, password, role)
VALUES ('admin', 'admin@codearena.com', '$2a$10$placeholder_bcrypt_hash', 'ADMIN');
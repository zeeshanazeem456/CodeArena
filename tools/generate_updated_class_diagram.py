from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import math


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "diagrams"
OUT_DIR.mkdir(exist_ok=True)
OUT = OUT_DIR / "CodeArena_Updated_Class_Diagram.png"

W, H = 6400, 4300
BG = "white"
BOX = "#73C8EB"
HEADER = "#62BCE2"
PACKAGE_FILL = "#F7FBFE"
PACKAGE_BORDER = "#3A6E82"
LINE = "#222222"
DASH = "#444444"
TEXT = "#000000"


def font(size, bold=False):
    paths = [
        r"C:\Windows\Fonts\arialbd.ttf" if bold else r"C:\Windows\Fonts\arial.ttf",
        r"C:\Windows\Fonts\calibrib.ttf" if bold else r"C:\Windows\Fonts\calibri.ttf",
    ]
    for p in paths:
        if Path(p).exists():
            return ImageFont.truetype(p, size)
    return ImageFont.load_default()


F_TITLE = font(34, True)
F_PKG = font(24, True)
F_CLASS = font(20, True)
F_SMALL = font(17)
F_TINY = font(15)
F_LABEL = font(16)


class Box:
    def __init__(self, name, x, y, w, attrs=None, ops=None, stereotype=None, enum_values=None):
        self.name = name
        self.x = x
        self.y = y
        self.w = w
        self.attrs = attrs or []
        self.ops = ops or []
        self.stereotype = stereotype
        self.enum_values = enum_values or []
        rows = 2 + len(self.attrs) + len(self.ops) + len(self.enum_values)
        self.h = max(92, 42 + rows * 23)
        if stereotype:
            self.h += 18

    @property
    def left(self): return self.x
    @property
    def right(self): return self.x + self.w
    @property
    def top(self): return self.y
    @property
    def bottom(self): return self.y + self.h
    @property
    def center(self): return (self.x + self.w / 2, self.y + self.h / 2)

    def anchor(self, side):
        if side == "top":
            return (self.x + self.w / 2, self.y)
        if side == "bottom":
            return (self.x + self.w / 2, self.y + self.h)
        if side == "left":
            return (self.x, self.y + self.h / 2)
        if side == "right":
            return (self.x + self.w, self.y + self.h / 2)
        return self.center


def text_fit(draw, text, max_width, fnt):
    if draw.textlength(text, font=fnt) <= max_width:
        return text
    ell = "..."
    while text and draw.textlength(text + ell, font=fnt) > max_width:
        text = text[:-1]
    return text + ell


def draw_package(draw, name, x, y, w, h):
    draw.rounded_rectangle([x, y, x + w, y + h], radius=8, fill=PACKAGE_FILL, outline=PACKAGE_BORDER, width=3)
    tab_w, tab_h = 270, 44
    draw.rectangle([x, y - tab_h, x + tab_w, y], fill=BOX, outline=PACKAGE_BORDER, width=3)
    draw.text((x + 16, y - tab_h + 10), name, fill=TEXT, font=F_PKG)


def draw_box(draw, b: Box):
    x, y, w, h = b.x, b.y, b.w, b.h
    draw.rectangle([x, y, x + w, y + h], fill=BOX, outline=LINE, width=3)
    header_h = 34 if not b.stereotype else 56
    draw.rectangle([x, y, x + w, y + header_h], fill=HEADER, outline=LINE, width=2)
    cy = y + 5
    if b.stereotype:
        st = f"<<{b.stereotype}>>"
        tw = draw.textlength(st, font=F_TINY)
        draw.text((x + (w - tw) / 2, cy), st, fill=TEXT, font=F_TINY)
        cy += 18
    tw = draw.textlength(b.name, font=F_CLASS)
    draw.text((x + (w - tw) / 2, cy), b.name, fill=TEXT, font=F_CLASS)
    yy = y + header_h
    draw.line([x, yy, x + w, yy], fill=LINE, width=2)
    pad = 10
    if b.enum_values:
        for item in b.enum_values:
            draw.text((x + pad, yy + 6), item, fill=TEXT, font=F_SMALL)
            yy += 23
        return
    for attr in b.attrs:
        draw.text((x + pad, yy + 6), text_fit(draw, attr, w - 2 * pad, F_SMALL), fill=TEXT, font=F_SMALL)
        yy += 23
    draw.line([x, yy + 4, x + w, yy + 4], fill=LINE, width=2)
    yy += 8
    for op in b.ops:
        draw.text((x + pad, yy + 5), text_fit(draw, op, w - 2 * pad, F_SMALL), fill=TEXT, font=F_SMALL)
        yy += 23


def arrowhead(draw, p1, p2, fill=LINE, open_head=False, triangle=False):
    x1, y1 = p1
    x2, y2 = p2
    ang = math.atan2(y2 - y1, x2 - x1)
    size = 18
    a1 = ang + math.pi - 0.42
    a2 = ang + math.pi + 0.42
    p3 = (x2 + size * math.cos(a1), y2 + size * math.sin(a1))
    p4 = (x2 + size * math.cos(a2), y2 + size * math.sin(a2))
    if triangle:
        draw.polygon([p2, p3, p4], fill=BG, outline=fill)
        draw.line([p3, p4], fill=fill, width=2)
    elif open_head:
        draw.line([p2, p3], fill=fill, width=3)
        draw.line([p2, p4], fill=fill, width=3)
    else:
        draw.polygon([p2, p3, p4], fill=fill)


def draw_line(draw, boxes, a, aside, b, bside, label="", dashed=False, inheritance=False, color=LINE, mid_y=None):
    p1 = boxes[a].anchor(aside)
    p2 = boxes[b].anchor(bside)
    if mid_y is None:
        pts = [p1, p2]
    else:
        pts = [p1, (p1[0], mid_y), (p2[0], mid_y), p2]
    if dashed:
        for s, e in zip(pts, pts[1:]):
            draw_dashed(draw, s, e, color)
    else:
        draw.line(pts, fill=color, width=3, joint="curve")
    arrowhead(draw, pts[-2], pts[-1], fill=color, triangle=inheritance)
    if label:
        lx = sum(p[0] for p in pts) / len(pts)
        ly = sum(p[1] for p in pts) / len(pts)
        draw.text((lx + 8, ly - 22), label, fill=color, font=F_LABEL)


def draw_dashed(draw, p1, p2, fill):
    x1, y1 = p1
    x2, y2 = p2
    dx, dy = x2 - x1, y2 - y1
    dist = math.hypot(dx, dy)
    if dist == 0:
        return
    dash, gap = 18, 12
    n = int(dist // (dash + gap)) + 1
    ux, uy = dx / dist, dy / dist
    for i in range(n):
        start = i * (dash + gap)
        end = min(start + dash, dist)
        if start >= dist:
            break
        draw.line([(x1 + ux * start, y1 + uy * start), (x1 + ux * end, y1 + uy * end)], fill=fill, width=2)


def legend(draw):
    x, y = 4860, 80
    draw.rounded_rectangle([x, y, x + 1430, y + 210], radius=8, fill="#FFFFFF", outline=PACKAGE_BORDER, width=2)
    draw.text((x + 24, y + 20), "Legend", fill=TEXT, font=F_CLASS)
    draw.line([x + 28, y + 70, x + 210, y + 70], fill=LINE, width=3)
    arrowhead(draw, (x + 160, y + 70), (x + 210, y + 70))
    draw.text((x + 235, y + 58), "solid dependency / association", fill=TEXT, font=F_SMALL)
    draw_dashed(draw, (x + 28, y + 115), (x + 210, y + 115), DASH)
    arrowhead(draw, (x + 160, y + 115), (x + 210, y + 115), fill=DASH)
    draw.text((x + 235, y + 103), "dashed uses/imports/helper relationship", fill=TEXT, font=F_SMALL)
    draw.line([x + 28, y + 160, x + 210, y + 160], fill=LINE, width=3)
    arrowhead(draw, (x + 160, y + 160), (x + 210, y + 160), triangle=True)
    draw.text((x + 235, y + 148), "inheritance / extends", fill=TEXT, font=F_SMALL)


def main():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    title = "CodeArena - Updated Class Diagram"
    draw.text(((W - draw.textlength(title, font=F_TITLE)) / 2, 24), title, fill=TEXT, font=F_TITLE)
    draw.text((70, 78), "Generated from the completed JavaFX / SQLite project source code", fill="#333333", font=F_SMALL)
    legend(draw)

    # package frames
    draw_package(draw, "com.codearena.app / ui", 70, 210, 1980, 520)
    draw_package(draw, "com.codearena.service", 70, 900, 6260, 850)
    draw_package(draw, "com.codearena.dao", 70, 1980, 6260, 740)
    draw_package(draw, "com.codearena.model", 70, 2970, 3880, 1080)
    draw_package(draw, "com.codearena.judge", 4170, 2970, 2160, 650)
    draw_package(draw, "com.codearena.util", 4170, 3790, 2160, 420)

    boxes = {}
    def add(name, x, y, w, attrs=None, ops=None, stereotype=None, enum_values=None):
        boxes[name] = Box(name, x, y, w, attrs, ops, stereotype, enum_values)

    # app/ui
    add("MainApp", 120, 290, 430, ["- primaryStage : Stage"], ["+start(stage) : void", "+stop() : void", "+main(args) : void"])
    add("ScreenFactory", 750, 260, 760, ["- selectedProblem : Problem", "- selectedBattle : Battle", "- currentScreenName : String"], ["+create(screenName) : Parent", "-login() : Parent", "-dashboard() : Parent", "-codeEditor() : Parent", "-adminPanel() : Parent", "-applyTheme(node) : void"])
    add("JavaFX Application", 1650, 300, 330, [], ["+start(stage) : void"], "external")

    # services
    service_y1, service_y2 = 970, 1320
    add("AuthService", 120, service_y1, 500, ["- userDAO : UserDAO"], ["+login(username,password) : User", "+register(username,email,password) : boolean", "+logout() : void"])
    add("ProblemService", 720, service_y1, 570, ["- problemDAO : ProblemDAO", "- submissionDAO : SubmissionDAO", "- testCaseDAO : TestCaseDAO"], ["+getAllProblems() : List<Problem>", "+getFilteredProblems(...) : List<Problem>", "+isSolvedByUser(...) : boolean"])
    add("JudgeService", 1400, service_y1, 520, ["- judgeEngine : JudgeEngine"], ["+evaluateReportAsync(...) : void", "+evaluateAsync(...) : void"])
    add("UserProgressService", 2030, service_y1, 590, ["- userDAO : UserDAO", "- submissionDAO : SubmissionDAO", "- badgeService : BadgeService"], ["+awardProblemSolved(...) : void", "+awardBattleWin(winner, loser) : void"])
    add("BadgeService", 2740, service_y1, 540, ["- badgeDAO : BadgeDAO", "- submissionDAO : SubmissionDAO", "- battleDAO : BattleDAO", "- userDAO : UserDAO"], ["+getBadgesForUser(id) : List<Badge>", "+syncUserBadges(user) : void", "+checkSubmissionBadges(...) : void"])
    add("ProfileService", 3400, service_y1, 540, ["- userDAO : UserDAO", "- submissionDAO : SubmissionDAO", "- battleDAO : BattleDAO", "- badgeService : BadgeService"], ["+getUser(id) : User", "+getSubmissionHistory(id) : List", "+getBadges(id) : List<Badge>"])
    add("BattleService", 4050, service_y1, 620, ["- battleDAO : BattleDAO", "- userDAO : UserDAO", "- problemDAO : ProblemDAO", "- progressService : UserProgressService"], ["+createBattleRoom(...) : Battle", "+joinBattleRoom(...) : Battle", "+finishWithWinner(...) : boolean"])
    add("SquadService", 4780, service_y1, 500, ["- squadDAO : SquadDAO", "- userDAO : UserDAO"], ["+createSquad(...) : Squad", "+joinSquad(...) : void", "+getSquadLeaderboard() : List<Squad>"])
    add("AdminService", 5380, service_y1, 540, ["- userDAO : UserDAO", "- problemDAO : ProblemDAO", "- testCaseDAO : TestCaseDAO", "- submissionDAO : SubmissionDAO"], ["+getUsers() : List<User>", "+saveProblem(problem) : void", "+getSubmissions() : List"])
    add("AnalyticsService", 570, service_y2, 560, ["- userDAO : UserDAO", "- submissionDAO : SubmissionDAO", "- problemDAO : ProblemDAO"], ["+getTotalUsers() : int", "+getSubmissionVerdictBreakdown() : Map", "+getProblemSuccessRates() : List"])
    add("LeaderboardService", 1260, service_y2, 450, ["- userDAO : UserDAO"], ["+getRankedUsers() : List<User>"])
    add("JudgeReport", 1840, service_y2, 430, ["- verdict : Verdict", "- results : List<TestCaseResult>"], ["+getVerdict() : Verdict", "+getResults() : List"], "inner")

    # daos
    dao_y1 = 2060
    add("BaseDAO<T>", 110, dao_y1 + 130, 420, [], ["+getById(id) : T", "+save(entity) : void", "+delete(id) : void"], "abstract")
    add("UserDAO", 660, dao_y1, 500, [], ["+findByUsername(name) : User", "+register(user) : boolean", "+updateProgress(...) : void", "+getAllRanked() : List<User>"])
    add("ProblemDAO", 1280, dao_y1, 520, [], ["+getAll() : List<Problem>", "+filterByDifficulty(d) : List", "+getAcceptanceRate(id) : double", "+save(problem) : void"])
    add("TestCaseDAO", 1920, dao_y1, 480, [], ["+getByProblemId(id) : List<TestCase>", "+getSampleByProblemId(id) : List", "+save(testCase) : void"])
    add("SubmissionDAO", 2520, dao_y1, 560, [], ["+save(submission) : void", "+findByUserId(id) : List", "+hasAcceptedSubmission(...) : boolean", "+verdictBreakdown() : Map"])
    add("BattleDAO", 3210, dao_y1, 560, [], ["+findByUserId(id) : List<Battle>", "+findByJoinCode(code) : Battle", "+addParticipant(...) : void", "+save(battle) : void"])
    add("BadgeDAO", 3890, dao_y1, 500, [], ["+findAllWithUserStatus(id) : List<Badge>", "+awardBadge(userId, code) : void"])
    add("SquadDAO", 4510, dao_y1, 500, [], ["+getById(id) : Squad", "+findByName(name) : Squad", "+getMembers(id) : List<User>", "+save(squad) : void"])

    # models
    add("BaseEntity", 130, 3050, 350, ["- id : int", "- createdAt : String"], ["+getId() : int", "+setId(id) : void"], "abstract")
    add("User", 620, 3020, 470, ["- username : String", "- email : String", "- password : String", "- role : String", "- xp : int", "- squadId : Integer"], ["+isActive() : boolean", "+getRankTitle() : String"])
    add("Problem", 1220, 3020, 500, ["- title : String", "- description : String", "- difficulty : Difficulty", "- category : String", "- isPublished : boolean"], ["+getDifficulty() : Difficulty", "+setPublished(value) : void"])
    add("TestCase", 1860, 3020, 440, ["- problemId : int", "- input : String", "- expected : String", "- sample : boolean"], ["+isHidden() : boolean", "+getSequenceOrder() : int"])
    add("Submission", 2440, 3020, 500, ["- userId : int", "- problemId : int", "- code : String", "- language : String", "- verdict : Verdict"], ["+getRuntimeMs() : int", "+setBattleId(id) : void"])
    add("Battle", 3060, 3020, 500, ["- player1Id : int", "- player2Id : int", "- problemId : int", "- winnerId : Integer", "- status : String"], ["+getBattleMode() : String", "+getTimeLimit() : int"])
    add("Badge", 620, 3560, 460, ["- code : String", "- name : String", "- category : String", "- imagePath : String", "- earnedAt : String"], ["+isEarned() : boolean"])
    add("Squad", 1220, 3560, 450, ["- name : String", "- description : String", "- leaderId : int"], ["+getLeaderId() : int"])
    add("Difficulty", 1860, 3560, 400, [], [], "enumeration", ["EASY", "MEDIUM", "HARD", "+getLabel() : String"])

    # judge
    add("JudgeEngine", 4260, 3040, 560, ["- testCaseDAO : TestCaseDAO", "- submissionDAO : SubmissionDAO", "- lastResults : List"], ["+runSubmission(submission) : Verdict", "+getLastResults() : List"])
    add("TestCaseRunner", 4950, 3040, 520, [], ["+run(tc, dir, language) : TestCaseResult", "-normalizeForComparison(v) : String"], "final")
    add("TestCaseResult", 5600, 3040, 520, ["- verdict : Verdict", "- input : String", "- actualOutput : String", "- expectedOutput : String"], ["+getRuntimeMs() : long"], "inner")
    add("Sandbox", 4410, 3400, 450, ["- BASE_DIR : Path"], ["+createTempDir(id) : Path", "+writeSourceFile(...) : void", "+cleanup(id) : void"], "final")
    add("PythonRuntime", 5100, 3400, 440, ["- CANDIDATES : List"], ["+compileCommand(file) : String[]", "+runCommand(file) : String[]"], "package")
    add("Verdict", 5700, 3400, 420, [], [], "enumeration", ["AC", "WA", "CE", "RE", "TLE", "+priority() : int", "+isTerminal() : boolean"])

    # util
    add("DBConnection", 4240, 3860, 430, ["- DB_PATH : Path", "- connection : Connection"], ["+getConnection() : Connection", "+closeConnection() : void"], "final")
    add("PersistenceHandler", 4770, 3860, 430, [], ["+initialize() : void", "+shutdown() : void"], "final")
    add("SchemaInitializer", 5300, 3860, 430, [], ["+run() : void", "-ensureSchemaMigrations() : void"], "final")
    add("SeedInitializer", 5830, 3860, 430, [], ["+run() : void", "-ensureDemoContent() : void"], "final")
    add("SessionManager", 4240, 4070, 420, ["- currentUser : User"], ["+setCurrentUser(user) : void", "+getCurrentUser() : User", "+isLoggedIn() : boolean"], "final")
    add("NavigationUtil", 4770, 4070, 420, ["- flashMessage : String"], ["+navigateTo(screen,node) : void", "+setFlashMessage(msg) : void"], "final")
    add("XPCalculator", 5300, 4070, 420, [], ["+forSolvingProblem(d) : int", "+getRankTitle(xp) : String", "+nextRankThreshold(xp) : int"], "final")
    add("DAOException", 5830, 4070, 400, [], ["+DAOException(message)", "+DAOException(message,cause)"])
    add("AuthException", 5830, 3920, 400, [], ["+AuthException(message)", "+AuthException(message,cause)"])

    # Draw relationship lines behind boxes
    # inheritance
    for child in ["Badge", "Battle", "Problem", "Squad", "Submission"]:
        draw_line(draw, boxes, child, "left" if child in ["Badge", "Squad"] else "top", "BaseEntity", "right" if child in ["Badge", "Squad"] else "bottom", "extends", inheritance=True)
    for child in ["ProblemDAO", "SubmissionDAO", "BattleDAO", "SquadDAO"]:
        draw_line(draw, boxes, child, "left", "BaseDAO<T>", "right", "extends", inheritance=True)
    draw_line(draw, boxes, "MainApp", "right", "JavaFX Application", "left", "extends", inheritance=True)
    draw_line(draw, boxes, "DAOException", "top", "AuthException", "bottom", "RuntimeException", inheritance=True)

    # high-level app/util
    draw_line(draw, boxes, "MainApp", "right", "ScreenFactory", "left", "uses", dashed=True)
    draw_line(draw, boxes, "MainApp", "bottom", "PersistenceHandler", "top", "initializes", dashed=True, mid_y=820)
    draw_line(draw, boxes, "ScreenFactory", "bottom", "AuthService", "top", "uses", dashed=True)
    draw_line(draw, boxes, "ScreenFactory", "bottom", "ProblemService", "top", "uses", dashed=True)
    draw_line(draw, boxes, "ScreenFactory", "bottom", "JudgeService", "top", "uses", dashed=True)
    draw_line(draw, boxes, "ScreenFactory", "bottom", "BattleService", "top", "uses", dashed=True)
    draw_line(draw, boxes, "ScreenFactory", "bottom", "AdminService", "top", "uses", dashed=True)
    draw_line(draw, boxes, "ScreenFactory", "right", "SessionManager", "left", "session", dashed=True, mid_y=760)
    draw_line(draw, boxes, "ScreenFactory", "right", "NavigationUtil", "left", "navigate", dashed=True, mid_y=800)

    # services to daos
    mapping = [
        ("AuthService", "UserDAO"), ("ProblemService", "ProblemDAO"), ("ProblemService", "SubmissionDAO"), ("ProblemService", "TestCaseDAO"),
        ("JudgeService", "JudgeEngine"), ("UserProgressService", "UserDAO"), ("UserProgressService", "SubmissionDAO"), ("UserProgressService", "BadgeService"),
        ("BadgeService", "BadgeDAO"), ("BadgeService", "SubmissionDAO"), ("BadgeService", "BattleDAO"), ("ProfileService", "UserDAO"),
        ("ProfileService", "SubmissionDAO"), ("ProfileService", "BattleDAO"), ("BattleService", "BattleDAO"), ("BattleService", "ProblemDAO"),
        ("BattleService", "UserDAO"), ("SquadService", "SquadDAO"), ("SquadService", "UserDAO"), ("AdminService", "UserDAO"),
        ("AdminService", "ProblemDAO"), ("AdminService", "TestCaseDAO"), ("AdminService", "SubmissionDAO"), ("AnalyticsService", "SubmissionDAO"),
        ("AnalyticsService", "ProblemDAO"), ("LeaderboardService", "UserDAO")
    ]
    for s, d in mapping:
        draw_line(draw, boxes, s, "bottom", d, "top", "uses", dashed=True)

    # dao to db/exception
    for d in ["UserDAO", "ProblemDAO", "TestCaseDAO", "SubmissionDAO", "BattleDAO", "BadgeDAO", "SquadDAO"]:
        draw_line(draw, boxes, d, "bottom", "DBConnection", "top", "SQL", dashed=True, mid_y=2840)
        draw_line(draw, boxes, d, "bottom", "DAOException", "top", "throws", dashed=True, mid_y=2865)

    # dao to model
    dao_model = [("UserDAO", "User"), ("ProblemDAO", "Problem"), ("TestCaseDAO", "TestCase"), ("SubmissionDAO", "Submission"),
                 ("BattleDAO", "Battle"), ("BadgeDAO", "Badge"), ("SquadDAO", "Squad")]
    for d, m in dao_model:
        draw_line(draw, boxes, d, "bottom", m, "top", "maps", dashed=True)

    # model associations
    draw_line(draw, boxes, "Problem", "right", "TestCase", "left", "1 contains 1..*")
    draw_line(draw, boxes, "User", "right", "Submission", "left", "1 submits *")
    draw_line(draw, boxes, "Problem", "right", "Submission", "left", "1 receives *")
    draw_line(draw, boxes, "User", "right", "Battle", "left", "participates")
    draw_line(draw, boxes, "Squad", "top", "User", "bottom", "1 has *")
    draw_line(draw, boxes, "Problem", "bottom", "Difficulty", "top", "uses", dashed=True)
    draw_line(draw, boxes, "Submission", "right", "Verdict", "left", "uses", dashed=True)

    # judge relationships
    draw_line(draw, boxes, "JudgeService", "bottom", "JudgeReport", "top", "returns")
    draw_line(draw, boxes, "JudgeService", "bottom", "JudgeEngine", "top", "delegates", dashed=True)
    draw_line(draw, boxes, "JudgeEngine", "right", "TestCaseRunner", "left", "executes", dashed=True)
    draw_line(draw, boxes, "TestCaseRunner", "right", "TestCaseResult", "left", "creates")
    draw_line(draw, boxes, "JudgeEngine", "bottom", "Sandbox", "top", "writes/cleans", dashed=True)
    draw_line(draw, boxes, "JudgeEngine", "bottom", "PythonRuntime", "top", "Python", dashed=True)
    draw_line(draw, boxes, "TestCaseRunner", "bottom", "Verdict", "top", "returns", dashed=True)
    draw_line(draw, boxes, "JudgeEngine", "left", "TestCaseDAO", "right", "loads", dashed=True)
    draw_line(draw, boxes, "JudgeEngine", "left", "SubmissionDAO", "right", "saves", dashed=True)

    # persistence util
    draw_line(draw, boxes, "PersistenceHandler", "right", "SchemaInitializer", "left", "runs", dashed=True)
    draw_line(draw, boxes, "SchemaInitializer", "right", "SeedInitializer", "left", "then", dashed=True)
    draw_line(draw, boxes, "PersistenceHandler", "left", "DBConnection", "right", "opens/closes", dashed=True)
    draw_line(draw, boxes, "SessionManager", "top", "User", "right", "stores", dashed=True)
    draw_line(draw, boxes, "NavigationUtil", "top", "ScreenFactory", "right", "creates", dashed=True, mid_y=3720)
    draw_line(draw, boxes, "UserProgressService", "bottom", "XPCalculator", "top", "calculates", dashed=True, mid_y=1840)

    # draw boxes on top
    for b in boxes.values():
        draw_box(draw, b)

    # small note
    note = "All production Java types are represented. ScreenFactory owns many UI helper methods, summarized to keep the final class diagram readable."
    draw.text((80, 4180), note, fill="#333333", font=F_SMALL)

    img.save(OUT)
    print(OUT)


if __name__ == "__main__":
    main()

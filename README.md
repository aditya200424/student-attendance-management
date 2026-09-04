# Attendo — Attendance Tracker System

A full-stack attendance tracker built for academic evaluation. **No database is used anywhere** — every entity (students, teachers, admins, attendance records, subjects) is stored in `java.util.HashMap` in memory, backed by JSON files on disk purely for persistence across restarts.

## Tech stack

- **Backend:** Java 21, Spring Boot 3.3, Spring Security + JWT, Maven, Jackson, JSON file storage
- **Frontend:** HTML5, CSS3 (custom glassmorphism design system), vanilla JavaScript, Chart.js, Font Awesome

## Where the DSA lives

| Structure | Where | Purpose |
|---|---|---|
| `HashMap<String, Student/Teacher/Admin/Subject>` | `repository/*.java` | O(1) average lookup/insert/delete by ID |
| Secondary `HashMap` indices (email, roll number) | `StudentRepository`, `TeacherRepository`, `AdminRepository` | O(1) average lookup for login and search |
| `HashMap<String, ArrayList<Attendance>>` | `AttendanceRepository` | O(1) bucket fetch of one student's attendance history |
| `HashSet<String>` per student | `AttendanceRepository` | Unique present-dates tracking |
| `Deque` used as a **Stack** | `AttendanceRepository.undoStack` | O(1) undo-last-attendance-action |
| `Queue` (LinkedList) | `AttendanceRepository.pendingRequestsQueue` | FIFO attendance-correction requests |
| `PriorityQueue` (max-heap) | `AttendanceService.getLeaderboard()` | O(log n) insert, ranks students by attendance % |

Every class file has comments explaining the specific complexity of each operation.

## Running the backend

Requires **JDK 21** and **Maven 3.9+** (or use the included wrapper if you add one).

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. On first boot it loads `../data/*.json` into HashMaps; every write flushes back to those files, so your data survives restarts.

## Running the frontend

The frontend is static HTML/CSS/JS — no build step. Simplest option:

```bash
cd frontend
python3 -m http.server 5500
```

Then open `http://localhost:5500`. Make sure the backend is running on port 8080 first (see `frontend/js/api.js` → `API_BASE` if you need to change the URL).

## Demo credentials (seeded in /data)

| Role | ID | Password |
|---|---|---|
| Admin | `ADM001` | `admin123` |
| Teacher | `TCH001` | `teacher123` |
| Teacher | `TCH002` | `teacher123` |
| Student | `STU001` (roll `CSE21001`) | `student123` |

All other seeded students (`STU002`–`STU005`) use the same demo password `student123`.

## Project structure

```
attendance-tracker/
├── backend/
│   └── src/main/java/com/attendance/tracker/
│       ├── config/        # SecurityConfig (JWT filter chain, CORS)
│       ├── controller/    # REST controllers
│       ├── dto/           # Request/response payloads
│       ├── exception/     # Global exception handling
│       ├── model/         # Entities (Student, Teacher, Admin, Attendance, Subject)
│       ├── repository/    # HashMap-backed "repositories" (the DSA core)
│       ├── security/      # JwtUtil + JwtAuthFilter
│       ├── service/       # Business logic (search, percentages, leaderboard, reports)
│       └── util/          # PasswordUtil (SHA-256), JsonStorageUtil
├── frontend/
│   ├── index.html             # Landing page
│   ├── login.html             # Role-based login
│   ├── admin-dashboard.html   # Admin: students, teachers, subjects, reports, charts
│   ├── teacher-dashboard.html # Teacher: take attendance, history, student search
│   ├── student-dashboard.html # Student: own attendance %, subject breakdown, calendar
│   ├── css/style.css
│   └── js/{api.js, auth.js}
└── data/
    ├── students.json
    ├── teachers.json
    ├── admins.json
    ├── attendance.json
    └── subjects.json
```

## REST API summary

```
POST   /api/auth/login
POST   /api/auth/logout

GET    /api/students?search=&department=&semester=&section=
GET    /api/students/{id}
POST   /api/students
PUT    /api/students/{id}
DELETE /api/students/{id}

GET    /api/teachers
POST   /api/teachers
DELETE /api/teachers/{id}

GET    /api/subjects
POST   /api/subjects
DELETE /api/subjects/{id}

POST   /api/attendance/mark
PUT    /api/attendance/update?studentId=&subjectId=&date=&status=
DELETE /api/attendance/delete?studentId=&subjectId=&date=
POST   /api/attendance/undo
GET    /api/attendance/student/{id}
GET    /api/attendance/student/{id}/percentage
GET    /api/attendance/class?subjectId=&date=
GET    /api/attendance/leaderboard?topN=

GET    /api/reports
GET    /api/reports/monthly?days=
GET    /api/reports/department-wise
GET    /api/reports/student/{id}
```

## Notes for evaluation / extension

- Newly created students default to a password equal to their roll number (SHA-256 hashed); teachers default to `teacher@123`. Wire up a "change password" endpoint if you need self-service resets.
- PDF/Excel export, QR-code attendance, and email notifications are listed as bonus/optional in the brief and are not wired up in this build — the REST layer already returns clean JSON that a PDF/Excel export utility could consume directly.
- No `mvn`/`javac` toolchain was available in the environment this was generated in, so the backend has been reviewed line-by-line and brace-balance-checked but not machine-compiled. Run `mvn compile` first thing after downloading to catch anything.

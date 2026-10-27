# executable.md — Student ERP: Populate Assignments, Examinations, Timetable & Events

> **Status:** ✅ COMPLETED — backend changes + populate script implemented & compiled (2026-10-25)
> **Date:** 2026-10-25
> **Scope:** Seed realistic academic data for all 8 branches × 4 years (32 sections) — assignments scoped to exact concerned courses, examinations, Mon–Fri timetables, and 5 events.
> **Build:** ✅ `mvn -f backend/pom.xml -q -DskipTests package` — SUCCESS (only harmless Lombok `Unsafe::objectFieldOffset` warnings)

---

## 1. Current State (from live DB)

| Table | Rows | Problem |
|-------|------|---------|
| students | 558 | Only 5 enrolled in sections |
| sections | 2 | Only `CSE-A`/`CSE-B` (sem 2); most students have no section |
| student_sections | 5 | Near-total lack of enrollments |
| courses | 400 | `study_year` is NULL on **all** rows |
| notehub_subjects | 397 | Has authoritative `study_year` mapping |
| faculty | 212 | username = employeeNumber, password = employeeNumber |
| rooms/slots/assignments/exams | 0 | DataInitializer never ran |
| events | 1 | Stale "INFINITY X" |

**Curriculum reality:**
- AIML Y2/Y3 and ENTC/MECH/CHEM have **zero practical or tutorial courses** → must use `TUTORIAL` fallback.
- CHEM only has Y4 courses; FY (DESH) has only tutorials, no labs → tutorial-only FY timetable.

---

## 2. Backend Changes (5 files) — ✅ DONE

### 2.1 NEW `controller/SectionController.java` — `/api/sections`
- `GET` (authenticated) → `sectionRepository.findAll()`
- `GET /{id}`, `GET /program/{programId}`, `GET /program/{id}/semester/{sem}`
- `POST` (ADMIN) → create section with `program.id` / `academicYear.id`
- **Side effect:** fixes existing broken dropdowns in `assignment/index.html:274` and `examination/index.html:293` (currently 500).

### 2.2 `controller/CourseController.java` — `update()`
Add `course.setStudyYear(updated.getStudyYear());` so the year survives updates.

### 2.3 `controller/TimetableController.java` — new endpoint
```java
@PostMapping("/room")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Room> createRoom(@RequestBody Room room) {
    return ResponseEntity.ok(roomRepository.save(room));
}
```

### 2.4 `service/AssignmentService.java` + `controller/AssignmentController.java`
- `AssignmentService.findAll()` → `assignmentRepository.findAll()`
- `list()`: admin branch returns `findAll()` instead of throwing on missing student
- Refine `getCoursesForStudent()`: exact branch only (`"CSE AIML"` → `AIML`, **not** `CSE`); `studyYear == 1` → DESH courses; fallback year-wide then all

---

## 3. Populate Script — `scripts/populate_academics.py` — ✅ DONE (created, ~450 lines)

Style: match existing scripts (`urllib`, `ThreadPoolExecutor(8)`, `API_URL` env, admin login). Idempotent, resumable, `--branch/--year/--dry-run` filters.

### 3.1 Phases

| Phase | Action | Idempotent check |
|-------|--------|-----------------|
| P0 | Load reference data (departments, programs, academic years, courses, notehub subjects, faculty, students, sections, rooms) | — |
| P1 | Backfill `Course.studyYear` via PUT (from notehub map) | skip if year matches |
| P2 | Create rooms: `LH-101…LH-108` (LECTURE, 70 cap), `LAB-AI-01`, `LAB-CS-01`, `LAB-EC-01`, `LAB-ME-01` (LAB, 40 cap) | skip by name |
| P3 | Create 32 sections `{BRANCH}-{FY\|SY\|TY\|FINAL}-A`, sem `1/3/5/7`, cap 60 | skip by name |
| P4 | Enroll every student in matching section (program + studyYear) | skip if already enrolled |
| P5 | Build per-section course plan (exact concerned courses, with lab/tutorial/lecture quotas) | deterministic seed |
| P6 | Create FacultyCourse assignments (round-robin faculty by dept) | skip existing pairs |
| P7 | Generate timetable (Mon–Fri, slots 1–5, 1 lab/tutorial/day, ≤6h, no gaps) | skip if section has slots |
| P8 | Create 2 assignments per section-course (faculty login per assigned faculty) | skip by title |
| P9 | Create MID_TERM + END_TERM per section-course, weekdays, max 1/day/section | skip by name+course |
| P10 | Create 5 curated VIT events | skip by name |
| P11 | Verify + summary | — |

### 3.2 Timetable Algorithm

- Days: `MON`–`FRI` only
- Slots 1–5 → 08:00–09:00, 09:00–10:00, 10:00–11:00, 11:00–12:00, 12:00–13:00 (5h/day, zero gaps)
- Exactly **1** `PRACTICAL` (or `TUTORIAL` fallback) per day at rotating slot `[2,3,4,5,2]`
- Other 4 slots = `LECTURE`, no course twice in a day
- Room: lecture halls / lab pool assigned by section index (no cross-section conflict enforcement — not requested)

### 3.3 Course Plan Selection (exact concerned course rule)

1. Pool A = notehub subjects where `studyYear == S` AND course dept == section branch
2. Pool B = notehub subjects where `studyYear == S` (shared HS/BS courses — supplies labs/tutorials)
3. Pool C = branch dept courses (any year); DESH courses for FY
4. Select up to 9: 3 practical, 3 tutorial, 3 lecture; backfill from B/C if quotas unmet
5. Deterministic `random.seed(sectionId)`, printed with `--dry-run`

---

## 4. Build & Run — ✅ Backend built; script ready to run

```powershell
# 1. Build backend — ✅ DONE
cmd /c "C:\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin\mvn.cmd -f backend\pom.xml -q -DskipTests package"

# 2. Rebuild container (run next)
docker compose down; docker compose build backend; docker compose up -d

# 3. Wait for startup, then run script (run next)
python scripts\populate_academics.py --dry-run
python scripts\populate_academics.py --branch CSE --year SY --dry-run
python scripts\populate_academics.py
```

> **Implementation notes (what was actually written):**
> - `SectionController.java` (NEW): `GET /api/sections`, `GET /{id}`, `GET /program/{programId}`, `GET /program/{id}/semester/{sem}`, `POST` + `PUT` + `DELETE` (ADMIN) with `program.id` / `academicYear.id` resolution — fixes 500s in `assignment/index.html:274` & `examination/index.html:293`.
> - `CourseController.update()`: added `course.setStudyYear(updated.getStudyYear())`.
> - `TimetableController`: added `POST /api/timetable/room` (ADMIN).
> - `AssignmentService.findAll()` added; `AssignmentController.list()`: ADMIN → `findAll()`, otherwise student/faculty scope, final fallback `findAll()` (no throw); `getCoursesForStudent()`: exact-branch resolver (`"CSE AIML"` → `AIML`), FY → DESH courses first, then exact branch+year, fallback year-wide, then all.
> - `scripts/populate_academics.py`: P0 reference load; P1 studyYear backfill via `PUT /api/courses/{id}`; P2 rooms (8 LH + 4 LAB); P3 32 sections `{BRANCH}-{FY|SY|TY|FINAL}-A`; P4 enrollment via `POST /api/students/{id}/sections/{sectionId}`; P5 deterministic course plan (seed=`sectionId`, quotas 3P/3T/3L); P6 `POST /api/faculty/{id}/courses`; P7 25 slots/section via `POST /api/timetable/slot`; P8 2 assignments/section-course (faculty token, fallback admin); P9 MID+END exams (weekdays, 1/day/section); P10 5 VIT events; P11 counts verify.

---

## 5. Verification — ✅ ALL PASSED (2026-10-25, live DB)

| Check | Expected | Actual |
|-------|----------|--------|
| `GET /api/sections` | 34 (32 new + 2 legacy) | ✅ 34 |
| Enrollments (`student_sections`) | ≈ 558+ | ✅ 563 (558 + 5 legacy) |
| Slots (`time_slots`) | 800 (32 × 25) | ✅ 800, every new section = 25, 4 LECTURE + 1 PRACTICAL/TUTORIAL per day |
| Assignments | ≈ 190+ | ✅ 576 (288 section-courses × 2) |
| Exams | ≈ 380+ | ✅ 576 (MID_TERM + END_TERM per section-course) |
| Events | 6 (1 existing + 5 new) | ✅ 6 |
| Faculty assignments | 288 + 3 legacy | ✅ 291 |
| SY AIML student (`AIML2301`) | 25 slots, exact-branch courses | ✅ `/timetable/me` = 25; `/courses/for-student` = 22 all-AIML (21 yr-2 + 1 unmapped) |

**Extra fixes found & applied during execution:**
1. `StudentSection` / `FacultyCourse` LAZY → EAGER (nested objects serialized as `null`, broke skip-checks and `/students/{id}/sections`, `/faculty/{id}/courses` consumers).
2. `StudentService.enrollInSection()` + `FacultyService.assignCourse()` now idempotent (return existing instead of duplicating); deduped 558 + 288 double-created rows via MySQL (`ss` 1121→563, `fc` 579→291).
3. `AssignmentController.create()` (admin path): resolves assigned faculty via new `FacultyCourseRepository.findBySectionId()` when caller has no faculty identity; script P8 switched from JSON to multipart form-data (`@ModelAttribute` + dotted `course.id`/`section.id` keys) — recovered 18 missing assignments (558→576).

---

## 6. Risks

- ~1,500 REST calls → ~2–4 min runtime
- Room–slot conflicts across sections not enforced (32 sections × 25 slots exceeds room pool) — not requested
- FY and lab-scarce years use `TUTORIAL` label instead of `PRACTICAL`
- Existing `CSE-A/B` (sem 2) and 5 legacy enrollments left untouched (additive)

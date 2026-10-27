#!/usr/bin/env python3
"""
Populate Assignments, Examinations, Timetable & Events for Student ERP.
Scope: 8 branches x 4 years (32 sections) - assignments scoped to exact
concerned courses, examinations, Mon-Fri timetables, and 5 curated events.

Style: urllib + ThreadPoolExecutor(8), API_URL env, admin login.
Idempotent, resumable. Filters: --branch / --year / --dry-run.

Phases:
  P0 reference data | P1 backfill Course.studyYear | P2 rooms
  P3 sections | P4 enrollments | P5 course plan | P6 faculty assignments
  P7 timetable | P8 assignments | P9 exams | P10 events | P11 verify
"""

import sys
import os
import json
import random
import argparse
import urllib.request
import urllib.error
import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

API_BASE = os.environ.get("API_URL", "http://localhost:8080/api")

BRANCHES = ["CSE", "AIML", "IT", "AIDS", "ENTC", "MECH", "CHEM", "INST"]
YEAR_MAP = {
    "FY":    {"studyYear": 1, "semester": 1},
    "SY":    {"studyYear": 2, "semester": 3},
    "TY":    {"studyYear": 3, "semester": 5},
    "FINAL": {"studyYear": 4, "semester": 7},
}
PROGRAM_BY_BRANCH = {
    "CSE": "BTECH-CSE", "AIML": "BTECH-AIML", "IT": "BTECH-IT",
    "AIDS": "BTECH-AIDS", "ENTC": "BTECH-ENTC", "MECH": "BTECH-MECH",
    "CHEM": "BTECH-CHEM", "INST": "BTECH-INST",
}
DAYS = ["MON", "TUE", "WED", "THU", "FRI"]
SLOT_TIMES = {1: ("08:00:00", "09:00:00"), 2: ("09:00:00", "10:00:00"),
              3: ("10:00:00", "11:00:00"), 4: ("11:00:00", "12:00:00"),
              5: ("12:00:00", "13:00:00")}
LAB_SLOT_BY_DAY = {"MON": 2, "TUE": 3, "WED": 4, "THU": 5, "FRI": 2}

ROOMS = [
    {"name": n, "building": "Lecture Hall Complex", "capacity": 70, "type": "LECTURE"}
    for n in ["LH-101", "LH-102", "LH-103", "LH-104", "LH-105", "LH-106", "LH-107", "LH-108"]
] + [
    {"name": "LAB-AI-01", "building": "AI Block", "capacity": 40, "type": "LAB"},
    {"name": "LAB-CS-01", "building": "CS Block", "capacity": 40, "type": "LAB"},
    {"name": "LAB-EC-01", "building": "EC Block", "capacity": 40, "type": "LAB"},
    {"name": "LAB-ME-01", "building": "ME Block", "capacity": 40, "type": "LAB"},
]

EVENTS = [
    {"name": "VIT Hackathon 2026", "description": "24-hour inter-college hackathon on AI and sustainability.",
     "startDate": "2026-11-14", "endDate": "2026-11-15", "venue": "Innovation Centre", "active": True},
    {"name": "TechnoFest Project Expo", "description": "Final-year project exhibition across all 8 branches.",
     "startDate": "2026-12-05", "endDate": "2026-12-06", "venue": "Main Auditorium", "active": True},
    {"name": "Industry Guest Lecture: Cloud Native", "description": "Guest lecture by industry architects on cloud-native systems.",
     "startDate": "2026-11-22", "endDate": "2026-11-22", "venue": "Seminar Hall A", "active": True},
    {"name": "Inter-College Sports Meet", "description": "Annual sports meet: cricket, football, athletics.",
     "startDate": "2026-12-12", "endDate": "2026-12-14", "venue": "Sports Ground", "active": True},
    {"name": "Placement Orientation Drive", "description": "Orientation for SY/TY students on placements and internships.",
     "startDate": "2026-11-28", "endDate": "2026-11-28", "venue": "Placement Cell", "active": True},
]


def req(method, path, data=None, token=None, params=None, form=None, files=None):
    url = API_BASE + path
    if params:
        qs = "&".join(f"{k}={v}" for k, v in params.items() if v is not None)
        if qs:
            url += "?" + qs
    headers = {}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    body = None
    if form is not None:
        boundary = "----erp boundary"
        headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
        parts = []
        for k, v in form.items():
            if v is None:
                continue
            parts.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"{k}\"\r\n\r\n{v}\r\n")
        parts.append(f"--{boundary}--\r\n")
        body = "".join(parts).encode()
    elif data is not None:
        headers["Content-Type"] = "application/json"
        body = json.dumps(data).encode()
    r = urllib.request.Request(url, method=method, headers=headers, data=body)
    try:
        with urllib.request.urlopen(r, timeout=30) as resp:
            txt = resp.read().decode()
            return resp.status, json.loads(txt) if txt else None
    except urllib.error.HTTPError as e:
        txt = e.read().decode()
        try:
            return e.code, json.loads(txt)
        except Exception:
            return e.code, {"error": txt[:500]}
    except Exception as e:
        return 0, {"error": str(e)}


def login(username, password):
    s, r = req("POST", "/auth/login", data={"username": username, "password": password})
    if s == 200 and isinstance(r, dict) and "token" in r:
        return r["token"]
    return None


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--branch", default=None, help="Limit to branch, e.g. CSE")
    ap.add_argument("--year", default=None, help="Limit to year tag FY/SY/TY/FINAL")
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    branches = [b for b in BRANCHES if args.branch is None or b == args.branch.upper()]
    years = [y for y in YEAR_MAP if args.year is None or y == args.year.upper()]
    if not branches or not years:
        print("No matching branch/year filter.");
        sys.exit(1)

    admin = login("admin", "admin123")
    if not admin:
        print("[ERROR] admin login failed"); sys.exit(1)
    print("[OK] admin login")

    # ---- P0 reference data ----
    def get(path):
        s, r = req("GET", path, token=admin)
        return r if s == 200 else []
    departments = get("/departments")
    programs = get("/programs")
    acad_years = get("/academic-years")
    courses = get("/courses")
    notehub_subjects = get("/notehub/subjects")
    try:
        s, r = req("GET", "/notehub/subjects", token=admin)
        if s != 200:
            notehub_subjects = []
    except Exception:
        pass
    # notehub subjects endpoint may need params; fallback to empty
    faculties = get("/faculty")
    students = get("/students")
    sections = get("/sections")
    rooms = get("/timetable/rooms") if False else []
    # rooms endpoint is admin-only list; try
    s, r = req("GET", "/timetable/rooms", token=admin)
    rooms = r if s == 200 else []
    # academic year fallback
    ay_id = acad_years[0]["id"] if acad_years else None
    for ay in acad_years:
        if ay.get("current"):
            ay_id = ay["id"]
    print(f"P0: depts={len(departments)} programs={len(programs)} courses={len(courses)} "
          f"notehub={len(notehub_subjects)} faculty={len(faculties)} students={len(students)} "
          f"sections={len(sections)} rooms={len(rooms)} ay={ay_id}")

    prog_by_code = {p["code"]: p for p in programs}
    dept_by_code = {d["code"].upper(): d for d in departments}

    # ---- P1 backfill Course.studyYear from notehub map ----
    course_year = {}
    for nhs in notehub_subjects:
        c = nhs.get("course")
        if c and c.get("id") and nhs.get("studyYear"):
            course_year.setdefault(c["id"], []).append(nhs["studyYear"])
    backfill = {}
    for cid, yrs in course_year.items():
        backfill[cid] = max(set(yrs), key=yrs.count)
    print(f"P1: {len(backfill)} courses need year mapping")
    def backfill_course(c):
        cid = c["id"]
        want = backfill.get(cid)
        if want is None or c.get("studyYear") == want:
            return ("skip", cid)
        payload = dict(c)
        payload["studyYear"] = want
        if args.dry_run:
            return ("would-update", cid)
        s, r = req("PUT", f"/courses/{cid}", data=payload, token=admin)
        return ("ok" if s == 200 else "fail", cid)
    if not args.dry_run:
        with ThreadPoolExecutor(max_workers=8) as ex:
            futs = [ex.submit(backfill_course, c) for c in courses if c["id"] in backfill]
            done = sum(1 for f in as_completed(futs) if f.result()[0] in ("ok", "skip"))
        print(f"P1: backfilled {done}/{len(backfill)}")
        s, r = req("GET", "/courses", token=admin)
        courses = r if s == 200 else courses
    else:
        for c in courses:
            if c["id"] in backfill and c.get("studyYear") != backfill[c["id"]]:
                print(f"  [dry-run] course {c.get('code')} -> year {backfill[c['id']]}")

    by_id_course = {c["id"]: c for c in courses}

    # ---- P2 rooms ----
    existing_rooms = {r.get("name") for r in rooms}
    for rm in ROOMS:
        if rm["name"] in existing_rooms:
            continue
        if args.dry_run:
            print(f"  [dry-run] create room {rm['name']}")
            continue
        s, r = req("POST", "/timetable/room", data=rm, token=admin)
        if s not in (200, 201):
            print(f"  [WARN] room {rm['name']}: {s} {r}")
    s, r = req("GET", "/timetable/rooms", token=admin)
    rooms = r if s == 200 else rooms
    print(f"P2: rooms={len(rooms)}")

    # ---- P3 sections (32) ----
    existing_sec = {sec.get("name") for sec in sections}
    sec_to_create = []
    for b in branches:
        for y in years:
            name = f"{b}-{y}-A"
            if name not in existing_sec:
                sec_to_create.append((b, y, name))
    prog_cache = {}
    for b, y, name in sec_to_create:
        pcode = PROGRAM_BY_BRANCH[b]
        prog = prog_by_code.get(pcode)
        if not prog:
            print(f"  [WARN] no program {pcode} for section {name}");
            continue
        payload = {"name": name, "semester": YEAR_MAP[y]["semester"],
                   "capacity": 60, "program": {"id": prog["id"]},
                   "academicYear": {"id": ay_id} if ay_id else None}
        if args.dry_run:
            print(f"  [dry-run] create section {name} sem={payload['semester']} prog={pcode}")
            continue
        s, r = req("POST", "/sections", data=payload, token=admin)
        if s not in (200, 201):
            print(f"  [WARN] section {name}: {s} {r}")
    s, r = req("GET", "/sections", token=admin)
    sections = r if s == 200 else sections
    sec_by_name = {sec["name"]: sec for sec in sections}
    print(f"P3: sections={len(sections)} (target 32 new + 2 legacy)")

    # ---- P4 enroll every student in matching section ----
    def section_for_student(stu):
        sy = stu.get("studyYear")
        tag = {1: "FY", 2: "SY", 3: "TY", 4: "FINAL"}.get(sy)
        if not tag or (args.year and tag != args.year.upper()):
            return None
        branch = guess_branch(stu, programs)
        if not branch or (args.branch and branch != args.branch.upper()):
            return None
        return sec_by_name.get(f"{branch}-{tag}-A")

    def guess_branch(stu, programs):
        cs = (stu.get("course") or "").upper().strip()
        if "AIML" in cs:
            return "AIML"
        if "AIDS" in cs:
            return "AIDS"
        # exact tokens before substring CSE (avoids "CSE AIML" -> CSE)
        for b in ["ENTC", "MECH", "CHEM", "INST", "CSE", "IT"]:
            if cs == b or cs.endswith(" " + b) or cs.startswith(b + " "):
                return b
        if "CSE" in cs:
            return "AIML" if "AIML" in cs else "CSE"
        prog = stu.get("program") or {}
        pcode = (prog.get("code") or "").upper()
        for b in BRANCHES:
            if b in pcode:
                return "AIML" if b == "AIML" or "AIML" in pcode else b
        return None

    enroll_jobs = []
    for stu in students:
        sec = section_for_student(stu)
        if sec:
            enroll_jobs.append((stu, sec))
    print(f"P4: {len(enroll_jobs)} enrollments to ensure")
    if not args.dry_run:
        def enroll(job):
            stu, sec = job
            s, r = req("GET", f"/students/{stu['id']}/sections", token=admin)
            if s == 200 and any((e.get("section") or {}).get("id") == sec["id"] for e in (r or [])):
                return "skip"
            s, r = req("POST", f"/students/{stu['id']}/sections/{sec['id']}", token=admin)
            return "ok" if s == 200 else f"fail:{s}"
        with ThreadPoolExecutor(max_workers=8) as ex:
            res = list(ex.map(enroll, enroll_jobs))
        print(f"P4: ok={res.count('ok')} skip={res.count('skip')} fail={len(res)-res.count('ok')-res.count('skip')}")
    else:
        print(f"  [dry-run] would enroll {len(enroll_jobs)} students")

    # ---- P5 course plan per section (deterministic) ----
    def dept_of(course):
        d = course.get("department") or {}
        return (d.get("code") or "").upper()

    def is_practical(c):
        return (c.get("practicalHours") or 0) > 0
    def is_tutorial(c):
        return (c.get("tutorialHours") or 0) > 0

    plan_by_section = {}
    for sec in sections:
        if sec["name"] not in sec_by_name:
            continue
        # only manage our 32 (skip legacy CSE-A/B unless matched)
        parts = sec["name"].split("-")
        if len(parts) != 3:
            continue
        b, y = parts[0], parts[1]
        if b not in branches or y not in years:
            continue
        sy = YEAR_MAP[y]["studyYear"]
        rnd = random.Random(sec["id"])
        poolA = [c for c in courses if c.get("studyYear") == sy and dept_of(c) == b]
        poolB = [c for c in courses if c.get("studyYear") == sy]
        poolC = [c for c in courses if dept_of(c) == (b if sy > 1 else "DESH")]
        if sy == 1 and not poolC:
            poolC = [c for c in courses if dept_of(c) == "DESH"]
        prac = [c for c in poolA if is_practical(c)] or [c for c in poolB if is_practical(c)] or []
        tut = [c for c in poolA if is_tutorial(c) and c not in prac] or \
              [c for c in poolB if is_tutorial(c) and c not in prac] or []
        lec = [c for c in poolA if c not in prac and c not in tut] or \
              [c for c in poolB if c not in prac and c not in tut] or poolC
        # dedupe preserve order
        def dedup(lst):
            seen, out = set(), []
            for c in lst:
                if c["id"] not in seen:
                    seen.add(c["id"]); out.append(c)
            return out
        prac, tut, lec = dedup(prac), dedup(tut), dedup(lec)
        rnd.shuffle(prac); rnd.shuffle(tut); rnd.shuffle(lec)
        need_p, need_t, need_l = 3, 3, 3
        sel_p, sel_t, sel_l = prac[:need_p], [c for c in tut if c not in prac][:need_t], \
                             [c for c in lec if c not in prac and c not in tut][:need_l]
        # backfill quotas from B/C
        backup = dedup(poolB + poolC)
        for c in backup:
            if len(sel_p) + len(sel_t) + len(sel_l) >= 9:
                break
            if c["id"] in {x["id"] for x in sel_p + sel_t + sel_l}:
                continue
            if len(sel_p) < need_p and is_practical(c):
                sel_p.append(c)
            elif len(sel_t) < need_t:
                sel_t.append(c)
            elif len(sel_l) < need_l:
                sel_l.append(c)
        for c in backup:
            if len(sel_p) + len(sel_t) + len(sel_l) >= 9:
                break
            if c["id"] not in {x["id"] for x in sel_p + sel_t + sel_l}:
                (sel_p if len(sel_p) < need_p else sel_t if len(sel_t) < need_t else sel_l).append(c)
        plan_by_section[sec["id"]] = {"section": sec, "practical": sel_p, "tutorial": sel_t, "lecture": sel_l}
    if args.dry_run:
        for sid, p in plan_by_section.items():
            sec = p["section"]
            print(f"  [dry-run] {sec['name']}: P={[c.get('code') for c in p['practical']]} "
                  f"T={[c.get('code') for c in p['tutorial']]} L={[c.get('code') for c in p['lecture']]}")

    # ---- P6 faculty assignments (round-robin by dept) ----
    fac_by_dept = {}
    for f in faculties:
        d = ((f.get("department") or {}).get("code") or "").upper()
        fac_by_dept.setdefault(d, []).append(f)
    all_fac = faculties or []
    def pick_faculty(dept_code, idx):
        pool = fac_by_dept.get(dept_code) or all_fac
        return pool[idx % len(pool)] if pool else None
    if not args.dry_run:
        s, r = req("GET", "/faculty", token=admin)
        fc_existing = set()
        # fetch existing assignments per faculty to build skip set
        def load_fc(f):
            s2, r2 = req("GET", f"/faculty/{f['id']}/courses", token=admin)
            out = []
            if s2 == 200:
                for fc in r2 or []:
                    c = (fc.get("course") or {}).get("id")
                    sec = (fc.get("section") or {}).get("id")
                    out.append((c, sec))
            return out
        with ThreadPoolExecutor(max_workers=8) as ex:
            for fcs in ex.map(load_fc, faculties):
                fc_existing.update(fcs)
        jobs = []
        for sid, p in plan_by_section.items():
            sec = p["section"]
            idx = 0
            for c in p["practical"] + p["tutorial"] + p["lecture"]:
                if (c["id"], sid) in fc_existing:
                    idx += 1
                    continue
                f = pick_faculty(dept_of(by_id_course.get(c["id"], c)), idx)
                if f:
                    jobs.append((f, c, sec))
                idx += 1
        def assign(job):
            f, c, sec = job
            s, r = req("POST", f"/faculty/{f['id']}/courses", token=admin,
                       params={"courseId": c["id"], "sectionId": sec["id"], "academicYearId": ay_id})
            return s in (200, 201)
        with ThreadPoolExecutor(max_workers=8) as ex:
            results = list(ex.map(assign, jobs))
        print(f"P6: assigned {sum(results)}/{len(jobs)} (skipped {len(plan_by_section)*9-len(jobs)} existing)")

    # ---- P7 timetable Mon-Fri slots 1-5 ----
    faculty_of = {}  # (courseId, sectionId) -> facultyId
    # reload mapping via faculty/courses endpoints

    def load_fc_single(f):
        s2, r2 = req("GET", f"/faculty/{f['id']}/courses", token=admin)
        return f, (r2 if s2 == 200 else [])
    if not args.dry_run:
        with ThreadPoolExecutor(max_workers=8) as ex:
            for fut in as_completed([ex.submit(load_fc_single, f) for f in faculties]):
                f, r2 = fut.result()
                for fc in r2 or []:
                    c = (fc.get("course") or {}).get("id")
                    sec = (fc.get("section") or {}).get("id")
                    if c and sec:
                        faculty_of.setdefault((c, sec), f["id"])
    lecture_rooms = [r for r in rooms if (r.get("type") or "").upper() != "LAB"] or rooms
    lab_rooms = [r for r in rooms if (r.get("type") or "").upper() == "LAB"] or rooms
    def build_slots(sid, p):
        sec = p["section"]
        all_c = p["practical"] + p["tutorial"] + p["lecture"]
        if not all_c:
            return []
        lec_pool = (p["lecture"] or all_c)
        lab_pool = (p["practical"] or p["tutorial"] or all_c)
        has_practical = bool(p["practical"])
        slots = []
        li = 0
        for di, day in enumerate(DAYS):
            lab_slot = LAB_SLOT_BY_DAY[day]
            used = set()
            # choose lab/tutorial course deterministically
            lab_course = lab_pool[(sec["id"] + di) % len(lab_pool)]
            lab_type = "PRACTICAL" if (has_practical and lab_course in p["practical"]) else "TUTORIAL"
            for slot in (1, 2, 3, 4, 5):
                if slot == lab_slot:
                    course, typ = lab_course, lab_type
                else:
                    # next lecture course not used today
                    course = None
                    for _ in range(len(lec_pool)):
                        cand = lec_pool[(li) % len(lec_pool)]
                        li += 1
                        if cand["id"] not in used:
                            course = cand
                            break
                    if course is None:
                        course = lec_pool[(sec["id"] + slot) % len(lec_pool)]
                    typ = "LECTURE"
                used.add(course["id"])
                start, end = SLOT_TIMES[slot]
                room = (lab_rooms if typ in ("PRACTICAL", "TUTORIAL") else lecture_rooms)
                room = room[(sec["id"] + slot) % len(room)] if room else None
                fid = faculty_of.get((course["id"], sid))
                slots.append({"day": day, "slot": slot, "start": start, "end": end,
                              "course": course, "type": typ, "room": room, "fid": fid})
        return slots
    if args.dry_run:
        print(f"P7: [dry-run] would generate 25 slots/section x {len(plan_by_section)} sections")
    else:
        s, r = req("GET", "/exams", token=admin)  # warm
        total = 0
        def ensure_section_timetable(item):
            sid, p = item
            s2, r2 = req("GET", f"/timetable/section/{sid}", token=admin)
            if s2 == 200 and len(r2 or []) >= 25:
                return 0
            created = 0
            for sl in build_slots(sid, p):
                if not sl["room"] or not sl["fid"]:
                    continue
                payload = {"dayOfWeek": sl["day"], "startTime": sl["start"], "endTime": sl["end"],
                           "slotNumber": sl["slot"], "course": {"id": sl["course"]["id"]},
                           "faculty": {"id": sl["fid"]}, "room": {"id": sl["room"]["id"]},
                           "section": {"id": sid}, "type": sl["type"]}
                s3, _ = req("POST", "/timetable/slot", data=payload, token=admin)
                if s3 in (200, 201):
                    created += 1
            return created
        with ThreadPoolExecutor(max_workers=8) as ex:
            for c in ex.map(ensure_section_timetable, list(plan_by_section.items())):
                total += c
        print(f"P7: created {total} slots")

    # ---- P8 assignments: 2 per section-course (faculty login) ----
    fac_tokens = {}
    def faculty_token(f):
        if f["id"] in fac_tokens:
            return fac_tokens[f["id"]]
        uname = (f.get("user") or {}).get("username") or f.get("employeeNumber")
        for pw in [f.get("employeeNumber"), "faculty123"]:
            if not pw:
                continue
            t = login(uname, pw)
            if t:
                fac_tokens[f["id"]] = t
                return t
        return None
    fac_by_id = {f["id"]: f for f in faculties}
    jobs8 = []
    for sid, p in plan_by_section.items():
        sec = p["section"]
        for c in p["practical"] + p["tutorial"] + p["lecture"]:
            fid = faculty_of.get((c["id"], sid))
            jobs8.append((sec, c, fid))
    if args.dry_run:
        print(f"P8: [dry-run] would create {2*len(jobs8)} assignments ({len(jobs8)} section-courses x 2)")
    else:
        s, r = req("GET", "/assignments", token=admin)
        existing_titles = {a.get("title") for a in (r or [])} if s == 200 else set()
        def create_two(job):
            sec, c, fid = job
            out = 0
            f = fac_by_id.get(fid) if fid else None
            tok = faculty_token(f) if f else admin
            if not tok:
                tok = admin
            for n in (1, 2):
                title = f"{c.get('code')} - Assignment {n} ({sec['name']})"
                if title in existing_titles:
                    continue
                form = {"title": title,
                        "description": f"Problem set {n} for {c.get('name')} ({sec['name']})",
                        "course.id": c["id"], "section.id": sec["id"],
                        "dueDate": (datetime.date.today() + datetime.timedelta(days=14 + n * 7)).isoformat(),
                        "maxMarks": 20, "status": "PUBLISHED",
                        "assignmentType": "THEORY"}
                # Controller uses @ModelAttribute -> must send multipart form-data
                # with dotted nested keys (course.id / section.id).
                s2, _ = req("POST", "/assignments", form=form, token=tok)
                if s2 in (200, 201):
                    out += 1
            return out
        # Note: controller uses @ModelAttribute, so form-data (not JSON) is required.
        with ThreadPoolExecutor(max_workers=8) as ex:
            total8 = sum(ex.map(create_two, jobs8))
        print(f"P8: created {total8} assignments")

    # ---- P9 exams: MID_TERM + END_TERM per section-course, max 1/day/section ----
    if args.dry_run:
        print(f"P9: [dry-run] would create {2*len(jobs8)} exams")
    else:
        s, r = req("GET", "/exams", token=admin)
        existing_ex = {(e.get("name"), (e.get("course") or {}).get("id")) for e in (r or [])} if s == 200 else set()
        base = datetime.date.today() + datetime.timedelta(days=21)
        # per-section weekday cursor
        cursors = {}
        def next_weekday(d):
            while d.weekday() >= 5:
                d += datetime.timedelta(days=1)
            return d
        total9 = 0
        for (sec, c, fid) in sorted(jobs8, key=lambda j: (j[0]["id"], j[1].get("code") or "")):
            sid = sec["id"]
            cur = cursors.get(sid, base)
            for exam_type in ("MID_TERM", "END_TERM"):
                name = f"{c.get('code')} {exam_type} ({sec['name']})"
                if (name, c["id"]) in existing_ex:
                    continue
                cur = next_weekday(cur)
                payload = {"name": name, "examType": exam_type, "course": {"id": c["id"]},
                           "section": {"id": sid}, "academicYear": {"id": ay_id} if ay_id else None,
                           "examDate": cur.isoformat(), "startTime": "10:00:00",
                           "endTime": "13:00:00", "venue": "Exam Hall",
                           "maxMarks": 70 if exam_type == "END_TERM" else 30, "status": "SCHEDULED"}
                s2, _ = req("POST", "/exams", data=payload, token=admin)
                if s2 in (200, 201):
                    total9 += 1
                cur += datetime.timedelta(days=1)
            cursors[sid] = cur
        print(f"P9: created {total9} exams")

    # ---- P10 events ----
    s, r = req("GET", "/events", token=admin)
    existing_ev = {e.get("name") for e in (r or [])} if s == 200 else set()
    for ev in EVENTS:
        if ev["name"] in existing_ev:
            continue
        if args.dry_run:
            print(f"  [dry-run] create event {ev['name']}")
            continue
        s, _ = req("POST", "/events", data=ev, token=admin)
        if s not in (200, 201):
            print(f"  [WARN] event {ev['name']}: {s}")
    print("P10: events ensured")

    # ---- P11 verify ----
    if not args.dry_run:
        counts = {}
        for path, key in [("/sections", "sections"), ("/assignments", "assignments"),
                          ("/exams", "exams"), ("/events", "events")]:
            s, r = req("GET", path, token=admin)
            counts[key] = len(r) if s == 200 else -1
        s, r = req("GET", "/students", token=admin)
        counts["students"] = len(r) if s == 200 else -1
        print(f"P11 verify: {counts}")
        print("Done.")


if __name__ == "__main__":
    main()

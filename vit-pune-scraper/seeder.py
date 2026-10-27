"""
VIT Pune ERP Database Seeder
Authenticates with the Student ERP backend and synchronizes departments, programs, courses, and NoteHub subjects.
Supports both insertion of new curriculum records and updating existing records with clean names and associations.
"""

import sys
import json
import logging
from pathlib import Path
from typing import Dict, Any, List, Optional

import requests

from config import (
    ERP_CONFIG,
    DEPARTMENTS,
    PROGRAMS,
    OUTPUT_JSON_PATH
)

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("erp_seeder")


class ERPSeeder:
    def __init__(self, base_url: Optional[str] = None, username: Optional[str] = None, password: Optional[str] = None):
        self.base_url = (base_url or ERP_CONFIG["base_url"]).rstrip("/")
        self.username = username or ERP_CONFIG["username"]
        self.password = password or ERP_CONFIG["password"]
        self.token = None
        self.session = requests.Session()

    def authenticate(self) -> bool:
        """Authenticate as ADMIN to acquire JWT bearer token."""
        login_url = f"{self.base_url}{ERP_CONFIG['auth_endpoint']}"
        logger.info(f"Authenticating with ERP at {login_url} as '{self.username}'...")
        try:
            resp = self.session.post(
                login_url,
                json={"username": self.username, "password": self.password},
                headers={"Content-Type": "application/json"},
                timeout=15
            )
            if resp.status_code == 200:
                data = resp.json()
                self.token = data.get("token")
                self.session.headers.update({
                    "Authorization": f"Bearer {self.token}",
                    "Content-Type": "application/json"
                })
                logger.info(f"Authentication successful! Role: {data.get('role')}")
                return True
            else:
                logger.error(f"Authentication failed: HTTP {resp.status_code} - {resp.text}")
                return False
        except Exception as e:
            logger.error(f"Cannot connect to ERP backend at {self.base_url}: {e}")
            return False

    def sync_departments(self) -> Dict[str, Dict[str, Any]]:
        """Ensure all required academic departments exist in ERP."""
        dept_url = f"{self.base_url}{ERP_CONFIG['departments_endpoint']}"
        logger.info("Fetching existing departments from ERP...")
        existing_resp = self.session.get(dept_url, timeout=15)
        existing_depts = {}
        if existing_resp.status_code == 200:
            for d in existing_resp.json():
                existing_depts[d["code"]] = d

        dept_map = {}
        created_count = 0
        for code, info in DEPARTMENTS.items():
            if code in existing_depts:
                dept_map[code] = existing_depts[code]
            else:
                payload = {
                    "code": info["code"],
                    "name": info["name"],
                    "description": info["description"],
                    "active": info["active"]
                }
                res = self.session.post(dept_url, json=payload, timeout=15)
                if res.status_code in [200, 201]:
                    dept_obj = res.json()
                    dept_map[code] = dept_obj
                    created_count += 1
                    logger.info(f"Created Department: {code} - {info['name']}")
                else:
                    logger.warning(f"Failed to create department {code}: {res.status_code} - {res.text}")

        logger.info(f"Departments synced: {len(dept_map)} active ({created_count} newly created).")
        return dept_map

    def sync_programs(self, dept_map: Dict[str, Dict[str, Any]]) -> Dict[str, Dict[str, Any]]:
        """Ensure all academic degree programs exist in ERP."""
        prog_url = f"{self.base_url}{ERP_CONFIG['programs_endpoint']}"
        logger.info("Fetching existing degree programs from ERP...")
        existing_resp = self.session.get(prog_url, timeout=15)
        existing_progs = {}
        if existing_resp.status_code == 200:
            for p in existing_resp.json():
                existing_progs[p["code"]] = p

        prog_map = {}
        created_count = 0
        for p in PROGRAMS:
            code = p["code"]
            dept_code = p["departmentCode"]
            dept = dept_map.get(dept_code)
            if not dept:
                logger.warning(f"Department {dept_code} not found for program {code}; skipping.")
                continue

            if code in existing_progs:
                prog_map[code] = existing_progs[code]
            else:
                payload = {
                    "department": {"id": dept["id"]},
                    "code": code,
                    "name": p["name"],
                    "degreeType": p["degreeType"],
                    "durationYears": p["durationYears"],
                    "totalCredits": p["totalCredits"],
                    "active": p["active"]
                }
                res = self.session.post(prog_url, json=payload, timeout=15)
                if res.status_code in [200, 201]:
                    prog_obj = res.json()
                    prog_map[code] = prog_obj
                    created_count += 1
                    logger.info(f"Created Program: {code} - {p['name']}")
                else:
                    logger.warning(f"Failed to create program {code}: {res.status_code} - {res.text}")

        logger.info(f"Programs synced: {len(prog_map)} active ({created_count} newly created).")
        return prog_map

    def seed_curriculum(self, curriculum_data: Optional[Dict[str, Any]] = None, json_file: Optional[Path] = None, update_existing: bool = True) -> Dict[str, Any]:
        """Seed scraped courses and NoteHub subjects into ERP."""
        if not self.token and not self.authenticate():
            return {"error": "Authentication failed"}

        # Load curriculum data
        if not curriculum_data:
            path = json_file or OUTPUT_JSON_PATH
            if not path.exists():
                logger.error(f"Curriculum JSON file not found at {path}. Run scraper first.")
                return {"error": f"File not found: {path}"}
            with open(path, "r", encoding="utf-8") as f:
                curriculum_data = json.load(f)

        courses_to_seed = curriculum_data.get("courses", [])
        logger.info(f"Loaded {len(courses_to_seed)} courses from curriculum data.")

        # 1. Sync Departments & Programs
        dept_map = self.sync_departments()
        prog_map = self.sync_programs(dept_map)

        # 2. Fetch existing courses
        courses_url = f"{self.base_url}{ERP_CONFIG['courses_endpoint']}"
        logger.info("Fetching existing courses from ERP...")
        c_resp = self.session.get(courses_url, timeout=30)
        existing_courses = {}
        if c_resp.status_code == 200:
            for c in c_resp.json():
                existing_courses[c["code"]] = c

        # 3. Seed / Update Courses
        courses_created = 0
        courses_updated = 0
        course_db_map = dict(existing_courses)

        for c in courses_to_seed:
            code = c["code"]
            dept_code = c.get("departmentCode", "CSE")
            dept = dept_map.get(dept_code) or dept_map.get("CSE")

            desc = f"VIT Pune {dept_code} Course: {c['name']} (Credits: {c['credits']}, Scheme: {c['lectureHours']}-{c['tutorialHours']}-{c['practicalHours']})"
            desc = desc[:490]

            payload = {
                "department": {"id": dept["id"]},
                "code": code,
                "name": c["name"][:150],
                "description": desc,
                "credits": c["credits"],
                "courseType": c.get("courseType", "CORE")[:30],
                "lectureHours": c.get("lectureHours", 3),
                "tutorialHours": c.get("tutorialHours", 0),
                "practicalHours": c.get("practicalHours", 0),
                "studyYear": c.get("studyYear", 2)
            }

            if code in course_db_map:
                if update_existing:
                    existing_obj = course_db_map[code]
                    existing_dept_id = (existing_obj.get("department") or {}).get("id")
                    if existing_obj.get("name") != c["name"][:150] or existing_dept_id != dept["id"] or existing_obj.get("studyYear") != c.get("studyYear"):
                        try:
                            put_res = self.session.put(f"{courses_url}/{existing_obj['id']}", json=payload, timeout=15)
                            if put_res.status_code == 200:
                                course_db_map[code] = put_res.json()
                                courses_updated += 1
                        except Exception as e:
                            logger.error(f"Error updating course {code}: {e}")
                continue

            try:
                res = self.session.post(courses_url, json=payload, timeout=15)
                if res.status_code in [200, 201]:
                    saved_course = res.json()
                    course_db_map[code] = saved_course
                    courses_created += 1
                    if courses_created % 25 == 0:
                        logger.info(f"Progress: Seeded {courses_created} courses...")
                else:
                    logger.warning(f"Failed to create course {code}: {res.status_code} - {res.text}")
            except Exception as e:
                logger.error(f"Error seeding course {code}: {e}")

        logger.info(f"Courses sync complete: {courses_created} created, {courses_updated} updated, {len(course_db_map)} total in DB.")

        # 4. Fetch existing NoteHub subjects
        notehub_url = f"{self.base_url}{ERP_CONFIG['notehub_subjects_endpoint']}"
        logger.info("Fetching existing NoteHub subjects from ERP...")
        nh_resp = self.session.get(notehub_url, timeout=30)
        existing_nh_map = {}
        if nh_resp.status_code == 200:
            for s in nh_resp.json():
                c_code = s.get("code")
                if c_code:
                    existing_nh_map[c_code] = s

        # 5. Seed / Update NoteHub Subjects
        nh_created = 0
        nh_updated = 0

        for c in courses_to_seed:
            code = c["code"]
            db_course = course_db_map.get(code)
            if not db_course:
                continue

            course_id = db_course["id"]
            study_year = c.get("studyYear", 2)
            if study_year not in [1, 2, 3, 4]:
                study_year = 2

            payload = {
                "code": code,
                "name": c["name"][:150],
                "studyYear": study_year,
                "course": {"id": course_id}
            }

            if code in existing_nh_map:
                if update_existing:
                    existing_nh = existing_nh_map[code]
                    if existing_nh.get("name") != c["name"][:150] or existing_nh.get("studyYear") != study_year:
                        try:
                            put_res = self.session.put(f"{notehub_url}/{existing_nh['id']}", json=payload, timeout=15)
                            if put_res.status_code == 200:
                                nh_updated += 1
                        except Exception as e:
                            logger.error(f"Error updating NoteHub subject {code}: {e}")
                continue

            try:
                res = self.session.post(notehub_url, json=payload, timeout=15)
                if res.status_code in [200, 201]:
                    existing_nh_map[code] = res.json()
                    nh_created += 1
                    if nh_created % 25 == 0:
                        logger.info(f"Progress: Seeded {nh_created} NoteHub subjects...")
                else:
                    logger.warning(f"Failed to create NoteHub subject {code}: {res.status_code} - {res.text}")
            except Exception as e:
                logger.error(f"Error seeding NoteHub subject {code}: {e}")

        logger.info(f"NoteHub subjects sync complete: {nh_created} created, {nh_updated} updated.")

        summary = {
            "departments": len(dept_map),
            "programs": len(prog_map),
            "courses_total": len(course_db_map),
            "courses_created": courses_created,
            "courses_updated": courses_updated,
            "notehub_created": nh_created,
            "notehub_updated": nh_updated
        }
        return summary

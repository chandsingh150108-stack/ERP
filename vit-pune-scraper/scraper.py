"""
VIT Pune Curriculum Scraper Engine
Extracts course structure, teaching schemes, credits, and syllabus data from official PDF documents.
Provides clean classification by Study Year (1 to 4) and Department/Branches.
"""

import os
import re
import csv
import json
import logging
from pathlib import Path
from typing import List, Dict, Any, Optional, Set

import requests
import pypdf
import pdfplumber

from config import (
    DOWNLOADS_DIR,
    DATA_DIR,
    OUTPUT_JSON_PATH,
    OUTPUT_CSV_PATH,
    SYLLABUS_SOURCES,
    DEPARTMENTS,
    COURSE_CODE_REGEX,
    COURSE_CATEGORIES
)

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("vit_scraper")

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

COURSE_CODE_COMPILED = re.compile(COURSE_CODE_REGEX)


def clean_text(text: Optional[str]) -> str:
    """Sanitize extracted strings, normalize spaces, and strip control characters and bad unicode."""
    if not text:
        return ""
    text = re.sub(r'[\x00-\x08\x0b\x0c\x0e-\x1f\x7f-\x9f]', '', text)
    text = text.replace('\n', ' ').replace('\r', ' ')
    text = text.replace('\ufffd', '-').replace('\u2013', '-').replace('\u2014', '-').replace('\u2212', '-')
    return re.sub(r'\s+', ' ', text).strip()


def is_suspicious_title(name: str) -> bool:
    """Detect broken or concatenated syllabus titles."""
    if not name or len(name) < 3:
        return True
    if COURSE_CODE_COMPILED.match(name.strip().upper()):
        return True
    n_lower = name.lower()
    if any(k in n_lower for k in ['issue', 'page', 'rev no', 'dt.', 'ff no', 'vision', 'mission', 'contents', 'module -', 'semester -']):
        return True
    if re.search(r'\band\s+[A-Z]{2,5}\s*\d{3,4}', name):
        return True
    if re.search(r'\bDesign Thinking \d+ and\b', name, flags=re.IGNORECASE):
        return True
    return False


def clean_course_name(name: str) -> str:
    """Sanitize and normalize course title."""
    if not name:
        return ""
    # Strip category prefixes like PCC: MDM: etc.
    name = re.sub(r'^(?:PCC|MDM|VSEC|ESC|BSC|HSM|AEC|HSS|FP|OE\d?|PEC|EEMC|CC|HSSM)\s*:\s*', '', name, flags=re.IGNORECASE)
    # Strip trailing dots and page numbers from TOC
    name = re.sub(r'\.{2,}.*$', '', name)
    # Strip trailing slot / credits info like "3 s02", "3 S03", "3 s04"
    name = re.sub(r'\s+\d+\s+[sS]\d+.*$', '', name)
    # Strip trailing numbers like " 2 2 30 10 60 3"
    name = re.sub(r'\s+\d+(?:\s+\d+)+.*$', '', name)
    name = re.sub(r'\s+\d+$', '', name)
    # Strip FF No or issue header leftovers
    name = re.sub(r'^(?:FF\s*No\.?\s*:?\s*\d+[A-Z]?|Module\s*[:\-]\s*[IVX]+)\s*', '', name, flags=re.IGNORECASE)
    # Strip trailing teaching scheme / prereq leftovers
    name = re.sub(r'\s+(?:Teaching Scheme|Course Prereq|Prereq|Credits?|Total Credits|Module|Dept\.|A\.Y\.).*$', '', name, flags=re.IGNORECASE)
    # Strip trailing module indicators like "(Modules V & VI))" or "(Modules V & VI)"
    name = re.sub(r'\s*\(\s*Modules?\s+[IVX0-9\s&,]+\)*\s*$', '', name, flags=re.IGNORECASE)
    name = clean_text(name)
    # Clean multiple dashes or trailing dash
    name = re.sub(r'\s*-\s*-\s*', ' - ', name)
    name = re.sub(r'\s+-\s*$', '', name)
    # Strip leading/trailing punctuation
    return name.strip(' -:;,./')


def parse_int_safe(val: Any, default: int = 0) -> int:
    """Safely parse integer from string or cell value."""
    if val is None:
        return default
    val_str = str(val).strip()
    match = re.search(r'\d+', val_str)
    if match:
        try:
            return int(match.group())
        except ValueError:
            return default
    return default


def infer_study_year(course_code: str, fallback_year: int = 2) -> int:
    """
    Infer academic study year (1 to 4) from the course code pattern or fallback.
    VIT Pune codes encode year in the numeric portion:
    - 1xxx -> Year 1 (FY)
    - 2xxx -> Year 2 (SY)
    - 3xxx -> Year 3 (TY)
    - 4xxx -> Year 4 (B.Tech Final Year)
    """
    match = re.search(r'^[A-Z]{2,5}\s*([1-4])\d{2,3}[A-Z]?$', course_code)
    if match:
        return int(match.group(1))
    return fallback_year


def infer_department(course_code: str, fallback_dept: str, study_year: int = 2) -> str:
    """
    Map course code prefix to department code.
    All First Year (study_year == 1) courses belong to DESH.
    """
    if study_year == 1:
        return "DESH"

    code_upper = course_code.upper()
    if code_upper.startswith("CI") or code_upper.startswith("AI") or code_upper.startswith("CSAI"):
        return "AIDS"
    elif code_upper.startswith("ML") or code_upper.startswith("AIML"):
        return "AIML"
    elif code_upper.startswith("CS"):
        return "CSE"
    elif code_upper.startswith("IT"):
        return "IT"
    elif code_upper.startswith("ET") or code_upper.startswith("EC"):
        return "ENTC"
    elif code_upper.startswith("ME"):
        return "MECH"
    elif code_upper.startswith("CH"):
        return "CHEM"
    elif code_upper.startswith("IC") or code_upper.startswith("IN"):
        return "INST"
    elif code_upper.startswith("HS") or code_upper.startswith("ES") or code_upper.startswith("CV") or code_upper.startswith("AS") or code_upper.startswith("XX") or code_upper.startswith("MM") or code_upper.startswith("MD"):
        return "DESH" if study_year == 1 else fallback_dept
    return fallback_dept


def get_applicable_departments(course_code: str, primary_dept: str, study_year: int) -> List[str]:
    """
    Determine all academic department/branch tracks where this course is taught.
    - FY courses are common to all engineers under DESH.
    - Shared humanities (HS...), soft skills (SH...), multidisciplinary minors (MD..., MM...)
      are taken across all SY/TY departments.
    """
    if study_year == 1 or primary_dept == "DESH":
        return ["DESH"]
    code_upper = course_code.upper()
    if code_upper.startswith("HS") or code_upper.startswith("SH") or code_upper.startswith("MD") or code_upper.startswith("MM"):
        return ["CSE", "AIML", "AIDS", "IT", "MECH", "ENTC", "CHEM", "INST"]
    return [primary_dept]


class VITCurriculumScraper:
    def __init__(self, downloads_dir: Path = DOWNLOADS_DIR, data_dir: Path = DATA_DIR):
        self.downloads_dir = Path(downloads_dir)
        self.data_dir = Path(data_dir)
        self.downloads_dir.mkdir(parents=True, exist_ok=True)
        self.data_dir.mkdir(parents=True, exist_ok=True)

    def download_syllabi(self, force: bool = False, dept_filter: Optional[str] = None) -> List[Dict[str, Any]]:
        """Download official syllabus PDFs from VIT Pune website."""
        downloaded = []
        sources = SYLLABUS_SOURCES
        if dept_filter:
            sources = [s for s in sources if s["department"].upper() == dept_filter.upper()]

        for item in sources:
            dest_path = self.downloads_dir / item["filename"]
            item_info = dict(item)
            item_info["filepath"] = dest_path

            if dest_path.exists() and dest_path.stat().st_size > 10000 and not force:
                logger.info(f"Using cached PDF: {item['filename']} ({dest_path.stat().st_size // 1024} KB)")
                downloaded.append(item_info)
                continue

            logger.info(f"Downloading {item['name']} from {item['url']}...")
            try:
                resp = requests.get(item["url"], headers=HEADERS, timeout=60, stream=True)
                if resp.status_code == 200:
                    with open(dest_path, "wb") as f:
                        for chunk in resp.iter_content(chunk_size=16384):
                            if chunk:
                                f.write(chunk)
                    logger.info(f"Saved: {item['filename']} ({dest_path.stat().st_size // 1024} KB)")
                    downloaded.append(item_info)
                else:
                    logger.warning(f"Failed to download {item['filename']}: HTTP {resp.status_code}")
            except Exception as e:
                logger.error(f"Network error while downloading {item['filename']}: {e}")

        return downloaded

    def parse_desh_toc(self, reader: pypdf.PdfReader, pdf_filename: str) -> Dict[str, Dict[str, Any]]:
        """Extract clean course title mapping from Table of Contents in First Year (DESH) PDF."""
        courses = {}
        # Pages 2 and 3 (0-indexed 1 and 2) contain the complete TOC
        for page_num in [1, 2]:
            if page_num >= len(reader.pages):
                continue
            txt = reader.pages[page_num].extract_text() or ""
            for line in txt.split('\n'):
                m = re.match(r'^\s*\d+\.\s*([A-Z]{2,5}\s*\d{3,4}[A-Z]?)\s+(.*?)\s+\d+\s*$', line)
                if m:
                    cand_code = re.sub(r'\s+', '', m.group(1)).upper()
                    raw_title = re.sub(r'^[A-Z]{2,5}\d{4}/[A-Z]{2,5}\d{4}\s*', '', m.group(2))
                    cand_name = clean_course_name(raw_title)
                    if len(cand_name) > 2:
                        courses[cand_code] = {
                            "code": cand_code,
                            "name": cand_name,
                            "credits": 3,
                            "courseType": "CORE",
                            "lectureHours": 3,
                            "tutorialHours": 0,
                            "practicalHours": 0,
                            "studyYear": 1,
                            "departmentCode": "DESH",
                            "applicableDepartments": ["DESH"],
                            "sourcePdf": pdf_filename,
                            "page": page_num + 1,
                            "authoritative": True
                        }
        return courses

    def parse_pdf(self, pdf_path: Path, dept_code: str, default_year: int) -> List[Dict[str, Any]]:
        """Parse tables and course structures from a single syllabus PDF using multi-channel extraction."""
        extracted_courses = {}
        if not pdf_path.exists():
            logger.warning(f"File not found: {pdf_path}")
            return []

        logger.info(f"Parsing {pdf_path.name} (Dept: {dept_code}, Fallback Year: {default_year})...")

        # 1. Full-document fast pass using pypdf
        table_pages_indices = set()
        try:
            reader = pypdf.PdfReader(str(pdf_path))
            total_pages = len(reader.pages)

            # Special case: DESH FY syllabus TOC
            if dept_code == "DESH" or "DESH" in pdf_path.name:
                desh_courses = self.parse_desh_toc(reader, pdf_path.name)
                if desh_courses:
                    extracted_courses.update(desh_courses)
                    logger.info(f"Extracted {len(desh_courses)} courses from DESH TOC.")

            header_pattern = re.compile(
                r'([A-Z]{2,5}\s*\d{3,4}[A-Z]?)\s*:{1,2}\s*([^:\n\r]{3,80}?)(?=\s*[A-Z]{2,5}\s*\d{3,4}|\s*(?:Teaching Scheme|Course Prereq|Prereq|Credits?|Total Credits|Syllabus|CVV|Marks|Theory|\bFF No\b|$))',
                re.IGNORECASE
            )
            toc_pattern = re.compile(
                r'([A-Z]{2,5}\s*\d{3,4}[A-Z]?)\s*:{1,2}\s*([^\.\n\r]+?)(?:\s*\.{2,}|\s+\d+$)',
                re.IGNORECASE
            )
            labeled_code_pattern = re.compile(
                r'(?:Course Code|Subject Code|Code)\s*:\s*([A-Z]{2,5}\s*\d{3,4}[A-Z]?)',
                re.IGNORECASE
            )
            labeled_name_pattern = re.compile(
                r'(?:Course Name|Subject Name|Title)\s*:\s*([^\n\r]{3,80})',
                re.IGNORECASE
            )

            for page_idx in range(total_pages):
                raw_text = reader.pages[page_idx].extract_text() or ""
                if not raw_text.strip():
                    continue

                text_lower = raw_text.lower()
                clean_single_line = " ".join(raw_text.split())

                # Check if this page contains curriculum structure tables
                is_structure_page = (
                    any(k in text_lower for k in ["ff no. 653", "ff no 653", "structure of first year", "title: course structure", "title:course structure"]) or
                    ("course structure" in text_lower and any(k in text_lower for k in ["sub no", "subject code", "sub code", "branch:"]))
                ) and not any(k in text_lower for k in ["ff no. 654", "ff no 654", "syllabus template"])
                if is_structure_page:
                    table_pages_indices.add(page_idx)

                # Channel 1A: Parse Header Pattern (e.g., FF No 654 syllabus detail sections)
                for m in header_pattern.finditer(clean_single_line):
                    cand_code = re.sub(r'\s+', '', m.group(1)).upper()
                    if COURSE_CODE_COMPILED.match(cand_code) and not cand_code.startswith("PAGE"):
                        cand_name = clean_course_name(m.group(2))
                        if len(cand_name) > 3 and not any(k in cand_name.lower() for k in ['issue', 'page', 'rev no', 'dt.', 'ff no', 'vision', 'mission']):
                            cr_match = re.search(r'(?:Total Credits|Credits?)\s*:\s*(\d+)', clean_single_line[m.start():m.start()+200], re.IGNORECASE)
                            cr = int(cr_match.group(1)) if cr_match else 3
                            yr = infer_study_year(cand_code, default_year)
                            dept = infer_department(cand_code, dept_code, yr)
                            app_depts = get_applicable_departments(cand_code, dept, yr)
                            
                            if cand_code not in extracted_courses:
                                extracted_courses[cand_code] = {
                                    "code": cand_code,
                                    "name": cand_name,
                                    "credits": cr,
                                    "courseType": "CORE",
                                    "lectureHours": 3,
                                    "tutorialHours": 0,
                                    "practicalHours": 0,
                                    "studyYear": yr,
                                    "departmentCode": dept,
                                    "applicableDepartments": app_depts,
                                    "sourcePdf": pdf_path.name,
                                    "page": page_idx + 1,
                                    "authoritative": False
                                }
                            else:
                                if not extracted_courses[cand_code].get("authoritative", False):
                                    if not is_suspicious_title(cand_name) and len(cand_name) > len(extracted_courses[cand_code]["name"]):
                                        extracted_courses[cand_code]["name"] = cand_name

                # Channel 1B: Parse Table of Contents / Index pattern
                for m in toc_pattern.finditer(clean_single_line):
                    cand_code = re.sub(r'\s+', '', m.group(1)).upper()
                    if COURSE_CODE_COMPILED.match(cand_code) and not cand_code.startswith("PAGE"):
                        cand_name = clean_course_name(m.group(2))
                        if len(cand_name) > 3 and not any(k in cand_name.lower() for k in ['issue', 'page', 'rev no', 'dt.', 'ff no', 'contents']):
                            yr = infer_study_year(cand_code, default_year)
                            dept = infer_department(cand_code, dept_code, yr)
                            app_depts = get_applicable_departments(cand_code, dept, yr)

                            if cand_code not in extracted_courses:
                                extracted_courses[cand_code] = {
                                    "code": cand_code,
                                    "name": cand_name,
                                    "credits": 3,
                                    "courseType": "CORE",
                                    "lectureHours": 3,
                                    "tutorialHours": 0,
                                    "practicalHours": 0,
                                    "studyYear": yr,
                                    "departmentCode": dept,
                                    "applicableDepartments": app_depts,
                                    "sourcePdf": pdf_path.name,
                                    "page": page_idx + 1,
                                    "authoritative": False
                                }
                            else:
                                if not extracted_courses[cand_code].get("authoritative", False):
                                    if not is_suspicious_title(cand_name) and len(cand_name) > len(extracted_courses[cand_code]["name"]):
                                        extracted_courses[cand_code]["name"] = cand_name

                # Channel 1C: Explicit labeled course code / course name
                m_code = labeled_code_pattern.search(clean_single_line)
                m_name = labeled_name_pattern.search(clean_single_line)
                if m_code and m_name:
                    cand_code = re.sub(r'\s+', '', m_code.group(1)).upper()
                    if COURSE_CODE_COMPILED.match(cand_code) and not cand_code.startswith("PAGE"):
                        cand_name = clean_course_name(m_name.group(1))
                        if len(cand_name) > 3 and not any(k in cand_name.lower() for k in ['issue', 'page', 'rev no', 'dt.', 'ff no']):
                            yr = infer_study_year(cand_code, default_year)
                            dept = infer_department(cand_code, dept_code, yr)
                            app_depts = get_applicable_departments(cand_code, dept, yr)

                            if cand_code not in extracted_courses:
                                extracted_courses[cand_code] = {
                                    "code": cand_code,
                                    "name": cand_name,
                                    "credits": 3,
                                    "courseType": "CORE",
                                    "lectureHours": 3,
                                    "tutorialHours": 0,
                                    "practicalHours": 0,
                                    "studyYear": yr,
                                    "departmentCode": dept,
                                    "applicableDepartments": app_depts,
                                    "sourcePdf": pdf_path.name,
                                    "page": page_idx + 1,
                                    "authoritative": False
                                }
                            else:
                                if not extracted_courses[cand_code].get("authoritative", False):
                                    if not is_suspicious_title(cand_name) and len(cand_name) > len(extracted_courses[cand_code]["name"]):
                                        extracted_courses[cand_code]["name"] = cand_name

        except Exception as e:
            logger.warning(f"Error in pypdf pass for {pdf_path.name}: {e}")

        # 2. Targeted pdfplumber table extraction on candidate pages
        if table_pages_indices:
            try:
                with pdfplumber.open(pdf_path) as pdf:
                    for page_idx in sorted(table_pages_indices):
                        if page_idx >= len(pdf.pages):
                            continue
                        page = pdf.pages[page_idx]
                        tables = page.extract_tables() or []

                        for table in tables:
                            if not table or len(table) < 2:
                                continue

                            code_col = -1
                            name_col = -1
                            type_col = -1
                            th_col = -1
                            tut_col = -1
                            lab_col = -1
                            credit_col = -1

                            for row_i, r in enumerate(table[:3]):
                                for c_i, c_val in enumerate(r):
                                    if c_val is None:
                                        continue
                                    c_clean = clean_text(c_val).lower()
                                    if any(k in c_clean for k in ["course code", "sub code", "subject code", "sub no", "subject no", "code"]) and code_col == -1:
                                        code_col = c_i
                                    elif any(k in c_clean for k in ["course name", "subject name", "course title", "title"]) and name_col == -1:
                                        name_col = c_i
                                    elif "type" in c_clean and type_col == -1:
                                        type_col = c_i
                                    elif c_clean in ["th", "theory", "l", "lecture"] and th_col == -1:
                                        th_col = c_i
                                    elif c_clean in ["tut", "tutorial", "t"] and tut_col == -1:
                                        tut_col = c_i
                                    elif c_clean in ["lab", "laboratory", "p", "practical"] and lab_col == -1:
                                        lab_col = c_i
                                    elif any(k in c_clean for k in ["credits", "credit", "cr"]) and credit_col == -1:
                                        credit_col = c_i

                            for row in table:
                                clean_row = [clean_text(c) for c in row if c is not None]
                                if not clean_row:
                                    continue

                                found_code = None
                                found_code_idx = -1

                                if code_col != -1 and code_col < len(row) and row[code_col]:
                                    cand = re.sub(r'\s+', '', clean_text(row[code_col])).upper()
                                    if COURSE_CODE_COMPILED.match(cand) and not cand.startswith("PAGE"):
                                        found_code = cand
                                        found_code_idx = code_col

                                if not found_code:
                                    for c_idx, cell in enumerate(row):
                                        if not cell:
                                            continue
                                        cand = re.sub(r'\s+', '', clean_text(cell)).upper()
                                        if COURSE_CODE_COMPILED.match(cand) and not cand.startswith("PAGE"):
                                            found_code = cand
                                            found_code_idx = c_idx
                                            break

                                if not found_code:
                                    continue

                                raw_name = ""
                                if name_col != -1 and name_col < len(row) and row[name_col]:
                                    raw_name = clean_text(row[name_col])
                                else:
                                    for c_idx in range(found_code_idx + 1, len(row)):
                                        cell_val = clean_text(row[c_idx])
                                        if len(cell_val) > 3 and not re.match(r'^\d+$', cell_val) and cell_val != found_code:
                                            raw_name = cell_val
                                            break

                                course_name = clean_course_name(raw_name)
                                if not course_name or len(course_name) < 2 or course_name.upper() == found_code.upper() or COURSE_CODE_COMPILED.match(course_name.upper()) or any(h in course_name.lower() for h in ["course name", "subject name", "module", "semester", "contact hours"]):
                                    continue

                                th = parse_int_safe(row[th_col], 3) if (th_col != -1 and th_col < len(row)) else 3
                                tut = parse_int_safe(row[tut_col], 0) if (tut_col != -1 and tut_col < len(row)) else 0
                                lab = parse_int_safe(row[lab_col], 0) if (lab_col != -1 and lab_col < len(row)) else 0
                                credits = parse_int_safe(row[credit_col], 0) if (credit_col != -1 and credit_col < len(row)) else 0

                                if credits <= 0 or credits > 10:
                                    calculated_cr = th + tut + (lab // 2)
                                    credits = max(1, calculated_cr) if calculated_cr > 0 else 3

                                raw_type = clean_text(row[type_col]).upper() if (type_col != -1 and type_col < len(row) and row[type_col]) else "CORE"
                                course_type = COURSE_CATEGORIES.get(raw_type, "CORE")
                                if "PCC" in raw_name.upper():
                                    course_type = "CORE"
                                elif "MDM" in raw_name.upper():
                                    course_type = "MULTI_DISCIPLINARY"
                                elif "VSEC" in raw_name.upper():
                                    course_type = "SKILL_ENHANCEMENT"
                                elif "FP" in raw_name.upper():
                                    course_type = "FIELD_PROJECT"

                                study_year = infer_study_year(found_code, default_year)
                                assigned_dept = infer_department(found_code, dept_code, study_year)
                                app_depts = get_applicable_departments(found_code, assigned_dept, study_year)

                                # Update or insert into map
                                if found_code in extracted_courses:
                                    entry = extracted_courses[found_code]
                                    entry["credits"] = credits
                                    entry["lectureHours"] = th
                                    entry["tutorialHours"] = tut
                                    entry["practicalHours"] = lab
                                    entry["courseType"] = course_type
                                    if course_name and course_name.upper() != found_code.upper() and not COURSE_CODE_COMPILED.match(course_name.upper()):
                                        entry["name"] = course_name
                                    entry["authoritative"] = True
                                    for d in app_depts:
                                        if d not in entry.get("applicableDepartments", []):
                                            entry["applicableDepartments"].append(d)
                                else:
                                    extracted_courses[found_code] = {
                                        "code": found_code,
                                        "name": course_name,
                                        "credits": credits,
                                        "courseType": course_type,
                                        "lectureHours": th,
                                        "tutorialHours": tut,
                                        "practicalHours": lab,
                                        "studyYear": study_year,
                                        "departmentCode": assigned_dept,
                                        "applicableDepartments": app_depts,
                                        "sourcePdf": pdf_path.name,
                                        "page": page_idx + 1,
                                        "authoritative": True
                                    }
            except Exception as e:
                logger.error(f"Error reading tables with pdfplumber from {pdf_path}: {e}")

        course_list = list(extracted_courses.values())
        logger.info(f"Extracted {len(course_list)} unique courses from {pdf_path.name}")
        return course_list

    def extract_all(self, force_download: bool = False, dept_filter: Optional[str] = None) -> List[Dict[str, Any]]:
        """Execute full scraping pipeline across all department syllabi."""
        downloaded_sources = self.download_syllabi(force=force_download, dept_filter=dept_filter)
        raw_courses = []

        for src in downloaded_sources:
            courses = self.parse_pdf(
                pdf_path=src["filepath"],
                dept_code=src["department"],
                default_year=src["default_year"]
            )
            raw_courses.extend(courses)

        # Deduplicate and consolidate courses by unique course code
        course_map = {}
        for c in raw_courses:
            code = c["code"]
            if code not in course_map:
                course_map[code] = dict(c)
                if "applicableDepartments" not in course_map[code]:
                    course_map[code]["applicableDepartments"] = [c["departmentCode"]]
            else:
                existing = course_map[code]
                c_auth = c.get("authoritative", False)
                ex_auth = existing.get("authoritative", False)

                # 1. Authoritative table/TOC beats non-authoritative regex
                if c_auth and not ex_auth:
                    existing["name"] = c["name"]
                    existing["credits"] = c["credits"]
                    existing["authoritative"] = True
                    existing["sourcePdf"] = c.get("sourcePdf")
                elif not c_auth and ex_auth:
                    pass  # Keep authoritative name!
                else:
                    # Both authoritative or both non-authoritative:
                    # Prefer 2026-27 or newer pattern over older
                    c_src = str(c.get("sourcePdf", ""))
                    ex_src = str(existing.get("sourcePdf", ""))
                    if "2026-27" in c_src and "2026-27" not in ex_src:
                        existing["name"] = c["name"]
                        existing["credits"] = c["credits"]
                        existing["sourcePdf"] = c.get("sourcePdf")
                    elif not is_suspicious_title(c["name"]) and (is_suspicious_title(existing["name"]) or len(c["name"]) > len(existing["name"])):
                        existing["name"] = c["name"]

                if c["credits"] > existing["credits"] and existing["credits"] == 3:
                    existing["credits"] = c["credits"]
                if c["lectureHours"] > existing["lectureHours"]:
                    existing["lectureHours"] = c["lectureHours"]
                if c["practicalHours"] > existing["practicalHours"]:
                    existing["practicalHours"] = c["practicalHours"]
                if existing["courseType"] == "CORE" and c["courseType"] != "CORE":
                    existing["courseType"] = c["courseType"]
                for d in c.get("applicableDepartments", []):
                    if d not in existing["applicableDepartments"]:
                        existing["applicableDepartments"].append(d)

        consolidated = sorted(course_map.values(), key=lambda x: (x["departmentCode"], x["studyYear"], x["code"]))
        logger.info(f"Total unique consolidated courses extracted: {len(consolidated)}")
        return consolidated

    def export(self, courses: List[Dict[str, Any]], json_path: Path = OUTPUT_JSON_PATH, csv_path: Path = OUTPUT_CSV_PATH):
        """Export scraped courses to standard JSON and CSV files."""
        json_path.parent.mkdir(parents=True, exist_ok=True)
        csv_path.parent.mkdir(parents=True, exist_ok=True)

        with open(json_path, "w", encoding="utf-8") as f:
            json.dump({
                "institution": "Vishwakarma Institute of Technology, Pune",
                "institutionCode": "VIT_PUNE",
                "totalCourses": len(courses),
                "departments": DEPARTMENTS,
                "courses": courses
            }, f, indent=2, ensure_ascii=False)
        logger.info(f"Exported JSON dataset to: {json_path}")

        if courses:
            fieldnames = [
                "code", "name", "credits", "courseType", "lectureHours",
                "tutorialHours", "practicalHours", "studyYear", "departmentCode",
                "applicableDepartments", "sourcePdf"
            ]
            with open(csv_path, "w", newline="", encoding="utf-8") as f:
                writer = csv.DictWriter(f, fieldnames=fieldnames, extrasaction="ignore")
                writer.writeheader()
                for c in courses:
                    row = dict(c)
                    if isinstance(row.get("applicableDepartments"), list):
                        row["applicableDepartments"] = ",".join(row["applicableDepartments"])
                    writer.writerow(row)
            logger.info(f"Exported CSV dataset to: {csv_path}")

    def get_summary_stats(self, courses: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Generate breakdown statistics by department, year, and course type."""
        dept_counts = {}
        year_counts = {1: 0, 2: 0, 3: 0, 4: 0}
        type_counts = {}

        for c in courses:
            dept = c["departmentCode"]
            dept_counts[dept] = dept_counts.get(dept, 0) + 1
            y = c.get("studyYear", 2)
            year_counts[y] = year_counts.get(y, 0) + 1
            t = c.get("courseType", "CORE")
            type_counts[t] = type_counts.get(t, 0) + 1

        return {
            "total": len(courses),
            "by_department": dept_counts,
            "by_year": year_counts,
            "by_type": type_counts
        }

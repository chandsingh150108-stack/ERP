"""
Configuration and constants for VIT Pune Curriculum Scraper & ERP Seeder.
"""
import os
from pathlib import Path

# Base Paths
BASE_DIR = Path(__file__).resolve().parent
DOWNLOADS_DIR = BASE_DIR / "downloads"
DATA_DIR = BASE_DIR / "data"

OUTPUT_JSON_PATH = DATA_DIR / "vit_pune_curriculum.json"
OUTPUT_CSV_PATH = DATA_DIR / "vit_pune_courses.csv"

# Ensure directories exist
DOWNLOADS_DIR.mkdir(parents=True, exist_ok=True)
DATA_DIR.mkdir(parents=True, exist_ok=True)

# VIT Pune Departments
DEPARTMENTS = {
    "DESH": {
        "code": "DESH",
        "name": "Department of Engineering Sciences and Humanities",
        "description": "First Year engineering foundation across basic sciences, mathematics, and introductory computing.",
        "active": True
    },
    "CSE": {
        "code": "CSE",
        "name": "Computer Science and Engineering",
        "description": "Core computer science, algorithms, software engineering, databases, and advanced systems.",
        "active": True
    },
    "IT": {
        "code": "IT",
        "name": "Information Technology",
        "description": "Information architecture, web engineering, cloud computing, networks, and distributed systems.",
        "active": True
    },
    "AIDS": {
        "code": "AIDS",
        "name": "Artificial Intelligence and Data Science",
        "description": "Data engineering, machine learning pipelines, statistical computing, and predictive models.",
        "active": True
    },
    "AIML": {
        "code": "AIML",
        "name": "Artificial Intelligence and Machine Learning",
        "description": "Neural networks, deep learning, computer vision, natural language processing, and autonomous systems.",
        "active": True
    },
    "ENTC": {
        "code": "ENTC",
        "name": "Electronics and Telecommunication Engineering",
        "description": "Embedded systems, signal processing, VLSI design, wireless communications, and IoT.",
        "active": True
    },
    "MECH": {
        "code": "MECH",
        "name": "Mechanical Engineering",
        "description": "Thermodynamics, mechanical design, CAD/CAM, fluid mechanics, robotics, and industrial automation.",
        "active": True
    },
    "CHEM": {
        "code": "CHEM",
        "name": "Chemical Engineering",
        "description": "Chemical processes, reaction engineering, heat transfer, separation processes, and process dynamics.",
        "active": True
    },
    "INST": {
        "code": "INST",
        "name": "Instrumentation Engineering",
        "description": "Sensors and transducers, industrial automation, PLC/SCADA, process control, and biomedical instrumentation.",
        "active": True
    }
}

# Programs to register in ERP
PROGRAMS = [
    {
        "code": "BTECH-CSE",
        "name": "B.Tech Computer Science and Engineering",
        "degreeType": "B.Tech",
        "durationYears": 4,
        "totalCredits": 160,
        "departmentCode": "CSE",
        "active": True
    },
    {
        "code": "BTECH-IT",
        "name": "B.Tech Information Technology",
        "degreeType": "B.Tech",
        "durationYears": 4,
        "totalCredits": 160,
        "departmentCode": "IT",
        "active": True
    },
    {
        "code": "BTECH-AIDS",
        "name": "B.Tech Artificial Intelligence and Data Science",
        "degreeType": "B.Tech",
        "durationYears": 4,
        "totalCredits": 160,
        "departmentCode": "AIDS",
        "active": True
    },
    {
        "code": "BTECH-AIML",
        "name": "B.Tech Artificial Intelligence and Machine Learning",
        "degreeType": "B.Tech",
        "durationYears": 4,
        "totalCredits": 160,
        "departmentCode": "AIML",
        "active": True
    },
    {
        "code": "BTECH-ENTC",
        "name": "B.Tech Electronics and Telecommunication Engineering",
        "degreeType": "B.Tech",
        "durationYears": 4,
        "totalCredits": 160,
        "departmentCode": "ENTC",
        "active": True
    },
    {
        "code": "BTECH-MECH",
        "name": "B.Tech Mechanical Engineering",
        "degreeType": "B.Tech",
        "durationYears": 4,
        "totalCredits": 160,
        "departmentCode": "MECH",
        "active": True
    },
    {
        "code": "BTECH-CHEM",
        "name": "B.Tech Chemical Engineering",
        "degreeType": "B.Tech",
        "durationYears": 4,
        "totalCredits": 160,
        "departmentCode": "CHEM",
        "active": True
    },
    {
        "code": "BTECH-INST",
        "name": "B.Tech Instrumentation Engineering",
        "degreeType": "B.Tech",
        "durationYears": 4,
        "totalCredits": 160,
        "departmentCode": "INST",
        "active": True
    }
]

# Official Syllabus PDF Sources from vit.edu
SYLLABUS_SOURCES = [
    {
        "department": "DESH",
        "name": "First Year Common (FY B.Tech)",
        "url": "https://www.vit.edu/DESH/wp-content/uploads/2025/09/FY-B-Tech-Syllabus-A.Y2025-26.pdf",
        "filename": "DESH_FY_2025-26.pdf",
        "default_year": 1
    },
    {
        "department": "CSE",
        "name": "Computer Engineering (Full 4-Year Syllabus)",
        "url": "https://www.vit.edu/computer/wp-content/uploads/2024/11/Syllabus_CS_AY2024_25.pdf",
        "filename": "CSE_Full_2024-25.pdf",
        "default_year": 2
    },
    {
        "department": "CSE",
        "name": "Computer Engineering (SY B.Tech)",
        "url": "https://www.vit.edu/computer/wp-content/uploads/2026/08/Syllabus_SY-CS_AY2026-27_V3.pdf",
        "filename": "CSE_SY_2026-27.pdf",
        "default_year": 2
    },
    {
        "department": "CSE",
        "name": "Computer Engineering (TY & Final Year B.Tech)",
        "url": "https://www.vit.edu/computer/wp-content/uploads/2025/08/TY-BTech-2025-26_Ver-2.pdf",
        "filename": "CSE_TY_BTech_2025-26.pdf",
        "default_year": 3
    },
    {
        "department": "IT",
        "name": "Information Technology (Complete Syllabus)",
        "url": "https://www.vit.edu/IT/wp-content/uploads/2025/08/Final-Syllabus-AY-2025-26.pdf",
        "filename": "IT_Final_2025-26.pdf",
        "default_year": 2
    },
    {
        "department": "AIDS",
        "name": "AI & Data Science (Complete Syllabus)",
        "url": "https://www.vit.edu/CSE-AI/wp-content/uploads/2025/08/Syllabus_CSE_AI_AY25_26_Ver1.0-CSAI-Dept-Ver2.0-8-8-2025.pdf",
        "filename": "AIDS_Complete_2025-26.pdf",
        "default_year": 2
    },
    {
        "department": "AIML",
        "name": "AI & Machine Learning (Complete Multi-Year Syllabus)",
        "url": "https://www.vit.edu/CSE-AI-ML/wp-content/uploads/2025/09/AIML-Syllabus_CSE_AIML_AY-25_26_Ver-3.0-_F.docx-3.pdf",
        "filename": "AIML_Complete_2025-26.pdf",
        "default_year": 2
    },
    {
        "department": "AIML",
        "name": "AI & Machine Learning (SY Syllabus)",
        "url": "https://www.vit.edu/CSE-AI-ML/wp-content/uploads/2026/08/SY_Syllabus-CSEAIML.pdf",
        "filename": "AIML_SY_2026-27.pdf",
        "default_year": 2
    },
    {
        "department": "ENTC",
        "name": "Electronics & Telecommunication (Complete Syllabus)",
        "url": "https://www.vit.edu/E-TC/wp-content/uploads/2025/02/ETC-structure-and-Syllabus-2024-25-Merged-1.pdf",
        "filename": "ENTC_Merged_2024-25.pdf",
        "default_year": 2
    },
    {
        "department": "MECH",
        "name": "Mechanical Engineering (Structure & Syllabi)",
        "url": "https://www.vit.edu/mechanical/wp-content/uploads/2026/08/VIT_Mech-Structure-Syllabi-AY-2026-27_Sin-1.pdf",
        "filename": "MECH_Complete_2026-27.pdf",
        "default_year": 2
    },
    {
        "department": "CHEM",
        "name": "Chemical Engineering (Complete B.Tech Syllabus)",
        "url": "https://www.vit.edu/chemical/wp-content/uploads/2024/04/BTech_Chemical-syllabus-2022-23.pdf",
        "filename": "CHEM_Complete_BTech.pdf",
        "default_year": 3
    },
    {
        "department": "CHEM",
        "name": "Chemical Engineering (Merged Syllabus)",
        "url": "https://www.vit.edu/chemical/wp-content/uploads/2026/07/Merged-chemical-2026-27-syllabus.pdf",
        "filename": "CHEM_Merged_2026-27.pdf",
        "default_year": 2
    },
    {
        "department": "INST",
        "name": "Instrumentation Engineering (Syllabus 2025-26)",
        "url": "https://www.vit.edu/instrumentation/wp-content/uploads/2026/01/Syllabus-25-26.pdf",
        "filename": "INST_Complete_2025-26.pdf",
        "default_year": 2
    }
]

# Regex patterns for VIT Pune course codes (e.g., CS2305, HS1084, IT3001, ME1019, MD3101, ET2001)
COURSE_CODE_REGEX = r'^[A-Z]{2,5}\s*\d{3,4}[A-Z]?$'

# Course categories recognizable from syllabus tables
COURSE_CATEGORIES = {
    "PCC": "CORE",
    "CORE": "CORE",
    "ESC": "ENGINEERING_SCIENCE",
    "ESE": "ENGINEERING_SCIENCE",
    "BSC": "BASIC_SCIENCE",
    "BSE": "BASIC_SCIENCE",
    "HSM": "HUMANITIES",
    "HSS": "HUMANITIES",
    "AEC": "ABILITY_ENHANCEMENT",
    "VSEC": "SKILL_ENHANCEMENT",
    "MDM": "MULTI_DISCIPLINARY",
    "MDOE": "OPEN_ELECTIVE",
    "OE": "OPEN_ELECTIVE",
    "PEC": "PROFESSIONAL_ELECTIVE",
    "FP": "FIELD_PROJECT",
    "CC": "CO_CURRICULAR",
    "AU": "AUDIT"
}

# ERP Backend Configuration
ERP_CONFIG = {
    "base_url": os.getenv("ERP_BASE_URL", "http://localhost:8080"),
    "auth_endpoint": "/api/auth/login",
    "departments_endpoint": "/api/departments",
    "programs_endpoint": "/api/programs",
    "courses_endpoint": "/api/courses",
    "notehub_subjects_endpoint": "/api/notehub/subjects",
    "username": os.getenv("ERP_ADMIN_USER", "admin"),
    "password": os.getenv("ERP_ADMIN_PASS", "admin123")
}

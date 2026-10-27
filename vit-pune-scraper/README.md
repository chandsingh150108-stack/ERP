# VIT Pune Curriculum Scraper & ERP Seeder 🎓

[![Python 3.10+](https://img.shields.io/badge/python-3.10+-blue.svg)](https://www.python.org/downloads/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Format: JSON & CSV](https://img.shields.io/badge/export-JSON%20%7C%20CSV-orange.svg)](#data-exports)
[![Compatibility: Student ERP](https://img.shields.io/badge/integrates-Student%20ERP-purple.svg)](#erp-database-seeding)

An automated data scraping, extraction, and database seeding pipeline for the complete academic curriculum of **Vishwakarma Institute of Technology (VIT Pune)**.

This utility parses official syllabus documents across all engineering departments and academic years (First Year to Final Year B.Tech), extracts structured course information (Course Code, Name, Credits, Teaching Scheme, Category, Study Year, and Department), and exports clean datasets as well as automatically seeds them into the **Student ERP System** via REST API.

---

## 🏛️ Department Coverage

The scraper extracts courses across all 9 academic departments of VIT Pune:

| Department Code | Full Department Name | Academic Scope |
| :--- | :--- | :--- |
| **DESH** | Dept. of Engineering Sciences & Humanities | First Year (FY B.Tech - Semesters 1 & 2) |
| **CSE** | Computer Science and Engineering | SY, TY, Final Year B.Tech |
| **IT** | Information Technology | Complete 4-Year B.Tech Curriculum |
| **AIDS** | Artificial Intelligence & Data Science | Complete 4-Year B.Tech Curriculum |
| **AIML** | Artificial Intelligence & Machine Learning | SY, TY, Final Year B.Tech |
| **ENTC** | Electronics & Telecommunication Engineering | Complete 4-Year B.Tech Curriculum |
| **MECH** | Mechanical Engineering | Complete 4-Year B.Tech Curriculum |
| **CHEM** | Chemical Engineering | Complete 4-Year B.Tech Curriculum |
| **INST** | Instrumentation Engineering | Complete 4-Year B.Tech Curriculum |

---

## 🚀 Key Features

- **Automated PDF Downloader**: Downloads and caches official syllabus PDFs directly from `vit.edu`.
- **Intelligent Table Parsing**: Utilizes `pdfplumber` with heuristic column detection to extract teaching schemes, course titles, categories, and credit allocations.
- **Course Normalization & Deduplication**: Strips table artifacts, handles multi-disciplinary course codes, cleans course names, and resolves duplicates across shared modules.
- **Dual Format Export**: Produces standardized, structured datasets in both **JSON** (`data/vit_pune_curriculum.json`) and **CSV** (`data/vit_pune_courses.csv`).
- **One-Click ERP Seeder**: Directly authenticates with the Student ERP backend (`/api/auth/login`), synchronizes departments, creates academic degree programs, and seeds courses and NoteHub subjects with relational mappings.
- **Standalone CLI**: Simple command-line interface with rich progress tracking and breakdown statistics.

---

## 📂 Repository Structure

```
vit-pune-scraper/
├── README.md               # Complete documentation and setup guide
├── LICENSE                 # MIT Open-Source License
├── requirements.txt        # Python package dependencies
├── .gitignore              # Git ignore rules for cached PDFs, venv, and IDE files
├── config.py               # Department mappings, syllabus URLs, and ERP endpoints
├── scraper.py              # PDF extraction engine, normalization, and exporters
├── seeder.py               # REST API client for Student ERP database ingestion
├── main.py                 # Unified CLI interface
├── data/
│   ├── .gitkeep
│   ├── vit_pune_curriculum.json   # Exported JSON dataset
│   └── vit_pune_courses.csv       # Exported CSV dataset
└── downloads/
    └── .gitignore          # Cached syllabus PDF files (ignored from git)
```

---

## 🛠️ Installation & Setup

### Prerequisites
- Python **3.10+** (Tested on Python 3.11, 3.12, 3.13)
- `pip` package manager

### 1. Clone or Copy the Repository
```bash
git clone https://github.com/<your-username>/vit-pune-scraper.git
cd vit-pune-scraper
```

### 2. Create a Virtual Environment (Optional but recommended)
```bash
# Windows
python -m venv venv
.\venv\Scripts\activate

# Linux / macOS
python3 -m venv venv
source venv/bin/activate
```

### 3. Install Dependencies
```bash
pip install -r requirements.txt
```

---

## 💻 CLI Usage Guide

Run the CLI using `python main.py [FLAGS]`:

### 1. Full End-to-End Execution (Scrape + Export + Seed)
Downloads missing PDFs, parses the curriculum, exports JSON/CSV, and seeds the running ERP:
```bash
python main.py --all
```

### 2. Scrape Only (Generate JSON & CSV)
Extracts courses from all departments without seeding:
```bash
python main.py --scrape
```

### 3. Scrape Specific Department
Filter scraping to a single department (e.g., Computer Engineering):
```bash
python main.py --scrape --dept CSE
```

### 4. Seed Existing Scraped Data into ERP
Ingests previously scraped JSON into the Student ERP database:
```bash
python main.py --seed
```

With custom credentials or ERP host:
```bash
python main.py --seed --erp-url http://localhost:8080 --username admin --password admin123
```

### 5. View Dataset Statistics
Displays tabular breakdown by department, academic study year, and course category:
```bash
python main.py --stats
```

### 6. Force Re-download Cached PDFs
Re-downloads all syllabus PDFs fresh from `vit.edu`:
```bash
python main.py --scrape --force-download
```

---

## 📊 Data Schemas

### Course JSON Schema (`data/vit_pune_curriculum.json`)
```json
{
  "institution": "Vishwakarma Institute of Technology, Pune",
  "institutionCode": "VIT_PUNE",
  "totalCourses": 355,
  "courses": [
    {
      "code": "CS2305",
      "name": "Data Structures-I",
      "credits": 4,
      "courseType": "CORE",
      "lectureHours": 3,
      "tutorialHours": 0,
      "practicalHours": 2,
      "studyYear": 2,
      "departmentCode": "CSE",
      "sourcePdf": "CSE_SY_2026-27.pdf"
    }
  ]
}
```

### CSV Schema (`data/vit_pune_courses.csv`)
| Column | Type | Example | Description |
| :--- | :--- | :--- | :--- |
| `code` | String | `CS2305` | Unique VIT Pune course code |
| `name` | String | `Data Structures-I` | Course title |
| `credits` | Integer | `4` | Total academic credit points |
| `courseType` | String | `CORE` | `CORE`, `BASIC_SCIENCE`, `MULTI_DISCIPLINARY`, etc. |
| `lectureHours` | Integer | `3` | Weekly lecture hours (Theory) |
| `tutorialHours`| Integer | `0` | Weekly tutorial hours |
| `practicalHours`| Integer | `2` | Weekly lab/practical hours |
| `studyYear` | Integer | `2` | Academic level: 1 (FY), 2 (SY), 3 (TY), 4 (Final) |
| `departmentCode`| String | `CSE` | Department offering the course |
| `sourcePdf` | String | `CSE_SY_...` | Name of the syllabus PDF source |

---

## 🌐 ERP Database Seeding

The seeder automates the following actions via REST endpoints:
1. **Department Sync**: Validates and creates all 9 VIT Pune engineering departments (`/api/departments`).
2. **Program Sync**: Registers 4-year degree programs (`BTECH-CSE`, `BTECH-IT`, `BTECH-AIDS`, `BTECH-AIML`, `BTECH-ENTC`, `BTECH-MECH`, `BTECH-CHEM`, `BTECH-INST`) under `/api/programs`.
3. **Course Ingestion**: Persists courses with teaching hour breakdowns into `/api/courses`.
4. **NoteHub Integration**: Creates corresponding `NoteHubSubject` records (`/api/notehub/subjects`) mapped to each course and academic study year (1–4), instantly enabling student note uploads and resource sharing.

---

## 📤 How to Publish to Your Own GitHub Repository

To publish this folder as an independent repository on your GitHub account:

```bash
# 1. Navigate to the vit-pune-scraper directory
cd vit-pune-scraper

# 2. Initialize a fresh Git repository
git init

# 3. Add all files
git add .

# 4. Create your initial commit
git commit -m "feat: initial commit for VIT Pune curriculum scraper and ERP seeder"

# 5. Set default branch to main
git branch -M main

# 6. Add your personal GitHub remote
git remote add origin https://github.com/<your-username>/vit-pune-scraper.git

# 7. Push to GitHub
git push -u origin main
```

---

## 📄 License
This project is open-source and released under the [MIT License](LICENSE).

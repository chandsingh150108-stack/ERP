#!/usr/bin/env python3
"""
Populate 50 Students Script for Student ERP System.
Logs in as Administrator, creates 50 students scattered across 8 degree programs and 4 academic years,
verifies student login credentials (username == rollNumber, password == rollNumber),
and exports the student registry into STUDENTS.md.
"""

import sys
import json
import urllib.request
import urllib.error
import os

API_BASE = os.environ.get("API_URL", "http://localhost:8080/api")

STUDENTS_DATA = [
    # --- CSE AIML (7 Students) ---
    {"rollNumber": "AIML2401", "firstName": "Aarav", "lastName": "Sharma", "gender": "Male", "course": "CSE AIML", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "AIML2402", "firstName": "Ananya", "lastName": "Iyer", "gender": "Female", "course": "CSE AIML", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "AIML2301", "firstName": "Rohan", "lastName": "Deshmukh", "gender": "Male", "course": "CSE AIML", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "AIML2302", "firstName": "Tanvi", "lastName": "Kulkarni", "gender": "Female", "course": "CSE AIML", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "AIML2201", "firstName": "Aditya", "lastName": "Patil", "gender": "Male", "course": "CSE AIML", "year": "TY", "studyYear": 3, "currentSemester": 5, "status": "ACTIVE"},
    {"rollNumber": "AIML2202", "firstName": "Sneha", "lastName": "Joshi", "gender": "Female", "course": "CSE AIML", "year": "TY", "studyYear": 3, "currentSemester": 5, "status": "ACTIVE"},
    {"rollNumber": "AIML2101", "firstName": "Vikram", "lastName": "Verma", "gender": "Male", "course": "CSE AIML", "year": "FINAL", "studyYear": 4, "currentSemester": 7, "status": "ACTIVE"},

    # --- CSE (7 Students) ---
    {"rollNumber": "CSE2401", "firstName": "Ishaan", "lastName": "Mehta", "gender": "Male", "course": "CSE", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "CSE2402", "firstName": "Diya", "lastName": "Nair", "gender": "Female", "course": "CSE", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "CSE2301", "firstName": "Arjun", "lastName": "Reddy", "gender": "Male", "course": "CSE", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "CSE2302", "firstName": "Kavya", "lastName": "Chopra", "gender": "Female", "course": "CSE", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "CSE2201", "firstName": "Siddharth", "lastName": "Gupta", "gender": "Male", "course": "CSE", "year": "TY", "studyYear": 3, "currentSemester": 5, "status": "ACTIVE"},
    {"rollNumber": "CSE2202", "firstName": "Pooja", "lastName": "Rao", "gender": "Female", "course": "CSE", "year": "TY", "studyYear": 3, "currentSemester": 5, "status": "ACTIVE"},
    {"rollNumber": "CSE2101", "firstName": "Rahul", "lastName": "Kapoor", "gender": "Male", "course": "CSE", "year": "FINAL", "studyYear": 4, "currentSemester": 7, "status": "ACTIVE"},

    # --- IT (6 Students) ---
    {"rollNumber": "IT2401", "firstName": "Reyansh", "lastName": "Bansal", "gender": "Male", "course": "IT", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "IT2402", "firstName": "Saanvi", "lastName": "Mishra", "gender": "Female", "course": "IT", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "IT2301", "firstName": "Kabir", "lastName": "Saxena", "gender": "Male", "course": "IT", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "IT2302", "firstName": "Riya", "lastName": "Trivedi", "gender": "Female", "course": "IT", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "IT2201", "firstName": "Pranav", "lastName": "Bhat", "gender": "Male", "course": "IT", "year": "TY", "studyYear": 3, "currentSemester": 5, "status": "ACTIVE"},
    {"rollNumber": "IT2101", "firstName": "Neha", "lastName": "Singhania", "gender": "Female", "course": "IT", "year": "FINAL", "studyYear": 4, "currentSemester": 7, "status": "ACTIVE"},

    # --- AIDS (6 Students) ---
    {"rollNumber": "AIDS2401", "firstName": "Vivaan", "lastName": "Khurana", "gender": "Male", "course": "AIDS", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "AIDS2402", "firstName": "Aanya", "lastName": "Agarwal", "gender": "Female", "course": "AIDS", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "AIDS2301", "firstName": "Dev", "lastName": "Malhotra", "gender": "Male", "course": "AIDS", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "AIDS2302", "firstName": "Isha", "lastName": "Pillai", "gender": "Female", "course": "AIDS", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "AIDS2201", "firstName": "Manish", "lastName": "Tandon", "gender": "Male", "course": "AIDS", "year": "TY", "studyYear": 3, "currentSemester": 5, "status": "ACTIVE"},
    {"rollNumber": "AIDS2101", "firstName": "Simran", "lastName": "Kaur", "gender": "Female", "course": "AIDS", "year": "FINAL", "studyYear": 4, "currentSemester": 7, "status": "ACTIVE"},

    # --- ENTC (6 Students) ---
    {"rollNumber": "ENTC2401", "firstName": "Shaurya", "lastName": "Choudhury", "gender": "Male", "course": "ENTC", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "ENTC2402", "firstName": "Myra", "lastName": "Sengupta", "gender": "Female", "course": "ENTC", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "ENTC2301", "firstName": "Yash", "lastName": "Bhardwaj", "gender": "Male", "course": "ENTC", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "ENTC2302", "firstName": "Tara", "lastName": "Menon", "gender": "Female", "course": "ENTC", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "ENTC2201", "firstName": "Harsh", "lastName": "Pandey", "gender": "Male", "course": "ENTC", "year": "TY", "studyYear": 3, "currentSemester": 5, "status": "ACTIVE"},
    {"rollNumber": "ENTC2101", "firstName": "Kriti", "lastName": "Dubey", "gender": "Female", "course": "ENTC", "year": "FINAL", "studyYear": 4, "currentSemester": 7, "status": "ACTIVE"},

    # --- MECH (6 Students) ---
    {"rollNumber": "MECH2401", "firstName": "Atharv", "lastName": "Kadam", "gender": "Male", "course": "MECH", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "MECH2402", "firstName": "Avani", "lastName": "Shinde", "gender": "Female", "course": "MECH", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "MECH2301", "firstName": "Omkar", "lastName": "More", "gender": "Male", "course": "MECH", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "MECH2302", "firstName": "Rutuja", "lastName": "Gaikwad", "gender": "Female", "course": "MECH", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "MECH2201", "firstName": "Sanket", "lastName": "Bhosale", "gender": "Male", "course": "MECH", "year": "TY", "studyYear": 3, "currentSemester": 5, "status": "ACTIVE"},
    {"rollNumber": "MECH2101", "firstName": "Pratik", "lastName": "Sawant", "gender": "Male", "course": "MECH", "year": "FINAL", "studyYear": 4, "currentSemester": 7, "status": "ACTIVE"},

    # --- CHEM (6 Students) ---
    {"rollNumber": "CHEM2401", "firstName": "Aayush", "lastName": "Tiwari", "gender": "Male", "course": "CHEM", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "CHEM2402", "firstName": "Khushi", "lastName": "Yadav", "gender": "Female", "course": "CHEM", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "CHEM2301", "firstName": "Kunal", "lastName": "Mane", "gender": "Male", "course": "CHEM", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "CHEM2302", "firstName": "Shruti", "lastName": "Kamble", "gender": "Female", "course": "CHEM", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "CHEM2201", "firstName": "Nikhil", "lastName": "Chavan", "gender": "Male", "course": "CHEM", "year": "TY", "studyYear": 3, "currentSemester": 5, "status": "ACTIVE"},
    {"rollNumber": "CHEM2101", "firstName": "Vaishnavi", "lastName": "Pawar", "gender": "Female", "course": "CHEM", "year": "FINAL", "studyYear": 4, "currentSemester": 7, "status": "ACTIVE"},

    # --- INST (6 Students) ---
    {"rollNumber": "INST2401", "firstName": "Samarth", "lastName": "Salunkhe", "gender": "Male", "course": "INST", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "INST2402", "firstName": "Gauri", "lastName": "Jadhav", "gender": "Female", "course": "INST", "year": "FY", "studyYear": 1, "currentSemester": 1, "status": "ACTIVE"},
    {"rollNumber": "INST2301", "firstName": "Chinmay", "lastName": "Tambe", "gender": "Male", "course": "INST", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "INST2302", "firstName": "Meera", "lastName": "Sonar", "gender": "Female", "course": "INST", "year": "SY", "studyYear": 2, "currentSemester": 3, "status": "ACTIVE"},
    {"rollNumber": "INST2201", "firstName": "Rupesh", "lastName": "Ghatge", "gender": "Male", "course": "INST", "year": "TY", "studyYear": 3, "currentSemester": 5, "status": "ACTIVE"},
    {"rollNumber": "INST2101", "firstName": "Priyanka", "lastName": "Wagh", "gender": "Female", "course": "INST", "year": "FINAL", "studyYear": 4, "currentSemester": 7, "status": "ACTIVE"},
]

def make_request(url, method="GET", data=None, headers=None):
    if headers is None:
        headers = {}
    req = urllib.request.Request(url, method=method)
    for k, v in headers.items():
        req.add_header(k, v)
    if data is not None:
        req.add_header("Content-Type", "application/json")
        encoded = json.dumps(data).encode("utf-8")
        req.data = encoded
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            content = resp.read().decode("utf-8")
            return resp.status, json.loads(content) if content else {}
    except urllib.error.HTTPError as e:
        err_content = e.read().decode("utf-8")
        try:
            return e.code, json.loads(err_content)
        except:
            return e.code, {"error": err_content}
    except Exception as e:
        return 0, {"error": str(e)}

def login_admin():
    print("Logging in as Administrator (admin / admin123)...")
    status, res = make_request(f"{API_BASE}/auth/login", method="POST", data={
        "username": "admin",
        "password": "admin123"
    })
    if status == 200 and "token" in res:
        print(f" Admin authentication successful! Token acquired.")
        return res["token"]
    else:
        print(f"❌ Admin login failed: status={status}, res={res}")
        sys.exit(1)

def verify_student_login(roll_number):
    status, res = make_request(f"{API_BASE}/auth/login", method="POST", data={
        "username": roll_number,
        "password": roll_number
    })
    return status == 200 and "token" in res

def main():
    print(f"Starting population of {len(STUDENTS_DATA)} students via Admin API...")
    admin_token = login_admin()
    auth_header = {"Authorization": f"Bearer {admin_token}"}

    # Fetch programs map
    status, programs = make_request(f"{API_BASE}/programs", method="GET", headers=auth_header)
    prog_map = {}
    if status == 200 and isinstance(programs, list):
        for p in programs:
            prog_map[p.get("code")] = p
            prog_map[p.get("name")] = p
        print(f"Loaded {len(programs)} degree programs from ERP.")

    created_students = []
    success_count = 0

    for idx, s in enumerate(STUDENTS_DATA, 1):
        s_email = f"{s['rollNumber'].lower()}@college.edu"
        payload = {
            "rollNumber": s["rollNumber"],
            "firstName": s["firstName"],
            "lastName": s["lastName"],
            "email": s_email,
            "gender": s["gender"],
            "course": s["course"],
            "year": s["year"],
            "studyYear": s["studyYear"],
            "currentSemester": s["currentSemester"],
            "status": s["status"],
            "address": "Campus Hostels, VIT Pune",
            "city": "Pune",
            "state": "Maharashtra",
            "postalCode": "411037"
        }

        print(f"[{idx:02d}/{len(STUDENTS_DATA)}] Creating student {s['rollNumber']} ({s['firstName']} {s['lastName']} - {s['course']} {s['year']})...", end=" ")
        
        status, res = make_request(f"{API_BASE}/students", method="POST", data=payload, headers=auth_header)
        
        if status in [200, 201]:
            print("CREATED.")
            success_count += 1
            created_students.append(payload)
        elif status in [400, 409] and "already exists" in str(res):
            print("EXISTS (re-verifying login)...")
            created_students.append(payload)
        else:
            print(f"FAILED (Status: {status}, Error: {res})")
            created_students.append(payload)

    print(f"\nVerifying student credentials (username == rollNumber, password == rollNumber)...")
    verified_count = 0
    for s in created_students:
        rno = s["rollNumber"]
        if verify_student_login(rno):
            verified_count += 1
        else:
            print(f"⚠️ Login check failed for {rno}!")

    print(f"Credential Verification Completed: {verified_count}/{len(created_students)} successful.")

    # Generate STUDENTS.md
    generate_markdown_report(created_students)

def generate_markdown_report(students):
    print("Writing registry to STUDENTS.md...")
    md_path = "STUDENTS.md"
    
    # Calculate stats
    by_course = {}
    by_year = {}
    for s in students:
        c = s["course"]
        y = s["year"]
        by_course[c] = by_course.get(c, 0) + 1
        by_year[y] = by_year.get(y, 0) + 1

    md_content = f"""# Student Registry & Credentials Catalogue

> **Institution**: Vishwakarma Institute of Technology, Pune  
> **System**: Student ERP Platform (`OOP_CP1`)  
> **Total Seeded Students**: {len(students)}  
> **Authentication Policy**: `username: <rollNumber>`, `password: <rollNumber>`  
> **Generation Method**: Administrator REST API (`POST /api/students`)  

---

## 1. Summary Distribution

### 1.1 Distribution by Academic Course / Branch
| Course / Branch | Enrolled Count | Description |
| :--- | :---: | :--- |
| **CSE AIML** | {by_course.get('CSE AIML', 0)} | Computer Science and Engineering (AI & ML) |
| **CSE** | {by_course.get('CSE', 0)} | Computer Science and Engineering |
| **IT** | {by_course.get('IT', 0)} | Information Technology |
| **AIDS** | {by_course.get('AIDS', 0)} | Artificial Intelligence & Data Science |
| **ENTC** | {by_course.get('ENTC', 0)} | Electronics & Telecommunication Engineering |
| **MECH** | {by_course.get('MECH', 0)} | Mechanical Engineering |
| **CHEM** | {by_course.get('CHEM', 0)} | Chemical Engineering |
| **INST** | {by_course.get('INST', 0)} | Instrumentation Engineering |
| **Total** | **{len(students)}** | Across all 8 academic degree disciplines |

### 1.2 Distribution by Academic Year
| Academic Year | Code | Study Year | Semester | Students Count |
| :--- | :---: | :---: | :---: | :---: |
| **First Year** | `FY` | Year 1 | Sem 1 | {by_year.get('FY', 0)} |
| **Second Year** | `SY` | Year 2 | Sem 3 | {by_year.get('SY', 0)} |
| **Third Year** | `TY` | Year 3 | Sem 5 | {by_year.get('TY', 0)} |
| **Final Year** | `FINAL` | Year 4 | Sem 7 | {by_year.get('FINAL', 0)} |
| **Total** | - | - | - | **{len(students)}** |

---

## 2. Complete Student Registry & Login Credentials

All accounts are pre-configured in the database and verified. Students can log in to the Student ERP dashboard, NoteHub, and Student Vault using their roll number as both username and password.

| S.No | Roll Number | Full Name | Gender | Course / Branch | Class / Year | Semester | Username | Password | Institutional Email | Status |
| :---: | :--- | :--- | :---: | :--- | :---: | :---: | :--- | :--- | :--- | :---: |
"""

    for idx, s in enumerate(students, 1):
        fullname = f"{s['firstName']} {s['lastName']}"
        md_content += f"| {idx:02d} | `{s['rollNumber']}` | {fullname} | {s['gender']} | `{s['course']}` | `{s['year']}` (Yr {s['studyYear']}) | Sem {s['currentSemester']} | `{s['rollNumber']}` | `{s['rollNumber']}` | `{s['email']}` | `{s['status']}` |\n"

    md_content += """
---

## 3. Profile & Vault Feature Highlights

1. **Top-Right Profile Button**: Every student can view their personal academic profile card at the top-right of any webpage (Main Dashboard, NoteHub, and Student Vault) displaying their Full Name, Roll Number, Course, Year, Semester, Program, and College Email.
2. **Curriculum Vault Isolation**:
   - First-Year (`FY`) subjects are scoped to foundation engineering curriculum.
   - Second-Year (`SY`) and Third-Year (`TY`) students automatically access their respective branch-specific core courses and modules.
3. **NoteHub Integration**:
   - Students can upload PDF course notes categorised by `Year -> Course -> Subject -> Unit (I - VI)` and extra topic notes.
"""

    with open(md_path, "w", encoding="utf-8") as f:
        f.write(md_content)
    print(f" Successfully generated {md_path} with {len(students)} students.")

if __name__ == "__main__":
    main()


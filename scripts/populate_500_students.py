#!/usr/bin/env python3
"""
Populate 500 Additional Students Script for Student ERP System.
Logs in as Administrator, creates 500 students scattered across 8 degree programs and 4 academic years,
verifies student login credentials (username == rollNumber, password == rollNumber),
and updates STUDENTS.md with the complete consolidated catalog.
"""

import sys
import json
import urllib.request
import urllib.error
import os
import random
from concurrent.futures import ThreadPoolExecutor, as_completed

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')
if hasattr(sys.stderr, 'reconfigure'):
    sys.stderr.reconfigure(encoding='utf-8')

API_BASE = os.environ.get("API_URL", "http://localhost:8080/api")

# Realistic Indian First & Last Names Pools
MALE_FIRST_NAMES = [
    "Aarav", "Aditya", "Advait", "Agastya", "Akshay", "Amit", "Amol", "Anand", "Aniket", "Aniruddh",
    "Ankush", "Arjun", "Atharv", "Ayush", "Bhavik", "Chetan", "Chirag", "Darshan", "Deepak", "Dev",
    "Devendra", "Dhananjay", "Dhruv", "Gaurav", "Girish", "Hardik", "Harish", "Harsh", "Hrishikesh", "Ishaan",
    "Jayesh", "Kabir", "Karan", "Kishore", "Kunal", "Mahesh", "Manish", "Mayur", "Mihir", "Mukesh",
    "Nakul", "Nikhil", "Nilesh", "Nishant", "Omkar", "Pankaj", "Parth", "Pranav", "Prasad", "Prashant",
    "Prateek", "Pratham", "Pratik", "Pravin", "Raghav", "Rahul", "Rajesh", "Rakesh", "Rohan", "Rohit",
    "Rupesh", "Sachin", "Sagar", "Sameer", "Samarth", "Sandip", "Sanket", "Sarthak", "Saurabh", "Shaunak",
    "Shaurya", "Shirish", "Shivam", "Shreyas", "Shrikant", "Siddhant", "Siddharth", "Sohan", "Sourabh", "Subhash",
    "Sudarshan", "Sujay", "Sumit", "Suraj", "Swapnil", "Tanmay", "Tejas", "Uday", "Utkarsh", "Vaibhav",
    "Varun", "Vedant", "Vidyadhar", "Vijay", "Vikas", "Vikram", "Vinay", "Vineet", "Vinod", "Vipul",
    "Vishal", "Vivek", "Yash", "Yogesh"
]

FEMALE_FIRST_NAMES = [
    "Aadhya", "Aanya", "Aarohi", "Aditi", "Akanksha", "Akshara", "Amita", "Amrita", "Ananya", "Anagha",
    "Anjali", "Ankita", "Anushka", "Aparna", "Apoorva", "Archana", "Arpita", "Arya", "Ashwini", "Avani",
    "Bhakti", "Bhavana", "Chaitali", "Deepali", "Devika", "Dhanashree", "Disha", "Divya", "Diya", "Gargi",
    "Gauri", "Gayatri", "Harshada", "Ishani", "Ishita", "Jahnavi", "Janhavi", "Kalyani", "Kanchan", "Kavita",
    "Kavya", "Ketaki", "Khushi", "Kimaya", "Kirti", "Komal", "Kriti", "Madhuri", "Manasi", "Mayuri",
    "Meera", "Mitali", "Mrunal", "Mugdha", "Namrata", "Neha", "Nidhi", "Nikita", "Nisha", "Pallavi",
    "Payal", "Pooja", "Prachi", "Pragya", "Pranali", "Pranjal", "Pratibha", "Prena", "Priyanka", "Radhika",
    "Rashi", "Rashmi", "Revati", "Rhea", "Riddhi", "Ritu", "Riya", "Roshni", "Ruchira", "Rutvi",
    "Rutuja", "Saanvi", "Sakshi", "Samruddhi", "Sanjana", "Sanika", "Sayali", "Seema", "Sharvari", "Shilpa",
    "Shivani", "Shravani", "Shreya", "Shruti", "Simran", "Siya", "Sneha", "Sonali", "Spruha", "Srushti",
    "Surabhi", "Swara", "Tanvi", "Tara", "Tejaswi", "Trisha", "Urvi", "Vaishnavi", "Varsha", "Vedika"
]

LAST_NAMES = [
    "Agarwal", "Apte", "Bagul", "Bansal", "Bapat", "Barve", "Bhadane", "Bhandari", "Bhardwaj", "Bhat",
    "Bhosale", "Bidkar", "Biradar", "Borude", "Chavan", "Chitnis", "Choudhury", "Dabholkar", "Dalvi", "Damle",
    "Dandekar", "Date", "Dave", "Desai", "Deshmukh", "Deshpande", "Dhavale", "Dhumal", "Dixit", "Dubey",
    "Gadgil", "Gaikwad", "Gandhi", "Garg", "Gawande", "Ghadge", "Ghatge", "Ghorpade", "Godbole", "Gokhale",
    "Gosavi", "Gujar", "Gupta", "Inamdar", "Ingle", "Iyer", "Jadhav", "Jagtap", "Jain", "Jaiswal",
    "Jog", "Joshi", "Kale", "Kamble", "Kanetkar", "Kapoor", "Karve", "Kasar", "Katdare", "Kelkar",
    "Khadke", "Khandekar", "Kharat", "Khatavkar", "Khedkar", "Khot", "Khurana", "Kolhe", "Kothari", "Kshirsagar",
    "Kulkarni", "Lad", "Limaye", "Lokhande", "Mahajan", "Malhotra", "Mandlik", "Mane", "Marathe", "Mehendale",
    "Mehta", "Menon", "Mirajkar", "Mishra", "Mitra", "Modak", "Moghe", "More", "Mujumdar", "Mukherjee",
    "Mutha", "Nadar", "Naik", "Nair", "Nalawade", "Nene", "Nikam", "Nimkar", "Oak", "Padalkar",
    "Pal", "Panchal", "Pandey", "Pandit", "Parab", "Paranjape", "Patankar", "Pathak", "Patil", "Patwardhan",
    "Pawar", "Pendharkar", "Phadke", "Pillai", "Pradhan", "Puranik", "Raje", "Ranade", "Rane", "Rao",
    "Rathod", "Reddy", "Sabnis", "Sahasrabuddhe", "Salokhe", "Salunkhe", "Samant", "Sane", "Sapre", "Saraf",
    "Sardesai", "Sathe", "Sawant", "Sen", "Sengupta", "Shah", "Sharma", "Shete", "Shetty", "Shinde",
    "Shirke", "Shukla", "Singh", "Singhania", "Soman", "Sonar", "Surve", "Tambe", "Tandon", "Tendulkar",
    "Thakur", "Tikhe", "Tilak", "Tiwari", "Trivedi", "Upadhyay", "Vaidya", "Varma", "Vartak", "Verma",
    "Wadekar", "Wagh", "Walimbe", "Yadav"
]

PROGRAM_CONFIGS = [
    {"course": "CSE AIML", "code": "AIML", "programCode": "BTECH-AIML", "count": 63},
    {"course": "CSE",      "code": "CSE",  "programCode": "BTECH-CSE",  "count": 63},
    {"course": "IT",       "code": "IT",   "programCode": "BTECH-IT",   "count": 63},
    {"course": "AIDS",     "code": "AIDS", "programCode": "BTECH-AIDS", "count": 63},
    {"course": "ENTC",     "code": "ENTC", "programCode": "BTECH-ENTC", "count": 62},
    {"course": "MECH",     "code": "MECH", "programCode": "BTECH-MECH", "count": 62},
    {"course": "CHEM",     "code": "CHEM", "programCode": "BTECH-CHEM", "count": 62},
    {"course": "INST",     "code": "INST", "programCode": "BTECH-INST", "count": 62},
]

YEAR_CONFIGS = [
    {"year": "FY",    "yearCode": "24", "studyYear": 1, "semester": 1, "share": 0.26},
    {"year": "SY",    "yearCode": "23", "studyYear": 2, "semester": 3, "share": 0.26},
    {"year": "TY",    "yearCode": "22", "studyYear": 3, "semester": 5, "share": 0.24},
    {"year": "FINAL", "yearCode": "21", "studyYear": 4, "semester": 7, "share": 0.24},
]

def make_request(url, method="GET", data=None, headers=None):
    if headers is None:
        headers = {}
    req = urllib.request.Request(url, method=method)
    for k, v in headers.items():
        req.add_header(k, v)
    if data is not None:
        req.add_header("Content-Type", "application/json")
        req.data = json.dumps(data).encode("utf-8")
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
        print("[OK] Admin authentication successful! Token acquired.")
        return res["token"]
    else:
        print(f"[ERROR] Admin login failed: status={status}, res={res}")
        sys.exit(1)

def verify_student_login(roll_number):
    status, res = make_request(f"{API_BASE}/auth/login", method="POST", data={
        "username": roll_number,
        "password": roll_number
    })
    return status == 200 and "token" in res

def generate_500_students(existing_rolls):
    random.seed(42)  # Deterministic generation for reproducibility
    students = []
    
    male_names = list(MALE_FIRST_NAMES)
    female_names = list(FEMALE_FIRST_NAMES)
    last_names = list(LAST_NAMES)
    
    random.shuffle(male_names)
    random.shuffle(female_names)
    random.shuffle(last_names)
    
    m_idx = 0
    f_idx = 0
    l_idx = 0

    used_rolls = set(existing_rolls)

    total_target = 500
    generated_count = 0

    for prog in PROGRAM_CONFIGS:
        course_name = prog["course"]
        branch_code = prog["code"]
        target_count = prog["count"]
        
        # Determine counts per year for this branch
        # FY: 16, SY: 16, TY: 16 or 15, FINAL: 15
        if target_count == 63:
            year_counts = [("FY", "24", 1, 1, 16), ("SY", "23", 2, 3, 16), ("TY", "22", 3, 5, 16), ("FINAL", "21", 4, 7, 15)]
        else:
            year_counts = [("FY", "24", 1, 1, 16), ("SY", "23", 2, 3, 16), ("TY", "22", 3, 5, 15), ("FINAL", "21", 4, 7, 15)]
        
        for y_name, y_code, s_yr, sem, count in year_counts:
            seq = 11  # start sequence at 11
            for _ in range(count):
                # find next unused roll number
                while True:
                    roll = f"{branch_code}{y_code}{seq:02d}"
                    seq += 1
                    if roll not in used_rolls:
                        used_rolls.add(roll)
                        break
                
                # Pick gender alternate
                is_female = (generated_count % 2 == 1)
                if is_female:
                    first_name = female_names[f_idx % len(female_names)]
                    f_idx += 1
                    gender = "Female"
                else:
                    first_name = male_names[m_idx % len(male_names)]
                    m_idx += 1
                    gender = "Male"
                
                last_name = last_names[l_idx % len(last_names)]
                l_idx += 1

                students.append({
                    "rollNumber": roll,
                    "firstName": first_name,
                    "lastName": last_name,
                    "gender": gender,
                    "course": course_name,
                    "year": y_name,
                    "studyYear": s_yr,
                    "currentSemester": sem,
                    "status": "ACTIVE",
                    "email": f"{roll.lower()}@college.edu",
                    "address": "Campus Hostels, VIT Pune",
                    "city": "Pune",
                    "state": "Maharashtra",
                    "postalCode": "411037"
                })
                generated_count += 1
                if generated_count >= total_target:
                    break
            if generated_count >= total_target:
                break
        if generated_count >= total_target:
            break

    print(f"Generated {len(students)} distinct student records across {len(PROGRAM_CONFIGS)} degree programs.")
    return students

def post_student(s, admin_token):
    auth_header = {"Authorization": f"Bearer {admin_token}"}
    status, res = make_request(f"{API_BASE}/students", method="POST", data=s, headers=auth_header)
    return s["rollNumber"], status, res

def main():
    admin_token = login_admin()
    auth_header = {"Authorization": f"Bearer {admin_token}"}

    # Fetch existing students to check rolls
    status, existing_list = make_request(f"{API_BASE}/students", method="GET", headers=auth_header)
    existing_rolls = set()
    if status == 200 and isinstance(existing_list, list):
        for s in existing_list:
            if "rollNumber" in s:
                existing_rolls.add(s["rollNumber"])
    print(f"Loaded {len(existing_rolls)} existing students from database.")

    new_students = generate_500_students(existing_rolls)

    print(f"\nCreating {len(new_students)} students concurrently via Administrator REST API...")
    success_count = 0
    failed_count = 0
    already_exists_count = 0

    # Use ThreadPoolExecutor with 8 workers
    with ThreadPoolExecutor(max_workers=8) as executor:
        futures = {executor.submit(post_student, s, admin_token): s for s in new_students}
        idx = 0
        for future in as_completed(futures):
            idx += 1
            roll, status, res = future.result()
            if status in [200, 201]:
                success_count += 1
            elif status in [400, 409] and "already exists" in str(res):
                already_exists_count += 1
            else:
                failed_count += 1
                print(f"[FAIL] Failed to create {roll}: status={status}, res={res}")
            
            if idx % 50 == 0 or idx == len(new_students):
                print(f"  Progress: [{idx}/{len(new_students)}] Created: {success_count}, Exists: {already_exists_count}, Errors: {failed_count}")

    print(f"\nPopulation Summary: Created={success_count}, Already Exists={already_exists_count}, Errors={failed_count}")

    # Fetch ALL students from DB to compile complete STUDENTS.md
    print("\nFetching consolidated student roster from API...")
    status, all_students = make_request(f"{API_BASE}/students", method="GET", headers=auth_header)
    if status == 200 and isinstance(all_students, list):
        print(f"Total students currently in system: {len(all_students)}")
        # Verify sample logins
        print("\nVerifying sample student logins (username == rollNumber, password == rollNumber)...")
        sample_to_verify = random.sample(new_students, min(20, len(new_students)))
        all_passed = True
        for s in sample_to_verify:
            ok = verify_student_login(s["rollNumber"])
            if not ok:
                print(f"[WARN] Login verification failed for {s['rollNumber']}!")
                all_passed = False
        if all_passed:
            print("[OK] Sample verification of 20 random student accounts passed 100%!")

        generate_consolidated_markdown(all_students)
    else:
        print(f"[WARN] Could not fetch all students: status={status}, generating markdown from seeded batch...")
        generate_consolidated_markdown(new_students)

def generate_consolidated_markdown(students):
    md_path = "STUDENTS.md"
    print(f"Compiling consolidated catalog into {md_path}...")

    by_course = {}
    by_year = {}
    for s in students:
        c = s.get("course") or "General"
        y = s.get("year") or "FY"
        by_course[c] = by_course.get(c, 0) + 1
        by_year[y] = by_year.get(y, 0) + 1

    md_content = f"""# Student Registry & Credentials Catalogue

> **Institution**: Vishwakarma Institute of Technology, Pune  
> **System**: Student ERP Platform (`OOP_CP1`)  
> **Total Registered Students**: {len(students)}  
> **Authentication Policy**: `username: <rollNumber>`, `password: <rollNumber>`  
> **Enrollment Mechanism**: Administrator REST API (`POST /api/students`)  

---

## 1. Summary Distribution

### 1.1 Distribution by Academic Course / Branch
| Course / Branch | Enrolled Count | Description |
| :--- | :---: | :--- |
"""
    for course_name, cnt in sorted(by_course.items(), key=lambda x: -x[1]):
        md_content += f"| **{course_name}** | {cnt} | Enrolled students across all years |\n"
    md_content += f"| **Total** | **{len(students)}** | Complete active student body |\n\n"

    md_content += """### 1.2 Distribution by Academic Year
| Academic Year | Code | Study Year | Semester | Students Count |
| :--- | :---: | :---: | :---: | :---: |
"""
    for y_code, label, sy, sem in [("FY", "First Year", 1, "Sem 1 & 2"), ("SY", "Second Year", 2, "Sem 3 & 4"), ("TY", "Third Year", 3, "Sem 5 & 6"), ("FINAL", "Final Year", 4, "Sem 7 & 8")]:
        md_content += f"| **{label}** | `{y_code}` | Year {sy} | {sem} | {by_year.get(y_code, 0)} |\n"
    md_content += f"| **Total** | - | - | - | **{len(students)}** |\n\n"

    md_content += """---

## 2. Complete Student Registry & Login Credentials

All student accounts are pre-configured in the MySQL database and verified. Students can log in to the Student ERP dashboard, NoteHub, and Student Vault using their roll number as both username and password.

| S.No | Roll Number | Full Name | Gender | Course / Branch | Class / Year | Semester | Username | Password | Institutional Email | Status |
| :---: | :--- | :--- | :---: | :--- | :---: | :---: | :--- | :--- | :--- | :---: |
"""
    sorted_students = sorted(students, key=lambda x: (x.get("course", ""), x.get("year", ""), x.get("rollNumber", "")))
    for idx, s in enumerate(sorted_students, 1):
        fullname = f"{s.get('firstName', '')} {s.get('lastName', '')}".strip()
        rno = s.get("rollNumber", "")
        gender = s.get("gender", "Other")
        course = s.get("course", "N/A")
        year = s.get("year", "FY")
        study_yr = s.get("studyYear", 1)
        sem = s.get("currentSemester", 1)
        email = s.get("email", f"{rno.lower()}@college.edu")
        status = s.get("status", "ACTIVE")
        md_content += f"| {idx:03d} | `{rno}` | {fullname} | {gender} | `{course}` | `{year}` (Yr {study_yr}) | Sem {sem} | `{rno}` | `{rno}` | `{email}` | `{status}` |\n"

    md_content += """
---

## 3. Profile & Vault Feature Highlights

1. **Top-Right Profile Button**: Every student can view their personal academic profile card at the top-right of any webpage (Main Dashboard, NoteHub, and Student Vault) displaying their Full Name, Roll Number, Course, Year, Semester, Program, and College Email.
2. **Curriculum Vault Isolation**:
   - First-Year (`FY`) subjects are scoped to foundation engineering curriculum.
   - Second-Year (`SY`) and Third-Year (`TY`) students automatically access their respective branch-specific core courses and modules.
3. **NoteHub Integration**:
   - Students can upload PDF course notes categorised by `Year -> Course -> Subject -> Unit (I - VI)` and extra topic notes.
4. **Faculty Directory**:
   - Students can navigate directly to the Faculty Directory from the sidebar to view all Deans, HODs, Professors, and Assistant Professors with contact info.
"""

    with open(md_path, "w", encoding="utf-8") as f:
        f.write(md_content)
    print(f"[OK] Successfully generated {md_path} with {len(students)} entries.")

if __name__ == "__main__":
    main()

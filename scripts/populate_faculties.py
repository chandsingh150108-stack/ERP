#!/usr/bin/env python3
"""
Populate Faculties Script for Student ERP System.
Assigns 1 faculty member for every 2 courses/subjects across all academic departments,
establishing the department hierarchy: Department HOD -> Professor -> Assistant Professor.
Also creates Central Leadership & Administration Faculties:
- Dean of Admissions
- Dean of Examination
- Dean of Student Affairs & Fees
- Dean of Academic Affairs
- Dean of Research & Development
- Chief Accounts & Fee Officer
- Scholarship & Financial Aid Officer
"""

import os
import sys
import json
import math
import urllib.request
import urllib.error

API_BASE = os.environ.get("API_URL", "http://localhost:8080/api")

FIRST_NAMES_MALE = [
    "Rajesh", "Sanjay", "Arvind", "Rahul", "Nitin", "Mahesh", "Deepak", "Suresh",
    "Sandeep", "Ajay", "Vikas", "Hemant", "Shrikant", "Manoj", "Pravin", "Vinod",
    "Prasad", "Ganesh", "Vivek", "Ravindra", "Satish", "Jagdish", "Kiran", "Santosh",
    "Milind", "Anand", "Rajendra", "Jayant", "Sudhir", "Prashant", "Shailesh",
    "Chandrashekhar", "Harish", "Narendra", "Avinash", "Sunil", "Pramod", "Vijay",
    "Abhay", "Jitendra", "Girish", "Amol", "Sachin", "Tushar", "Bhushan", "Yogesh",
    "Pradeep", "Dhananjay", "Shantanu", "Makarand", "Sameer", "Devendra", "Ashok"
]

FIRST_NAMES_FEMALE = [
    "Sunita", "Preeti", "Meenakshi", "Smita", "Pooja", "Vandana", "Anjali", "Shilpa",
    "Manisha", "Kavita", "Neha", "Swati", "Rohini", "Archana", "Rekha", "Pallavi",
    "Ashwini", "Supriya", "Leena", "Chitra", "Madhuri", "Bhavana", "Sangeeta", "Deepali",
    "Alka", "Seema", "Urmila", "Shubhangi", "Surekha", "Pratibha", "Varsha", "Rashmi",
    "Vidya", "Jayshree", "Sarita", "Manjiri", "Sucheta", "Mangala", "Bharati", "Anita",
    "Aparna", "Geeta", "Madhura", "Sayali", "Radhika", "Namrata", "Renuka", "Tanvi"
]

LAST_NAMES = [
    "Kulkarni", "Deshmukh", "Joshi", "Patil", "Sharma", "Shinde", "Gaikwad", "Pawar",
    "Jadhav", "Chavan", "More", "Bhosale", "Mane", "Kamble", "Tambe", "Wagh",
    "Salunkhe", "Sonar", "Ghatge", "Sawant", "Tiwari", "Mehta", "Iyer", "Nair",
    "Reddy", "Chopra", "Gupta", "Rao", "Kapoor", "Bansal", "Mishra", "Saxena",
    "Trivedi", "Bhat", "Singhania", "Khurana", "Agarwal", "Malhotra", "Pillai",
    "Tandon", "Kaur", "Choudhury", "Sengupta", "Bhardwaj", "Menon", "Pandey",
    "Dubey", "Kadam", "Bapat", "Gokhale", "Apte", "Kare", "Puranik", "Datar"
]

ADMIN_FACULTIES = [
    {
        "employeeNumber": "ADM001",
        "firstName": "Arvind",
        "lastName": "Joshi",
        "designation": "Dean of Admissions",
        "phone": "+91 98220 10001",
        "email": "dean.admissions@vit.edu",
        "specialization": "Admissions Strategy, Seat Allocation & Eligibility Verification",
        "assignedSubjects": "Central Admissions Portfolio; MHT-CET & JEE Seat Allocation Matrix",
        "deptCode": "ADMIN"
    },
    {
        "employeeNumber": "ADM002",
        "firstName": "Meenakshi",
        "lastName": "Sundaram",
        "designation": "Dean of Examination",
        "phone": "+91 98220 10002",
        "email": "dean.exam@vit.edu",
        "specialization": "Examination Governance, Continuous Assessment & Result Evaluation",
        "assignedSubjects": "Autonomous Examination Cell; Grading Matrix & Transcript Certification",
        "deptCode": "ADMIN"
    },
    {
        "employeeNumber": "ADM003",
        "firstName": "Sanjay",
        "lastName": "Deshpande",
        "designation": "Dean of Student Affairs & Fees",
        "phone": "+91 98220 10003",
        "email": "dean.studentaffairs@vit.edu",
        "specialization": "Student Welfare, Fee Regulation, Discipline & Campus Grievances",
        "assignedSubjects": "Student Affairs Directorate; Fee Structure Regulatory Compliance",
        "deptCode": "ADMIN"
    },
    {
        "employeeNumber": "ADM004",
        "firstName": "Suresh",
        "lastName": "Shinde",
        "designation": "Dean of Academic Affairs",
        "phone": "+91 98220 10004",
        "email": "dean.academics@vit.edu",
        "specialization": "Academic Governance, NEP 2020 Implementation & Curriculum Review",
        "assignedSubjects": "Institute Academic Board; Autonomous Curriculum Architecture",
        "deptCode": "ADMIN"
    },
    {
        "employeeNumber": "ADM005",
        "firstName": "Nitin",
        "lastName": "Kulkarni",
        "designation": "Dean of Research & Development",
        "phone": "+91 98220 10005",
        "email": "dean.research@vit.edu",
        "specialization": "Sponsored Research, Patents, Innovations & Industry Collaborations",
        "assignedSubjects": "R&D Advisory Council; Intellectual Property & Patent Cell",
        "deptCode": "ADMIN"
    },
    {
        "employeeNumber": "ADM006",
        "firstName": "Ramesh",
        "lastName": "Patil",
        "designation": "Chief Accounts & Fee Officer",
        "phone": "+91 98220 10006",
        "email": "fees.accounts@vit.edu",
        "specialization": "Tuition Fee Billing, Hostel Dues, Challans & Refund Processing",
        "assignedSubjects": "Finance & Accounts Office; Student Fee Portals & Online Collections",
        "deptCode": "ADMIN"
    },
    {
        "employeeNumber": "ADM007",
        "firstName": "Sunita",
        "lastName": "Rao",
        "designation": "Scholarship & Financial Aid Officer",
        "phone": "+91 98220 10007",
        "email": "scholarships@vit.edu",
        "specialization": "EBC / TFWS / MahaDBT Government Scholarships & Fee Concessions",
        "assignedSubjects": "Institutional Financial Aid Office; State DBT Scholarship Cell",
        "deptCode": "ADMIN"
    }
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
    print("Authenticating as Administrator (admin / admin123)...")
    status, res = make_request(f"{API_BASE}/auth/login", method="POST", data={
        "username": "admin",
        "password": "admin123"
    })
    if status == 200 and "token" in res:
        print(" Admin token acquired.")
        return res["token"]
    else:
        print(f"❌ Admin login failed: {status} {res}")
        sys.exit(1)

def ensure_admin_department(auth_header):
    status, depts = make_request(f"{API_BASE}/departments", method="GET", headers=auth_header)
    dept_map = {}
    if status == 200 and isinstance(depts, list):
        for d in depts:
            dept_map[d.get("code")] = d

    if "ADMIN" not in dept_map:
        print("Creating Central Administration department...")
        st, res = make_request(f"{API_BASE}/departments", method="POST", data={
            "code": "ADMIN",
            "name": "Central Administration & Leadership",
            "description": "Institute Leadership, Deans, Examination and Finance Administration",
            "active": True
        }, headers=auth_header)
        if st in [200, 201]:
            dept_map["ADMIN"] = res
            print(" Department 'ADMIN' created successfully.")
        else:
            print(f"⚠️ Could not create ADMIN dept ({st}): {res}. Using DESH fallback.")
            dept_map["ADMIN"] = dept_map.get("DESH", list(dept_map.values())[0])
    return dept_map

def main():
    token = login_admin()
    auth_header = {"Authorization": f"Bearer {token}"}

    dept_map = ensure_admin_department(auth_header)

    print("Fetching existing courses from catalogue...")
    status, courses = make_request(f"{API_BASE}/courses", method="GET", headers=auth_header)
    if status != 200 or not isinstance(courses, list):
        print(f"❌ Failed to fetch courses: {status} {courses}")
        sys.exit(1)

    print(f"Loaded {len(courses)} courses.")

    # Group courses by department code
    courses_by_dept = {}
    for c in courses:
        dcode = c.get("department", {}).get("code", "CSE")
        courses_by_dept.setdefault(dcode, []).append(c)

    print("\n--- Courses by Department ---")
    for dcode, c_list in sorted(courses_by_dept.items()):
        print(f"  {dcode}: {len(c_list)} courses -> will produce {math.ceil(len(c_list)/2)} faculties (1 faculty per 2 subjects)")

    total_created = 0

    # 1. Populate Central Leadership & Deans
    print("\n--- [1/2] Populating Deans & Administration Faculties ---")
    admin_dept = dept_map.get("ADMIN", list(dept_map.values())[0])
    for f in ADMIN_FACULTIES:
        payload = {
            "employeeNumber": f["employeeNumber"],
            "firstName": f["firstName"],
            "lastName": f["lastName"],
            "designation": f["designation"],
            "phone": f["phone"],
            "email": f["email"],
            "specialization": f["specialization"],
            "assignedSubjects": f["assignedSubjects"],
            "employmentStatus": "ACTIVE",
            "department": {"id": admin_dept["id"]}
        }
        st, res = make_request(f"{API_BASE}/faculty", method="POST", data=payload, headers=auth_header)
        if st in [200, 201]:
            print(f" Created {f['designation']}: Dr./Prof. {f['firstName']} {f['lastName']} ({f['phone']} | {f['email']})")
            total_created += 1
        elif st in [400, 409] and "exists" in str(res):
            print(f" Exists {f['designation']}: {f['employeeNumber']}")
            total_created += 1
        else:
            print(f"❌ Error creating {f['employeeNumber']}: {st} {res}")

    # 2. Populate Department Faculties (1 faculty per 2 subjects)
    print("\n--- [2/2] Populating Department Faculties (1 Faculty for 2 Subjects) ---")
    name_idx = 0
    phone_counter = 12010

    for dcode, c_list in sorted(courses_by_dept.items()):
        dept_obj = dept_map.get(dcode)
        if not dept_obj:
            print(f"⚠️ Dept {dcode} not found in dept_map, skipping.")
            continue

        num_faculties = math.ceil(len(c_list) / 2)
        print(f"\nProcessing Department {dcode} ({len(c_list)} courses -> {num_faculties} faculties)...")

        # Hierarchy distribution:
        # Index 0: HOD (Professor & HOD)
        # Next ~20-25%: Professor
        # Rest: Assistant Professor
        num_profs = max(1, round(num_faculties * 0.22))

        for fac_idx in range(num_faculties):
            c1 = c_list[fac_idx * 2]
            c2 = c_list[fac_idx * 2 + 1] if (fac_idx * 2 + 1) < len(c_list) else None

            if c2:
                assigned_subs = f"{c1['code']}: {c1['name']} & {c2['code']}: {c2['name']}"
                spec = f"{c1['name']}, {c2['name']}"
            else:
                assigned_subs = f"{c1['code']}: {c1['name']}"
                spec = f"{c1['name']}"

            # Designation hierarchy
            if fac_idx == 0:
                designation = f"Professor & HOD"
            elif fac_idx <= num_profs:
                designation = "Professor"
            else:
                designation = "Assistant Professor"

            is_female = (name_idx % 3 == 0)
            if is_female:
                fname = FIRST_NAMES_FEMALE[name_idx % len(FIRST_NAMES_FEMALE)]
            else:
                fname = FIRST_NAMES_MALE[name_idx % len(FIRST_NAMES_MALE)]
            lname = LAST_NAMES[name_idx % len(LAST_NAMES)]
            name_idx += 1

            emp_no = f"FAC_{dcode}{fac_idx+1:02d}"
            phone = f"+91 98220 {phone_counter}"
            phone_counter += 1
            email = f"{fname.lower()}.{lname.lower()}.{dcode.lower()}{fac_idx+1:02d}@vit.edu"

            payload = {
                "employeeNumber": emp_no,
                "firstName": fname,
                "lastName": lname,
                "designation": designation,
                "phone": phone,
                "email": email,
                "specialization": spec,
                "assignedSubjects": assigned_subs,
                "employmentStatus": "ACTIVE",
                "department": {"id": dept_obj["id"]}
            }

            st, res = make_request(f"{API_BASE}/faculty", method="POST", data=payload, headers=auth_header)
            if st in [200, 201]:
                total_created += 1
                if fac_idx == 0 or fac_idx <= num_profs or fac_idx == num_faculties - 1:
                    print(f"  [{fac_idx+1:02d}/{num_faculties}] {emp_no} | {designation} {fname} {lname} | {phone} | {email}")
            elif st in [400, 409] and "exists" in str(res):
                total_created += 1
            else:
                print(f"[ERROR] Failed to create {emp_no}: {st} {res}")

    print(f"\n[OK] Faculty Population Complete! Total records processed: {total_created}")

    # Verify via GET /api/faculty
    print("Verifying via GET /api/faculty...")
    st, fac_list = make_request(f"{API_BASE}/faculty", method="GET", headers=auth_header)
    if st == 200 and isinstance(fac_list, list):
        print(f"[OK] Verified: {len(fac_list)} faculties returned by the API!")
    else:
        print(f"[WARN] Verification check returned status {st}: {fac_list}")

if __name__ == "__main__":
    main()

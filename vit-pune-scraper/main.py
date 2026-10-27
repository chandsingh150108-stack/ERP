#!/usr/bin/env python3
"""
VIT Pune Curriculum Scraper & ERP Seeder CLI
Extract institute-wide courses and curriculum from official VIT Pune PDFs and seed into Student ERP.
"""

import sys
import argparse
import json
from pathlib import Path
from tabulate import tabulate

# Configure standard output to utf-8 for cross-platform unicode safety
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

from scraper import VITCurriculumScraper
from seeder import ERPSeeder
from config import OUTPUT_JSON_PATH, OUTPUT_CSV_PATH, DEPARTMENTS


def print_banner():
    banner = r"""
========================================================================
     __   ___ _____   ____                  ____                                
     \ \ / / |_   _| |  _ \ _   _ _ __   ___ / ___|  ___ _ __ __ _ _ __   ___ _ __ 
      \ V /| | | |   | |_) | | | | '_ \ / _ \\___ \ / __| '__/ _` | '_ \ / _ \ '__|
       \_/ |_| |_|   |_|   \__,_|_| |_|\___||____/ \___|_|  \__,_| .__/ \___|_|   
                                                                   |_|              
           Automated Curriculum Scraper & ERP Seeder for VIT Pune
========================================================================
"""
    print(banner)


def show_stats(courses):
    total = len(courses)
    dept_counts = {}
    year_counts = {1: 0, 2: 0, 3: 0, 4: 0}
    type_counts = {}

    for c in courses:
        dept = c.get("departmentCode", "OTHER")
        dept_counts[dept] = dept_counts.get(dept, 0) + 1
        y = c.get("studyYear", 2)
        year_counts[y] = year_counts.get(y, 0) + 1
        t = c.get("courseType", "CORE")
        type_counts[t] = type_counts.get(t, 0) + 1

    print("\n[+] Total Courses Extracted:", total)

    dept_table = [[k, DEPARTMENTS.get(k, {}).get("name", "Unknown"), v] for k, v in sorted(dept_counts.items())]
    print("\n--- Breakdown by Department ---")
    print(tabulate(dept_table, headers=["Code", "Department Name", "Courses Count"], tablefmt="github"))

    year_table = [[f"Year {k} (Semester {2*k-1}-{2*k})", v] for k, v in sorted(year_counts.items())]
    print("\n--- Breakdown by Academic Study Year ---")
    print(tabulate(year_table, headers=["Academic Level", "Courses Count"], tablefmt="github"))

    type_table = [[k, v] for k, v in sorted(type_counts.items())]
    print("\n--- Breakdown by Course Category ---")
    print(tabulate(type_table, headers=["Category Type", "Courses Count"], tablefmt="github"))


def main():
    print_banner()

    parser = argparse.ArgumentParser(
        description="Scrape official VIT Pune syllabus PDFs and seed into Student ERP."
    )
    parser.add_argument("--scrape", action="store_true", help="Download and scrape syllabus PDFs into JSON/CSV.")
    parser.add_argument("--seed", action="store_true", help="Seed scraped curriculum into the ERP database.")
    parser.add_argument("--all", action="store_true", help="Execute full pipeline: download, scrape, export, and seed.")
    parser.add_argument("--stats", action="store_true", help="Display statistics of currently scraped curriculum dataset.")
    parser.add_argument("--dept", type=str, help="Filter by department code (e.g. CSE, IT, AIDS, MECH, DESH).")
    parser.add_argument("--force-download", action="store_true", help="Force re-downloading PDFs even if cached.")
    parser.add_argument("--erp-url", type=str, default="http://localhost:8080", help="Base URL of Student ERP backend.")
    parser.add_argument("--username", type=str, default="admin", help="ERP Admin username.")
    parser.add_argument("--password", type=str, default="admin123", help="ERP Admin password.")

    args = parser.parse_args()

    # Default action if no flags provided
    if not (args.scrape or args.seed or args.all or args.stats):
        if OUTPUT_JSON_PATH.exists():
            args.stats = True
        else:
            args.all = True

    scraper = VITCurriculumScraper()
    courses = []

    if args.scrape or args.all:
        print("\n[*] Starting curriculum extraction pipeline...")
        courses = scraper.extract_all(force_download=args.force_download, dept_filter=args.dept)
        scraper.export(courses)
        print(f"\n[+] Successfully saved {len(courses)} courses to:")
        print(f"    - JSON: {OUTPUT_JSON_PATH}")
        print(f"    - CSV:  {OUTPUT_CSV_PATH}")
        show_stats(courses)

    if args.stats and not (args.scrape or args.all):
        if not OUTPUT_JSON_PATH.exists():
            print(f"[-] No curriculum data found at {OUTPUT_JSON_PATH}. Run with --scrape first.")
            sys.exit(1)
        with open(OUTPUT_JSON_PATH, "r", encoding="utf-8") as f:
            data = json.load(f)
        courses = data.get("courses", [])
        show_stats(courses)

    if args.seed or args.all:
        print("\n[*] Starting ERP database synchronization...")
        seeder = ERPSeeder(base_url=args.erp_url, username=args.username, password=args.password)
        res = seeder.seed_curriculum(json_file=OUTPUT_JSON_PATH)
        if "error" in res:
            print(f"[-] Seeding failed: {res['error']}")
            sys.exit(1)
        else:
            print("\n=================== SEEDING SUMMARY ===================")
            summary_table = [
                ["Departments Active", res.get("departments", 0)],
                ["Programs Active", res.get("programs", 0)],
                ["Total Courses in ERP", res.get("courses_total", 0)],
                ["Courses Created", res.get("courses_created", 0)],
                ["Courses Updated", res.get("courses_updated", 0)],
                ["NoteHub Subjects Created", res.get("notehub_created", 0)],
                ["NoteHub Subjects Updated", res.get("notehub_updated", 0)]
            ]
            print(tabulate(summary_table, headers=["Metric", "Value"], tablefmt="github"))
            print("=======================================================\n")
            print("[SUCCESS] Curriculum synchronization complete!")


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
Grades the exercise battery from surefire's per-test XML reports.

Two modes, chosen by whether the PR_BODY env var is set:

- Template mode (PR_BODY is set, e.g. running in the PR workflow): the PR
  description's checklist (see .github/PULL_REQUEST_TEMPLATE.md) declares which
  exercises the student wants graded. A checked exercise must pass ALL of its
  tests to count as "Passed" -- any failure (including its stub still throwing
  UnsupportedOperationException, i.e. they forgot to implement it despite
  checking the box) fails CI. Unchecked exercises are skipped entirely, whether
  their tests pass or not.

- Auto-detect mode (PR_BODY is unset, e.g. running `mvn test` + this script
  locally outside of a PR): falls back to inferring "attempted" from whether a
  test's failure is the stub's UnsupportedOperationException marker or a real
  failure. Useful for a quick local sanity check, but the PR checklist is the
  source of truth in CI.

Exit code: 0 unless at least one graded exercise has a failure.
"""

import glob
import os
import re
import sys
import xml.etree.ElementTree as ET

REPORTS_GLOB = "target/surefire-reports/TEST-*.xml"

NOT_IMPLEMENTED_MARKERS = ("UnsupportedOperationException", "Ejercicio")

CHECKED_BOX_PATTERN = re.compile(r"-\s*\[[xX]\][^\n]*?Ejercicio\s+(\d+)")

# Maps a test class's simple name (the part after '$' for @Nested classes,
# or the whole simple class name for top-level DAO test classes) to the
# exercise number it exercises. Classes not listed here (e.g. tests for
# functionality that predates the exercise battery) are ignored.
CLASS_TO_EXERCISE = {
    "GetReservationsByUserTests": 1,
    "GetAveragePriceByCompanyTests": 2,
    "GetTopNMostExpensiveTests": 3,
    "ReservationDaoFileImplTest": 3,
    "GetFlightsSortedByPriceThenAirlineTests": 4,
    "GroupReservationsByCountryTests": 5,
    "GetValidatedUserTests": 6,
    "CreateReservationTests": 7,
    "AddReservationIfNotDuplicateTests": 8,
    "GetUsersPresentInFileAndDaoTests": 9,
    "FileUserDaoImplTest": 9,
    "ExportReservationsToCsvTests": 10,
    "ReservationExportDaoImplTest": 10,
    "GetUserReservationSummariesTests": 11,
    "ValidateReservationCandidatesTests": 12,
}

ALL_EXERCISES = sorted(set(CLASS_TO_EXERCISE.values()))


def simple_class_name(classname):
    return classname.split("$")[-1].split(".")[-1]


def classify(testcase):
    failure = testcase.find("failure")
    error = testcase.find("error")
    node = failure if failure is not None else error
    if node is None:
        return "PASSED"

    text = " ".join(filter(None, [node.get("message", ""), node.text or ""]))
    if all(marker in text for marker in NOT_IMPLEMENTED_MARKERS):
        return "NOT_IMPLEMENTED"
    return "FAILED"


def collect_results():
    results = {n: {"PASSED": 0, "NOT_IMPLEMENTED": 0, "FAILED": 0, "failed_tests": []} for n in ALL_EXERCISES}

    for report_path in sorted(glob.glob(REPORTS_GLOB)):
        tree = ET.parse(report_path)
        for testcase in tree.getroot().findall("testcase"):
            classname = testcase.get("classname", "")
            exercise = CLASS_TO_EXERCISE.get(simple_class_name(classname))
            if exercise is None:
                continue

            status = classify(testcase)
            results[exercise][status] += 1
            if status == "FAILED":
                results[exercise]["failed_tests"].append(f"{classname}#{testcase.get('name')}")

    return results


def parse_claimed_exercises(pr_body):
    claimed = set()
    unknown = set()
    for match in CHECKED_BOX_PATTERN.finditer(pr_body):
        number = int(match.group(1))
        if number in CLASS_TO_EXERCISE.values():
            claimed.add(number)
        else:
            unknown.add(number)
    return claimed, unknown


def build_table(results, mode, claimed=None):
    header = "| Exercise | Status | Passed | Not implemented | Failed |"
    lines = [header, "|---|---|---|---|---|"]
    failing = []

    for exercise in ALL_EXERCISES:
        r = results[exercise]
        total = r["PASSED"] + r["NOT_IMPLEMENTED"] + r["FAILED"]

        if mode == "template" and exercise not in claimed:
            status = "Not claimed (skipped)"
        elif r["FAILED"] > 0 or (mode == "template" and r["PASSED"] != total):
            status = "FAILED"
            failing.append(exercise)
        elif r["NOT_IMPLEMENTED"] == total:
            status = "Not attempted"
        elif r["PASSED"] == total:
            status = "Passed"
        else:
            status = "Partially implemented"

        lines.append(f"| Ejercicio {exercise} | {status} | {r['PASSED']} | {r['NOT_IMPLEMENTED']} | {r['FAILED']} |")

    return "\n".join(lines), failing


def main():
    reports = sorted(glob.glob(REPORTS_GLOB))
    if not reports:
        print("No surefire reports found under target/surefire-reports/. Did `mvn test` run?")
        return 1

    results = collect_results()
    pr_body = os.environ.get("PR_BODY")
    mode = "template" if pr_body is not None else "auto"

    claimed = None
    if mode == "template":
        claimed, unknown = parse_claimed_exercises(pr_body)
        if unknown:
            print(f"Warning: PR checklist references unknown exercise number(s): {sorted(unknown)}")
        if not claimed:
            print("No exercise checked off in the PR description -- nothing to grade.")

    table, failing = build_table(results, mode, claimed)
    print(f"Mode: {mode}" + (f" (claimed: {sorted(claimed)})" if claimed is not None else ""))
    print(table)

    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_path:
        with open(summary_path, "a") as f:
            f.write("## Exercise results\n\n")
            if mode == "template":
                f.write(f"Graded from the PR checklist. Claimed exercises: {sorted(claimed) or 'none'}\n\n")
            f.write(table)
            f.write("\n")

    if failing:
        print("\nThe following claimed/attempted exercises are failing:")
        for exercise in failing:
            for failed_test in results[exercise]["failed_tests"]:
                print(f"  - Ejercicio {exercise}: {failed_test}")
            if not results[exercise]["failed_tests"]:
                print(f"  - Ejercicio {exercise}: claimed as done but its tests are not all passing")
        return 1

    print("\nNo graded exercise is failing.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
